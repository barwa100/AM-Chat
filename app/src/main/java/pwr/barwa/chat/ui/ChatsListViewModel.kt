package pwr.barwa.chat.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pwr.barwa.chat.data.SignalRConnector
import pwr.barwa.chat.data.dto.ChannelDto
import pwr.barwa.chat.data.requests.CreateChannelRequest

/**
 * ViewModel odpowiedzialny za zarządzanie listą czatów
 */
class ChatsListViewModel(private val signalRConnector: SignalRConnector) : ViewModel() {
    private val _chats = MutableStateFlow<List<ChannelDto>>(emptyList())
    val chats: StateFlow<List<ChannelDto>> = _chats

    // Stany dla dialogów
    private val _showNewChatDialog = MutableStateFlow(false)
    val showNewChatDialog: StateFlow<Boolean> = _showNewChatDialog

    private val _showNewGroupDialog = MutableStateFlow(false)
    val showNewGroupDialog: StateFlow<Boolean> = _showNewGroupDialog

    // Upload states
    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading

    private val _uploadError = MutableStateFlow<String?>(null)
    val uploadError: StateFlow<String?> = _uploadError

    init {
        loadChats()
        signalRConnector.onChannelListReceived.addListener("ChatsListView", { channels ->
            _chats.value = channels
        })

        viewModelScope.launch {
            signalRConnector.channels.collect { channels ->
                _chats.value = channels
            }
        }
    }

    fun removeListeners() {
        signalRConnector.onChannelListReceived.removeListener("ChatsListView")
    }

    fun loadChats() {
        viewModelScope.launch {
            signalRConnector.requestChannelList()
        }
    }

    fun startNewChat(chatName: String, avatarUri: Uri?, context: Context, initialMessage: String = "") {
        viewModelScope.launch {
            try {
                // Upload the image if one was selected
                _isUploading.value = true
                _uploadError.value = null
                val imageString: String? = avatarUri?.toString()
                signalRConnector.createChannel(
                    CreateChannelRequest(
                        Name = chatName,
                        UserIds = listOf(), // Add members if needed
                        Image = imageString // Add image if needed
                    )
                )
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
