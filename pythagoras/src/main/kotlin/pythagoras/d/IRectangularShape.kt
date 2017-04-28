//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.d

/**
 * An interface implemented by [IShape] classes whose geometry is defined by a rectangular
 * frame. The framing rectangle *defines* the geometry, but may in some cases differ from
 * the *bounding* rectangle of the shape.
 */
interface IRectangularShape : IShape {
    /** Returns the x-coordinate of the upper-left corner of the framing rectangle.  */
    fun x(): Double

    /** Returns the y-coordinate of the upper-left corner of the framing rectangle.  */
    fun y(): Double

    /** Returns the width of the framing rectangle.  */
    fun width(): Double

    /** Returns the height of the framing rectangle.  */
    fun height(): Double

    /** Returns the minimum x,y-coordinate of the framing rectangle.  */
    fun min(): Point

    /** Returns the minimum x-coordinate of the framing rectangle.  */
    fun minX(): Double

    /** Returns the minimum y-coordinate of the framing rectangle.  */
    fun minY(): Double

    /** Returns the maximum x,y-coordinate of the framing rectangle.  */
    fun max(): Point

    /** Returns the maximum x-coordinate of the framing rectangle.  */
    fun maxX(): Double

    /** Returns the maximum y-coordinate of the framing rectangle.  */
    fun maxY(): Double

    /** Returns the center of the framing rectangle.  */
    fun center(): Point

    /** Returns the x-coordinate of the center of the framing rectangle.  */
    fun centerX(): Double

    /** Returns the y-coordinate of the center of the framing rectangle.  */
    fun centerY(): Double

    /** Returns a copy of this shape's framing rectangle.  */
    fun frame(): Rectangle

    /** Initializes the supplied rectangle with this shape's framing rectangle.
     * @return the supplied rectangle.
     */
    fun frame(target: Rectangle): Rectangle
}
