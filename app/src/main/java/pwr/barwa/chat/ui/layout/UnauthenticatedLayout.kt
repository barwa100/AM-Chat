package pwr.barwa.chat.ui.layout

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import pwr.barwa.chat.ui.theme.ChatTheme

@Composable
fun UnauthenticatedLayout(
    content: @Composable (padding: PaddingValues) -> Unit
) {
    // Create a Scaffold with a top bar and bottom bar with theme applied
    ChatTheme {
        Scaffold { paddingValues ->
            content(paddingValues)
        }
    }
}