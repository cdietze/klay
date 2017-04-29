//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

/**
 * Implements the identity transform.
 */
class IdentityTransform : AbstractTransform() {

    override // from Transform
    val uniformScale: Float
        get() {
            return 1f
        }

    override // from Transform
    val scaleX: Float
        get() {
            return 1f
        }

    override // from Transform
    val scaleY: Float
        get() {
            return 1f
        }

    override // from Transform
    val rotation: Float
        get() {
            return 0f
        }

    override // from Transform
    val tx: Float
        get() {
            return 0f
        }

    override // from Transform
    val ty: Float
        get() {
            return 0f
        }

    override // from Transform
    fun get(matrix: FloatArray) {
        matrix[0] = 1f
        matrix[1] = 0f
        matrix[2] = 0f
        matrix[3] = 1f
        matrix[4] = 0f
        matrix[5] = 0f
    }

    override // from Transform
    fun invert(): Transform {
        return this
    }

    override // from Transform
    fun concatenate(other: Transform): Transform {
        return other
    }

    override // from Transform
    fun preConcatenate(other: Transform): Transform {
        return other
    }

    override // from Transform
    fun lerp(other: Transform, t: Float): Transform {
        throw UnsupportedOperationException() // TODO
    }

    override // from Transform
    fun transform(p: IPoint, into: Point): Point {
        return into.set(p)
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
    fun transform(src: FloatArray, srcOff: Int, dst: FloatArray, dstOff: Int, count: Int) {
        var srcOff = srcOff
        var dstOff = dstOff
        for (ii in 0..count - 1) {
            dst[dstOff++] = src[srcOff++]
        }
    }

    override // from Transform
    fun inverseTransform(p: IPoint, into: Point): Point {
        return into.set(p)
    }

    override // from Transform
    fun transformPoint(v: IVector, into: Vector): Vector {
        return into.set(v)
    }

    override // from Transform
    fun transform(v: IVector, into: Vector): Vector {
        return into.set(v)
    }

    override // from Transform
    fun inverseTransform(v: IVector, into: Vector): Vector {
        return into.set(v)
    }

    override // from Transform
    fun copy(): IdentityTransform {
        return this
    }

    override // from Transform
    fun generality(): Int {
        return GENERALITY
    }

    override fun toString(): String {
        return "ident"
    }

    companion object {
        /** Identifies the identity transform in [.generality].  */
        val GENERALITY = 0
    }
}
