package pwr.barwa.chat.ui

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pwr.barwa.chat.data.SignalRConnector
import pwr.barwa.chat.data.dto.ChannelDto
import pwr.barwa.chat.data.dto.MessageDto
import pwr.barwa.chat.data.dto.UserDto
import pwr.barwa.chat.data.model.MediaType
import pwr.barwa.chat.data.model.Message
import pwr.barwa.chat.data.requests.SendTextMessage
import pwr.barwa.chat.data.requests.SendMediaMessage
import pwr.barwa.chat.data.dto.MessageType
import pwr.barwa.chat.services.AuthService
import pwr.barwa.chat.services.MediaStorageService
import java.io.File

class ChatDetailsViewModel(
    private val signalRConnector: SignalRConnector,
    private val context: Context
) : ViewModel() {

    private val _selectedChat = MutableStateFlow<ChannelDto?>(null)
    val selectedChat: StateFlow<ChannelDto?> = _selectedChat

    private val _channelMessages = MutableStateFlow<List<MessageDto>>(emptyList())
    val channelMessages: StateFlow<List<MessageDto>> = _channelMessages

    private val _channelMembers = MutableStateFlow<List<UserDto>>(emptyList())
    val channelMembers: StateFlow<List<UserDto>> = _channelMembers

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _mediaDownloadProgress = MutableStateFlow<MediaStorageService.MediaDownloadProgress?>(null)
    val mediaDownloadProgress: StateFlow<MediaStorageService.MediaDownloadProgress?> = _mediaDownloadProgress

    private val _localMediaPaths = MutableStateFlow<Map<Long, String>>(emptyMap())
    val localMediaPaths: StateFlow<Map<Long, String>> = _localMediaPaths

    // Stan przechowujący ID nowych wiadomości (do animacji)
    private val _newMessageIds = MutableStateFlow<Set<Long>>(emptySet())
    val newMessageIds: StateFlow<Set<Long>> = _newMessageIds

    // Usługa do zarządzania mediami
    private val mediaStorageService = MediaStorageService(context)

    init {
        signalRConnector.onChannelReceived.addListener("ChatDetailsView", { channelDto ->
            _selectedChat.value = channelDto
        })

        signalRConnector.onChannelMessagesReceived.addListener("ChatDetailsView", { messages ->
            // Zapisz aktualną listę wiadomości, aby móc porównać ją z nową i znaleźć nowe wiadomości
            val currentMessages = _channelMessages.value
            val newMessages = messages.filter { newMessage ->
                currentMessages.none { it.id == newMessage.id }
            }

            // Dodaj nowe wiadomości do listy wiadomości do animacji
            if (newMessages.isNotEmpty()) {
                val newIds = newMessages.map { it.id }.toSet()
                _newMessageIds.value = _newMessageIds.value + newIds
            }

            _channelMessages.value = messages
            // Po otrzymaniu wiadomości, rozpocznij pobieranie mediów
            downloadMediaForMessages(messages)
        })

        signalRConnector.onChannelMembersReceived.addListener("ChatDetailsView", { members ->
            _channelMembers.value = members
        })

        // Zbieranie aktualizacji z SignalR flows
        viewModelScope.launch {
            signalRConnector.messages.collect { messages ->
                if (_selectedChat.value != null) {
                    val filteredMessages = messages.filter {
                        it.channelId == _selectedChat.value?.id
                    }

                    // Zapisz aktualną listę wiadomości, aby móc porównać ją z nową i znaleźć nowe wiadomości
                    val currentMessages = _channelMessages.value
                    val newMessages = filteredMessages.filter { newMessage ->
                        currentMessages.none { it.id == newMessage.id }
                    }

                    // Dodaj nowe wiadomości do listy wiadomości do animacji
                    if (newMessages.isNotEmpty()) {
                        val newIds = newMessages.map { it.id }.toSet()
                        _newMessageIds.value = _newMessageIds.value + newIds
                    }

                    _channelMessages.value = filteredMessages
                    downloadMediaForMessages(filteredMessages)
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

    /**
     * Pobieranie mediów dla wiadomości z załącznikami
     */
    private fun downloadMediaForMessages(messages: List<MessageDto>) {
        // Pobieraj tylko wiadomości z załącznikami mediów
        val mediaMessages = messages.filter { it.type != MessageType.TEXT }

        if (mediaMessages.isEmpty()) return

        viewModelScope.launch {
            // Dla każdej wiadomości z mediami, sprawdź czy już została pobrana
            mediaMessages.forEach { messageDto ->
                // Jeśli już mamy ścieżkę lokalną dla tej wiadomości, pomijamy
                if (_localMediaPaths.value.containsKey(messageDto.id)) {
                    return@forEach
                }

                // Konwertuj MessageDto na Message
                val message = Message(
                    id = messageDto.id,
                    chatId = messageDto.channelId,
                    senderId = messageDto.senderId,
                    content = messageDto.data,
                    timestamp = messageDto.created,
                    isRead = true,
                    mediaType = when (messageDto.type) {
                        MessageType.IMAGE -> MediaType.IMAGE
                        MessageType.VIDEO -> MediaType.VIDEO
                        MessageType.AUDIO -> MediaType.AUDIO
                        else -> null
                    },
                    mediaExtension = "." + messageDto.data,
                    mediaUrl = AuthService.URL_BASE + "Media/" + messageDto.id + "." + messageDto.data
                )

                // Pobierz medium
                mediaStorageService.downloadAndSaveMedia(message).fold(
                    onSuccess = { localPath ->
                        // Zaktualizuj mapę lokalnych ścieżek
                        val updatedPaths = _localMediaPaths.value.toMutableMap()
                        updatedPaths[messageDto.id] = localPath
                        _localMediaPaths.value = updatedPaths
                        Log.d("ChatDetailsViewModel", "Media downloaded for message ${messageDto.id}: $localPath")
                    },
                    onFailure = { error ->
                        Log.e("ChatDetailsViewModel", "Error downloading media for message ${messageDto.id}: ${error.message}")
                        _errorMessage.value = "Nie udało się pobrać mediów: ${error.message}"
                    }
                )
            }
        }
    }

    /**
     * Pobierz wszystkie media dla aktualnych wiadomości
     */
    fun downloadAllPendingMedia() {
        viewModelScope.launch {
            mediaStorageService.downloadAllPendingMedia().collectLatest { progress ->
                _mediaDownloadProgress.value = progress
            }
        }
    }

    /**
     * Pobierz Uri do lokalnego pliku
     */
    fun getMediaUri(messageId: Long): Uri? {
        val localPath = _localMediaPaths.value[messageId] ?: return null
        return mediaStorageService.getMediaUri(localPath)
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
                        SendMediaMessage.fromByteArray(
                            channelId = chat.id,
                            byteArray = mediaData,
                            extension = extension,
                            messageType = messageType
                        )
                    )
                    // Po wysłaniu wiadomości, odśwież listę wiadomości
                    signalRConnector.getChannelMessages(chat.id)
                } catch (e: Exception) {
                    Log.e("ChatDetailsViewModel", "Błąd podczas wysyłania wiadomości medialnej: ${e.message}", e)
                    _errorMessage.value = "Nie udało się wysłać wiadomości: ${e.message}"
                }
            }
        } ?: run {
            Log.e("ChatDetailsViewModel", "Próba wysłania wiadomości bez wybranego czatu")
            _errorMessage.value = "Nie wybrano czatu"
        }
    }

    // Funkcja zwracająca aktualnego zalogowanego użytkownika
    fun getCurrentUser() = signalRConnector.getCurrentUser()

    /**
     * Sprawdza, czy wiadomość jest nowa i powinna być animowana
     */
    fun isNewMessage(messageId: Long): Boolean {
        return _newMessageIds.value.contains(messageId)
    }

    /**
     * Oznacza wiadomość jako już nie nową (po wyświetleniu animacji)
     */
    fun markMessageDisplayed(messageId: Long) {
        _newMessageIds.value = _newMessageIds.value - messageId
    }
}
