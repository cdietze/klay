package klay.core

import react.RFuture

/**
 * Bitmapped image data. May be loaded via [Assets] or created dynamically as in the backing
 * image for a [Canvas].
 */
abstract class Image : TileSource, Canvas.Drawable {

    /** Reports the asynchronous loading of this image. This will be completed with success or
     * failure when the image's asynchronous load completes.  */
    val state: RFuture<Image>

    /**
     * Returns whether this image is fully loaded. In general you'll want to react to
     * [.state], but this method is useful when you need to assert that something is only
     * allowed on a fully loaded image.
     */
    override val isLoaded: Boolean
        get() = state.isCompleteNow

    /**
     * Returns the scale of resolution independent pixels to actual pixels for this image. This will
     * be [Scale.ONE] unless HiDPI images are being used.
     */
    abstract val scale: Scale

    /**
     * This image's width in display units. If this image is loaded asynchrously, this will return
     * 0 until loading is complete. See [.state].
     */
    override val width: Float
        get() = scale.invScaled(pixelWidth.toFloat())

    /**
     * This image's height in display units. If this image is loaded asynchrously, this will return
     * 0 until loading is complete. See [.state].
     */
    override val height: Float
        get() = scale.invScaled(pixelHeight.toFloat())

    /**
     * Returns the width of this image in physical pixels. If this image is loaded asynchrously,
     * this will return 0 until loading is complete. See [.state].
     */
    abstract val pixelWidth: Int

    /**
     * Returns the height of this image in physical pixels. If this image is loaded asynchrously,
     * this will return 0 until loading is complete. See [.state].
     */
    abstract val pixelHeight: Int

    /**
     * Extracts pixel data from a rectangular area of this image. This method may perform poorly, in
     * particular on the HTML platform.

     * The returned pixel format is `(alpha << 24 | red << 16 | green << 8 | blue)`, where
     * alpha, red, green and blue are the corresponding channel values, ranging from 0 to 255
     * inclusive.

     * @param startX x-coordinate of the upper left corner of the area.
     * *
     * @param startY y-coordinate of the upper left corner of the area.
     * *
     * @param width width of the area.
     * *
     * @param height height of the area.
     * *
     * @param rgbArray will be filled with the pixel data from the area
     * *
     * @param offset fill start offset in rgbArray.
     * *
     * @param scanSize number of pixels in a row in rgbArray.
     */
    abstract fun getRgb(startX: Int, startY: Int, width: Int, height: Int, rgbArray: IntArray,
                        offset: Int, scanSize: Int)

    /**
     * Sets pixel data for a rectangular area of this image. This method may perform poorly, in
     * particular on the HTML platform. On the HTML platform, due to brower security limitations,
     * this method is only allowed on images created via [Graphics.createCanvas].

     * The pixel format is `(alpha << 24 | red << 16 | green << 8 | blue)`, where alpha, red,
     * green and blue are the corresponding channel values, ranging from 0 to 255 inclusive.

     * @param startX x-coordinate of the upper left corner of the area.
     * *
     * @param startY y-coordinate of the upper left corner of the area.
     * *
     * @param width width of the area.
     * *
     * @param height height of the area.
     * *
     * @param rgbArray will be filled with the pixel data from the area
     * *
     * @param offset fill start offset in rgbArray.
     * *
     * @param scanSize number of pixels in a row in rgbArray.
     */
    abstract fun setRgb(startX: Int, startY: Int, width: Int, height: Int, rgbArray: IntArray,
                        offset: Int, scanSize: Int)

    /**
     * Creates a pattern from this image which can be used to fill a canvas.
     */
    abstract fun createPattern(repeatX: Boolean, repeatY: Boolean): Pattern

    /**
     * Sets the texture config used when creating this image's default texture. Note: this must be
     * called before the first call to [.texture] so that it is configured before the default
     * texture is created and cached.
     */
    fun setConfig(config: Texture.Config): Image {
        texconf = config
        return this
    }

    /**
     * Returns, creating if necessary, this image's default texture. When the texture is created, it
     * will use the [Texture.Config] set via [.setConfig]. If an image's default texture
     * is [Texture.close]d, a subsequent call to this method will create a new default texture.
     */
    fun texture(): Texture {
        if (_texture == null || _texture!!.disposed()) _texture = createTexture(texconf)
        return _texture!!
    }

    /**
     * Updates this image's default texture with the current contents of the image, and returns the
     * texture. If the texture has not yet been created, then this simply creates it. This is only
     * necessary if you want to update the default texture for an image associated with a [ ], or if you have used [.setRgb] to change the contents of this image.
     */
    fun updateTexture(): Texture {
        if (_texture == null || _texture!!.disposed())
            _texture = createTexture(texconf)
        else
            _texture!!.update(this)
        return _texture!!
    }

    /**
     * Returns a future which will deliver the default texture for this image once its loading has
     * completed. Uses [.texture] to create the texture.
     */
    fun textureAsync(): RFuture<Texture> {
        return state.map { texture() }
    }

    /**
     * Creates a texture with this image's bitmap data using `config`. NOTE: this creates a new
     * texture with every call. This is generally only needed if you plan to create multiple textures
     * from the same bitmap, with different configurations. Otherwise just use [.texture] to
     * create the image's "default" texture which will be shared by all callers.
     */
    fun createTexture(config: Texture.Config): Texture {
        if (!isLoaded)
            throw IllegalStateException(
                    "Cannot create texture from unready image: " + this)

        val texWidth = config.toTexWidth(pixelWidth)
        val texHeight = config.toTexHeight(pixelHeight)
        if (texWidth <= 0 || texHeight <= 0)
            throw IllegalArgumentException(
                    "Invalid texture size: " + texWidth + "x" + texHeight + " from: " + this)

        val tex = Texture(gfx, gfx.createTexture(config), config, texWidth, texHeight,
                scale, width, height)
        tex.update(this) // this will handle non-POT source image conversion
        return tex
    }

    /** A region of an image which can be rendered to [Canvas]es and turned into a texture
     * (which is a [Tile] of the original image's texture).  */
    abstract class Region : TileSource, Canvas.Drawable

    /** Returns a region of this image which can be drawn independently.  */
    fun region(rx: Float, ry: Float, rwidth: Float, rheight: Float): Region {
        val image = this
        return object : Region() {
            private var tile: Tile? = null
            override val isLoaded: Boolean
                get() = image.isLoaded

            override fun tile(): Tile {
                if (tile == null) tile = image.texture().tile(rx, ry, rwidth, rheight)
                return tile!!
            }

            override fun tileAsync(): RFuture<Tile> {
                return image.state.map { tile() }
            }

            override val width: Float = rwidth
            override val height: Float = rheight

            override fun draw(gc: Any, x: Float, y: Float, width: Float, height: Float) {
                image.draw(gc, x, y, width, height, rx, ry, rwidth, rheight)
            }

            override fun draw(gc: Any, dx: Float, dy: Float, dw: Float, dh: Float,
                              sx: Float, sy: Float, sw: Float, sh: Float) {
                image.draw(gc, dx, dy, dw, dh, rx + sx, ry + sy, sw, sh)
            }
        }
    }

    /** Used with [.transform].  */
    interface BitmapTransformer

    /**
     * Generates a new image from this image's bitmap, using a transformer created for the platform
     * in use. See `JavaBitmapTransformer` for example.
     */
    abstract fun transform(xform: BitmapTransformer): Image

    override fun tile(): Tile {
        return texture()
    }

    override fun tileAsync(): RFuture<Tile> {
        return state.map { texture() }
    }

    protected val gfx: Graphics
    protected var texconf: Texture.Config = Texture.Config.DEFAULT
    protected var _texture: Texture? = null

    protected constructor(gfx: Graphics, state: RFuture<Image>) {
        this.gfx = gfx
        this.state = state
    }

    // this ctor is used for images that are constructed immediately with their images
    protected constructor(gfx: Graphics) {
        this.gfx = gfx
        this.state = RFuture.success(this)
    }

    /** Uploads this image's data into `tex`.  */
    abstract fun upload(gfx: Graphics, tex: Texture)
}
