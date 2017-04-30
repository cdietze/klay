//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

/**
 * Provides read-only access to an [Ellipse].
 */
interface IEllipse : IRectangularShape {
    /** Returns a mutable copy of this ellipse.  */
    fun clone(): Ellipse
}
