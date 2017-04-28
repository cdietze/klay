//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

import java.io.Serializable

/**
 * Represents a magnitude in two dimensions.
 */
class Dimension
/**
 * Creates a dimension with the specified width and height.
 */
@JvmOverloads constructor(width: Float = 0f, height: Float = 0f) : AbstractDimension(), Serializable {

    /** The magnitude in the x-dimension.  */
    var width: Float = 0.toFloat()

    /** The magnitude in the y-dimension.  */
    var height: Float = 0.toFloat()

    init {
        setSize(width, height)
    }

    /**
     * Creates a dimension with width and height equal to the supplied dimension.
     */
    constructor(d: IDimension) : this(d.width(), d.height()) {}

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
        setSize(d.width(), d.height())
    }

    override // from interface IDimension
    fun width(): Float {
        return width
    }

    override // from interface IDimension
    fun height(): Float {
        return height
    }

    companion object {
        private const val serialVersionUID = 3237732020142181995L
    }
}
/**
 * Creates a dimension with magnitude (0, 0).
 */
