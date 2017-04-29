//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.i

import java.lang.Math

/**
 * Provides most of the implementation of [IRectangle], obtaining only the location and
 * dimensions from the derived class.
 */
abstract class AbstractRectangle : IRectangle {
    override // from IRectangle
    fun minX(): Int {
        return x
    }

    override // from IRectangle
    fun minY(): Int {
        return y
    }

    override // from IRectangle
    fun maxX(): Int {
        return x + width - 1
    }

    override // from IRectangle
    fun maxY(): Int {
        return y + height - 1
    }

    override // from interface IRectangle
    fun location(): Point {
        return location(Point())
    }

    override // from interface IRectangle
    fun location(target: Point): Point {
        target.setLocation(x, y)
        return target
    }

    override // from interface IRectangle
    fun size(): Dimension {
        return size(Dimension())
    }

    override // from interface IRectangle
    fun size(target: Dimension): Dimension {
        target.setSize(width, height)
        return target
    }

    override // from interface IRectangle
    fun intersection(rx: Int, ry: Int, rw: Int, rh: Int): Rectangle {
        val x1 = Math.max(x, rx)
        val y1 = Math.max(y, ry)
        val x2 = Math.min(maxX(), rx + rw - 1)
        val y2 = Math.min(maxY(), ry + rh - 1)
        return Rectangle(x1, y1, x2 - x1, y2 - y1)
    }

    override // from interface IRectangle
    fun intersection(r: IRectangle): Rectangle {
        return intersection(r.x, r.y, r.width, r.height)
    }

    override // from interface IRectangle
    fun union(r: IRectangle): Rectangle {
        val rect = Rectangle(this)
        rect.add(r)
        return rect
    }

    override // from interface IRectangle
    fun outcode(px: Int, py: Int): Int {
        var code = 0

        if (width <= 0) {
            code = code or (IRectangle.OUT_LEFT or IRectangle.OUT_RIGHT)
        } else if (px < x) {
            code = code or IRectangle.OUT_LEFT
        } else if (px > maxX()) {
            code = code or IRectangle.OUT_RIGHT
        }

        if (height <= 0) {
            code = code or (IRectangle.OUT_TOP or IRectangle.OUT_BOTTOM)
        } else if (py < y) {
            code = code or IRectangle.OUT_TOP
        } else if (py > maxY()) {
            code = code or IRectangle.OUT_BOTTOM
        }

        return code
    }

    override // from interface IRectangle
    fun outcode(p: IPoint): Int {
        return outcode(p.x, p.y)
    }

    override // from interface IRectangle
    fun clone(): Rectangle {
        return Rectangle(this)
    }

    override // from interface IShape
    val isEmpty: Boolean
        get() = width <= 0 || height <= 0

    override // from interface IShape
    fun contains(px: Int, py: Int): Boolean {
        var px = px
        var py = py
        if (isEmpty) return false

        val x = x
        val y = y
        if (px < x || py < y) return false

        px -= x
        py -= y
        return px < width && py < height
    }

    override // from interface IShape
    fun contains(point: IPoint): Boolean {
        return contains(point.x, point.y)
    }

    override // from interface IShape
    fun contains(rx: Int, ry: Int, rw: Int, rh: Int): Boolean {
        if (isEmpty) return false

        val x1 = x
        val y1 = y
        val x2 = x1 + width
        val y2 = y1 + height
        return x1 <= rx && rx + rw <= x2 && y1 <= ry && ry + rh <= y2
    }

    override // from interface IShape
    fun contains(rect: IRectangle): Boolean {
        return contains(rect.x, rect.y, rect.width, rect.height)
    }

    override // from interface IShape
    fun intersects(rx: Int, ry: Int, rw: Int, rh: Int): Boolean {
        if (isEmpty) return false

        val x1 = x
        val y1 = y
        val x2 = x1 + width
        val y2 = y1 + height
        return rx + rw > x1 && rx < x2 && ry + rh > y1 && ry < y2
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

    override // from Object
    fun equals(obj: Any?): Boolean {
        if (obj === this) {
            return true
        }
        if (obj is AbstractRectangle) {
            val r = obj
            return r.x == x && r.y == y &&
                    r.width == width && r.height == height
        }
        return false
    }

    override // from Object
    fun hashCode(): Int {
        return x xor y xor width xor height
    }

    override // from Object
    fun toString(): String {
        return Dimensions.dimenToString(width, height) + Points.pointToString(x, y)
    }
}
