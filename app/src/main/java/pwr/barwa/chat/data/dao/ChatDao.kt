package pwr.barwa.chat.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import pwr.barwa.chat.data.model.Chat

@Dao
interface ChatDao {
    @Insert
    suspend fun insert(chat: Chat): Long

    @Query("SELECT * FROM chats ORDER BY timestamp DESC")
    suspend fun getAllChats(): List<Chat>
}