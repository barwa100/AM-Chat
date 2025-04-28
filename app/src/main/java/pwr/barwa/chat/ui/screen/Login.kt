package pwr.barwa.chat.ui.screen

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.Navigation.findNavController
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import pwr.barwa.chat.data.AppDatabase
import pwr.barwa.chat.ui.AppViewModelProvider
import pwr.barwa.chat.ui.LoginViewModel
import pwr.barwa.chat.ui.layout.UnauthenticatedLayout
import kotlin.coroutines.coroutineContext

@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit,
    onNavigateToRegister: () -> Unit = {},
    viewModel: LoginViewModel = viewModel(factory = AppViewModelProvider.Factory)
)
{
    val coroutineScope = rememberCoroutineScope()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Login",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (passwordVisible) "Ukryj" else "Pokaż"
                    TextButton(onClick = { passwordVisible = !passwordVisible }) {
                        Text(icon)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            //Add error message if login fails
            if (!errorMessage.isBlank()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    // Launch in a coroutine scope
                    coroutineScope.launch {
                        // Perform login operation
                        val user = viewModel.login(username, password)
                        if (user != null) {
                            onLoginClick(username, password)
                        } else {
                            // Handle login error
                            errorMessage = "Nie znaleziono użytkownika o podanych danych"
                            println("Invalid username or password")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = {
                    onNavigateToRegister()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Nie masz konta? Zarejestruj się")
            }
        }
}