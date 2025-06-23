package pwr.barwa.chat.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = Chat::class,
            parentColumns = ["id"],
            childColumns = ["chatId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("chatId")]
)
data class Message(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val chatId: Long,
    val senderId: Long,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val mediaType: MediaType? = null,
    val mediaExtension: String? = null,  // Rozszerzenie pliku multimedialnego
    val mediaUrl: String? = null,
    val localMediaPath: String? = null  // Ścieżka do zapisanego lokalnie pliku
)

enum class MediaType {
    IMAGE, VIDEO, AUDIO, FILE
}
