package klay.scene

import klay.core.Surface
import klay.core.Texture
import klay.core.Tile
import klay.core.TileSource
import pythagoras.f.IDimension
import pythagoras.f.Rectangle
import react.RFuture

/**
 * A layer that displays a texture or region of a texture (tile). By default, the layer is the same
 * size as its source, but its size can be changed from that default and the layer will either
 * scale or repeat the texture to cause it to fill its bounds depending on the [Texture] it
 * renders.
 */
open class ImageLayer : Layer {

    private var tile: Tile? = null

    /** An explicit width and height for this layer. If the width or height exceeds the underlying
     * tile width or height, it will be scaled or repeated depending on the tile texture's repeat
     * configuration in the pertinent axis. If either value is `< 0` that indicates that the
     * size of the tile being rendered should be used.

     *
     * Note: if you use these sizes in conjunction with a logical origin, you must set them via
     * [.setSize] to cause the origin to be recomputed.  */
    var forceWidth = -1f
    var forceHeight = -1f

    /** The subregion of the tile to render. If this is `null` (the default) the entire tile is
     * rendered. If [.forceWidth] or [.forceHeight] are not set, the width and height of
     * this image layer will be the width and height of the supplied region.

     *
     *  *Note:* when a subregion is configured, a texture will always be scaled, never
     * repeated. If you want to repeat a texture, you have to use the whole texture. This is a
     * limitation of OpenGL.

     *
     * Note: if you use this region in conjunction with a logical origin, you must set it via
     * [.setRegion] to cause the origin to be recomputed.  */
    var region: Rectangle? = null

    /**
     * Creates an image layer with the supplied texture tile.
     */
    constructor(tile: Tile) {
        setTile(tile)
    }

    /**
     * Obtains the tile from `source`, asynchronously if necessary, and displays it. If the
     * source is not ready, this layer will display nothing until it becomes ready and delivers its
     * tile.
     */
    constructor(source: TileSource) {
        setSource(source)
    }

    /**
     * Creates a texture layer with no texture. It will be invisible until a texture is set into it.
     */
    constructor()

    /**
     * Returns the tile rendered by this layer.
     */
    fun tile(): Tile? {
        return tile
    }

    /**
     * Sets the texture rendered by this layer. One can supplied `null` to release and clear
     * any texture currently being rendered and leave this layer in an uninitialized state. This
     * isn't something one would normally do, but could be useful if one was free-listing image
     * layers for some reason.
     */
    open fun setTile(tile: Tile?): ImageLayer {
        // avoid releasing and rereferencing texture if nothing changes
        if (this.tile !== tile) {
            if (this.tile != null) this.tile!!.texture().release()
            this.tile = tile
            checkOrigin()
            if (tile != null) tile.texture().reference()
        }
        return this
    }

    /**
     * Sets the texture rendered by this layer to the texture provided by `source`. If `source` is not yet ready, the texture will be set when it becomes ready. Until then any
     * previous texture will continue to be displayed.
     */
    fun setSource(source: TileSource): ImageLayer {
        if (source.isLoaded)
            setTile(source.tile())
        else
            source.tileAsync().onSuccess { tile: Tile -> setTile(tile) }
        return this
    }

    /**
     * Sets the tile rendered by this layer to the asynchronous result of `tile`. When the
     * future completes, this layer's tile will be set. Until then, the current tile (if any) will
     * continue to be rendered.
     */
    open fun setTile(tile: RFuture<out Tile>): ImageLayer {
        tile.onSuccess { tile: Tile -> setTile(tile) }
        return this
    }

    /** Sets [.forceWidth] and [.forceHeight].
     * @return `this`, for convenient call chaining.
     */
    fun setSize(width: Float, height: Float): ImageLayer {
        forceWidth = width
        forceHeight = height
        checkOrigin()
        return this
    }

    /** Sets [.forceWidth] and [.forceHeight].
     * @return `this`, for convenient call chaining.
     */
    fun setSize(size: IDimension): ImageLayer {
        return setSize(size.width, size.height)
    }

    /** Sets [.region].
     * @return `this`, for convenient call chaining.
     */
    fun setRegion(region: Rectangle): ImageLayer {
        this.region = region
        checkOrigin()
        return this
    }

    override fun width(): Float {
        if (forceWidth >= 0) return forceWidth
        if (region != null) return region!!.width
        return if (tile == null) 0f else tile!!.width()
    }

    override fun height(): Float {
        if (forceHeight >= 0) return forceHeight
        if (region != null) return region!!.height
        return if (tile == null) 0f else tile!!.height()
    }

    override fun close() {
        super.close()
        setTile(null as Tile?)
    }

    override fun paintImpl(surf: Surface) {
        if (tile != null) {
            val dwidth = width()
            val dheight = height()
            if (region == null)
                surf.draw(tile!!, 0f, 0f, dwidth, dheight)
            else
                surf.draw(tile!!, 0f, 0f, dwidth, dheight, region!!.x, region!!.y, region!!.width, region!!.height)
        }
    }

    @Suppress("unused")
    protected fun finalize() {
        setTile(null as Tile?)
    }
}
