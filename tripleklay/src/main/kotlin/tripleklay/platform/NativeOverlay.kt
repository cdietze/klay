package tripleklay.platform

import euklid.f.IRectangle

/**
 * A platform element that draws on top of the main klay root layer.
 */
interface NativeOverlay {
    /**
     * Sets the bounds of the overlay, in root coordinates.
     */
    fun setBounds(bounds: IRectangle)

    /**
     * Adds the native overlay to the display. If the overlay is already added, does nothing.
     */
    fun add()

    /**
     * Removes the native overlay from the display. If the overlay is already removed, does
     * nothing.
     */
    fun remove()
}
