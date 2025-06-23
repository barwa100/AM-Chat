package pwr.barwa.chat.ui.screen

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pwr.barwa.chat.R
import pwr.barwa.chat.data.dto.MessageDto
import pwr.barwa.chat.data.dto.MessageType
import pwr.barwa.chat.services.AuthService
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.net.toUri
import pwr.barwa.chat.ui.components.ChatAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailsScreen(
    chatId: Long,
    onNavigateToEditChat: (Long) -> Unit = {}, // Nowy parametr do nawigacji
    viewModel: ChatDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val chat by viewModel.selectedChat.collectAsState()
    val messages by viewModel.channelMessages.collectAsState()
    val users by viewModel.channelMembers.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var text by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var newChatName by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()

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

    LaunchedEffect(chatId) {
        viewModel.loadChatById(chatId)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.removeListeners()
        }
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
                            text = { Text("Dodaj do rozmowy") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = {
                                showMenu = false
                                onNavigateToEditChat(chatId)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Zmień nazwę czatu") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = {
                                showMenu = false
                                showRenameDialog = true
                            }
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
                            isCurrentUser = isCurrentUser,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Zmień nazwę czatu") },
            text = {
                OutlinedTextField(
                    value = newChatName,
                    onValueChange = { newChatName = it },
                    label = { Text("Nowa nazwa czatu") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.renameChat(newChatName)
                        showRenameDialog = false
                    }
                ) {
                    Text("Zmień")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
    }
}

@Composable
fun MessageBubble(
    message: MessageDto,
    bubbleColor: Color,
    textColor: Color,
    userName: String,
    isCurrentUser: Boolean,
    viewModel: ChatDetailsViewModel
) {
    val bubbleShape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (isCurrentUser) 16.dp else 4.dp,
        bottomEnd = if (isCurrentUser) 4.dp else 16.dp
    )
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    val localMediaUri = viewModel.getMediaUri(message.id)

    val isLocallyStored = localMediaUri != null

    val isNewMessage = viewModel.isNewMessage(message.id)

    val visibleState = remember { MutableTransitionState(!isNewMessage).apply { targetState = true } }

    LaunchedEffect(message.id) {
        if (isNewMessage) {
            delay(500)
            viewModel.markMessageDisplayed(message.id)
        }
    }

    AnimatedVisibility(
        visibleState = visibleState,
        enter = if (isNewMessage) {
            if (isCurrentUser) {
                fadeIn(animationSpec = tween(300)) +
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth / 3 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            } else {
                fadeIn(animationSpec = tween(300)) +
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> -fullWidth / 3 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) +
                scaleIn(
                    initialScale = 0.8f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
        } else {
            fadeIn(animationSpec = tween(0))
        }
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            if (!isCurrentUser) {
                Text(
                    text = userName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
                )
            }

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
                            val imageUri = localMediaUri ?: (AuthService.URL_BASE + "Media/" + message.id + "." + message.data).toUri()

                            Box {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(imageUri)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Obraz",
                                    contentScale = ContentScale.FillWidth,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            localMediaUri?.let { uri ->
                                                try {
                                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                                        setDataAndType(uri, when (message.type) {
                                                            MessageType.IMAGE -> "image/*"
                                                            MessageType.VIDEO -> "video/*"
                                                            MessageType.AUDIO -> "audio/*"
                                                            else -> "*/*"
                                                        })
                                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    }
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    Log.e("MessageBubble", "Błąd podczas otwierania pliku: ${e.message}")
                                                    Toast.makeText(context, "Nie można otworzyć pliku: ${e.message}", Toast.LENGTH_SHORT).show()
                                                }
                                            } ?: run {
                                                val remoteUrl = AuthService.URL_BASE + "Media/" + message.id + "." + message.data
                                                uriHandler.openUri(remoteUrl)
                                            }
                                        }
                                )

                                if (isLocallyStored) {
                                    LocalFileIndicator(textColor = textColor)
                                }
                            }
                        }
                        MessageType.VIDEO -> {
                            val extension = message.data ?: "mp4" // Fallback do mp4 jeśli brak rozszerzenia
                            val videoUri = localMediaUri?.toString()
                                ?: (AuthService.URL_BASE + "Media/" + message.id + "." + extension)

                            Box {
                                VideoPlayer(url = videoUri)

                                if (isLocallyStored) {
                                    LocalFileIndicator(textColor = textColor)
                                }
                            }
                        }
                        MessageType.AUDIO -> {
                            val extension = message.data ?: "mp3" // Fallback do mp3 jeśli brak rozszerzenia
                            val audioUri = localMediaUri?.toString()
                                ?: (AuthService.URL_BASE + "Media/" + message.id + "." + extension)

                            Box {
                                AudioPlayer(url = audioUri, textColor = textColor)

                                if (isLocallyStored) {
                                    LocalFileIndicator(textColor = textColor)
                                }
                            }
                        }
                        else -> {
                            Text(
                                text = message.data,
                                style = MaterialTheme.typography.bodyMedium,
                                color = textColor
                            )
                        }
                    }

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
}


@Composable
fun LocalFileIndicator(textColor: Color) {
    Box(
        modifier = Modifier
            .padding(8.dp)
            .size(32.dp)
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                shape = CircleShape
            )
            .border(
                width = 1.dp,
                color = textColor.copy(alpha = 0.5f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Check,  // Zmiana na dostępną ikonę
            contentDescription = "Plik dostępny offline",
            tint = textColor,
            modifier = Modifier.size(16.dp)
        )
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(url: String) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    exoPlayer.pause()
                }
                Lifecycle.Event.ON_RESUME -> {
                }
                Lifecycle.Event.ON_DESTROY -> {
                    exoPlayer.release()
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true
                    setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun AudioPlayer(url: String, textColor: Color) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    val isPlaying = remember { mutableStateOf(false) }
    val progress = remember { mutableStateOf(0f) }
    val duration = remember { mutableStateOf(0L) }
    val formattedPosition = remember { mutableStateOf("0:00") }
    val formattedDuration = remember { mutableStateOf("0:00") }
    val coroutineScope = rememberCoroutineScope()

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()

            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        duration.value = this@apply.duration
                        formattedDuration.value = formatDuration(this@apply.duration)
                    }
                }

                override fun onIsPlayingChanged(playing: Boolean) {
                    isPlaying.value = playing

                    if (playing) {
                        coroutineScope.launch {
                            val player = this@apply
                            while (isPlaying.value) {
                                try {
                                    val currentPos = player.currentPosition
                                    val totalDuration = player.duration.coerceAtLeast(1)
                                    progress.value = currentPos.toFloat() / totalDuration
                                    formattedPosition.value = formatDuration(currentPos)
                                    delay(500) // Aktualizacja co 500ms
                                } catch (e: Exception) {
                                    Log.e("AudioPlayer", "Błąd dostępu do exoPlayer: ${e.message}")
                                    break
                                }
                            }
                        }
                    }
                }
            })
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    exoPlayer.pause()
                }
                Lifecycle.Event.ON_DESTROY -> {
                    exoPlayer.release()
                }
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.release()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (isPlaying.value) {
                        exoPlayer.pause()
                    } else {
                        exoPlayer.play()
                    }
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = if (isPlaying.value)
                        painterResource(id = R.drawable.ic_pause)
                    else
                        painterResource(id = R.drawable.ic_play),
                    contentDescription = if (isPlaying.value) "Pauza" else "Odtwórz",
                    tint = textColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Slider(
                    value = progress.value,
                    onValueChange = {
                        progress.value = it
                        val newPosition = (exoPlayer.duration * it).toLong()
                        exoPlayer.seekTo(newPosition)
                    },
                    colors = androidx.compose.material3.SliderDefaults.colors(
                        thumbColor = textColor,
                        activeTrackColor = textColor,
                        inactiveTrackColor = textColor.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formattedPosition.value,
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor
                    )

                    Text(
                        text = formattedDuration.value,
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor
                    )
                }
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}

private fun formatMessageTime(timestamp: Long): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
}

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
