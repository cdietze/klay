package tripleklay.util

import klay.core.assert

/**
 * Encodes typed data into a string. This is the deflating counterpart to [Inflater].
 */
class Deflater : Conflater() {
    fun addBool(value: Boolean): Deflater {
        addChar(if (value) 't' else 'f')
        return this
    }

    fun addChar(c: Char): Deflater {
        _buf.append(c)
        return this
    }

    fun addNibble(value: Int): Deflater {
        check(value, 0, 0xF, "Nibble")
        _buf.append(Conflater.toHexString(value, 1))
        return this
    }

    fun addByte(value: Int): Deflater {
        check(value, Byte.MIN_VALUE.toInt(), Byte.MAX_VALUE.toInt(), "Byte")
        _buf.append(Conflater.toHexString(value, 2))
        return this
    }

    fun addShort(value: Int): Deflater {
        check(value, Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt(), "Short")
        _buf.append(Conflater.toHexString(value, 4))
        return this
    }

    fun addInt(value: Int): Deflater {
        _buf.append(Conflater.toHexString(value, 8))
        return this
    }

    fun addVarInt(value: Int): Deflater {
        var value = value
        assert(value > Int.MIN_VALUE) { "Can't use varint for Int.MIN_VALUE" }
        if (value < 0) {
            _buf.append(Conflater.NEG_MARKER)
            value *= -1
        }
        addVarInt(value, false)
        return this
    }

    fun addVarLong(value: Long): Deflater {
        var value = value
        assert(value > Long.MIN_VALUE) { "Can't use varlong for Long.MIN_VALUE" }
        if (value < 0) {
            _buf.append(Conflater.NEG_MARKER)
            value *= -1
        }
        addVarLong(value, false)
        return this
    }

    fun addBitVec(value: BitVec): Deflater {
        // find the index of the highest non-zero word
        val words = value._words
        var wc = 0
        for (ii in words.indices.reversed()) {
            if (words[ii] == 0) continue
            wc = ii + 1
            break
        }
        // now write out the number of words and their contents
        addVarInt(wc)
        for (ii in 0..wc - 1) addInt(words[ii])
        return this
    }

    fun addFLString(value: String): Deflater {
        _buf.append(value)
        return this
    }

    fun addString(value: String): Deflater {
        addShort(value.length)
        _buf.append(value)
        return this
    }

    fun <E : Enum<E>> addEnum(value: E): Deflater {
        return addString(value.name)
    }

    fun encoded(): String {
        return _buf.toString()
    }

    fun reset(): Deflater {
        _buf = StringBuilder()
        return this
    }

    protected fun addVarInt(value: Int, cont: Boolean) {
        if (value >= Conflater.BASE) addVarInt(value / Conflater.BASE, true)
        _buf.append((if (cont) Conflater.VARCONT else Conflater.VARABS)[value % Conflater.BASE])
    }

    protected fun addVarLong(value: Long, cont: Boolean) {
        if (value >= Conflater.BASE) addVarLong(value / Conflater.BASE, true)
        _buf.append((if (cont) Conflater.VARCONT else Conflater.VARABS)[(value % Conflater.BASE).toInt()])
    }

    protected fun check(value: Int, min: Int, max: Int, type: String) {
        assert(value >= min && value <= max) { "$type must be $min <= n <= $max" }
    }

    protected var _buf = StringBuilder()
}
