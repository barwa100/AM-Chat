package pwr.barwa.chat.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pwr.barwa.chat.ui.AppViewModelProvider
import pwr.barwa.chat.ui.ChatsListViewModel
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.runtime.DisposableEffect
import pwr.barwa.chat.data.dto.ChannelDto
import pwr.barwa.chat.data.dto.MessageType
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Badge
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.snap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.LaunchedEffect

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
    val newChatIds by viewModel.newChatIds.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    var chatName by remember { mutableStateOf("") }
    var groupName by remember { mutableStateOf("") }
    var members by remember { mutableStateOf("") }
    var selectedAvatarUri by remember { mutableStateOf<Uri?>(null) }
    var showImagePicker by remember { mutableStateOf(false) }
    var showSearchBar by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Stan przewijania listy czatów
    val listState = rememberLazyListState()

    // Stan przewijania w topAppBar
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

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

    // Automatyczne przewijanie do nowego chatu
    LaunchedEffect(newChatIds) {
        if (newChatIds.isNotEmpty() && chats.isNotEmpty()) {
            // Znajdź indeks nowego chatu
            val newChatIndex = chats.indexOfFirst { chat -> newChatIds.contains(chat.id) }
            if (newChatIndex >= 0) {
                // Przewiń do pozycji nowego chatu
                listState.animateScrollToItem(newChatIndex)
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Czaty",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                scrollBehavior = scrollBehavior,
                actions = {
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                FloatingActionButton(
                    onClick = { expanded = !expanded },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "Dodaj"
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
        ) {
            // Lista czatów
            if (chats.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Brak czatów",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FilledTonalButton(onClick = onNewChatClick) {
                            Text("Rozpocznij nowy czat")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    ),
                    state = listState // Ustawienie stanu listy
                ) {
                    items(
                        items = chats,
                        key = { chat -> chat.id } // Używa ID chatu jako klucza
                    ) { chat ->
                        // Pobierz informację o nowym chacie raz, na zewnątrz renderowania
                        val isNewChat = newChatIds.contains(chat.id)
                        ChatItemCard(
                            chat = chat,
                            onClick = { onChatClick(chat.id) },
                            onDeleteClick = { viewModel.deleteChat(chat.id) },
                            isNewItem = isNewChat
                        )
                    }
                    // Dodaj trochę miejsca na dole dla FAB
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }

            // Menu dodawania czatów (rozszerzane)
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 80.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Card(
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(16.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .clickable {
                                    expanded = false
                                    onNewChatClick()
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Nowy czat",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(16.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .clickable {
                                    expanded = false
                                    onCreateGroupClick()
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Person, // Zamiana Icons.Default.Group na Icons.Default.Person
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Nowa grupa",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog: New Chat
    if (showNewChatDialog) {
        AlertDialog(
            onDismissRequest = onDismissNewChatDialog,
            title = { Text("Rozpocznij nowy czat") },
            text = {
                Column {
                    //Avatar
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable(onClick = { showImagePicker = true })
                            .align(Alignment.CenterHorizontally)
                    ) {
                        if (selectedAvatarUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(selectedAvatarUri),
                                contentDescription = "Wybrany awatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Dodaj awatar",
                                modifier = Modifier
                                    .size(60.dp)
                                    .align(Alignment.Center),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    //Chat name
                    Text("Nazwa czatu:")
                    OutlinedTextField(
                        value = chatName,
                        onValueChange = { chatName = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.startNewChat(chatName, selectedAvatarUri, context)
                        onDismissNewChatDialog()
                        selectedAvatarUri = null // Reset for next use
                        chatName = ""
                    },
                    enabled = chatName.isNotBlank(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Rozpocznij")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    onDismissNewChatDialog()
                    selectedAvatarUri = null // Reset for next use
                    chatName = ""
                }) {
                    Text("Anuluj")
                }
            }
        )
    }

    // Dialog: New Group
    if (showNewGroupDialog) {
        AlertDialog(
            onDismissRequest = onDismissNewGroupDialog,
            title = { Text("Utwórz nową grupę") },
            text = {
                Column {
                    // Avatar preview and selection
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable(onClick = { showImagePicker = true })
                            .align(Alignment.CenterHorizontally)
                    ) {
                        if (selectedAvatarUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(selectedAvatarUri),
                                contentDescription = "Wybrany awatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.Person, // Zamiana Icons.Default.Group na Icons.Default.Person
                                contentDescription = "Dodaj awatar grupy",
                                modifier = Modifier
                                    .size(60.dp)
                                    .align(Alignment.Center),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        label = { Text("Nazwa grupy") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = members,
                        onValueChange = { members = it },
                        label = { Text("Członkowie (ID oddzielone przecinkami)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
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
                        groupName = ""
                        members = ""
                    },
                    enabled = groupName.isNotBlank() && members.isNotBlank(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Utwórz")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    onDismissNewGroupDialog()
                    selectedAvatarUri = null // Reset for next use
                    groupName = ""
                    members = ""
                }) {
                    Text("Anuluj")
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

@Composable
fun ChatAvatar(chat: ChannelDto, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
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
            val icon = if (chat.members.size > 2) Icons.Default.Person else Icons.Default.Person
            Icon(
                imageVector = icon,
                contentDescription = "Chat avatar",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChatItemCard(
    chat: ChannelDto,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    isNewItem: Boolean = false
) {
    val dismissState = rememberDismissState(
        confirmStateChange = {
            if (it == DismissValue.DismissedToStart) {
                onDeleteClick()
                true
            } else false
        }
    )

    // Animacja dla nowych elementów
    val isVisible = remember { Animatable(if (isNewItem) 0f else 1f) }

    // Wykonaj animację po pierwszym złożeniu
    LaunchedEffect(chat.id, isNewItem) {
        if (isNewItem) {
            isVisible.snapTo(0f)
            isVisible.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }

    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 2.dp)
        .graphicsLayer {
            // Zastosuj animację tylko dla nowych elementów
            if (isNewItem) {
                translationX = (1f - isVisible.value) * (-500f)  // Przesuń z lewej strony
                //scaleX = 0.8f + (isVisible.value * 0.2f)     // Skalowanie od 0.8 do 1
                //scaleY = 0.8f + (isVisible.value * 0.2f)
                alpha = isVisible.value                       // Przezroczystość
            }
        }
    ) {
        SwipeToDismiss(
            state = dismissState,
            background = {
                val direction = dismissState.dismissDirection
                val alignment = when (direction) {
                    DismissDirection.StartToEnd -> Alignment.CenterStart
                    DismissDirection.EndToStart -> Alignment.CenterEnd
                    null -> Alignment.Center
                }

                val color = when (direction) {
                    DismissDirection.StartToEnd -> Color.Green
                    DismissDirection.EndToStart -> MaterialTheme.colorScheme.errorContainer
                    null -> Color.Transparent
                }

                val icon = when (direction) {
                    DismissDirection.StartToEnd -> Icons.Default.Person
                    DismissDirection.EndToStart -> Icons.Default.Delete
                    null -> Icons.Default.Person
                }

                // Zastosuj zaokrąglone narożniki tylko dla zawartości tła
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp),
                    contentAlignment = alignment
                ) {
                    if (direction == DismissDirection.EndToStart) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 16.dp)
                        ) {
                            Text(
                                text = "Usuń",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            },
            dismissContent = {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onClick),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar
                        ChatAvatar(chat)

                        // Content
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = chat.name,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            // Last message with better formatting
                            Text(
                                text = chat.lastMessage?.let {
                                    when (it.type) {
                                        MessageType.TEXT -> it.data
                                        MessageType.IMAGE -> "🖼️ Obraz"
                                        MessageType.VIDEO -> "🎥 Wideo"
                                        MessageType.AUDIO -> "🎵 Audio"
                                        else -> "Wiadomość"
                                    }
                                } ?: "Brak wiadomości",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Time and unread count
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            // Time
                            chat.lastMessage?.let { message ->
                                Text(
                                    text = formatChatTime(message.created),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(4.dp))


                            }
                        }
                    }
                }
            },
            directions = setOf(DismissDirection.EndToStart)
        )
    }
}

@Composable
fun formatChatTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val date = Date(timestamp)
    val diff = now - timestamp

    return when {
        diff < 24 * 60 * 60 * 1000 -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        diff < 7 * 24 * 60 * 60 * 1000 -> SimpleDateFormat("EEE", Locale.getDefault()).format(date)
        else -> SimpleDateFormat("dd.MM", Locale.getDefault()).format(date)
    }
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
            onImageSelected(null)
            onDismiss()
        },
        title = { Text("Wybierz zdjęcie") },
        text = { Text("Wybierz zdjęcie z galerii") },
        confirmButton = {
            Button(
                onClick = {
                    launcher.launch("image/*")
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Galeria")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onImageSelected(null)
                    onDismiss()
                }
            ) {
                Text("Anuluj")
            }
        }
    )
}
