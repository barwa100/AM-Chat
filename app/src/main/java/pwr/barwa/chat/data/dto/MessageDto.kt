package pwr.barwa.chat.data.dto

data class MessageDto(
    val id: Long,
    val senderId: Long,
    val channelId: Long,
    val data: String,
    val type: MessageType,
    val created: Long,
    val updated: Long? = null
)

enum class MessageType {
    TEXT, IMAGE, VIDEO, AUDIO
}