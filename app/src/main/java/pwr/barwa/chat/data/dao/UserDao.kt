package pwr.barwa.chat.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import pwr.barwa.chat.data.model.User

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAll(): List<User>

    @Query("SELECT * FROM user WHERE username LIKE :username LIMIT 1")
    fun findByUsername(username: String): User?

    @Query("SELECT * FROM user WHERE password = :password and username = :username LIMIT 1")
    fun login(username: String, password: String): User?

    @Query("UPDATE user SET loggedIn = 1 where username = :username")
    fun setLoggedIn(username: String)

    @Query("UPDATE user set loggedIn=0")
    fun logout()

    @Query("SELECT * from user where loggedIn=1 limit 1")
    fun getLoggedInUser(): User?

    @Insert
    fun insertAll(vararg users: User) // Zwraca Unit()

    @Insert
    suspend fun insertUser(user: User): Long  // Zwraca ID nowego użytkownika

    @Delete
    suspend fun delete(user: User)
}