//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.d

/**
 * Provides read-only access to a [Dimension].
 */
interface IDimension : Cloneable {
    /**
     * Returns the magnitude in the x-dimension.
     */
    fun width(): Double

    /**
     * Returns the magnitude in the y-dimension.
     */
    fun height(): Double

    /**
     * Returns a mutable copy of this dimension.
     */
    public override fun clone(): Dimension
}
