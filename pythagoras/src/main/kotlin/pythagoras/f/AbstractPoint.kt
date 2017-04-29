//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

import pythagoras.util.Platform

/**
 * Provides most of the implementation of [IPoint], obtaining only the location from the
 * derived class.
 */
abstract class AbstractPoint : IPoint {
    override // from IPoint
    fun distanceSq(px: Float, py: Float): Float {
        return Points.distanceSq(x, y, px, py)
    }

    override // from IPoint
    fun distanceSq(p: XY): Float {
        return Points.distanceSq(x, y, p.x, p.y)
    }

    override // from IPoint
    fun distance(px: Float, py: Float): Float {
        return Points.distance(x, y, px, py)
    }

    override // from IPoint
    fun distance(p: XY): Float {
        return Points.distance(x, y, p.x, p.y)
    }

    override // from interface IPoint
    fun direction(other: XY): Float {
        return FloatMath.atan2(other.y - y, other.x - x)
    }

    override // from IPoint
    fun mult(s: Float): Point {
        return mult(s, Point())
    }

    override // from IPoint
    fun mult(s: Float, result: Point): Point {
        return result.set(x * s, y * s)
    }

    override // from IPoint
    fun add(x: Float, y: Float): Point {
        return Point(x + x, y + y)
    }

    override // from IPoint
    fun add(x: Float, y: Float, result: Point): Point {
        return result.set(x + x, y + y)
    }

    override // from IPoint
    fun add(other: XY, result: Point): Point {
        return add(other.x, other.y, result)
    }

    override // from IPoint
    fun subtract(x: Float, y: Float): Point {
        return subtract(x, y, Point())
    }

    override // from IPoint
    fun subtract(x: Float, y: Float, result: Point): Point {
        return result.set(x - x, y - y)
    }

    override // from IPoint
    fun subtract(other: XY, result: Point): Point {
        return subtract(other.x, other.y, result)
    }

    override // from IPoint
    fun rotate(angle: Float): Point {
        return rotate(angle, Point())
    }

    override // from IPoint
    fun rotate(angle: Float, result: Point): Point {
        val x = x
        val y = y
        val sina = FloatMath.sin(angle)
        val cosa = FloatMath.cos(angle)
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
            return x == p.x && y == p.y
        }
        return false
    }

    override fun hashCode(): Int {
        return Platform.hashCode(x) xor Platform.hashCode(y)
    }

    override fun toString(): String {
        return Points.pointToString(x, y)
    }
}
