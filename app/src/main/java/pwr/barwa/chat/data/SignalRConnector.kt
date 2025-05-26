package pwr.barwa.chat.data

import android.content.Context
import androidx.room.Room
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.TypeReference
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import pwr.barwa.chat.data.dto.ChannelDto
import pwr.barwa.chat.data.dto.MessageDto
import pwr.barwa.chat.data.dto.UserDto
import pwr.barwa.chat.data.requests.CreateChannelRequest
import pwr.barwa.chat.data.requests.SendMediaMessage
import pwr.barwa.chat.data.requests.SendTextMessage
import pwr.barwa.chat.services.AuthService
import java.lang.reflect.Type
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

    private val _contacts = MutableStateFlow<List<Long>>(emptyList())
    val contacts: StateFlow<List<Long>> = _contacts

    private val __users = MutableStateFlow<List<UserDto>>(emptyList())
    val users: StateFlow<List<UserDto>> = __users

    val onMessageReceived = Event<MessageDto>()
    val onChannelListReceived = Event<List<ChannelDto>>()
    val onContactsReceived = Event<List<UserDto>>()
    val onChannelReceived = Event<ChannelDto>()
    val onChannelMembersReceived = Event<List<UserDto>>()
    val onChannelMessagesReceived = Event<List<MessageDto>>()
    val onChannelCreated = Event<ChannelDto>()
    val onContactAdded = Event<UserDto>()
    val onUserAddedToChannel = Event<Triple<ChannelDto, UserDto, UserDto>>()

    suspend fun startConnection() {
        val channelType = object : TypeReference<List<ChannelDto>>() {}.type
        hubConnection.on("ReceiveMessage", { message ->
            _messages.value += message
            onMessageReceived.invoke(message)
        }, MessageDto::class.java)

        hubConnection.on("GetChannels", { channels: List<ChannelDto> ->
            _channels.value = channels
            onChannelListReceived.invoke(channels)
        }, channelType)

        hubConnection.on("GetChannel", { channel: ChannelDto ->
            _channels.value += channel
            onChannelReceived.invoke(channel)
        }, ChannelDto::class.java)

        hubConnection.on("GetContacts", { contacts: List<UserDto> ->
            _contacts.value = contacts.map { it.id }
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
            _contacts.value += user.id
            onContactAdded.invoke(user)
        }, UserDto::class.java)
        hubConnection.on("UserAddedToChannel", { channel: ChannelDto, user: UserDto, addedBy: UserDto ->
            _channels.value = _channels.value.map {
                if (it.id == channel.id) it.copy(members = it.members + user.id) else it
            }
            onUserAddedToChannel.invoke(Triple(channel, user, addedBy))
        }, ChannelDto::class.java, UserDto::class.java, UserDto::class.java)

        hubConnection.start().blockingAwait()
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
    fun addContact(userId: String) {
        hubConnection.send("AddContact", userId)
    }
    fun getChannelMessages(channelId: Long, beforeMessageId: Long? = null, limit: Int = 50) {
        hubConnection.send("GetChannelMessages", channelId, beforeMessageId, limit)
    }
    fun getChannelUsers(channelId: Long) {
        hubConnection.send("GetChannelMembers", channelId)
    }
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