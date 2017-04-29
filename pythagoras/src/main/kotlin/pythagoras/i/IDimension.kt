//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.i

/**
 * Provides read-only access to a [Dimension].
 */
interface IDimension {
    /**
     * Returns the magnitude in the x-dimension.
     */
    val width: Int

    /**
     * Returns the magnitude in the y-dimension.
     */
    val height: Int

    /**
     * Returns a mutable copy of this dimension.
     */
    fun clone(): Dimension
}
