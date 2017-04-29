//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

import pythagoras.util.Platform
import java.lang.Math

/**
 * A three element vector.
 */
class Vector3 : IVector3 {

    /** The components of the vector.  */
    override var x: Float = 0.toFloat()
    override var y: Float = 0.toFloat()
    override var z: Float = 0.toFloat()

    /**
     * Creates a vector from three components.
     */
    constructor(x: Float, y: Float, z: Float) {
        set(x, y, z)
    }

    /**
     * Creates a vector from an array of values.
     */
    constructor(values: FloatArray) {
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
    fun multLocal(v: Float): Vector3 {
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
    fun addLocal(x: Float, y: Float, z: Float): Vector3 {
        return add(x, y, z, this)
    }

    /**
     * Adds a scaled vector in-place to this one.

     * @return a reference to this vector, for chaining.
     */
    fun addScaledLocal(other: IVector3, v: Float): Vector3 {
        return addScaled(other, v, this)
    }

    /**
     * Linearly interpolates between this and the specified other vector in-place by the supplied
     * amount.

     * @return a reference to this vector, for chaining.
     */
    fun lerpLocal(other: IVector3, t: Float): Vector3 {
        return lerp(other, t, this)
    }

    /**
     * Copies the elements of another vector.

     * @return a reference to this vector, for chaining.
     */
    fun set(other: IVector3): Vector3 {
        return set(other.x, other.y, other.z)
    }

    /**
     * Copies the elements of an array.

     * @return a reference to this vector, for chaining.
     */
    fun set(values: FloatArray): Vector3 {
        return set(values[0], values[1], values[2])
    }

    /**
     * Sets all of the elements of the vector.

     * @return a reference to this vector, for chaining.
     */
    operator fun set(x: Float, y: Float, z: Float): Vector3 {
        this.x = x
        this.y = y
        this.z = z
        return this
    }

    override // from interface IVector3
    fun dot(other: IVector3): Float {
        return x * other.x + y * other.y + z * other.z
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
        val ox = other.x
        val oy = other.y
        val oz = other.z
        return result.set(y * oz - z * oy, z * ox - x * oz, x * oy - y * ox)
    }

    override // from interface IVector3
    fun triple(b: IVector3, c: IVector3): Float {
        val bx = b.x
        val by = b.y
        val bz = b.z
        val cx = c.x
        val cy = c.y
        val cz = c.z
        return x * (by * cz - bz * cy) + y * (bz * cx - bx * cz) + z * (bx * cy - by * cx)
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
    fun angle(other: IVector3): Float {
        return FloatMath.acos(dot(other) / (length() * other.length()))
    }

    override // from interface IVector3
    fun length(): Float {
        return FloatMath.sqrt(lengthSquared())
    }

    override // from interface IVector3
    fun lengthSquared(): Float {
        val x = this.x
        val y = this.y
        val z = this.z
        return x * x + y * y + z * z
    }

    override // from interface IVector3
    fun distance(other: IVector3): Float {
        return FloatMath.sqrt(distanceSquared(other))
    }

    override // from interface IVector3
    fun distanceSquared(other: IVector3): Float {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return dx * dx + dy * dy + dz * dz
    }

    override // from interface IVector3
    fun manhattanDistance(other: IVector3): Float {
        return Math.abs(x - other.x) + Math.abs(y - other.y) + Math.abs(z - other.z)
    }

    override // from interface IVector3
    fun mult(v: Float): Vector3 {
        return mult(v, Vector3())
    }

    override // from interface IVector3
    fun mult(v: Float, result: Vector3): Vector3 {
        return result.set(x * v, y * v, z * v)
    }

    override // from interface IVector3
    fun mult(other: IVector3): Vector3 {
        return mult(other, Vector3())
    }

    override // from interface IVector3
    fun mult(other: IVector3, result: Vector3): Vector3 {
        return result.set(x * other.x, y * other.y, z * other.z)
    }

    override // from interface IVector3
    fun add(other: IVector3): Vector3 {
        return add(other, Vector3())
    }

    override // from interface IVector3
    fun add(other: IVector3, result: Vector3): Vector3 {
        return add(other.x, other.y, other.z, result)
    }

    override // from interface IVector3
    fun subtract(other: IVector3): Vector3 {
        return subtract(other, Vector3())
    }

    override // from interface IVector3
    fun subtract(other: IVector3, result: Vector3): Vector3 {
        return add(-other.x, -other.y, -other.z, result)
    }

    override // from interface IVector3
    fun add(x: Float, y: Float, z: Float): Vector3 {
        return add(x, y, z, Vector3())
    }

    override // from interface IVector3
    fun add(x: Float, y: Float, z: Float, result: Vector3): Vector3 {
        return result.set(this.x + x, this.y + y, this.z + z)
    }

    override // from interface IVector3
    fun addScaled(other: IVector3, v: Float): Vector3 {
        return addScaled(other, v, Vector3())
    }

    override // from interface IVector3
    fun addScaled(other: IVector3, v: Float, result: Vector3): Vector3 {
        return result.set(x + other.x * v, y + other.y * v, z + other.z * v)
    }

    override // from interface IVector3
    fun lerp(other: IVector3, t: Float): Vector3 {
        return lerp(other, t, Vector3())
    }

    override // from interface IVector3
    fun lerp(other: IVector3, t: Float, result: Vector3): Vector3 {
        val x = this.x
        val y = this.y
        val z = this.z
        return result.set(x + t * (other.x - x), y + t * (other.y - y), z + t * (other.z - z))
    }

    override // from interface IVector3
    fun get(idx: Int): Float {
        when (idx) {
            0 -> return x
            1 -> return y
            2 -> return z
        }
        throw IndexOutOfBoundsException(idx.toString())
    }

    override // from interface IVector3
    fun get(values: FloatArray) {
        values[0] = x
        values[1] = y
        values[2] = z
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
        private const val serialVersionUID = -3884541171214417861L

        /** A unit vector in the X+ direction.  */
        val UNIT_X: IVector3 = Vector3(1f, 0f, 0f)

        /** A unit vector in the Y+ direction.  */
        val UNIT_Y: IVector3 = Vector3(0f, 1f, 0f)

        /** A unit vector in the Z+ direction.  */
        val UNIT_Z: IVector3 = Vector3(0f, 0f, 1f)

        /** A vector containing unity for all components.  */
        val UNIT_XYZ: IVector3 = Vector3(1f, 1f, 1f)

        /** A normalized version of UNIT_XYZ.  */
        val NORMAL_XYZ: IVector3 = UNIT_XYZ.normalize()

        /** The zero vector.  */
        val ZERO: IVector3 = Vector3(0f, 0f, 0f)

        /** A vector containing the minimum floating point value for all components
         * (note: the components are -[Float.MAX_VALUE], not [Float.MIN_VALUE]).  */
        val MIN_VALUE: IVector3 = Vector3(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE)

        /** A vector containing the maximum floating point value for all components.  */
        val MAX_VALUE: IVector3 = Vector3(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE)
    }
}
