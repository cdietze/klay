//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

/**
 * An interface provided by all shapes.
 */
interface IShape {
    /** Returns true if this shape encloses no area.  */
    val isEmpty: Boolean

    /** Returns true if this shape contains the specified point.  */
    fun contains(x: Float, y: Float): Boolean

    /** Returns true if this shape contains the supplied point.  */
    operator fun contains(point: XY): Boolean

    /** Returns true if this shape completely contains the specified rectangle.  */
    fun contains(x: Float, y: Float, width: Float, height: Float): Boolean

    /** Returns true if this shape completely contains the supplied rectangle.  */
    operator fun contains(r: IRectangle): Boolean

    /** Returns true if this shape intersects the specified rectangle.  */
    fun intersects(x: Float, y: Float, width: Float, height: Float): Boolean

    /** Returns true if this shape intersects the supplied rectangle.  */
    fun intersects(r: IRectangle): Boolean

    /** Returns a copy of the bounding rectangle for this shape.  */
    fun bounds(): Rectangle

    /** Initializes the supplied rectangle with this shape's bounding rectangle.
     * @return the supplied rectangle.
     */
    fun bounds(target: Rectangle): Rectangle

    /**
     * Returns an iterator over the path described by this shape.

     * @param at if supplied, the points in the path are transformed using this.
     */
    fun pathIterator(at: Transform): PathIterator

    /**
     * Returns an iterator over the path described by this shape.

     * @param at if supplied, the points in the path are transformed using this.
     * *
     * @param flatness when approximating curved segments with lines, this controls the maximum
     * *        distance the lines are allowed to deviate from the approximated curve, thus a higher
     * *        flatness value generally allows for a path with fewer segments.
     */
    fun pathIterator(at: Transform, flatness: Float): PathIterator
}
