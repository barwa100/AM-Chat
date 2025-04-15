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

    @Query("SELECT * FROM user WHERE email LIKE :email LIMIT 1")
    fun findByEmail(email: String): User?

    @Query("SELECT * FROM user WHERE password = :password and email = :email LIMIT 1")
    fun login(email: String, password: String): User?

    @Query("UPDATE user SET loggedIn = 1 where email = :email")
    fun setLoggedIn(email: String)

    @Query("UPDATE user set loggedIn=0")
    fun logout()

    @Query("SELECT * from user where loggedIn=1 limit 1")
    fun getLoggedInUser(): User?

    @Insert
    fun insertAll(vararg users: User)

    @Delete
    suspend fun delete(user: User)
}