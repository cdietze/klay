package tripleklay.util

import react.Closeable

/**
 * Maintains a reference to a resource. Handles destroying the resource before releasing the
 * reference.
 */
abstract class Ref<T> {

    /** Returns the current value of this reference, which may be null.  */
    fun get(): T? {
        return _target
    }

    /** Sets the current value of this reference, clearing any previously referenced object.
     * @return `target` to enabled code like: `F foo = fooref.set(new F())`.
     */
    fun set(target: T?): T? {
        clear()
        _target = target
        return target
    }

    /** Clears the target of this reference. Automatically calls [.onClear] if the reference
     * contains a non-null target.  */
    fun clear() {
        if (_target != null) {
            val toBeCleared: T = _target!!
            _target = null
            onClear(toBeCleared)
        }
    }

    /** Performs any cleanup on the supplied target (which has been just cleared).  */
    protected abstract fun onClear(target: T)

    protected var _target: T? = null

    companion object {
        /** Creates a reference to a [Closeable] target.  */
        fun <T : Closeable> create(target: T?): Ref<T> {
            val ref = object : Ref<T>() {
                override fun onClear(target: T) {
                    target.close()
                }
            }
            ref.set(target)
            return ref
        }
    }
}
