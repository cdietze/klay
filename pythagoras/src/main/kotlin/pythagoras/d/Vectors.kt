//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.d

/**
 * Vector-related utility methods.
 */
object Vectors {
    /** A unit vector in the X+ direction.  */
    val UNIT_X: IVector = Vector(1.0, 0.0)

    /** A unit vector in the Y+ direction.  */
    val UNIT_Y: IVector = Vector(0.0, 1.0)

    /** The zero vector.  */
    val ZERO: IVector = Vector(0.0, 0.0)

    /** A vector containing the minimum doubleing point value for all components
     * (note: the components are -[Float.MAX_VALUE], not [Float.MIN_VALUE]).  */
    val MIN_VALUE: IVector = Vector((-java.lang.Float.MAX_VALUE).toDouble(), (-java.lang.Float.MAX_VALUE).toDouble())

    /** A vector containing the maximum doubleing point value for all components.  */
    val MAX_VALUE: IVector = Vector(java.lang.Float.MAX_VALUE.toDouble(), java.lang.Float.MAX_VALUE.toDouble())

    /**
     * Creates a new vector from polar coordinates.
     */
    fun fromPolar(magnitude: Double, angle: Double): Vector {
        return Vector(magnitude * Math.cos(angle), magnitude * Math.sin(angle))
    }

    /**
     * Creates a vector from `from` to `to`.
     */
    fun from(from: XY, to: XY): Vector {
        return Vector(to.x() - from.x(), to.y() - from.y())
    }

    /**
     * Returns the magnitude of the specified vector.
     */
    fun length(x: Double, y: Double): Double {
        return Math.sqrt(lengthSq(x, y))
    }

    /**
     * Returns the square of the magnitude of the specified vector.
     */
    fun lengthSq(x: Double, y: Double): Double {
        return x * x + y * y
    }

    /**
     * Returns true if the supplied vector has zero magnitude.
     */
    fun isZero(x: Double, y: Double): Boolean {
        return x == 0.0 && y == 0.0
    }

    /**
     * Returns true if the supplied vector's x and y components are `epsilon` close to zero
     * magnitude.
     */
    @JvmOverloads fun isEpsilonZero(x: Double, y: Double, epsilon: Double = MathUtil.EPSILON): Boolean {
        return Math.abs(x) <= epsilon && Math.abs(y) <= epsilon
    }

    /**
     * Returns true if the supplied vectors' x and y components are equal to one another within
     * `epsilon`.
     */
    @JvmOverloads fun epsilonEquals(v1: IVector, v2: IVector, epsilon: Double = MathUtil.EPSILON): Boolean {
        return Math.abs(v1.x() - v2.x()) <= epsilon && Math.abs(v1.y() - v2.y()) <= epsilon
    }

    /** Transforms a vector as specified (as a point, accounting for translation), storing the
     * result in the vector provided.
     * @return a reference to the result vector, for chaining.
     */
    fun transform(x: Double, y: Double, sx: Double, sy: Double, rotation: Double,
                  tx: Double, ty: Double, result: Vector): Vector {
        return transform(x, y, sx, sy, Math.sin(rotation), Math.cos(rotation), tx, ty,
                result)
    }

    /**
     * Transforms a vector as specified, storing the result in the vector provided.
     * @return a reference to the result vector, for chaining.
     */
    fun transform(x: Double, y: Double, sx: Double, sy: Double, rotation: Double,
                  result: Vector): Vector {
        return transform(x, y, sx, sy, Math.sin(rotation), Math.cos(rotation), result)
    }

    /**
     * Transforms a vector as specified, storing the result in the vector provided.
     * @return a reference to the result vector, for chaining.
     */
    fun transform(x: Double, y: Double, sx: Double, sy: Double, sina: Double, cosa: Double,
                  result: Vector): Vector {
        return result.set((x * cosa - y * sina) * sx, (x * sina + y * cosa) * sy)
    }

    /** Transforms a vector as specified (as a point, accounting for translation), storing the
     * result in the vector provided.
     * @return a reference to the result vector, for chaining.
     */
    fun transform(x: Double, y: Double, sx: Double, sy: Double, sina: Double, cosa: Double,
                  tx: Double, ty: Double, result: Vector): Vector {
        return result.set((x * cosa - y * sina) * sx + tx, (x * sina + y * cosa) * sy + ty)
    }

    /**
     * Inverse transforms a vector as specified, storing the result in the vector provided.
     * @return a reference to the result vector, for chaining.
     */
    fun inverseTransform(x: Double, y: Double, sx: Double, sy: Double, rotation: Double,
                         result: Vector): Vector {
        val sinnega = Math.sin(-rotation)
        val cosnega = Math.cos(-rotation)
        val nx = x * cosnega - y * sinnega // unrotate
        val ny = x * sinnega + y * cosnega
        return result.set(nx / sx, ny / sy) // unscale
    }

    /**
     * Returns a string describing the supplied vector, of the form `+x+y`,
     * `+x-y`, `-x-y`, etc.
     */
    fun vectorToString(x: Double, y: Double): String {
        return MathUtil.toString(x) + MathUtil.toString(y)
    }
}
/**
 * Returns true if the supplied vector's x and y components are [MathUtil.EPSILON] close
 * to zero magnitude.
 */
/**
 * Returns true if the supplied vectors' x and y components are equal to one another within
 * [MathUtil.EPSILON].
 */
