package pwr.barwa.chat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pwr.barwa.chat.data.SignalRConnector
import pwr.barwa.chat.data.dao.ChatDao
import pwr.barwa.chat.data.dto.ChannelDto
import pwr.barwa.chat.data.model.Chat
import pwr.barwa.chat.data.requests.CreateChannelRequest


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

    init {
        loadChats()
        signalRConnector.onChannelReceived.addListener { channelDto ->
            _selectedChat.value = channelDto
        }
        signalRConnector.onChannelCreated.addListener {
            _chats.value += it
        }
    }

    private fun loadChats() {
        viewModelScope.launch {
            signalRConnector.requestChannelList()
            _chats.value = signalRConnector.channels.value

        }
    }

    fun startNewChat(chatName: String, initialMessage: String = "") {
        viewModelScope.launch {
            try {
                signalRConnector.createChannel(
                    CreateChannelRequest(
                        name = chatName,
                        members = listOf() // Add members if needed
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
                        name = groupName,
                        members = members
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
            signalRConnector.channel
        }
    }

    fun deleteChat(chatId: Long) {
        viewModelScope.launch {
            try {
                chatDao.deleteChat(chatId)
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