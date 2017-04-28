//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.i

/**
 * Provides read-only access to a [Point].
 */
interface IPoint : Cloneable {
    /** Returns this point's x-coordinate.  */
    fun x(): Int

    /** Returns this point's y-coordinate.  */
    fun y(): Int

    /** Returns the squared Euclidian distance between this point and the specified point.  */
    fun distanceSq(px: Int, py: Int): Int

    /** Returns the squared Euclidian distance between this point and the supplied point.  */
    fun distanceSq(p: IPoint): Int

    /** Returns the Euclidian distance between this point and the specified point.  */
    fun distance(px: Int, py: Int): Int

    /** Returns the Euclidian distance between this point and the supplied point.  */
    fun distance(p: IPoint): Int

    /** Translates this point by the specified offset.
     * @return a new point containing the result.
     */
    fun add(x: Int, y: Int): Point

    /** Translates this point by the specified offset and stores the result in the object provided.
     * @return a reference to the result, for chaining.
     */
    fun add(x: Int, y: Int, result: Point): Point

    /** Subtracts the supplied point from `this`.
     * @return a new point containing the result.
     */
    fun subtract(x: Int, y: Int): Point

    /** Subtracts the supplied point from `this` and stores the result in `result`.
     * @return a reference to the result, for chaining.
     */
    fun subtract(x: Int, y: Int, result: Point): Point

    /** Subtracts the supplied point from `this` and stores the result in `result`.
     * @return a reference to the result, for chaining.
     */
    fun subtract(other: IPoint, result: Point): Point

    /** Returns a mutable copy of this point.  */
    public override fun clone(): Point
}
