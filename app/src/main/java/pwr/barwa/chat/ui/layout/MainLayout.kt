package pwr.barwa.chat.ui.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController

@Composable
fun MainLayout(
    isAuthenticated: MutableState<Boolean>,
    navController: NavController,
    onLogoutClick: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    var backgroundColor by remember { mutableStateOf(Color(0xFFBB86FC)) }
//tutaj coś nie sztymuje
    Box(modifier = Modifier.background(backgroundColor)) {
        if (isAuthenticated.value) {
            AuthenticatedLayout(
                navController = navController,
                onLogoutClick = onLogoutClick,
                onBackgroundColorChange = { newColor ->
                    backgroundColor = newColor
                },
                content = content,
                backgroundColor = backgroundColor
            )
        } else {
            UnauthenticatedLayout {
                content(PaddingValues())
            }
        }
    }
}
