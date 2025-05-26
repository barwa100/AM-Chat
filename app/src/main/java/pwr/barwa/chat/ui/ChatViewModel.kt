package pwr.barwa.chat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import pwr.barwa.chat.data.SignalRConnector
import pwr.barwa.chat.data.dao.ChatDao
import pwr.barwa.chat.data.dto.ChannelDto
import pwr.barwa.chat.data.dto.MessageDto
import pwr.barwa.chat.data.dto.UserDto
import pwr.barwa.chat.data.model.Chat
import pwr.barwa.chat.data.requests.CreateChannelRequest
import pwr.barwa.chat.data.requests.SendTextMessage
import kotlin.math.sign


class ChatViewModel(private val signalRConnector: SignalRConnector) : ViewModel() {
    private val _chats = MutableStateFlow<List<ChannelDto>>(emptyList())
    val chats: StateFlow<List<ChannelDto>> = _chats

    //Zwracanie konkretnego czatu
    private val _selectedChat = MutableStateFlow<ChannelDto?>(null)
    val selectedChat: StateFlow<ChannelDto?> = _selectedChat

    // Stany dla dialogów
    private val _showNewChatDialog = MutableStateFlow(false)
    val showNewChatDialog: StateFlow<Boolean> = _showNewChatDialog

    private val _showNewGroupDialog = MutableStateFlow(false)
    val showNewGroupDialog: StateFlow<Boolean> = _showNewGroupDialog

    private val _channelMessages = MutableStateFlow<List<MessageDto>>(emptyList())
    val channelMessages: StateFlow<List<MessageDto>> = _channelMessages

    private val _channelMembers = MutableStateFlow<List<UserDto>>(emptyList())
    val channelMembers: StateFlow<List<UserDto>> = _channelMembers

    init {
        loadChats()
        signalRConnector.onChannelListReceived.addListener("ChatView", { channels ->
            _chats.value = channels
        })
        signalRConnector.onChannelReceived.addListener("ChatView", { channelDto ->
            _selectedChat.value = channelDto
        })
        viewModelScope.launch {
            signalRConnector.channels.collect { channels ->
                _chats.value = channels
            }
        }
        viewModelScope.launch {
            signalRConnector.messages.collect { messages ->
                _channelMessages.value = messages.filter {
                    _selectedChat.value?.id == it.channelId
                }
            }
        }
        viewModelScope.launch {
            signalRConnector.users.collect { members ->
                _channelMembers.value = members.filter {
                    _selectedChat.value?.members?.contains(it.id) ?: false
                }
            }
        }
    }
    fun onChannelReceived(channel: ChannelDto) {
        _selectedChat.value = channel
    }
    fun removeListeners() {
        signalRConnector.onChannelReceived.removeListener("ChatView")
        signalRConnector.onChannelListReceived.removeListener("ChatView")
    }

    fun loadChats() {
        viewModelScope.launch {
            signalRConnector.requestChannelList()
        }
    }

    fun startNewChat(chatName: String, initialMessage: String = "") {
        viewModelScope.launch {
            try {
                signalRConnector.createChannel(
                    CreateChannelRequest(
                        Name = chatName,
                        UserIds = listOf() // Add members if needed
                    )
                )
            } catch (e: Exception) {
                println("Chat start error: ${e.message}")
            }
        }
    }

    fun createNewGroup(groupName: String, members: List<Long>) {
        viewModelScope.launch {
            try {
                signalRConnector.createChannel(
                    CreateChannelRequest(
                        Name = groupName,
                        UserIds = members
                    )
                )
                loadChats()
            } catch (e: Exception) {
                println("Error creating group: ${e.message}")
            }
        }
    }

    fun loadChatById(chatId: Long) {
        viewModelScope.launch {
            signalRConnector.getChannel(chatId)
        }.also {
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
                } catch (e: Exception) {
                    println("Error sending message: ${e.message}")
                }
            }
        }
    }

    fun deleteChat(chatId: Long) {
        viewModelScope.launch {
            try {
                //chatDao.deleteChat(chatId)
                loadChats()
                _selectedChat.value = null
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