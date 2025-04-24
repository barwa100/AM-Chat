package pwr.barwa.chat.ui

import androidx.lifecycle.ViewModel
import pwr.barwa.chat.data.dao.UserDao
import pwr.barwa.chat.data.model.User
import pwr.barwa.chat.sha256

class LoginViewModel(userDao: UserDao) : ViewModel() {
    private val userDao = userDao

    fun login(username: String, password: String): User? {
        val user = userDao.findByUsername(username)
        return if (user != null && user.password == password.sha256()) {
            user
        } else {
            null
        }
    }

    fun register(username: String, password: String): User? {
        val existingUser = userDao.findByUsername(username)
        return if (existingUser == null) {
            val newUser = pwr.barwa.chat.data.model.User(username, username, password)
            userDao.insertAll(newUser)
            newUser
        } else {
            null
        }
    }
}