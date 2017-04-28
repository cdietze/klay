//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

/**
 * Represents a vector in a plane.
 */
class Vector : AbstractVector {
    /** The x-component of the vector.  */
    var x: Float = 0.toFloat()

    /** The y-component of the vector.  */
    var y: Float = 0.toFloat()

    /** Creates a vector with the specified x and y components.  */
    constructor(x: Float, y: Float) {
        set(x, y)
    }

    /** Creates a vector equal to `other`.  */
    constructor(other: XY) {
        set(other)
    }

    /** Creates a vector with zero x and y components.  */
    constructor() {}

    /** Computes the cross product of this and the specified other vector, storing the result in
     * this vector.
     * @return a reference to this vector, for chaining.
     */
    fun crossLocal(other: IVector): Vector {
        return cross(other, this)
    }

    /** Negates this vector in-place.
     * @return a reference to this vector, for chaining.
     */
    fun negateLocal(): Vector {
        return negate(this)
    }

    /** Normalizes this vector in-place.
     * @return a reference to this vector, for chaining.
     */
    fun normalizeLocal(): Vector {
        return normalize(this)
    }

    /** Scales this vector in place, uniformly by the specified magnitude.
     * @return a reference to this vector, for chaining.
     */
    fun scaleLocal(v: Float): Vector {
        return scale(v, this)
    }

    /** Scales this vector's x and y components, in place, independently by the x and y components
     * of the supplied vector.
     * @return a reference to this vector, for chaining.
     */
    fun scaleLocal(other: IVector): Vector {
        return scale(other, this)
    }

    /** Adds a vector in-place to this one.
     * @return a reference to this vector, for chaining.
     */
    fun addLocal(other: IVector): Vector {
        return add(other, this)
    }

    /** Subtracts a vector in-place from this one.
     * @return a reference to this vector, for chaining.
     */
    fun subtractLocal(other: IVector): Vector {
        return subtract(other, this)
    }

    /** Adds a vector in-place to this one.
     * @return a reference to this vector, for chaining.
     */
    fun addLocal(x: Float, y: Float): Vector {
        return add(x, y, this)
    }

    /** Subtracts a vector in-place from this one.
     * @return a reference to this vector, for chaining.
     */
    fun subtractLocal(x: Float, y: Float): Vector {
        return subtract(x, y, this)
    }

    /** Adds a scaled vector in-place to this one.
     * @return a reference to this vector, for chaining.
     */
    fun addScaledLocal(other: IVector, v: Float): Vector {
        return addScaled(other, v, this)
    }

    /** Rotates this vector in-place by the specified angle.
     * @return a reference to this vector, for chaining.
     */
    fun rotateLocal(angle: Float): Vector {
        return rotate(angle, this)
    }

    /** Linearly interpolates between this and `other` in-place by the supplied amount.
     * @return a reference to this vector, for chaining.
     */
    fun lerpLocal(other: IVector, t: Float): Vector {
        return lerp(other, t, this)
    }

    /** Copies the elements of another vector.
     * @return a reference to this vector, for chaining.
     */
    fun set(other: XY): Vector {
        return set(other.x(), other.y())
    }

    /** Copies the elements of an array.
     * @return a reference to this vector, for chaining.
     */
    fun set(values: FloatArray): Vector {
        return set(values[0], values[1])
    }

    /** Sets all of the elements of the vector.
     * @return a reference to this vector, for chaining.
     */
    operator fun set(x: Float, y: Float): Vector {
        this.x = x
        this.y = y
        return this
    }

    /**
     * Sets this vector's angle, preserving its magnitude.
     * @return a reference to this vector, for chaining.
     */
    fun setAngle(angle: Float): Vector {
        val l = length()
        return set(l * FloatMath.cos(angle), l * FloatMath.sin(angle))
    }

    /**
     * Sets this vector's magnitude, preserving its angle.
     */
    fun setLength(length: Float): Vector {
        return normalizeLocal().scaleLocal(length)
    }

    override // from XY
    fun x(): Float {
        return x
    }

    override // from XY
    fun y(): Float {
        return y
    }
}
