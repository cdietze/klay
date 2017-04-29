//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

import pythagoras.util.Platform
import pythagoras.util.SingularMatrixException
import java.lang.Math

/**
 * A 4x4 column-major matrix.
 */
class Matrix4 : IMatrix4 {

    /** The values of the matrix. The names take the form `mCOLROW`.  */
    override var m00: Float = 0.toFloat()
    override var m10: Float = 0.toFloat()
    override var m20: Float = 0.toFloat()
    override var m30: Float = 0.toFloat()
    override var m01: Float = 0.toFloat()
    override var m11: Float = 0.toFloat()
    override var m21: Float = 0.toFloat()
    override var m31: Float = 0.toFloat()
    override var m02: Float = 0.toFloat()
    override var m12: Float = 0.toFloat()
    override var m22: Float = 0.toFloat()
    override var m32: Float = 0.toFloat()
    override var m03: Float = 0.toFloat()
    override var m13: Float = 0.toFloat()
    override var m23: Float = 0.toFloat()
    override var m33: Float = 0.toFloat()

    /**
     * Creates a matrix from its components.
     */
    constructor(
            m00: Float, m10: Float, m20: Float, m30: Float,
            m01: Float, m11: Float, m21: Float, m31: Float,
            m02: Float, m12: Float, m22: Float, m32: Float,
            m03: Float, m13: Float, m23: Float, m33: Float) {
        set(m00, m10, m20, m30,
                m01, m11, m21, m31,
                m02, m12, m22, m32,
                m03, m13, m23, m33)
    }

    /**
     * Creates a matrix from an array of values.
     */
    constructor(values: FloatArray) {
        set(values)
    }

    /**
     * Copy constructor.
     */
    constructor(other: IMatrix4) {
        set(other)
    }

    /**
     * Creates an identity matrix.
     */
    constructor() {
        setToIdentity()
    }

    /**
     * Sets this matrix to the identity matrix.

     * @return a reference to this matrix, for chaining.
     */
    fun setToIdentity(): Matrix4 {
        return set(1f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f,
                0f, 0f, 1f, 0f,
                0f, 0f, 0f, 1f)
    }

    /**
     * Sets this matrix to all zeroes.

     * @return a reference to this matrix, for chaining.
     */
    fun setToZero(): Matrix4 {
        return set(0f, 0f, 0f, 0f,
                0f, 0f, 0f, 0f,
                0f, 0f, 0f, 0f,
                0f, 0f, 0f, 0f)
    }

    /**
     * Sets this to a matrix that first rotates, then translates.

     * @return a reference to this matrix, for chaining.
     */
    fun setToTransform(translation: IVector3, rotation: IQuaternion): Matrix4 {
        return setToRotation(rotation).setTranslation(translation)
    }

    /**
     * Sets this to a matrix that first scales, then rotates, then translates.

     * @return a reference to this matrix, for chaining.
     */
    fun setToTransform(translation: IVector3, rotation: IQuaternion, scale: Float): Matrix4 {
        return setToRotation(rotation).set(
                m00 * scale, m10 * scale, m20 * scale, translation.x,
                m01 * scale, m11 * scale, m21 * scale, translation.y,
                m02 * scale, m12 * scale, m22 * scale, translation.z,
                0f, 0f, 0f, 1f)
    }

    /**
     * Sets this to a matrix that first scales, then rotates, then translates.

     * @return a reference to this matrix, for chaining.
     */
    fun setToTransform(translation: IVector3, rotation: IQuaternion, scale: IVector3): Matrix4 {
        val sx = scale.x
        val sy = scale.y
        val sz = scale.z
        return setToRotation(rotation).set(
                m00 * sx, m10 * sy, m20 * sz, translation.x,
                m01 * sx, m11 * sy, m21 * sz, translation.y,
                m02 * sx, m12 * sy, m22 * sz, translation.z,
                0f, 0f, 0f, 1f)
    }

    /**
     * Sets this to a translation matrix.

     * @return a reference to this matrix, for chaining.
     */
    fun setToTranslation(translation: IVector3): Matrix4 {
        return setToTranslation(translation.x, translation.y, translation.z)
    }

    /**
     * Sets this to a translation matrix.

     * @return a reference to this matrix, for chaining.
     */
    fun setToTranslation(x: Float, y: Float, z: Float): Matrix4 {
        return set(1f, 0f, 0f, x,
                0f, 1f, 0f, y,
                0f, 0f, 1f, z,
                0f, 0f, 0f, 1f)
    }

    /**
     * Sets the translation component of this matrix.

     * @return a reference to this matrix, for chaining.
     */
    fun setTranslation(translation: IVector3): Matrix4 {
        return setTranslation(translation.x, translation.y, translation.z)
    }

    /**
     * Sets the translation component of this matrix.

     * @return a reference to this matrix, for chaining.
     */
    fun setTranslation(x: Float, y: Float, z: Float): Matrix4 {
        m30 = x
        m31 = y
        m32 = z
        return this
    }

    /**
     * Sets this to a rotation matrix that rotates one vector onto another.

     * @return a reference to this matrix, for chaining.
     */
    fun setToRotation(from: IVector3, to: IVector3): Matrix4 {
        val angle = from.angle(to)
        if (angle < MathUtil.EPSILON) {
            return setToIdentity()
        }
        if (angle <= FloatMath.PI - MathUtil.EPSILON) {
            return setToRotation(angle, from.cross(to).normalizeLocal())
        }
        // it's a 180 degree rotation; any axis orthogonal to the from vector will do
        val axis = Vector3(0f, from.z, -from.y)
        val length = axis.length()
        return setToRotation(FloatMath.PI, if (length < MathUtil.EPSILON)
            axis.set(-from.z, 0f, from.x).normalizeLocal()
        else
            axis.multLocal(1f / length))
    }

    /**
     * Sets this to a rotation matrix.

     * @return a reference to this matrix, for chaining.
     */
    fun setToRotation(angle: Float, axis: IVector3): Matrix4 {
        return setToRotation(angle, axis.x, axis.y, axis.z)
    }

    /**
     * Sets this to a rotation matrix. The formula comes from the OpenGL documentation for the
     * glRotatef function.

     * @return a reference to this matrix, for chaining.
     */
    fun setToRotation(angle: Float, x: Float, y: Float, z: Float): Matrix4 {
        val c = FloatMath.cos(angle)
        val s = FloatMath.sin(angle)
        val omc = 1f - c
        val xs = x * s
        val ys = y * s
        val zs = z * s
        val xy = x * y
        val xz = x * z
        val yz = y * z
        return set(x * x * omc + c, xy * omc - zs, xz * omc + ys, 0f,
                xy * omc + zs, y * y * omc + c, yz * omc - xs, 0f,
                xz * omc - ys, yz * omc + xs, z * z * omc + c, 0f,
                0f, 0f, 0f, 1f)
    }

    /**
     * Sets this to a rotation matrix. The formula comes from the
     * [Matrix and Quaternion FAQ](http://www.j3d.org/matrix_faq/matrfaq_latest.html).

     * @return a reference to this matrix, for chaining.
     */
    fun setToRotation(quat: IQuaternion): Matrix4 {
        val x = quat.x
        val y = quat.y
        val z = quat.z
        val w = quat.w
        val xx = x * x
        val yy = y * y
        val zz = z * z
        val xy = x * y
        val xz = x * z
        val xw = x * w
        val yz = y * z
        val yw = y * w
        val zw = z * w
        return set(1f - 2f * (yy + zz), 2f * (xy - zw), 2f * (xz + yw), 0f,
                2f * (xy + zw), 1f - 2f * (xx + zz), 2f * (yz - xw), 0f,
                2f * (xz - yw), 2f * (yz + xw), 1f - 2f * (xx + yy), 0f,
                0f, 0f, 0f, 1f)
    }

    /**
     * Sets this to a rotation plus scale matrix.

     * @return a reference to this matrix, for chaining.
     */
    fun setToRotationScale(rotScale: IMatrix3): Matrix4 {
        return set(rotScale.m00, rotScale.m01, rotScale.m02, 0f,
                rotScale.m10, rotScale.m11, rotScale.m12, 0f,
                rotScale.m20, rotScale.m21, rotScale.m22, 0f,
                0f, 0f, 0f, 1f)
    }

    /**
     * Sets this to a scale matrix.

     * @return a reference to this matrix, for chaining.
     */
    fun setToScale(scale: IVector3): Matrix4 {
        return setToScale(scale.x, scale.y, scale.z)
    }

    /**
     * Sets this to a uniform scale matrix.

     * @return a reference to this matrix, for chaining.
     */
    fun setToScale(s: Float): Matrix4 {
        return setToScale(s, s, s)
    }

    /**
     * Sets this to a scale matrix.

     * @return a reference to this matrix, for chaining.
     */
    fun setToScale(x: Float, y: Float, z: Float): Matrix4 {
        return set(x, 0f, 0f, 0f,
                0f, y, 0f, 0f,
                0f, 0f, z, 0f,
                0f, 0f, 0f, 1f)
    }

    /**
     * Sets this to a reflection across a plane intersecting the origin with the supplied normal.

     * @return a reference to this matrix, for chaining.
     */
    fun setToReflection(normal: IVector3): Matrix4 {
        return setToReflection(normal.x, normal.y, normal.z)
    }

    /**
     * Sets this to a reflection across a plane intersecting the origin with the supplied normal.

     * @return a reference to this matrix, for chaining.
     */
    fun setToReflection(x: Float, y: Float, z: Float): Matrix4 {
        val x2 = -2f * x
        val y2 = -2f * y
        val z2 = -2f * z
        val xy2 = x2 * y
        val xz2 = x2 * z
        val yz2 = y2 * z
        return set(1f + x2 * x, xy2, xz2, 0f,
                xy2, 1f + y2 * y, yz2, 0f,
                xz2, yz2, 1f + z2 * z, 0f,
                0f, 0f, 0f, 1f)
    }

    /**
     * Sets this to a reflection across the specified plane.

     * @return a reference to this matrix, for chaining.
     */
    fun setToReflection(plane: IPlane): Matrix4 {
        return setToReflection(plane.normal, plane.constant)
    }

    /**
     * Sets this to a reflection across the specified plane.

     * @return a reference to this matrix, for chaining.
     */
    fun setToReflection(normal: IVector3, constant: Float): Matrix4 {
        return setToReflection(normal.x, normal.y, normal.z, constant)
    }

    /**
     * Sets this to a reflection across the specified plane.

     * @return a reference to this matrix, for chaining.
     */
    fun setToReflection(x: Float, y: Float, z: Float, w: Float): Matrix4 {
        val x2 = -2f * x
        val y2 = -2f * y
        val z2 = -2f * z
        val xy2 = x2 * y
        val xz2 = x2 * z
        val yz2 = y2 * z
        val x2y2z2 = x * x + y * y + z * z
        return set(1f + x2 * x, xy2, xz2, x2 * w * x2y2z2,
                xy2, 1f + y2 * y, yz2, y2 * w * x2y2z2,
                xz2, yz2, 1f + z2 * z, z2 * w * x2y2z2,
                0f, 0f, 0f, 1f)
    }

    /**
     * Sets this to a skew by the specified amount relative to the given plane.

     * @return a reference to this matrix, for chaining.
     */
    fun setToSkew(plane: IPlane, amount: IVector3): Matrix4 {
        return setToSkew(plane.normal, plane.constant, amount)
    }

    /**
     * Sets this to a skew by the specified amount relative to the given plane.

     * @return a reference to this matrix, for chaining.
     */
    fun setToSkew(normal: IVector3, constant: Float, amount: IVector3): Matrix4 {
        return setToSkew(normal.x, normal.y, normal.z, constant,
                amount.x, amount.y, amount.z)
    }

    /**
     * Sets this to a skew by the specified amount relative to the given plane.

     * @return a reference to this matrix, for chaining.
     */
    fun setToSkew(a: Float, b: Float, c: Float, d: Float, x: Float, y: Float, z: Float): Matrix4 {
        return set(1f + a * x, b * x, c * x, d * x,
                a * y, 1f + b * y, c * y, d * y,
                a * z, b * z, 1f + c * z, d * z,
                0f, 0f, 0f, 1f)
    }

    /**
     * Sets this to a perspective projection matrix. The formula comes from the OpenGL
     * documentation for the gluPerspective function.

     * @return a reference to this matrix, for chaining.
     */
    fun setToPerspective(fovy: Float, aspect: Float, near: Float, far: Float): Matrix4 {
        val f = 1f / FloatMath.tan(fovy / 2f)
        val dscale = 1f / (near - far)
        return set(f / aspect, 0f, 0f, 0f,
                0f, f, 0f, 0f,
                0f, 0f, (far + near) * dscale, 2f * far * near * dscale,
                0f, 0f, -1f, 0f)
    }

    /**
     * Sets this to a perspective projection matrix.

     * @return a reference to this matrix, for chaining.
     */
    fun setToFrustum(
            left: Float, right: Float, bottom: Float, top: Float,
            near: Float, far: Float, nearFarNormal: IVector3 = Vector3.UNIT_Z): Matrix4 {
        val rrl = 1f / (right - left)
        val rtb = 1f / (top - bottom)
        val rnf = 1f / (near - far)
        val n2 = 2f * near
        val s = (far + near) / (near * nearFarNormal.z - far * nearFarNormal.z)
        return set(n2 * rrl, 0f, (right + left) * rrl, 0f,
                0f, n2 * rtb, (top + bottom) * rtb, 0f,
                s * nearFarNormal.x, s * nearFarNormal.y, (far + near) * rnf, n2 * far * rnf,
                0f, 0f, -1f, 0f)
    }

    /**
     * Sets this to an orthographic projection matrix.

     * @return a reference to this matrix, for chaining.
     */
    fun setToOrtho(
            left: Float, right: Float, bottom: Float, top: Float,
            near: Float, far: Float, nearFarNormal: IVector3 = Vector3.UNIT_Z): Matrix4 {
        val rlr = 1f / (left - right)
        val rbt = 1f / (bottom - top)
        val rnf = 1f / (near - far)
        val s = 2f / (near * nearFarNormal.z - far * nearFarNormal.z)
        return set(-2f * rlr, 0f, 0f, (right + left) * rlr,
                0f, -2f * rbt, 0f, (top + bottom) * rbt,
                s * nearFarNormal.x, s * nearFarNormal.y, 2f * rnf, (far + near) * rnf,
                0f, 0f, 0f, 1f)
    }

    /**
     * Copies the contents of another matrix.

     * @return a reference to this matrix, for chaining.
     */
    fun set(other: IMatrix4): Matrix4 {
        return set(other.m00, other.m10, other.m20, other.m30,
                other.m01, other.m11, other.m21, other.m31,
                other.m02, other.m12, other.m22, other.m32,
                other.m03, other.m13, other.m23, other.m33)
    }

    /**
     * Copies the elements of a row-major array.

     * @return a reference to this matrix, for chaining.
     */
    fun set(values: FloatArray): Matrix4 {
        return set(values[0], values[1], values[2], values[3],
                values[4], values[5], values[6], values[7],
                values[8], values[9], values[10], values[11],
                values[12], values[13], values[14], values[15])
    }

    /**
     * Sets all of the matrix's components at once.

     * @return a reference to this matrix, for chaining.
     */
    operator fun set(
            m00: Float, m10: Float, m20: Float, m30: Float,
            m01: Float, m11: Float, m21: Float, m31: Float,
            m02: Float, m12: Float, m22: Float, m32: Float,
            m03: Float, m13: Float, m23: Float, m33: Float): Matrix4 {
        this.m00 = m00
        this.m01 = m01
        this.m02 = m02
        this.m03 = m03
        this.m10 = m10
        this.m11 = m11
        this.m12 = m12
        this.m13 = m13
        this.m20 = m20
        this.m21 = m21
        this.m22 = m22
        this.m23 = m23
        this.m30 = m30
        this.m31 = m31
        this.m32 = m32
        this.m33 = m33
        return this
    }

    /**
     * Transposes this matrix in-place.

     * @return a reference to this matrix, for chaining.
     */
    fun transposeLocal(): Matrix4 {
        return transpose(this)
    }

    /**
     * Multiplies this matrix in-place by another.

     * @return a reference to this matrix, for chaining.
     */
    fun multLocal(other: IMatrix4): Matrix4 {
        return mult(other, this)
    }

    /**
     * Multiplies this matrix in-place by another, treating the matricees as affine.

     * @return a reference to this matrix, for chaining.
     */
    fun multAffineLocal(other: IMatrix4): Matrix4 {
        return multAffine(other, this)
    }

    /**
     * Inverts this matrix in-place.

     * @return a reference to this matrix, for chaining.
     */
    fun invertLocal(): Matrix4 {
        return invert(this)
    }

    /**
     * Inverts this matrix in-place as an affine matrix.

     * @return a reference to this matrix, for chaining.
     */
    fun invertAffineLocal(): Matrix4 {
        return invertAffine(this)
    }

    /**
     * Linearly interpolates between the this and the specified other matrix, placing the result in
     * this matrix.

     * @return a reference to this matrix, for chaining.
     */
    fun lerpLocal(other: IMatrix4, t: Float): Matrix4 {
        return lerp(other, t, this)
    }

    /**
     * Linearly interpolates between this and the specified other matrix (treating the matrices as
     * affine), placing the result in this matrix.

     * @return a reference to this matrix, for chaining.
     */
    fun lerpAffineLocal(other: IMatrix4, t: Float): Matrix4 {
        return lerpAffine(other, t, this)
    }

    override // from IMatrix4
    fun transpose(): Matrix4 {
        return transpose(Matrix4())
    }

    override // from IMatrix4
    fun transpose(result: Matrix4): Matrix4 {
        return result.set(m00, m01, m02, m03,
                m10, m11, m12, m13,
                m20, m21, m22, m23,
                m30, m31, m32, m33)
    }

    override // from IMatrix4
    fun mult(other: IMatrix4): Matrix4 {
        return mult(other, Matrix4())
    }

    override // from IMatrix4
    fun mult(other: IMatrix4, result: Matrix4): Matrix4 {
        val m00 = this.m00
        val m10 = this.m10
        val m20 = this.m20
        val m30 = this.m30
        val m01 = this.m01
        val m11 = this.m11
        val m21 = this.m21
        val m31 = this.m31
        val m02 = this.m02
        val m12 = this.m12
        val m22 = this.m22
        val m32 = this.m32
        val m03 = this.m03
        val m13 = this.m13
        val m23 = this.m23
        val m33 = this.m33
        val om00 = other.m00
        val om10 = other.m10
        val om20 = other.m20
        val om30 = other.m30
        val om01 = other.m01
        val om11 = other.m11
        val om21 = other.m21
        val om31 = other.m31
        val om02 = other.m02
        val om12 = other.m12
        val om22 = other.m22
        val om32 = other.m32
        val om03 = other.m03
        val om13 = other.m13
        val om23 = other.m23
        val om33 = other.m33
        return result.set(m00 * om00 + m10 * om01 + m20 * om02 + m30 * om03,
                m00 * om10 + m10 * om11 + m20 * om12 + m30 * om13,
                m00 * om20 + m10 * om21 + m20 * om22 + m30 * om23,
                m00 * om30 + m10 * om31 + m20 * om32 + m30 * om33,

                m01 * om00 + m11 * om01 + m21 * om02 + m31 * om03,
                m01 * om10 + m11 * om11 + m21 * om12 + m31 * om13,
                m01 * om20 + m11 * om21 + m21 * om22 + m31 * om23,
                m01 * om30 + m11 * om31 + m21 * om32 + m31 * om33,

                m02 * om00 + m12 * om01 + m22 * om02 + m32 * om03,
                m02 * om10 + m12 * om11 + m22 * om12 + m32 * om13,
                m02 * om20 + m12 * om21 + m22 * om22 + m32 * om23,
                m02 * om30 + m12 * om31 + m22 * om32 + m32 * om33,

                m03 * om00 + m13 * om01 + m23 * om02 + m33 * om03,
                m03 * om10 + m13 * om11 + m23 * om12 + m33 * om13,
                m03 * om20 + m13 * om21 + m23 * om22 + m33 * om23,
                m03 * om30 + m13 * om31 + m23 * om32 + m33 * om33)
    }

    override // from IMatrix4
    val isAffine: Boolean
        get() = m03 == 0f && m13 == 0f && m23 == 0f && m33 == 1f

    override // from IMatrix4
    val isMirrored: Boolean
        get() = m00 * (m11 * m22 - m12 * m21) + m01 * (m12 * m20 - m10 * m22) + m02 * (m10 * m21 - m11 * m20) < 0f

    override // from IMatrix4
    fun multAffine(other: IMatrix4): Matrix4 {
        return multAffine(other, Matrix4())
    }

    override // from IMatrix4
    fun multAffine(other: IMatrix4, result: Matrix4): Matrix4 {
        val m00 = this.m00
        val m10 = this.m10
        val m20 = this.m20
        val m30 = this.m30
        val m01 = this.m01
        val m11 = this.m11
        val m21 = this.m21
        val m31 = this.m31
        val m02 = this.m02
        val m12 = this.m12
        val m22 = this.m22
        val m32 = this.m32
        val om00 = other.m00
        val om10 = other.m10
        val om20 = other.m20
        val om30 = other.m30
        val om01 = other.m01
        val om11 = other.m11
        val om21 = other.m21
        val om31 = other.m31
        val om02 = other.m02
        val om12 = other.m12
        val om22 = other.m22
        val om32 = other.m32
        return result.set(m00 * om00 + m10 * om01 + m20 * om02,
                m00 * om10 + m10 * om11 + m20 * om12,
                m00 * om20 + m10 * om21 + m20 * om22,
                m00 * om30 + m10 * om31 + m20 * om32 + m30,

                m01 * om00 + m11 * om01 + m21 * om02,
                m01 * om10 + m11 * om11 + m21 * om12,
                m01 * om20 + m11 * om21 + m21 * om22,
                m01 * om30 + m11 * om31 + m21 * om32 + m31,

                m02 * om00 + m12 * om01 + m22 * om02,
                m02 * om10 + m12 * om11 + m22 * om12,
                m02 * om20 + m12 * om21 + m22 * om22,
                m02 * om30 + m12 * om31 + m22 * om32 + m32,

                0f, 0f, 0f, 1f)
    }

    override // from IMatrix4
    fun invert(): Matrix4 {
        return invert(Matrix4())
    }

    /**
     * {@inheritDoc} This code is based on the examples in the
     * [Matrix and Quaternion FAQ](http://www.j3d.org/matrix_faq/matrfaq_latest.html).
     */
    override // from IMatrix4
    fun invert(result: Matrix4): Matrix4 {
        val m00 = this.m00
        val m10 = this.m10
        val m20 = this.m20
        val m30 = this.m30
        val m01 = this.m01
        val m11 = this.m11
        val m21 = this.m21
        val m31 = this.m31
        val m02 = this.m02
        val m12 = this.m12
        val m22 = this.m22
        val m32 = this.m32
        val m03 = this.m03
        val m13 = this.m13
        val m23 = this.m23
        val m33 = this.m33
        // compute the determinant, storing the subdeterminants for later use
        val sd00 = m11 * (m22 * m33 - m23 * m32) + m21 * (m13 * m32 - m12 * m33) + m31 * (m12 * m23 - m13 * m22)
        val sd10 = m01 * (m22 * m33 - m23 * m32) + m21 * (m03 * m32 - m02 * m33) + m31 * (m02 * m23 - m03 * m22)
        val sd20 = m01 * (m12 * m33 - m13 * m32) + m11 * (m03 * m32 - m02 * m33) + m31 * (m02 * m13 - m03 * m12)
        val sd30 = m01 * (m12 * m23 - m13 * m22) + m11 * (m03 * m22 - m02 * m23) + m21 * (m02 * m13 - m03 * m12)
        val det = m00 * sd00 + m20 * sd20 - m10 * sd10 - m30 * sd30
        if (Math.abs(det) == 0f) {
            // determinant is zero; matrix is not invertible
            throw SingularMatrixException(this.toString())
        }
        val rdet = 1f / det
        return result.set(
                +sd00 * rdet,
                -(m10 * (m22 * m33 - m23 * m32) + m20 * (m13 * m32 - m12 * m33) + m30 * (m12 * m23 - m13 * m22)) * rdet,
                +(m10 * (m21 * m33 - m23 * m31) + m20 * (m13 * m31 - m11 * m33) + m30 * (m11 * m23 - m13 * m21)) * rdet,
                -(m10 * (m21 * m32 - m22 * m31) + m20 * (m12 * m31 - m11 * m32) + m30 * (m11 * m22 - m12 * m21)) * rdet,

                -sd10 * rdet,
                +(m00 * (m22 * m33 - m23 * m32) + m20 * (m03 * m32 - m02 * m33) + m30 * (m02 * m23 - m03 * m22)) * rdet,
                -(m00 * (m21 * m33 - m23 * m31) + m20 * (m03 * m31 - m01 * m33) + m30 * (m01 * m23 - m03 * m21)) * rdet,
                +(m00 * (m21 * m32 - m22 * m31) + m20 * (m02 * m31 - m01 * m32) + m30 * (m01 * m22 - m02 * m21)) * rdet,

                +sd20 * rdet,
                -(m00 * (m12 * m33 - m13 * m32) + m10 * (m03 * m32 - m02 * m33) + m30 * (m02 * m13 - m03 * m12)) * rdet,
                +(m00 * (m11 * m33 - m13 * m31) + m10 * (m03 * m31 - m01 * m33) + m30 * (m01 * m13 - m03 * m11)) * rdet,
                -(m00 * (m11 * m32 - m12 * m31) + m10 * (m02 * m31 - m01 * m32) + m30 * (m01 * m12 - m02 * m11)) * rdet,

                -sd30 * rdet,
                +(m00 * (m12 * m23 - m13 * m22) + m10 * (m03 * m22 - m02 * m23) + m20 * (m02 * m13 - m03 * m12)) * rdet,
                -(m00 * (m11 * m23 - m13 * m21) + m10 * (m03 * m21 - m01 * m23) + m20 * (m01 * m13 - m03 * m11)) * rdet,
                +(m00 * (m11 * m22 - m12 * m21) + m10 * (m02 * m21 - m01 * m22) + m20 * (m01 * m12 - m02 * m11)) * rdet)
    }

    override // from IMatrix4
    fun invertAffine(): Matrix4 {
        return invertAffine(Matrix4())
    }

    override // from IMatrix4
    fun invertAffine(result: Matrix4): Matrix4 {
        val m00 = this.m00
        val m10 = this.m10
        val m20 = this.m20
        val m30 = this.m30
        val m01 = this.m01
        val m11 = this.m11
        val m21 = this.m21
        val m31 = this.m31
        val m02 = this.m02
        val m12 = this.m12
        val m22 = this.m22
        val m32 = this.m32
        // compute the determinant, storing the subdeterminants for later use
        val sd00 = m11 * m22 - m21 * m12
        val sd10 = m01 * m22 - m21 * m02
        val sd20 = m01 * m12 - m11 * m02
        val det = m00 * sd00 + m20 * sd20 - m10 * sd10
        if (Math.abs(det) == 0f) {
            // determinant is zero; matrix is not invertible
            throw SingularMatrixException(this.toString())
        }
        val rdet = 1f / det
        return result.set(
                +sd00 * rdet,
                -(m10 * m22 - m20 * m12) * rdet,
                +(m10 * m21 - m20 * m11) * rdet,
                -(m10 * (m21 * m32 - m22 * m31) + m20 * (m12 * m31 - m11 * m32) + m30 * sd00) * rdet,

                -sd10 * rdet,
                +(m00 * m22 - m20 * m02) * rdet,
                -(m00 * m21 - m20 * m01) * rdet,
                +(m00 * (m21 * m32 - m22 * m31) + m20 * (m02 * m31 - m01 * m32) + m30 * sd10) * rdet,

                +sd20 * rdet,
                -(m00 * m12 - m10 * m02) * rdet,
                +(m00 * m11 - m10 * m01) * rdet,
                -(m00 * (m11 * m32 - m12 * m31) + m10 * (m02 * m31 - m01 * m32) + m30 * sd20) * rdet,

                0f, 0f, 0f, 1f)
    }

    override // from IMatrix4
    fun lerp(other: IMatrix4, t: Float): Matrix4 {
        return lerp(other, t, Matrix4())
    }

    override // from IMatrix4
    fun lerp(other: IMatrix4, t: Float, result: Matrix4): Matrix4 {
        val m00 = this.m00
        val m10 = this.m10
        val m20 = this.m20
        val m30 = this.m30
        val m01 = this.m01
        val m11 = this.m11
        val m21 = this.m21
        val m31 = this.m31
        val m02 = this.m02
        val m12 = this.m12
        val m22 = this.m22
        val m32 = this.m32
        val m03 = this.m03
        val m13 = this.m13
        val m23 = this.m23
        val m33 = this.m33
        return result.set(m00 + t * (other.m00 - m00),
                m10 + t * (other.m10 - m10),
                m20 + t * (other.m20 - m20),
                m30 + t * (other.m30 - m30),

                m01 + t * (other.m01 - m01),
                m11 + t * (other.m11 - m11),
                m21 + t * (other.m21 - m21),
                m31 + t * (other.m31 - m31),

                m02 + t * (other.m02 - m02),
                m12 + t * (other.m12 - m12),
                m22 + t * (other.m22 - m22),
                m32 + t * (other.m32 - m32),

                m03 + t * (other.m03 - m03),
                m13 + t * (other.m13 - m13),
                m23 + t * (other.m23 - m23),
                m33 + t * (other.m33 - m33))
    }

    override // from IMatrix4
    fun lerpAffine(other: IMatrix4, t: Float): Matrix4 {
        return lerpAffine(other, t, Matrix4())
    }

    override // from IMatrix4
    fun lerpAffine(other: IMatrix4, t: Float, result: Matrix4): Matrix4 {
        val m00 = this.m00
        val m10 = this.m10
        val m20 = this.m20
        val m30 = this.m30
        val m01 = this.m01
        val m11 = this.m11
        val m21 = this.m21
        val m31 = this.m31
        val m02 = this.m02
        val m12 = this.m12
        val m22 = this.m22
        val m32 = this.m32
        return result.set(m00 + t * (other.m00 - m00),
                m10 + t * (other.m10 - m10),
                m20 + t * (other.m20 - m20),
                m30 + t * (other.m30 - m30),

                m01 + t * (other.m01 - m01),
                m11 + t * (other.m11 - m11),
                m21 + t * (other.m21 - m21),
                m31 + t * (other.m31 - m31),

                m02 + t * (other.m02 - m02),
                m12 + t * (other.m12 - m12),
                m22 + t * (other.m22 - m22),
                m32 + t * (other.m32 - m32),

                0f, 0f, 0f, 1f)
    }

    override // from IMatrix4
    fun projectPointLocal(point: Vector3): Vector3 {
        return projectPoint(point, point)
    }

    override // from IMatrix4
    fun projectPoint(point: IVector3): Vector3 {
        return projectPoint(point, Vector3())
    }

    override // from IMatrix4
    fun projectPoint(point: IVector3, result: Vector3): Vector3 {
        val px = point.x
        val py = point.y
        val pz = point.z
        val rw = 1f / (m03 * px + m13 * py + m23 * pz + m33)
        return result.set((m00 * px + m10 * py + m20 * pz + m30) * rw,
                (m01 * px + m11 * py + m21 * pz + m31) * rw,
                (m02 * px + m12 * py + m22 * pz + m32) * rw)
    }

    override // from IMatrix4
    fun transformPointLocal(point: Vector3): Vector3 {
        return transformPoint(point, point)
    }

    override // from IMatrix4
    fun transformPoint(point: IVector3): Vector3 {
        return transformPoint(point, Vector3())
    }

    override // from IMatrix4
    fun transformPoint(point: IVector3, result: Vector3): Vector3 {
        val px = point.x
        val py = point.y
        val pz = point.z
        return result.set(m00 * px + m10 * py + m20 * pz + m30,
                m01 * px + m11 * py + m21 * pz + m31,
                m02 * px + m12 * py + m22 * pz + m32)
    }

    override // from IMatrix4
    fun transformPointZ(point: IVector3): Float {
        return m02 * point.x + m12 * point.y + m22 * point.z + m32
    }

    override // from IMatrix4
    fun transformVectorLocal(vector: Vector3): Vector3 {
        return transformVector(vector, vector)
    }

    override // from IMatrix4
    fun transformVector(vector: IVector3): Vector3 {
        return transformVector(vector, Vector3())
    }

    override // from IMatrix4
    fun transformVector(vector: IVector3, result: Vector3): Vector3 {
        val vx = vector.x
        val vy = vector.y
        val vz = vector.z
        return result.set(m00 * vx + m10 * vy + m20 * vz,
                m01 * vx + m11 * vy + m21 * vz,
                m02 * vx + m12 * vy + m22 * vz)
    }

    override // from IMatrix4
    fun transform(vector: IVector4): Vector4 {
        return transform(vector, Vector4())
    }

    override // from IMatrix4
    fun transform(vector: IVector4, result: Vector4): Vector4 {
        val vx = vector.x
        val vy = vector.y
        val vz = vector.z
        val vw = vector.w
        return result.set(m00 * vx + m10 * vy + m20 * vz + m30 * vw,
                m01 * vx + m11 * vy + m21 * vz + m31 * vw,
                m02 * vx + m12 * vy + m22 * vz + m32 * vw,
                m03 * vx + m13 * vy + m23 * vz + m33 * vw)
    }

    override // from IMatrix4
    fun extractRotation(): Quaternion {
        return extractRotation(Quaternion())
    }

    /**
     * {@inheritDoc} This uses the iterative polar decomposition algorithm described by
     * [Ken
       * Shoemake](http://www.cs.wisc.edu/graphics/Courses/838-s2002/Papers/polar-decomp.pdf).
     */
    override // from IMatrix4
    fun extractRotation(result: Quaternion): Quaternion {
        // start with the contents of the upper 3x3 portion of the matrix
        var n00 = this.m00
        var n10 = this.m10
        var n20 = this.m20
        var n01 = this.m01
        var n11 = this.m11
        var n21 = this.m21
        var n02 = this.m02
        var n12 = this.m12
        var n22 = this.m22
        for (ii in 0..9) {
            // store the results of the previous iteration
            val o00 = n00
            val o10 = n10
            val o20 = n20
            val o01 = n01
            val o11 = n11
            val o21 = n21
            val o02 = n02
            val o12 = n12
            val o22 = n22

            // compute average of the matrix with its inverse transpose
            val sd00 = o11 * o22 - o21 * o12
            val sd10 = o01 * o22 - o21 * o02
            val sd20 = o01 * o12 - o11 * o02
            val det = o00 * sd00 + o20 * sd20 - o10 * sd10
            if (Math.abs(det) == 0f) {
                // determinant is zero; matrix is not invertible
                throw SingularMatrixException(this.toString())
            }
            val hrdet = 0.5f / det
            n00 = +sd00 * hrdet + o00 * 0.5f
            n10 = -sd10 * hrdet + o10 * 0.5f
            n20 = +sd20 * hrdet + o20 * 0.5f

            n01 = -(o10 * o22 - o20 * o12) * hrdet + o01 * 0.5f
            n11 = +(o00 * o22 - o20 * o02) * hrdet + o11 * 0.5f
            n21 = -(o00 * o12 - o10 * o02) * hrdet + o21 * 0.5f

            n02 = +(o10 * o21 - o20 * o11) * hrdet + o02 * 0.5f
            n12 = -(o00 * o21 - o20 * o01) * hrdet + o12 * 0.5f
            n22 = +(o00 * o11 - o10 * o01) * hrdet + o22 * 0.5f

            // compute the difference; if it's small enough, we're done
            val d00 = n00 - o00
            val d10 = n10 - o10
            val d20 = n20 - o20
            val d01 = n01 - o01
            val d11 = n11 - o11
            val d21 = n21 - o21
            val d02 = n02 - o02
            val d12 = n12 - o12
            val d22 = n22 - o22
            if (d00 * d00 + d10 * d10 + d20 * d20 + d01 * d01 + d11 * d11 + d21 * d21 +
                    d02 * d02 + d12 * d12 + d22 * d22 < MathUtil.EPSILON) {
                break
            }
        }
        // now that we have a nice orthogonal matrix, we can extract the rotation quaternion
        // using the method described in http://en.wikipedia.org/wiki/Rotation_matrix#Conversions
        val x2 = Math.abs(1f + n00 - n11 - n22)
        val y2 = Math.abs(1f - n00 + n11 - n22)
        val z2 = Math.abs(1f - n00 - n11 + n22)
        val w2 = Math.abs(1f + n00 + n11 + n22)
        result.set(
                0.5f * FloatMath.sqrt(x2) * if (n12 >= n21) +1f else -1f,
                0.5f * FloatMath.sqrt(y2) * if (n20 >= n02) +1f else -1f,
                0.5f * FloatMath.sqrt(z2) * if (n01 >= n10) +1f else -1f,
                0.5f * FloatMath.sqrt(w2))
        return result
    }

    override // from IMatrix4
    fun extractRotationScale(result: Matrix3): Matrix3 {
        return result.set(m00, m01, m02,
                m10, m11, m12,
                m20, m21, m22)
    }

    override // from IMatrix4
    fun extractScale(): Vector3 {
        return extractScale(Vector3())
    }

    override // from IMatrix4
    fun extractScale(result: Vector3): Vector3 {
        return result.set(FloatMath.sqrt(m00 * m00 + m01 * m01 + m02 * m02),
                FloatMath.sqrt(m10 * m10 + m11 * m11 + m12 * m12),
                FloatMath.sqrt(m20 * m20 + m21 * m21 + m22 * m22))
    }

    override // from IMatrix4
    fun approximateUniformScale(): Float {
        return FloatMath.cbrt(m00 * (m11 * m22 - m12 * m21) +
                m01 * (m12 * m20 - m10 * m22) +
                m02 * (m10 * m21 - m11 * m20))
    }

    override // from IMatrix4
    fun epsilonEquals(other: IMatrix4, epsilon: Float): Boolean {
        return Math.abs(m00 - other.m00) < epsilon &&
                Math.abs(m10 - other.m10) < epsilon &&
                Math.abs(m20 - other.m20) < epsilon &&
                Math.abs(m30 - other.m30) < epsilon &&

                Math.abs(m01 - other.m01) < epsilon &&
                Math.abs(m11 - other.m11) < epsilon &&
                Math.abs(m21 - other.m21) < epsilon &&
                Math.abs(m31 - other.m31) < epsilon &&

                Math.abs(m02 - other.m02) < epsilon &&
                Math.abs(m12 - other.m12) < epsilon &&
                Math.abs(m22 - other.m22) < epsilon &&
                Math.abs(m32 - other.m32) < epsilon &&

                Math.abs(m03 - other.m03) < epsilon &&
                Math.abs(m13 - other.m13) < epsilon &&
                Math.abs(m23 - other.m23) < epsilon &&
                Math.abs(m33 - other.m33) < epsilon
    }

    override fun toString(): String {
        return "[[" + m00 + ", " + m10 + ", " + m20 + ", " + m30 + "], " +
                "[" + m01 + ", " + m11 + ", " + m21 + ", " + m31 + "], " +
                "[" + m02 + ", " + m12 + ", " + m22 + ", " + m32 + "], " +
                "[" + m03 + ", " + m13 + ", " + m23 + ", " + m33 + "]]"
    }

    override fun hashCode(): Int {
        return Platform.hashCode(m00) xor Platform.hashCode(m10) xor
                Platform.hashCode(m20) xor Platform.hashCode(m30) xor
                Platform.hashCode(m01) xor Platform.hashCode(m11) xor
                Platform.hashCode(m21) xor Platform.hashCode(m31) xor
                Platform.hashCode(m02) xor Platform.hashCode(m12) xor
                Platform.hashCode(m22) xor Platform.hashCode(m32) xor
                Platform.hashCode(m03) xor Platform.hashCode(m13) xor
                Platform.hashCode(m23) xor Platform.hashCode(m33)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Matrix4) {
            return false
        }
        val omat = other
        return m00 == omat.m00 && m10 == omat.m10 && m20 == omat.m20 && m30 == omat.m30 &&
                m01 == omat.m01 && m11 == omat.m11 && m21 == omat.m21 && m31 == omat.m31 &&
                m02 == omat.m02 && m12 == omat.m12 && m22 == omat.m22 && m32 == omat.m32 &&
                m03 == omat.m03 && m13 == omat.m13 && m23 == omat.m23 && m33 == omat.m33
    }

    companion object {
        private const val serialVersionUID = -4239146234415362557L

        /** The identity matrix.  */
        val IDENTITY: IMatrix4 = Matrix4()

        /** An empty matrix array.  */
        val EMPTY_ARRAY = arrayOfNulls<Matrix4>(0)
    }
}
/**
 * Sets this to a perspective projection matrix. The formula comes from the OpenGL
 * documentation for the glFrustum function.

 * @return a reference to this matrix, for chaining.
 */
/**
 * Sets this to an orthographic projection matrix. The formula comes from the OpenGL
 * documentation for the glOrtho function.

 * @return a reference to this matrix, for chaining.
 */
