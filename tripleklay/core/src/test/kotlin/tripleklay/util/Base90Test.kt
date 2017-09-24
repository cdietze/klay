package tripleklay.util

import org.junit.Assert.assertEquals
import org.junit.Test

class Base90Test {
    @Test fun testZero() {
        assertEquals(0, Base90.decodeInt(Base90.encodeInt(0)).toLong())
        assertEquals(1, Base90.encodeInt(0).length.toLong())
    }

    @Test fun testInts() {
        assertEquals(Integer.MIN_VALUE.toLong(), Base90.decodeInt(Base90.encodeInt(Integer.MIN_VALUE)).toLong())
        assertEquals(Integer.MAX_VALUE.toLong(), Base90.decodeInt(Base90.encodeInt(Integer.MAX_VALUE)).toLong())
        for (ii in 0 until java.lang.Short.MAX_VALUE) {
            assertEquals(ii.toLong(), Base90.decodeInt(Base90.encodeInt(ii)).toLong())
            assertEquals((-ii).toLong(), Base90.decodeInt(Base90.encodeInt(-ii)).toLong())
        }
        run {
            var ii = 0
            while (ii > 0 && ii <= Integer.MAX_VALUE) {
                assertEquals(ii.toLong(), Base90.decodeInt(Base90.encodeInt(ii)).toLong())
                ii += java.lang.Short.MAX_VALUE.toInt()
            }
        }
        var ii = Integer.MIN_VALUE
        while (ii < 0) {
            assertEquals(ii.toLong(), Base90.decodeInt(Base90.encodeInt(ii)).toLong())
            ii += java.lang.Short.MAX_VALUE.toInt()
        }
    }

    @Test fun testLongs() {
        assertEquals(java.lang.Long.MIN_VALUE, Base90.decodeLong(Base90.encodeLong(java.lang.Long.MIN_VALUE)))
        assertEquals(java.lang.Long.MAX_VALUE, Base90.decodeLong(Base90.encodeLong(java.lang.Long.MAX_VALUE)))
        for (ii in 0L until java.lang.Short.MAX_VALUE.toLong()) {
            assertEquals(ii, Base90.decodeLong(Base90.encodeLong(ii)))
            assertEquals(-ii, Base90.decodeLong(Base90.encodeLong(-ii)))
        }
        val incr = java.lang.Long.MAX_VALUE / java.lang.Short.MAX_VALUE
        run {
            var ii = 0L
            while (ii > 0 && ii <= java.lang.Long.MAX_VALUE) {
                assertEquals(ii, Base90.decodeLong(Base90.encodeLong(ii)))
                ii += incr
            }
        }
        var ii = java.lang.Long.MIN_VALUE
        while (ii < 0) {
            assertEquals(ii, Base90.decodeLong(Base90.encodeLong(ii)))
            ii += incr
        }
    }
}
