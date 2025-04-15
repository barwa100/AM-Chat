package pwr.barwa.chat.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [pwr.barwa.chat.data.model.User::class],
    version = 1,
    exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): pwr.barwa.chat.data.dao.UserDao

    companion object {
        const val DATABASE_NAME = "chat_database"
        var instance: AppDatabase? = null

        @Synchronized
        fun getDatabase(context: Context): AppDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    DATABASE_NAME
                ).build()
            }
            return instance!!
        }
    }
}