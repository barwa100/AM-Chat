package pwr.barwa.chat.ui.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.navigation.NavController

@Composable
fun MainLayout(
    isAuthenticated: MutableState<Boolean>,
    navController: NavController,
    onLogoutClick: () -> Unit,
    content: @Composable (() -> Unit)
) {
    if(isAuthenticated.value) {
        AuthenticatedLayout(
            navController = navController,
            onLogoutClick = onLogoutClick
        ) {
            content()
        }
    } else {
        UnauthenticatedLayout {
            content()
        }
    }
}