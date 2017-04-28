//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

/**
 * Provides read-only access to a [Line].
 */
interface ILine : IShape, Cloneable {
    /** Returns the x-coordinate of the start of this line.  */
    fun x1(): Float

    /** Returns the y-coordinate of the start of this line.  */
    fun y1(): Float

    /** Returns the x-coordinate of the end of this line.  */
    fun x2(): Float

    /** Returns the y-coordinate of the end of this line.  */
    fun y2(): Float

    /** Returns a copy of the starting point of this line.  */
    fun p1(): Point

    /** Initializes the supplied point with this line's starting point.
     * @return the supplied point.
     */
    fun p1(target: Point): Point

    /** Returns a copy of the ending point of this line.  */
    fun p2(): Point

    /** Initializes the supplied point with this line's ending point.
     * @return the supplied point.
     */
    fun p2(target: Point): Point

    /** Returns the square of the distance from the specified point to the line defined by this
     * line segment.  */
    fun pointLineDistSq(px: Float, py: Float): Float

    /** Returns the square of the distance from the supplied point to the line defined by this line
     * segment.  */
    fun pointLineDistSq(p: XY): Float

    /** Returns the distance from the specified point to the line defined by this line segment.  */
    fun pointLineDist(px: Float, py: Float): Float

    /** Returns the distance from the supplied point to the line defined by this line segment.  */
    fun pointLineDist(p: XY): Float

    /** Returns the square of the distance from the specified point this line segment.  */
    fun pointSegDistSq(px: Float, py: Float): Float

    /** Returns the square of the distance from the supplied point this line segment.  */
    fun pointSegDistSq(p: XY): Float

    /** Returns the distance from the specified point this line segment.  */
    fun pointSegDist(px: Float, py: Float): Float

    /** Returns the distance from the supplied point this line segment.  */
    fun pointSegDist(p: XY): Float

    /** Returns an indicator of where the specified point (px,py) lies with respect to this line
     * segment.  */
    fun relativeCCW(px: Float, py: Float): Int

    /** Returns an indicator of where the specified point lies with respect to this line segment.  */
    fun relativeCCW(p: XY): Int

    /** Returns a mutable copy of this line.  */
    public override fun clone(): Line
}
