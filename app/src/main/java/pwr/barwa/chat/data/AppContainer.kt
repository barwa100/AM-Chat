package pwr.barwa.chat.data

import android.content.Context

class AppContainer(private val context: Context) {
    val database: AppDatabase by lazy {
        AppDatabase.getInstance(context)
    }
    val signalRConnector: SignalRConnector by lazy {
        SignalRConnector.getInstance(context)
    }
}