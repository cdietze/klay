package tripleklay.util

import euklid.platform.arrayCopy

/**
 * A bit vector. We'd use Java's `BitSet`, but GWT doesn't support it. Note that we also use
 * `int` instead of `long` to be GWT/JavaScript-friendly. TODO: maybe use super-source
 * to use longs by default but int for GWT?
 */
class BitVec
/** Creates a bit vector with the specified initial capacity (in words).  */
constructor(words: Int = 16) {

    internal var _words = IntArray(words)

    /** Returns whether the `value`th bit it set.  */
    fun isSet(value: Int): Boolean {
        val word = value / 32
        return _words.size > word && _words[word] and (1 shl value % 32) != 0
    }

    /** Sets the `value`th bit.  */
    fun set(value: Int) {
        val word = value / 32
        if (_words.size <= word) {
            val words = IntArray(_words.size * 2)
            arrayCopy(_words, 0, words, 0, _words.size)
            _words = words
        }
        _words[word] = _words[word] or (1 shl value % 32)
    }

    /** Copies the contents of `other` to this bit vector.  */
    fun set(other: BitVec) {
        val owlength = other._words.size
        if (_words.size < owlength) _words = IntArray(owlength)
        arrayCopy(other._words, 0, _words, 0, owlength)
        _words.fill(owlength, _words.size, 0)
    }

    /** Clears the `value`th bit.  */
    fun clear(value: Int) {
        val word = value / 32
        if (_words.size > word) {
            _words[word] = _words[word] and (1 shl value % 32).inv()
        }
    }

    /** Clears all bits in this vector.  */
    fun clear() {
        for (ii in _words.indices) _words[ii] = 0
    }

    override fun toString(): String {
        val buf = StringBuilder("[")
        for (ii in 0.._words.size * 32 - 1) {
            if (!isSet(ii)) continue
            if (buf.length > 1) buf.append(", ")
            buf.append(ii)
        }
        return buf.append("]").toString()
    }
}

/**
 * Assigns the specified `value` to each element
 * in the given range (from `startIndex` (inclusive) to `endIndex` (exclusive)..
 */
private fun IntArray.fill(startIndex: Int, endIndex: Int, value: Int) {
    for (i in startIndex until endIndex) {
        this[i] = value
    }
}
