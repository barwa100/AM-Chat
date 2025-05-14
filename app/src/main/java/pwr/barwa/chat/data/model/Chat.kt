package pwr.barwa.chat.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "chats"
)
data class Chat(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long = 0,
    val name: String,
    val lastMessage: String,
    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(), // Store as timestamp
    val isGroup: Boolean = false
)