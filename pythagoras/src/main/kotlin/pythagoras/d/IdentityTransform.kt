//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.d

/**
 * Implements the identity transform.
 */
class IdentityTransform : AbstractTransform() {

    override // from Transform
    fun uniformScale(): Double {
        return 1.0
    }

    override // from Transform
    fun scaleX(): Double {
        return 1.0
    }

    override // from Transform
    fun scaleY(): Double {
        return 1.0
    }

    override // from Transform
    fun rotation(): Double {
        return 0.0
    }

    override // from Transform
    fun tx(): Double {
        return 0.0
    }

    override // from Transform
    fun ty(): Double {
        return 0.0
    }

    override // from Transform
    fun get(matrix: DoubleArray) {
        matrix[0] = 1.0
        matrix[1] = 0.0
        matrix[2] = 0.0
        matrix[3] = 1.0
        matrix[4] = 0.0
        matrix[5] = 0.0
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
    fun lerp(other: Transform, t: Double): Transform {
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
    fun transform(src: DoubleArray, srcOff: Int, dst: DoubleArray, dstOff: Int, count: Int) {
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
