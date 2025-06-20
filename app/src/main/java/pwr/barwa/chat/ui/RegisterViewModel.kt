package pwr.barwa.chat.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pwr.barwa.chat.data.dao.UserDao
import pwr.barwa.chat.data.model.User
import pwr.barwa.chat.data.responses.TokenResponse
import pwr.barwa.chat.services.AuthService
import pwr.barwa.chat.sha256

class RegisterViewModel(private val userDao: UserDao) : ViewModel() {
    private val authService = AuthService()
    suspend fun register(username: String, password: String): Result<TokenResponse> {
        return withContext(Dispatchers.IO) {
            val registerResult = authService.register(username, password)
            if (registerResult.isSuccess) {
                val loginResult = authService.login(username, password)
                val userResult = authService.getUserByUsername(username)
                if (userResult.isSuccess) {
                    userResult.getOrNull()?.let {
                        CurrentUserHolder.setCurrentUser(it)
                    }
                }
                loginResult.fold(
                    onSuccess = { tokenResponse -> Result.success(tokenResponse) },
                    onFailure = { error -> Result.failure(error) }
                )
            } else {
                Result.failure(registerResult.exceptionOrNull() ?: Exception("Unknown error during registration"))
            }
        }
    }
}