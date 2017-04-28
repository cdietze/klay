//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.d

import pythagoras.util.NoninvertibleTransformException

/**
 * Implements an affine (3x2 matrix) transform. The transformation matrix has the form:
 * <pre>`[ m00, m10, tx ]
 * [ m01, m11, ty ]
 * [   0,   0,  1 ]
`</pre> *
 */
class AffineTransform : AbstractTransform {

    /** The scale, rotation and shear components of this transform.  */
    var m00: Double = 0.toDouble()
    var m01: Double = 0.toDouble()
    var m10: Double = 0.toDouble()
    var m11: Double = 0.toDouble()

    /** The translation components of this transform.  */
    var tx: Double = 0.toDouble()
    var ty: Double = 0.toDouble()

    /** Creates an affine transform from the supplied scale, rotation and translation.  */
    constructor(scale: Double, angle: Double, tx: Double, ty: Double) : this(scale, scale, angle, tx, ty) {}

    /** Creates an affine transform from the supplied scale, rotation and translation.  */
    constructor(scaleX: Double, scaleY: Double, angle: Double, tx: Double, ty: Double) {
        val sina = Math.sin(angle)
        val cosa = Math.cos(angle)
        this.m00 = cosa * scaleX
        this.m01 = sina * scaleY
        this.m10 = -sina * scaleX
        this.m11 = cosa * scaleY
        this.tx = tx
        this.ty = ty
    }

    /** Creates an affine transform with the specified transform matrix.  */
    @JvmOverloads constructor(m00: Double = 1.0, m01: Double = 0.0, m10: Double = 0.0, m11: Double = 1.0, tx: Double = 0.0, ty: Double = 0.0) {
        this.m00 = m00
        this.m01 = m01
        this.m10 = m10
        this.m11 = m11
        this.tx = tx
        this.ty = ty
    }

    /** Sets this affine transform matrix to `other`.
     * @return this instance, for chaining.
     */
    fun set(other: AffineTransform): AffineTransform {
        return setTransform(other.m00, other.m01, other.m10, other.m11, other.tx, other.ty)
    }

    override // from Transform
    fun uniformScale(): Double {
        // the square root of the signed area of the parallelogram spanned by the axis vectors
        val cp = m00 * m11 - m01 * m10
        return if (cp < 0f) -Math.sqrt(-cp) else Math.sqrt(cp)
    }

    override // from Transform
    fun scaleX(): Double {
        return Math.sqrt(m00 * m00 + m01 * m01)
    }

    override // from Transform
    fun scaleY(): Double {
        return Math.sqrt(m10 * m10 + m11 * m11)
    }

    override // from Transform
    fun rotation(): Double {
        // use the iterative polar decomposition algorithm described by Ken Shoemake:
        // http://www.cs.wisc.edu/graphics/Courses/838-s2002/Papers/polar-decomp.pdf

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
            if (Math.abs(det) == 0.0) {
                // determinant is zero; matrix is not invertible
                throw NoninvertibleTransformException(this.toString())
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
        return Math.atan2(n01, n00)
    }

    override // from Transform
    fun tx(): Double {
        return this.tx
    }

    override // from Transform
    fun ty(): Double {
        return this.ty
    }

    override // from Transform
    fun get(matrix: DoubleArray) {
        matrix[0] = m00
        matrix[1] = m01
        matrix[2] = m10
        matrix[3] = m11
        matrix[4] = tx
        matrix[5] = ty
    }

    override // from Transform
    fun setUniformScale(scale: Double): AffineTransform {
        return setScale(scale, scale) as AffineTransform
    }

    override // from Transform
    fun setScaleX(scaleX: Double): AffineTransform {
        // normalize the scale to 1, then re-apply
        val mult = scaleX / scaleX()
        m00 *= mult
        m01 *= mult
        return this
    }

    override // from Transform
    fun setScaleY(scaleY: Double): AffineTransform {
        // normalize the scale to 1, then re-apply
        val mult = scaleY / scaleY()
        m10 *= mult
        m11 *= mult
        return this
    }

    override // from Transform
    fun setRotation(angle: Double): AffineTransform {
        // extract the scale, then reapply rotation and scale together
        val sx = scaleX()
        val sy = scaleY()
        val sina = Math.sin(angle)
        val cosa = Math.cos(angle)
        m00 = cosa * sx
        m01 = sina * sx
        m10 = -sina * sy
        m11 = cosa * sy
        return this
    }

    override // from Transform
    fun setTranslation(tx: Double, ty: Double): AffineTransform {
        this.tx = tx
        this.ty = ty
        return this
    }

    override // from Transform
    fun setTx(tx: Double): AffineTransform {
        this.tx = tx
        return this
    }

    override // from Transform
    fun setTy(ty: Double): AffineTransform {
        this.ty = ty
        return this
    }

    override // from Transform
    fun setTransform(m00: Double, m01: Double, m10: Double, m11: Double,
                     tx: Double, ty: Double): AffineTransform {
        this.m00 = m00
        this.m01 = m01
        this.m10 = m10
        this.m11 = m11
        this.tx = tx
        this.ty = ty
        return this
    }

    override // from Transform
    fun uniformScale(scale: Double): AffineTransform {
        return scale(scale, scale)
    }

    override // from Transform
    fun scale(scaleX: Double, scaleY: Double): AffineTransform {
        m00 *= scaleX
        m01 *= scaleX
        m10 *= scaleY
        m11 *= scaleY
        return this
    }

    override // from Transform
    fun scaleX(scaleX: Double): AffineTransform {
        return Transforms.multiply(this, scaleX, 0.0, 0.0, 1.0, 0.0, 0.0, this)
    }

    override // from Transform
    fun scaleY(scaleY: Double): AffineTransform {
        return Transforms.multiply(this, 1.0, 0.0, 0.0, scaleY, 0.0, 0.0, this)
    }

    override // from Transform
    fun rotate(angle: Double): AffineTransform {
        val sina = Math.sin(angle)
        val cosa = Math.cos(angle)
        return Transforms.multiply(this, cosa, sina, -sina, cosa, 0.0, 0.0, this)
    }

    override // from Transform
    fun translate(tx: Double, ty: Double): AffineTransform {
        this.tx += m00 * tx + m10 * ty
        this.ty += m11 * ty + m01 * tx
        return this
    }

    override // from Transform
    fun translateX(tx: Double): AffineTransform {
        return Transforms.multiply(this, 1.0, 0.0, 0.0, 1.0, tx, 0.0, this)
    }

    override // from Transform
    fun translateY(ty: Double): AffineTransform {
        return Transforms.multiply(this, 1.0, 0.0, 0.0, 1.0, 0.0, ty, this)
    }

    override // from Transform
    fun shear(sx: Double, sy: Double): AffineTransform {
        return Transforms.multiply(this, 1.0, sy, sx, 1.0, 0.0, 0.0, this)
    }

    override // from Transform
    fun shearX(sx: Double): AffineTransform {
        return Transforms.multiply(this, 1.0, 0.0, sx, 1.0, 0.0, 0.0, this)
    }

    override // from Transform
    fun shearY(sy: Double): AffineTransform {
        return Transforms.multiply(this, 1.0, sy, 0.0, 1.0, 0.0, 0.0, this)
    }

    override // from Transform
    fun invert(): AffineTransform {
        // compute the determinant, storing the subdeterminants for later use
        val det = m00 * m11 - m10 * m01
        if (Math.abs(det) == 0.0) {
            // determinant is zero; matrix is not invertible
            throw NoninvertibleTransformException(this.toString())
        }
        val rdet = 1f / det
        return AffineTransform(
                +m11 * rdet, -m10 * rdet,
                -m01 * rdet, +m00 * rdet,
                (m10 * ty - m11 * tx) * rdet, (m01 * tx - m00 * ty) * rdet)
    }

    override // from Transform
    fun concatenate(other: Transform): Transform {
        if (generality() < other.generality()) {
            return other.preConcatenate(this)
        }
        if (other is AffineTransform) {
            return Transforms.multiply(this, other, AffineTransform())
        } else {
            val oaff = AffineTransform(other)
            return Transforms.multiply(this, oaff, oaff)
        }
    }

    override // from Transform
    fun preConcatenate(other: Transform): Transform {
        if (generality() < other.generality()) {
            return other.concatenate(this)
        }
        if (other is AffineTransform) {
            return Transforms.multiply(other, this, AffineTransform())
        } else {
            val oaff = AffineTransform(other)
            return Transforms.multiply(oaff, this, oaff)
        }
    }

    override // from Transform
    fun lerp(other: Transform, t: Double): Transform {
        if (generality() < other.generality()) {
            return other.lerp(this, -t) // TODO: is this correct?
        }

        val ot = other as? AffineTransform ?: AffineTransform(other)
        return AffineTransform(
                m00 + t * (ot.m00 - m00), m01 + t * (ot.m01 - m01),
                m10 + t * (ot.m10 - m10), m11 + t * (ot.m11 - m11),
                tx + t * (ot.tx - tx), ty + t * (ot.ty - ty))
    }

    override // from Transform
    fun transform(p: IPoint, into: Point): Point {
        val x = p.x()
        val y = p.y()
        return into.set(m00 * x + m10 * y + tx, m01 * x + m11 * y + ty)
    }

    override // from Transform
    fun transform(src: Array<IPoint>, srcOff: Int, dst: Array<Point>, dstOff: Int, count: Int) {
        var srcOff = srcOff
        var dstOff = dstOff
        for (ii in 0..count - 1) {
            transform(src[srcOff++], dst[dstOff++])
        }
    }

    override // from Transform
    fun transform(src: DoubleArray, srcOff: Int, dst: DoubleArray, dstOff: Int, count: Int) {
        var srcOff = srcOff
        var dstOff = dstOff
        for (ii in 0..count - 1) {
            val x = src[srcOff++]
            val y = src[srcOff++]
            dst[dstOff++] = m00 * x + m10 * y + tx
            dst[dstOff++] = m01 * x + m11 * y + ty
        }
    }

    override // from Transform
    fun inverseTransform(p: IPoint, into: Point): Point {
        val x = p.x() - tx
        val y = p.y() - ty
        val det = m00 * m11 - m01 * m10
        if (Math.abs(det) == 0.0) {
            // determinant is zero; matrix is not invertible
            throw NoninvertibleTransformException(this.toString())
        }
        val rdet = 1 / det
        return into.set((x * m11 - y * m10) * rdet,
                (y * m00 - x * m01) * rdet)
    }

    override // from Transform
    fun transformPoint(v: IVector, into: Vector): Vector {
        val x = v.x()
        val y = v.y()
        return into.set(m00 * x + m10 * y + tx, m01 * x + m11 * y + ty)
    }

    override // from Transform
    fun transform(v: IVector, into: Vector): Vector {
        val x = v.x()
        val y = v.y()
        return into.set(m00 * x + m10 * y, m01 * x + m11 * y)
    }

    override // from Transform
    fun inverseTransform(v: IVector, into: Vector): Vector {
        val x = v.x()
        val y = v.y()
        val det = m00 * m11 - m01 * m10
        if (Math.abs(det) == 0.0) {
            // determinant is zero; matrix is not invertible
            throw NoninvertibleTransformException(this.toString())
        }
        val rdet = 1 / det
        return into.set((x * m11 - y * m10) * rdet,
                (y * m00 - x * m01) * rdet)
    }

    override // from Transform
    fun copy(): AffineTransform {
        return AffineTransform(m00, m01, m10, m11, tx, ty)
    }

    override // from Transform
    fun generality(): Int {
        return GENERALITY
    }

    override fun toString(): String {
        if (m00 != 1.0 || m01 != 0.0 || m10 != 0.0 || m11 != 1.0)
            return "affine [" +
                    MathUtil.toString(m00) + " " + MathUtil.toString(m01) + " " +
                    MathUtil.toString(m10) + " " + MathUtil.toString(m11) + " " + translation() + "]"
        else if (tx != 0.0 || ty != 0.0)
            return "trans " + translation()
        else
            return "ident"
    }

    // we don't publicize this because it might encourage someone to do something stupid like
    // create a new AffineTransform from another AffineTransform using this instead of copy()
    protected constructor(other: Transform) : this(other.scaleX(), other.scaleY(), other.rotation(),
            other.tx(), other.ty()) {
    }

    companion object {
        /** Identifies the affine transform in [.generality].  */
        val GENERALITY = 4
    }
}
/** Creates an affine transform configured with the identity transform.  */
