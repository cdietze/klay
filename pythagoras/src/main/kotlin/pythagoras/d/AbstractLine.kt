//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.d

import java.util.NoSuchElementException

/**
 * Provides most of the implementation of [ILine], obtaining only the start and end points
 * from the derived class.
 */
abstract class AbstractLine : ILine {
    override // from interface ILine
    fun p1(): Point {
        return p1(Point())
    }

    override // from interface ILine
    fun p1(target: Point): Point {
        return target.set(x1(), y1())
    }

    override // from interface ILine
    fun p2(): Point {
        return p2(Point())
    }

    override // from interface ILine
    fun p2(target: Point): Point {
        return target.set(x2(), y2())
    }

    override // from interface ILine
    fun pointLineDistSq(px: Double, py: Double): Double {
        return Lines.pointLineDistSq(px, py, x1(), y1(), x2(), y2())
    }

    override // from interface ILine
    fun pointLineDistSq(p: XY): Double {
        return Lines.pointLineDistSq(p.x(), p.y(), x1(), y1(), x2(), y2())
    }

    override // from interface ILine
    fun pointLineDist(px: Double, py: Double): Double {
        return Lines.pointLineDist(px, py, x1(), y1(), x2(), y2())
    }

    override // from interface ILine
    fun pointLineDist(p: XY): Double {
        return Lines.pointLineDist(p.x(), p.y(), x1(), y1(), x2(), y2())
    }

    override // from interface ILine
    fun pointSegDistSq(px: Double, py: Double): Double {
        return Lines.pointSegDistSq(px, py, x1(), y1(), x2(), y2())
    }

    override // from interface ILine
    fun pointSegDistSq(p: XY): Double {
        return Lines.pointSegDistSq(p.x(), p.y(), x1(), y1(), x2(), y2())
    }

    override // from interface ILine
    fun pointSegDist(px: Double, py: Double): Double {
        return Lines.pointSegDist(px, py, x1(), y1(), x2(), y2())
    }

    override // from interface ILine
    fun pointSegDist(p: XY): Double {
        return Lines.pointSegDist(p.x(), p.y(), x1(), y1(), x2(), y2())
    }

    override // from interface ILine
    fun relativeCCW(px: Double, py: Double): Int {
        return Lines.relativeCCW(px, py, x1(), y1(), x2(), y2())
    }

    override // from interface ILine
    fun relativeCCW(p: XY): Int {
        return Lines.relativeCCW(p.x(), p.y(), x1(), y1(), x2(), y2())
    }

    override // from interface ILine
    fun clone(): Line {
        return Line(x1(), y1(), x2(), y2())
    }

    override // from interface IShape
    val isEmpty: Boolean
        get() = false

    override // from interface IShape
    fun contains(x: Double, y: Double): Boolean {
        return false
    }

    override // from interface IShape
    fun contains(point: XY): Boolean {
        return false
    }

    override // from interface IShape
    fun contains(x: Double, y: Double, w: Double, h: Double): Boolean {
        return false
    }

    override // from interface IShape
    fun contains(r: IRectangle): Boolean {
        return false
    }

    override // from interface IShape
    fun intersects(rx: Double, ry: Double, rw: Double, rh: Double): Boolean {
        return Lines.lineIntersectsRect(x1(), y1(), x2(), y2(), rx, ry, rw, rh)
    }

    override // from interface IShape
    fun intersects(r: IRectangle): Boolean {
        return r.intersectsLine(this)
    }

    override // from interface IShape
    fun bounds(): Rectangle {
        return bounds(Rectangle())
    }

    override // from interface IShape
    fun bounds(target: Rectangle): Rectangle {
        val x1 = x1()
        val x2 = x2()
        val y1 = y1()
        val y2 = y2()
        val rx: Double
        val ry: Double
        val rw: Double
        val rh: Double
        if (x1 < x2) {
            rx = x1
            rw = x2 - x1
        } else {
            rx = x2
            rw = x1 - x2
        }
        if (y1 < y2) {
            ry = y1
            rh = y2 - y1
        } else {
            ry = y2
            rh = y1 - y2
        }
        target.setBounds(rx, ry, rw, rh)
        return target
    }

    override // from interface IShape
    fun pathIterator(at: Transform): PathIterator {
        return Iterator(this, at)
    }

    override // from interface IShape
    fun pathIterator(at: Transform, flatness: Double): PathIterator {
        return Iterator(this, at)
    }

    /** An iterator over an [ILine].  */
    protected class Iterator internal constructor(l: ILine, private val t: Transform?) : PathIterator {
        private val x1: Double
        private val y1: Double
        private val x2: Double
        private val y2: Double
        private var index: Int = 0

        init {
            this.x1 = l.x1()
            this.y1 = l.y1()
            this.x2 = l.x2()
            this.y2 = l.y2()
        }

        override fun windingRule(): Int {
            return PathIterator.WIND_NON_ZERO
        }

        override val isDone: Boolean
            get() = index > 1

        override fun next() {
            index++
        }

        override fun currentSegment(coords: DoubleArray): Int {
            if (isDone) {
                throw NoSuchElementException("Iterator out of bounds")
            }
            val type: Int
            if (index == 0) {
                type = PathIterator.SEG_MOVETO
                coords[0] = x1
                coords[1] = y1
            } else {
                type = PathIterator.SEG_LINETO
                coords[0] = x2
                coords[1] = y2
            }
            t?.transform(coords, 0, coords, 0, 1)
            return type
        }
    }
}
