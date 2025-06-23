package pwr.barwa.chat.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pwr.barwa.chat.data.SignalRConnector
import pwr.barwa.chat.data.dao.UserDao
import pwr.barwa.chat.data.responses.TokenResponse
import pwr.barwa.chat.services.AuthService

class LoginViewModel() : ViewModel() {
    private val authService = AuthService()
    suspend fun login(username: String, password: String): Result<TokenResponse> {
        // Wykonaj logowanie tylko raz
        val loginResult = withContext(Dispatchers.IO) {
            authService.login(username, password)
        }

        if (loginResult.isSuccess) {
            val token = loginResult.getOrNull()?.accessToken
            if (token != null) {
                // Inicjalizuj połączenie SignalR
                SignalRConnector.getInstance(token).startConnection()

                // Pobierz informacje o użytkowniku
                val userResult = authService.getUserByUsername(username)
                if (userResult.isSuccess) {
                    userResult.getOrNull()?.let {
                        // Ustaw informacje o użytkowniku w CurrentUserHolder
                        CurrentUserHolder.setCurrentUser(it)
                    }
                }
            }
        }

        return loginResult
    }
}