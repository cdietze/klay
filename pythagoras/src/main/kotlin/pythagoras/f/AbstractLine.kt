//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

import java.util.*

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
    fun pointLineDistSq(px: Float, py: Float): Float {
        return Lines.pointLineDistSq(px, py, x1(), y1(), x2(), y2())
    }

    override // from interface ILine
    fun pointLineDistSq(p: XY): Float {
        return Lines.pointLineDistSq(p.x(), p.y(), x1(), y1(), x2(), y2())
    }

    override // from interface ILine
    fun pointLineDist(px: Float, py: Float): Float {
        return Lines.pointLineDist(px, py, x1(), y1(), x2(), y2())
    }

    override // from interface ILine
    fun pointLineDist(p: XY): Float {
        return Lines.pointLineDist(p.x(), p.y(), x1(), y1(), x2(), y2())
    }

    override // from interface ILine
    fun pointSegDistSq(px: Float, py: Float): Float {
        return Lines.pointSegDistSq(px, py, x1(), y1(), x2(), y2())
    }

    override // from interface ILine
    fun pointSegDistSq(p: XY): Float {
        return Lines.pointSegDistSq(p.x(), p.y(), x1(), y1(), x2(), y2())
    }

    override // from interface ILine
    fun pointSegDist(px: Float, py: Float): Float {
        return Lines.pointSegDist(px, py, x1(), y1(), x2(), y2())
    }

    override // from interface ILine
    fun pointSegDist(p: XY): Float {
        return Lines.pointSegDist(p.x(), p.y(), x1(), y1(), x2(), y2())
    }

    override // from interface ILine
    fun relativeCCW(px: Float, py: Float): Int {
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
    fun contains(x: Float, y: Float): Boolean {
        return false
    }

    override // from interface IShape
    fun contains(point: XY): Boolean {
        return false
    }

    override // from interface IShape
    fun contains(x: Float, y: Float, w: Float, h: Float): Boolean {
        return false
    }

    override // from interface IShape
    fun contains(r: IRectangle): Boolean {
        return false
    }

    override // from interface IShape
    fun intersects(rx: Float, ry: Float, rw: Float, rh: Float): Boolean {
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
        val rx: Float
        val ry: Float
        val rw: Float
        val rh: Float
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
    fun pathIterator(at: Transform?): PathIterator {
        return Iterator(this, at)
    }

    override // from interface IShape
    fun pathIterator(at: Transform?, flatness: Float): PathIterator {
        return Iterator(this, at)
    }

    /** An iterator over an [ILine].  */
    protected class Iterator internal constructor(l: ILine, private val t: Transform?) : PathIterator {
        private val x1: Float
        private val y1: Float
        private val x2: Float
        private val y2: Float
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

        override fun currentSegment(coords: FloatArray): Int {
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
