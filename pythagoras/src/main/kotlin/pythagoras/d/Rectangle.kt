//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.d

import java.io.Serializable

/**
 * Represents an area in two dimensions.
 */
class Rectangle : AbstractRectangle, Serializable {

    /** The x-coordinate of the rectangle's upper left corner.  */
    var x: Double = 0.toDouble()

    /** The y-coordinate of the rectangle's upper left corner.  */
    var y: Double = 0.toDouble()

    /** The width of the rectangle.  */
    var width: Double = 0.toDouble()

    /** The height of the rectangle.  */
    var height: Double = 0.toDouble()

    /**
     * Constructs a rectangle at (0,0) and with dimensions (0,0).
     */
    constructor() {}

    /**
     * Constructs a rectangle with the supplied upper-left corner and dimensions (0,0).
     */
    constructor(p: XY) {
        setBounds(p.x(), p.y(), 0.0, 0.0)
    }

    /**
     * Constructs a rectangle with upper-left corner at (0,0) and the supplied dimensions.
     */
    constructor(d: IDimension) {
        setBounds(0.0, 0.0, d.width(), d.height())
    }

    /**
     * Constructs a rectangle with upper-left corner at the supplied point and with the supplied
     * dimensions.
     */
    constructor(p: XY, d: IDimension) {
        setBounds(p.x(), p.y(), d.width(), d.height())
    }

    /**
     * Constructs a rectangle with the specified upper-left corner and dimensions.
     */
    constructor(x: Double, y: Double, width: Double, height: Double) {
        setBounds(x, y, width, height)
    }

    /**
     * Constructs a rectangle with bounds equal to the supplied rectangle.
     */
    constructor(r: IRectangle) {
        setBounds(r.x(), r.y(), r.width(), r.height())
    }

    /**
     * Sets the upper-left corner of this rectangle to the specified point.
     */
    fun setLocation(x: Double, y: Double) {
        this.x = x
        this.y = y
    }

    /**
     * Sets the upper-left corner of this rectangle to the supplied point.
     */
    fun setLocation(p: XY) {
        setLocation(p.x(), p.y())
    }

    /**
     * Sets the size of this rectangle to the specified dimensions.
     */
    fun setSize(width: Double, height: Double) {
        this.width = width
        this.height = height
    }

    /**
     * Sets the size of this rectangle to the supplied dimensions.
     */
    fun setSize(d: IDimension) {
        setSize(d.width(), d.height())
    }

    /**
     * Sets the bounds of this rectangle to the specified bounds.
     */
    fun setBounds(x: Double, y: Double, width: Double, height: Double) {
        this.x = x
        this.y = y
        this.height = height
        this.width = width
    }

    /**
     * Sets the bounds of this rectangle to those of the supplied rectangle.
     */
    fun setBounds(r: IRectangle) {
        setBounds(r.x(), r.y(), r.width(), r.height())
    }

    /**
     * Grows the bounds of this rectangle by the specified amount (i.e. the upper-left corner moves
     * by the specified amount in the negative x and y direction and the width and height grow by
     * twice the specified amount).
     */
    fun grow(dx: Double, dy: Double) {
        x -= dx
        y -= dy
        width += dx + dx
        height += dy + dy
    }

    /**
     * Translates the upper-left corner of this rectangle by the specified amount.
     */
    fun translate(mx: Double, my: Double) {
        x += mx
        y += my
    }

    /**
     * Expands the bounds of this rectangle to contain the specified point.
     */
    fun add(px: Double, py: Double) {
        val x1 = Math.min(x, px)
        val x2 = Math.max(x + width, px)
        val y1 = Math.min(y, py)
        val y2 = Math.max(y + height, py)
        setBounds(x1, y1, x2 - x1, y2 - y1)
    }

    /**
     * Expands the bounds of this rectangle to contain the supplied point.
     */
    fun add(p: XY) {
        add(p.x(), p.y())
    }

    /**
     * Expands the bounds of this rectangle to contain the supplied rectangle.
     */
    fun add(r: IRectangle) {
        val x1 = Math.min(x, r.x())
        val x2 = Math.max(x + width, r.x() + r.width())
        val y1 = Math.min(y, r.y())
        val y2 = Math.max(y + height, r.y() + r.height())
        setBounds(x1, y1, x2 - x1, y2 - y1)
    }

    override // from interface IRectangularShape
    fun x(): Double {
        return x
    }

    override // from interface IRectangularShape
    fun y(): Double {
        return y
    }

    override // from interface IRectangularShape
    fun width(): Double {
        return width
    }

    override // from interface IRectangularShape
    fun height(): Double {
        return height
    }

    override // from RectangularShape
    fun setFrame(x: Double, y: Double, width: Double, height: Double) {
        setBounds(x, y, width, height)
    }

    companion object {
        private const val serialVersionUID = 6584214897153526799L
    }
}
