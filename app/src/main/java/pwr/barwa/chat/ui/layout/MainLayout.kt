package pwr.barwa.chat.ui.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.navigation.NavController

@Composable
fun MainLayout(
    isAuthenticated: MutableState<Boolean>,
    navController: NavController,
    content: @Composable (() -> Unit)
) {
    if(isAuthenticated.value) {
        AuthenticatedLayout(navController) {
            content()
        }
    }
    else {
        UnauthenticatedLayout {
            content()
        }
    }
}