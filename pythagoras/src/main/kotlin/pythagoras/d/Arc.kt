//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.d

import java.io.Serializable

/**
 * Represents an arc defined by a framing rectangle, start angle, angular extend, and closure type.
 */
class Arc : AbstractArc, Serializable {

    /** The x-coordinate of this arc's framing rectangle.  */
    var x: Double = 0.toDouble()

    /** The y-coordinate of this arc's framing rectangle.  */
    var y: Double = 0.toDouble()

    /** The width of this arc's framing rectangle.  */
    var width: Double = 0.toDouble()

    /** The height of this arc's framing rectangle.  */
    var height: Double = 0.toDouble()

    /** The starting angle of this arc.  */
    var start: Double = 0.toDouble()

    /** The angular extent of this arc.  */
    var extent: Double = 0.toDouble()

    /**
     * Creates an arc of the specified type with frame (0x0+0+0) and zero angles.
     */
    @JvmOverloads constructor(type: Int = IArc.OPEN) {
        setArcType(type)
    }

    /**
     * Creates an arc of the specified type with the specified framing rectangle, starting angle
     * and angular extent.
     */
    constructor(x: Double, y: Double, width: Double, height: Double, start: Double, extent: Double, type: Int) {
        setArc(x, y, width, height, start, extent, type)
    }

    /**
     * Creates an arc of the specified type with the supplied framing rectangle, starting angle and
     * angular extent.
     */
    constructor(bounds: IRectangle, start: Double, extent: Double, type: Int) {
        setArc(bounds.x(), bounds.y(), bounds.width(), bounds.height(),
                start, extent, type)
    }

    override // from interface IArc
    fun arcType(): Int {
        return type
    }

    override // from interface IArc
    fun x(): Double {
        return x
    }

    override // from interface IArc
    fun y(): Double {
        return y
    }

    override // from interface IArc
    fun width(): Double {
        return width
    }

    override // from interface IArc
    fun height(): Double {
        return height
    }

    override // from interface IArc
    fun angleStart(): Double {
        return start
    }

    override // from interface IArc
    fun angleExtent(): Double {
        return extent
    }

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
    fun setAngleStart(start: Double) {
        this.start = start
    }

    /**
     * Sets the angular extent of this arc to the specified value.
     */
    fun setAngleExtent(extent: Double) {
        this.extent = extent
    }

    /**
     * Sets the location, size, angular extents, and closure type of this arc to the specified
     * values.
     */
    fun setArc(x: Double, y: Double, width: Double, height: Double,
               start: Double, extent: Double, type: Int) {
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
    fun setArc(point: XY, size: IDimension, start: Double, extent: Double, type: Int) {
        setArc(point.x(), point.y(), size.width(), size.height(), start, extent, type)
    }

    /**
     * Sets the location, size, angular extents, and closure type of this arc to the specified
     * values.
     */
    fun setArc(rect: IRectangle, start: Double, extent: Double, type: Int) {
        setArc(rect.x(), rect.y(), rect.width(), rect.height(), start, extent, type)
    }

    /**
     * Sets the location, size, angular extents, and closure type of this arc to the same values as
     * the supplied arc.
     */
    fun setArc(arc: IArc) {
        setArc(arc.x(), arc.y(), arc.width(), arc.height(), arc.angleStart(),
                arc.angleExtent(), arc.arcType())
    }

    /**
     * Sets the location, size, angular extents, and closure type of this arc based on the
     * specified values.
     */
    fun setArcByCenter(x: Double, y: Double, radius: Double,
                       start: Double, extent: Double, type: Int) {
        setArc(x - radius, y - radius, radius * 2f, radius * 2f, start, extent, type)
    }

    /**
     * Sets the location, size, angular extents, and closure type of this arc based on the
     * specified values.
     */
    fun setArcByTangent(p1: XY, p2: XY, p3: XY, radius: Double) {
        // use simple geometric calculations of arc center, radius and angles by tangents
        var a1 = -Math.atan2(p1.y() - p2.y(), p1.x() - p2.x())
        var a2 = -Math.atan2(p3.y() - p2.y(), p3.x() - p2.x())
        val am = (a1 + a2) / 2f
        var ah = a1 - am
        val d = radius / Math.abs(Math.sin(ah))
        val x = p2.x() + d * Math.cos(am)
        val y = p2.y() - d * Math.sin(am)
        ah = if (ah >= 0f) Math.PI * 1.5f - ah else Math.PI * 0.5f - ah
        a1 = normAngle(Math.toDegrees(am - ah))
        a2 = normAngle(Math.toDegrees(am + ah))
        var delta = a2 - a1
        if (delta <= 0f) {
            delta += 360.0
        }
        setArcByCenter(x, y, radius, a1, delta, type)
    }

    /**
     * Sets the starting angle of this arc to the angle defined by the supplied point relative to
     * the center of this arc.
     */
    fun setAngleStart(point: XY) {
        val angle = Math.atan2(point.y() - centerY(), point.x() - centerX())
        setAngleStart(normAngle(-Math.toDegrees(angle)))
    }

    /**
     * Sets the starting angle and angular extent of this arc using two sets of coordinates. The
     * first set of coordinates is used to determine the angle of the starting point relative to
     * the arc's center. The second set of coordinates is used to determine the angle of the end
     * point relative to the arc's center. The arc will always be non-empty and extend
     * counterclockwise from the first point around to the second point.
     */
    fun setAngles(x1: Double, y1: Double, x2: Double, y2: Double) {
        val cx = centerX()
        val cy = centerY()
        val a1 = normAngle(-Math.toDegrees(Math.atan2(y1 - cy, x1 - cx)))
        var a2 = normAngle(-Math.toDegrees(Math.atan2(y2 - cy, x2 - cx)))
        a2 -= a1
        if (a2 <= 0f) {
            a2 += 360.0
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
        setAngles(p1.x(), p1.y(), p2.x(), p2.y())
    }

    override // from RectangularShape
    fun setFrame(x: Double, y: Double, width: Double, height: Double) {
        setArc(x, y, width, height, angleStart(), angleExtent(), type)
    }

    private var type: Int = 0

    companion object {
        private const val serialVersionUID = -2351063986218111710L
    }
}
/**
 * Creates an open arc with frame (0x0+0+0) and zero angles.
 */
