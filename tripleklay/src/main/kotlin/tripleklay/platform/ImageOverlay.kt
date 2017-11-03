package tripleklay.platform

import klay.core.Image

/**
 * A native overlay that simply draws an klay image.
 */
interface ImageOverlay : NativeOverlay {
    /**
     * Gets the image.
     */
    fun image(): Image

    /**
     * Queues up a repaint. Games must call this whenever the image is updated.
     */
    fun repaint()
}
