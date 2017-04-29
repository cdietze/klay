//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

/**
 * Represents a quadratic curve.
 */
class QuadCurve : AbstractQuadCurve {

    /** The x-coordinate of the start of this curve.  */
    override var x1: Float = 0.toFloat()

    /** The y-coordinate of the start of this curve.  */
    override var y1: Float = 0.toFloat()

    /** The x-coordinate of the control point.  */
    override var ctrlX: Float = 0.toFloat()

    /** The y-coordinate of the control point.  */
    override var ctrlY: Float = 0.toFloat()

    /** The x-coordinate of the end of this curve.  */
    override var x2: Float = 0.toFloat()

    /** The y-coordinate of the end of this curve.  */
    override var y2: Float = 0.toFloat()

    /**
     * Creates a quad curve with all points at (0,0).
     */
    constructor() {}

    /**
     * Creates a quad curve with the specified start, control, and end points.
     */
    constructor(x1: Float, y1: Float, ctrlx: Float, ctrly: Float, x2: Float, y2: Float) {
        setCurve(x1, y1, ctrlx, ctrly, x2, y2)
    }

    /**
     * Configures the start, control, and end points for this curve.
     */
    fun setCurve(x1: Float, y1: Float, ctrlx: Float, ctrly: Float, x2: Float, y2: Float) {
        this.x1 = x1
        this.y1 = y1
        this.ctrlX = ctrlx
        this.ctrlY = ctrly
        this.x2 = x2
        this.y2 = y2
    }

    /**
     * Configures the start, control, and end points for this curve.
     */
    fun setCurve(p1: XY, cp: XY, p2: XY) {
        setCurve(p1.x, p1.y, cp.x, cp.y, p2.x, p2.y)
    }

    /**
     * Configures the start, control, and end points for this curve, using the values at the
     * specified offset in the `coords` array.
     */
    fun setCurve(coords: FloatArray, offset: Int) {
        setCurve(coords[offset + 0], coords[offset + 1],
                coords[offset + 2], coords[offset + 3],
                coords[offset + 4], coords[offset + 5])
    }

    /**
     * Configures the start, control, and end points for this curve, using the values at the
     * specified offset in the `points` array.
     */
    fun setCurve(points: Array<XY>, offset: Int) {
        setCurve(points[offset + 0].x, points[offset + 0].y,
                points[offset + 1].x, points[offset + 1].y,
                points[offset + 2].x, points[offset + 2].y)
    }

    /**
     * Configures the start, control, and end points for this curve to be the same as the supplied
     * curve.
     */
    fun setCurve(curve: IQuadCurve) {
        setCurve(curve.x1, curve.y1, curve.ctrlX, curve.ctrlY,
                curve.x2, curve.y2)
    }

    companion object {
        private const val serialVersionUID = -6760122161413212105L
    }
}
