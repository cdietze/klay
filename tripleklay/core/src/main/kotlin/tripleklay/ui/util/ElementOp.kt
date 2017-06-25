package tripleklay.ui.util

import tripleklay.ui.Element

/**
 * Defines a method that applies an operation to an element.
 * @param <T> the leaf type of Element.
</T> */
abstract class ElementOp<in T : Element<*>> {

    /**
     * Applies an arbitrary operation to the given element.
     */
    abstract fun apply(elem: T)

    /**
     * Iterates the given elements and applies this operation to each.
     */
    fun applyToEach(elems: Iterable<T>) {
        for (elem in elems) apply(elem)
    }

    companion object {
        /**
         * Returns an element operation that enables or disables its elements. Usage:
         * <pre>`Hierarchy.of(elem).apply(ElementOp.setEnabled(false));
        `</pre> *
         */
        fun setEnabled(enabled: Boolean): ElementOp<Element<*>> {
            return object : ElementOp<Element<*>>() {
                override fun apply(elem: Element<*>) {
                    elem.setEnabled(enabled)
                }
            }
        }
    }
}
