package pwr.barwa.chat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pwr.barwa.chat.data.dao.ChatDao
import pwr.barwa.chat.data.model.Chat
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ChatViewModel(private val chatDao: ChatDao) : ViewModel() {
    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats

    // Stany dla dialogów
    private val _showNewChatDialog = MutableStateFlow(false)
    val showNewChatDialog: StateFlow<Boolean> = _showNewChatDialog

    private val _showNewGroupDialog = MutableStateFlow(false)
    val showNewGroupDialog: StateFlow<Boolean> = _showNewGroupDialog

    init {
        loadChats()
    }

    private fun loadChats() {
        viewModelScope.launch {
            _chats.value = chatDao.getAllChats()
        }
    }

    fun addDummyChat() {
        viewModelScope.launch {
            val chat1 = Chat(
                name = "New Chat ${System.currentTimeMillis()}",
                lastMessage = "Hello there!"
            )
            val chat2 = Chat(
                name = "New Chat ${System.currentTimeMillis()}",
                lastMessage = "General Kenobi."
            )

            chatDao.insert(chat1)
            chatDao.insert(chat2)
            loadChats()
        }
    }

    fun startNewChat(chatName: String, initialMessage: String = "") {
        viewModelScope.launch {
            try {
                val newChat = Chat(
                    name = chatName,
                    lastMessage = initialMessage.ifEmpty { "Chat started" },
                    timestamp = System.currentTimeMillis(),
                    isGroup = false
                )
                chatDao.insert(newChat)
                loadChats()
            } catch (e: Exception) {
                println("Chat start error: ${e.message}")
            }
        }
    }

    fun createNewGroup(groupName: String, members: List<String>) {
        viewModelScope.launch {
            try {
                val newGroup = Chat(
                    name = groupName,
                    lastMessage = "Group created with ${members.size} members",
                    timestamp = System.currentTimeMillis(),
                    isGroup = true
                )
                chatDao.insert(newGroup)
                loadChats()
            } catch (e: Exception) {
                println("Błąd tworzenia grupy: ${e.message}")
            }
        }
    }

    // Obsługa dialogów
    fun onNewChatClick() { _showNewChatDialog.value = true }
    fun onNewGroupClick() { _showNewGroupDialog.value = true }
    fun dismissNewChatDialog() { _showNewChatDialog.value = false }
    fun dismissNewGroupDialog() { _showNewGroupDialog.value = false }
}