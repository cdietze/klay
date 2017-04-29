//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

/**
 * Provides read-only access to a [CubicCurve].
 */
interface ICubicCurve : IShape {
    /** Returns the x-coordinate of the start of this curve.  */
    val x1: Float

    /** Returns the y-coordinate of the start of this curve.  */
    val y1: Float

    /** Returns the x-coordinate of the first control point.  */
    val ctrlX1: Float

    /** Returns the y-coordinate of the first control point.  */
    val ctrlY1: Float

    /** Returns the x-coordinate of the second control point.  */
    val ctrlX2: Float

    /** Returns the y-coordinate of the second control point.  */
    val ctrlY2: Float

    /** Returns the x-coordinate of the end of this curve.  */
    val x2: Float

    /** Returns the y-coordinate of the end of this curve.  */
    val y2: Float

    /** Returns a copy of the starting point of this curve.  */
    fun p1(): Point

    /** Returns a copy of the first control point of this curve.  */
    fun ctrlP1(): Point

    /** Returns a copy of the second control point of this curve.  */
    fun ctrlP2(): Point

    /** Returns a copy of the ending point of this curve.  */
    fun p2(): Point

    /** Returns the square of the flatness (maximum distance of a control point from the line
     * connecting the end points) of this curve.  */
    fun flatnessSq(): Float

    /** Returns the flatness (maximum distance of a control point from the line connecting the end
     * points) of this curve.  */
    fun flatness(): Float

    /** Subdivides this curve and stores the results into `left` and `right`.  */
    fun subdivide(left: CubicCurve, right: CubicCurve)

    /** Returns a mutable copy of this curve.  */
    fun clone(): CubicCurve
}
