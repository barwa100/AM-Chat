package pwr.barwa.chat

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import pwr.barwa.chat.ui.layout.MainLayout
import pwr.barwa.chat.ui.screen.Debug
import pwr.barwa.chat.ui.screen.LoginScreen
import pwr.barwa.chat.ui.screen.Register
import pwr.barwa.chat.ui.theme.ChatTheme
import androidx.compose.material3.Button
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import pwr.barwa.chat.data.dto.UserDto
import pwr.barwa.chat.services.AuthService
import pwr.barwa.chat.ui.AppViewModelProvider
import pwr.barwa.chat.ui.ChatsListViewModel
import pwr.barwa.chat.ui.CurrentUserHolder
import pwr.barwa.chat.ui.screen.ChatsScreen
import pwr.barwa.chat.ui.screen.ChatDetailsScreen
import pwr.barwa.chat.ui.screen.Contacts
import pwr.barwa.chat.ui.screen.MyProfileScreen
import pwr.barwa.chat.ui.screen.SplashScreen

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
//
                MainLayout(isAuthenticated, navController,
                    onLogoutClick = {
                        clearUserSession(ctx)
                        isAuthenticated.value = false
                        navController.navigate(Login) {
                            popUpTo(Login) {
                                inclusive = true
                            }
                        }
                    },
                )
                { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = SplashScreen,
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
                            val viewModel: ChatsListViewModel = viewModel(factory = AppViewModelProvider.Factory)

                            ChatsScreen(
                                onChatClick = {chatId ->
                                    println("Clicked chatId: $chatId")
                                    navController.navigate("chat_details/$chatId")},
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
                                Text("Invalid chat")
                            }
                        }
                        composable<Contacts>{
                            Contacts()
                        }
                        composable<MyProfile> {
                            val ctx = this@MainActivity
                            val session = getUserSession(ctx)
                            var user by remember { mutableStateOf<UserDto?>(null) }

                            LaunchedEffect(session) {
                                if (session != null) {
                                    val result = AuthService().getUserByUsername(session.first)
                                    result.onSuccess { loadedUser ->
                                        CurrentUserHolder.setCurrentUser(loadedUser)
                                        user = loadedUser
                                    }.onFailure {
                                        Log.e("MyProfile", "Failed to load user", it)
                                    }
                                }
                            }
                            if (user != null) {
                                MyProfileScreen()
                            } else {
                                Text("Loading user data...")
                            }
                        }
                        composable<SplashScreen> {
                            SplashScreen(
                                onActionComplete = { loggedIn ->
                                    if (loggedIn){
                                        navController.navigate(Chats){
                                            isAuthenticated.value = true
                                            popUpTo(SplashScreen) {
                                                inclusive = true
                                            }
                                        }
                                    }
                                    else {
                                        navController.navigate(Login) {
                                            popUpTo(SplashScreen) {
                                                inclusive = true
                                            }
                                        }
                                    }
                                }
                            )
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
object Contacts
@Serializable
object MyProfile
@Serializable
object SplashScreen
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

