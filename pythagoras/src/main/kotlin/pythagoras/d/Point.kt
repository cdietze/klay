//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.d

import java.io.Serializable

/**
 * Represents a point on a plane.
 */
class Point : AbstractPoint, Serializable {

    /** The x-coordinate of the point.  */
    var x: Double = 0.toDouble()

    /** The y-coordinate of the point.  */
    var y: Double = 0.toDouble()

    /**
     * Constructs a point at (0, 0).
     */
    constructor() {}

    /**
     * Constructs a point at the specified coordinates.
     */
    constructor(x: Double, y: Double) {
        set(x, y)
    }

    /**
     * Constructs a point with coordinates equal to the supplied point.
     */
    constructor(p: XY) {
        set(p.x(), p.y())
    }

    /** Sets the coordinates of this point to be equal to those of the supplied point.
     * @return a reference to this this, for chaining.
     */
    fun set(p: XY): Point {
        return set(p.x(), p.y())
    }

    /** Sets the coordinates of this point to the supplied values.
     * @return a reference to this this, for chaining.
     */
    operator fun set(x: Double, y: Double): Point {
        this.x = x
        this.y = y
        return this
    }

    /** Multiplies this point by a scale factor.
     * @return a a reference to this point, for chaining.
     */
    fun multLocal(s: Double): Point {
        return mult(s, this)
    }

    /** Translates this point by the specified offset.
     * @return a reference to this point, for chaining.
     */
    fun addLocal(dx: Double, dy: Double): Point {
        return add(dx, dy, this)
    }

    /** Rotates this point in-place by the specified angle.
     * @return a reference to this point, for chaining.
     */
    fun rotateLocal(angle: Double): Point {
        return rotate(angle, this)
    }

    /** Subtracts the supplied x/y from this point.
     * @return a reference to this point, for chaining.
     */
    fun subtractLocal(x: Double, y: Double): Point {
        return subtract(x, y, this)
    }

    override // from XY
    fun x(): Double {
        return x
    }

    override // from XY
    fun y(): Double {
        return y
    }

    companion object {
        private const val serialVersionUID = 4524700003415412445L
    }
}
