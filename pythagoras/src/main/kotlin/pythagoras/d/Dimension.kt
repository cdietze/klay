//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.d

import java.io.Serializable

/**
 * Represents a magnitude in two dimensions.
 */
class Dimension
/**
 * Creates a dimension with the specified width and height.
 */
@JvmOverloads constructor(width: Double = 0.0, height: Double = 0.0) : AbstractDimension(), Serializable {

    /** The magnitude in the x-dimension.  */
    var width: Double = 0.toDouble()

    /** The magnitude in the y-dimension.  */
    var height: Double = 0.toDouble()

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
    fun setSize(width: Double, height: Double) {
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
    fun width(): Double {
        return width
    }

    override // from interface IDimension
    fun height(): Double {
        return height
    }

    companion object {
        private const val serialVersionUID = 6057102762997878357L
    }
}
/**
 * Creates a dimension with magnitude (0, 0).
 */
