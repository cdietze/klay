//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.d

/**
 * [Transform] related utility methods.
 */
object Transforms {
    /**
     * Creates and returns a new shape that is the supplied shape transformed by this transform's
     * matrix.
     */
    fun createTransformedShape(t: Transform, src: IShape?): IShape? {
        if (src == null) {
            return null
        }
        if (src is Path) {
            return src.createTransformedShape(t)
        }
        val path = src.pathIterator(t)
        val dst = Path(path.windingRule())
        dst.append(path, false)
        return dst
    }

    /**
     * Multiplies the supplied two affine transforms, storing the result in `into`. `into` may refer to the same instance as `a` or `b`.
     * @return `into` for chaining.
     */
    fun <T : Transform> multiply(a: AffineTransform, b: AffineTransform, into: T): T {
        return multiply(a.m00, a.m01, a.m10, a.m11, a.tx, a.ty,
                b.m00, b.m01, b.m10, b.m11, b.tx, b.ty, into)
    }

    /**
     * Multiplies the supplied two affine transforms, storing the result in `into`. `into` may refer to the same instance as `a`.
     * @return `into` for chaining.
     */
    fun <T : Transform> multiply(
            a: AffineTransform, m00: Double, m01: Double, m10: Double, m11: Double, tx: Double, ty: Double, into: T): T {
        return multiply(a.m00, a.m01, a.m10, a.m11, a.tx, a.ty, m00, m01, m10, m11, tx, ty, into)
    }

    /**
     * Multiplies the supplied two affine transforms, storing the result in `into`. `into` may refer to the same instance as `b`.
     * @return `into` for chaining.
     */
    fun <T : Transform> multiply(
            m00: Double, m01: Double, m10: Double, m11: Double, tx: Double, ty: Double, b: AffineTransform, into: T): T {
        return multiply(m00, m01, m10, m11, tx, ty, b.m00, b.m01, b.m10, b.m11, b.tx, b.ty, into)
    }

    /**
     * Multiplies the supplied two affine transforms, storing the result in `into`.
     * @return `into` for chaining.
     */
    fun <T : Transform> multiply(
            am00: Double, am01: Double, am10: Double, am11: Double, atx: Double, aty: Double,
            bm00: Double, bm01: Double, bm10: Double, bm11: Double, btx: Double, bty: Double, into: T): T {
        into.setTransform(am00 * bm00 + am10 * bm01,
                am01 * bm00 + am11 * bm01,
                am00 * bm10 + am10 * bm11,
                am01 * bm10 + am11 * bm11,
                am00 * btx + am10 * bty + atx,
                am01 * btx + am11 * bty + aty)
        return into
    }
}
