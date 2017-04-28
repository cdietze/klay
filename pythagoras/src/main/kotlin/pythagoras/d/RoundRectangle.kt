//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.d

import java.io.Serializable

/**
 * Represents a rectangle with rounded corners, defined by an arc width and height.
 */
class RoundRectangle : AbstractRoundRectangle, Serializable {

    /** The x-coordinate of the framing rectangle.  */
    var x: Double = 0.toDouble()

    /** The y-coordinate of the framing rectangle.  */
    var y: Double = 0.toDouble()

    /** The width of the framing rectangle.  */
    var width: Double = 0.toDouble()

    /** The height of the framing rectangle.  */
    var height: Double = 0.toDouble()

    /** The width of the arc that defines the rounded corners.  */
    var arcwidth: Double = 0.toDouble()

    /** The height of the arc that defines the rounded corners.  */
    var archeight: Double = 0.toDouble()

    /**
     * Creates a rounded rectangle with frame (0x0+0+0) and corners of size (0x0).
     */
    constructor() {}

    /**
     * Creates a rounded rectangle with the specified frame and corner dimensions.
     */
    constructor(x: Double, y: Double, width: Double, height: Double,
                arcwidth: Double, archeight: Double) {
        setRoundRect(x, y, width, height, arcwidth, archeight)
    }

    /**
     * Sets the frame and corner dimensions of this rectangle to the specified values.
     */
    fun setRoundRect(x: Double, y: Double, width: Double, height: Double,
                     arcwidth: Double, archeight: Double) {
        this.x = x
        this.y = y
        this.width = width
        this.height = height
        this.arcwidth = arcwidth
        this.archeight = archeight
    }

    /**
     * Sets the frame and corner dimensions of this rectangle to be equal to those of the supplied
     * rectangle.
     */
    fun setRoundRect(rr: IRoundRectangle) {
        setRoundRect(rr.x(), rr.y(), rr.width(), rr.height(),
                rr.arcWidth(), rr.arcHeight())
    }

    override // from interface IRoundRectangle
    fun arcWidth(): Double {
        return arcwidth
    }

    override // from interface IRoundRectangle
    fun arcHeight(): Double {
        return archeight
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

    override // from RoundRectangle
    fun setFrame(x: Double, y: Double, width: Double, height: Double) {
        setRoundRect(x, y, width, height, arcwidth, archeight)
    }

    companion object {
        private const val serialVersionUID = -8496388509757573705L
    }
}
