//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f
import java.lang.Math

/**
 * The base class for various [IShape] objects whose geometry is defined by a rectangular
 * frame.
 */
abstract class RectangularShape : IRectangularShape {
    /**
     * Sets the location and size of the framing rectangle of this shape to the specified values.
     */
    abstract fun setFrame(x: Float, y: Float, width: Float, height: Float)

    /**
     * Sets the location and size of the framing rectangle of this shape to the supplied values.
     */
    fun setFrame(loc: XY, size: IDimension) {
        setFrame(loc.x, loc.y, size.width, size.height)
    }

    /**
     * Sets the location and size of the framing rectangle of this shape to be equal to the
     * supplied rectangle.
     */
    fun setFrame(r: IRectangle) {
        setFrame(r.x, r.y, r.width, r.height)
    }

    /**
     * Sets the location and size of the framing rectangle of this shape based on the specified
     * diagonal line.
     */
    fun setFrameFromDiagonal(x1: Float, y1: Float, x2: Float, y2: Float) {
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
        setFrame(rx, ry, rw, rh)
    }

    /**
     * Sets the location and size of the framing rectangle of this shape based on the supplied
     * diagonal line.
     */
    fun setFrameFromDiagonal(p1: XY, p2: XY) {
        setFrameFromDiagonal(p1.x, p1.y, p2.x, p2.y)
    }

    /**
     * Sets the location and size of the framing rectangle of this shape based on the specified
     * center and corner points.
     */
    fun setFrameFromCenter(centerX: Float, centerY: Float,
                           cornerX: Float, cornerY: Float) {
        val width = Math.abs(cornerX - centerX)
        val height = Math.abs(cornerY - centerY)
        setFrame(centerX - width, centerY - height, width * 2, height * 2)
    }

    /**
     * Sets the location and size of the framing rectangle of this shape based on the supplied
     * center and corner points.
     */
    fun setFrameFromCenter(center: XY, corner: XY) {
        setFrameFromCenter(center.x, center.y, corner.x, corner.y)
    }

    override // from IRectangularShape
    val min: Point
        get() {
            return Point(minX, minY)
        }

    override // from IRectangularShape
    val minX: Float
        get() {
            return x
        }

    override // from IRectangularShape
    val minY: Float
        get() {
            return y
        }

    override // from IRectangularShape
    val max: Point
        get() {
            return Point(maxX, maxY)
        }

    override // from IRectangularShape
    val maxX: Float
        get() {
            return x + width
        }

    override // from IRectangularShape
    val maxY: Float
        get() {
            return y + height
        }

    override // from IRectangularShape
    val center: Point
        get() {
            return Point(centerX, centerY)
        }

    override // from IRectangularShape
    val centerX: Float
        get() {
            return x + width / 2
        }

    override // from IRectangularShape
    val centerY: Float
        get() {
            return y + height / 2
        }

    override // from IRectangularShape
    fun frame(): Rectangle {
        return bounds()
    }

    override // from IRectangularShape
    fun frame(target: Rectangle): Rectangle {
        return bounds(target)
    }

    override // from interface IShape
    val isEmpty: Boolean
        get() = width <= 0 || height <= 0

    override // from interface IShape
    fun contains(point: XY): Boolean {
        return contains(point.x, point.y)
    }

    override // from interface IShape
    fun contains(rect: IRectangle): Boolean {
        return contains(rect.x, rect.y, rect.width, rect.height)
    }

    override // from interface IShape
    fun intersects(rect: IRectangle): Boolean {
        return intersects(rect.x, rect.y, rect.width, rect.height)
    }

    override // from interface IShape
    fun bounds(): Rectangle {
        return bounds(Rectangle())
    }

    override // from interface IShape
    fun bounds(target: Rectangle): Rectangle {
        target.setBounds(x, y, width, height)
        return target
    }

    override // from interface IShape
    fun pathIterator(t: Transform?, flatness: Float): PathIterator {
        return FlatteningPathIterator(pathIterator(t), flatness)
    }
}
