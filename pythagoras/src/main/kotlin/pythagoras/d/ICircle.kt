//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.d

/**
 * Provides read-only access to a [Circle].
 */
interface ICircle {
    /** Returns this circle's x-coordinate.  */
    fun x(): Double

    /** Returns this circle's y-coordinate.  */
    fun y(): Double

    /** Returns this circle's radius.  */
    fun radius(): Double

    /** Returns true if this circle intersects the supplied circle.  */
    fun intersects(c: ICircle): Boolean

    /** Returns true if this circle contains the supplied point.  */
    operator fun contains(p: XY): Boolean

    /** Returns true if this circle contains the specified point.  */
    fun contains(x: Double, y: Double): Boolean

    /** Translates the circle by the specified offset.
     * @return a new Circle containing the result.
     */
    fun offset(x: Double, y: Double): Circle

    /** Translates the circle by the specified offset and stores the result in the supplied object.
     * @return a reference to the result, for chaining.
     */
    fun offset(x: Double, y: Double, result: Circle): Circle

    /** Returns a mutable copy of this circle.  */
    fun clone(): Circle
}
