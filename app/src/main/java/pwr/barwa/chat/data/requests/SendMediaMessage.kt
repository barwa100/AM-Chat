package pwr.barwa.chat.data.requests

import android.util.Base64
import pwr.barwa.chat.data.dto.MessageType

class SendMediaMessage(
    val channelId: Long,
    val dataBase64: String, // Teraz przesyłamy jako łańcuch Base64
    val extension: String,
    val messageType: Int
) {
    companion object {
        /**
         * Konwertuje dane binarne (ByteArray) do obiektu SendMediaMessage z danymi zakodowanymi jako Base64
         */
        fun fromByteArray(channelId: Long, byteArray: ByteArray, extension: String, messageType: MessageType): SendMediaMessage {
            val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            return SendMediaMessage(channelId, base64String, extension, messageType.value)
        }
    }
}
