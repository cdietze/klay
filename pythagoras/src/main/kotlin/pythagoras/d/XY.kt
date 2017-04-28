//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.d

/**
 * Defines an x/y coordinate. This is implemented by both `Point` and `Vector` so that
 * APIs which require an x/y coordinate, but don't really want to mak the distinction between a
 * translation vector versus a point in 2D space, can simply accept both.
 */
interface XY {
    /** The x coordinate.  */
    fun x(): Double

    /** The y coordinate.  */
    fun y(): Double
}
