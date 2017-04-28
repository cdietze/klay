//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.d

import pythagoras.util.Platform

import java.io.Serializable
import java.nio.DoubleBuffer

/**
 * A three element vector.
 */
class Vector3 : IVector3, Serializable {

    /** The components of the vector.  */
    var x: Double = 0.toDouble()
    var y: Double = 0.toDouble()
    var z: Double = 0.toDouble()

    /**
     * Creates a vector from three components.
     */
    constructor(x: Double, y: Double, z: Double) {
        set(x, y, z)
    }

    /**
     * Creates a vector from an array of values.
     */
    constructor(values: DoubleArray) {
        set(values)
    }

    /**
     * Copy constructor.
     */
    constructor(other: IVector3) {
        set(other)
    }

    /**
     * Creates a zero vector.
     */
    constructor() {}

    /**
     * Computes the cross product of this and the specified other vector, storing the result
     * in this vector.

     * @return a reference to this vector, for chaining.
     */
    fun crossLocal(other: IVector3): Vector3 {
        return cross(other, this)
    }

    /**
     * Negates this vector in-place.

     * @return a reference to this vector, for chaining.
     */
    fun negateLocal(): Vector3 {
        return negate(this)
    }

    /**
     * Absolute-values this vector in-place.

     * @return a reference to this vector, for chaining.
     */
    fun absLocal(): Vector3 {
        return abs(this)
    }

    /**
     * Normalizes this vector in-place.

     * @return a reference to this vector, for chaining.
     */
    fun normalizeLocal(): Vector3 {
        return normalize(this)
    }

    /**
     * Multiplies this vector in-place by a scalar.

     * @return a reference to this vector, for chaining.
     */
    fun multLocal(v: Double): Vector3 {
        return mult(v, this)
    }

    /**
     * Multiplies this vector in-place by another.

     * @return a reference to this vector, for chaining.
     */
    fun multLocal(other: IVector3): Vector3 {
        return mult(other, this)
    }

    /**
     * Adds a vector in-place to this one.

     * @return a reference to this vector, for chaining.
     */
    fun addLocal(other: IVector3): Vector3 {
        return add(other, this)
    }

    /**
     * Subtracts a vector in-place from this one.

     * @return a reference to this vector, for chaining.
     */
    fun subtractLocal(other: IVector3): Vector3 {
        return subtract(other, this)
    }

    /**
     * Adds a vector in-place to this one.

     * @return a reference to this vector, for chaining.
     */
    fun addLocal(x: Double, y: Double, z: Double): Vector3 {
        return add(x, y, z, this)
    }

    /**
     * Adds a scaled vector in-place to this one.

     * @return a reference to this vector, for chaining.
     */
    fun addScaledLocal(other: IVector3, v: Double): Vector3 {
        return addScaled(other, v, this)
    }

    /**
     * Linearly interpolates between this and the specified other vector in-place by the supplied
     * amount.

     * @return a reference to this vector, for chaining.
     */
    fun lerpLocal(other: IVector3, t: Double): Vector3 {
        return lerp(other, t, this)
    }

    /**
     * Copies the elements of another vector.

     * @return a reference to this vector, for chaining.
     */
    fun set(other: IVector3): Vector3 {
        return set(other.x(), other.y(), other.z())
    }

    /**
     * Copies the elements of an array.

     * @return a reference to this vector, for chaining.
     */
    fun set(values: DoubleArray): Vector3 {
        return set(values[0], values[1], values[2])
    }

    /**
     * Sets all of the elements of the vector.

     * @return a reference to this vector, for chaining.
     */
    operator fun set(x: Double, y: Double, z: Double): Vector3 {
        this.x = x
        this.y = y
        this.z = z
        return this
    }

    override // from IVector3
    fun x(): Double {
        return x
    }

    override // from IVector3
    fun y(): Double {
        return y
    }

    override // from IVector3
    fun z(): Double {
        return z
    }

    override // from interface IVector3
    fun dot(other: IVector3): Double {
        return x * other.x() + y * other.y() + z * other.z()
    }

    override // from interface IVector3
    fun cross(other: IVector3): Vector3 {
        return cross(other, Vector3())
    }

    override // from interface IVector3
    fun cross(other: IVector3, result: Vector3): Vector3 {
        val x = this.x
        val y = this.y
        val z = this.z
        val ox = other.x()
        val oy = other.y()
        val oz = other.z()
        return result.set(y * oz - z * oy, z * ox - x * oz, x * oy - y * ox)
    }

    override // from interface IVector3
    fun triple(b: IVector3, c: IVector3): Double {
        val bx = b.x()
        val by = b.y()
        val bz = b.z()
        val cx = c.x()
        val cy = c.y()
        val cz = c.z()
        return x() * (by * cz - bz * cy) + y() * (bz * cx - bx * cz) + z() * (bx * cy - by * cx)
    }

    override // from interface IVector3
    fun negate(): Vector3 {
        return negate(Vector3())
    }

    override // from interface IVector3
    fun negate(result: Vector3): Vector3 {
        return result.set(-x, -y, -z)
    }

    override // from interface IVector3
    fun abs(): Vector3 {
        return abs(Vector3())
    }

    override // from interface IVector3
    fun abs(result: Vector3): Vector3 {
        return result.set(Math.abs(x), Math.abs(y), Math.abs(z))
    }

    override // from interface IVector3
    fun normalize(): Vector3 {
        return normalize(Vector3())
    }

    override // from interface IVector3
    fun normalize(result: Vector3): Vector3 {
        return mult(1f / length(), result)
    }

    override // from interface IVector3
    fun angle(other: IVector3): Double {
        return Math.acos(dot(other) / (length() * other.length()))
    }

    override // from interface IVector3
    fun length(): Double {
        return Math.sqrt(lengthSquared())
    }

    override // from interface IVector3
    fun lengthSquared(): Double {
        val x = this.x
        val y = this.y
        val z = this.z
        return x * x + y * y + z * z
    }

    override // from interface IVector3
    fun distance(other: IVector3): Double {
        return Math.sqrt(distanceSquared(other))
    }

    override // from interface IVector3
    fun distanceSquared(other: IVector3): Double {
        val dx = x - other.x()
        val dy = y - other.y()
        val dz = z - other.z()
        return dx * dx + dy * dy + dz * dz
    }

    override // from interface IVector3
    fun manhattanDistance(other: IVector3): Double {
        return Math.abs(x - other.x()) + Math.abs(y - other.y()) + Math.abs(z - other.z())
    }

    override // from interface IVector3
    fun mult(v: Double): Vector3 {
        return mult(v, Vector3())
    }

    override // from interface IVector3
    fun mult(v: Double, result: Vector3): Vector3 {
        return result.set(x * v, y * v, z * v)
    }

    override // from interface IVector3
    fun mult(other: IVector3): Vector3 {
        return mult(other, Vector3())
    }

    override // from interface IVector3
    fun mult(other: IVector3, result: Vector3): Vector3 {
        return result.set(x * other.x(), y * other.y(), z * other.z())
    }

    override // from interface IVector3
    fun add(other: IVector3): Vector3 {
        return add(other, Vector3())
    }

    override // from interface IVector3
    fun add(other: IVector3, result: Vector3): Vector3 {
        return add(other.x(), other.y(), other.z(), result)
    }

    override // from interface IVector3
    fun subtract(other: IVector3): Vector3 {
        return subtract(other, Vector3())
    }

    override // from interface IVector3
    fun subtract(other: IVector3, result: Vector3): Vector3 {
        return add(-other.x(), -other.y(), -other.z(), result)
    }

    override // from interface IVector3
    fun add(x: Double, y: Double, z: Double): Vector3 {
        return add(x, y, z, Vector3())
    }

    override // from interface IVector3
    fun add(x: Double, y: Double, z: Double, result: Vector3): Vector3 {
        return result.set(this.x + x, this.y + y, this.z + z)
    }

    override // from interface IVector3
    fun addScaled(other: IVector3, v: Double): Vector3 {
        return addScaled(other, v, Vector3())
    }

    override // from interface IVector3
    fun addScaled(other: IVector3, v: Double, result: Vector3): Vector3 {
        return result.set(x + other.x() * v, y + other.y() * v, z + other.z() * v)
    }

    override // from interface IVector3
    fun lerp(other: IVector3, t: Double): Vector3 {
        return lerp(other, t, Vector3())
    }

    override // from interface IVector3
    fun lerp(other: IVector3, t: Double, result: Vector3): Vector3 {
        val x = this.x
        val y = this.y
        val z = this.z
        return result.set(x + t * (other.x() - x), y + t * (other.y() - y), z + t * (other.z() - z))
    }

    override // from interface IVector3
    fun get(idx: Int): Double {
        when (idx) {
            0 -> return x
            1 -> return y
            2 -> return z
        }
        throw IndexOutOfBoundsException(idx.toString())
    }

    override // from interface IVector3
    fun get(values: DoubleArray) {
        values[0] = x
        values[1] = y
        values[2] = z
    }

    override // from interface IVector3
    fun get(buf: DoubleBuffer): DoubleBuffer {
        return buf.put(x).put(y).put(z)
    }

    override fun toString(): String {
        return "[$x, $y, $z]"
    }

    override fun hashCode(): Int {
        return Platform.hashCode(x) xor Platform.hashCode(y) xor Platform.hashCode(z)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Vector3) {
            return false
        }
        val ovec = other
        return x == ovec.x && y == ovec.y && z == ovec.z
    }

    companion object {
        private const val serialVersionUID = -6374261949619913930L

        /** A unit vector in the X+ direction.  */
        val UNIT_X: IVector3 = Vector3(1.0, 0.0, 0.0)

        /** A unit vector in the Y+ direction.  */
        val UNIT_Y: IVector3 = Vector3(0.0, 1.0, 0.0)

        /** A unit vector in the Z+ direction.  */
        val UNIT_Z: IVector3 = Vector3(0.0, 0.0, 1.0)

        /** A vector containing unity for all components.  */
        val UNIT_XYZ: IVector3 = Vector3(1.0, 1.0, 1.0)

        /** A normalized version of UNIT_XYZ.  */
        val NORMAL_XYZ: IVector3 = UNIT_XYZ.normalize()

        /** The zero vector.  */
        val ZERO: IVector3 = Vector3(0.0, 0.0, 0.0)

        /** A vector containing the minimum doubleing point value for all components
         * (note: the components are -[Float.MAX_VALUE], not [Float.MIN_VALUE]).  */
        val MIN_VALUE: IVector3 = Vector3((-java.lang.Float.MAX_VALUE).toDouble(), (-java.lang.Float.MAX_VALUE).toDouble(), (-java.lang.Float.MAX_VALUE).toDouble())

        /** A vector containing the maximum doubleing point value for all components.  */
        val MAX_VALUE: IVector3 = Vector3(java.lang.Float.MAX_VALUE.toDouble(), java.lang.Float.MAX_VALUE.toDouble(), java.lang.Float.MAX_VALUE.toDouble())
    }
}
