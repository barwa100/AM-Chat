package pwr.barwa.chat.data

import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import com.microsoft.signalr.TypeReference
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pwr.barwa.chat.data.dto.ChannelDto
import pwr.barwa.chat.data.dto.MessageDto
import pwr.barwa.chat.data.dto.UserDto
import pwr.barwa.chat.data.requests.CreateChannelRequest
import pwr.barwa.chat.data.requests.SendMediaMessage
import pwr.barwa.chat.data.requests.SendTextMessage
import pwr.barwa.chat.services.AuthService
import kotlin.jvm.java

class SignalRConnector(val token: String) {

    private val hubConnection: HubConnection by lazy {
        HubConnectionBuilder
            .create(AuthService.URL_BASE + "ws")
            .withAccessTokenProvider(Single.just(token))
            .build()
    }

    private val _messages = MutableStateFlow<List<MessageDto>>(emptyList())
    val messages: StateFlow<List<MessageDto>> = _messages

    private val _channels = MutableStateFlow<List<ChannelDto>>(emptyList())
    val channels: StateFlow<List<ChannelDto>> = _channels

    private val _contacts = MutableStateFlow<List<UserDto>>(emptyList())
    val contacts: StateFlow<List<UserDto>> = _contacts

    private val __users = MutableStateFlow<List<UserDto>>(emptyList())
    val users: StateFlow<List<UserDto>> = __users

    private val _currentUser = MutableStateFlow<UserDto?>(null)
    val currentUser: StateFlow<UserDto?> = _currentUser

    val onMessageReceived = Event<MessageDto>()
    val onChannelListReceived = Event<List<ChannelDto>>()
    val onContactsReceived = Event<List<UserDto>>()
    val onChannelReceived = Event<ChannelDto>()
    val onChannelMembersReceived = Event<List<UserDto>>()
    val onChannelMessagesReceived = Event<List<MessageDto>>()
    val onChannelCreated = Event<ChannelDto>()
    val onContactAdded = Event<UserDto>()
    val onUserAddedToChannel = Event<Triple<ChannelDto, UserDto, UserDto>>()
    val onChannelDeleted = Event<Long>()
    private val scope = CoroutineScope(Dispatchers.IO)

    suspend fun startConnection() {
        hubConnection.on("ReceiveMessage", { message ->
            _messages.value += message
            onMessageReceived.invoke(message)
        }, MessageDto::class.java)

        hubConnection.on("GetChannels", { channels: List<ChannelDto> ->
            _channels.value = channels
            onChannelListReceived.invoke(channels)
        }, object: TypeReference<List<ChannelDto>>() {}.type)

        hubConnection.on("GetChannel", { channel: ChannelDto ->
            if (_channels.value.any { it.id == channel.id }) {
                _channels.value = _channels.value.map {
                    if (it.id == channel.id) channel else it
                }
            } else {
                _channels.value += channel
            }
            onChannelReceived.invoke(channel)
        }, ChannelDto::class.java)

        hubConnection.on("GetContacts", { contacts: List<UserDto> ->
            _contacts.value = contacts
            onContactsReceived.invoke(contacts)
        }, object : TypeReference<List<UserDto>>() {}.type)

        hubConnection.on("GetChannelMembers", { members: List<UserDto> ->
            __users.value = members
            onChannelMembersReceived.invoke(members)
        }, object : TypeReference<List<UserDto>>() {}.type)

        hubConnection.on("GetChannelMessages", { messages: List<MessageDto> ->
            _messages.value = messages
            onChannelMessagesReceived.invoke(messages)
        }, object : TypeReference<List<MessageDto>>() {}.type)

        hubConnection.on("ChannelCreated", { channel: ChannelDto ->
            _channels.value += channel
            onChannelCreated.invoke(channel)
        }, ChannelDto::class.java)

        hubConnection.on("NewContact", { user: UserDto ->
            _contacts.value += user
            onContactAdded.invoke(user)
        }, UserDto::class.java)
        hubConnection.on("UserAddedToChannel", { channel: ChannelDto, user: UserDto, addedBy: UserDto ->
            _channels.value = _channels.value.map {
                if (it.id == channel.id) it.copy(members = it.members + user.id) else it
            }
            onUserAddedToChannel.invoke(Triple(channel, user, addedBy))
        }, ChannelDto::class.java, UserDto::class.java, UserDto::class.java)

        hubConnection.on("ChannelDeleted", { deletedChannelId: Long ->
            _channels.value = _channels.value.filter { it.id != deletedChannelId }
            onChannelDeleted.invoke(deletedChannelId)
        }, Long::class.java)

        hubConnection.onClosed { error ->
            println("Połączenie zostało utracone: ${error?.message}")
            scope.launch {
                reconnect()
            }
        }

        reconnect()
    }
    private suspend fun reconnect(){
        var retryCount = 0
        val maxRetries = 5000
        val retryDelay = 2000L // 2 sekundy

        while (hubConnection.connectionState != HubConnectionState.CONNECTED && retryCount < maxRetries) {
            try {
                println("Próba ponownego połączenia... ($retryCount)")
                hubConnection.start().blockingAwait()
                println("Połączono ponownie!")
            } catch (e: Exception) {
                println("Nie udało się połączyć: ${e.message}")
                retryCount++
                kotlinx.coroutines.delay(retryDelay)
            }
        }

        if (hubConnection.connectionState != HubConnectionState.CONNECTED) {
            println("Nie udało się połączyć po $maxRetries próbach.")
        }
    }
    fun stopConnection() {
        hubConnection.stop()
    }
    fun sendMessage(message: SendTextMessage) {
        hubConnection.send("SendMessage", message)
    }
    fun sendMediaMessage(message: SendMediaMessage) {
        hubConnection.send("SendMediaMessage", message)
    }
    fun requestChannelList() {
        hubConnection.send("GetChannels")
    }
    fun createChannel(createChannelRequest: CreateChannelRequest) {
        hubConnection.send("CreateChannel", createChannelRequest)
    }
    fun addUserToChannel(channelId: Long, userId: Long) {
        hubConnection.send("AddToChannel", channelId, userId)
    }
    fun getContactList() {
        hubConnection.send("GetContacts")
    }
    fun addContact(userName: String) {
        hubConnection.send("AddContact", userName)
    }
    fun getChannelMessages(channelId: Long, beforeMessageId: Long? = null, limit: Int = 50) {
        hubConnection.send("GetChannelMessages", channelId, beforeMessageId, limit)
    }
    fun getChannelUsers(channelId: Long) {
        hubConnection.send("GetChannelMembers", channelId)
    }
    fun getChannel(channelId: Long) {
        hubConnection.send("GetChannel", channelId)
    }
    fun deleteChannel(channelId: Long) {
        hubConnection.send("DeleteChannel", channelId) //lub chatId?
    }
    fun setCurrentUser(user: UserDto) {
        _currentUser.value = user
    }
    fun getCurrentUser(): UserDto? = _currentUser.value


    companion object {

        @Volatile
        private var Instance : SignalRConnector? = null
        fun getInstance(token: String? = null): SignalRConnector {
            if (Instance == null && token == null) {
                throw IllegalArgumentException("Token must not be null")
            }
            return Instance ?: synchronized(this) {
                Instance ?: SignalRConnector(token!!).also {
                    Instance = it
                }
            }
        }

    }
}