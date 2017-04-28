//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

import java.io.Serializable

/**
 * Represents a rectangle with rounded corners, defined by an arc width and height.
 */
class RoundRectangle : AbstractRoundRectangle, Serializable {

    /** The x-coordinate of the framing rectangle.  */
    var x: Float = 0.toFloat()

    /** The y-coordinate of the framing rectangle.  */
    var y: Float = 0.toFloat()

    /** The width of the framing rectangle.  */
    var width: Float = 0.toFloat()

    /** The height of the framing rectangle.  */
    var height: Float = 0.toFloat()

    /** The width of the arc that defines the rounded corners.  */
    var arcwidth: Float = 0.toFloat()

    /** The height of the arc that defines the rounded corners.  */
    var archeight: Float = 0.toFloat()

    /**
     * Creates a rounded rectangle with frame (0x0+0+0) and corners of size (0x0).
     */
    constructor() {}

    /**
     * Creates a rounded rectangle with the specified frame and corner dimensions.
     */
    constructor(x: Float, y: Float, width: Float, height: Float,
                arcwidth: Float, archeight: Float) {
        setRoundRect(x, y, width, height, arcwidth, archeight)
    }

    /**
     * Sets the frame and corner dimensions of this rectangle to the specified values.
     */
    fun setRoundRect(x: Float, y: Float, width: Float, height: Float,
                     arcwidth: Float, archeight: Float) {
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
    fun arcWidth(): Float {
        return arcwidth
    }

    override // from interface IRoundRectangle
    fun arcHeight(): Float {
        return archeight
    }

    override // from interface IRectangularShape
    fun x(): Float {
        return x
    }

    override // from interface IRectangularShape
    fun y(): Float {
        return y
    }

    override // from interface IRectangularShape
    fun width(): Float {
        return width
    }

    override // from interface IRectangularShape
    fun height(): Float {
        return height
    }

    override // from RoundRectangle
    fun setFrame(x: Float, y: Float, width: Float, height: Float) {
        setRoundRect(x, y, width, height, arcwidth, archeight)
    }

    companion object {
        private const val serialVersionUID = 5850741513376725608L
    }
}
