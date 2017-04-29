//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

/**
 * Represents an ellipse that is described by a framing rectangle.
 */
class Ellipse : AbstractEllipse {

    /** The x-coordinate of the framing rectangle.  */
    override var x: Float = 0.toFloat()

    /** The y-coordinate of the framing rectangle.  */
    override var y: Float = 0.toFloat()

    /** The width of the framing rectangle.  */
    override var width: Float = 0.toFloat()

    /** The height of the framing rectangle.  */
    override var height: Float = 0.toFloat()

    /**
     * Creates an ellipse with framing rectangle (0x0+0+0).
     */
    constructor() {}

    /**
     * Creates an ellipse with the specified framing rectangle.
     */
    constructor(x: Float, y: Float, width: Float, height: Float) {
        setFrame(x, y, width, height)
    }

    override // from RectangularShape
    fun setFrame(x: Float, y: Float, width: Float, height: Float) {
        this.x = x
        this.y = y
        this.width = width
        this.height = height
    }

    companion object {
        private const val serialVersionUID = -1205529661373764424L
    }
}
