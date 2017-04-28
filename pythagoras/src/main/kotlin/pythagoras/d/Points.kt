//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.d

/**
 * Point-related utility methods.
 */
object Points {
    /** The point at the origin.  */
    val ZERO: IPoint = Point(0.0, 0.0)

    /**
     * Returns the squared Euclidean distance between the specified two points.
     */
    fun distanceSq(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        var x2 = x2
        var y2 = y2
        x2 -= x1
        y2 -= y1
        return x2 * x2 + y2 * y2
    }

    /**
     * Returns the Euclidean distance between the specified two points.
     */
    fun distance(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        return Math.sqrt(distanceSq(x1, y1, x2, y2))
    }

    /**
     * Returns true if the supplied points' x and y components are equal to one another within
     * `epsilon`.
     */
    @JvmOverloads fun epsilonEquals(p1: IPoint, p2: IPoint, epsilon: Double = MathUtil.EPSILON): Boolean {
        return Math.abs(p1.x() - p2.x()) < epsilon && Math.abs(p1.y() - p2.y()) < epsilon
    }

    /** Transforms a point as specified, storing the result in the point provided.
     * @return a reference to the result point, for chaining.
     */
    fun transform(x: Double, y: Double, sx: Double, sy: Double, rotation: Double,
                  tx: Double, ty: Double, result: Point): Point {
        return transform(x, y, sx, sy, Math.sin(rotation), Math.cos(rotation), tx, ty,
                result)
    }

    /** Transforms a point as specified, storing the result in the point provided.
     * @return a reference to the result point, for chaining.
     */
    fun transform(x: Double, y: Double, sx: Double, sy: Double, sina: Double, cosa: Double,
                  tx: Double, ty: Double, result: Point): Point {
        return result.set((x * cosa - y * sina) * sx + tx, (x * sina + y * cosa) * sy + ty)
    }

    /** Inverse transforms a point as specified, storing the result in the point provided.
     * @return a reference to the result point, for chaining.
     */
    fun inverseTransform(x: Double, y: Double, sx: Double, sy: Double, rotation: Double,
                         tx: Double, ty: Double, result: Point): Point {
        var x = x
        var y = y
        x -= tx
        y -= ty // untranslate
        val sinnega = Math.sin(-rotation)
        val cosnega = Math.cos(-rotation)
        val nx = x * cosnega - y * sinnega // unrotate
        val ny = x * sinnega + y * cosnega
        return result.set(nx / sx, ny / sy) // unscale
    }

    /**
     * Returns a string describing the supplied point, of the form `+x+y`,
     * `+x-y`, `-x-y`, etc.
     */
    fun pointToString(x: Double, y: Double): String {
        return MathUtil.toString(x) + MathUtil.toString(y)
    }
}
/**
 * Returns true if the supplied points' x and y components are equal to one another within
 * [MathUtil.EPSILON].
 */
