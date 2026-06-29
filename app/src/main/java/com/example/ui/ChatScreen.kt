package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.shadow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput

val DarkGreen = Color(0xFF1B5E20)
val MediumGreen = Color(0xFF2E7D32)
val LightBackground = Color(0xFFF8FAF9)
val Slate100 = Color(0xFFF1F5F9)
val Slate400 = Color(0xFF94A3B8)
val Slate900 = Color(0xFF0F172A)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var textState by remember { mutableStateOf(TextFieldValue("")) }
    val focusManager = LocalFocusManager.current

    // Scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(color = DarkGreen, shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .padding(top = 32.dp) // extra padding for status bar
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ASISTEN DIGITAL",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "Sertifikasi SMKPPN Bima",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(
                        onClick = { viewModel.clearChat() },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear Chat",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(text = "Dapodik v.2025.a", color = Color.White, fontSize = 11.sp)
                    }
                    Box(
                        modifier = Modifier
                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(text = "Permen 11/2025", color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        },
        containerColor = LightBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0) // handled manually
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(LightBackground)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                }
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.messages) { message ->
                    ChatMessageBubble(message)
                }
                
                // Quick Action Chips
                if (uiState.messages.size == 1 && !uiState.isLoading) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            QuickActionButton("Syarat Pencairan", viewModel)
                            QuickActionButton("Cek Kepmen 222/O", viewModel)
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            QuickActionButton("Validasi JJM", viewModel)
                            QuickActionButton("Aturan Linieritas", viewModel)
                        }
                    }
                }
                
                if (uiState.isLoading) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Slate100, CircleShape)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = textState,
                            onValueChange = { textState = it },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input"),
                            textStyle = LocalTextStyle.current.copy(
                                color = Slate900,
                                fontSize = 14.sp
                            ),
                            cursorBrush = SolidColor(DarkGreen),
                            decorationBox = { innerTextField ->
                                if (textState.text.isEmpty()) {
                                    Text(
                                        text = "Tanya tentang aturan baru...",
                                        color = Slate400,
                                        fontSize = 14.sp
                                    )
                                }
                                innerTextField()
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (textState.text.isNotBlank()) {
                                    viewModel.sendMessage(textState.text)
                                    textState = TextFieldValue("")
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .background(DarkGreen, CircleShape)
                                .testTag("send_button"),
                            enabled = textState.text.isNotBlank() && !uiState.isLoading
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Kirim",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tim Digitalisasi Sekolah dan Operator Dapodik SMKPP Negeri Bima",
                        color = Slate400,
                        fontSize = 9.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(text: String, viewModel: ChatViewModel) {
    Surface(
        onClick = { viewModel.sendMessage(text) },
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, MediumGreen.copy(alpha = 0.2f)),
        modifier = Modifier.padding(end = 4.dp)
    ) {
        Text(
            text = text,
            color = DarkGreen,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun ChatMessageBubble(message: ChatMessage) {
    val isUser = message.isFromUser
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val backgroundColor = if (isUser) {
        MediumGreen
    } else if (message.isError) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        Color.White
    }
    
    val textColor = if (isUser) {
        Color.White
    } else if (message.isError) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        Slate900
    }
    
    val timeString = remember {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
    }
    
    val clipboardManager = LocalClipboardManager.current

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 0.dp,
                            bottomEnd = if (isUser) 0.dp else 16.dp
                        )
                    )
                    .background(backgroundColor)
                    .then(
                        if (!isUser && !message.isError) {
                            Modifier.border(1.dp, Slate100, RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = 0.dp,
                                bottomEnd = 16.dp
                            ))
                        } else Modifier
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column {
                    Text(
                        text = message.text,
                        color = textColor,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                    if (!isUser && !message.isError) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(
                                onClick = { clipboardManager.setText(AnnotatedString(message.text)) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy text",
                                    tint = Slate400,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = timeString,
                color = Slate400,
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}
