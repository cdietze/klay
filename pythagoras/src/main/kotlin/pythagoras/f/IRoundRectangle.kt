//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

/**
 * Provides read-only access to a [RoundRectangle].
 */
interface IRoundRectangle : IRectangularShape, Cloneable {
    /** Returns the width of the corner arc.  */
    fun arcWidth(): Float

    /** Returns the height of the corner arc.  */
    fun arcHeight(): Float

    /** Returns a mutable copy of this round rectangle.  */
    public override fun clone(): RoundRectangle
}
