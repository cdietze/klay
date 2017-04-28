//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

/**
 * Dimension-related utility methods.
 */
object Dimensions {
    /** A dimension width zero width and height.  */
    val ZERO: IDimension = Dimension(0f, 0f)

    /**
     * Returns a string describing the supplied dimension, of the form `widthxheight`.
     */
    fun dimenToString(width: Float, height: Float): String {
        return width.toString() + "x" + height
    }
}
