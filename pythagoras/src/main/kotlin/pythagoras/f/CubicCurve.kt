//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

import java.io.Serializable

/**
 * Represents a cubic curve.
 */
class CubicCurve : AbstractCubicCurve, Serializable {

    /** The x-coordinate of the start of this curve.  */
    var x1: Float = 0.toFloat()

    /** The y-coordinate of the start of this curve.  */
    var y1: Float = 0.toFloat()

    /** The x-coordinate of the first control point.  */
    var ctrlx1: Float = 0.toFloat()

    /** The y-coordinate of the first control point.  */
    var ctrly1: Float = 0.toFloat()

    /** The x-coordinate of the second control point.  */
    var ctrlx2: Float = 0.toFloat()

    /** The x-coordinate of the second control point.  */
    var ctrly2: Float = 0.toFloat()

    /** The x-coordinate of the end of this curve.  */
    var x2: Float = 0.toFloat()

    /** The y-coordinate of the end of this curve.  */
    var y2: Float = 0.toFloat()

    /**
     * Creates a cubic curve with all points at (0,0).
     */
    constructor() {}

    /**
     * Creates a cubic curve with the specified start, control, and end points.
     */
    constructor(x1: Float, y1: Float, ctrlx1: Float, ctrly1: Float,
                ctrlx2: Float, ctrly2: Float, x2: Float, y2: Float) {
        setCurve(x1, y1, ctrlx1, ctrly1, ctrlx2, ctrly2, x2, y2)
    }

    /**
     * Configures the start, control and end points for this curve.
     */
    fun setCurve(x1: Float, y1: Float, ctrlx1: Float, ctrly1: Float, ctrlx2: Float,
                 ctrly2: Float, x2: Float, y2: Float) {
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
    fun setCurve(coords: FloatArray, offset: Int) {
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
    fun x1(): Float {
        return x1
    }

    override // from interface ICubicCurve
    fun y1(): Float {
        return y1
    }

    override // from interface ICubicCurve
    fun ctrlX1(): Float {
        return ctrlx1
    }

    override // from interface ICubicCurve
    fun ctrlY1(): Float {
        return ctrly1
    }

    override // from interface ICubicCurve
    fun ctrlX2(): Float {
        return ctrlx2
    }

    override // from interface ICubicCurve
    fun ctrlY2(): Float {
        return ctrly2
    }

    override // from interface ICubicCurve
    fun x2(): Float {
        return x2
    }

    override // from interface ICubicCurve
    fun y2(): Float {
        return y2
    }

    companion object {
        private const val serialVersionUID = -3306427309314031213L
    }
}
