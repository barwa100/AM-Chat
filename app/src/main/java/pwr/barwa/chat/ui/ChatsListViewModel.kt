package pwr.barwa.chat.ui

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pwr.barwa.chat.data.SignalRConnector
import pwr.barwa.chat.data.dto.ChannelDto
import pwr.barwa.chat.data.requests.CreateChannelRequest


class ChatsListViewModel(private val signalRConnector: SignalRConnector) : ViewModel() {
    private val _chats = MutableStateFlow<List<ChannelDto>>(emptyList())
    val chats: StateFlow<List<ChannelDto>> = _chats

    private val _newChatIds = MutableStateFlow<Set<Long>>(emptySet())
    val newChatIds: StateFlow<Set<Long>> = _newChatIds

    private val _showNewChatDialog = MutableStateFlow(false)
    val showNewChatDialog: StateFlow<Boolean> = _showNewChatDialog

    private val _showNewGroupDialog = MutableStateFlow(false)
    val showNewGroupDialog: StateFlow<Boolean> = _showNewGroupDialog

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading

    private val _uploadError = MutableStateFlow<String?>(null)
    val uploadError: StateFlow<String?> = _uploadError

    // Dodajemy stan odświeżania
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    init {
        loadChats()

        // Pobierz dane o aktualnym użytkowniku
        signalRConnector.requestCurrentUser()

        signalRConnector.onChannelListReceived.addListener("ChatsListView", { channels ->
            _chats.value = sortChannels(distinctById(channels))
        })

        signalRConnector.onChannelCreated.addListener("ChatsListView", { channel ->
            Log.d("ChatsListViewModel", "Nowy czat utworzony: ${channel.name}, ID: ${channel.id}")

            // Sprawdź czy kanał z takim ID już istnieje na liście
            if (_chats.value.none { it.id == channel.id }) {
                _newChatIds.value = _newChatIds.value + channel.id
                // Aktualizuj listę czatów dodając tylko ten nowy kanał i eliminując ewentualne duplikaty
                _chats.value = sortChannels(distinctById(_chats.value + channel))

                viewModelScope.launch {
                    delay(2000)
                    _newChatIds.value = _newChatIds.value - channel.id
                    Log.d("ChatsListViewModel", "Usunięto ID z animacji: ${channel.id}")
                }
            } else {
                Log.d("ChatsListViewModel", "Kanał o ID: ${channel.id} już istnieje, ignoruję")
            }
        })

        viewModelScope.launch {
            signalRConnector.channels.collect { channels ->
                _chats.value = sortChannels(distinctById(channels))
            }
        }
    }

    private fun sortChannels(channels: List<ChannelDto>): List<ChannelDto> {
        return channels.sortedByDescending { channel ->
            when {
                channel.lastMessage != null -> channel.lastMessage.created
                _newChatIds.value.contains(channel.id) -> System.currentTimeMillis()
                else -> channel.created
            }
        }
    }

    // Funkcja pomocnicza do eliminowania duplikatów przez ID
    private fun distinctById(channels: List<ChannelDto>): List<ChannelDto> {
        return channels.distinctBy { it.id }
    }

    fun removeListeners() {
        signalRConnector.onChannelListReceived.removeListener("ChatsListView")
        signalRConnector.onChannelCreated.removeListener("ChatsListView")
    }

    fun markChatAsNew(chatId: Long) {
        _newChatIds.value = _newChatIds.value + chatId
        Log.d("ChatsListViewModel", "Ręcznie oznaczono czat jako nowy: $chatId")

        viewModelScope.launch {
            delay(2000)
            _newChatIds.value = _newChatIds.value - chatId
        }
    }

    fun loadChats() {
        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                signalRConnector.requestChannelList()
            } finally {
                // Dodajemy małe opóźnienie, aby animacja odświeżania była widoczna
                delay(500)
                _isRefreshing.value = false
            }
        }
    }

    fun startNewChat(chatName: String, avatarUri: Uri?, context: Context, userId: Long) {
        viewModelScope.launch {
            try {
                _isUploading.value = true
                _uploadError.value = null
                val imageString: String? = avatarUri?.toString()
                signalRConnector.createChannel(
                    CreateChannelRequest(
                        Name = chatName,
                        UserIds = listOf(userId), // Add members if needed
                        Image = imageString // Add image if needed
                    )
                )
                loadChats()
            } catch (e: Exception) {
                _uploadError.value = "Failed to create chat: ${e.message}"
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun createNewGroup(groupName: String, members: List<Long>, avatarUri: Uri?, context: Context) {
        viewModelScope.launch {
            try {
                _isUploading.value = true
                _uploadError.value = null
                val imageString: String? = avatarUri?.toString()
                signalRConnector.createChannel(
                    CreateChannelRequest(
                        Name = groupName,
                        UserIds = members,
                        Image = imageString
                    )
                )
                loadChats()
            } catch (e: Exception) {
                _uploadError.value = "Failed to create group: ${e.message}"
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun deleteChat(chatId: Long) {
        viewModelScope.launch {
            try {
                // Usuń na serwerze
                signalRConnector.deleteChannel(chatId)

                // Odśwież dane
                loadChats()
            } catch (e: Exception) {
                println("Error deleting chat: ${e.message}")
            }
        }
    }

    // Obsługa dialogów
    fun onNewChatClick() { _showNewChatDialog.value = true }
    fun onNewGroupClick() { _showNewGroupDialog.value = true }
    fun dismissNewChatDialog() { _showNewChatDialog.value = false }
    fun dismissNewGroupDialog() { _showNewGroupDialog.value = false }
}
