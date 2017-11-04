package tripleklay.util

import org.junit.Test
import kotlin.test.assertEquals

class Base90Test {
    @Test
    fun testZero() {
        assertEquals(0, Base90.decodeInt(Base90.encodeInt(0)).toLong())
        assertEquals(1, Base90.encodeInt(0).length.toLong())
    }

    @Test
    fun testInts() {
        assertEquals(Int.MIN_VALUE.toLong(), Base90.decodeInt(Base90.encodeInt(Int.MIN_VALUE)).toLong())
        assertEquals(Int.MAX_VALUE.toLong(), Base90.decodeInt(Base90.encodeInt(Int.MAX_VALUE)).toLong())
        for (ii in 0..Short.MAX_VALUE - 1) {
            assertEquals(ii.toLong(), Base90.decodeInt(Base90.encodeInt(ii)).toLong())
            assertEquals((-ii).toLong(), Base90.decodeInt(Base90.encodeInt(-ii)).toLong())
        }
        run {
            var ii = 0
            while (ii > 0 && ii <= Int.MAX_VALUE) {
                assertEquals(ii.toLong(), Base90.decodeInt(Base90.encodeInt(ii)).toLong())
                ii += Short.MAX_VALUE.toInt()
            }
        }
        var ii = Int.MIN_VALUE
        while (ii < 0) {
            assertEquals(ii.toLong(), Base90.decodeInt(Base90.encodeInt(ii)).toLong())
            ii += Short.MAX_VALUE.toInt()
        }
    }

    @Test
    fun testLongs() {
        assertEquals(Long.MIN_VALUE, Base90.decodeLong(Base90.encodeLong(Long.MIN_VALUE)))
        assertEquals(Long.MAX_VALUE, Base90.decodeLong(Base90.encodeLong(Long.MAX_VALUE)))
        for (ii in 0L..Short.MAX_VALUE - 1) {
            assertEquals(ii, Base90.decodeLong(Base90.encodeLong(ii)))
            assertEquals(-ii, Base90.decodeLong(Base90.encodeLong(-ii)))
        }
        val incr = Long.MAX_VALUE / Short.MAX_VALUE
        run {
            var ii: Long = 0
            while (ii > 0 && ii <= Long.MAX_VALUE) {
                assertEquals(ii, Base90.decodeLong(Base90.encodeLong(ii)))
                ii += incr
            }
        }
        var ii = Long.MIN_VALUE
        while (ii < 0) {
            assertEquals(ii, Base90.decodeLong(Base90.encodeLong(ii)))
            ii += incr
        }
    }
}
