package pwr.barwa.chat.ui.screen

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pwr.barwa.chat.ui.AppViewModelProvider
import pwr.barwa.chat.ui.ChatsListViewModel
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.DisposableEffect
import pwr.barwa.chat.data.dto.ChannelDto
import pwr.barwa.chat.data.dto.MessageType
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import android.net.Uri
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.ripple
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(
    onBackClick: () -> Unit,
    onChatClick: (Long) -> Unit,
    onNewChatClick: () -> Unit,
    onCreateGroupClick: () -> Unit,
    onDismissNewChatDialog: () -> Unit,
    onDismissNewGroupDialog: () -> Unit,
    viewModel: ChatsListViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {


    val showNewChatDialog by viewModel.showNewChatDialog.collectAsState()
    val showNewGroupDialog by viewModel.showNewGroupDialog.collectAsState()
    val chats by viewModel.chats.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    var chatName by remember { mutableStateOf("") }
    var groupName by remember { mutableStateOf("") }
    var members by remember { mutableStateOf("") }
    var selectedAvatarUri by remember { mutableStateOf<Uri?>(null) }
    var showImagePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Add this function to handle image selection
    fun handleImageSelection(uri: Uri?) {
        selectedAvatarUri = uri
    }
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
                                        .background(Color.Transparent)
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
                        //Avatar
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null, // Removes the ripple effect
                                    onClick = { showImagePicker = true }
                                )
                                .align(Alignment.CenterHorizontally)
                        ) {
                            if (selectedAvatarUri != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(selectedAvatarUri),
                                    contentDescription = "Selected avatar",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Add avatar",
                                    modifier = Modifier
                                        .size(60.dp)
                                        .align(Alignment.Center),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        //Chat name
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
                            viewModel.startNewChat(chatName, selectedAvatarUri, context)
                            onDismissNewChatDialog()
                            selectedAvatarUri = null // Reset for next use
                        },
                        enabled = chatName.isNotBlank()
                    ) {
                        Text("Start")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        onDismissNewChatDialog()
                        selectedAvatarUri = null // Reset for next use
                    }) {
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
                        // Avatar preview and selection
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null, // Removes the ripple effect
                                    onClick = { showImagePicker = true }
                                )
                                .align(Alignment.CenterHorizontally)
                        ) {
                            if (selectedAvatarUri != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(selectedAvatarUri),
                                    contentDescription = "Selected avatar",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Add avatar",
                                    modifier = Modifier
                                        .size(60.dp)
                                        .align(Alignment.Center),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

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
                            viewModel.createNewGroup(groupName, memberList, selectedAvatarUri, context)
                            onDismissNewGroupDialog()
                            selectedAvatarUri = null // Reset for next use
                        },
                        enabled = groupName.isNotBlank() && members.isNotBlank()
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        onDismissNewGroupDialog()
                        selectedAvatarUri = null // Reset for next use
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Add the image picker dialog
        if (showImagePicker) {
            ImagePickerDialog(
                onDismiss = { showImagePicker = false },
                onImageSelected = { uri ->
                    selectedAvatarUri = uri
                    showImagePicker = false
                }
            )
        }
    }
}


@Composable
fun ChatItem(chat: ChannelDto, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(all = 16.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(), // Explicit ripple
                onClick = onClick
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Chat avatar/image
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (!chat.image.isNullOrEmpty()) {
                AsyncImage(
                    model = chat.image,
                    contentDescription = "Chat avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Fallback icon when no image is available
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Chat avatar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = chat.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = chat.lastMessage?.let {
                    when (it.type) {
                        MessageType.TEXT -> it.data
                        else -> "Media message"
                    }
                } ?: "No messages yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// Helper function to format message timestamp
private fun formatMessageTime(timestamp: String): String {
    // Implement your timestamp formatting logic here
    // For example: return timestamp.substring(11, 16) // Just show hours:minutes
    return timestamp // Return as-is for now
}

@Composable
fun ImagePickerDialog(
    onDismiss: () -> Unit,
    onImageSelected: (Uri?) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            onImageSelected(uri)
        }
    )

    AlertDialog(
        onDismissRequest = {
            onImageSelected(null) // Anulowanie wyboru
            onDismiss()
        },
        title = { Text("Select Avatar") },
        text = { Text("Choose an image from your gallery") },
        confirmButton = {
            Button(
                onClick = {
                    launcher.launch("image/*")
                }
            ) {
                Text("Gallery")
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    onImageSelected(null) // Anulowanie wyboru
                    onDismiss()
                }
            ) {
                Text("Cancel")
            }
        }
    )
}
