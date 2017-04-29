//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

import pythagoras.util.Platform
import pythagoras.util.SingularMatrixException
import java.lang.Math

/**
 * A 3x3 column-major matrix.
 */
class Matrix3 : IMatrix3 {

    /** The values of the matrix. The names take the form `mCOLROW`.  */
    var m00: Float = 0.toFloat()
    var m10: Float = 0.toFloat()
    var m20: Float = 0.toFloat()
    var m01: Float = 0.toFloat()
    var m11: Float = 0.toFloat()
    var m21: Float = 0.toFloat()
    var m02: Float = 0.toFloat()
    var m12: Float = 0.toFloat()
    var m22: Float = 0.toFloat()

    /**
     * Creates a matrix from its components.
     */
    constructor(m00: Float, m10: Float, m20: Float,
                m01: Float, m11: Float, m21: Float,
                m02: Float, m12: Float, m22: Float) {
        set(m00, m10, m20,
                m01, m11, m21,
                m02, m12, m22)
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
    constructor(other: Matrix3) {
        set(other)
    }

    /**
     * Creates an identity matrix.
     */
    constructor() {
        setToIdentity()
    }

    /**
     * Sets the matrix element at the specified row and column.
     */
    fun setElement(row: Int, col: Int, value: Float) {
        when (col) {
            0 -> when (row) {
                0 -> {
                    m00 = value
                    return
                }
                1 -> {
                    m01 = value
                    return
                }
                2 -> {
                    m02 = value
                    return
                }
            }
            1 -> when (row) {
                0 -> {
                    m10 = value
                    return
                }
                1 -> {
                    m11 = value
                    return
                }
                2 -> {
                    m12 = value
                    return
                }
            }
            2 -> when (row) {
                0 -> {
                    m20 = value
                    return
                }
                1 -> {
                    m21 = value
                    return
                }
                2 -> {
                    m22 = value
                    return
                }
            }
        }
        throw ArrayIndexOutOfBoundsException()
    }

    /**
     * Sets the specified row (0, 1, 2) to the supplied values.
     */
    fun setRow(row: Int, x: Float, y: Float, z: Float) {
        when (row) {
            0 -> {
                m00 = x
                m10 = y
                m20 = z
            }
            1 -> {
                m01 = x
                m11 = y
                m21 = z
            }
            2 -> {
                m02 = x
                m12 = y
                m22 = z
            }
            else -> throw ArrayIndexOutOfBoundsException()
        }
    }

    /**
     * Sets the specified row (0, 1, 2) to the supplied vector.
     */
    fun setRow(row: Int, v: Vector3) {
        setRow(row, v.x, v.y, v.z)
    }

    /**
     * Sets the specified column (0, 1, 2) to the supplied values.
     */
    fun setColumn(col: Int, x: Float, y: Float, z: Float) {
        when (col) {
            0 -> {
                m00 = x
                m01 = y
                m02 = z
            }
            1 -> {
                m10 = x
                m11 = y
                m12 = z
            }
            2 -> {
                m20 = x
                m21 = y
                m22 = z
            }
            else -> throw ArrayIndexOutOfBoundsException()
        }
    }

    /**
     * Sets the specified column (0, 1, 2) to the supplied vector.
     */
    fun setColumn(col: Int, v: Vector3) {
        setColumn(col, v.x, v.y, v.z)
    }

    /**
     * Sets this matrix to the identity matrix.

     * @return a reference to this matrix, for chaining.
     */
    fun setToIdentity(): Matrix3 {
        return set(1f, 0f, 0f,
                0f, 1f, 0f,
                0f, 0f, 1f)
    }

    /**
     * Sets this matrix to all zeroes.

     * @return a reference to this matrix, for chaining.
     */
    fun setToZero(): Matrix3 {
        return set(0f, 0f, 0f,
                0f, 0f, 0f,
                0f, 0f, 0f)
    }

    /**
     * Sets this to a rotation matrix that rotates one vector onto another.

     * @return a reference to this matrix, for chaining.
     */
    fun setToRotation(from: IVector3, to: IVector3): Matrix3 {
        val angle = from.angle(to)
        return if (angle < 0.0001f)
            setToIdentity()
        else
            setToRotation(angle, from.cross(to).normalizeLocal())
    }

    /**
     * Sets this to a rotation matrix.

     * @return a reference to this matrix, for chaining.
     */
    fun setToRotation(angle: Float, axis: IVector3): Matrix3 {
        return setToRotation(angle, axis.x, axis.y, axis.z)
    }

    /**
     * Sets this to a rotation matrix. The formula comes from the OpenGL documentation for the
     * glRotatef function.

     * @return a reference to this matrix, for chaining.
     */
    fun setToRotation(angle: Float, x: Float, y: Float, z: Float): Matrix3 {
        val c = FloatMath.cos(angle)
        val s = FloatMath.sin(angle)
        val omc = 1f - c
        val xs = x * s
        val ys = y * s
        val zs = z * s
        val xy = x * y
        val xz = x * z
        val yz = y * z
        return set(x * x * omc + c, xy * omc - zs, xz * omc + ys,
                xy * omc + zs, y * y * omc + c, yz * omc - xs,
                xz * omc - ys, yz * omc + xs, z * z * omc + c)
    }

    /**
     * Sets this to a rotation matrix. The formula comes from the
     * [Matrix and Quaternion FAQ](http://www.j3d.org/matrix_faq/matrfaq_latest.html).

     * @return a reference to this matrix, for chaining.
     */
    fun setToRotation(quat: IQuaternion): Matrix3 {
        val qx = quat.x
        val qy = quat.y
        val qz = quat.z
        val qw = quat.w
        val xx = qx * qx
        val yy = qy * qy
        val zz = qz * qz
        val xy = qx * qy
        val xz = qx * qz
        val xw = qx * qw
        val yz = qy * qz
        val yw = qy * qw
        val zw = qz * qw
        return set(1f - 2f * (yy + zz), 2f * (xy - zw), 2f * (xz + yw),
                2f * (xy + zw), 1f - 2f * (xx + zz), 2f * (yz - xw),
                2f * (xz - yw), 2f * (yz + xw), 1f - 2f * (xx + yy))
    }

    /**
     * Sets this to a scale matrix.

     * @return a reference to this matrix, for chaining.
     */
    fun setToScale(scale: IVector3): Matrix3 {
        return setToScale(scale.x, scale.y, scale.z)
    }

    /**
     * Sets this to a uniform scale matrix.

     * @return a reference to this matrix, for chaining.
     */
    fun setToScale(s: Float): Matrix3 {
        return setToScale(s, s, s)
    }

    /**
     * Sets this to a scale matrix.

     * @return a reference to this matrix, for chaining.
     */
    fun setToScale(x: Float, y: Float, z: Float): Matrix3 {
        return set(x, 0f, 0f,
                0f, y, 0f,
                0f, 0f, z)
    }

    /**
     * Sets this to a reflection across a plane intersecting the origin with the supplied normal.

     * @return a reference to this matrix, for chaining.
     */
    fun setToReflection(normal: IVector3): Matrix3 {
        return setToReflection(normal.x, normal.y, normal.z)
    }

    /**
     * Sets this to a reflection across a plane intersecting the origin with the supplied normal.

     * @return a reference to this matrix, for chaining.
     */
    fun setToReflection(x: Float, y: Float, z: Float): Matrix3 {
        val x2 = -2f * x
        val y2 = -2f * y
        val z2 = -2f * z
        val xy2 = x2 * y
        val xz2 = x2 * z
        val yz2 = y2 * z
        return set(1f + x2 * x, xy2, xz2,
                xy2, 1f + y2 * y, yz2,
                xz2, yz2, 1f + z2 * z)
    }

    /**
     * Sets this to a matrix that first rotates, then translates.

     * @return a reference to this matrix, for chaining.
     */
    fun setToTransform(translation: IVector, rotation: Float): Matrix3 {
        return setToRotation(rotation).setTranslation(translation)
    }

    /**
     * Sets this to a matrix that first scales, then rotates, then translates.

     * @return a reference to this matrix, for chaining.
     */
    fun setToTransform(translation: IVector, rotation: Float, scale: Float): Matrix3 {
        return setToRotation(rotation).set(m00 * scale, m10 * scale, translation.x,
                m01 * scale, m11 * scale, translation.y,
                0f, 0f, 1f)
    }

    /**
     * Sets this to a matrix that first scales, then rotates, then translates.

     * @return a reference to this matrix, for chaining.
     */
    fun setToTransform(translation: IVector, rotation: Float, scale: IVector): Matrix3 {
        val sx = scale.x
        val sy = scale.y
        return setToRotation(rotation).set(m00 * sx, m10 * sy, translation.x,
                m01 * sx, m11 * sy, translation.y,
                0f, 0f, 1f)
    }

    /**
     * Sets this to a translation matrix.

     * @return a reference to this matrix, for chaining.
     */
    fun setToTranslation(translation: IVector): Matrix3 {
        return setToTranslation(translation.x, translation.y)
    }

    /**
     * Sets this to a translation matrix.

     * @return a reference to this matrix, for chaining.
     */
    fun setToTranslation(x: Float, y: Float): Matrix3 {
        return set(1f, 0f, x,
                0f, 1f, y,
                0f, 0f, 1f)
    }

    /**
     * Sets the translation component of this matrix.

     * @return a reference to this matrix, for chaining.
     */
    fun setTranslation(translation: IVector): Matrix3 {
        return setTranslation(translation.x, translation.y)
    }

    /**
     * Sets the translation component of this matrix.

     * @return a reference to this matrix, for chaining.
     */
    fun setTranslation(x: Float, y: Float): Matrix3 {
        m20 = x
        m21 = y
        return this
    }

    /**
     * Sets this to a rotation matrix.

     * @return a reference to this matrix, for chaining.
     */
    fun setToRotation(angle: Float): Matrix3 {
        val sina = FloatMath.sin(angle)
        val cosa = FloatMath.cos(angle)
        return set(cosa, -sina, 0f,
                sina, cosa, 0f,
                0f, 0f, 1f)
    }

    /**
     * Transposes this matrix in-place.

     * @return a reference to this matrix, for chaining.
     */
    fun transposeLocal(): Matrix3 {
        return transpose(this)
    }

    /**
     * Multiplies this matrix in-place by another.

     * @return a reference to this matrix, for chaining.
     */
    fun multLocal(other: IMatrix3): Matrix3 {
        return mult(other, this)
    }

    /**
     * Adds `other` to this matrix, in place.

     * @return a reference to this matrix, for chaining.
     */
    fun addLocal(other: IMatrix3): Matrix3 {
        return add(other, this)
    }

    /**
     * Multiplies this matrix in-place by another, treating the matricees as affine.

     * @return a reference to this matrix, for chaining.
     */
    fun multAffineLocal(other: IMatrix3): Matrix3 {
        return multAffine(other, this)
    }

    /**
     * Inverts this matrix in-place.

     * @return a reference to this matrix, for chaining.
     */
    fun invertLocal(): Matrix3 {
        return invert(this)
    }

    /**
     * Inverts this matrix in-place as an affine matrix.

     * @return a reference to this matrix, for chaining.
     */
    fun invertAffineLocal(): Matrix3 {
        return invertAffine(this)
    }

    /**
     * Linearly interpolates between the this and the specified other matrix, placing the result in
     * this matrix.

     * @return a reference to this matrix, for chaining.
     */
    fun lerpLocal(other: IMatrix3, t: Float): Matrix3 {
        return lerp(other, t, this)
    }

    /**
     * Linearly interpolates between this and the specified other matrix (treating the matrices as
     * affine), placing the result in this matrix.

     * @return a reference to this matrix, for chaining.
     */
    fun lerpAffineLocal(other: IMatrix3, t: Float): Matrix3 {
        return lerpAffine(other, t, this)
    }

    /**
     * Copies the contents of another matrix.

     * @return a reference to this matrix, for chaining.
     */
    fun set(other: IMatrix3): Matrix3 {
        return set(other.m00(), other.m10(), other.m20(),
                other.m01(), other.m11(), other.m21(),
                other.m02(), other.m12(), other.m22())
    }

    /**
     * Copies the elements of an array.

     * @return a reference to this matrix, for chaining.
     */
    fun set(values: FloatArray): Matrix3 {
        return set(values[0], values[1], values[2],
                values[3], values[4], values[5],
                values[6], values[7], values[8])
    }

    /**
     * Sets all of the matrix's components at once.

     * @return a reference to this matrix, for chaining.
     */
    operator fun set(
            m00: Float, m10: Float, m20: Float,
            m01: Float, m11: Float, m21: Float,
            m02: Float, m12: Float, m22: Float): Matrix3 {
        this.m00 = m00
        this.m01 = m01
        this.m02 = m02
        this.m10 = m10
        this.m11 = m11
        this.m12 = m12
        this.m20 = m20
        this.m21 = m21
        this.m22 = m22
        return this
    }

    override // from IMatrix3
    fun m00(): Float {
        return m00
    }

    override // from IMatrix3
    fun m10(): Float {
        return m10
    }

    override // from IMatrix3
    fun m20(): Float {
        return m20
    }

    override // from IMatrix3
    fun m01(): Float {
        return m01
    }

    override // from IMatrix3
    fun m11(): Float {
        return m11
    }

    override // from IMatrix3
    fun m21(): Float {
        return m21
    }

    override // from IMatrix3
    fun m02(): Float {
        return m02
    }

    override // from IMatrix3
    fun m12(): Float {
        return m12
    }

    override // from IMatrix3
    fun m22(): Float {
        return m22
    }

    override // from IMatrix3
    fun element(row: Int, col: Int): Float {
        when (col) {
            0 -> when (row) {
                0 -> return m00
                1 -> return m01
                2 -> return m02
            }
            1 -> when (row) {
                0 -> return m10
                1 -> return m11
                2 -> return m12
            }
            2 -> when (row) {
                0 -> return m20
                1 -> return m21
                2 -> return m22
            }
        }
        throw ArrayIndexOutOfBoundsException()
    }

    override // from IMatrix3
    fun getRow(row: Int, result: Vector3) {
        when (row) {
            0 -> {
                result.x = m00
                result.y = m10
                result.z = m20
            }
            1 -> {
                result.x = m01
                result.y = m11
                result.z = m21
            }
            2 -> {
                result.x = m02
                result.y = m12
                result.z = m22
            }
            else -> throw ArrayIndexOutOfBoundsException()
        }
    }

    override // from IMatrix3
    fun getColumn(col: Int, result: Vector3) {
        when (col) {
            0 -> {
                result.x = m00
                result.y = m01
                result.z = m02
            }
            1 -> {
                result.x = m10
                result.y = m11
                result.z = m12
            }
            2 -> {
                result.x = m20
                result.y = m21
                result.z = m22
            }
            else -> throw ArrayIndexOutOfBoundsException()
        }
    }

    override // from IMatrix3
    fun transpose(): Matrix3 {
        return transpose(Matrix3())
    }

    override // from IMatrix3
    fun transpose(result: Matrix3): Matrix3 {
        return result.set(m00, m01, m02,
                m10, m11, m12,
                m20, m21, m22)
    }

    override // from IMatrix3
    fun mult(other: IMatrix3): Matrix3 {
        return mult(other, Matrix3())
    }

    override // from IMatrix3
    fun mult(other: IMatrix3, result: Matrix3): Matrix3 {
        val m00 = this.m00
        val m01 = this.m01
        val m02 = this.m02
        val m10 = this.m10
        val m11 = this.m11
        val m12 = this.m12
        val m20 = this.m20
        val m21 = this.m21
        val m22 = this.m22
        val om00 = other.m00()
        val om01 = other.m01()
        val om02 = other.m02()
        val om10 = other.m10()
        val om11 = other.m11()
        val om12 = other.m12()
        val om20 = other.m20()
        val om21 = other.m21()
        val om22 = other.m22()
        return result.set(m00 * om00 + m10 * om01 + m20 * om02,
                m00 * om10 + m10 * om11 + m20 * om12,
                m00 * om20 + m10 * om21 + m20 * om22,

                m01 * om00 + m11 * om01 + m21 * om02,
                m01 * om10 + m11 * om11 + m21 * om12,
                m01 * om20 + m11 * om21 + m21 * om22,

                m02 * om00 + m12 * om01 + m22 * om02,
                m02 * om10 + m12 * om11 + m22 * om12,
                m02 * om20 + m12 * om21 + m22 * om22)
    }

    override // from IMatrix3
    fun add(other: IMatrix3): Matrix3 {
        return add(other, Matrix3())
    }

    override // from IMatrix3
    fun add(other: IMatrix3, result: Matrix3): Matrix3 {
        return result.set(m00 + other.m00(), m01 + other.m01(), m02 + other.m02(),
                m10 + other.m10(), m11 + other.m11(), m12 + other.m12(),
                m20 + other.m20(), m21 + other.m21(), m22 + other.m22())
    }

    override // from IMatrix3
    val isAffine: Boolean
        get() = m02 == 0f && m12 == 0f && m22 == 1f

    override // from IMatrix3
    fun multAffine(other: IMatrix3): Matrix3 {
        return multAffine(other, Matrix3())
    }

    override // from IMatrix3
    fun multAffine(other: IMatrix3, result: Matrix3): Matrix3 {
        val m00 = this.m00
        val m01 = this.m01
        val m10 = this.m10
        val m11 = this.m11
        val m20 = this.m20
        val m21 = this.m21
        val om00 = other.m00()
        val om01 = other.m01()
        val om10 = other.m10()
        val om11 = other.m11()
        val om20 = other.m20()
        val om21 = other.m21()
        return result.set(m00 * om00 + m10 * om01,
                m00 * om10 + m10 * om11,
                m00 * om20 + m10 * om21 + m20,

                m01 * om00 + m11 * om01,
                m01 * om10 + m11 * om11,
                m01 * om20 + m11 * om21 + m21,

                0f, 0f, 1f)
    }

    override // from IMatrix3
    fun invert(): Matrix3 {
        return invert(Matrix3())
    }

    /**
     * {@inheritDoc} This code is based on the examples in the
     * [Matrix and Quaternion FAQ](http://www.j3d.org/matrix_faq/matrfaq_latest.html).
     */
    @Throws(SingularMatrixException::class)
    override // from IMatrix3
    fun invert(result: Matrix3): Matrix3 {
        val m00 = this.m00
        val m01 = this.m01
        val m02 = this.m02
        val m10 = this.m10
        val m11 = this.m11
        val m12 = this.m12
        val m20 = this.m20
        val m21 = this.m21
        val m22 = this.m22
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
        return result.set(+sd00 * rdet,
                -(m10 * m22 - m20 * m12) * rdet,
                +(m10 * m21 - m20 * m11) * rdet,

                -sd10 * rdet,
                +(m00 * m22 - m20 * m02) * rdet,
                -(m00 * m21 - m20 * m01) * rdet,

                +sd20 * rdet,
                -(m00 * m12 - m10 * m02) * rdet,
                +(m00 * m11 - m10 * m01) * rdet)
    }

    override // from IMatrix3
    fun invertAffine(): Matrix3 {
        return invertAffine(Matrix3())
    }

    @Throws(SingularMatrixException::class)
    override // from IMatrix3
    fun invertAffine(result: Matrix3): Matrix3 {
        val m00 = this.m00
        val m01 = this.m01
        val m10 = this.m10
        val m11 = this.m11
        val m20 = this.m20
        val m21 = this.m21
        // compute the determinant, storing the subdeterminants for later use
        val det = m00 * m11 - m10 * m01
        if (Math.abs(det) == 0f) {
            // determinant is zero; matrix is not invertible
            throw SingularMatrixException(this.toString())
        }
        val rdet = 1f / det
        return result.set(+m11 * rdet,
                -m10 * rdet,
                +(m10 * m21 - m20 * m11) * rdet,

                -m01 * rdet,
                +m00 * rdet,
                -(m00 * m21 - m20 * m01) * rdet,

                0f, 0f, 1f)
    }

    override // from IMatrix3
    fun lerp(other: IMatrix3, t: Float): Matrix3 {
        return lerp(other, t, Matrix3())
    }

    override // from IMatrix3
    fun lerp(other: IMatrix3, t: Float, result: Matrix3): Matrix3 {
        val m00 = this.m00
        val m01 = this.m01
        val m02 = this.m02
        val m10 = this.m10
        val m11 = this.m11
        val m12 = this.m12
        val m20 = this.m20
        val m21 = this.m21
        val m22 = this.m22
        val om00 = other.m00()
        val om01 = other.m01()
        val om02 = other.m02()
        val om10 = other.m10()
        val om11 = other.m11()
        val om12 = other.m12()
        val om20 = other.m20()
        val om21 = other.m21()
        val om22 = other.m22()
        return result.set(m00 + t * (om00 - m00),
                m10 + t * (om10 - m10),
                m20 + t * (om20 - m20),

                m01 + t * (om01 - m01),
                m11 + t * (om11 - m11),
                m21 + t * (om21 - m21),

                m02 + t * (om02 - m02),
                m12 + t * (om12 - m12),
                m22 + t * (om22 - m22))
    }

    override // from IMatrix3
    fun lerpAffine(other: IMatrix3, t: Float): Matrix3 {
        return lerpAffine(other, t, Matrix3())
    }

    override // from IMatrix3
    fun lerpAffine(other: IMatrix3, t: Float, result: Matrix3): Matrix3 {
        val m00 = this.m00
        val m01 = this.m01
        val m10 = this.m10
        val m11 = this.m11
        val m20 = this.m20
        val m21 = this.m21
        val om00 = other.m00()
        val om01 = other.m01()
        val om10 = other.m10()
        val om11 = other.m11()
        val om20 = other.m20()
        val om21 = other.m21()
        return result.set(m00 + t * (om00 - m00),
                m10 + t * (om10 - m10),
                m20 + t * (om20 - m20),

                m01 + t * (om01 - m01),
                m11 + t * (om11 - m11),
                m21 + t * (om21 - m21),

                0f, 0f, 1f)
    }

    override // from IMatrix3
    fun get(buf: FloatBuffer): FloatBuffer {
        buf.put(m00).put(m01).put(m02)
        buf.put(m10).put(m11).put(m12)
        buf.put(m20).put(m21).put(m22)
        return buf
    }

    override // from IMatrix3
    fun transformLocal(vector: Vector3): Vector3 {
        return transform(vector, vector)
    }

    override // from IMatrix3
    fun transform(vector: IVector3): Vector3 {
        return transform(vector, Vector3())
    }

    override // from IMatrix3
    fun transform(vector: IVector3, result: Vector3): Vector3 {
        val vx = vector.x
        val vy = vector.y
        val vz = vector.z
        return result.set(m00 * vx + m10 * vy + m20 * vz,
                m01 * vx + m11 * vy + m21 * vz,
                m02 * vx + m12 * vy + m22 * vz)
    }

    override // from IMatrix3
    fun transformPointLocal(point: Vector): Vector {
        return transformPoint(point, point)
    }

    override // from IMatrix3
    fun transformPoint(point: IVector): Vector {
        return transformPoint(point, Vector())
    }

    override // from IMatrix3
    fun transformPoint(point: IVector, result: Vector): Vector {
        val px = point.x
        val py = point.y
        return result.set(m00 * px + m10 * py + m20, m01 * px + m11 * py + m21)
    }

    override // from IMatrix3
    fun transformVectorLocal(vector: Vector): Vector {
        return transformVector(vector, vector)
    }

    override // from IMatrix3
    fun transformVector(vector: IVector): Vector {
        return transformVector(vector, Vector())
    }

    override // from IMatrix3
    fun transformVector(vector: IVector, result: Vector): Vector {
        val vx = vector.x
        val vy = vector.y
        return result.set(m00 * vx + m10 * vy, m01 * vx + m11 * vy)
    }

    /**
     * {@inheritDoc} This uses the iterative polar decomposition algorithm described by
     * [Ken
       * Shoemake](http://www.cs.wisc.edu/graphics/Courses/838-s2002/Papers/polar-decomp.pdf).
     */
    override // from IMatrix3
    fun extractRotation(): Float {
        // start with the contents of the upper 2x2 portion of the matrix
        var n00 = m00
        var n10 = m10
        var n01 = m01
        var n11 = m11
        for (ii in 0..9) {
            // store the results of the previous iteration
            val o00 = n00
            val o10 = n10
            val o01 = n01
            val o11 = n11

            // compute average of the matrix with its inverse transpose
            val det = o00 * o11 - o10 * o01
            if (Math.abs(det) == 0f) {
                // determinant is zero; matrix is not invertible
                throw SingularMatrixException(this.toString())
            }
            val hrdet = 0.5f / det
            n00 = +o11 * hrdet + o00 * 0.5f
            n10 = -o01 * hrdet + o10 * 0.5f

            n01 = -o10 * hrdet + o01 * 0.5f
            n11 = +o00 * hrdet + o11 * 0.5f

            // compute the difference; if it's small enough, we're done
            val d00 = n00 - o00
            val d10 = n10 - o10
            val d01 = n01 - o01
            val d11 = n11 - o11
            if (d00 * d00 + d10 * d10 + d01 * d01 + d11 * d11 < MathUtil.EPSILON) {
                break
            }
        }
        // now that we have a nice orthogonal matrix, we can extract the rotation
        return FloatMath.atan2(n01, n00)
    }

    override // from IMatrix3
    fun extractScale(): Vector {
        return extractScale(Vector())
    }

    override // from IMatrix3
    fun extractScale(result: Vector): Vector {
        val m00 = this.m00
        val m01 = this.m01
        val m10 = this.m10
        val m11 = this.m11
        return result.set(FloatMath.sqrt(m00 * m00 + m01 * m01),
                FloatMath.sqrt(m10 * m10 + m11 * m11))
    }

    override // from IMatrix3
    fun approximateUniformScale(): Float {
        val cp = m00 * m11 - m01 * m10
        return if (cp < 0f) -FloatMath.sqrt(-cp) else FloatMath.sqrt(cp)
    }

    override fun toString(): String {
        return "[[" + m00 + ", " + m10 + ", " + m20 + "], " +
                "[" + m01 + ", " + m11 + ", " + m21 + "], " +
                "[" + m02 + ", " + m12 + ", " + m22 + "]]"
    }

    override fun hashCode(): Int {
        return Platform.hashCode(m00) xor Platform.hashCode(m10) xor Platform.hashCode(m20) xor
                Platform.hashCode(m01) xor Platform.hashCode(m11) xor Platform.hashCode(m21) xor
                Platform.hashCode(m02) xor Platform.hashCode(m12) xor Platform.hashCode(m22)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Matrix3) {
            return false
        }
        val omat = other
        return m00 == omat.m00 && m10 == omat.m10 && m20 == omat.m20 &&
                m01 == omat.m01 && m11 == omat.m11 && m21 == omat.m21 &&
                m02 == omat.m02 && m12 == omat.m12 && m22 == omat.m22
    }

    companion object {
        private const val serialVersionUID = 2090355290484132872L

        /** the identity matrix.  */
        val IDENTITY = Matrix3()
    }
}
