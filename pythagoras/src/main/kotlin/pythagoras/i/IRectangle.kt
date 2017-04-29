//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.i

/**
 * Provides read-only access to a [Rectangle].
 */
interface IRectangle : IShape {

    /** Returns the x-coordinate of the upper-left corner of the framing rectangle.  */
    val x: Int

    /** Returns the y-coordinate of the upper-left corner of the framing rectangle.  */
    val y: Int

    /** Returns the width of the framing rectangle.  */
    val width: Int

    /** Returns the height of the framing rectangle.  */
    val height: Int

    /** Returns the minimum x-coordinate of the framing rectangle.  */
    fun minX(): Int

    /** Returns the minimum y-coordinate of the framing rectangle.  */
    fun minY(): Int

    /** Returns the maximum x-coordinate of the framing rectangle. *Note:* this method
     * differs from its floating-point counterparts in that it considers `(x + width - 1)` to
     * be a rectangle's maximum x-coordinate.  */
    fun maxX(): Int

    /** Returns the maximum y-coordinate of the framing rectangle. *Note:* this method
     * differs from its floating-point counterparts in that it considers `(y + height - 1)`
     * to be a rectangle's maximum x-coordinate.  */
    fun maxY(): Int

    /** Returns a copy of this rectangle's upper-left corner.  */
    fun location(): Point

    /** Initializes the supplied point with this rectangle's upper-left corner.
     * @return the supplied point.
     */
    fun location(target: Point): Point

    /** Returns a copy of this rectangle's size.  */
    fun size(): Dimension

    /** Initializes the supplied dimension with this rectangle's size.
     * @return the supplied dimension.
     */
    fun size(target: Dimension): Dimension

    /** Returns the intersection of the specified rectangle and this rectangle (i.e. the largest
     * rectangle contained in both this and the specified rectangle).  */
    fun intersection(rx: Int, ry: Int, rw: Int, rh: Int): Rectangle

    /** Returns the intersection of the supplied rectangle and this rectangle (i.e. the largest
     * rectangle contained in both this and the supplied rectangle).  */
    fun intersection(r: IRectangle): Rectangle

    /** Returns the union of the supplied rectangle and this rectangle (i.e. the smallest rectangle
     * that contains both this and the supplied rectangle).  */
    fun union(r: IRectangle): Rectangle

    /** Returns a set of flags indicating where the specified point lies in relation to the bounds
     * of this rectangle. See [.OUT_LEFT], etc.  */
    fun outcode(px: Int, py: Int): Int

    /** Returns a set of flags indicating where the supplied point lies in relation to the bounds of
     * this rectangle. See [.OUT_LEFT], etc.  */
    fun outcode(point: IPoint): Int

    /** Returns a mutable copy of this rectangle.  */
    fun clone(): Rectangle

    companion object {
        /** The bitmask that indicates that a point lies to the left of this rectangle. See
         * [.outcode].  */
        val OUT_LEFT = 1

        /** The bitmask that indicates that a point lies above this rectangle. See [.outcode].  */
        val OUT_TOP = 2

        /** The bitmask that indicates that a point lies to the right of this rectangle. See
         * [.outcode].  */
        val OUT_RIGHT = 4

        /** The bitmask that indicates that a point lies below this rectangle. See [.outcode].  */
        val OUT_BOTTOM = 8
    }
}
