package pwr.barwa.chat.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import pwr.barwa.chat.data.model.Chat

@Dao
interface ChatDao {
    @Insert
    suspend fun insert(chat: Chat): Long

    @Insert
    suspend fun insertAll(vararg chats: Chat) // Dodaj kilka chatow naraz

    @Query("SELECT * FROM chats ORDER BY timestamp DESC")
    suspend fun getAllChats(): List<Chat>
}