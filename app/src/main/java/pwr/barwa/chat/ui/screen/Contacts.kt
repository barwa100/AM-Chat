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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.Arrangement
import pwr.barwa.chat.data.dto.UserDto
import pwr.barwa.chat.ui.AppViewModelProvider
import pwr.barwa.chat.ui.ContactsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Contacts(
    viewModel: ContactsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    var showDialog by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("") }
    val contacts by viewModel.contacts.collectAsState()

    // Stan przewijania w topAppBar
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.removeListeners()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Kontakty",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                scrollBehavior = scrollBehavior,
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
                    onClick = { showDialog = !showDialog },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Dodaj kontakt"
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
        ) {
            if (contacts.isEmpty()) {
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
                            "Brak kontaktów",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    )
                ) {
                    items(
                        count = contacts.size,
                        key = { index -> contacts[index].id },
                        itemContent = { index ->
                            ContactItem(user = contacts[index])
                        }
                    )
                    // Dodaj trochę miejsca na dole dla FAB
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Dodaj nowy kontakt") },
            text = {
                Column {
                    Text("Wpisz nazwę użytkownika:")
                    TextField(
                        value = userName,
                        onValueChange = { userName = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addContact(userName)
                        userName = ""
                        showDialog = false
                    },
                    enabled = userName.isNotBlank()
                ) {
                    Text("Dodaj")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    userName = ""
                }) {
                    Text("Anuluj")
                }
            }
        )
    }
}

@Composable
fun ContactItem(user: UserDto) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            ContactAvatar(user)

            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = user.userName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun ContactAvatar(user: UserDto, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (!user.avatarUrl.isNullOrEmpty()) {
            // Wyświetl awatar z URL jeśli jest dostępny
            AsyncImage(
                model = user.avatarUrl,
                contentDescription = "Contact avatar",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Wyświetl domyślną ikonę jeśli nie ma awatara
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Contact avatar",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
