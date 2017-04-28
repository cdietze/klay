//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.d

import pythagoras.util.Platform

/**
 * Provides most of the implementation of [IDimension], obtaining only width and height from
 * the derived class.
 */
abstract class AbstractDimension : IDimension {
    override // from interface IDimension
    fun clone(): Dimension {
        return Dimension(this)
    }

    override fun hashCode(): Int {
        return Platform.hashCode(width()) xor Platform.hashCode(height())
    }

    override fun equals(obj: Any?): Boolean {
        if (obj === this) {
            return true
        }
        if (obj is AbstractDimension) {
            val d = obj
            return d.width() == width() && d.height() == height()
        }
        return false
    }

    override fun toString(): String {
        return Dimensions.dimenToString(width(), height())
    }
}
