package pwr.barwa.chat.data

class Event<T> {
    private val listeners = mutableListOf<(T) -> Unit>()

    fun addListener(listener: (T) -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: (T) -> Unit) {
        listeners.remove(listener)
    }

    fun invoke(data: T) {
        for (listener in listeners) {
            listener(data)
        }
    }
}