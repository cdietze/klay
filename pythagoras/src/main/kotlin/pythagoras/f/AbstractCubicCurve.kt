//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

import java.lang.Math

/**
 * Provides most of the implementation of [ICubicCurve], obtaining only the start, end and
 * control points from the derived class.
 */
abstract class AbstractCubicCurve : ICubicCurve {
    override // from interface ICubicCurve
    fun p1(): Point {
        return Point(x1, y1)
    }

    override // from interface ICubicCurve
    fun ctrlP1(): Point {
        return Point(ctrlX1, ctrlY1)
    }

    override // from interface ICubicCurve
    fun ctrlP2(): Point {
        return Point(ctrlX2, ctrlY2)
    }

    override // from interface ICubicCurve
    fun p2(): Point {
        return Point(x2, y2)
    }

    override // from interface ICubicCurve
    fun flatnessSq(): Float {
        return CubicCurves.flatnessSq(x1, y1, ctrlX1, ctrlY1,
                ctrlX2, ctrlY2, x2, y2)
    }

    override // from interface ICubicCurve
    fun flatness(): Float {
        return CubicCurves.flatness(x1, y1, ctrlX1, ctrlY1,
                ctrlX2, ctrlY2, x2, y2)
    }

    override // from interface ICubicCurve
    fun subdivide(left: CubicCurve, right: CubicCurve) {
        CubicCurves.subdivide(this, left, right)
    }

    override // from interface ICubicCurve
    fun clone(): CubicCurve {
        return CubicCurve(x1, y1, ctrlX1, ctrlY1,
                ctrlX2, ctrlY2, x2, y2)
    }

    override // from interface IShape
            // curves contain no space
    val isEmpty: Boolean
        get() = true

    override // from interface IShape
    fun contains(px: Float, py: Float): Boolean {
        return Crossing.isInsideEvenOdd(Crossing.crossShape(this, px, py))
    }

    override // from interface IShape
    fun contains(rx: Float, ry: Float, rw: Float, rh: Float): Boolean {
        val cross = Crossing.intersectShape(this, rx, ry, rw, rh)
        return cross != Crossing.CROSSING && Crossing.isInsideEvenOdd(cross)
    }

    override // from interface IShape
    fun contains(p: XY): Boolean {
        return contains(p.x, p.y)
    }

    override // from interface IShape
    fun contains(r: IRectangle): Boolean {
        return contains(r.x, r.y, r.width, r.height)
    }

    override // from interface IShape
    fun intersects(rx: Float, ry: Float, rw: Float, rh: Float): Boolean {
        val cross = Crossing.intersectShape(this, rx, ry, rw, rh)
        return cross == Crossing.CROSSING || Crossing.isInsideEvenOdd(cross)
    }

    override // from interface IShape
    fun intersects(r: IRectangle): Boolean {
        return intersects(r.x, r.y, r.width, r.height)
    }

    override // from interface IShape
    fun bounds(): Rectangle {
        return bounds(Rectangle())
    }

    override // from interface IShape
    fun bounds(target: Rectangle): Rectangle {
        val x1 = x1
        val y1 = y1
        val x2 = x2
        val y2 = y2
        val ctrlx1 = ctrlX1
        val ctrly1 = ctrlY1
        val ctrlx2 = ctrlX2
        val ctrly2 = ctrlY2
        val rx1 = Math.min(Math.min(x1, x2), Math.min(ctrlx1, ctrlx2))
        val ry1 = Math.min(Math.min(y1, y2), Math.min(ctrly1, ctrly2))
        val rx2 = Math.max(Math.max(x1, x2), Math.max(ctrlx1, ctrlx2))
        val ry2 = Math.max(Math.max(y1, y2), Math.max(ctrly1, ctrly2))
        target.setBounds(rx1, ry1, rx2 - rx1, ry2 - ry1)
        return target
    }

    override // from interface IShape
    fun pathIterator(t: Transform?): PathIterator {
        return Iterator(this, t)
    }

    override // from interface IShape
    fun pathIterator(at: Transform?, flatness: Float): PathIterator {
        return FlatteningPathIterator(pathIterator(at), flatness)
    }

    /** An iterator over an [ICubicCurve].  */
    protected class Iterator internal constructor(private val c: ICubicCurve, private val t: Transform?) : PathIterator {
        private var index: Int = 0

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
            val count: Int
            if (index == 0) {
                type = PathIterator.SEG_MOVETO
                coords[0] = c.x1
                coords[1] = c.y1
                count = 1
            } else {
                type = PathIterator.SEG_CUBICTO
                coords[0] = c.ctrlX1
                coords[1] = c.ctrlY1
                coords[2] = c.ctrlX2
                coords[3] = c.ctrlY2
                coords[4] = c.x2
                coords[5] = c.y2
                count = 3
            }
            t?.transform(coords, 0, coords, 0, count)
            return type
        }
    }
}
