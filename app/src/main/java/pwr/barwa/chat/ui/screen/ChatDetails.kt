package pwr.barwa.chat.ui.screen

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pwr.barwa.chat.ui.AppViewModelProvider
import pwr.barwa.chat.ui.ChatDetailsViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import kotlinx.coroutines.launch
import pwr.barwa.chat.data.SignalRConnector
import pwr.barwa.chat.data.dto.MessageDto
import pwr.barwa.chat.data.dto.MessageType
import pwr.barwa.chat.ui.screen.common.ChatAvatar
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailsScreen(
    chatId: Long,
    viewModel: ChatDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val chat by viewModel.selectedChat.collectAsState()
    val messages by viewModel.channelMessages.collectAsState()
    val users by viewModel.channelMembers.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    // Stan komponentu
    var text by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    val lazyListState = rememberLazyListState()

    // Przewijanie do najnowszej wiadomości przy zmianie listy
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            lazyListState.animateScrollToItem(messages.size - 1)
        }
    }

    // Launcher dla wyboru plików z galerii
    val mediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            handleMediaSelection(uri, context, viewModel)
        }
    }

    // Załaduj czat
    LaunchedEffect(chatId) {
        viewModel.loadChatById(chatId)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        chat?.let {
                            ChatAvatar(
                                chat = it,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = chat?.name ?: "Czat",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            if (users.isNotEmpty()) {
                                val statusText = if (users.size > 1) {
                                    "${users.size} uczestników"
                                } else {
                                    "Online" // Można zastąpić rzeczywistym statusem z API
                                }

                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    // Usunięto strzałkę powrotu
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Więcej opcji",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Informacje o czacie") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = { showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Wycisz powiadomienia") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = { showMenu = false }
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 2.dp,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pole do wpisywania wiadomości
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        placeholder = { Text("Wiadomość") },
                        shape = RoundedCornerShape(24.dp),
                        trailingIcon = {
                            IconButton(onClick = { mediaLauncher.launch("*/*") }) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Załącz",
                                    modifier = Modifier.scale(0.9f),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )

                    // Przycisk wysyłania
                    IconButton(
                        onClick = {
                            if (text.isNotBlank()) {
                                viewModel.sendMessage(text)
                                text = ""
                            }
                        },
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Wyślij",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.scale(1.2f).size(28.dp)
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 8.dp)
        ) {
            // Lista wiadomości - sortowanie od najstarszych do najnowszych
            LazyColumn(
                modifier = Modifier.weight(1f),
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
            ) {
                val sortedMessages = messages.sortedBy { it.created }

                items(sortedMessages) { message ->
                    val currentUser = users.find { it.id == message.senderId }
                    val isCurrentUser = message.senderId == viewModel.getCurrentUser()?.id
                    val alignment = if (isCurrentUser) Arrangement.End else Arrangement.Start
                    val bubbleColor = if (isCurrentUser)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                    val textColor = if (isCurrentUser)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
                    ) {
                        MessageBubble(
                            message = message,
                            bubbleColor = bubbleColor,
                            textColor = textColor,
                            userName = currentUser?.userName ?: "Użytkownik ${message.senderId}",
                            isCurrentUser = isCurrentUser
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: MessageDto,
    bubbleColor: Color,
    textColor: Color,
    userName: String,
    isCurrentUser: Boolean
) {
    val bubbleShape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (isCurrentUser) 16.dp else 4.dp,
        bottomEnd = if (isCurrentUser) 4.dp else 16.dp
    )

    Column(
        modifier = Modifier
            .widthIn(max = 280.dp)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        // Nazwa użytkownika (tylko dla innych użytkowników)
        if (!isCurrentUser) {
            Text(
                text = userName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
            )
        }

        // Treść wiadomości
        Card(
            shape = bubbleShape,
            colors = CardDefaults.cardColors(
                containerColor = bubbleColor
            )
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                when (message.type) {
                    MessageType.TEXT -> {
                        Text(
                            text = message.data,
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor
                        )
                    }
                    MessageType.IMAGE -> {
                        Column {
                            Text(
                                text = "🖼️ Obraz",
                                style = MaterialTheme.typography.bodyMedium,
                                color = textColor
                            )
                        }
                    }
                    MessageType.VIDEO -> {
                        Text(
                            text = "🎥 Wideo",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor
                        )
                    }
                    MessageType.AUDIO -> {
                        Text(
                            text = "🎵 Audio",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor
                        )
                    }
                    else -> {
                        Text(
                            text = message.data,
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor
                        )
                    }
                }

                // Czas wiadomości
                Text(
                    text = formatMessageTime(message.created),
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.7f),
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp)
                )
            }
        }
    }
}

// Helper function to format message timestamp
private fun formatMessageTime(timestamp: Long): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
}

// Funkcja pomocnicza do obsługi wybranych mediów
private fun handleMediaSelection(uri: Uri, context: Context, viewModel: ChatDetailsViewModel) {
    try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val outputStream = ByteArrayOutputStream()

        inputStream?.use { input ->
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
        }

        val byteArray = outputStream.toByteArray()
        val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
        val extension = when {
            mimeType.startsWith("image/") -> "jpg"
            mimeType.startsWith("video/") -> "mp4"
            mimeType.startsWith("audio/") -> "mp3"
            else -> "bin"
        }

        // Określ typ wiadomości na podstawie MIME type
        val messageType = when {
            mimeType.startsWith("image/") -> MessageType.IMAGE
            mimeType.startsWith("video/") -> MessageType.VIDEO
            mimeType.startsWith("audio/") -> MessageType.AUDIO
            else -> MessageType.TEXT
        }

        // Wysyłanie mediów
        viewModel.sendMediaMessage(byteArray, extension, messageType)

    } catch (e: IOException) {
        e.printStackTrace()
    }
}
