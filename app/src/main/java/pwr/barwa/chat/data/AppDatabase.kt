package pwr.barwa.chat.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [pwr.barwa.chat.data.model.User::class, pwr.barwa.chat.data.model.Chat::class],
    version = 6,
    exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): pwr.barwa.chat.data.dao.UserDao
    abstract fun chatDao(): pwr.barwa.chat.data.dao.ChatDao

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

