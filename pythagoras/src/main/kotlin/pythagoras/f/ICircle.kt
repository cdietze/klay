//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

/**
 * Provides read-only access to a [Circle].
 */
interface ICircle {
    /** Returns this circle's x-coordinate.  */
    val x: Float

    /** Returns this circle's y-coordinate.  */
    val y: Float

    /** Returns this circle's radius.  */
    val radius: Float

    /** Returns true if this circle intersects the supplied circle.  */
    fun intersects(c: ICircle): Boolean

    /** Returns true if this circle contains the supplied point.  */
    operator fun contains(p: XY): Boolean

    /** Returns true if this circle contains the specified point.  */
    fun contains(x: Float, y: Float): Boolean

    /** Translates the circle by the specified offset.
     * @return a new Circle containing the result.
     */
    fun offset(x: Float, y: Float): Circle

    /** Translates the circle by the specified offset and stores the result in the supplied object.
     * @return a reference to the result, for chaining.
     */
    fun offset(x: Float, y: Float, result: Circle): Circle

    /** Returns a mutable copy of this circle.  */
    fun clone(): Circle
}
