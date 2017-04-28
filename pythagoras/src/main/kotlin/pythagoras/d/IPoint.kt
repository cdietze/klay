//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.d

/**
 * Provides read-only access to a [Point].
 */
interface IPoint : XY, Cloneable {
    /** Returns the squared Euclidian distance between this point and the specified point.  */
    fun distanceSq(px: Double, py: Double): Double

    /** Returns the squared Euclidian distance between this point and the supplied point.  */
    fun distanceSq(p: XY): Double

    /** Returns the Euclidian distance between this point and the specified point.  */
    fun distance(px: Double, py: Double): Double

    /** Returns the Euclidian distance between this point and the supplied point.  */
    fun distance(p: XY): Double

    /** Returns the angle (in radians) of the vector starting at this point and ending at the
     * supplied other point.  */
    fun direction(other: XY): Double

    /** Multiplies this point by a scale factor.
     * @return a new point containing the result.
     */
    fun mult(s: Double): Point

    /** Multiplies this point by a scale factor and places the result in the supplied object.
     * @return a reference to the result, for chaining.
     */
    fun mult(s: Double, result: Point): Point

    /** Translates this point by the specified offset.
     * @return a new point containing the result.
     */
    fun add(x: Double, y: Double): Point

    /** Translates this point by the specified offset and stores the result in the object provided.
     * @return a reference to the result, for chaining.
     */
    fun add(x: Double, y: Double, result: Point): Point

    /** Translates this point by the specified offset and stores the result in the object provided.
     * @return a reference to the result, for chaining.
     */
    fun add(other: XY, result: Point): Point

    /** Subtracts the supplied point from `this`.
     * @return a new point containing the result.
     */
    fun subtract(x: Double, y: Double): Point

    /** Subtracts the supplied point from `this` and stores the result in `result`.
     * @return a reference to the result, for chaining.
     */
    fun subtract(x: Double, y: Double, result: Point): Point

    /** Subtracts the supplied point from `this` and stores the result in `result`.
     * @return a reference to the result, for chaining.
     */
    fun subtract(other: XY, result: Point): Point

    /** Rotates this point around the origin by the specified angle.
     * @return a new point containing the result.
     */
    fun rotate(angle: Double): Point

    /** Rotates this point around the origin by the specified angle, storing the result in the
     * point provided.
     * @return a reference to the result point, for chaining.
     */
    fun rotate(angle: Double, result: Point): Point

    /** Returns a mutable copy of this point.  */
    public override fun clone(): Point
}
