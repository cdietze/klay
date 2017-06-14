package tripleklay.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.*

class BagTest {
    @Test fun testAddContainsRemove() {
        val bag = Bag<Int>()
        for (ii in 15..34) {
            bag.add(ii)
        }
        assertEquals((35 - 15).toLong(), bag.size().toLong())
        for (ii in 0..49) {
            assertEquals(ii >= 15 && ii < 35, bag.contains(ii))
        }
        for (ii in 0..49) {
            assertEquals(ii >= 15 && ii < 35, bag.remove(ii))
        }
        assertEquals(0, bag.size().toLong())
        bag.add(3)
        bag.add(5)
        bag.add(9)
        assertEquals(9, bag.removeLast())
        assertEquals(5, bag.removeLast())
        assertEquals(3, bag.removeLast())
        assertEquals(0, bag.size().toLong())
    }

    @Test fun testIterator() {
        val bag = Bag<Int>()
        val values = HashSet<Int>()
        values.add(5)
        values.add(10)
        values.add(25)

        for (elem in values) bag.add(elem)
        assertEquals(values.size.toLong(), bag.size().toLong())
        for (elem in bag) assertTrue(values.remove(elem))
        assertTrue(values.isEmpty())
    }
}
