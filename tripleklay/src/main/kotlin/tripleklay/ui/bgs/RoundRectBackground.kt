package tripleklay.ui.bgs

import klay.core.Graphics
import klay.scene.ImageLayer
import euklid.f.IDimension
import tripleklay.ui.Background

/**
 * Draws a rounded rectangle with optional border as a background.
 */
class RoundRectBackground constructor(private val _gfx: Graphics, private val _bgColor: Int, private val _radius: Float,
                                      private val _borderColor: Int = 0, private val _borderWidth: Float = 0f, private val _borderRadius: Float = _radius) : Background() {

    override fun instantiate(size: IDimension): Background.Instance {
        val canvas = _gfx.createCanvas(size)
        if (_borderWidth > 0) {
            canvas.setFillColor(_borderColor).fillRoundRect(
                    0f, 0f, size.width, size.height, _radius)
            // scale the inner radius based on the ratio of the inner height to the full height;
            // this improves the uniformity of the border substantially
            val iwidth = size.width - 2 * _borderWidth
            val iheight = size.height - 2 * _borderWidth
            val iradius = _borderRadius * (iheight / size.height)
            canvas.setFillColor(_bgColor).fillRoundRect(
                    _borderWidth, _borderWidth, iwidth, iheight, iradius)
        } else {
            canvas.setFillColor(_bgColor).fillRoundRect(0f, 0f, size.width, size.height, _radius)
        }
        val layer = ImageLayer(canvas.toTexture())
        return LayerInstance(size, layer)
    }
}
