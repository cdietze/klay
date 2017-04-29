//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.i

/**
 * Represents a point on a plane.
 */
class Point
/**
 * Constructs a point at the specified coordinates.
 */
constructor(
        /** The x-coordinate of the point.  */
        override var x: Int = 0,
        /** The y-coordinate of the point.  */
        override var y: Int = 0

) : AbstractPoint() {

    /**
     * Constructs a point with coordinates equal to the supplied point.
     */
    constructor(p: IPoint) : this(p.x, p.y)

    /**
     * Sets the coordinates of this point to be equal to those of the supplied point.
     */
    fun setLocation(p: IPoint) {
        setLocation(p.x, p.y)
    }

    /**
     * Sets the coordinates of this point to the supplied values.
     */
    fun setLocation(x: Int, y: Int) {
        this.x = x
        this.y = y
    }

    /**
     * A synonym for [.setLocation].
     */
    fun move(x: Int, y: Int) {
        setLocation(x, y)
    }

    /**
     * Translates this point by the specified offset.
     */
    fun translate(dx: Int, dy: Int) {
        x += dx
        y += dy
    }

    /** Sets the coordinates of this point to be equal to those of the supplied point.
     * @return a reference to this this, for chaining.
     */
    fun set(p: IPoint): Point {
        return set(p.x, p.y)
    }

    /** Sets the coordinates of this point to the supplied values.
     * @return a reference to this this, for chaining.
     */
    operator fun set(x: Int, y: Int): Point {
        this.x = x
        this.y = y
        return this
    }

    /** Translates this point by the specified offset.
     * @return a reference to this point, for chaining.
     */
    fun addLocal(dx: Int, dy: Int): Point {
        return add(dx, dy, this)
    }

    /** Subtracts the supplied x/y from this point.
     * @return a reference to this point, for chaining.
     */
    fun subtractLocal(x: Int, y: Int): Point {
        return subtract(x, y, this)
    }

    companion object {
        private const val serialVersionUID = -6346341779228562585L
    }
}
