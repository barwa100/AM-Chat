package pwr.barwa.chat.data

class Event<T> {
    private val listeners = mutableMapOf<String, (T) -> Unit>()

    fun addListener(id: String, listener: (T) -> Unit) {
        listeners[id] = listener
    }

    fun removeListener(id: String) {
        listeners.remove(id)
    }

    fun invoke(data: T) {
        listeners.values.forEach { it(data) }
    }
}