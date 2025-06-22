package pwr.barwa.chat.data.dto

data class MessageDto(
    val id: Long,
    val senderId: Long,
    val channelId: Long,
    val data: String,
    private val messageType: Int?, // Zmieniamy typ z MessageType na Int z możliwością null
    val created: Long,
    val updated: Long? = null
) {
    // Dodajemy właściwość, która konwertuje Int na MessageType
    val type: MessageType
        get() = when(messageType) {
            null -> MessageType.TEXT // Domyślna wartość gdy null
            1 -> MessageType.TEXT
            2 -> MessageType.IMAGE
            3 -> MessageType.VIDEO
            4 -> MessageType.AUDIO
            else -> MessageType.TEXT // Domyślna dla nieznanych wartości
        }
}

enum class MessageType(val value: Int) {
    TEXT(1),
    IMAGE(2),
    VIDEO(3),
    AUDIO(4)
}

