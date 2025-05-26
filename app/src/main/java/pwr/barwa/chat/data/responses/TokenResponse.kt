package pwr.barwa.chat.data.responses

data class TokenResponse(
    val tokenType: String?,
    val accessToken: String,
    val expiresIn: Long,
    val refreshToken: String
)