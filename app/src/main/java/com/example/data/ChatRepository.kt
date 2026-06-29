package com.example.data

import kotlinx.coroutines.flow.Flow

class ChatRepository(private val dao: ChatMessageDao) {
    val allMessages: Flow<List<ChatMessageEntity>> = dao.getAllMessages()

    suspend fun insertMessage(message: ChatMessageEntity) {
        dao.insertMessage(message)
    }

    suspend fun clearHistory() {
        dao.deleteAllMessages()
    }
}
