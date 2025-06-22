package pwr.barwa.chat.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pwr.barwa.chat.data.SignalRConnector
import pwr.barwa.chat.data.dto.ChannelDto
import pwr.barwa.chat.data.dto.MessageDto
import pwr.barwa.chat.data.dto.UserDto
import pwr.barwa.chat.data.requests.CreateChannelRequest
import pwr.barwa.chat.data.requests.SendTextMessage
import android.content.Context

/**
 * @deprecated Ta klasa została podzielona na ChatsListViewModel i ChatDetailsViewModel.
 * Należy użyć jednej z tych klas zamiast tej.
 */
@Deprecated("Użyj ChatsListViewModel lub ChatDetailsViewModel zamiast tej klasy")
class ChatViewModel(private val signalRConnector: SignalRConnector) : ViewModel() {
    // Tworzenie instancji nowych ViewModeli
    private val chatsListViewModel = ChatsListViewModel(signalRConnector)
    private val chatDetailsViewModel = ChatDetailsViewModel(signalRConnector)

    // Delegacja stanów do odpowiednich ViewModeli
    val chats: StateFlow<List<ChannelDto>> = chatsListViewModel.chats
    val selectedChat: StateFlow<ChannelDto?> = chatDetailsViewModel.selectedChat
    val showNewChatDialog: StateFlow<Boolean> = chatsListViewModel.showNewChatDialog
    val showNewGroupDialog: StateFlow<Boolean> = chatsListViewModel.showNewGroupDialog
    val channelMessages: StateFlow<List<MessageDto>> = chatDetailsViewModel.channelMessages
    val channelMembers: StateFlow<List<UserDto>> = chatDetailsViewModel.channelMembers
    val isUploading: StateFlow<Boolean> = chatsListViewModel.isUploading
    val uploadError: StateFlow<String?> = chatsListViewModel.uploadError

    // Delegacja funkcji do odpowiednich ViewModeli
    fun onChannelReceived(channel: ChannelDto) = chatDetailsViewModel.onChannelReceived(channel)

    fun removeListeners() {
        chatsListViewModel.removeListeners()
        chatDetailsViewModel.removeListeners()
    }

    fun loadChats() = chatsListViewModel.loadChats()

    fun loadChatById(chatId: Long) = chatDetailsViewModel.loadChatById(chatId)

    fun sendMessage(message: String) = chatDetailsViewModel.sendMessage(message)

    fun deleteChat(chatId: Long) = chatsListViewModel.deleteChat(chatId)

    // Obsługa dialogów
    fun onNewChatClick() = chatsListViewModel.onNewChatClick()
    fun onNewGroupClick() = chatsListViewModel.onNewGroupClick()
    fun dismissNewChatDialog() = chatsListViewModel.dismissNewChatDialog()
    fun dismissNewGroupDialog() = chatsListViewModel.dismissNewGroupDialog()
}

