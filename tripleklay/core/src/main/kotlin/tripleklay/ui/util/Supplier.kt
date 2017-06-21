package tripleklay.ui.util

import react.Closeable
import tripleklay.ui.Element

/**
 * Supplies elements. The means of achieving this depends on the situation. Common cases are to
 * provide a fixed instance or to construct a new element for the caller to cache. Particular
 * attention is paid to ownership and orderly resource disposal.
 */
abstract class Supplier : Closeable {

    /**
     * Gets the element. Ownership of the element's resources (its layer) must also be transferred.
     * For example, if you don't add the element to any hierarchy, you need to call its `layer.close` later.
     */
    abstract fun get(): Element<*>

    /**
     * Disposes resources associated with the supplier instance. The base class implementation does
     * nothing.
     */
    override fun close() {}

    companion object {
        /**
         * Creates a supplier that will return a previously created element the first time and null
         * thereafter. If the element is still present when dispose is called, the element's layer will
         * be disposeed.
         */
        fun auto(elem: Element<*>): Supplier {
            return object : Supplier() {
                internal var element: Element<*>? = elem
                override fun get(): Element<*> {
                    val ret = element
                    element = null
                    return ret!!
                }

                override fun close() {
                    if (element != null) element!!.layer.close()
                    element = null
                }
            }
        }

        /**
         * Creates a supplier that wraps another supplier and on dispose also disposes the created
         * element, if it implements [Closeable].
         */
        fun withDispose(other: Supplier): Supplier {
            return object : Supplier() {
                internal var created: Element<*>? = null
                override fun get(): Element<*> {
                    created = other.get()
                    return created!!
                }

                override fun close() {
                    other.close()
                    if (created is Closeable) {
                        (created as Closeable).close()
                    }
                }
            }
        }
    }
}
