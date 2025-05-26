package pwr.barwa.chat.ui.screen

import android.graphics.drawable.GradientDrawable.Orientation
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pwr.barwa.chat.data.model.Chat
import pwr.barwa.chat.ui.AppViewModelProvider
import pwr.barwa.chat.ui.ChatViewModel
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.material.DismissState
import androidx.compose.material.Surface
import androidx.compose.runtime.DisposableEffect
import pwr.barwa.chat.data.dto.ChannelDto
import pwr.barwa.chat.data.dto.MessageType


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(
    onBackClick: () -> Unit,
    onChatClick: (Long) -> Unit,
    onNewChatClick: () -> Unit,
    onCreateGroupClick: () -> Unit,
    onDismissNewChatDialog: () -> Unit,
    onDismissNewGroupDialog: () -> Unit,
    viewModel: ChatViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {

    val showNewChatDialog by viewModel.showNewChatDialog.collectAsState()
    val showNewGroupDialog by viewModel.showNewGroupDialog.collectAsState()
    val chats by viewModel.chats.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    var chatName by remember { mutableStateOf("") }
    var groupName by remember { mutableStateOf("") }
    var members by remember { mutableStateOf("") }

    viewModel.loadChats()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.removeListeners()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (chats.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No chats available")
            }
        } else {
            @OptIn(ExperimentalMaterialApi::class)
            LazyColumn {
//                items(
//                    count = chats.size,
//                    key = { index -> chats[index].id },
//                    itemContent = { index ->
//                        val chat = chats[index]
//                        ChatItem(chat = chat, onClick = { onChatClick(chat.id) })
//                        HorizontalDivider(
//                            //  linia o grubości 2.dp z marginesami bocznymi 16.dp.
//                            thickness = 2.dp,
//                        )
//                    }
//                )
                items(
                    count = chats.size,
                    key = { index -> chats[index].id },
                    itemContent = { index ->
                        val chat = chats[index]
                        val dismissState = rememberDismissState(
                            confirmStateChange = {
                                if (it == DismissValue.DismissedToStart) {
                                    viewModel.deleteChat(chat.id)
                                    true
                                } else false
                            }
                        )

                        SwipeToDismiss(
                            state = dismissState,
                            background = {
                                // Tło podczas przesuwania
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.White)
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.Red
                                    )
                                }
                            },
                            directions = setOf(DismissDirection.EndToStart),
                            dismissContent = {
                                ChatItem(chat = chat, onClick = { onChatClick(chat.id) })
                            }
                        )

                        HorizontalDivider(thickness = 2.dp)
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            AnimatedVisibility(visible = expanded) {
                Column(horizontalAlignment = Alignment.End) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            expanded = false
                            onNewChatClick()
                        },
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        text = { Text("New chat") },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    ExtendedFloatingActionButton(
                        onClick = {
                            expanded = false
                            onCreateGroupClick()
                        },
                        icon = { Icon(Icons.Default.Person, contentDescription = null) },
                        text = { Text("New group") }
                    )
                }
            }

            FloatingActionButton(
                onClick = { expanded = !expanded },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = "Add"
                )
            }
        }

        // Dialog: New Chat
        if (showNewChatDialog) {
            AlertDialog(
                onDismissRequest = onDismissNewChatDialog,
                title = { Text("Start New Chat") },
                text = {
                    Column {
                        Text("Enter chat name:")
                        TextField(
                            value = chatName,
                            onValueChange = { chatName = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.startNewChat(chatName)
                            onDismissNewChatDialog()
                        },
                        enabled = chatName.isNotBlank()
                    ) {
                        Text("Start")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismissNewChatDialog) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Dialog: New Group
        if (showNewGroupDialog) {
            AlertDialog(
                onDismissRequest = onDismissNewGroupDialog,
                title = { Text("Create New Group") },
                text = {
                    Column {
                        TextField(
                            value = groupName,
                            onValueChange = { groupName = it },
                            label = { Text("Group Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        TextField(
                            value = members,
                            onValueChange = { members = it },
                            label = { Text("Members (comma-separated)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val memberList = members.split(",").map { it.trim().toLong() }
                            viewModel.createNewGroup(groupName, memberList)
                            onDismissNewGroupDialog()
                        },
                        enabled = groupName.isNotBlank() && members.isNotBlank()
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismissNewGroupDialog) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}


@Composable
fun ChatItem(chat: ChannelDto, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp,16.dp)
            .clickable { onClick() }
    ) {
        Text(
            text = chat.name,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = chat.lastMessage?.let {
                if (it.type == MessageType.TEXT) {
                    it.data
                } else {
                    "Media message"
                }
            } ?: "No messages yet",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
