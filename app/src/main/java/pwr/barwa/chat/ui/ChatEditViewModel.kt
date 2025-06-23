package pwr.barwa.chat.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pwr.barwa.chat.data.SignalRConnector
import pwr.barwa.chat.data.dto.ChannelDto
import pwr.barwa.chat.data.dto.UserDto

/**
 * ViewModel dla ekranu edycji czatu, pozwalający na zarządzanie uczestnikami czatu
 */
class ChatEditViewModel(
    private val signalRConnector: SignalRConnector,
    private val context: Context
) : ViewModel() {

    private val _selectedChat = MutableStateFlow<ChannelDto?>(null)
    val selectedChat: StateFlow<ChannelDto?> = _selectedChat

    private val _channelMembers = MutableStateFlow<List<UserDto>>(emptyList())
    val channelMembers: StateFlow<List<UserDto>> = _channelMembers

    private val _availableContacts = MutableStateFlow<List<UserDto>>(emptyList())
    val availableContacts: StateFlow<List<UserDto>> = _availableContacts

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // Stan dla informacji zwrotnej o dodaniu uczestnika
    private val _addParticipantFeedback = MutableStateFlow<UserDto?>(null)
    val addParticipantFeedback: StateFlow<UserDto?> = _addParticipantFeedback

    init {
        // Nasłuchiwanie zmian kanału
        signalRConnector.onChannelReceived.addListener("ChatEditView") { channelDto ->
            _selectedChat.value = channelDto
        }

        // Nasłuchiwanie zmian członków kanału
        signalRConnector.onChannelMembersReceived.addListener("ChatEditView") { members ->
            _channelMembers.value = members
            refreshAvailableContacts()
        }

        // Nasłuchiwanie zmian kontaktów
        signalRConnector.onContactsReceived.addListener("ChatEditView") { contacts ->
            refreshAvailableContacts()
        }

        // Słuchanie zdarzenia dodania użytkownika do kanału
        signalRConnector.onUserAddedToChannel.addListener("ChatEditView") { (channel, addedUser, _) ->
            if (channel.id == _selectedChat.value?.id) {
                // Ustawienie informacji zwrotnej o dodanym użytkowniku
                _addParticipantFeedback.value = addedUser
                loadChatById(channel.id)
            }
        }
    }

    /**
     * Ładowanie czatu po ID
     */
    fun loadChatById(chatId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                signalRConnector.getChannel(chatId)
                signalRConnector.getChannelUsers(chatId)
                signalRConnector.getContactList() // Pobierz listę kontaktów
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Błąd podczas ładowania czatu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Dodawanie użytkownika do czatu
     */
    fun addUserToChannel(userId: Long) {
        val channelId = _selectedChat.value?.id ?: return
        val selectedUser = _availableContacts.value.find { it.id == userId }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                signalRConnector.addUserToChannel(channelId, userId)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Błąd podczas dodawania użytkownika: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Odświeżanie listy dostępnych kontaktów (tych, których jeszcze nie ma w czacie)
     */
    private fun refreshAvailableContacts() {
        val currentMembers = _channelMembers.value.map { it.id }.toSet()
        val allContacts = signalRConnector.contacts.value

        // Filtrujemy kontakty, aby wyświetlić tylko te, których jeszcze nie ma w czacie
        _availableContacts.value = allContacts.filter { contact ->
            !currentMembers.contains(contact.id)
        }
    }

    /**
     * Usuwanie listenerów przy zniszczeniu ViewModel
     */
    fun removeListeners() {
        signalRConnector.onChannelReceived.removeListener("ChatEditView")
        signalRConnector.onChannelMembersReceived.removeListener("ChatEditView")
        signalRConnector.onContactsReceived.removeListener("ChatEditView")
        signalRConnector.onUserAddedToChannel.removeListener("ChatEditView")
    }

    /**
     * Funkcja zwracająca aktualnego zalogowanego użytkownika
     */
    fun getCurrentUser() = signalRConnector.getCurrentUser()

    /**
     * Resetuje informację o dodanym uczestniku (po wyświetleniu komunikatu)
     */
    fun resetAddParticipantFeedback() {
        _addParticipantFeedback.value = null
    }
}
