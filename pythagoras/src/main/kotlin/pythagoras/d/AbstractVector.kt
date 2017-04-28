//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.d

import pythagoras.util.Platform

/**
 * Provides most of the implementation of [IVector], obtaining only x and y from the derived
 * class.
 */
abstract class AbstractVector : IVector {
    override // from interface IVector
    fun dot(other: IVector): Double {
        return x() * other.x() + y() * other.y()
    }

    override // from interface IVector
    fun cross(other: IVector): Vector {
        return cross(other, Vector())
    }

    override // from interface IVector
    fun cross(other: IVector, result: Vector): Vector {
        val x = x()
        val y = y()
        val ox = other.x()
        val oy = other.y()
        return result.set(y * ox - x * oy, x * oy - y * ox)
    }

    override // from interface IVector
    fun negate(): Vector {
        return negate(Vector())
    }

    override // from interface IVector
    fun negate(result: Vector): Vector {
        return result.set(-x(), -y())
    }

    override // from interface IVector
    fun normalize(): Vector {
        return normalize(Vector())
    }

    override // from interface IVector
    fun normalize(result: Vector): Vector {
        return scale(1f / length(), result)
    }

    override // from interface IVector
    fun length(): Double {
        return Math.sqrt(lengthSq())
    }

    override // from interface IVector
    fun lengthSq(): Double {
        val x = x()
        val y = y()
        return x * x + y * y
    }

    override // from interface IVector
    val isZero: Boolean
        get() = Vectors.isZero(x(), y())

    override // from interface IVector
    fun distance(other: IVector): Double {
        return Math.sqrt(distanceSq(other))
    }

    override // from interface IVector
    fun distanceSq(other: IVector): Double {
        val dx = x() - other.x()
        val dy = y() - other.y()
        return dx * dx + dy * dy
    }

    override // from interface IVector
    fun angle(): Double {
        return Math.atan2(y(), x())
    }

    override // from interface IVector
    fun angleBetween(other: IVector): Double {
        val cos = dot(other) / (length() * other.length())
        return if (cos >= 1f) 0.0 else Math.acos(cos)
    }

    override // from interface IVector
    fun scale(v: Double): Vector {
        return scale(v, Vector())
    }

    override // from interface IVector
    fun scale(v: Double, result: Vector): Vector {
        return result.set(x() * v, y() * v)
    }

    override // from interface IVector
    fun scale(other: IVector): Vector {
        return scale(other, Vector())
    }

    override // from interface IVector
    fun scale(other: IVector, result: Vector): Vector {
        return result.set(x() * other.x(), y() * other.y())
    }

    override // from interface IVector
    fun add(other: IVector): Vector {
        return add(other, Vector())
    }

    override // from interface IVector
    fun add(other: IVector, result: Vector): Vector {
        return add(other.x(), other.y(), result)
    }

    override // from interface IVector
    fun subtract(other: IVector): Vector {
        return subtract(other, Vector())
    }

    override // from interface IVector
    fun subtract(other: IVector, result: Vector): Vector {
        return add(-other.x(), -other.y(), result)
    }

    override // from interface IVector
    fun add(x: Double, y: Double): Vector {
        return add(x, y, Vector())
    }

    override // from interface IVector
    fun add(x: Double, y: Double, result: Vector): Vector {
        return result.set(x() + x, y() + y)
    }

    override // from interface IVector
    fun subtract(x: Double, y: Double): Vector {
        return subtract(x, y, Vector())
    }

    override // from interface IVector
    fun subtract(x: Double, y: Double, result: Vector): Vector {
        return result.set(x() - x, y() - y)
    }

    override // from interface IVector
    fun addScaled(other: IVector, v: Double): Vector {
        return addScaled(other, v, Vector())
    }

    override // from interface IVector
    fun addScaled(other: IVector, v: Double, result: Vector): Vector {
        return result.set(x() + other.x() * v, y() + other.y() * v)
    }

    override // from interface IVector
    fun rotate(angle: Double): Vector {
        return rotate(angle, Vector())
    }

    override // from interface IVector
    fun rotate(angle: Double, result: Vector): Vector {
        val x = x()
        val y = y()
        val sina = Math.sin(angle)
        val cosa = Math.cos(angle)
        return result.set(x * cosa - y * sina, x * sina + y * cosa)
    }

    override // from interface IVector
    fun rotateAndAdd(angle: Double, add: IVector, result: Vector): Vector {
        val x = x()
        val y = y()
        val sina = Math.sin(angle)
        val cosa = Math.cos(angle)
        return result.set(x * cosa - y * sina + add.x(), x * sina + y * cosa + add.y())
    }

    override // from interface IVector
    fun rotateScaleAndAdd(angle: Double, scale: Double, add: IVector, result: Vector): Vector {
        val x = x()
        val y = y()
        val sina = Math.sin(angle)
        val cosa = Math.cos(angle)
        return result.set((x * cosa - y * sina) * scale + add.x(),
                (x * sina + y * cosa) * scale + add.y())
    }

    override // from interface IVector
    fun lerp(other: IVector, t: Double): Vector {
        return lerp(other, t, Vector())
    }

    override // from interface IVector
    fun lerp(other: IVector, t: Double, result: Vector): Vector {
        val x = x()
        val y = y()
        val dx = other.x() - x
        val dy = other.y() - y
        return result.set(x + t * dx, y + t * dy)
    }

    override // from interface IVector
    fun clone(): Vector {
        return Vector(this)
    }

    override fun equals(obj: Any?): Boolean {
        if (obj === this) {
            return true
        }
        if (obj is AbstractVector) {
            val p = obj
            return x() == p.x() && y() == p.y()
        }
        return false
    }

    override fun hashCode(): Int {
        return Platform.hashCode(x()) xor Platform.hashCode(y())
    }

    override fun toString(): String {
        return Vectors.vectorToString(x(), y())
    }
}
