package tripleklay.ui.util

import euklid.f.Dimension

/** Corresponds to the distances that some rectangular object's edges will be offset when,
 * for example, it is contained in another rectangle.  */
open class Insets
/** Creates new insets.  */
(
        /** The amount to inset an edge.  */
        protected var _top: Float, protected var _right: Float, protected var _bottom: Float, protected var _left: Float) {

    /** Insets with changeable values.  */
    class Mutable(i: Insets) : Insets(i.top(), i.right(), i.bottom(), i.left()) {

        override fun mutable(): Mutable {
            return this
        }

        /** Adds the given insets to these. Returns `this` for chaining.  */
        fun add(insets: Insets): Mutable {
            _top += insets._top
            _right += insets._right
            _bottom += insets._bottom
            _left += insets._left
            return this
        }

        /** Sets the top edge and returns `this` for chaining.  */
        fun top(newTop: Float): Mutable {
            _top = newTop
            return this
        }

        /** Sets the right edge and returns `this` for chaining.  */
        fun right(newRight: Float): Mutable {
            _right = newRight
            return this
        }

        /** Sets the bottom edge and returns `this` for chaining.  */
        fun bottom(newBottom: Float): Mutable {
            _bottom = newBottom
            return this
        }

        /** Sets the left edge and returns `this` for chaining.  */
        fun left(newLeft: Float): Mutable {
            _left = newLeft
            return this
        }
    }

    /** Gets the top inset.  */
    fun top(): Float {
        return _top
    }

    /** Gets the right inset.  */
    fun right(): Float {
        return _right
    }

    /** Gets the bottom inset.  */
    fun bottom(): Float {
        return _bottom
    }

    /** Gets the left inset.  */
    fun left(): Float {
        return _left
    }

    /** Returns the total adjustment to width.  */
    fun width(): Float {
        return _left + _right
    }

    /** Returns this total adjustment to height.  */
    fun height(): Float {
        return _top + _bottom
    }

    /** Adds these insets to the supplied dimensions. Returns `size` for chaining.  */
    fun addTo(size: Dimension): Dimension {
        size.width += width()
        size.height += height()
        return size
    }

    /** Adds these insets from the supplied dimensions. Returns `size` for chaining.  */
    fun subtractFrom(size: Dimension): Dimension {
        size.width -= width()
        size.height -= height()
        return size
    }

    /** Gets or creates a copy of these insets that can be mutated. Note, if storing an instance,
     * the caller is expected to assign to the return value here in case a new object is
     * allocated.  */
    open fun mutable(): Mutable {
        return Mutable(this)
    }

    /** Returns a new instance which is the supplied adjustments added to these insets.  */
    fun adjust(dtop: Float, dright: Float, dbottom: Float, dleft: Float): Insets {
        return Insets(_top + dtop, _right + dright, _bottom + dbottom, _left + dleft)
    }

    override fun toString(): String {
        return _top.toString() + "," + _right + "," + _bottom + "," + _left
    }

    companion object {
        /** Read-only instance with zero for all edges.  */
        var ZERO = Insets(0f, 0f, 0f, 0f)

        /** Returns a read-only instance with all edges set to the same value.  */
        fun uniform(`val`: Float): Insets {
            return Insets(`val`, `val`, `val`, `val`)
        }

        /** Returns a read-only instance with left and right set to one value and top and bottom set
         * to another.  */
        fun symmetric(horiz: Float, vert: Float): Insets {
            return Insets(vert, horiz, vert, horiz)
        }
    }
}

