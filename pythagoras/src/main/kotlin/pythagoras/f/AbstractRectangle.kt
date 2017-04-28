//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

import pythagoras.util.Platform

import java.util.NoSuchElementException

/**
 * Provides most of the implementation of [IRectangle], obtaining only the location and
 * dimensions from the derived class.
 */
abstract class AbstractRectangle : RectangularShape(), IRectangle {
    override // from interface IRectangle
    fun location(): Point {
        return location(Point())
    }

    override // from interface IRectangle
    fun location(target: Point): Point {
        return target.set(x(), y())
    }

    override // from interface IRectangle
    fun size(): Dimension {
        return size(Dimension())
    }

    override // from interface IRectangle
    fun size(target: Dimension): Dimension {
        target.setSize(width(), height())
        return target
    }

    override // from interface IRectangle
    fun intersection(rx: Float, ry: Float, rw: Float, rh: Float): Rectangle {
        val x1 = Math.max(x(), rx)
        val y1 = Math.max(y(), ry)
        val x2 = Math.min(maxX(), rx + rw)
        val y2 = Math.min(maxY(), ry + rh)
        return Rectangle(x1, y1, x2 - x1, y2 - y1)
    }

    override // from interface IRectangle
    fun intersection(r: IRectangle): Rectangle {
        return intersection(r.x(), r.y(), r.width(), r.height())
    }

    override // from interface IRectangle
    fun union(r: IRectangle): Rectangle {
        val rect = Rectangle(this)
        rect.add(r)
        return rect
    }

    override // from interface IRectangle
    fun intersectsLine(x1: Float, y1: Float, x2: Float, y2: Float): Boolean {
        return Lines.lineIntersectsRect(x1, y1, x2, y2, x(), y(), width(), height())
    }

    override // from interface IRectangle
    fun intersectsLine(l: ILine): Boolean {
        return intersectsLine(l.x1(), l.y1(), l.x2(), l.y2())
    }

    override // from interface IRectangle
    fun outcode(px: Float, py: Float): Int {
        var code = 0

        if (width() <= 0) {
            code = code or (IRectangle.OUT_LEFT or IRectangle.OUT_RIGHT)
        } else if (px < x()) {
            code = code or IRectangle.OUT_LEFT
        } else if (px > maxX()) {
            code = code or IRectangle.OUT_RIGHT
        }

        if (height() <= 0) {
            code = code or (IRectangle.OUT_TOP or IRectangle.OUT_BOTTOM)
        } else if (py < y()) {
            code = code or IRectangle.OUT_TOP
        } else if (py > maxY()) {
            code = code or IRectangle.OUT_BOTTOM
        }

        return code
    }

    override // from interface IRectangle
    fun outcode(p: XY): Int {
        return outcode(p.x(), p.y())
    }

    override // from interface IRectangle
    fun clone(): Rectangle {
        return Rectangle(this)
    }

    override // from interface IShape
    fun contains(px: Float, py: Float): Boolean {
        var px = px
        var py = py
        if (isEmpty) return false

        val x = x()
        val y = y()
        if (px < x || py < y) return false

        px -= x
        py -= y
        return px <= width() && py <= height()
    }

    override // from interface IShape
    fun contains(rx: Float, ry: Float, rw: Float, rh: Float): Boolean {
        if (isEmpty) return false

        val x1 = x()
        val y1 = y()
        val x2 = x1 + width()
        val y2 = y1 + height()
        return x1 <= rx && rx + rw <= x2 && y1 <= ry && ry + rh <= y2
    }

    override // from interface IShape
    fun intersects(rx: Float, ry: Float, rw: Float, rh: Float): Boolean {
        if (isEmpty) return false

        val x1 = x()
        val y1 = y()
        val x2 = x1 + width()
        val y2 = y1 + height()
        return rx + rw > x1 && rx < x2 && ry + rh > y1 && ry < y2
    }

    override // from interface IShape
    fun pathIterator(t: Transform?): PathIterator {
        return Iterator(this, t)
    }

    override // from interface IShape
    fun pathIterator(t: Transform?, flatness: Float): PathIterator {
        return Iterator(this, t)
    }

    override // from Object
    fun equals(obj: Any?): Boolean {
        if (obj === this) {
            return true
        }
        if (obj is AbstractRectangle) {
            val r = obj
            return r.x() == x() && r.y() == y() &&
                    r.width() == width() && r.height() == height()
        }
        return false
    }

    override // from Object
    fun hashCode(): Int {
        return Platform.hashCode(x()) xor Platform.hashCode(y()) xor
                Platform.hashCode(width()) xor Platform.hashCode(height())
    }

    override // from Object
    fun toString(): String {
        return Dimensions.dimenToString(width(), height()) + Points.pointToString(x(), y())
    }

    /** An iterator over an [IRectangle].  */
    protected class Iterator internal constructor(r: IRectangle, private val t: Transform?) : PathIterator {
        private val x: Float
        private val y: Float
        private val width: Float
        private val height: Float

        /** The current segment index.  */
        private var index: Int = 0

        init {
            this.x = r.x()
            this.y = r.y()
            this.width = r.width()
            this.height = r.height()
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
            if (index == 0) {
                type = PathIterator.SEG_MOVETO
                coords[0] = x
                coords[1] = y
            } else {
                type = PathIterator.SEG_LINETO
                when (index) {
                    1 -> {
                        coords[0] = x + width
                        coords[1] = y
                    }
                    2 -> {
                        coords[0] = x + width
                        coords[1] = y + height
                    }
                    3 -> {
                        coords[0] = x
                        coords[1] = y + height
                    }
                    4 -> {
                        coords[0] = x
                        coords[1] = y
                    }
                }
            }
            t?.transform(coords, 0, coords, 0, 1)
            return type
        }
    }
}
