//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.i

/**
 * Represents a magnitude in two dimensions.
 */
class Dimension
/**
 * Creates a dimension with the specified width and height.
 */
constructor(
        /** The magnitude in the x-dimension.  */
        override var width: Int = 0,
        /** The magnitude in the y-dimension.  */
        override var height: Int = 0
) : AbstractDimension() {

    /**
     * Creates a dimension with width and height equal to the supplied dimension.
     */
    constructor(d: IDimension) : this(d.width, d.height) {}

    /**
     * Sets the magnitudes of this dimension to the specified width and height.
     */
    fun setSize(width: Int, height: Int) {
        this.width = width
        this.height = height
    }

    /**
     * Sets the magnitudes of this dimension to be equal to the supplied dimension.
     */
    fun setSize(d: IDimension) {
        setSize(d.width, d.height)
    }

    companion object {
        private const val serialVersionUID = 5773214044931265346L
    }
}
/**
 * Creates a dimension with magnitude (0, 0).
 */
