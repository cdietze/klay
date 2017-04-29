//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

/**
 * Represents a circle on a plane.
 */
class Circle : AbstractCircle {

    /** The x-coordinate of the circle.  */
    override var x: Float = 0.toFloat()

    /** The y-coordinate of the circle.  */
    override var y: Float = 0.toFloat()

    /** The radius of the circle.  */
    override var radius: Float = 0.toFloat()

    /**
     * Constructs a circle at (0, 0) with radius 0
     */
    constructor() {}

    /**
     * Constructs a circle with the specified properties
     */
    constructor(x: Float, y: Float, radius: Float) {
        set(x, y, radius)
    }

    /**
     * Constructs a circle with the specified properties
     */
    constructor(p: XY, radius: Float) : this(p.x, p.y, radius) {}

    /**
     * Constructs a circle with properties equal to the supplied circle.
     */
    constructor(c: ICircle) : this(c.x, c.y, c.radius) {}

    /** Sets the properties of this circle to be equal to those of the supplied circle.
     * @return a reference to this this, for chaining.
     */
    fun set(c: ICircle): Circle {
        return set(c.x, c.y, c.radius)
    }

    /** Sets the properties of this circle to the supplied values.
     * @return a reference to this this, for chaining.
     */
    operator fun set(x: Float, y: Float, radius: Float): Circle {
        this.x = x
        this.y = y
        this.radius = radius
        return this
    }

    companion object {
        private const val serialVersionUID = -4841212861047390886L
    }
}
