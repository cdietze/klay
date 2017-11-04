package klay.core.json

import org.junit.Test
import kotlin.test.assertEquals

/**
 * Attempts to test that numbers are correctly round-tripped.
 */
class InternalJsonNumberTest {
    @Test
    fun testBasicNumberRead() {
        val array = JsonParser.array().from("[1, 1.0, 1.00]")!!
        assertEquals(Int::class, array[0]!!::class)
        assertEquals(Double::class, array[1]!!::class)
        assertEquals(Double::class, array[2]!!::class)
    }

    @Test
    fun testBasicNumberWrite() {
        val array = JsonArray.from(1, 1.0, 1.0f)
        assertEquals("[1,1.0,1.0]", JsonStringWriter().array(array).write())
    }

    @Test
    fun testLargeIntRead() {
        val array = JsonParser.array().from("[-300000000,300000000]")!!
        assertEquals(Int::class, array[0]!!::class)
        assertEquals(-300000000, array[0])
        assertEquals(Int::class, array[1]!!::class)
        assertEquals(300000000, array[1])
    }

    @Test
    fun testLargeIntWrite() {
        val array = JsonArray.from(-300000000, 300000000)
        assertEquals("[-300000000,300000000]", JsonStringWriter().array(array).write())
    }

    @Test
    fun testLongRead() {
        val array = JsonParser.array().from("[-3000000000,3000000000]")!!
        assertEquals(Long::class, array[0]!!::class)
        assertEquals(-3000000000L, array[0])
        assertEquals(Long::class, array[1]!!::class)
        assertEquals(3000000000L, array[1])
    }

    @Test
    fun testLongWrite() {
        val array = JsonArray.from(1L, -3000000000L, 3000000000L)
        assertEquals("[1,-3000000000,3000000000]", JsonStringWriter().array(array).write())
    }

    /**
     * Test around the edges of the integral types
     */
    @Test
    fun testAroundEdges() {
        val array = JsonArray.from(
                Int.MAX_VALUE, Int.MAX_VALUE.toLong() + 1,
                Int.MIN_VALUE, Int.MIN_VALUE.toLong() - 1,
                Long.MAX_VALUE, Long.MIN_VALUE)
        val json = JsonStringWriter().array(array).write()
        assertEquals("[2147483647,2147483648,-2147483648,-2147483649,9223372036854775807,-9223372036854775808]", json)
        val array2 = JsonParser.array().from(json)!!
        val json2 = JsonStringWriter().array(array2).write()
        assertEquals(json, json2)
    }
}
