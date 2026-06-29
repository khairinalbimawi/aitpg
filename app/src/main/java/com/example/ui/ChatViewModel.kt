package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.AppDatabase
import com.example.data.ChatMessageEntity
import com.example.data.ChatRepository
import androidx.room.Room
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import com.example.network.Candidate
import com.example.network.Content
import com.example.network.GenerateContentRequest
import com.example.network.Part
import com.example.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: String,
    val text: String,
    val isFromUser: Boolean,
    val isError: Boolean = false
)

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val database = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "chat_database"
    ).build()
    
    private val repository = ChatRepository(database.chatMessageDao())

    private val _uiState = MutableStateFlow(ChatUiState())
    // Expose a combined state flow that listens to the DB
    val uiState: StateFlow<ChatUiState> = kotlinx.coroutines.flow.combine(
        repository.allMessages,
        _uiState
    ) { dbMessages, state ->
        val mappedMessages = dbMessages.map { 
            ChatMessage(it.id, it.text, it.isFromUser, it.isError)
        }
        state.copy(messages = mappedMessages + state.messages)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ChatUiState()
    )

    private val systemInstruction = """
        Anda adalah Asisten TPG (Tunjangan Profesi Guru) khusus untuk SMKPP Negeri Bima.
        Anda ahli Aturan Dapodik 2026.c, pencairan tunjangan, & Linieritas Mapel (Permendikbudristek 11/2025, Kepmen 221/P/2025, Kepmen 222/O/2025).
        Instruksi: Jawab seringkas mungkin, padat, dan akurat (hemat token). Berikan jawaban hanya berdasarkan regulasi terbaru tersebut.
        
        Pengetahuan Khusus:
        - Jabatan Guru Wali bernilai 2 Jam Pelajaran (Wajib untuk semua Guru).
        - Jika Guru Memiliki Jam 10 + Tugas Tambahan Utama 12 Jam + Guru Wali maka sertifikasinya TIDAK AKAN VALID, karena tidak memenuhi syarat tatap muka guru minimal 12 Jam.
        - Guru BK: Sertifikasi 24 Jam harus membimbing 5 Rombel. Jika ada Tugas Tambahan Utama 12 Jam, minimal 3 Rombel agar setara 24 Jam.
        - Guru Tunggal: Berapapun jam di sekolah, WAJIB ditambah Tugas Tambahan Linier Ekuivalensi (TTLE) maksimal 3 TTLE (Contoh: Wali Kelas, Pembina Ekskul, Ketua TPPK, dll). Catatan: "Guru wali" tidak dihitung sebagai TTLE.
    """.trimIndent()

    init {
        viewModelScope.launch {
            repository.allMessages.collect { msgs ->
                if (msgs.isEmpty()) {
                    repository.insertMessage(
                        ChatMessageEntity(
                            id = java.util.UUID.randomUUID().toString(),
                            text = "Halo! Saya Asisten TPG SMKPP Negeri Bima. Ada yang bisa saya bantu terkait Tunjangan Profesi Guru, pencairan, atau info linieritas mapel?",
                            isFromUser = false
                        )
                    )
                }
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearHistory()
            _uiState.update { it.copy(messages = emptyList()) }
        }
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        val userMessage = ChatMessageEntity(
            id = java.util.UUID.randomUUID().toString(),
            text = userText,
            isFromUser = true
        )

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            repository.insertMessage(userMessage)
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                
                val currentMessages = uiState.value.messages
                val history = currentMessages
                    .filter { !it.isError }
                    .drop(1)
                    .map { msg ->
                        Content(
                            role = if (msg.isFromUser) "user" else "model",
                            parts = listOf(Part(text = msg.text))
                        )
                    }

                // Add the latest user message to history, since it might not be emitted by Room yet
                val updatedHistory = history + Content(
                    role = "user",
                    parts = listOf(Part(text = userText))
                )

                val request = GenerateContentRequest(
                    contents = updatedHistory,
                    systemInstruction = Content(
                        role = "system",
                        parts = listOf(Part(text = systemInstruction))
                    )
                )

                val response = RetrofitClient.service.generateContent(apiKey, request)
                val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "Maaf, saya tidak bisa memproses permintaan Anda saat ini."

                val botMessage = ChatMessageEntity(
                    id = java.util.UUID.randomUUID().toString(),
                    text = responseText,
                    isFromUser = false
                )

                repository.insertMessage(botMessage)
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string() ?: e.message()
                val text = if (e.code() == 429) {
                    "Maaf, saat ini sistem sedang sibuk (Terlalu banyak permintaan). Mohon tunggu beberapa saat dan coba lagi."
                } else if (e.code() == 403) {
                    "API Key tidak valid atau telah diblokir (Error 403). Silakan perbarui GEMINI_API_KEY Anda di panel Secrets AI Studio dengan API Key yang baru."
                } else if (e.code() == 503) {
                    "Layanan API Gemini sedang tidak tersedia. Mohon coba lagi nanti."
                } else {
                    "Terjadi kesalahan HTTP ${e.code()}: $errorBody"
                }
                val errorMessage = ChatMessage(
                    id = java.util.UUID.randomUUID().toString(),
                    text = text,
                    isFromUser = false,
                    isError = true
                )
                _uiState.update {
                    it.copy(
                        messages = it.messages + errorMessage,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                val errorMessage = ChatMessage(
                    id = java.util.UUID.randomUUID().toString(),
                    text = "Terjadi kesalahan: ${e.message}",
                    isFromUser = false,
                    isError = true
                )
                _uiState.update {
                    it.copy(
                        messages = it.messages + errorMessage,
                        isLoading = false
                    )
                }
            }
        }
    }
}
