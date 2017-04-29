//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

/**
 * Provides read-only access to a [Vector3].
 */
interface IVector3 {
    /** Returns the x-component of this vector.  */
    val x: Float

    /** Returns the y-component of this vector.  */
    val y: Float

    /** Returns the z-component of this vector.  */
    val z: Float

    /**
     * Computes and returns the dot product of this and the specified other vector.
     */
    fun dot(other: IVector3): Float

    /**
     * Computes the cross product of this and the specified other vector.

     * @return a new vector containing the result.
     */
    fun cross(other: IVector3): Vector3

    /**
     * Computes the cross product of this and the specified other vector, placing the result
     * in the object supplied.

     * @return a reference to the result, for chaining.
     */
    fun cross(other: IVector3, result: Vector3): Vector3

    /**
     * Computes the triple product of this and the specified other vectors, which is equal to
     * `this.dot(b.cross(c))`.
     */
    fun triple(b: IVector3, c: IVector3): Float

    /**
     * Negates this vector.

     * @return a new vector containing the result.
     */
    fun negate(): Vector3

    /**
     * Negates this vector, storing the result in the supplied object.

     * @return a reference to the result, for chaining.
     */
    fun negate(result: Vector3): Vector3

    /**
     * Absolute-values this vector.

     * @return a new vector containing the result.
     */
    fun abs(): Vector3

    /**
     * Absolute-values this vector, storing the result in the supplied object.

     * @return a reference to the result, for chaining.
     */
    fun abs(result: Vector3): Vector3

    /**
     * Normalizes this vector.

     * @return a new vector containing the result.
     */
    fun normalize(): Vector3

    /**
     * Normalizes this vector, storing the result in the object supplied.

     * @return a reference to the result, for chaining.
     */
    fun normalize(result: Vector3): Vector3

    /**
     * Returns the angle between this vector and the specified other vector.
     */
    fun angle(other: IVector3): Float

    /**
     * Returns the length of this vector.
     */
    fun length(): Float

    /**
     * Returns the squared length of this vector.
     */
    fun lengthSquared(): Float

    /**
     * Returns the distance from this vector to the specified other vector.
     */
    fun distance(other: IVector3): Float

    /**
     * Returns the squared distance from this vector to the specified other.
     */
    fun distanceSquared(other: IVector3): Float

    /**
     * Returns the Manhattan distance between this vector and the specified other.
     */
    fun manhattanDistance(other: IVector3): Float

    /**
     * Multiplies this vector by a scalar.

     * @return a new vector containing the result.
     */
    fun mult(v: Float): Vector3

    /**
     * Multiplies this vector by a scalar and places the result in the supplied object.

     * @return a reference to the result, for chaining.
     */
    fun mult(v: Float, result: Vector3): Vector3

    /**
     * Multiplies this vector by another.

     * @return a new vector containing the result.
     */
    fun mult(other: IVector3): Vector3

    /**
     * Multiplies this vector by another, storing the result in the object provided.

     * @return a reference to the result vector, for chaining.
     */
    fun mult(other: IVector3, result: Vector3): Vector3

    /**
     * Adds a vector to this one.

     * @return a new vector containing the result.
     */
    fun add(other: IVector3): Vector3

    /**
     * Adds a vector to this one, storing the result in the object provided.

     * @return a reference to the result, for chaining.
     */
    fun add(other: IVector3, result: Vector3): Vector3

    /**
     * Subtracts a vector from this one.

     * @return a new vector containing the result.
     */
    fun subtract(other: IVector3): Vector3

    /**
     * Subtracts a vector from this one and places the result in the supplied object.

     * @return a reference to the result, for chaining.
     */
    fun subtract(other: IVector3, result: Vector3): Vector3

    /**
     * Adds a vector to this one.

     * @return a new vector containing the result.
     */
    fun add(x: Float, y: Float, z: Float): Vector3

    /**
     * Adds a vector to this one and stores the result in the object provided.

     * @return a reference to the result, for chaining.
     */
    fun add(x: Float, y: Float, z: Float, result: Vector3): Vector3

    /**
     * Adds a scaled vector to this one.

     * @return a new vector containing the result.
     */
    fun addScaled(other: IVector3, v: Float): Vector3

    /**
     * Adds a scaled vector to this one and stores the result in the supplied vector.

     * @return a reference to the result, for chaining.
     */
    fun addScaled(other: IVector3, v: Float, result: Vector3): Vector3

    /**
     * Linearly interpolates between this and the specified other vector by the supplied amount.

     * @return a new vector containing the result.
     */
    fun lerp(other: IVector3, t: Float): Vector3

    /**
     * Linearly interpolates between this and the supplied other vector by the supplied amount,
     * storing the result in the supplied object.

     * @return a reference to the result, for chaining.
     */
    fun lerp(other: IVector3, t: Float, result: Vector3): Vector3

    /**
     * Returns the element at the idx'th position of the vector.
     */
    operator fun get(idx: Int): Float

    /**
     * Populates the supplied array with the contents of this vector.
     */
    operator fun get(values: FloatArray)
}
