package pwr.barwa.chat.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
    }

}