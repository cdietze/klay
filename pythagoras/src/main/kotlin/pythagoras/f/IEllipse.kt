//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

/**
 * Provides read-only access to an [Ellipse].
 */
interface IEllipse : IRectangularShape, Cloneable {
    /** Returns a mutable copy of this ellipse.  */
    public override fun clone(): Ellipse
}
