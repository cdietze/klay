//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

/**
 * A ray consisting of an origin point and a unit direction vector.
 */
class Ray3 : IRay3 {
    /** The ray's point of origin.  */
    val origin = Vector3()

    /** The ray's unit direction vector.  */
    val direction = Vector3()

    /**
     * Creates a ray with the values contained in the supplied origin point and unit direction
     * vector.
     */
    constructor(origin: Vector3, direction: Vector3) {
        set(origin, direction)
    }

    /**
     * Copy constructor.
     */
    constructor(other: Ray3) {
        set(other)
    }

    /**
     * Creates an empty (invalid) ray.
     */
    constructor() {}

    /**
     * Copies the parameters of another ray.

     * @return a reference to this ray, for chaining.
     */
    fun set(other: Ray3): Ray3 {
        return set(other.origin(), other.direction())
    }

    /**
     * Sets the ray parameters to the values contained in the supplied vectors.

     * @return a reference to this ray, for chaining.
     */
    operator fun set(origin: Vector3, direction: Vector3): Ray3 {
        this.origin.set(origin)
        this.direction.set(direction)
        return this
    }

    // /**
    //  * Transforms this ray in-place.
    //  *
    //  * @return a reference to this ray, for chaining.
    //  */
    // public Ray3 transformLocal (Transform3D transform) {
    //     return transform(transform, this);
    // }

    override // from IRay3
    fun origin(): Vector3 {
        return origin
    }

    override // from IRay3
    fun direction(): Vector3 {
        return direction
    }

    // @Override // from IRay3
    // public Ray3 transform (Transform3D transform) {
    //     return transform(transform, new Ray3());
    // }

    // @Override // from IRay3
    // public Ray3 transform (Transform3D transform, Ray3 result) {
    //     transform.transformPoint(origin, result.origin);
    //     transform.transformVector(direction, result.direction).normalizeLocal();
    //     return result;
    // }

    override fun toString(): String {
        return "[origin=$origin, direction=$direction]"
    }
}
