package klay.core.react

/**
 * TODO: handle race conditions (disconnects while emitting, etc.)
 */

interface Connection<T> {
    fun disconnect(): Unit
    val slot: Slot<T>
}

interface Signal<T> {
    fun emit(event: T): Unit
    fun connect(slot: Slot<T>): Connection<T>

    companion object {
        fun <T> create(): Signal<T> = object : Signal<T> {
            private val connections = mutableListOf<Connection<T>>()

            override fun emit(event: T) {
                connections.forEach { it.slot.invoke(event) }
            }

            override fun connect(slot: Slot<T>): Connection<T> {
                val connection = object : Connection<T> {
                    override fun disconnect() {
                        connections.remove(this)
                    }

                    override val slot = slot
                }
                connections.add(connection)
                return connection
            }

        }
    }
}

interface Value<T> : Signal<T> {
    fun get(): T
}

typealias Slot<T> = (T) -> Unit
