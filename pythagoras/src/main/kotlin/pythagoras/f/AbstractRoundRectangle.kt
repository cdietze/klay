//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

import java.util.NoSuchElementException

/**
 * Provides most of the implementation of [IRoundRectangle], obtaining the framing rectangle
 * from the derived class.
 */
abstract class AbstractRoundRectangle : RectangularShape(), IRoundRectangle {
    override // from interface IRoundRectangle
    fun clone(): RoundRectangle {
        return RoundRectangle(x(), y(), width(), height(),
                arcWidth(), arcHeight())
    }

    override // from interface IShape
    fun contains(px: Float, py: Float): Boolean {
        var px = px
        var py = py
        if (isEmpty) return false

        val rx1 = x()
        val ry1 = y()
        val rx2 = rx1 + width()
        val ry2 = ry1 + height()
        if (px < rx1 || px >= rx2 || py < ry1 || py >= ry2) {
            return false
        }

        val aw = arcWidth() / 2f
        val ah = arcHeight() / 2f
        val cx: Float
        val cy: Float
        if (px < rx1 + aw) {
            cx = rx1 + aw
        } else if (px > rx2 - aw) {
            cx = rx2 - aw
        } else {
            return true
        }

        if (py < ry1 + ah) {
            cy = ry1 + ah
        } else if (py > ry2 - ah) {
            cy = ry2 - ah
        } else {
            return true
        }

        px = (px - cx) / aw
        py = (py - cy) / ah
        return px * px + py * py <= 1f
    }

    override // from interface IShape
    fun contains(rx: Float, ry: Float, rw: Float, rh: Float): Boolean {
        if (isEmpty || rw <= 0f || rh <= 0f) return false
        val rx1 = rx
        val ry1 = ry
        val rx2 = rx + rw
        val ry2 = ry + rh
        return contains(rx1, ry1) && contains(rx2, ry1) && contains(rx2, ry2) && contains(rx1, ry2)
    }

    override // from interface IShape
    fun intersects(rx: Float, ry: Float, rw: Float, rh: Float): Boolean {
        if (isEmpty || rw <= 0f || rh <= 0f) return false

        val x1 = x()
        val y1 = y()
        val x2 = x1 + width()
        val y2 = y1 + height()
        val rx1 = rx
        val ry1 = ry
        val rx2 = rx + rw
        val ry2 = ry + rh
        if (rx2 < x1 || x2 < rx1 || ry2 < y1 || y2 < ry1) {
            return false
        }

        val cx = (x1 + x2) / 2f
        val cy = (y1 + y2) / 2f
        val nx = if (cx < rx1) rx1 else if (cx > rx2) rx2 else cx
        val ny = if (cy < ry1) ry1 else if (cy > ry2) ry2 else cy
        return contains(nx, ny)
    }

    override // from interface IShape
    fun pathIterator(at: Transform?): PathIterator {
        return Iterator(this, at)
    }

    /** Provides an iterator over an [IRoundRectangle].  */
    protected class Iterator internal constructor(rr: IRoundRectangle, private val t: Transform?) : PathIterator {
        private val x: Float
        private val y: Float
        private val width: Float
        private val height: Float
        private val aw: Float
        private val ah: Float
        private var index: Int = 0

        init {
            this.x = rr.x()
            this.y = rr.y()
            this.width = rr.width()
            this.height = rr.height()
            this.aw = Math.min(width, rr.arcWidth())
            this.ah = Math.min(height, rr.arcHeight())
            if (width < 0f || height < 0f || aw < 0f || ah < 0f) {
                index = POINTS.size
            }
        }

        override fun windingRule(): Int {
            return PathIterator.WIND_NON_ZERO
        }

        override val isDone: Boolean
            get() = index > POINTS.size

        override fun next() {
            index++
        }

        override fun currentSegment(coords: FloatArray): Int {
            if (isDone) {
                throw NoSuchElementException("Iterator out of bounds")
            }
            if (index == POINTS.size) {
                return PathIterator.SEG_CLOSE
            }
            var j = 0
            val p = POINTS[index]
            var i = 0
            while (i < p.size) {
                coords[j++] = x + p[i + 0] * width + p[i + 1] * aw
                coords[j++] = y + p[i + 2] * height + p[i + 3] * ah
                i += 4
            }
            t?.transform(coords, 0, coords, 0, j / 2)
            return TYPES[index]
        }
    }

    companion object {

        // the path for round corners is generated the same way as for Ellipse

        /** The segment types correspond to points array.  */
        protected val TYPES = intArrayOf(PathIterator.SEG_MOVETO, PathIterator.SEG_LINETO, PathIterator.SEG_CUBICTO, PathIterator.SEG_LINETO, PathIterator.SEG_CUBICTO, PathIterator.SEG_LINETO, PathIterator.SEG_CUBICTO, PathIterator.SEG_LINETO, PathIterator.SEG_CUBICTO)

        /** The coefficient to calculate control points of Bezier curves.  */
        protected val U = 0.5f - 2f / 3f * (FloatMath.sqrt(2f) - 1f)

        /** The points coordinates calculation table.  */
        protected val POINTS = arrayOf(floatArrayOf(0f, 0.5f, 0f, 0f), // MOVETO
                floatArrayOf(1f, -0.5f, 0f, 0f), // LINETO
                floatArrayOf(1f, -U, 0f, 0f, 1f, 0f, 0f, U, 1f, 0f, 0f, 0.5f), // CUBICTO
                floatArrayOf(1f, 0f, 1f, -0.5f), // LINETO
                floatArrayOf(1f, 0f, 1f, -U, 1f, -U, 1f, 0f, 1f, -0.5f, 1f, 0f), // CUBICTO
                floatArrayOf(0f, 0.5f, 1f, 0f), // LINETO
                floatArrayOf(0f, U, 1f, 0f, 0f, 0f, 1f, -U, 0f, 0f, 1f, -0.5f), // CUBICTO
                floatArrayOf(0f, 0f, 0f, 0.5f), // LINETO
                floatArrayOf(0f, 0f, 0f, U, 0f, U, 0f, 0f, 0f, 0.5f, 0f, 0f))// CUBICTO
    }
}
