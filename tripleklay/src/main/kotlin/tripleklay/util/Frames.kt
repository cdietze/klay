package tripleklay.util

import euklid.f.IPoint
import euklid.f.IRectangle

/**
 * Models the frames of a flipbook animation. The image frames may be trimmed, in which case the
 * image for a given frame may have an offset within the logical bounds of the entire flipbook.
 */
interface Frames {
    /** Returns the width of a logical frame.  */
    fun width(): Float

    /** Returns the height of a logical frame.  */
    fun height(): Float

    /** Returns the number of frames available.  */
    fun count(): Int

    /** Returns the bounds for the specified frame.  */
    fun bounds(index: Int): IRectangle

    /** Returns the offset (into the logical bounds) of the specified frame.  */
    fun offset(index: Int): IPoint

    /** Configures the supplied image layer with the specified frame. The layer's image will be
     * updated and the layer's translation will be adjusted to the requested frame's offset  */
    fun apply(index: Int, layer: klay.scene.ImageLayer)
}
