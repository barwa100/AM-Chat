package pwr.barwa.chat.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pwr.barwa.chat.data.SignalRConnector
import pwr.barwa.chat.services.AuthService

class SessionViewModel(private val sharedPreferences: SharedPreferences) : ViewModel()  {
    suspend fun checkSession(): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            val username = sharedPreferences.getString("username", null)
            val password = sharedPreferences.getString("password", null)

            AuthService().login(username ?: "", password ?: "").let { result ->
                if (result.isSuccess) {
                    val token = result.getOrNull()?.accessToken
                    if (token != null) {
                        sharedPreferences.edit().putString("token", token).apply()
                        SignalRConnector.getInstance(token).startConnection()

                        // Pobierz informacje o użytkowniku i ustaw je w CurrentUserHolder
                        val userResult = AuthService().getUserByUsername(username ?: "")
                        userResult.onSuccess { user ->
                            CurrentUserHolder.setCurrentUser(user)
                        }

                        Result.success(true)
                    } else {
                        Result.failure(Exception("Token is null"))
                    }
                } else {
                    Result.failure(result.exceptionOrNull() ?: Exception("Login failed"))
                }
            }

        }
    }
}