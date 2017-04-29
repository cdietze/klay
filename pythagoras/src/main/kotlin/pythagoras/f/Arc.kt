//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

import java.lang.Math

/**
 * Represents an arc defined by a framing rectangle, start angle, angular extend, and closure type.
 */
class Arc : AbstractArc {

    /** The x-coordinate of this arc's framing rectangle.  */
    override var x: Float = 0.toFloat()

    /** The y-coordinate of this arc's framing rectangle.  */
    override var y: Float = 0.toFloat()

    /** The width of this arc's framing rectangle.  */
    override var width: Float = 0.toFloat()

    /** The height of this arc's framing rectangle.  */
    override var height: Float = 0.toFloat()

    /** The starting angle of this arc.  */
    var start: Float = 0.toFloat()

    /** The angular extent of this arc.  */
    var extent: Float = 0.toFloat()

    /**
     * Creates an arc of the specified type with frame (0x0+0+0) and zero angles.
     */
    constructor(type: Int = IArc.OPEN) {
        setArcType(type)
    }

    /**
     * Creates an arc of the specified type with the specified framing rectangle, starting angle
     * and angular extent.
     */
    constructor(x: Float, y: Float, width: Float, height: Float, start: Float, extent: Float, type: Int) {
        setArc(x, y, width, height, start, extent, type)
    }

    /**
     * Creates an arc of the specified type with the supplied framing rectangle, starting angle and
     * angular extent.
     */
    constructor(bounds: IRectangle, start: Float, extent: Float, type: Int) {
        setArc(bounds.x, bounds.y, bounds.width, bounds.height,
                start, extent, type)
    }

    override val arcType: Int get() = type
    override val angleStart: Float get() = start
    override val angleExtent: Float get() = extent

    /**
     * Sets the type of this arc to the specified value.
     */
    fun setArcType(type: Int) {
        if (type != IArc.OPEN && type != IArc.CHORD && type != IArc.PIE) {
            throw IllegalArgumentException("Invalid Arc type: " + type)
        }
        this.type = type
    }

    /**
     * Sets the starting angle of this arc to the specified value.
     */
    fun setAngleStart(start: Float) {
        this.start = start
    }

    /**
     * Sets the angular extent of this arc to the specified value.
     */
    fun setAngleExtent(extent: Float) {
        this.extent = extent
    }

    /**
     * Sets the location, size, angular extents, and closure type of this arc to the specified
     * values.
     */
    fun setArc(x: Float, y: Float, width: Float, height: Float,
               start: Float, extent: Float, type: Int) {
        setArcType(type)
        this.x = x
        this.y = y
        this.width = width
        this.height = height
        this.start = start
        this.extent = extent
    }

    /**
     * Sets the location, size, angular extents, and closure type of this arc to the specified
     * values.
     */
    fun setArc(point: XY, size: IDimension, start: Float, extent: Float, type: Int) {
        setArc(point.x, point.y, size.width, size.height, start, extent, type)
    }

    /**
     * Sets the location, size, angular extents, and closure type of this arc to the specified
     * values.
     */
    fun setArc(rect: IRectangle, start: Float, extent: Float, type: Int) {
        setArc(rect.x, rect.y, rect.width, rect.height, start, extent, type)
    }

    /**
     * Sets the location, size, angular extents, and closure type of this arc to the same values as
     * the supplied arc.
     */
    fun setArc(arc: IArc) {
        setArc(arc.x, arc.y, arc.width, arc.height, arc.angleStart,
                arc.angleExtent, arc.arcType)
    }

    /**
     * Sets the location, size, angular extents, and closure type of this arc based on the
     * specified values.
     */
    fun setArcByCenter(x: Float, y: Float, radius: Float,
                       start: Float, extent: Float, type: Int) {
        setArc(x - radius, y - radius, radius * 2f, radius * 2f, start, extent, type)
    }

    /**
     * Sets the location, size, angular extents, and closure type of this arc based on the
     * specified values.
     */
    fun setArcByTangent(p1: XY, p2: XY, p3: XY, radius: Float) {
        // use simple geometric calculations of arc center, radius and angles by tangents
        var a1 = -FloatMath.atan2(p1.y - p2.y, p1.x - p2.x)
        var a2 = -FloatMath.atan2(p3.y - p2.y, p3.x - p2.x)
        val am = (a1 + a2) / 2f
        var ah = a1 - am
        val d = radius / Math.abs(FloatMath.sin(ah))
        val x = p2.x + d * FloatMath.cos(am)
        val y = p2.y - d * FloatMath.sin(am)
        ah = if (ah >= 0f) FloatMath.PI * 1.5f - ah else FloatMath.PI * 0.5f - ah
        a1 = normAngle(FloatMath.toDegrees(am - ah))
        a2 = normAngle(FloatMath.toDegrees(am + ah))
        var delta = a2 - a1
        if (delta <= 0f) {
            delta += 360f
        }
        setArcByCenter(x, y, radius, a1, delta, type)
    }

    /**
     * Sets the starting angle of this arc to the angle defined by the supplied point relative to
     * the center of this arc.
     */
    fun setAngleStart(point: XY) {
        val angle = FloatMath.atan2(point.y - centerY, point.x - centerX)
        setAngleStart(normAngle(-FloatMath.toDegrees(angle)))
    }

    /**
     * Sets the starting angle and angular extent of this arc using two sets of coordinates. The
     * first set of coordinates is used to determine the angle of the starting point relative to
     * the arc's center. The second set of coordinates is used to determine the angle of the end
     * point relative to the arc's center. The arc will always be non-empty and extend
     * counterclockwise from the first point around to the second point.
     */
    fun setAngles(x1: Float, y1: Float, x2: Float, y2: Float) {
        val cx = centerX
        val cy = centerY
        val a1 = normAngle(-FloatMath.toDegrees(FloatMath.atan2(y1 - cy, x1 - cx)))
        var a2 = normAngle(-FloatMath.toDegrees(FloatMath.atan2(y2 - cy, x2 - cx)))
        a2 -= a1
        if (a2 <= 0f) {
            a2 += 360f
        }
        setAngleStart(a1)
        setAngleExtent(a2)
    }

    /**
     * Sets the starting angle and angular extent of this arc using two sets of coordinates. The
     * first set of coordinates is used to determine the angle of the starting point relative to
     * the arc's center. The second set of coordinates is used to determine the angle of the end
     * point relative to the arc's center. The arc will always be non-empty and extend
     * counterclockwise from the first point around to the second point.
     */
    fun setAngles(p1: XY, p2: XY) {
        setAngles(p1.x, p1.y, p2.x, p2.y)
    }

    override // from RectangularShape
    fun setFrame(x: Float, y: Float, width: Float, height: Float) {
        setArc(x, y, width, height, angleStart, angleExtent, type)
    }

    private var type: Int = 0

    companion object {
        private const val serialVersionUID = 378120636227888073L
    }
}
/**
 * Creates an open arc with frame (0x0+0+0) and zero angles.
 */
