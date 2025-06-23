package pwr.barwa.chat.data

import android.util.Log
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
import java.util.UUID

class SignalRConnector private constructor(private val token: String, private val instanceId: String = UUID.randomUUID().toString()) {

    // Bezpośrednio inicjalizujemy połączenie przy tworzeniu instancji
    private val hubConnection: HubConnection = createHubConnection()

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
    val onCurrentUserReceived = Event<UserDto>()
    private val scope = CoroutineScope(Dispatchers.IO)

    private fun createHubConnection(): HubConnection {
        println("Tworzę nowe połączenie SignalR dla instancji $instanceId z tokenem $token")
        return HubConnectionBuilder
            .create(AuthService.URL_BASE + "ws")
            .withAccessTokenProvider(Single.just(token))
            .build()
    }


    suspend fun startConnection() {
        println("Rozpoczynam połączenie dla instancji $instanceId")

        // Czyszczenie danych
        _messages.value = emptyList()
        _channels.value = emptyList()
        _contacts.value = emptyList()
        __users.value = emptyList()
        _currentUser.value = null

        // Rejestracja zdarzeń
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

        hubConnection.on("UserJoined", { channel: ChannelDto, user: UserDto, addedBy: UserDto ->
            _channels.value = _channels.value.map {
                if (it.id == channel.id) it.copy(members = it.members + user.id) else it
            }
            onUserAddedToChannel.invoke(Triple(channel, user, addedBy))
        }, ChannelDto::class.java, UserDto::class.java, UserDto::class.java)

        hubConnection.on("ChannelDeleted", { deletedChannelId: Long ->
            _channels.value = _channels.value.filter { it.id != deletedChannelId }
            onChannelDeleted.invoke(deletedChannelId)
        }, Long::class.java)

        hubConnection.on("GetCurrentUser", { user: UserDto ->
            Log.d("SignalRConnector", "Aktualny użytkownik odebrany: ${user.userName}, ID: ${user.id}")
            _currentUser.value = user
            onCurrentUserReceived.invoke(user)
        }, UserDto::class.java)

        hubConnection.onClosed { error ->
            println("Połączenie zostało utracone dla instancji $instanceId: ${error?.message}")
            scope.launch {
                reconnect()
            }
        }
        reconnect()
        requestCurrentUser()
    }

    private suspend fun reconnect() {
        var retryCount = 0
        val maxRetries = 5
        val retryDelay = 2000L // 2 sekundy

        while (hubConnection.connectionState != HubConnectionState.CONNECTED && retryCount < maxRetries) {
            try {
                println("Próba ponownego połączenia dla instancji $instanceId... ($retryCount)")
                hubConnection.start().blockingAwait()
                println("Połączono ponownie!")
            } catch (e: Exception) {
                println("Nie udało się połączyć: ${e.message}")
                retryCount++
                kotlinx.coroutines.delay(retryDelay)
            }
        }

        if (hubConnection.connectionState != HubConnectionState.CONNECTED) {
            println("Nie udało się połączyć po $maxRetries próbach dla instancji $instanceId.")
        }
    }

    fun stopConnection() {
        try {
            if (hubConnection.connectionState == HubConnectionState.CONNECTED) {
                hubConnection.stop()
                println("Połączenie zostało zatrzymane dla instancji $instanceId")
            }
        } catch (e: Exception) {
            println("Błąd podczas zatrzymywania połączenia dla instancji $instanceId: ${e.message}")
        }
    }


    fun logout() {
        try {
            stopConnection()
            println("Wylogowano użytkownika z instancji $instanceId")

            _messages.value = emptyList()
            _channels.value = emptyList()
            _contacts.value = emptyList()
            __users.value = emptyList()
            _currentUser.value = null

            // Usuń referencję do tej instancji
            synchronized(companions) {
                currentInstance.compareAndSet(this, null)
                println("Usunięto referencję do instancji $instanceId z currentInstance")
            }
        } catch (e: Exception) {
            println("Błąd podczas procesu wylogowania dla instancji $instanceId: ${e.message}")
        }
    }

    // Metody do komunikacji z serwerem
    fun sendMessage(message: SendTextMessage) {
        if (hubConnection.connectionState == HubConnectionState.CONNECTED) {
            hubConnection.send("SendMessage", message)
        } else {
            println("Brak połączenia dla instancji $instanceId - próbuję ponownie połączyć")
            scope.launch {
                reconnect()
                if (hubConnection.connectionState == HubConnectionState.CONNECTED) {
                    hubConnection.send("SendMessage", message)
                    println("Ponowne połączenie nawiązane, wysłano wiadomość")
                } else {
                    println("Nie można wysłać wiadomości - wciąż brak połączenia dla instancji $instanceId")
                }
            }
        }
    }

    fun sendMediaMessage(message: SendMediaMessage) {
        if (hubConnection.connectionState == HubConnectionState.CONNECTED) {
            hubConnection.send("SendMediaMessage", message)
        } else {
            println("Brak połączenia dla instancji $instanceId - próbuję ponownie połączyć")
            scope.launch {
                reconnect()
                if (hubConnection.connectionState == HubConnectionState.CONNECTED) {
                    hubConnection.send("SendMediaMessage", message)
                    println("Ponowne połączenie nawiązane, wysłano wiadomość medialną")
                } else {
                    println("Nie można wysłać wiadomości medialnej - wciąż brak połączenia dla instancji $instanceId")
                }
            }
        }
    }

    fun requestChannelList() {
        if (hubConnection.connectionState == HubConnectionState.CONNECTED) {
            hubConnection.send("GetChannels")
        } else {
            println("Nie można pobrać listy kanałów - brak połączenia dla instancji $instanceId")
        }
    }

    fun createChannel(createChannelRequest: CreateChannelRequest) {
        if (hubConnection.connectionState == HubConnectionState.CONNECTED) {
            hubConnection.send("CreateChannel", createChannelRequest)
        } else {
            println("Nie można utworzyć kanału - brak połączenia dla instancji $instanceId")
        }
    }

    fun addUserToChannel(channelId: Long, userId: Long) {
        if (hubConnection.connectionState == HubConnectionState.CONNECTED) {
            hubConnection.send("AddToChannel", channelId, userId)
        } else {
            println("Nie można dodać użytkownika do kanału - brak połączenia dla instancji $instanceId")
        }
    }

    fun getContactList() {
        if (hubConnection.connectionState == HubConnectionState.CONNECTED) {
            hubConnection.send("GetContacts")
        } else {
            println("Nie można pobrać listy kontaktów - brak połączenia dla instancji $instanceId")
        }
    }

    fun addContact(userName: String) {
        if (hubConnection.connectionState == HubConnectionState.CONNECTED) {
            hubConnection.send("AddContact", userName)
        } else {
            println("Nie można dodać kontaktu - brak połączenia dla instancji $instanceId")
        }
    }

    fun getChannelMessages(channelId: Long, beforeMessageId: Long? = null, limit: Int = 50) {
        if (hubConnection.connectionState == HubConnectionState.CONNECTED) {
            hubConnection.send("GetChannelMessages", channelId, beforeMessageId, limit)
        } else {
            println("Nie można pobrać wiadomości kanału - brak połączenia dla instancji $instanceId")
        }
    }

    fun getChannelUsers(channelId: Long) {
        if (hubConnection.connectionState == HubConnectionState.CONNECTED) {
            hubConnection.send("GetChannelMembers", channelId)
        } else {
            println("Nie można pobrać użytkowników kanału - brak połączenia dla instancji $instanceId")
        }
    }

    fun getChannel(channelId: Long) {
        if (hubConnection.connectionState == HubConnectionState.CONNECTED) {
            hubConnection.send("GetChannel", channelId)
        } else {
            println("Nie można pobrać kanału - brak połączenia dla instancji $instanceId")
        }
    }

    fun deleteChannel(channelId: Long) {
        if (hubConnection.connectionState == HubConnectionState.CONNECTED) {
            hubConnection.send("DeleteChannel", channelId)
        } else {
            println("Nie można usunąć kanału - brak połączenia dla instancji $instanceId")
        }
    }

    fun requestCurrentUser() {
        if (hubConnection.connectionState == HubConnectionState.CONNECTED) {
            hubConnection.send("GetCurrentUser")
        } else {
            println("Nie można pobrać aktualnego użytkownika - brak połączenia dla instancji $instanceId")
        }
    }

    fun setCurrentUser(user: UserDto) {
        _currentUser.value = user
    }

    fun getCurrentUser(): UserDto? = _currentUser.value

    companion object {
        // Przechowywanie aktualnej instancji jako AtomicReference dla bezpiecznego dostępu wielowątkowego
        private val currentInstance = java.util.concurrent.atomic.AtomicReference<SignalRConnector?>(null)
        private val companions = Any() // Obiekt do synchronizacji


        fun getInstance(token: String? = null): SignalRConnector {
            synchronized(companions) {
                if (token != null) {
                    val oldInstance = currentInstance.get()
                    oldInstance?.stopConnection()

                    val newInstance = SignalRConnector(token)
                    println("Utworzono nową instancję SignalRConnector (ID: ${newInstance.instanceId})")

                    currentInstance.set(newInstance)
                    return newInstance
                }

                currentInstance.get()?.let { return it }

                throw IllegalStateException("SignalRConnector nie został zainicjalizowany. Najpierw zaloguj się.")
            }
        }

        fun removeInstance() {
            synchronized(companions) {
                val instance = currentInstance.getAndSet(null)
                instance?.stopConnection()
                println("Usunięto instancję SignalRConnector")
            }
        }

        fun hasActiveConnection(): Boolean {
            return currentInstance.get() != null
        }
    }
}
