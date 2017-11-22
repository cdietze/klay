package tripleklay.ui

import euklid.f.Dimension
import euklid.f.IDimension

/**
 * Defines the interface to layouts, which implement a particular layout policy.
 */
abstract class Layout {
    /** An abstract base class for all layout constraints.  */
    abstract class Constraint {
        /** Called by an element when it is configured with a constraint.  */
        open fun setElement(elem: Element<*>) {
            // nothing needed by default
        }

        /** Allows a layout constraint to adjust an element's x hint.  */
        open fun adjustHintX(hintX: Float): Float {
            return hintX // no adjustments by default
        }

        /** Allows a layout constraint to adjust an element's y hint.  */
        open fun adjustHintY(hintY: Float): Float {
            return hintY // no adjustments by default
        }

        /** Allows a layout constraint to adjust an element's preferred size.  */
        open fun adjustPreferredSize(psize: Dimension, hintX: Float, hintY: Float) {
            // no adjustments by default
        }
    }

    /**
     * Computes and returns the size needed to arrange children of the supplied container according
     * to their preferred size, given the specified x and y size hints.
     */
    abstract fun computeSize(elems: Container<*>, hintX: Float, hintY: Float): Dimension

    /**
     * Lays out the supplied elements into a region of the specified dimensions.
     */
    abstract fun layout(elems: Container<*>, left: Float, top: Float,
                        width: Float, height: Float)

    // make Element.resolveStyle "visible" to custom layouts
    protected fun <V> resolveStyle(elem: Element<*>, style: Style<V>): V {
        return elem.resolveStyle(style)
    }

    // make Element.preferredSize "visible" to custom layouts
    protected fun preferredSize(elem: Element<*>, hintX: Float, hintY: Float): IDimension {
        return elem.preferredSize(hintX, hintY)
    }

    protected fun setBounds(elem: Element<*>, x: Float, y: Float, width: Float, height: Float) {
        elem.setLocation(x, y)
        elem.setSize(width, height)
    }
}
