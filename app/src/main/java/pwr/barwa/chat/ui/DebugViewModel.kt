package pwr.barwa.chat.ui

    import androidx.compose.runtime.State
    import androidx.compose.runtime.mutableStateOf
    import androidx.lifecycle.ViewModel
    import androidx.lifecycle.viewModelScope
    import kotlinx.coroutines.launch
    import pwr.barwa.chat.data.SignalRConnector

    class DebugViewModel(private val signalRConnector: SignalRConnector) : ViewModel() {

        private val _messages = mutableStateOf<List<String>>(emptyList())
        val messages: State<List<String>> = _messages

        init {
            viewModelScope.launch {
                signalRConnector.messages.collect { newMessages ->
                    _messages.value = newMessages
                }
            }
        }

        suspend fun connectToChat() {
            signalRConnector.startConnection()
        }

        fun disconnectFromChat() {
            signalRConnector.stopConnection()
        }

        fun sendMessage(message: String) {
            signalRConnector.sendMessage(message)
        }
}