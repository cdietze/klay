//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.i

/**
 * Dimension-related utility methods.
 */
object Dimensions {
    /**
     * Returns a string describing the supplied dimension, of the form `widthxheight`.
     */
    fun dimenToString(width: Int, height: Int): String {
        return width.toString() + "x" + height
    }
}
