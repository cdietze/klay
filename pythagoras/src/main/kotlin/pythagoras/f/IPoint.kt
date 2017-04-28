//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

/**
 * Provides read-only access to a [Point].
 */
interface IPoint : XY, Cloneable {
    /** Returns the squared Euclidian distance between this point and the specified point.  */
    fun distanceSq(px: Float, py: Float): Float

    /** Returns the squared Euclidian distance between this point and the supplied point.  */
    fun distanceSq(p: XY): Float

    /** Returns the Euclidian distance between this point and the specified point.  */
    fun distance(px: Float, py: Float): Float

    /** Returns the Euclidian distance between this point and the supplied point.  */
    fun distance(p: XY): Float

    /** Returns the angle (in radians) of the vector starting at this point and ending at the
     * supplied other point.  */
    fun direction(other: XY): Float

    /** Multiplies this point by a scale factor.
     * @return a new point containing the result.
     */
    fun mult(s: Float): Point

    /** Multiplies this point by a scale factor and places the result in the supplied object.
     * @return a reference to the result, for chaining.
     */
    fun mult(s: Float, result: Point): Point

    /** Translates this point by the specified offset.
     * @return a new point containing the result.
     */
    fun add(x: Float, y: Float): Point

    /** Translates this point by the specified offset and stores the result in the object provided.
     * @return a reference to the result, for chaining.
     */
    fun add(x: Float, y: Float, result: Point): Point

    /** Translates this point by the specified offset and stores the result in the object provided.
     * @return a reference to the result, for chaining.
     */
    fun add(other: XY, result: Point): Point

    /** Subtracts the supplied point from `this`.
     * @return a new point containing the result.
     */
    fun subtract(x: Float, y: Float): Point

    /** Subtracts the supplied point from `this` and stores the result in `result`.
     * @return a reference to the result, for chaining.
     */
    fun subtract(x: Float, y: Float, result: Point): Point

    /** Subtracts the supplied point from `this` and stores the result in `result`.
     * @return a reference to the result, for chaining.
     */
    fun subtract(other: XY, result: Point): Point

    /** Rotates this point around the origin by the specified angle.
     * @return a new point containing the result.
     */
    fun rotate(angle: Float): Point

    /** Rotates this point around the origin by the specified angle, storing the result in the
     * point provided.
     * @return a reference to the result point, for chaining.
     */
    fun rotate(angle: Float, result: Point): Point

    /** Returns a mutable copy of this point.  */
    public override fun clone(): Point
}
