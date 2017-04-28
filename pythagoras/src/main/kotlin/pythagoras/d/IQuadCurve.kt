//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.d

/**
 * Provides read-only access to a [QuadCurve].
 */
interface IQuadCurve : IShape, Cloneable {
    /** Returns the x-coordinate of the start of this curve.  */
    fun x1(): Double

    /** Returns the y-coordinate of the start of this curve.  */
    fun y1(): Double

    /** Returns the x-coordinate of the control point.  */
    fun ctrlX(): Double

    /** Returns the y-coordinate of the control point.  */
    fun ctrlY(): Double

    /** Returns the x-coordinate of the end of this curve.  */
    fun x2(): Double

    /** Returns the y-coordinate of the end of this curve.  */
    fun y2(): Double

    /** Returns a copy of the starting point of this curve.  */
    fun p1(): Point

    /** Returns a copy of the control point of this curve.  */
    fun ctrlP(): Point

    /** Returns a copy of the ending point of this curve.  */
    fun p2(): Point

    /** Returns the square of the flatness (maximum distance of a control point from the line
     * connecting the end points) of this curve.  */
    fun flatnessSq(): Double

    /** Returns the flatness (maximum distance of a control point from the line connecting the end
     * points) of this curve.  */
    fun flatness(): Double

    /** Subdivides this curve and stores the results into `left` and `right`.  */
    fun subdivide(left: QuadCurve, right: QuadCurve)

    /** Returns a mutable copy of this curve.  */
    public override fun clone(): QuadCurve
}
