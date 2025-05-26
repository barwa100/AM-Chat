package pwr.barwa.chat.ui

import android.window.SplashScreenView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import pwr.barwa.chat.ChatApplication

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            LoginViewModel(chatApplication().container.database.userDao())
        }
        initializer {
            RegisterViewModel(chatApplication().container.database.userDao())
        }
        initializer {
            ChatViewModel(chatApplication().container.database.chatDao())
        }
        initializer {
            DebugViewModel(chatApplication().container.signalRConnector)
        }
        initializer {
            SessionViewModel(chatApplication().container.sharedPreferences)
        }
    }
}
fun CreationExtras.chatApplication() : ChatApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ChatApplication)