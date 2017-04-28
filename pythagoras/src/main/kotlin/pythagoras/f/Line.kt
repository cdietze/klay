//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

import java.io.Serializable

/**
 * Represents a line segment.
 */
class Line : AbstractLine, Serializable {

    /** The x-coordinate of the start of this line segment.  */
    var x1: Float = 0.toFloat()

    /** The y-coordinate of the start of this line segment.  */
    var y1: Float = 0.toFloat()

    /** The x-coordinate of the end of this line segment.  */
    var x2: Float = 0.toFloat()

    /** The y-coordinate of the end of this line segment.  */
    var y2: Float = 0.toFloat()

    /**
     * Creates a line from (0,0) to (0,0).
     */
    constructor() {}

    /**
     * Creates a line from (x1,y1), to (x2,y2).
     */
    constructor(x1: Float, y1: Float, x2: Float, y2: Float) {
        setLine(x1, y1, x2, y2)
    }

    /**
     * Creates a line from p1 to p2.
     */
    constructor(p1: XY, p2: XY) {
        setLine(p1, p2)
    }

    /**
     * Sets the start and end point of this line to the specified values.
     */
    fun setLine(x1: Float, y1: Float, x2: Float, y2: Float) {
        this.x1 = x1
        this.y1 = y1
        this.x2 = x2
        this.y2 = y2
    }

    /**
     * Sets the start and end of this line to the specified points.
     */
    fun setLine(p1: XY, p2: XY) {
        setLine(p1.x(), p1.y(), p2.x(), p2.y())
    }

    override // from interface ILine
    fun x1(): Float {
        return x1
    }

    override // from interface ILine
    fun y1(): Float {
        return y1
    }

    override // from interface ILine
    fun x2(): Float {
        return x2
    }

    override // from interface ILine
    fun y2(): Float {
        return y2
    }

    companion object {
        private const val serialVersionUID = -1771222822536940013L
    }
}
