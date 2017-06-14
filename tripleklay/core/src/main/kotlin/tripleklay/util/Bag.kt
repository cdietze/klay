package tripleklay.util

/**
 * An unordered collection of elements which may contain duplicates. Elements must not be null. The
 * elements will be reordered during normal operation of the bag. This is optimized for fast
 * additions, removals and iteration. It is not optimized for programmer ass coverage; see the
 * warnings below.
 *
 * *Note:* extra bounds checking is *not performed* which means that some invalid
 * operations will succeeed and return null rather than throwing `IndexOutOfBoundsException`.
 * Be careful.
 *
 * *Note:* the iterator returned by [.iterator] does not make concurrent
 * modification checks, so concurrent modifications will cause unspecified behavior. Don't do
 * that.
 */
class Bag<E>
/** Creates a bag with the specified initial capacity.
 * The default initial capacity is 16.*/
@JvmOverloads constructor(initialCapacity: Int = 16) : Iterable<E> {

    private var _elems: Array<Any?> = arrayOfNulls(initialCapacity)
    private var _size: Int = 0

    /** Returns the number of elements in this bag. */
    fun size(): Int {
        return _size
    }

    /** Returns whether this bag is empty.  */
    val isEmpty: Boolean
        get() = _size == 0

    /** Returns the element at `index`.  */
    operator fun get(index: Int): E {
        val elem = _elems[index] as E
        return elem
    }

    /** Returns whether this bag contains `elem`. Equality is by reference.  */
    operator fun contains(elem: E): Boolean {
        val elems = _elems
        var ii = 0
        val ll = _size
        while (ii < ll) {
            if (elem === elems[ii]) return true
            ii++
        }
        return false
    }

    /** Returns whether this bag contains at least one element matching `pred`.  */
    operator fun contains(pred: (E) -> Boolean): Boolean {
        val elems = _elems
        var ii = 0
        val ll = _size
        while (ii < ll) {
            if (pred.invoke(get(ii))) return true
            ii++
        }
        return false
    }

    /** Adds `elem` to this bag. The element will always be added to the end of the bag.
     * @return the index at which the element was added.
     */
    fun add(elem: E): Int {
        if (_size == _elems.size) expand(_elems.size * 3 / 2 + 1)
        _elems[_size++] = elem
        return _size
    }

    /** Removes the element at the specified index.
     * @return the removed element.
     */
    fun removeAt(index: Int): E {
        val elem = _elems[index] as E
        _elems[index] = _elems[--_size]
        _elems[_size] = null
        return elem
    }

    /** Removes the first occurrance of `elem` from the bag. Equality is by reference.
     * @return true if `elem` was found and removed, false if not.
     */
    fun remove(elem: E): Boolean {
        val elems = _elems
        var ii = 0
        val ll = _size
        while (ii < ll) {
            val ee = elems[ii]
            if (ee === elem) {
                elems[ii] = elems[--_size]
                elems[_size] = null
                return true
            }
            ii++
        }
        return false
    }

    /** Removes all elements that match `pred`.
     * @return true if at least one element was found and removed, false otherwise.
     */
    fun removeWhere(pred: (E) -> Boolean): Boolean {
        val elems = _elems
        var removed = 0
        var ii = 0
        val ll = _size
        while (ii < ll) {
            if (pred.invoke(get(ii))) {
                // back ii up so that we recheck the element we're swapping into place here
                elems[ii--] = elems[--_size]
                elems[_size] = null
                removed += 1
            }
            ii++
        }
        return removed > 0
    }

    /** Removes and returns the last element of the bag.
     * @throws ArrayIndexOutOfBoundsException if the bag is empty.
     */
    fun removeLast(): E {
        val elem = _elems[--_size] as E
        _elems[_size] = null
        return elem
    }

    /** Removes all elements from this bag.  */
    fun removeAll() {
        val elems = _elems
        for (ii in 0.._size - 1) elems[ii] = null
        _size = 0
    }

    override fun iterator(): MutableIterator<E> {
        return object : MutableIterator<E> {
            override fun hasNext(): Boolean {
                return _pos < _size
            }

            override fun next(): E {
                return get(_pos++)
            }

            override fun remove() {
                this@Bag.removeAt(--_pos)
            }

            protected var _pos: Int = 0
        }
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
        val elems = arrayOfNulls<Any>(capacity)
        System.arraycopy(_elems, 0, elems, 0, _elems.size)
        _elems = elems
    }
}
