package pwr.barwa.chat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pwr.barwa.chat.data.dao.UserDao
import pwr.barwa.chat.data.model.User
import pwr.barwa.chat.sha256

class LoginViewModel(private val userDao: UserDao) : ViewModel() {

    suspend fun login(username: String, password: String): User? {
        return withContext(Dispatchers.IO) {
            val user = userDao.findByUsername(username)
            return@withContext if (user != null && user.password == password.sha256()) {
                user
            } else {
                null
            }
        }
    }
}