package pwr.barwa.chat.data.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity

data class User(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "display_name") val displayName: String,
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "password") val password: String,
    val loggedIn: Boolean?
) {
    constructor(displayName: String, email: String, password: String) : this(
        id = 0,
        displayName = displayName,
        email = email,
        password = password,
        loggedIn = null
    )

    constructor(id: Long, displayName: String, email: String, password: String) : this(
        id = id,
        displayName = displayName,
        email = email,
        password = password,
        loggedIn = null
    )
}