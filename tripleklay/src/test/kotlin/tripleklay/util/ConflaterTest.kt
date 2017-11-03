package tripleklay.util

import org.junit.Test
import kotlin.test.assertEquals

class ConflaterTest {
    @Test
    fun testInflaterDeflater() {
        val d = Deflater()
        for (ii in 0..15) d.addNibble(ii)
        d.addBool(true)
        for (ii in Byte.MIN_VALUE..Byte.MAX_VALUE) d.addByte(ii)
        d.addFLString("FIXED")
        run {
            var ii = Short.MIN_VALUE.toInt()
            while (ii <= Short.MAX_VALUE) {
                d.addShort(ii)
                ii += 128
            }
        }
        d.addString("four")
        run {
            var ii = Int.MIN_VALUE
            while (ii <= 0) {
                d.addInt(ii)
                if (ii > Int.MIN_VALUE) d.addVarInt(ii)
                ii += 65536
            }
        }
        run {
            var ii = Int.MAX_VALUE
            while (ii >= 0) {
                d.addInt(ii).addVarInt(ii)
                ii -= 65536
            }
        }
        run {
            var ii = Long.MIN_VALUE + LONG_STRIDE
            while (ii <= 0) {
                d.addVarLong(ii)
                ii += LONG_STRIDE
            }
        }
        run {
            var ii = Long.MAX_VALUE
            while (ii >= 0) {
                d.addVarLong(ii)
                ii -= LONG_STRIDE
            }
        }
        d.addBool(false)

        val i = Inflater(d.encoded())
        for (ii in 0..15) assertEquals(ii.toLong(), i.popNibble().toLong())
        assertEquals(true, i.popBool())
        for (ii in Byte.MIN_VALUE..Byte.MAX_VALUE) assertEquals(ii.toLong(), i.popByte().toLong())
        assertEquals("FIXED", i.popFLString("FIXED".length))
        run {
            var ii = Short.MIN_VALUE.toInt()
            while (ii <= Short.MAX_VALUE) {
                assertEquals(ii.toLong(), i.popShort().toLong())
                ii += 128
            }
        }
        assertEquals("four", i.popString())
        run {
            var ii = Int.MIN_VALUE
            while (ii <= 0) {
                assertEquals(ii.toLong(), i.popInt().toLong())
                if (ii > Int.MIN_VALUE) assertEquals(ii.toLong(), i.popVarInt().toLong())
                ii += 65536
            }
        }
        run {
            var ii = Int.MAX_VALUE
            while (ii >= 0) {
                assertEquals(ii.toLong(), i.popInt().toLong())
                assertEquals(ii.toLong(), i.popVarInt().toLong())
                ii -= 65536
            }
        }
        run {
            var ii = Long.MIN_VALUE + LONG_STRIDE
            while (ii <= 0) {
                assertEquals(ii, i.popVarLong())
                ii += LONG_STRIDE
            }
        }
        var ii = Long.MAX_VALUE
        while (ii >= 0) {
            assertEquals(ii, i.popVarLong())
            ii -= LONG_STRIDE
        }
        assertEquals(false, i.popBool())
    }

    companion object {

        private val LONG_STRIDE = Long.MAX_VALUE shr 16
    }
}
