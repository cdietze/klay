//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

/**
 * Provides read-only access to a [QuadCurve].
 */
interface IQuadCurve : IShape {
    /** Returns the x-coordinate of the start of this curve.  */
    val x1: Float

    /** Returns the y-coordinate of the start of this curve.  */
    val y1: Float

    /** Returns the x-coordinate of the control point.  */
    val ctrlX: Float

    /** Returns the y-coordinate of the control point.  */
    val ctrlY: Float

    /** Returns the x-coordinate of the end of this curve.  */
    val x2: Float

    /** Returns the y-coordinate of the end of this curve.  */
    val y2: Float

    /** Returns a copy of the starting point of this curve.  */
    fun p1(): Point

    /** Returns a copy of the control point of this curve.  */
    fun ctrlP(): Point

    /** Returns a copy of the ending point of this curve.  */
    fun p2(): Point

    /** Returns the square of the flatness (maximum distance of a control point from the line
     * connecting the end points) of this curve.  */
    fun flatnessSq(): Float

    /** Returns the flatness (maximum distance of a control point from the line connecting the end
     * points) of this curve.  */
    fun flatness(): Float

    /** Subdivides this curve and stores the results into `left` and `right`.  */
    fun subdivide(left: QuadCurve, right: QuadCurve)

    /** Returns a mutable copy of this curve.  */
    fun clone(): QuadCurve
}
