package pwr.barwa.chat.data.requests

import pwr.barwa.chat.data.dto.MessageType

class SendMediaMessage(
    val channelId: Long,
    val data: ByteArray,
    val extension: String,
    val messageType: MessageType
)