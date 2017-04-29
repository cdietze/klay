//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

/**
 * Represents a magnitude in two dimensions.
 */
class Dimension
/**
 * Creates a dimension with the specified width and height,
 * using (0,0) as default.
*/
constructor(width: Float = 0f, height: Float = 0f) : AbstractDimension() {

    /** The magnitude in the x-dimension.  */
    override var width: Float = 0.toFloat()

    /** The magnitude in the y-dimension.  */
    override var height: Float = 0.toFloat()

    init {
        setSize(width, height)
    }



    /**
     * Creates a dimension with width and height equal to the supplied dimension.
     */
    constructor(d: IDimension) : this(d.width, d.height) {}

    /**
     * Sets the magnitudes of this dimension to the specified width and height.
     */
    fun setSize(width: Float, height: Float) {
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
        private const val serialVersionUID = 3237732020142181995L
    }
}
