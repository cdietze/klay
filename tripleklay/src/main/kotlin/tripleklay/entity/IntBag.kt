package tripleklay.entity

/** An unordered bag of ints. Used internally by entity things.  */
class IntBag : System.Entities {

    protected var _elems: IntArray
    protected var _size: Int = 0

    init {
        _elems = IntArray(16)
    }

    override fun size(): Int {
        return _size
    }

    fun isEmpty(): Boolean = _size == 0

    override fun get(index: Int): Int {
        return _elems[index]
    }

    operator fun contains(elem: Int): Boolean {
        var ii = 0
        val ll = _size
        while (ii < ll) {
            if (elem == _elems[ii]) return true
            ii++
        }
        return false
    }

    fun add(elem: Int): IntBag {
        if (_size == _elems.size) expand(_elems.size * 3 / 2 + 1)
        _elems[_size++] = elem
        return this
    }

    fun removeAt(index: Int): Int {
        val elem = _elems[index]
        _elems[index] = _elems[--_size]
        return elem
    }

    fun remove(elem: Int): Int {
        var ii = 0
        val ll = _size
        while (ii < ll) {
            val ee = _elems[ii]
            if (ee == elem) {
                _elems[ii] = _elems[--_size]
                return ii
            }
            ii++
        }
        return -1
    }

    fun removeLast(): Int {
        return _elems[--_size]
    }

    fun clear(): IntBag {
        _size = 0
        return this
    }

    override fun toString(): String {
        val buf = StringBuilder("{")
        var ii = 0
        val ll = _size
        while (ii < ll) {
            if (ii > 0) buf.append(",")
            buf.append(_elems[ii])
            ii++
        }
        return buf.append("}").toString()
    }

    private fun expand(capacity: Int) {
        _elems = _elems.copyOf(capacity)
    }
}
