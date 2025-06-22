package pwr.barwa.chat.ui.screen

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import pwr.barwa.chat.ui.AppViewModelProvider
import pwr.barwa.chat.ui.ChatDetailsViewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import pwr.barwa.chat.data.dto.MessageType
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
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

    // Stan komponentu
    var text by remember { mutableStateOf("") }
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
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = chat?.name ?: "Chat Details",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Lista wiadomości - sortowanie od najstarszych do najnowszych
            LazyColumn(
                modifier = Modifier.weight(1f),
                state = lazyListState
            ) {
                val sortedMessages = messages.sortedBy { it.created }
                items(sortedMessages) { message ->
                    val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(message.created))
                    val userName = users.find { it.id == message.senderId }?.userName ?: message.senderId.toString()
                    Text(text = "$userName: ${message.data}", color = Color.Black)
                    Text(text = date, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Pole wprowadzania i przyciski akcji
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Przycisk do załączania plików
                IconButton(
                    onClick = { mediaLauncher.launch("*/*") }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Załącz plik",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Pole do wpisywania wiadomości
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Wpisz wiadomość...") }
                )

                // Przycisk wysyłania
                IconButton(
                    onClick = {
                        if (text.isNotBlank()) {
                            viewModel.sendMessage(text)
                            text = ""
                        }
                    },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Wyślij",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
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
        // Tutaj można dodać obsługę błędów, np. wyświetlenie komunikatu dla użytkownika
    }
}
