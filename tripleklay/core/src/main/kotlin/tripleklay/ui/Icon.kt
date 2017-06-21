package tripleklay.ui

import klay.scene.Layer
import react.RFuture

/**
 * An interface for icons.
 */
interface Icon {
    /**
     * Returns the width of this icon. If the icon is not yet loaded, this should return zero.
     * TODO(cdi) make this into a get-only property (as well as the other funs)
     */
    fun width(): Float

    /**
     * Returns the height of this icon. If the icon is not yet loaded, this should return zero.
     */
    fun height(): Float

    /**
     * Creates a new layer for displaying this icon. The caller is takes ownership of the new layer
     * and is responsible for its destruction.
     */
    fun render(): Layer

    /**
     * A future which is completed when this icon has loaded.
     */
    fun state(): RFuture<Icon>
}
