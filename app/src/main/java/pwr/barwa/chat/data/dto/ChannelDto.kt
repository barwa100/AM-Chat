package pwr.barwa.chat.data.dto

data class ChannelDto(
    val id: Long,
    val name: String,
    val members: List<Long>,
    val messages: List<Long>
)