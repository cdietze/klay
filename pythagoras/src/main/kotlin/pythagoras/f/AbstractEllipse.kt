//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

/**
 * Provides most of the implementation of [IEllipse], obtaining the framing rectangle from
 * the derived class.
 */
abstract class AbstractEllipse : RectangularShape(), IEllipse {
    override // from IEllipse
    fun clone(): Ellipse {
        return Ellipse(x, y, width, height)
    }

    override // from interface IShape
    fun contains(px: Float, py: Float): Boolean {
        if (isEmpty) return false
        val a = (px - x) / width - 0.5f
        val b = (py - y) / height - 0.5f
        return a * a + b * b < 0.25f
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
        val cx = x + width / 2f
        val cy = y + height / 2f
        val rx1 = rx
        val ry1 = ry
        val rx2 = rx + rw
        val ry2 = ry + rh
        val nx = if (cx < rx1) rx1 else if (cx > rx2) rx2 else cx
        val ny = if (cy < ry1) ry1 else if (cy > ry2) ry2 else cy
        return contains(nx, ny)
    }

    override // from interface IShape
    fun pathIterator(at: Transform?): PathIterator {
        return Iterator(this, at)
    }

    /** An iterator over an [IEllipse].  */
    protected class Iterator internal constructor(e: IEllipse, private val t: Transform?) : PathIterator {
        private val x: Float
        private val y: Float
        private val width: Float
        private val height: Float
        private var index: Int = 0

        init {
            this.x = e.x
            this.y = e.y
            this.width = e.width
            this.height = e.height
            if (width < 0f || height < 0f) {
                index = 6
            }
        }

        override fun windingRule(): Int {
            return PathIterator.WIND_NON_ZERO
        }

        override val isDone: Boolean
            get() = index > 5

        override fun next() {
            index++
        }

        override fun currentSegment(coords: FloatArray): Int {
            if (isDone) {
                throw NoSuchElementException("Iterator out of bounds")
            }
            if (index == 5) {
                return PathIterator.SEG_CLOSE
            }
            val type: Int
            val count: Int
            if (index == 0) {
                type = PathIterator.SEG_MOVETO
                count = 1
                val p = POINTS[3]
                coords[0] = x + p[4] * width
                coords[1] = y + p[5] * height
            } else {
                type = PathIterator.SEG_CUBICTO
                count = 3
                val p = POINTS[index - 1]
                var j = 0
                for (i in 0..2) {
                    coords[j] = x + p[j++] * width
                    coords[j] = y + p[j++] * height
                }
            }
            t?.transform(coords, 0, coords, 0, count)
            return type
        }
    }

    companion object {

        // An ellipse is subdivided into four quarters by x and y axis. Each part is approximated by a
        // cubic Bezier curve. The arc in the first quarter starts in (a, 0) and finishes in (0, b)
        // points. Control points for the cubic curve are (a, 0), (a, m), (n, b) and (0, b) where n and
        // m are calculated based on the requirement that the Bezier curve in point 0.5 should lay on
        // the arc.

        /** The coefficient to calculate control points of Bezier curves.  */
        private val U = 2f / 3f * (FloatMath.sqrt(2f) - 1f)

        /** The points coordinates calculation table.  */
        private val POINTS = arrayOf(floatArrayOf(1f, 0.5f + U, 0.5f + U, 1f, 0.5f, 1f), floatArrayOf(0.5f - U, 1f, 0f, 0.5f + U, 0f, 0.5f), floatArrayOf(0f, 0.5f - U, 0.5f - U, 0f, 0.5f, 0f), floatArrayOf(0.5f + U, 0f, 1f, 0.5f - U, 1f, 0.5f))
    }
}
