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
            LoginViewModel()
        }
        initializer {
            RegisterViewModel()
        }
        // Nowe ViewModele
        initializer {
            ChatsListViewModel(chatApplication().container.signalRConnector)
        }
        initializer {
            ChatDetailsViewModel(
                signalRConnector = chatApplication().container.signalRConnector,
                context = chatApplication().applicationContext
            )
        }
        initializer {
            ChatEditViewModel(
                signalRConnector = chatApplication().container.signalRConnector,
                context = chatApplication().applicationContext
            )
        }
        initializer {
            DebugViewModel(chatApplication().container.signalRConnector)
        }
        initializer {
            SessionViewModel(chatApplication().container.sharedPreferences)
        }
        initializer {
            ContactsViewModel(chatApplication().container.signalRConnector)
        }
        initializer {
            MyProfileViewModel(
                signalRConnector = chatApplication().container.signalRConnector,
                context = chatApplication().applicationContext
            )
        }
    }
}
fun CreationExtras.chatApplication() : ChatApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ChatApplication)

