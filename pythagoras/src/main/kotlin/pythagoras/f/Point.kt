//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

/**
 * Represents a point on a plane.
 */
class Point : AbstractPoint {

    /** The x-coordinate of the point.  */
    override var x: Float = 0.toFloat()

    /** The y-coordinate of the point.  */
    override var y: Float = 0.toFloat()

    /**
     * Constructs a point at (0, 0).
     */
    constructor() {}

    /**
     * Constructs a point at the specified coordinates.
     */
    constructor(x: Float, y: Float) {
        set(x, y)
    }

    /**
     * Constructs a point with coordinates equal to the supplied point.
     */
    constructor(p: XY) {
        set(p.x, p.y)
    }

    /** Sets the coordinates of this point to be equal to those of the supplied point.
     * @return a reference to this this, for chaining.
     */
    fun set(p: XY): Point {
        return set(p.x, p.y)
    }

    /** Sets the coordinates of this point to the supplied values.
     * @return a reference to this this, for chaining.
     */
    operator fun set(x: Float, y: Float): Point {
        this.x = x
        this.y = y
        return this
    }

    /** Multiplies this point by a scale factor.
     * @return a a reference to this point, for chaining.
     */
    fun multLocal(s: Float): Point {
        return mult(s, this)
    }

    /** Translates this point by the specified offset.
     * @return a reference to this point, for chaining.
     */
    fun addLocal(dx: Float, dy: Float): Point {
        return add(dx, dy, this)
    }

    /** Rotates this point in-place by the specified angle.
     * @return a reference to this point, for chaining.
     */
    fun rotateLocal(angle: Float): Point {
        return rotate(angle, this)
    }

    /** Subtracts the supplied x/y from this point.
     * @return a reference to this point, for chaining.
     */
    fun subtractLocal(x: Float, y: Float): Point {
        return subtract(x, y, this)
    }

    companion object {
        private const val serialVersionUID = -2666598890366249427L
    }
}
