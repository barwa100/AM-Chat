package pwr.barwa.chat.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pwr.barwa.chat.data.model.Chat
import pwr.barwa.chat.ui.AppViewModelProvider
import pwr.barwa.chat.ui.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(
    onBackClick: () -> Unit,
    onChatClick: (Long) -> Unit,
    viewModel: ChatViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val coroutineScope = rememberCoroutineScope()
    val chats by viewModel.chats.collectAsState()

        if (chats.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No chats available")
            }
        } else {
            LazyColumn(
            ) {
                items(
                    count = chats.size,
                    key = { index -> chats[index].id },
                    itemContent = { index ->
                        val chat = chats[index]
                        ChatItem(chat = chat, onClick = { onChatClick(chat.id) })
                        HorizontalDivider( //  linia o grubości 2.dp z marginesami bocznymi 16.dp.
                            thickness = 2.dp,
                        )

                    }
                )
            }
        }
}

@Composable
fun ChatItem(chat: Chat, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp,16.dp)
            .clickable { /* Obsługa kliknięcia na czat */ }
    ) {
        Text(
            text = chat.name,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = chat.lastMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}