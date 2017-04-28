//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.d;

import pythagoras.util.Platform;

import java.io.Serializable;
import java.nio.DoubleBuffer;

/**
 * A plane consisting of a unit normal and a constant. All points on the plane satisfy the equation
 * {@code Ax + By + Cz + D = 0}, where (A, B, C) is the plane normal and D is the constant.
 */
public class Plane implements IPlane, Serializable
{
    private static final long serialVersionUID = 5309702666731799041L;

    /** The X/Y plane. */
    public static final Plane XY_PLANE = new Plane(Vector3.UNIT_Z, 0f);

    /** The X/Z plane. */
    public static final Plane XZ_PLANE = new Plane(Vector3.UNIT_Y, 0f);

    /** The Y/Z plane. */
    public static final Plane YZ_PLANE = new Plane(Vector3.UNIT_X, 0f);

    /** The plane constant. */
    public double constant;

    /**
     * Creates a plane from the specified normal and constant.
     */
    public Plane (IVector3 normal, double constant) {
        set(normal, constant);
    }

    /**
     * Creates a plane with the specified parameters.
     */
    public Plane (double[] values) {
        set(values);
    }

    /**
     * Creates a plane with the specified parameters.
     */
    public Plane (double a, double b, double c, double d) {
        set(a, b, c, d);
    }

    /**
     * Copy constructor.
     */
    public Plane (Plane other) {
        set(other);
    }

    /**
     * Creates an empty (invalid) plane.
     */
    public Plane () {
    }

    /**
     * Copies the parameters of another plane.
     *
     * @return a reference to this plane (for chaining).
     */
    public Plane set (Plane other) {
        return set(other.normal(), other.constant);
    }

    /**
     * Sets the parameters of the plane.
     *
     * @return a reference to this plane (for chaining).
     */
    public Plane set (IVector3 normal, double constant) {
        return set(normal.x(), normal.y(), normal.z(), constant);
    }

    /**
     * Sets the parameters of the plane.
     *
     * @return a reference to this plane (for chaining).
     */
    public Plane set (double[] values) {
        return set(values[0], values[1], values[2], values[3]);
    }

    /**
     * Sets the parameters of the plane.
     *
     * @return a reference to this plane (for chaining).
     */
    public Plane set (double a, double b, double c, double d) {
        _normal.set(a, b, c);
        constant = d;
        return this;
    }

    /**
     * Sets this plane based on the three points provided.
     *
     * @return a reference to the plane (for chaining).
     */
    public Plane fromPoints (IVector3 p1, IVector3 p2, IVector3 p3) {
        // compute the normal by taking the cross product of the two vectors formed
        p2.subtract(p1, _v1);
        p3.subtract(p1, _v2);
        _v1.cross(_v2, _normal).normalizeLocal();

        // use the first point to determine the constant
        constant = -_normal.dot(p1);
        return this;
    }

    /**
     * Sets this plane based on a point on the plane and the plane normal.
     *
     * @return a reference to the plane (for chaining).
     */
    public Plane fromPointNormal (IVector3 pt, IVector3 normal) {
        return set(normal, -normal.dot(pt));
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
     *
     * @return a reference to this plane, for chaining.
     */
    public Plane negateLocal () {
        return negate(this);
    }

    @Override // from IPlane
    public double constant () {
        return constant;
    }

    @Override // from IPlane
    public IVector3 normal () {
        return _normal;
    }

    @Override // from IPlane
    public DoubleBuffer get (DoubleBuffer buf) {
        return buf.put(_normal.x).put(_normal.y).put(_normal.z).put(constant);
    }

    @Override // from IPlane
    public double distance (IVector3 pt) {
        return _normal.dot(pt) + constant;
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

    @Override // from IPlane
    public Plane negate () {
        return negate(new Plane());
    }

    @Override // from IPlane
    public Plane negate (Plane result) {
        _normal.negate(result._normal);
        result.constant = -constant;
        return result;
    }

    @Override // from IPlane
    public boolean intersection (IRay3 ray, Vector3 result) {
        double distance = distance(ray);
        if (Double.isNaN(distance) || distance < 0f) {
            return false;
        } else {
            ray.origin().addScaled(ray.direction(), distance, result);
            return true;
        }
    }

    @Override // from IPlane
    public double distance (IRay3 ray) {
        double dividend = -distance(ray.origin());
        double divisor = _normal.dot(ray.direction());
        if (Math.abs(dividend) < MathUtil.EPSILON) {
            return 0f; // origin is on plane
        } else if (Math.abs(divisor) < MathUtil.EPSILON) {
            return Float.NaN; // ray is parallel to plane
        } else {
            return dividend / divisor;
        }
    }

    @Override
    public int hashCode () {
        return _normal.hashCode() ^ Platform.hashCode(constant);
    }

    @Override
    public boolean equals (Object other) {
        if (!(other instanceof Plane)) {
            return false;
        }
        Plane oplane = (Plane)other;
        return constant == oplane.constant && _normal.equals(oplane.normal());
    }

    /** The plane normal. */
    protected final Vector3 _normal = new Vector3();

    /** Working vectors for computation. */
    protected final Vector3 _v1 = new Vector3(), _v2 = new Vector3();
}
