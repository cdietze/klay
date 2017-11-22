package tripleklay.util

import euklid.f.Dimension
import euklid.f.IDimension
import react.Value

/**
 * A specialized `Value` for dimensions.
 */
class DimensionValue : Value<IDimension> {
    /**
     * Creates a new value with the given dimension.
     */
    constructor(value: IDimension) : super(value) {}

    /**
     * Creates a new value with a new dimension of the given width and height.
     */
    constructor(width: Float, height: Float) : super(Dimension(width, height)) {}

    /**
     * Updates the value to a new dimension of the given width and height.
     */
    fun update(width: Float, height: Float) {
        update(Dimension(width, height))
    }

    /**
     * Updates the value to a new dimension with the current height and the given width.
     */
    fun updateWidth(width: Float) {
        update(Dimension(width, get().height))
    }

    /**
     * Updates the value to a new dimension with the current width and the given height.
     */
    fun updateHeight(height: Float) {
        update(Dimension(get().width, height))
    }
}
