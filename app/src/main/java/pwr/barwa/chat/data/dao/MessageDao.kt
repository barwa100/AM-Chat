package pwr.barwa.chat.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import pwr.barwa.chat.data.model.Message

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message): Long

    @Update
    suspend fun updateMessage(message: Message)

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesForChat(chatId: Long): Flow<List<Message>>

    @Query("SELECT * FROM messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: Long): Message?

    @Query("UPDATE messages SET localMediaPath = :localPath WHERE id = :messageId")
    suspend fun updateLocalMediaPath(messageId: Long, localPath: String)

    @Query("SELECT * FROM messages WHERE mediaUrl IS NOT NULL AND localMediaPath IS NULL")
    suspend fun getMessagesWithPendingMediaDownload(): List<Message>

    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun deleteMessagesByChatId(chatId: Long)
}
