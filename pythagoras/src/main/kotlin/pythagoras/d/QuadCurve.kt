//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.d

import java.io.Serializable

/**
 * Represents a quadratic curve.
 */
class QuadCurve : AbstractQuadCurve, Serializable {

    /** The x-coordinate of the start of this curve.  */
    var x1: Double = 0.toDouble()

    /** The y-coordinate of the start of this curve.  */
    var y1: Double = 0.toDouble()

    /** The x-coordinate of the control point.  */
    var ctrlx: Double = 0.toDouble()

    /** The y-coordinate of the control point.  */
    var ctrly: Double = 0.toDouble()

    /** The x-coordinate of the end of this curve.  */
    var x2: Double = 0.toDouble()

    /** The y-coordinate of the end of this curve.  */
    var y2: Double = 0.toDouble()

    /**
     * Creates a quad curve with all points at (0,0).
     */
    constructor() {}

    /**
     * Creates a quad curve with the specified start, control, and end points.
     */
    constructor(x1: Double, y1: Double, ctrlx: Double, ctrly: Double, x2: Double, y2: Double) {
        setCurve(x1, y1, ctrlx, ctrly, x2, y2)
    }

    /**
     * Configures the start, control, and end points for this curve.
     */
    fun setCurve(x1: Double, y1: Double, ctrlx: Double, ctrly: Double, x2: Double, y2: Double) {
        this.x1 = x1
        this.y1 = y1
        this.ctrlx = ctrlx
        this.ctrly = ctrly
        this.x2 = x2
        this.y2 = y2
    }

    /**
     * Configures the start, control, and end points for this curve.
     */
    fun setCurve(p1: XY, cp: XY, p2: XY) {
        setCurve(p1.x(), p1.y(), cp.x(), cp.y(), p2.x(), p2.y())
    }

    /**
     * Configures the start, control, and end points for this curve, using the values at the
     * specified offset in the `coords` array.
     */
    fun setCurve(coords: DoubleArray, offset: Int) {
        setCurve(coords[offset + 0], coords[offset + 1],
                coords[offset + 2], coords[offset + 3],
                coords[offset + 4], coords[offset + 5])
    }

    /**
     * Configures the start, control, and end points for this curve, using the values at the
     * specified offset in the `points` array.
     */
    fun setCurve(points: Array<XY>, offset: Int) {
        setCurve(points[offset + 0].x(), points[offset + 0].y(),
                points[offset + 1].x(), points[offset + 1].y(),
                points[offset + 2].x(), points[offset + 2].y())
    }

    /**
     * Configures the start, control, and end points for this curve to be the same as the supplied
     * curve.
     */
    fun setCurve(curve: IQuadCurve) {
        setCurve(curve.x1(), curve.y1(), curve.ctrlX(), curve.ctrlY(),
                curve.x2(), curve.y2())
    }

    override // from interface IQuadCurve
    fun x1(): Double {
        return x1
    }

    override // from interface IQuadCurve
    fun y1(): Double {
        return y1
    }

    override // from interface IQuadCurve
    fun ctrlX(): Double {
        return ctrlx
    }

    override // from interface IQuadCurve
    fun ctrlY(): Double {
        return ctrly
    }

    override // from interface IQuadCurve
    fun x2(): Double {
        return x2
    }

    override // from interface IQuadCurve
    fun y2(): Double {
        return y2
    }

    companion object {
        private const val serialVersionUID = -7816504376651508296L
    }
}
