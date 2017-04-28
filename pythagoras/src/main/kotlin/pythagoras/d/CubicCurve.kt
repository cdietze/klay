//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.d

import java.io.Serializable

/**
 * Represents a cubic curve.
 */
class CubicCurve : AbstractCubicCurve, Serializable {

    /** The x-coordinate of the start of this curve.  */
    var x1: Double = 0.toDouble()

    /** The y-coordinate of the start of this curve.  */
    var y1: Double = 0.toDouble()

    /** The x-coordinate of the first control point.  */
    var ctrlx1: Double = 0.toDouble()

    /** The y-coordinate of the first control point.  */
    var ctrly1: Double = 0.toDouble()

    /** The x-coordinate of the second control point.  */
    var ctrlx2: Double = 0.toDouble()

    /** The x-coordinate of the second control point.  */
    var ctrly2: Double = 0.toDouble()

    /** The x-coordinate of the end of this curve.  */
    var x2: Double = 0.toDouble()

    /** The y-coordinate of the end of this curve.  */
    var y2: Double = 0.toDouble()

    /**
     * Creates a cubic curve with all points at (0,0).
     */
    constructor() {}

    /**
     * Creates a cubic curve with the specified start, control, and end points.
     */
    constructor(x1: Double, y1: Double, ctrlx1: Double, ctrly1: Double,
                ctrlx2: Double, ctrly2: Double, x2: Double, y2: Double) {
        setCurve(x1, y1, ctrlx1, ctrly1, ctrlx2, ctrly2, x2, y2)
    }

    /**
     * Configures the start, control and end points for this curve.
     */
    fun setCurve(x1: Double, y1: Double, ctrlx1: Double, ctrly1: Double, ctrlx2: Double,
                 ctrly2: Double, x2: Double, y2: Double) {
        this.x1 = x1
        this.y1 = y1
        this.ctrlx1 = ctrlx1
        this.ctrly1 = ctrly1
        this.ctrlx2 = ctrlx2
        this.ctrly2 = ctrly2
        this.x2 = x2
        this.y2 = y2
    }

    /**
     * Configures the start, control and end points for this curve.
     */
    fun setCurve(p1: XY, cp1: XY, cp2: XY, p2: XY) {
        setCurve(p1.x(), p1.y(), cp1.x(), cp1.y(),
                cp2.x(), cp2.y(), p2.x(), p2.y())
    }

    /**
     * Configures the start, control and end points for this curve, using the values at the
     * specified offset in the `coords` array.
     */
    fun setCurve(coords: DoubleArray, offset: Int) {
        setCurve(coords[offset + 0], coords[offset + 1], coords[offset + 2], coords[offset + 3],
                coords[offset + 4], coords[offset + 5], coords[offset + 6], coords[offset + 7])
    }

    /**
     * Configures the start, control and end points for this curve, using the values at the
     * specified offset in the `points` array.
     */
    fun setCurve(points: Array<XY>, offset: Int) {
        setCurve(points[offset + 0].x(), points[offset + 0].y(),
                points[offset + 1].x(), points[offset + 1].y(),
                points[offset + 2].x(), points[offset + 2].y(),
                points[offset + 3].x(), points[offset + 3].y())
    }

    /**
     * Configures the start, control and end points for this curve to be the same as the supplied
     * curve.
     */
    fun setCurve(curve: ICubicCurve) {
        setCurve(curve.x1(), curve.y1(), curve.ctrlX1(), curve.ctrlY1(),
                curve.ctrlX2(), curve.ctrlY2(), curve.x2(), curve.y2())
    }

    override // from interface ICubicCurve
    fun x1(): Double {
        return x1
    }

    override // from interface ICubicCurve
    fun y1(): Double {
        return y1
    }

    override // from interface ICubicCurve
    fun ctrlX1(): Double {
        return ctrlx1
    }

    override // from interface ICubicCurve
    fun ctrlY1(): Double {
        return ctrly1
    }

    override // from interface ICubicCurve
    fun ctrlX2(): Double {
        return ctrlx2
    }

    override // from interface ICubicCurve
    fun ctrlY2(): Double {
        return ctrly2
    }

    override // from interface ICubicCurve
    fun x2(): Double {
        return x2
    }

    override // from interface ICubicCurve
    fun y2(): Double {
        return y2
    }

    companion object {
        private const val serialVersionUID = 1344542230356205271L
    }
}
