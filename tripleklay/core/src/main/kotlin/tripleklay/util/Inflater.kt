package tripleklay.util

/**
 * Inflates data encoded by [Deflater].
 */
class Inflater(protected val _data: String) : Conflater() {

    fun popBool(): Boolean {
        return popChar() == 't'
    }

    fun popChar(): Char {
        return _data.get(_pos++)
    }

    fun popNibble(): Int {
        return Conflater.fromHexString(_data, pos(1), 1)
    }

    fun popByte(): Int {
        return Conflater.fromHexString(_data, pos(2), 2)
    }

    fun popShort(): Int {
        return Conflater.fromHexString(_data, pos(4), 4)
    }

    fun popInt(): Int {
        return Conflater.fromHexString(_data, pos(8), 8)
    }

    fun popVarInt(): Int {
        var value = 0
        var c: Char
        val neg = _data.get(_pos) == Conflater.NEG_MARKER
        if (neg) _pos++
        do {
            value *= Conflater.BASE
            c = _data.get(_pos++)
            value += if (c >= Conflater.CONT0) c - Conflater.CONT0 else c - Conflater.ABS0
        } while (c >= Conflater.CONT0)
        return if (neg) -1 * value else value
    }

    fun popVarLong(): Long {
        var value: Long = 0
        var c: Char
        val neg = _data.get(_pos) == Conflater.NEG_MARKER
        if (neg) _pos++
        do {
            value *= Conflater.BASE.toLong()
            c = _data.get(_pos++)
            value += (if (c >= Conflater.CONT0) c - Conflater.CONT0 else c - Conflater.ABS0).toLong()
        } while (c >= Conflater.CONT0)
        return if (neg) -1 * value else value
    }

    fun popBitVec(): BitVec {
        val words = popVarInt()
        val vec = BitVec(words)
        for (ii in 0..words - 1) vec._words[ii] = popInt()
        return vec
    }

    fun popFLString(length: Int): String {
        return _data.substring(pos(length), _pos)
    }

    fun popString(): String {
        return _data.substring(pos(popShort()), _pos)
    }

    inline fun <reified E : Enum<E>> popEnum(eclass: Class<E>): E {
        return enumValues<E>().find { it.name == popString() }!!
    }

    fun eos(): Boolean {
        return _pos >= _data.length
    }

    protected fun pos(incr: Int): Int {
        val pos = _pos
        _pos += incr
        return pos
    }

    protected var _pos: Int = 0
}
