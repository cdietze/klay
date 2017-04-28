//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.d

import java.io.Serializable

/**
 * Represents a line segment.
 */
class Line : AbstractLine, Serializable {

    /** The x-coordinate of the start of this line segment.  */
    var x1: Double = 0.toDouble()

    /** The y-coordinate of the start of this line segment.  */
    var y1: Double = 0.toDouble()

    /** The x-coordinate of the end of this line segment.  */
    var x2: Double = 0.toDouble()

    /** The y-coordinate of the end of this line segment.  */
    var y2: Double = 0.toDouble()

    /**
     * Creates a line from (0,0) to (0,0).
     */
    constructor() {}

    /**
     * Creates a line from (x1,y1), to (x2,y2).
     */
    constructor(x1: Double, y1: Double, x2: Double, y2: Double) {
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
    fun setLine(x1: Double, y1: Double, x2: Double, y2: Double) {
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
    fun x1(): Double {
        return x1
    }

    override // from interface ILine
    fun y1(): Double {
        return y1
    }

    override // from interface ILine
    fun x2(): Double {
        return x2
    }

    override // from interface ILine
    fun y2(): Double {
        return y2
    }

    companion object {
        private const val serialVersionUID = -9086971085479796688L
    }
}
