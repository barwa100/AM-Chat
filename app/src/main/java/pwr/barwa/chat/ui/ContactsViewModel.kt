package pwr.barwa.chat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pwr.barwa.chat.data.SignalRConnector
import pwr.barwa.chat.data.dao.UserDao
import pwr.barwa.chat.data.dto.UserDto

class ContactsViewModel(private val signalRConnector: SignalRConnector) : ViewModel() {
    private val _contacts = MutableStateFlow<List<UserDto>>( emptyList())
    val contacts: StateFlow<List<UserDto>> = _contacts

    init {
        loadContacts()
        viewModelScope.launch {
            signalRConnector.contacts.collect { users ->
                _contacts.value = users
            }
        }
    }

    private fun loadContacts() {
        viewModelScope.launch {
            signalRConnector.getContactList()
        }
    }

    fun removeListeners() {
        signalRConnector.onContactsReceived.removeListener("ContactsView")
        signalRConnector.onContactAdded.removeListener("ContactsView")
    }

    fun addContact(user: String) {
        viewModelScope.launch {
            signalRConnector.addContact(user)
        }
    }

}