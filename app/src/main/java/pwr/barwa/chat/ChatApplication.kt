package pwr.barwa.chat

import android.app.Application
import pwr.barwa.chat.data.AppContainer

class ChatApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}