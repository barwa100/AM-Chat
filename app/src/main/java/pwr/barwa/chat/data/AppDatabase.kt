package pwr.barwa.chat.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import pwr.barwa.chat.data.model.Chat
import pwr.barwa.chat.data.model.Message
import pwr.barwa.chat.data.model.User
import pwr.barwa.chat.data.dao.ChatDao
import pwr.barwa.chat.data.dao.MessageDao
import pwr.barwa.chat.data.dao.UserDao
import pwr.barwa.chat.data.converters.MediaTypeConverter

@Database(
    entities = [Message::class, User::class, Chat::class],
    version = 8,
    exportSchema = false)
@TypeConverters(MediaTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao

    companion object {

        @Volatile
        private var Instance : AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "chat_database"
                ).fallbackToDestructiveMigration(true).build().also {
                    Instance = it
                }
            }
        }

    }
}

