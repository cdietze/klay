//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

/**
 * Provides read-only access to an [Arc].
 */
interface IArc : IRectangularShape {

    /** Returns the type of this arc: [.OPEN], etc.  */
    val arcType: Int

    /** Returns the starting angle of this arc.  */
    val angleStart: Float

    /** Returns the angular extent of this arc.  */
    val angleExtent: Float

    /** Returns the intersection of the ray from the center (defined by the starting angle) and the
     * elliptical boundary of the arc.  */
    val startPoint: Point

    /** Writes the intersection of the ray from the center (defined by the starting angle) and the
     * elliptical boundary of the arc into `target`.
     * @return the supplied point.
     */
    fun startPoint(target: Point): Point

    /** Returns the intersection of the ray from the center (defined by the starting angle plus the
     * angular extent of the arc) and the elliptical boundary of the arc.  */
    val endPoint: Point

    /** Writes the intersection of the ray from the center (defined by the starting angle plus the
     * angular extent of the arc) and the elliptical boundary of the arc into `target`.
     * @return the supplied point.
     */
    fun endPoint(target: Point): Point

    /** Returns whether the specified angle is within the angular extents of this arc.  */
    fun containsAngle(angle: Float): Boolean

    /** Returns a mutable copy of this arc.  */
    fun clone(): Arc

    companion object {
        /** An arc type indicating a simple, unconnected curve.  */
        val OPEN = 0

        /** An arc type indicating a closed curve, connected by a straight line from the starting to
         * the ending point of the arc.  */
        val CHORD = 1

        /** An arc type indicating a closed curve, connected by a line from the starting point of the
         * arc to the center of the circle defining the arc, and another straight line from that center
         * to the ending point of the arc.  */
        val PIE = 2
    }
}
