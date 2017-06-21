package tripleklay.ui.util

import tripleklay.ui.Container
import tripleklay.ui.Element

/**
 * A view for the hierarchical structure of an [Element].
 */
class Hierarchy
/**
 * Creates a new view focused on the given element.
 */
(
        /** The element that is the focus of this view.  */
        val elem: Element<*>) {
    /**
     * Iterates over the ancestors of an element. See [.ancestors].
     */
    class Ancestors(var current: Element<*>?) : Iterator<Element<*>> {

        init {
            if (current == null) {
                throw IllegalArgumentException()
            }
        }

        override fun hasNext(): Boolean {
            return current != null
        }

        override fun next(): Element<*> {
            if (!hasNext()) {
                throw IllegalStateException()
            }
            val next = current
            current = current!!.parent()
            return next!!
        }
    }

    /**
     * Tests if the given element is a proper descendant contained in this hierarchy, or is the
     * root.
     */
    fun hasDescendant(descendant: Element<*>?): Boolean {
        if (descendant === elem) return true
        if (descendant == null) return false
        return hasDescendant(descendant.parent())
    }

    /**
     * Returns an object to iterate over the ancestors of this hierarchy, including the root.
     */
    fun ancestors(): Iterable<Element<*>> {
        return object : Iterable<Element<*>> {
            override fun iterator(): Iterator<Element<*>> {
                return Ancestors(elem)
            }
        }
    }

    /**
     * Applies the given operation to the root of the hierarchy and to every proper descendant.
     */
    fun apply(op: ElementOp<Element<*>>): Hierarchy {
        forEachDescendant(elem, op)
        return this
    }

    companion object {

        /**
         * Create a new view of the given element.
         */
        fun of(elem: Element<*>): Hierarchy {
            return Hierarchy(elem)
        }

        protected fun forEachDescendant(root: Element<*>, op: ElementOp<Element<*>>) {
            op.apply(root)
            if (root is Container<*>) {
                val es = root
                var ii = 0
                val ll = es.childCount()
                while (ii < ll) {
                    forEachDescendant(es.childAt(ii), op)
                    ++ii
                }
            }
        }
    }
}
