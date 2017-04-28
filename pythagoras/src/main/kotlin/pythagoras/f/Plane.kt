//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

import pythagoras.util.Platform

import java.io.Serializable
import java.nio.FloatBuffer

/**
 * A plane consisting of a unit normal and a constant. All points on the plane satisfy the equation
 * `Ax + By + Cz + D = 0`, where (A, B, C) is the plane normal and D is the constant.
 */
class Plane : IPlane, Serializable {

    /** The plane constant.  */
    var constant: Float = 0.toFloat()

    /**
     * Creates a plane from the specified normal and constant.
     */
    constructor(normal: IVector3, constant: Float) {
        set(normal, constant)
    }

    /**
     * Creates a plane with the specified parameters.
     */
    constructor(values: FloatArray) {
        set(values)
    }

    /**
     * Creates a plane with the specified parameters.
     */
    constructor(a: Float, b: Float, c: Float, d: Float) {
        set(a, b, c, d)
    }

    /**
     * Copy constructor.
     */
    constructor(other: Plane) {
        set(other)
    }

    /**
     * Creates an empty (invalid) plane.
     */
    constructor() {}

    /**
     * Copies the parameters of another plane.

     * @return a reference to this plane (for chaining).
     */
    fun set(other: Plane): Plane {
        return set(other.normal(), other.constant)
    }

    /**
     * Sets the parameters of the plane.

     * @return a reference to this plane (for chaining).
     */
    operator fun set(normal: IVector3, constant: Float): Plane {
        return set(normal.x(), normal.y(), normal.z(), constant)
    }

    /**
     * Sets the parameters of the plane.

     * @return a reference to this plane (for chaining).
     */
    fun set(values: FloatArray): Plane {
        return set(values[0], values[1], values[2], values[3])
    }

    /**
     * Sets the parameters of the plane.

     * @return a reference to this plane (for chaining).
     */
    operator fun set(a: Float, b: Float, c: Float, d: Float): Plane {
        _normal.set(a, b, c)
        constant = d
        return this
    }

    /**
     * Sets this plane based on the three points provided.

     * @return a reference to the plane (for chaining).
     */
    fun fromPoints(p1: IVector3, p2: IVector3, p3: IVector3): Plane {
        // compute the normal by taking the cross product of the two vectors formed
        p2.subtract(p1, _v1)
        p3.subtract(p1, _v2)
        _v1.cross(_v2, _normal).normalizeLocal()

        // use the first point to determine the constant
        constant = -_normal.dot(p1)
        return this
    }

    /**
     * Sets this plane based on a point on the plane and the plane normal.

     * @return a reference to the plane (for chaining).
     */
    fun fromPointNormal(pt: IVector3, normal: IVector3): Plane {
        return set(normal, -normal.dot(pt))
    }

    // /**
    //  * Transforms this plane in-place by the specified transformation.
    //  *
    //  * @return a reference to this plane, for chaining.
    //  */
    // public Plane transformLocal (Transform3D transform) {
    //     return transform(transform, this);
    // }

    /**
     * Negates this plane in-place.

     * @return a reference to this plane, for chaining.
     */
    fun negateLocal(): Plane {
        return negate(this)
    }

    override // from IPlane
    fun constant(): Float {
        return constant
    }

    override // from IPlane
    fun normal(): IVector3 {
        return _normal
    }

    override // from IPlane
    fun get(buf: FloatBuffer): FloatBuffer {
        return buf.put(_normal.x).put(_normal.y).put(_normal.z).put(constant)
    }

    override // from IPlane
    fun distance(pt: IVector3): Float {
        return _normal.dot(pt) + constant
    }

    // @Override // from IPlane
    // public Plane transform (Transform3D transform) {
    //     return transform(transform, new Plane());
    // }

    // @Override // from IPlane
    // public Plane transform (Transform3D transform, Plane result) {
    //     transform.transformPointLocal(_normal.mult(-constant, _v1));
    //     transform.transformVector(_normal, _v2).normalizeLocal();
    //     return result.fromPointNormal(_v1, _v2);
    // }

    override // from IPlane
    fun negate(): Plane {
        return negate(Plane())
    }

    override // from IPlane
    fun negate(result: Plane): Plane {
        _normal.negate(result._normal)
        result.constant = -constant
        return result
    }

    override // from IPlane
    fun intersection(ray: IRay3, result: Vector3): Boolean {
        val distance = distance(ray)
        if (java.lang.Float.isNaN(distance) || distance < 0f) {
            return false
        } else {
            ray.origin().addScaled(ray.direction(), distance, result)
            return true
        }
    }

    override // from IPlane
    fun distance(ray: IRay3): Float {
        val dividend = -distance(ray.origin())
        val divisor = _normal.dot(ray.direction())
        if (Math.abs(dividend) < MathUtil.EPSILON) {
            return 0f // origin is on plane
        } else if (Math.abs(divisor) < MathUtil.EPSILON) {
            return java.lang.Float.NaN // ray is parallel to plane
        } else {
            return dividend / divisor
        }
    }

    override fun hashCode(): Int {
        return _normal.hashCode() xor Platform.hashCode(constant)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Plane) {
            return false
        }
        val oplane = other
        return constant == oplane.constant && _normal == oplane.normal()
    }

    /** The plane normal.  */
    protected val _normal = Vector3()

    /** Working vectors for computation.  */
    protected val _v1 = Vector3()
    protected val _v2 = Vector3()

    companion object {
        private const val serialVersionUID = -1683127117567129189L

        /** The X/Y plane.  */
        val XY_PLANE = Plane(Vector3.UNIT_Z, 0f)

        /** The X/Z plane.  */
        val XZ_PLANE = Plane(Vector3.UNIT_Y, 0f)

        /** The Y/Z plane.  */
        val YZ_PLANE = Plane(Vector3.UNIT_X, 0f)
    }
}
