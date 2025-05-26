package pwr.barwa.chat.data.dto

data class UserDto(
    val id: Long,
    val channels: List<Long>,
    val messages: List<Long>,
    val contacts: List<Long>
)