package pwr.barwa.chat.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import pwr.barwa.chat.data.model.Chat
import pwr.barwa.chat.data.model.User

@Dao
interface ChatDao {
    @Insert
    suspend fun insert(chat: Chat): Long

    @Query("SELECT * FROM chats ORDER BY timestamp DESC")
    suspend fun getAllChats(): List<Chat>

    @Query("SELECT * FROM chats WHERE id = :chatId")
    suspend fun getChatById(chatId: Long): Chat?

    @Query("DELETE FROM chats WHERE id = :chatId")
    suspend fun deleteChat(chatId: Long)
}