//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

/**
 * Provides read-only access to a [RoundRectangle].
 */
interface IRoundRectangle : IRectangularShape {
    /** Returns the width of the corner arc.  */
    val arcWidth: Float

    /** Returns the height of the corner arc.  */
    val arcHeight: Float

    /** Returns a mutable copy of this round rectangle.  */
    fun clone(): RoundRectangle
}
