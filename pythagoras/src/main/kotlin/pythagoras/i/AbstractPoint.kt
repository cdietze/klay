//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.i

/**
 * Provides most of the implementation of [IPoint], obtaining only the location from the
 * derived class.
 */
abstract class AbstractPoint : IPoint {
    override // from IPoint
    fun distanceSq(px: Int, py: Int): Int {
        return Points.distanceSq(x(), y(), px, py)
    }

    override // from IPoint
    fun distanceSq(p: IPoint): Int {
        return Points.distanceSq(x(), y(), p.x(), p.y())
    }

    override // from IPoint
    fun distance(px: Int, py: Int): Int {
        return Points.distance(x(), y(), px, py)
    }

    override // from IPoint
    fun distance(p: IPoint): Int {
        return Points.distance(x(), y(), p.x(), p.y())
    }

    override // from IPoint
    fun add(x: Int, y: Int): Point {
        return Point(x() + x, y() + y)
    }

    override // from IPoint
    fun add(x: Int, y: Int, result: Point): Point {
        return result.set(x() + x, y() + y)
    }

    override // from IPoint
    fun subtract(x: Int, y: Int): Point {
        return subtract(x, y, Point())
    }

    override fun subtract(x: Int, y: Int, result: Point): Point {
        return result.set(x() - x, y() - y)
    }

    override fun subtract(other: IPoint, result: Point): Point {
        return subtract(other.x(), other.y(), result)
    }

    override // from interface IPoint
    fun clone(): Point {
        return Point(this)
    }

    override fun equals(obj: Any?): Boolean {
        if (obj === this) {
            return true
        }
        if (obj is AbstractPoint) {
            val p = obj
            return x() == p.x() && y() == p.y()
        }
        return false
    }

    override fun hashCode(): Int {
        return x() xor y()
    }

    override fun toString(): String {
        return Points.pointToString(x(), y())
    }
}
