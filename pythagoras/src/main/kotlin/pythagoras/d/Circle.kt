//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.d

import java.io.Serializable

/**
 * Represents a circle on a plane.
 */
class Circle : AbstractCircle, Serializable {

    /** The x-coordinate of the circle.  */
    var x: Double = 0.toDouble()

    /** The y-coordinate of the circle.  */
    var y: Double = 0.toDouble()

    /** The radius of the circle.  */
    var radius: Double = 0.toDouble()

    /**
     * Constructs a circle at (0, 0) with radius 0
     */
    constructor() {}

    /**
     * Constructs a circle with the specified properties
     */
    constructor(x: Double, y: Double, radius: Double) {
        set(x, y, radius)
    }

    /**
     * Constructs a circle with the specified properties
     */
    constructor(p: XY, radius: Double) : this(p.x(), p.y(), radius) {}

    /**
     * Constructs a circle with properties equal to the supplied circle.
     */
    constructor(c: ICircle) : this(c.x(), c.y(), c.radius()) {}

    /** Sets the properties of this circle to be equal to those of the supplied circle.
     * @return a reference to this this, for chaining.
     */
    fun set(c: ICircle): Circle {
        return set(c.x(), c.y(), c.radius())
    }

    /** Sets the properties of this circle to the supplied values.
     * @return a reference to this this, for chaining.
     */
    operator fun set(x: Double, y: Double, radius: Double): Circle {
        this.x = x
        this.y = y
        this.radius = radius
        return this
    }

    override fun x(): Double {
        return x
    }

    override fun y(): Double {
        return y
    }

    override fun radius(): Double {
        return radius
    }

    companion object {
        private const val serialVersionUID = -3344650739420164686L
    }
}
