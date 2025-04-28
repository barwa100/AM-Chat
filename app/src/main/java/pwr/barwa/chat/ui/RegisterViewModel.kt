package pwr.barwa.chat.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pwr.barwa.chat.data.dao.UserDao
import pwr.barwa.chat.data.model.User
import pwr.barwa.chat.sha256

class RegisterViewModel(private val userDao: UserDao) : ViewModel() {

    suspend fun register(username: String, password: String): User? {
        return withContext(Dispatchers.IO) {
            val existingUser = userDao.findByUsername(username)
            return@withContext if (existingUser == null) {
                val newUser = pwr.barwa.chat.data.model.User(username, username, password.sha256())
                userDao.insertAll(newUser)
                newUser
            } else {
                null
            }
        }
    }
}