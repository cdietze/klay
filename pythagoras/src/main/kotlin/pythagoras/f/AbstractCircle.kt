//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

import pythagoras.util.Platform

/**
 * Provides most of the implementation of [ICircle], obtaining only the location and radius
 * from the derived class.
 */
abstract class AbstractCircle : ICircle {
    override // from ICircle
    fun intersects(c: ICircle): Boolean {
        val maxDist = radius() + c.radius()
        return Points.distanceSq(x(), y(), c.x(), c.y()) < maxDist * maxDist
    }

    override // from ICircle
    fun contains(p: XY): Boolean {
        val r = radius()
        return Points.distanceSq(x(), y(), p.x(), p.y()) < r * r
    }

    override // from ICircle
    fun contains(x: Float, y: Float): Boolean {
        val r = radius()
        return Points.distanceSq(x(), y(), x, y) < r * r
    }

    override // from ICircle
    fun offset(x: Float, y: Float): Circle {
        return Circle(x() + x, y() + y, radius())
    }

    override // from ICircle
    fun offset(x: Float, y: Float, result: Circle): Circle {
        result.set(x() + x, y() + y, radius())
        return result
    }

    override // from ICircle
    fun clone(): Circle {
        return Circle(this)
    }

    override fun equals(obj: Any?): Boolean {
        if (obj === this) {
            return true
        }
        if (obj is AbstractCircle) {
            val c = obj
            return x() == c.x() && y() == c.y() && radius() == c.radius()
        }
        return false
    }

    override fun hashCode(): Int {
        return Platform.hashCode(x()) xor Platform.hashCode(y()) xor Platform.hashCode(radius())
    }
}
