package tripleklay.util

import klay.core.Canvas
import klay.core.Graphics
import klay.scene.CanvasLayer
import klay.scene.GroupLayer
import klay.scene.ImageLayer
import pythagoras.f.IDimension
import react.Closeable

/**
 * Handles the maintenance of a canvas image and layer for displaying a chunk of pre-rendered
 * graphics.
 */
class Glyph : Closeable {
    constructor(parent: GroupLayer) {
        _parent = parent
        _depth = null
    }

    constructor(parent: GroupLayer, depth: Float) {
        _parent = parent
        _depth = depth
    }

    /** Ensures that the canvas image is at least the specified dimensions and cleared to all
     * transparent pixels. Also creates and adds the image layer to the parent layer if needed.  */
    fun prepare(gfx: Graphics, dim: IDimension) {
        prepare(gfx, dim.width, dim.height)
    }

    /** Ensures that the canvas image is at least the specified dimensions and cleared to all
     * transparent pixels. Also creates and adds the image layer to the parent layer if needed.  */
    fun prepare(gfx: Graphics, width: Float, height: Float) {
        var layer = _layer
        if (layer == null) {
            layer = CanvasLayer(gfx, width, height)
            if (_depth != null) layer.setDepth(_depth)
            _parent.add(layer)
            _layer = layer
        } else if (layer.width() < width || layer.height() < height) {
            // TODO: should we ever shrink it?
            layer.resize(width, height)
        }
        _preparedWidth = width
        _preparedHeight = height
    }

    /** Returns the layer that contains our glyph image. Valid after [.prepare].  */
    fun layer(): ImageLayer? {
        return _layer
    }

    /** Starts a drawing session into this glyph's canvas. Call [.end] when drawing is done.
     * Valid after [.prepare].  */
    fun begin(): Canvas {
        return _layer!!.begin().clear()
    }

    /** Completes a drawing sesion into this glyph's canvas and uploads the image data to GPU  */
    fun end() {
        _layer!!.end()
    }

    /** Disposes the layer and image, removing them from the containing widget.  */
    override fun close() {
        if (_layer != null) {
            _layer!!.close()
            _layer = null
        }
        _preparedWidth = 0f
        _preparedHeight = 0f
    }

    /**
     * Returns the width of the last call to [.prepare], or zero if the glyph is not
     * prepared. The canvas should be at least this width, or null if the glyph is not prepared.
     */
    fun preparedWidth(): Float {
        return _preparedWidth
    }

    /**
     * Returns the height of the last call to [.prepare], or zero if the glyph is not
     * prepared. The canvas should be at least this height, or null if the glyph is not prepared.
     */
    fun preparedHeight(): Float {
        return _preparedHeight
    }

    /**
     * Prepares the canvas and renders the supplied text at `x, y` using the given config.
     */
    @JvmOverloads fun renderText(gfx: Graphics, text: StyledText.Plain, x: Int = 0, y: Int = 0) {
        prepare(gfx, text.width(), text.height())
        val canvas = begin()
        text.render(canvas, x.toFloat(), y.toFloat())
        end()
        _layer!!.setTranslation(text.style.effect.offsetX(), text.style.effect.offsetY())
    }

    private val _parent: GroupLayer
    private val _depth: Float?
    private var _layer: CanvasLayer? = null
    private var _preparedWidth: Float = 0.toFloat()
    private var _preparedHeight: Float = 0.toFloat()
}
/**
 * Prepares the canvas and renders the supplied text at 0, 0 using the given config.
 */
