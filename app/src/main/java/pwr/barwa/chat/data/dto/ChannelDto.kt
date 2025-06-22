package pwr.barwa.chat.data.dto

data class ChannelDto(
    val id: Long,
    val name: String,
    val members: List<Long>,
    val messages: List<Long>,
    val lastMessage: MessageDto?,
    val isGroup: Boolean,
    val image: String? = null,
    val created: Long
)