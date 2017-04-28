//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.d

/**
 * Dimension-related utility methods.
 */
object Dimensions {
    /** A dimension width zero width and height.  */
    val ZERO: IDimension = Dimension(0.0, 0.0)

    /**
     * Returns a string describing the supplied dimension, of the form `widthxheight`.
     */
    fun dimenToString(width: Double, height: Double): String {
        return width.toString() + "x" + height
    }
}
