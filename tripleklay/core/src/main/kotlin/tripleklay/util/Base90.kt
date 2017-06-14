package tripleklay.util

/**
 * Encodes and decodes ints and longs to strings in base 90.
 */
object Base90 {
    fun encodeInt(value: Int): String {
        var value = value
        val buf = StringBuilder()
        if (value < 0) {
            buf.append(NEG_MARKER)
            value -= Integer.MIN_VALUE
        }
        do {
            buf.append(CHARS[value % BASE])
            value /= BASE
        } while (value > 0)
        return buf.toString()
    }

    fun decodeInt(data: String): Int {
        var data = data
        var neg = false
        if (data.length > 0 && data[0] == NEG_MARKER) {
            neg = true
            data = data.substring(1)
        }
        var value = 0
        for (ii in data.length - 1 downTo 0) {
            value *= BASE
            value += data[ii] - FIRST
        }
        if (neg) value += Integer.MIN_VALUE
        return value
    }

    fun encodeLong(value: Long): String {
        var value = value
        val buf = StringBuilder()
        if (value < 0) {
            buf.append(NEG_MARKER)
            value -= java.lang.Long.MIN_VALUE
        }
        while (value > 0) {
            buf.append(CHARS[(value % BASE).toInt()])
            value /= BASE.toLong()
        }
        return buf.toString()
    }

    fun decodeLong(data: String): Long {
        var data = data
        var neg = false
        if (data.length > 0 && data[0] == NEG_MARKER) {
            neg = true
            data = data.substring(1)
        }
        var value: Long = 0
        for (ii in data.length - 1 downTo 0) {
            value *= BASE.toLong()
            value += (data[ii] - FIRST).toLong()
        }
        if (neg) value += java.lang.Long.MIN_VALUE
        return value
    }

    /** Used to encode ints and longs.  */
    private val CHARS = "\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNO" + // ! intentionally omitted
            "PQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}"  // ~ intentionally omitted

    /** Used when encoding and decoding.  */
    private val BASE = CHARS.length

    /** Used when decoding.  */
    private val FIRST = CHARS[0]

    /** A character used to mark negative values.  */
    private val NEG_MARKER = '!'
}
