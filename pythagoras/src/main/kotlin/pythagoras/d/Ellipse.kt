//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.d

import java.io.Serializable

/**
 * Represents an ellipse that is described by a framing rectangle.
 */
class Ellipse : AbstractEllipse, Serializable {

    /** The x-coordinate of the framing rectangle.  */
    var x: Double = 0.toDouble()

    /** The y-coordinate of the framing rectangle.  */
    var y: Double = 0.toDouble()

    /** The width of the framing rectangle.  */
    var width: Double = 0.toDouble()

    /** The height of the framing rectangle.  */
    var height: Double = 0.toDouble()

    /**
     * Creates an ellipse with framing rectangle (0x0+0+0).
     */
    constructor() {}

    /**
     * Creates an ellipse with the specified framing rectangle.
     */
    constructor(x: Double, y: Double, width: Double, height: Double) {
        setFrame(x, y, width, height)
    }

    override // from interface IRectangularShape
    fun x(): Double {
        return x
    }

    override // from interface IRectangularShape
    fun y(): Double {
        return y
    }

    override // from interface IRectangularShape
    fun width(): Double {
        return width
    }

    override // from interface IRectangularShape
    fun height(): Double {
        return height
    }

    override // from RectangularShape
    fun setFrame(x: Double, y: Double, width: Double, height: Double) {
        this.x = x
        this.y = y
        this.width = width
        this.height = height
    }

    companion object {
        private const val serialVersionUID = -2681903285662523175L
    }
}
