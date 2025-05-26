package pwr.barwa.chat.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.viewModelFactory
import com.microsoft.signalr.HubConnectionBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pwr.barwa.chat.ui.AppViewModelProvider
import pwr.barwa.chat.ui.DebugViewModel

@Composable
fun Debug(
    onLogoutClick: () -> Unit,
    viewModel: DebugViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    // This is a placeholder for the debug screen
    // You can add your debug UI components here
    // For example, you can use Text, Button, etc.
    // to display debug information or controls
    Text(
        text = "Debug Screen",
        style = MaterialTheme.typography.headlineMedium
    )
    Box(
        modifier = Modifier
            .padding(top = 32.dp)
    ) {
        Button(
            onClick = onLogoutClick
        ) {
            Text(text = "Logout")
        }
        Spacer(modifier = Modifier.height(16.dp))
        val coroutineScope = rememberCoroutineScope()
        LaunchedEffect(Unit) {
            coroutineScope.launch {
                withContext(Dispatchers.IO) {
                    viewModel.connectToChat()
                }
            }
        }
        DisposableEffect(Unit) {
            onDispose {
                viewModel.disconnectFromChat()
            }
        }

        val messages by viewModel.messages
        var message by remember { mutableStateOf("") }
        Column(modifier = Modifier.padding(0.dp,48.dp, 16.dp,8.dp).fillMaxSize()) {
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Wiadomość") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(onClick = {
                viewModel.sendMessage(message)
                message = ""
            }) {
                Text("Wyślij wiadomość")
            }
            Spacer(modifier = Modifier.height(16.dp))
            messages.forEach { message ->
                Text(text = message.data)
            }

        }

    }

}