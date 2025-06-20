package pwr.barwa.chat.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pwr.barwa.chat.data.dao.UserDao
import pwr.barwa.chat.data.responses.TokenResponse
import pwr.barwa.chat.services.AuthService

class LoginViewModel(private val userDao: UserDao) : ViewModel() {
    private val authService = AuthService()
    suspend fun login(username: String, password: String): Result<TokenResponse> {
        val loginResult = authService.login(username, password)
        if (loginResult.isSuccess) {
            val userResult = authService.getUserByUsername(username)
            if (userResult.isSuccess) {
                userResult.getOrNull()?.let {
                    CurrentUserHolder.setCurrentUser(it)
                }
            }
        }

        return withContext(Dispatchers.IO) {
            authService.login(username, password)
        }
    }
}