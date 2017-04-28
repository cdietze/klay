//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

/**
 * Provides read-only access to a [Dimension].
 */
interface IDimension : Cloneable {
    /**
     * Returns the magnitude in the x-dimension.
     */
    fun width(): Float

    /**
     * Returns the magnitude in the y-dimension.
     */
    fun height(): Float

    /**
     * Returns a mutable copy of this dimension.
     */
    public override fun clone(): Dimension
}
