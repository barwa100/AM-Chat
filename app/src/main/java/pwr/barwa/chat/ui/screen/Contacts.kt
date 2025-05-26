package pwr.barwa.chat.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pwr.barwa.chat.data.dto.UserDto
import pwr.barwa.chat.ui.AppViewModelProvider
import pwr.barwa.chat.ui.ContactsViewModel

@Composable
fun Contacts(
    viewModel: ContactsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    var showDialog by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("") }
    val contacts by viewModel.contacts.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.removeListeners()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {


        if (contacts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No contacts saved")
            }
        } else {
            (LazyColumn {
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
            count = contacts.size,
            key = { index -> index },
            itemContent = { index ->
                ContactItem(
                    user = contacts[index],
                )
                HorizontalDivider(thickness = 2.dp)
            }
        )
    })
        }


        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {

            FloatingActionButton(
                onClick = { showDialog = !showDialog },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add"
                )
            }
        }
        if (showDialog){
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Add new contact") },
                text = {
                    Column {
                        Text("Enter username:")
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
                        },
                        enabled = userName.isNotBlank()
                    ) {
                        Text("Start")
                    }
                },
                dismissButton = {
                    TextButton(onClick =  { showDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun ContactItem(user: UserDto) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp,16.dp)
    ) {
        Text(
            text = user.userName,
            style = MaterialTheme.typography.titleMedium
        )

    }
}