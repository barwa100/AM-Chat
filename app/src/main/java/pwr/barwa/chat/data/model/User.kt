package pwr.barwa.chat.data.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")

data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "display_name") val displayName: String,
    @ColumnInfo(name = "username") val username: String,
    @ColumnInfo(name = "password") val password: String,
    val loggedIn: Boolean? = null
) {
//    constructor(displayName: String, username: String, password: String) : this(
//        id = 0,
//        displayName = displayName,
//        username = username,
//        password = password,
//        loggedIn = null
//    )
//
//    constructor(id: Long, displayName: String, username: String, password: String) : this(
//        id = id,
//        displayName = displayName,
//        username = username,
//        password = password,
//        loggedIn = null
//    )
}