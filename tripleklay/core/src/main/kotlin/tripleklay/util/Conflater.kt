package tripleklay.util

/**
 * Code shared by [Inflater] and `Deflater`.
 */
abstract class Conflater {
    companion object {

        internal fun toHexString(value: Int, ncount: Int): String {
            var value = value
            val data = CharArray(ncount)
            for (ii in ncount - 1 downTo 0) {
                data[ii] = HEX_CHARS[value and 0xF]
                value = value ushr 4
            }
            return String(data)
        }

        internal fun fromHexString(buf: String, offset: Int, ncount: Int): Int {
            var value = 0
            var ii = offset
            val ll = offset + ncount
            while (ii < ll) {
                value = value shl 4
                val c = buf[ii]
                val nibble = if (c >= 'A') 10 + c.toInt() - 'A'.toInt() else c - '0'
                value = value or nibble
                ii++
            }
            if (ncount == 2 && value > java.lang.Byte.MAX_VALUE)
                value -= 256
            else if (ncount == 4 && value > java.lang.Short.MAX_VALUE) value -= 65536
            return value
        }

        protected val HEX_CHARS = "0123456789ABCDEF"

        // used for variable length int encoding (by Inflater/Deflater)
        internal val VARABS = "\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNO" // ! intentionally omitted
        internal val VARCONT = "PQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}" // ~ intentionally omitted
        internal val BASE = VARABS.length
        internal val ABS0 = VARABS[0]
        internal val CONT0 = VARCONT[0]
        internal val NEG_MARKER = '!'
    }
}
