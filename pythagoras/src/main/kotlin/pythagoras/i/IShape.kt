//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.i

/**
 * An interface provided by all shapes.
 */
interface IShape {
    /** Returns true if this shape encloses no area.  */
    val isEmpty: Boolean

    /** Returns true if this shape contains the specified point.  */
    fun contains(x: Int, y: Int): Boolean

    /** Returns true if this shape contains the supplied point.  */
    operator fun contains(point: IPoint): Boolean

    /** Returns true if this shape completely contains the specified rectangle.  */
    fun contains(x: Int, y: Int, width: Int, height: Int): Boolean

    /** Returns true if this shape completely contains the supplied rectangle.  */
    operator fun contains(r: IRectangle): Boolean

    /** Returns true if this shape intersects the specified rectangle.  */
    fun intersects(x: Int, y: Int, width: Int, height: Int): Boolean

    /** Returns true if this shape intersects the supplied rectangle.  */
    fun intersects(r: IRectangle): Boolean

    /** Returns a copy of the bounding rectangle for this shape.  */
    fun bounds(): Rectangle

    /** Initializes the supplied rectangle with this shape's bounding rectangle.
     * @return the supplied rectangle.
     */
    fun bounds(target: Rectangle): Rectangle
}
