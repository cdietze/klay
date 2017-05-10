package klay.scene

import klay.core.Canvas
import klay.core.Graphics
import klay.core.Texture
import klay.core.Tile
import pythagoras.f.IDimension
import react.RFuture

/**
 * Simplifies the process of displaying a [Canvas] which is updated after its initial
 * creation. When modifying the canvas, one must call [.begin] to obtain a reference to the
 * canvas, do the desired rendering, then call [.end] to upload the modified image data to
 * the GPU for display by this layer.
 */
class CanvasLayer : klay.scene.ImageLayer {

    private val gfx: Graphics
    private var canvas: Canvas? = null

    /**
     * Creates a canvas layer with a backing canvas of `size` (in display units). This layer
     * will display nothing until a [.begin]/[.end] pair is used to render something to
     * its backing canvas.
     */
    constructor(gfx: Graphics, size: IDimension) : this(gfx, size.width, size.height) {}

    /**
     * Creates a canvas layer with a backing canvas of size `width x height` (in display
     * units). This layer will display nothing until a [.begin]/[.end] pair is used to
     * render something to its backing canvas.
     */
    constructor(gfx: Graphics, width: Float, height: Float) {
        this.gfx = gfx
        resize(width, height)
    }

    /**
     * Creates a canvas layer with the supplied backing canvas. The canvas will immediately be
     * uploaded to the GPU for display.
     */
    constructor(gfx: Graphics, canvas: Canvas) {
        this.gfx = gfx
        this.canvas = canvas
        super.setTile(canvas.image.createTexture(Texture.Config.DEFAULT))
    }

    /**
     * Resizes the canvas that is displayed by this layer.

     *
     * Note: this throws away the old canvas and creates a new blank canvas with the desired size.
     * Thus this should immediately be followed by a [.begin]/[.end] pair which updates
     * the contents of the new canvas. Until then, it will display the old image data.
     */
    fun resize(width: Float, height: Float) {
        if (canvas != null) canvas!!.close()
        canvas = gfx.createCanvas(width, height)
    }

    /** Starts a drawing operation on this layer's backing canvas. Thus must be followed by a call to
     * [.end] when the drawing is complete.  */
    fun begin(): Canvas {
        return canvas!!
    }

    /** Informs this layer that a drawing operation has just completed. The backing canvas image data
     * is uploaded to the GPU.  */
    fun end() {
        val tex = tile() as Texture
        val image = canvas!!.image
        // if our texture is already the right size, just update it
        if (tex != null && tex.pixelWidth == image.pixelWidth() &&
                tex.pixelHeight == image.pixelHeight())
            tex.update(image)
        else
            super.setTile(canvas!!.image.createTexture(Texture.Config.DEFAULT))// otherwise we need to create a new texture (setTexture will unreference the old texture which
        // will cause it to be destroyed)
    }

    override fun setTile(tile: Tile?): ImageLayer {
        if (tile == null || tile is Texture)
            return super.setTile(tile)
        else
            throw UnsupportedOperationException()
    }

    override fun setTile(tile: RFuture<out Tile>): ImageLayer {
        throw UnsupportedOperationException()
    }

    override fun width(): Float {
        return if (forceWidth < 0) canvas!!.width else forceWidth
    }

    override fun height(): Float {
        return if (forceHeight < 0) canvas!!.height else forceHeight
    }

    override fun close() {
        super.close()
        if (canvas != null) {
            canvas!!.close()
            canvas = null
        }
    }
}
