package pwr.barwa.chat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pwr.barwa.chat.data.dao.UserDao
import pwr.barwa.chat.data.model.User
import pwr.barwa.chat.data.responses.TokenResponse
import pwr.barwa.chat.services.AuthService
import pwr.barwa.chat.sha256

class LoginViewModel(private val userDao: UserDao) : ViewModel() {
    private val authService = AuthService()
    suspend fun login(username: String, password: String): Result<TokenResponse> {
        return withContext(Dispatchers.IO) {
            authService.login(username, password)
        }
    }
}