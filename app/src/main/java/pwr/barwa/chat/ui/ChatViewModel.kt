package pwr.barwa.chat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pwr.barwa.chat.data.dao.ChatDao
import pwr.barwa.chat.data.model.Chat

class ChatViewModel(private val chatDao: ChatDao) : ViewModel() {
    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats

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

            chatDao.insertAll(chat1, chat2)
            loadChats()
        }
    }
}