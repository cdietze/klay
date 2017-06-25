package tripleklay.ui.bgs

import klay.core.Surface
import klay.core.Tile
import klay.core.Tint
import klay.scene.Layer
import pythagoras.f.IDimension
import tripleklay.ui.Background
import tripleklay.ui.util.Scale9

/**
 * A background constructed by scaling the parts of a source image to fit the target width and
 * height. Uses [Scale9].
 */
class Scale9Background
/** Creates a new background using the given texture. The texture is assumed to be divided into
 * aa 3x3 grid of 9 equal pieces.
 */
(private var _tile: Tile) : Background() {

    private var _s9: Scale9 = Scale9(_tile.width, _tile.height)
    private var _destScale = 1f
    private var _tint = Tint.NOOP_TINT

    /** Returns the scale 9 instance for mutation. Be sure to finish mutation prior to binding.  */
    fun scale9(): Scale9 {
        return _s9
    }

    /** Sets the width of the left and right edges of the source axes to the given value. NOTE:
     * `xborder` may be zero, to indicate that the source image has no left or right pieces,
     * i.e. just three total pieces: top, bottom and center.
     */
    fun xborder(xborder: Float): Scale9Background {
        _s9.xaxis.resize(0, xborder)
        _s9.xaxis.resize(2, xborder)
        return this
    }

    /** Sets the height of the top and bottom edges of the source axes to the given value. NOTE:
     * `yborder` may be zero, to indicate that the source image has no top or bottom pieces,
     * i.e. just three pieces: left, right and center.
     */
    fun yborder(yborder: Float): Scale9Background {
        _s9.yaxis.resize(0, yborder)
        _s9.yaxis.resize(2, yborder)
        return this
    }

    /** Sets all edges of the source axes to the given value. Equivalent of calling `xborder(border).yborder(border)`.
     */
    fun corners(size: Float): Scale9Background {
        return xborder(size).yborder(size)
    }

    /** Sets an overall destination scale for the background. When instantiated, the target width
     * and height are divided by this value, and when rendering the layer scale is multiplied by
     * this value. This allows games to use high res images with smaller screen sizes.
     */
    fun destScale(scale: Float): Scale9Background {
        _destScale = scale
        return this
    }

    override fun instantiate(size: IDimension): Background.Instance {
        return LayerInstance(size, object : Layer() {
            // The destination scale 9.
            internal var dest = Scale9(size.width / _destScale, size.height / _destScale, _s9)

            override fun paintImpl(surf: Surface) {
                surf.saveTx()
                surf.scale(_destScale, _destScale)
                val alpha = this@Scale9Background.alpha
                if (alpha != null) surf.setAlpha(alpha)
                if (_tint != Tint.NOOP_TINT) surf.setTint(_tint)
                // issue the 9 draw calls
                for (yy in 0..2)
                    for (xx in 0..2) {
                        drawPart(surf, xx, yy)
                    }
                if (alpha != null) surf.setAlpha(1f) // alpha is not part of save/restore
                surf.restoreTx()
            }

            private fun drawPart(surf: Surface, x: Int, y: Int) {
                val dw = dest.xaxis.size(x)
                val dh = dest.yaxis.size(y)
                if (dw == 0f || dh == 0f) return
                surf.draw(_tile, dest.xaxis.coord(x), dest.yaxis.coord(y), dw, dh,
                        _s9.xaxis.coord(x), _s9.yaxis.coord(y),
                        _s9.xaxis.size(x), _s9.yaxis.size(y))
            }
        })
    }

    /**
     * Sets the tint for this background, as `ARGB`.

     *
     *  *NOTE:* this will overwrite any value configured via [.alpha]. Either
     * include your desired alpha in the high bits of `tint` or set [.alpha] after
     * calling this method.

     *
     *  *NOTE:* the RGB components of a layer's tint only work on GL-based backends. It
     * is not possible to tint layers using the HTML5 canvas and Flash backends.
     */
    fun setTint(tint: Int): Scale9Background {
        _tint = tint
        this.alpha = (tint shr 24 and 0xFF) / 255f
        return this
    }
}
