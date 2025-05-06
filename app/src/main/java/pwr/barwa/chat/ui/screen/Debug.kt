package pwr.barwa.chat.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.microsoft.signalr.HubConnectionBuilder

@Composable
fun Debug(
    onLogoutClick: () -> Unit,
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
        val connection = HubConnectionBuilder.create("http://10.0.2.2:5093/ws").build()
        connection.on("ReceiveMessage", { message ->
            println("Received message: $message")
        }, String::class.java)
        connection.onClosed { error ->
            println("Connection closed with error: $error")
        }
        val coroutineScope = rememberCoroutineScope()
        coroutineScope.run {
            connection.start().blockingAwait()
        }
        connection.send("SendMessage", "Hello from client")

    }

}