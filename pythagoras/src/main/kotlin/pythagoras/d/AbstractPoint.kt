//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.d

import pythagoras.util.Platform

/**
 * Provides most of the implementation of [IPoint], obtaining only the location from the
 * derived class.
 */
abstract class AbstractPoint : IPoint {
    override // from IPoint
    fun distanceSq(px: Double, py: Double): Double {
        return Points.distanceSq(x(), y(), px, py)
    }

    override // from IPoint
    fun distanceSq(p: XY): Double {
        return Points.distanceSq(x(), y(), p.x(), p.y())
    }

    override // from IPoint
    fun distance(px: Double, py: Double): Double {
        return Points.distance(x(), y(), px, py)
    }

    override // from IPoint
    fun distance(p: XY): Double {
        return Points.distance(x(), y(), p.x(), p.y())
    }

    override // from interface IPoint
    fun direction(other: XY): Double {
        return Math.atan2(other.y() - y(), other.x() - x())
    }

    override // from IPoint
    fun mult(s: Double): Point {
        return mult(s, Point())
    }

    override // from IPoint
    fun mult(s: Double, result: Point): Point {
        return result.set(x() * s, y() * s)
    }

    override // from IPoint
    fun add(x: Double, y: Double): Point {
        return Point(x() + x, y() + y)
    }

    override // from IPoint
    fun add(x: Double, y: Double, result: Point): Point {
        return result.set(x() + x, y() + y)
    }

    override // from IPoint
    fun add(other: XY, result: Point): Point {
        return add(other.x(), other.y(), result)
    }

    override // from IPoint
    fun subtract(x: Double, y: Double): Point {
        return subtract(x, y, Point())
    }

    override // from IPoint
    fun subtract(x: Double, y: Double, result: Point): Point {
        return result.set(x() - x, y() - y)
    }

    override // from IPoint
    fun subtract(other: XY, result: Point): Point {
        return subtract(other.x(), other.y(), result)
    }

    override // from IPoint
    fun rotate(angle: Double): Point {
        return rotate(angle, Point())
    }

    override // from IPoint
    fun rotate(angle: Double, result: Point): Point {
        val x = x()
        val y = y()
        val sina = Math.sin(angle)
        val cosa = Math.cos(angle)
        return result.set(x * cosa - y * sina, x * sina + y * cosa)
    }

    override // from IPoint
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
        return Platform.hashCode(x()) xor Platform.hashCode(y())
    }

    override fun toString(): String {
        return Points.pointToString(x(), y())
    }
}
