package pwr.barwa.chat.data

import android.content.Context
import androidx.room.Room
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SignalRConnector {

    private val hubConnection: HubConnection by lazy {
        HubConnectionBuilder
            .create("http://10.0.2.2:5093/ws")
            .build()
    }

    private val _messages = MutableStateFlow<List<String>>(emptyList())
    val messages: StateFlow<List<String>> = _messages


    suspend fun startConnection() {

        hubConnection.on("ReceiveMessage", { message ->
            _messages.value += message
        }, String::class.java)

        hubConnection.start().blockingAwait()
    }
    fun stopConnection() {
        hubConnection.stop()
    }
    fun sendMessage(message: String) {
        hubConnection.send("SendMessage", message)
    }
    companion object {

        @Volatile
        private var Instance : SignalRConnector? = null
        fun getInstance(context: Context): SignalRConnector {
            return Instance ?: synchronized(this) {
                SignalRConnector()
            }
        }

    }
}