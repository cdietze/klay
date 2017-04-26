package klay.core.react

import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test

class ReactTest {
    @Test
    fun shouldBeNotified() {
        val signal = Signal.create<Int>()
        val list = mutableListOf<Int>()
        signal.connect { event -> list.add(event) }
        signal.emit(1)
        signal.emit(2)
        signal.emit(3)
        assertThat(list, equalTo(listOf(1, 2, 3)))
    }

    @Test
    fun shouldNotBeNotifiedAfterDisconnect() {
        val signal = Signal.create<Int>()
        val list = mutableListOf<Int>()
        val connection = signal.connect { event -> list.add(event) }
        signal.emit(1)
        signal.emit(2)
        connection.disconnect()
        signal.emit(3)
        assertThat(list, equalTo(listOf(1, 2)))

    }
}
