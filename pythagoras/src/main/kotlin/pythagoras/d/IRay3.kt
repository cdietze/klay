//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.d

/**
 * Provides read-only access to a [Ray3].
 */
interface IRay3 {
    /**
     * Returns a reference to the ray's point of origin.
     */
    fun origin(): IVector3

    /**
     * Returns a reference to the ray's unit direction vector.
     */
    fun direction(): IVector3

    // /**
    //  * Transforms this ray.
    //  *
    //  * @return a new ray containing the result.
    //  */
    // Ray3 transform (Transform3D transform);

    // /**
    //  * Transforms this ray, placing the result in the object provided.
    //  *
    //  * @return a reference to the result ray, for chaining.
    //  */
    // Ray3 transform (Transform3D transform, Ray3 result);
}
