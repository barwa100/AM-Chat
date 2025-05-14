package pwr.barwa.chat

import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Button
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import pwr.barwa.chat.ui.AppViewModelProvider
import pwr.barwa.chat.ui.ChatViewModel
import pwr.barwa.chat.ui.screen.ChatsScreen
import pwr.barwa.chat.ui.screen.ChatDetailsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        var session = getUserSession(this)
        val ctx = this
        setContent {

            val navController = rememberNavController()
            val isAuthenticated = remember { mutableStateOf(false) }
            var session by remember { mutableStateOf(getUserSession(ctx)) }
            if (session != null) {
                isAuthenticated.value = true
            }
//
                MainLayout(isAuthenticated, navController)
                {
                    NavHost(
                        navController = navController,
                        startDestination = if(isAuthenticated.value) Chats else Login,
//                        startDestination = if (isAuthenticated.value) Chats else Login,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(0.dp)
                    ) {
                        composable<Login> {
                            LoginScreen(
                                onLoginClick = { username, password ->
                                    println(username + " " + password)
                                    isAuthenticated.value = true
                                    saveUserSession(ctx, username, password)
                                    session = getUserSession(ctx)
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
                                    saveUserSession(ctx, username, password)
                                    session = getUserSession(ctx)
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
                            Greeting(greeting.name)

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                // Powitanie użytkownika
                                Greeting(session?.first ?: "Guest")

                                Spacer(modifier = Modifier.height(16.dp)) // Przerwa między przyciskami

                                 //Przycisk do przejścia do ekranów chatów
                                Button(
                                    onClick = {navController.navigate(Chats)},
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Go to Chats")
                                }

                                Spacer(modifier = Modifier.height(16.dp)) // Przerwa między przyciskami

                                // Przycisk do wylogowania
                                Button(
                                    onClick = {
                                        // Wyczyść sesję użytkownika
                                        clearUserSession(ctx)

                                        // Przejdź do ekranu logowania
                                        navController.navigate(Login) {
                                            popUpTo(GreetingRoute::class.java.name) {
                                                inclusive = true
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Logout")
                                }
                            }
                        }
                        composable<Chats> {
                            session = getUserSession(ctx)
                            val viewModel: ChatViewModel = viewModel(factory = AppViewModelProvider.Factory)

                            ChatsScreen(
                                onBackClick = {navController.popBackStack()},
                                onChatClick = { navController.navigate("chat_details/{chatId}")},
                                onNewChatClick = { viewModel.onNewChatClick() },
                                onCreateGroupClick = { viewModel.onNewGroupClick() },
                                onDismissNewChatDialog = { viewModel.dismissNewChatDialog() },
                                onDismissNewGroupDialog = { viewModel.dismissNewGroupDialog() },
                                viewModel = viewModel
                            )
                        }
                        composable("chat_details/{chatId}") { backStackEntry ->
                            val chatId = backStackEntry.arguments?.getString("chatId")?.toLongOrNull()
                            if (chatId != null) {
                                ChatDetailsScreen(chatId = chatId)
                            } else {
                                Text("Invalid chat ID")
                            }
                        }
                        composable<Debug> {
                            Debug(onLogoutClick = {
                                clearUserSession(ctx)
                                isAuthenticated.value = false
                                navController.navigate(Login) {
                                    popUpTo(Login) {
                                        inclusive = true
                                    }
                                }
                            })
                        }
                    }
                }
        }
    }

    fun saveUserSession(context: Context, username: String, password: String) {
        val sharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("username", username)
            putString("password", password)
            apply()
        }
    }

    fun clearUserSession(context: Context) {
        val sharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            clear()
            apply()
        }
    }

    fun getUserSession(context: Context): Pair<String, String>? {
        val sharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", null)
        val password = sharedPreferences.getString("password", null)
        return if (username != null && password != null) {
            Pair(username, password)
        } else {
            null
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
object Chats
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

