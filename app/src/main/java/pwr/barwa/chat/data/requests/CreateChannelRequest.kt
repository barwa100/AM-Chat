package pwr.barwa.chat.data.requests

class CreateChannelRequest(
    val name: String,
    val members: List<Long>
)