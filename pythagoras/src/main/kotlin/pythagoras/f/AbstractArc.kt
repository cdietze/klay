//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

import java.util.*

/**
 * Provides most of the implementation of [IArc], obtaining only the frame and other metrics
 * from the derived class.
 */
abstract class AbstractArc : RectangularShape(), IArc {
    override // from interface IArc
    fun startPoint(): Point {
        return startPoint(Point())
    }

    override // from interface IArc
    fun startPoint(target: Point): Point {
        val a = FloatMath.toRadians(angleStart())
        return target.set(x() + (1f + FloatMath.cos(a)) * width() / 2f,
                y() + (1f - FloatMath.sin(a)) * height() / 2f)
    }

    override // from interface IArc
    fun endPoint(): Point {
        return endPoint(Point())
    }

    override // from interface IArc
    fun endPoint(target: Point): Point {
        val a = FloatMath.toRadians(angleStart() + angleExtent())
        return target.set(x() + (1f + FloatMath.cos(a)) * width() / 2f,
                y() + (1f - FloatMath.sin(a)) * height() / 2f)
    }

    override // from interface IArc
    fun containsAngle(angle: Float): Boolean {
        var angle = angle
        val extent = angleExtent()
        if (extent >= 360f) {
            return true
        }
        angle = normAngle(angle)
        val a1 = normAngle(angleStart())
        val a2 = a1 + extent
        if (a2 > 360f) {
            return angle >= a1 || angle <= a2 - 360f
        }
        if (a2 < 0f) {
            return angle >= a2 + 360f || angle <= a1
        }
        return if (extent > 0f) a1 <= angle && angle <= a2 else a2 <= angle && angle <= a1
    }

    override // from interface IArc
    fun clone(): Arc {
        return Arc(x(), y(), width(), height(), angleStart(), angleExtent(),
                arcType())
    }

    override // from RectangularShape
    val isEmpty: Boolean
        get() = arcType() == IArc.OPEN || super.isEmpty

    override // from RectangularShape
    fun contains(px: Float, py: Float): Boolean {
        // normalize point
        val nx = (px - x()) / width() - 0.5f
        val ny = (py - y()) / height() - 0.5f
        if (nx * nx + ny * ny > 0.25) {
            return false
        }

        val extent = angleExtent()
        val absExtent = Math.abs(extent)
        if (absExtent >= 360f) {
            return true
        }

        val containsAngle = containsAngle(FloatMath.toDegrees(-FloatMath.atan2(ny, nx)))
        if (arcType() == IArc.PIE) {
            return containsAngle
        }
        if (absExtent <= 180f && !containsAngle) {
            return false
        }

        val l = Line(startPoint(), endPoint())
        val ccw1 = l.relativeCCW(px, py)
        val ccw2 = l.relativeCCW(centerX(), centerY())
        return ccw1 == 0 || ccw2 == 0 || (ccw1 + ccw2 == 0) xor (absExtent > 180f)
    }

    override // from RectangularShape
    fun contains(rx: Float, ry: Float, rw: Float, rh: Float): Boolean {
        if (!(contains(rx, ry) && contains(rx + rw, ry) &&
                contains(rx + rw, ry + rh) && contains(rx, ry + rh))) {
            return false
        }

        val absExtent = Math.abs(angleExtent())
        if (arcType() != IArc.PIE || absExtent <= 180f || absExtent >= 360f) {
            return true
        }

        val r = Rectangle(rx, ry, rw, rh)
        val cx = centerX()
        val cy = centerY()
        if (r.contains(cx, cy)) {
            return false
        }

        val p1 = startPoint()
        val p2 = endPoint()
        return !r.intersectsLine(cx, cy, p1.x(), p1.y()) && !r.intersectsLine(cx, cy, p2.x(), p2.y())
    }

    override // from RectangularShape
    fun intersects(rx: Float, ry: Float, rw: Float, rh: Float): Boolean {
        if (isEmpty || rw <= 0f || rh <= 0f) {
            return false
        }

        // check: does arc contain rectangle's points
        if (contains(rx, ry) || contains(rx + rw, ry) ||
                contains(rx, ry + rh) || contains(rx + rw, ry + rh)) {
            return true
        }

        val cx = centerX()
        val cy = centerY()
        val p1 = startPoint()
        val p2 = endPoint()

        // check: does rectangle contain arc's points
        val r = Rectangle(rx, ry, rw, rh)
        if (r.contains(p1) || r.contains(p2) || arcType() == IArc.PIE && r.contains(cx, cy)) {
            return true
        }

        if (arcType() == IArc.PIE) {
            if (r.intersectsLine(p1.x(), p1.y(), cx, cy) || r.intersectsLine(p2.x(), p2.y(), cx, cy)) {
                return true
            }
        } else {
            if (r.intersectsLine(p1.x(), p1.y(), p2.x(), p2.y())) {
                return true
            }
        }

        // nearest rectangle point
        val nx = if (cx < rx) rx else if (cx > rx + rw) rx + rw else cx
        val ny = if (cy < ry) ry else if (cy > ry + rh) ry + rh else cy
        return contains(nx, ny)
    }

    override // from RectangularShape
    fun bounds(target: Rectangle): Rectangle {
        if (isEmpty) {
            target.setBounds(x(), y(), width(), height())
            return target
        }

        val rx1 = x()
        val ry1 = y()
        val rx2 = rx1 + width()
        val ry2 = ry1 + height()

        val p1 = startPoint()
        val p2 = endPoint()

        var bx1 = if (containsAngle(180f)) rx1 else Math.min(p1.x(), p2.x())
        var by1 = if (containsAngle(90f)) ry1 else Math.min(p1.y(), p2.y())
        var bx2 = if (containsAngle(0f)) rx2 else Math.max(p1.x(), p2.x())
        var by2 = if (containsAngle(270f)) ry2 else Math.max(p1.y(), p2.y())

        if (arcType() == IArc.PIE) {
            val cx = centerX()
            val cy = centerY()
            bx1 = Math.min(bx1, cx)
            by1 = Math.min(by1, cy)
            bx2 = Math.max(bx2, cx)
            by2 = Math.max(by2, cy)
        }
        target.setBounds(bx1, by1, bx2 - bx1, by2 - by1)
        return target
    }

    override // from interface IShape
    fun pathIterator(at: Transform): PathIterator {
        return Iterator(this, at)
    }

    /** Returns a normalized angle (bound between 0 and 360 degrees).  */
    protected fun normAngle(angle: Float): Float {
        return angle - FloatMath.floor(angle / 360f) * 360f
    }

    /** An iterator over an [IArc].  */
    protected class Iterator internal constructor(a: IArc,
                                                  /** The path iterator transformation  */
                                                  private val t: Transform?) : PathIterator {
        /** The x coordinate of left-upper corner of the arc rectangle bounds  */
        private val x: Float

        /** The y coordinate of left-upper corner of the arc rectangle bounds  */
        private val y: Float

        /** The width of the arc rectangle bounds  */
        private val width: Float

        /** The height of the arc rectangle bounds  */
        private val height: Float

        /** The start angle of the arc in degrees  */
        private var angle: Float = 0.toFloat()

        /** The angle extent in degrees  */
        private val extent: Float

        /** The closure type of the arc  */
        private val type: Int

        /** The current segment index  */
        private var index: Int = 0

        /** The number of arc segments the source arc subdivided to be approximated by Bezier
         * curves. Depends on extent value.  */
        private var arcCount: Int = 0

        /** The number of line segments. Depends on closure type.  */
        private var lineCount: Int = 0

        /** The step to calculate next arc subdivision point  */
        private var step: Float = 0.toFloat()

        /** The temporary value of cosinus of the current angle  */
        private var cos: Float = 0.toFloat()

        /** The temporary value of sinus of the current angle  */
        private var sin: Float = 0.toFloat()

        /** The coefficient to calculate control points of Bezier curves  */
        private var k: Float = 0.toFloat()

        /** The temporary value of x coordinate of the Bezier curve control vector  */
        private var kx: Float = 0.toFloat()

        /** The temporary value of y coordinate of the Bezier curve control vector  */
        private var ky: Float = 0.toFloat()

        /** The x coordinate of the first path point (MOVE_TO)  */
        private var mx: Float = 0.toFloat()

        /** The y coordinate of the first path point (MOVE_TO)  */
        private var my: Float = 0.toFloat()

        init {
            this.width = a.width() / 2f
            this.height = a.height() / 2f
            this.x = a.x() + width
            this.y = a.y() + height
            this.angle = -FloatMath.toRadians(a.angleStart())
            this.extent = -a.angleExtent()
            this.type = a.arcType()

            if (width < 0 || height < 0) {
                arcCount = 0
                lineCount = 0
                index = 1
            } else {

                if (Math.abs(extent) >= 360f) {
                    arcCount = 4
                    k = 4f / 3f * (FloatMath.sqrt(2f) - 1f)
                    step = FloatMath.PI / 2f
                    if (extent < 0f) {
                        step = -step
                        k = -k
                    }
                } else {
                    arcCount = MathUtil.iceil(Math.abs(extent) / 90f)
                    step = FloatMath.toRadians(extent / arcCount)
                    k = 4f / 3f * (1f - FloatMath.cos(step / 2f)) / FloatMath.sin(step / 2f)
                }

                lineCount = 0
                if (type == IArc.CHORD) {
                    lineCount++
                } else if (type == IArc.PIE) {
                    lineCount += 2
                }
            }
        }

        override fun windingRule(): Int {
            return PathIterator.WIND_NON_ZERO
        }

        override val isDone: Boolean
            get() = index > arcCount + lineCount

        override fun next() {
            index++
        }

        override fun currentSegment(coords: FloatArray): Int {
            if (isDone) {
                throw NoSuchElementException("Iterator out of bounds")
            }
            val type: Int
            val count: Int
            if (index == 0) {
                type = PathIterator.SEG_MOVETO
                count = 1
                cos = FloatMath.cos(angle)
                sin = FloatMath.sin(angle)
                kx = k * width * sin
                ky = k * height * cos
                mx = x + cos * width
                coords[0] = mx
                my = y + sin * height
                coords[1] = my
            } else if (index <= arcCount) {
                type = PathIterator.SEG_CUBICTO
                count = 3
                coords[0] = mx - kx
                coords[1] = my + ky
                angle += step
                cos = FloatMath.cos(angle)
                sin = FloatMath.sin(angle)
                kx = k * width * sin
                ky = k * height * cos
                mx = x + cos * width
                coords[4] = mx
                my = y + sin * height
                coords[5] = my
                coords[2] = mx + kx
                coords[3] = my - ky
            } else if (index == arcCount + lineCount) {
                type = PathIterator.SEG_CLOSE
                count = 0
            } else {
                type = PathIterator.SEG_LINETO
                count = 1
                coords[0] = x
                coords[1] = y
            }
            t?.transform(coords, 0, coords, 0, count)
            return type
        }
    }
}
