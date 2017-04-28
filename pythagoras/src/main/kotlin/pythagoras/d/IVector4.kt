//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.d

import java.nio.DoubleBuffer

/**
 * Provides read-only access to a [Vector4].
 */
interface IVector4 {
    /** Returns the x-component of this vector.  */
    fun x(): Double

    /** Returns the y-component of this vector.  */
    fun y(): Double

    /** Returns the z-component of this vector.  */
    fun z(): Double

    /** Returns the w-component of this vector.  */
    fun w(): Double

    /**
     * Populates the supplied buffer with the contents of this vector.

     * @return a reference to the buffer, for chaining.
     */
    operator fun get(buf: DoubleBuffer): DoubleBuffer

    /**
     * Compares this vector to another with the provided epsilon.
     */
    fun epsilonEquals(other: IVector4, epsilon: Double): Boolean

    /**
     * Negates this vector.

     * @return a new vector containing the result.
     */
    fun negate(): Vector4

    /**
     * Negates this vector, storing the result in the supplied object.

     * @return a reference to the result, for chaining.
     */
    fun negate(result: Vector4): Vector4

    /**
     * Absolute-values this vector.

     * @return a new vector containing the result.
     */
    fun abs(): Vector4

    /**
     * Absolute-values this vector, storing the result in the supplied object.

     * @return a reference to the result, for chaining.
     */
    fun abs(result: Vector4): Vector4

    /**
     * Multiplies this vector by a scalar.

     * @return a new vector containing the result.
     */
    fun mult(v: Double): Vector4

    /**
     * Multiplies this vector by a scalar and places the result in the supplied object.

     * @return a reference to the result, for chaining.
     */
    fun mult(v: Double, result: Vector4): Vector4

    /**
     * Multiplies this vector by a matrix (V * M).

     * @return a new vector containing the result.
     */
    fun mult(matrix: IMatrix4): Vector4

    /**
     * Multiplies this vector by a matrix (V * M) and stores the result in the object provided.

     * @return a reference to the result vector, for chaining.
     */
    fun mult(matrix: IMatrix4, result: Vector4): Vector4
}
