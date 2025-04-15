package pwr.barwa.chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import pwr.barwa.chat.ui.layout.AuthenticatedLayout
import pwr.barwa.chat.ui.layout.MainLayout
import pwr.barwa.chat.ui.screen.Debug
import pwr.barwa.chat.ui.screen.LoginScreen
import pwr.barwa.chat.ui.screen.Register
import pwr.barwa.chat.ui.theme.ChatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {

            val navController = rememberNavController()
            val isAuthenticated = remember { mutableStateOf(true) }

            MainLayout(isAuthenticated, navController)
            {
                NavHost(
                    navController = navController,
                    startDestination = Debug,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(0.dp)
                ) {
                    composable<Login> {
                        LoginScreen(
                            onLoginClick = { username, password ->
                                println(username + " " + password)
                                isAuthenticated.value = true
                                navController.navigate(GreetingRoute(username)) {
                                    popUpTo(Login) {
                                        inclusive = true
                                    }
                                }
                            },
                            onNavigateToRegister = {
                                navController.navigate(Register) {
                                    popUpTo(Login) {
                                        inclusive = true
                                    }
                                }
                            }
                        )
                    }
                    composable<Register> {
                        Register(
                            onRegisterClick = { username, password ->
                                println(username + " " + password)
                                isAuthenticated.value = true
                                navController.navigate(GreetingRoute(username)) {
                                    popUpTo(Login) {
                                        inclusive = true
                                    }
                                }
                            },
                            onLoginClick = {
                                navController.navigate(Login) {
                                    popUpTo(Register) {
                                        inclusive = true
                                    }
                                }
                            }
                        )
                    }
                    composable<GreetingRoute> { backstack ->
                        val greeting = backstack.toRoute<GreetingRoute>()
                        Greeting(
                            greeting.name
                        )
                    }
                    composable<Debug> {
                        Debug()
                    }
                }
            }
        }
    }
}

@Serializable
object Login
@Serializable
object Register
@Serializable
data class GreetingRoute(val name: String)
@Serializable
object Debug
@Composable
fun Greeting(name: String) {
    Text(
        text = "Hello $name!"
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ChatTheme {
        Greeting("Android")
    }
}