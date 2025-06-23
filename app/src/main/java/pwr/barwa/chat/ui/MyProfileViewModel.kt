package pwr.barwa.chat.ui

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pwr.barwa.chat.data.SignalRConnector
import pwr.barwa.chat.data.dto.UserDto

object CurrentUserHolder {
    private val _currentUser = MutableStateFlow<UserDto?>(null)
    val currentUser: StateFlow<UserDto?> = _currentUser

    fun setCurrentUser(user: UserDto) {
        _currentUser.value = user
        SignalRConnector.getInstance().setCurrentUser(user)
    }

    fun getCurrentUser(): UserDto? = _currentUser.value

    fun resetCurrentUser() {
        _currentUser.value = null
    }
}

class MyProfileViewModel(private val signalRConnector: SignalRConnector)  : ViewModel() {
    private val _user = MutableStateFlow<UserDto?>(null)
    val user: StateFlow<UserDto?> = _user

    var profileDescription by mutableStateOf("")

    init {
        viewModelScope.launch {
            CurrentUserHolder.currentUser.collect { currentUser ->
                _user.value = currentUser
            }
        }
    }

    fun onChangePassword() {
        // np. pokaż dialog lub przenieś do innego widoku
    }

    fun onChangeAvatar(newAvatarUrl: String) {

    }

    fun updateDescription(newDescription: String) {
        profileDescription = newDescription
    }


}

