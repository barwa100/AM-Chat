package pwr.barwa.chat.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pwr.barwa.chat.data.SignalRConnector
import pwr.barwa.chat.data.dao.UserDao
import pwr.barwa.chat.data.dto.UserDto

class ContactsViewModel(private val signalRConnector: SignalRConnector) : ViewModel() {
    private val _contacts = MutableStateFlow<List<UserDto>>(emptyList())
    val contacts: StateFlow<List<UserDto>> = _contacts

    private val _newContactIds = MutableStateFlow<Set<Long>>(emptySet())
    val newContactIds: StateFlow<Set<Long>> = _newContactIds

    // Dodajemy stan odświeżania
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    init {
        loadContacts()
        viewModelScope.launch {
            signalRConnector.onContactAdded.addListener("ContactsView", { contact ->
                Log.d("ContactsViewModel", "Nowy kontakt odebrany: ${contact.userName}, ID: ${contact.id}")
                // Sprawdź, czy kontakt już istnieje na liście przed dodaniem
                if (_contacts.value.none { it.id == contact.id }) {
                    _contacts.value = _contacts.value + contact

                    _newContactIds.value = _newContactIds.value + contact.id

                    Log.d("ContactsViewModel", "Dodano ID do animacji: ${contact.id}, aktualne IDs: $_newContactIds")

                    // Po określonym czasie usuń ID z listy nowych kontaktów (animacja się zakończy)
                    viewModelScope.launch {
                        delay(2000) // Wydłużono czas do 2 sekund
                        _newContactIds.value = _newContactIds.value - contact.id
                        Log.d("ContactsViewModel", "Usunięto ID z animacji po opóźnieniu: ${contact.id}")
                    }
                }
            })

            signalRConnector.contacts.collect { users ->
                // Zachowaj informacje o nowych kontaktach podczas aktualizacji całej listy
                val currentNewIds = _newContactIds.value

                // Filtrowanie duplikatów na podstawie id
                val uniqueUsers = users.distinctBy { it.id }

                if (_contacts.value != uniqueUsers) {
                    Log.d("ContactsViewModel", "Aktualizacja listy kontaktów: ${uniqueUsers.size} kontaktów")
                    _contacts.value = uniqueUsers
                }
            }
        }
    }

    // Aktualizacja metody, aby obsługiwała odświeżanie
    fun loadContacts() {
        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                signalRConnector.getContactList()
            } finally {
                // Dodajemy małe opóźnienie, aby animacja odświeżania była widoczna
                delay(500)
                _isRefreshing.value = false
            }
        }
    }

    fun removeListeners() {
        signalRConnector.onContactAdded.removeListener("ContactsView")
    }

    fun addContact(user: String) {
        viewModelScope.launch {
            signalRConnector.addContact(user)
        }
    }

    // Metoda do ręcznego oznaczania kontaktu jako nowy (dla testów)
    fun markContactAsNew(contactId: Long) {
        _newContactIds.value = _newContactIds.value + contactId
        Log.d("ContactsViewModel", "Ręcznie oznaczono kontakt jako nowy: $contactId")

        viewModelScope.launch {
            delay(2000)
            _newContactIds.value = _newContactIds.value - contactId
        }
    }
}
