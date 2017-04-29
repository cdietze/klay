//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

/**
 * Provides read-only access to a [Plane].
 */
interface IPlane {
    /** Returns the plane constant.  */
    val constant: Float

    /** Returns the plane normal.  */
    val normal: IVector3

    /**
     * Computes and returns the signed distance from the plane to the specified point.
     */
    fun distance(pt: IVector3): Float

    // /**
    //  * Transforms this plane by the specified transformation.
    //  *
    //  * @return a new plane containing the result.
    //  */
    // Plane transform (Transform3D transform);

    // /**
    //  * Transforms this plane by the specified transformation, placing the result in the object
    //  * provided.
    //  *
    //  * @return a reference to the result plane, for chaining.
    //  */
    // Plane transform (Transform3D transform, Plane result);

    /**
     * Negates this plane.

     * @return a new plane containing the result.
     */
    fun negate(): Plane

    /**
     * Negates this plane, placing the result in the object provided.

     * @return a reference to the result, for chaining.
     */
    fun negate(result: Plane): Plane

    /**
     * Computes the intersection of the supplied ray with this plane, placing the result
     * in the given vector (if the ray intersects).

     * @return true if the ray intersects the plane (in which case the result will contain
     * * the point of intersection), false if not.
     */
    fun intersection(ray: IRay3, result: Vector3): Boolean

    /**
     * Computes the signed distance to this plane along the specified ray.

     * @return the signed distance, or [Float.NaN] if the ray runs parallel to the plane.
     */
    fun distance(ray: IRay3): Float
}
