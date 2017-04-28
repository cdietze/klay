//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.d

/**
 * Provides read-only access to a [Vector].
 */
interface IVector : XY {
    /** Computes and returns the dot product of this and the specified other vector.  */
    fun dot(other: IVector): Double

    /** Computes the cross product of this and the specified other vector.
     * @return a new vector containing the result.
     */
    fun cross(other: IVector): Vector

    /** Computes the cross product of this and the specified other vector, placing the result in
     * the object supplied.
     * @return a reference to the result, for chaining.
     */
    fun cross(other: IVector, result: Vector): Vector

    /** Negates this vector.
     * @return a new vector containing the result.
     */
    fun negate(): Vector

    /** Negates this vector, storing the result in the supplied object.
     * @return a reference to the result, for chaining.
     */
    fun negate(result: Vector): Vector

    /** Normalizes this vector.
     * @return a new vector containing the result.
     */
    fun normalize(): Vector

    /** Normalizes this vector, storing the result in the object supplied.
     * @return a reference to the result, for chaining.
     */
    fun normalize(result: Vector): Vector

    /** Returns the length (magnitude) of this vector.  */
    fun length(): Double

    /** Returns the squared length of this vector.  */
    fun lengthSq(): Double

    /** Returns true if this vector has zero magnitude.  */
    val isZero: Boolean

    /** Returns the distance from this vector to the specified other vector.  */
    fun distance(other: IVector): Double

    /** Returns the squared distance from this vector to the specified other.  */
    fun distanceSq(other: IVector): Double

    /** Returns the angle of this vector.  */
    fun angle(): Double

    /** Returns the angle between this vector and the specified other vector.  */
    fun angleBetween(other: IVector): Double

    /** Scales this vector uniformly by the specified magnitude.
     * @return a new vector containing the result.
     */
    fun scale(v: Double): Vector

    /** Scales this vector uniformly by the specified magnitude, and places the result in the
     * supplied object.
     * @return a reference to the result, for chaining.
     */
    fun scale(v: Double, result: Vector): Vector

    /** Scales this vector's x and y components independently by the x and y components of the
     * supplied vector.
     * @return a new vector containing the result.
     */
    fun scale(other: IVector): Vector

    /** Scales this vector's x and y components independently by the x and y components of the
     * supplied vector, and stores the result in the object provided.
     * @return a reference to the result vector, for chaining.
     */
    fun scale(other: IVector, result: Vector): Vector

    /** Adds a vector to this one.
     * @return a new vector containing the result.
     */
    fun add(other: IVector): Vector

    /** Adds a vector to this one, storing the result in the object provided.
     * @return a reference to the result, for chaining.
     */
    fun add(other: IVector, result: Vector): Vector

    /** Adds a vector to this one.
     * @return a new vector containing the result.
     */
    fun add(x: Double, y: Double): Vector

    /** Adds a vector to this one and stores the result in the object provided.
     * @return a reference to the result, for chaining.
     */
    fun add(x: Double, y: Double, result: Vector): Vector

    /** Adds a scaled vector to this one.
     * @return a new vector containing the result.
     */
    fun addScaled(other: IVector, v: Double): Vector

    /** Adds a scaled vector to this one and stores the result in the supplied vector.
     * @return a reference to the result, for chaining.
     */
    fun addScaled(other: IVector, v: Double, result: Vector): Vector

    /** Subtracts a vector from this one.
     * @return a new vector containing the result.
     */
    fun subtract(other: IVector): Vector

    /** Subtracts a vector from this one and places the result in the supplied object.
     * @return a reference to the result, for chaining.
     */
    fun subtract(other: IVector, result: Vector): Vector

    /** Subtracts a vector from this one.
     * @return a new vector containing the result.
     */
    fun subtract(x: Double, y: Double): Vector

    /** Subtracts a vector from this one and places the result in the supplied object.
     * @return a reference to the result, for chaining.
     */
    fun subtract(x: Double, y: Double, result: Vector): Vector

    /** Rotates this vector by the specified angle.
     * @return a new vector containing the result.
     */
    fun rotate(angle: Double): Vector

    /** Rotates this vector by the specified angle, storing the result in the vector provided.
     * @return a reference to the result vector, for chaining.
     */
    fun rotate(angle: Double, result: Vector): Vector

    /** Rotates this vector by the specified angle and adds another vector to it, placing the
     * result in the object provided.
     * @return a reference to the result, for chaining.
     */
    fun rotateAndAdd(angle: Double, add: IVector, result: Vector): Vector

    /** Rotates this vector by the specified angle, applies a uniform scale, and adds another
     * vector to it, placing the result in the object provided.
     * @return a reference to the result, for chaining.
     */
    fun rotateScaleAndAdd(angle: Double, scale: Double, add: IVector, result: Vector): Vector

    /** Linearly interpolates between this and the specified other vector by the supplied amount.
     * @return a new vector containing the result.
     */
    fun lerp(other: IVector, t: Double): Vector

    /** Linearly interpolates between this and the supplied other vector by the supplied amount,
     * storing the result in the supplied object.
     * @return a reference to the result, for chaining.
     */
    fun lerp(other: IVector, t: Double, result: Vector): Vector

    /** Returns a mutable copy of this vector.  */
    fun clone(): Vector
}
