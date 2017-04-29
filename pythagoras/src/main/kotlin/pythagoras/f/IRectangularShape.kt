//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

/**
 * An interface implemented by [IShape] classes whose geometry is defined by a rectangular
 * frame. The framing rectangle *defines* the geometry, but may in some cases differ from
 * the *bounding* rectangle of the shape.
 */
interface IRectangularShape : IShape {
    /** Returns the x-coordinate of the upper-left corner of the framing rectangle.  */
    val x: Float

    /** Returns the y-coordinate of the upper-left corner of the framing rectangle.  */
    val y: Float

    /** Returns the width of the framing rectangle.  */
    val width: Float

    /** Returns the height of the framing rectangle.  */
    val height: Float

    /** Returns the minimum x,y-coordinate of the framing rectangle.  */
    val min: Point

    /** Returns the minimum x-coordinate of the framing rectangle.  */
    val minX: Float

    /** Returns the minimum y-coordinate of the framing rectangle.  */
    val minY: Float

    /** Returns the maximum x,y-coordinate of the framing rectangle.  */
    val max: Point

    /** Returns the maximum x-coordinate of the framing rectangle.  */
    val maxX: Float

    /** Returns the maximum y-coordinate of the framing rectangle.  */
    val maxY: Float

    /** Returns the center of the framing rectangle.  */
    val center: Point

    /** Returns the x-coordinate of the center of the framing rectangle.  */
    val centerX: Float

    /** Returns the y-coordinate of the center of the framing rectangle.  */
    val centerY: Float

    /** Returns a copy of this shape's framing rectangle.  */
    fun frame(): Rectangle

    /** Initializes the supplied rectangle with this shape's framing rectangle.
     * @return the supplied rectangle.
     */
    fun frame(target: Rectangle): Rectangle
}
