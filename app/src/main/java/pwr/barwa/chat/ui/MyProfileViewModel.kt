package pwr.barwa.chat.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
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
import java.io.ByteArrayOutputStream
import java.io.InputStream

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

class MyProfileViewModel(private val signalRConnector: SignalRConnector, private val context: Context) : ViewModel() {
    private val _user = MutableStateFlow<UserDto?>(null)
    val user: StateFlow<UserDto?> = _user

    private val _isAvatarUpdating = MutableStateFlow(false)
    val isAvatarUpdating: StateFlow<Boolean> = _isAvatarUpdating

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    var profileDescription by mutableStateOf("")

    init {
        viewModelScope.launch {
            CurrentUserHolder.currentUser.collect { currentUser ->
                _user.value = currentUser
            }
        }

        // Dodanie listener'a na zdarzenie zmiany awatara
        signalRConnector.onUserAvatarChanged.addListener("MyProfileView", { (userId, newAvatarUrl) ->
            if (_user.value?.id == userId) {
                _user.value = _user.value?.copy(avatarUrl = newAvatarUrl)
            }
        })
    }

    fun onChangePassword() {
        // np. pokaż dialog lub przenieś do innego widoku
    }

    fun onChangeAvatar(imageUri: Uri) {
        viewModelScope.launch {
            try {
                _isAvatarUpdating.value = true
                _errorMessage.value = null

                // Odczytanie wybranego obrazu
                val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                if (inputStream == null) {
                    _errorMessage.value = "Nie można odczytać pliku obrazu"
                    _isAvatarUpdating.value = false
                    return@launch
                }

                // Konwersja do Bitmap i kompresja
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()

                // Skalowanie obrazu, aby zmniejszyć rozmiar
                val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 300,
                    (300 * bitmap.height.toFloat() / bitmap.width).toInt(), true)

                // Konwersja do Base64
                val byteArrayOutputStream = ByteArrayOutputStream()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()
                val base64String = Base64.encodeToString(byteArray, Base64.DEFAULT)

                // Określenie rozszerzenia pliku
                val extension = "jpg"

                // Wysłanie do serwera
                signalRConnector.changeUserAvatar(base64String, extension)

                _isAvatarUpdating.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Błąd podczas aktualizacji awatara: ${e.message}"
                _isAvatarUpdating.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        signalRConnector.onUserAvatarChanged.removeListener("MyProfileView")
    }
}
