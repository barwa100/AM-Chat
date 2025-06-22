package pwr.barwa.chat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pwr.barwa.chat.data.SignalRConnector
import pwr.barwa.chat.data.dto.ChannelDto
import pwr.barwa.chat.data.dto.MessageDto
import pwr.barwa.chat.data.dto.UserDto
import pwr.barwa.chat.data.requests.SendTextMessage
import pwr.barwa.chat.data.requests.SendMediaMessage
import pwr.barwa.chat.data.dto.MessageType

/**
 * ViewModel odpowiedzialny za szczegóły czatu, wiadomości i członków
 */
class ChatDetailsViewModel(private val signalRConnector: SignalRConnector) : ViewModel() {

    private val _selectedChat = MutableStateFlow<ChannelDto?>(null)
    val selectedChat: StateFlow<ChannelDto?> = _selectedChat

    private val _channelMessages = MutableStateFlow<List<MessageDto>>(emptyList())
    val channelMessages: StateFlow<List<MessageDto>> = _channelMessages

    private val _channelMembers = MutableStateFlow<List<UserDto>>(emptyList())
    val channelMembers: StateFlow<List<UserDto>> = _channelMembers

    init {
        signalRConnector.onChannelReceived.addListener("ChatDetailsView", { channelDto ->
            _selectedChat.value = channelDto
        })

        signalRConnector.onChannelMessagesReceived.addListener("ChatDetailsView", { messages ->
            _channelMessages.value = messages
        })

        signalRConnector.onChannelMembersReceived.addListener("ChatDetailsView", { members ->
            _channelMembers.value = members
        })

        // Zbieranie aktualizacji z SignalR flows
        viewModelScope.launch {
            signalRConnector.messages.collect { messages ->
                if (_selectedChat.value != null) {
                    _channelMessages.value = messages.filter {
                        it.channelId == _selectedChat.value?.id
                    }
                }
            }
        }

        viewModelScope.launch {
            signalRConnector.users.collect { users ->
                if (_selectedChat.value != null) {
                    _channelMembers.value = users.filter {
                        _selectedChat.value?.members?.contains(it.id) ?: false
                    }
                }
            }
        }
    }

    fun onChannelReceived(channel: ChannelDto) {
        _selectedChat.value = channel
    }

    fun removeListeners() {
        signalRConnector.onChannelReceived.removeListener("ChatDetailsView")
        signalRConnector.onChannelMessagesReceived.removeListener("ChatDetailsView")
        signalRConnector.onChannelMembersReceived.removeListener("ChatDetailsView")
    }

    fun loadChatById(chatId: Long) {
        viewModelScope.launch {
            signalRConnector.getChannel(chatId)
            signalRConnector.getChannelUsers(chatId)
            signalRConnector.getChannelMessages(chatId)
        }
    }

    fun sendMessage(message: String) {
        _selectedChat.value?.let { chat ->
            viewModelScope.launch {
                try {
                    signalRConnector.sendMessage(SendTextMessage(
                        channelId = chat.id,
                        text = message
                    ))
                    // Po wysłaniu wiadomości, odśwież listę wiadomości
                    signalRConnector.getChannelMessages(chat.id)
                } catch (e: Exception) {
                    println("Error sending message: ${e.message}")
                }
            }
        }
    }

    fun clearChatState() {
        _selectedChat.value = null
        _channelMessages.value = emptyList()
        _channelMembers.value = emptyList()
    }

    fun sendMediaMessage(mediaData: ByteArray, extension: String, messageType: MessageType) {
        _selectedChat.value?.let { chat ->
            viewModelScope.launch {
                try {
                    signalRConnector.sendMediaMessage(
                        SendMediaMessage(
                            channelId = chat.id,
                            data = mediaData,
                            extension = extension,
                            messageType = messageType
                        )
                    )
                    // Po wysłaniu wiadomości, odśwież listę wiadomości
                    signalRConnector.getChannelMessages(chat.id)
                } catch (e: Exception) {
                    println("Error sending media message: ${e.message}")
                }
            }
        }
    }
}
