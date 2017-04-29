//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

/**
 * Provides read-only access to a [Quaternion].
 */
interface IQuaternion {
    /** Returns the x-component of this quaternion.  */
    val x: Float

    /** Returns the y-component of this quaternion.  */
    val y: Float

    /** Returns the z-component of this quaternion.  */
    val z: Float

    /** Returns the w-component of this quaternion.  */
    val w: Float

    /**
     * Populates the supplied array with the contents of this quaternion.
     */
    operator fun get(values: FloatArray)

    /**
     * Checks whether any of the components of this quaternion are not-numbers.
     */
    fun hasNaN(): Boolean

    /**
     * Computes the angles to pass to [Quaternion.fromAngles] to reproduce this rotation,
     * placing them in the provided vector. This uses the factorization method described in David
     * Eberly's [Euler Angle
       * Formulas](http://www.geometrictools.com/Documentation/EulerAngles.pdf).

     * @return a reference to the result vector, for chaining.
     */
    fun toAngles(result: Vector3): Vector3

    /**
     * Computes and returns the angles to pass to [Quaternion.fromAngles] to reproduce this
     * rotation.

     * @return a new vector containing the resulting angles.
     */
    fun toAngles(): Vector3

    /**
     * Normalizes this quaternion.

     * @return a new quaternion containing the result.
     */
    fun normalize(): Quaternion

    /**
     * Normalizes this quaternion, storing the result in the object provided.

     * @return a reference to the result, for chaining.
     */
    fun normalize(result: Quaternion): Quaternion

    /**
     * Inverts this quaternion.

     * @return a new quaternion containing the result.
     */
    fun invert(): Quaternion

    /**
     * Inverts this quaternion, storing the result in the object provided.

     * @return a reference to the result, for chaining.
     */
    fun invert(result: Quaternion): Quaternion

    /**
     * Multiplies this quaternion by another.

     * @return a new quaternion containing the result.
     */
    fun mult(other: IQuaternion): Quaternion

    /**
     * Multiplies this quaternion by another and stores the result in the provided object.

     * @return a reference to the result, for chaining.
     */
    fun mult(other: IQuaternion, result: Quaternion): Quaternion

    /**
     * Interpolates between this and the specified other quaternion.

     * @return a new quaternion containing the result.
     */
    fun slerp(other: IQuaternion, t: Float): Quaternion

    /**
     * Interpolates between this and the specified other quaternion, placing the result in the
     * object provided. Based on the code in Nick Bobick's article,
     * [Rotating Objects
       * Using Quaternions](http://www.gamasutra.com/features/19980703/quaternions_01.htm).

     * @return a reference to the result quaternion, for chaining.
     */
    fun slerp(other: IQuaternion, t: Float, result: Quaternion): Quaternion

    /**
     * Transforms a vector by this quaternion.

     * @return a new vector containing the result.
     */
    fun transform(vector: IVector3): Vector3

    /**
     * Transforms a vector by this quaternion and places the result in the provided object.

     * @return a reference to the result, for chaining.
     */
    fun transform(vector: IVector3, result: Vector3): Vector3

    /**
     * Transforms the unit x vector by this quaternion, placing the result in the provided object.

     * @return a reference to the result, for chaining.
     */
    fun transformUnitX(result: Vector3): Vector3

    /**
     * Transforms the unit y vector by this quaternion, placing the result in the provided object.

     * @return a reference to the result, for chaining.
     */
    fun transformUnitY(result: Vector3): Vector3

    /**
     * Transforms the unit z vector by this quaternion, placing the result in the provided object.

     * @return a reference to the result, for chaining.
     */
    fun transformUnitZ(result: Vector3): Vector3

    /**
     * Transforms a vector by this quaternion and adds another vector to it, placing the result
     * in the object provided.

     * @return a reference to the result, for chaining.
     */
    fun transformAndAdd(vector: IVector3, add: IVector3, result: Vector3): Vector3

    /**
     * Transforms a vector by this quaternion, applies a uniform scale, and adds another vector to
     * it, placing the result in the object provided.

     * @return a reference to the result, for chaining.
     */
    fun transformScaleAndAdd(vector: IVector3, scale: Float, add: IVector3, result: Vector3): Vector3

    /**
     * Transforms a vector by this quaternion and returns the z coordinate of the result.
     */
    fun transformZ(vector: IVector3): Float

    /**
     * Returns the amount of rotation about the z axis (for the purpose of flattening the
     * rotation).
     */
    val rotationZ: Float

    /**
     * Integrates the provided angular velocity over the specified timestep.

     * @return a new quaternion containing the result.
     */
    fun integrate(velocity: IVector3, t: Float): Quaternion

    /**
     * Integrates the provided angular velocity over the specified timestep, storing the result in
     * the object provided.

     * @return a reference to the result object, for chaining.
     */
    fun integrate(velocity: IVector3, t: Float, result: Quaternion): Quaternion
}
