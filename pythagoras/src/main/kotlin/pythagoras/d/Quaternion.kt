//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.d

import pythagoras.util.Platform

import java.io.Serializable
import java.util.Random

/**
 * A unit quaternion. Many of the formulas come from the
 * [Matrix and Quaternion FAQ](http://www.j3d.org/matrix_faq/matrfaq_latest.html).
 */
class Quaternion : IQuaternion, Serializable {

    /** The components of the quaternion.  */
    var x: Double = 0.toDouble()
    var y: Double = 0.toDouble()
    var z: Double = 0.toDouble()
    var w: Double = 0.toDouble()

    /**
     * Creates a quaternion from four components.
     */
    constructor(x: Double, y: Double, z: Double, w: Double) {
        set(x, y, z, w)
    }

    /**
     * Creates a quaternion from an array of values.
     */
    constructor(values: DoubleArray) {
        set(values)
    }

    /**
     * Copy constructor.
     */
    constructor(other: IQuaternion) {
        set(other)
    }

    /**
     * Creates an identity quaternion.
     */
    constructor() {
        set(0.0, 0.0, 0.0, 1.0)
    }

    /**
     * Copies the elements of another quaternion.

     * @return a reference to this quaternion, for chaining.
     */
    fun set(other: IQuaternion): Quaternion {
        return set(other.x(), other.y(), other.z(), other.w())
    }

    /**
     * Copies the elements of an array.

     * @return a reference to this quaternion, for chaining.
     */
    fun set(values: DoubleArray): Quaternion {
        return set(values[0], values[1], values[2], values[3])
    }

    /**
     * Sets all of the elements of the quaternion.

     * @return a reference to this quaternion, for chaining.
     */
    operator fun set(x: Double, y: Double, z: Double, w: Double): Quaternion {
        this.x = x
        this.y = y
        this.z = z
        this.w = w
        return this
    }

    /**
     * Sets this quaternion to the rotation of the first normalized vector onto the second.

     * @return a reference to this quaternion, for chaining.
     */
    fun fromVectors(from: IVector3, to: IVector3): Quaternion {
        val angle = from.angle(to)
        if (angle < MathUtil.EPSILON) {
            return set(IDENTITY)
        }
        if (angle <= Math.PI - MathUtil.EPSILON) {
            return fromAngleAxis(angle, from.cross(to).normalizeLocal())
        }
        // it's a 180 degree rotation; any axis orthogonal to the from vector will do
        val axis = Vector3(0.0, from.z(), -from.y())
        val length = axis.length()
        return fromAngleAxis(Math.PI, if (length < MathUtil.EPSILON)
            axis.set(-from.z(), 0.0, from.x()).normalizeLocal()
        else
            axis.multLocal(1f / length))
    }

    /**
     * Sets this quaternion to the rotation of (0, 0, -1) onto the supplied normalized vector.

     * @return a reference to the quaternion, for chaining.
     */
    fun fromVectorFromNegativeZ(to: IVector3): Quaternion {
        return fromVectorFromNegativeZ(to.x(), to.y(), to.z())
    }

    /**
     * Sets this quaternion to the rotation of (0, 0, -1) onto the supplied normalized vector.

     * @return a reference to the quaternion, for chaining.
     */
    fun fromVectorFromNegativeZ(tx: Double, ty: Double, tz: Double): Quaternion {
        val angle = Math.acos(-tz)
        if (angle < MathUtil.EPSILON) {
            return set(IDENTITY)
        }
        if (angle > Math.PI - MathUtil.EPSILON) {
            return set(0.0, 1.0, 0.0, 0.0) // 180 degrees about y
        }
        val len = Math.hypot(tx, ty)
        return fromAngleAxis(angle, ty / len, -tx / len, 0.0)
    }

    /**
     * Sets this quaternion to one that rotates onto the given unit axes.

     * @return a reference to this quaternion, for chaining.
     */
    fun fromAxes(nx: IVector3, ny: IVector3, nz: IVector3): Quaternion {
        val nxx = nx.x()
        val nyy = ny.y()
        val nzz = nz.z()
        val x2 = (1f + nxx - nyy - nzz) / 4f
        val y2 = (1f - nxx + nyy - nzz) / 4f
        val z2 = (1.0 - nxx - nyy + nzz) / 4f
        val w2 = 1.0 - x2 - y2 - z2
        return set(Math.sqrt(x2) * if (ny.z() >= nz.y()) +1f else -1f,
                Math.sqrt(y2) * if (nz.x() >= nx.z()) +1f else -1f,
                Math.sqrt(z2) * if (nx.y() >= ny.x()) +1f else -1f,
                Math.sqrt(w2))
    }

    /**
     * Sets this quaternion to the rotation described by the given angle and normalized
     * axis.

     * @return a reference to this quaternion, for chaining.
     */
    fun fromAngleAxis(angle: Double, axis: IVector3): Quaternion {
        return fromAngleAxis(angle, axis.x(), axis.y(), axis.z())
    }

    /**
     * Sets this quaternion to the rotation described by the given angle and normalized
     * axis.

     * @return a reference to this quaternion, for chaining.
     */
    fun fromAngleAxis(angle: Double, x: Double, y: Double, z: Double): Quaternion {
        val sina = Math.sin(angle / 2f)
        return set(x * sina, y * sina, z * sina, Math.cos(angle / 2f))
    }

    /**
     * Sets this to a random rotation obtained from a completely uniform distribution.
     */
    fun randomize(rand: Random): Quaternion {
        // pick angles according to the surface area distribution
        return fromAngles(MathUtil.lerp(-Math.PI, +Math.PI, rand.nextFloat().toDouble()),
                Math.asin(MathUtil.lerp(-1.0, +1.0, rand.nextFloat().toDouble())),
                MathUtil.lerp(-Math.PI, +Math.PI, rand.nextFloat().toDouble()))
    }

    /**
     * Sets this quaternion to one that first rotates about x by the specified number of radians,
     * then rotates about z by the specified number of radians.
     */
    fun fromAnglesXZ(x: Double, z: Double): Quaternion {
        val hx = x * 0.5f
        val hz = z * 0.5f
        val sx = Math.sin(hx)
        val cx = Math.cos(hx)
        val sz = Math.sin(hz)
        val cz = Math.cos(hz)
        return set(cz * sx, sz * sx, sz * cx, cz * cx)
    }

    /**
     * Sets this quaternion to one that first rotates about x by the specified number of radians,
     * then rotates about y by the specified number of radians.
     */
    fun fromAnglesXY(x: Double, y: Double): Quaternion {
        val hx = x * 0.5f
        val hy = y * 0.5f
        val sx = Math.sin(hx)
        val cx = Math.cos(hx)
        val sy = Math.sin(hy)
        val cy = Math.cos(hy)
        return set(cy * sx, sy * cx, -sy * sx, cy * cx)
    }

    /**
     * Sets this quaternion to one that first rotates about x by the specified number of radians,
     * then rotates about y, then about z.
     */
    fun fromAngles(angles: Vector3): Quaternion {
        return fromAngles(angles.x, angles.y, angles.z)
    }

    /**
     * Sets this quaternion to one that first rotates about x by the specified number of radians,
     * then rotates about y, then about z.
     */
    fun fromAngles(x: Double, y: Double, z: Double): Quaternion {
        // TODO: it may be more convenient to define the angles in the opposite order (first z,
        // then y, then x)
        val hx = x * 0.5f
        val hy = y * 0.5f
        val hz = z * 0.5f
        val sz = Math.sin(hz)
        val cz = Math.cos(hz)
        val sy = Math.sin(hy)
        val cy = Math.cos(hy)
        val sx = Math.sin(hx)
        val cx = Math.cos(hx)
        val szsy = sz * sy
        val czsy = cz * sy
        val szcy = sz * cy
        val czcy = cz * cy
        return set(
                czcy * sx - szsy * cx,
                czsy * cx + szcy * sx,
                szcy * cx - czsy * sx,
                czcy * cx + szsy * sx)
    }

    /**
     * Normalizes this quaternion in-place.

     * @return a reference to this quaternion, for chaining.
     */
    fun normalizeLocal(): Quaternion {
        return normalize(this)
    }

    /**
     * Inverts this quaternion in-place.

     * @return a reference to this quaternion, for chaining.
     */
    fun invertLocal(): Quaternion {
        return invert(this)
    }

    /**
     * Multiplies this quaternion in-place by another.

     * @return a reference to this quaternion, for chaining.
     */
    fun multLocal(other: IQuaternion): Quaternion {
        return mult(other, this)
    }

    /**
     * Interpolates in-place between this and the specified other quaternion.

     * @return a reference to this quaternion, for chaining.
     */
    fun slerpLocal(other: IQuaternion, t: Double): Quaternion {
        return slerp(other, t, this)
    }

    /**
     * Transforms a vector in-place by this quaternion.

     * @return a reference to the vector, for chaining.
     */
    fun transformLocal(vector: Vector3): Vector3 {
        return transform(vector, vector)
    }

    /**
     * Integrates in-place the provided angular velocity over the specified timestep.

     * @return a reference to this quaternion, for chaining.
     */
    fun integrateLocal(velocity: IVector3, t: Double): Quaternion {
        return integrate(velocity, t, this)
    }

    override // from IQuaternion
    fun x(): Double {
        return x
    }

    override // from IQuaternion
    fun y(): Double {
        return y
    }

    override // from IQuaternion
    fun z(): Double {
        return z
    }

    override // from IQuaternion
    fun w(): Double {
        return w
    }

    override // from IQuaternion
    fun get(values: DoubleArray) {
        values[0] = x
        values[1] = y
        values[2] = z
        values[3] = w
    }

    override // from IQuaternion
    fun hasNaN(): Boolean {
        return java.lang.Double.isNaN(x) || java.lang.Double.isNaN(y) || java.lang.Double.isNaN(z) || java.lang.Double.isNaN(w)
    }

    override // from IQuaternion
    fun toAngles(result: Vector3): Vector3 {
        val sy = 2f * (y * w - x * z)
        if (sy < 1f - MathUtil.EPSILON) {
            if (sy > -1 + MathUtil.EPSILON) {
                return result.set(Math.atan2(y * z + x * w, 0.5f - (x * x + y * y)),
                        Math.asin(sy),
                        Math.atan2(x * y + z * w, 0.5f - (y * y + z * z)))
            } else {
                // not a unique solution; x + z = atan2(-m21, m11)
                return result.set(0.0,
                        -MathUtil.HALF_PI,
                        Math.atan2(x * w - y * z, 0.5f - (x * x + z * z)))
            }
        } else {
            // not a unique solution; x - z = atan2(-m21, m11)
            return result.set(0.0,
                    MathUtil.HALF_PI,
                    -Math.atan2(x * w - y * z, 0.5f - (x * x + z * z)))
        }
    }

    override // from IQuaternion
    fun toAngles(): Vector3 {
        return toAngles(Vector3())
    }

    override // from IQuaternion
    fun normalize(): Quaternion {
        return normalize(Quaternion())
    }

    override // from IQuaternion
    fun normalize(result: Quaternion): Quaternion {
        val rlen = 1f / Math.sqrt(x * x + y * y + z * z + w * w)
        return result.set(x * rlen, y * rlen, z * rlen, w * rlen)
    }

    override // from IQuaternion
    fun invert(): Quaternion {
        return invert(Quaternion())
    }

    override // from IQuaternion
    fun invert(result: Quaternion): Quaternion {
        return result.set(-x, -y, -z, w)
    }

    override // from IQuaternion
    fun mult(other: IQuaternion): Quaternion {
        return mult(other, Quaternion())
    }

    override // from IQuaternion
    fun mult(other: IQuaternion, result: Quaternion): Quaternion {
        val ox = other.x()
        val oy = other.y()
        val oz = other.z()
        val ow = other.w()
        return result.set(w * ox + x * ow + y * oz - z * oy,
                w * oy + y * ow + z * ox - x * oz,
                w * oz + z * ow + x * oy - y * ox,
                w * ow - x * ox - y * oy - z * oz)
    }

    override // from IQuaternion
    fun slerp(other: IQuaternion, t: Double): Quaternion {
        return slerp(other, t, Quaternion())
    }

    override // from IQuaternion
    fun slerp(other: IQuaternion, t: Double, result: Quaternion): Quaternion {
        var ox = other.x()
        var oy = other.y()
        var oz = other.z()
        var ow = other.w()
        var cosa = x * ox + y * oy + z * oz + w * ow
        val s0: Double
        val s1: Double

        // adjust signs if necessary
        if (cosa < 0f) {
            cosa = -cosa
            ox = -ox
            oy = -oy
            oz = -oz
            ow = -ow
        }

        // calculate coefficients; if the angle is too close to zero, we must fall back
        // to linear interpolation
        if (1f - cosa > MathUtil.EPSILON) {
            val angle = Math.acos(cosa)
            val sina = Math.sin(angle)
            s0 = Math.sin((1f - t) * angle) / sina
            s1 = Math.sin(t * angle) / sina
        } else {
            s0 = 1f - t
            s1 = t
        }

        return result.set(s0 * x + s1 * ox, s0 * y + s1 * oy, s0 * z + s1 * oz, s0 * w + s1 * ow)
    }

    override // from IQuaternion
    fun transform(vector: IVector3): Vector3 {
        return transform(vector, Vector3())
    }

    override // from IQuaternion
    fun transform(vector: IVector3, result: Vector3): Vector3 {
        val xx = x * x
        val yy = y * y
        val zz = z * z
        val xy = x * y
        val xz = x * z
        val xw = x * w
        val yz = y * z
        val yw = y * w
        val zw = z * w
        val vx = vector.x()
        val vy = vector.y()
        val vz = vector.z()
        val vx2 = vx * 2f
        val vy2 = vy * 2f
        val vz2 = vz * 2f
        return result.set(vx + vy2 * (xy - zw) + vz2 * (xz + yw) - vx2 * (yy + zz),
                vy + vx2 * (xy + zw) + vz2 * (yz - xw) - vy2 * (xx + zz),
                vz + vx2 * (xz - yw) + vy2 * (yz + xw) - vz2 * (xx + yy))
    }

    override // from IQuaternion
    fun transformUnitX(result: Vector3): Vector3 {
        return result.set(1f - 2f * (y * y + z * z), 2f * (x * y + z * w), 2f * (x * z - y * w))
    }

    override // from IQuaternion
    fun transformUnitY(result: Vector3): Vector3 {
        return result.set(2f * (x * y - z * w), 1f - 2f * (x * x + z * z), 2f * (y * z + x * w))
    }

    override // from IQuaternion
    fun transformUnitZ(result: Vector3): Vector3 {
        return result.set(2f * (x * z + y * w), 2f * (y * z - x * w), 1f - 2f * (x * x + y * y))
    }

    override // from IQuaternion
    fun transformAndAdd(vector: IVector3, add: IVector3, result: Vector3): Vector3 {
        val xx = x * x
        val yy = y * y
        val zz = z * z
        val xy = x * y
        val xz = x * z
        val xw = x * w
        val yz = y * z
        val yw = y * w
        val zw = z * w
        val vx = vector.x()
        val vy = vector.y()
        val vz = vector.z()
        val vx2 = vx * 2f
        val vy2 = vy * 2f
        val vz2 = vz * 2f
        return result.set(vx + vy2 * (xy - zw) + vz2 * (xz + yw) - vx2 * (yy + zz) + add.x(),
                vy + vx2 * (xy + zw) + vz2 * (yz - xw) - vy2 * (xx + zz) + add.y(),
                vz + vx2 * (xz - yw) + vy2 * (yz + xw) - vz2 * (xx + yy) + add.z())
    }

    override // from IQuaternion
    fun transformScaleAndAdd(vector: IVector3, scale: Double, add: IVector3,
                             result: Vector3): Vector3 {
        val xx = x * x
        val yy = y * y
        val zz = z * z
        val xy = x * y
        val xz = x * z
        val xw = x * w
        val yz = y * z
        val yw = y * w
        val zw = z * w
        val vx = vector.x()
        val vy = vector.y()
        val vz = vector.z()
        val vx2 = vx * 2f
        val vy2 = vy * 2f
        val vz2 = vz * 2f
        return result.set(
                (vx + vy2 * (xy - zw) + vz2 * (xz + yw) - vx2 * (yy + zz)) * scale + add.x(),
                (vy + vx2 * (xy + zw) + vz2 * (yz - xw) - vy2 * (xx + zz)) * scale + add.y(),
                (vz + vx2 * (xz - yw) + vy2 * (yz + xw) - vz2 * (xx + yy)) * scale + add.z())
    }

    override // from IQuaternion
    fun transformZ(vector: IVector3): Double {
        return vector.z() + vector.x() * 2.0 * (x * z - y * w) +
                vector.y() * 2.0 * (y * z + x * w) - vector.z() * 2.0 * (x * x + y * y)
    }

    override // from IQuaternion
    val rotationZ: Double
        get() = Math.atan2(2f * (x * y + z * w), 1f - 2f * (y * y + z * z))

    override // from IQuaternion
    fun integrate(velocity: IVector3, t: Double): Quaternion {
        return integrate(velocity, t, Quaternion())
    }

    override // from IQuaternion
    fun integrate(velocity: IVector3, t: Double, result: Quaternion): Quaternion {
        // TODO: use Runge-Kutta integration?
        val qx = 0.5f * velocity.x()
        val qy = 0.5f * velocity.y()
        val qz = 0.5f * velocity.z()
        return result.set(x + t * (qx * w + qy * z - qz * y),
                y + t * (qy * w + qz * x - qx * z),
                z + t * (qz * w + qx * y - qy * x),
                w + t * (-qx * x - qy * y - qz * z)).normalizeLocal()
    }

    override // documentation inherited
    fun toString(): String {
        return "[$x, $y, $z, $w]"
    }

    override // documentation inherited
    fun hashCode(): Int {
        return Platform.hashCode(x) xor Platform.hashCode(y) xor Platform.hashCode(z) xor
                Platform.hashCode(w)
    }

    override // documentation inherited
    fun equals(other: Any?): Boolean {
        if (other !is Quaternion) {
            return false
        }
        val oquat = other
        return x == oquat.x && y == oquat.y && z == oquat.z && w == oquat.w || x == -oquat.x && y == -oquat.y && z == -oquat.z && w == -oquat.x
    }

    companion object {
        private const val serialVersionUID = -2507768410601557773L

        /** The identity quaternion.  */
        val IDENTITY: IQuaternion = Quaternion(0.0, 0.0, 0.0, 1.0)
    }
}
