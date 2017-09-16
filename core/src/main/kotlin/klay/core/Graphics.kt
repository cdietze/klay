package klay.core

import klay.core.GL20.Companion.GL_CLAMP_TO_EDGE
import klay.core.GL20.Companion.GL_LINEAR
import klay.core.GL20.Companion.GL_LINEAR_MIPMAP_NEAREST
import klay.core.GL20.Companion.GL_NEAREST
import klay.core.GL20.Companion.GL_NEAREST_MIPMAP_NEAREST
import klay.core.GL20.Companion.GL_REPEAT
import klay.core.GL20.Companion.GL_RGBA
import klay.core.GL20.Companion.GL_TEXTURE_2D
import klay.core.GL20.Companion.GL_TEXTURE_MAG_FILTER
import klay.core.GL20.Companion.GL_TEXTURE_MIN_FILTER
import klay.core.GL20.Companion.GL_TEXTURE_WRAP_S
import klay.core.GL20.Companion.GL_TEXTURE_WRAP_T
import klay.core.GL20.Companion.GL_UNSIGNED_BYTE
import pythagoras.f.Dimension
import pythagoras.f.IDimension

/**
 * Provides access to graphics information and services.
 */
abstract class Graphics(open val plat: Platform,
                        /** Provides access to GL services.  */
                        open val gl: GL20,
                        private var scale: Scale) {
    protected val viewSizeM = Dimension()
    private var viewPixelWidth: Int = 0
    private var viewPixelHeight: Int = 0
    private var colorTex: Texture? = null // created lazily

    /** The current size of the graphics viewport.  */
    val viewSize: IDimension = viewSizeM

    /** The render target for the default framebuffer.  */
    var defaultRenderTarget: RenderTarget = object : RenderTarget(this@Graphics) {
        override fun id(): Int {
            return defaultFramebuffer()
        }

        override fun width(): Int {
            return viewPixelWidth
        }

        override fun height(): Int {
            return viewPixelHeight
        }

        override fun xscale(): Float {
            return scale.factor
        }

        override fun yscale(): Float {
            return scale.factor
        }

        override fun flip(): Boolean {
            return true
        }

        override fun close() {} // disable normal dispose-on-close behavior
    }

    /** Returns the display scale factor. This will be [Scale.ONE] except on HiDPI devices that
     * have been configured to use HiDPI mode.  */
    fun scale(): Scale {
        return scale
    }

    /**
     * Returns the size of the screen in display units. On some platforms (like the desktop) the
     * screen size may be larger than the view size.
     */
    abstract fun screenSize(): IDimension

    /**
     * Creates a [Canvas] with the specified display unit size.
     */
    fun createCanvas(width: Float, height: Float): Canvas {
        return createCanvasImpl(scale, scale.scaledCeil(width), scale.scaledCeil(height))
    }

    /** See [.createCanvas].  */
    fun createCanvas(size: IDimension): Canvas {
        return createCanvas(size.width, size.height)
    }

    /**
     * Creates an empty texture into which one can render. The supplied width and height are in
     * display units and will be converted to pixels based on the current scale factor.
     */
    fun createTexture(width: Float, height: Float, config: Texture.Config): Texture {
        val texWidth = config.toTexWidth(scale.scaledCeil(width))
        val texHeight = config.toTexHeight(scale.scaledCeil(height))
        if (texWidth <= 0 || texHeight <= 0)
            throw IllegalArgumentException(
                    "Invalid texture size: " + texWidth + "x" + texHeight)

        val id = createTexture(config)
        gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, texWidth, texHeight,
                0, GL_RGBA, GL_UNSIGNED_BYTE, null)
        return Texture(this, id, config, texWidth, texHeight, scale, width, height)
    }

    /** See [.createTexture].  */
    fun createTexture(size: IDimension, config: Texture.Config): Texture {
        return createTexture(size.width, size.height, config)
    }

    /**
     * Lays out a single line of text using the specified format. The text may subsequently be
     * rendered on a canvas via [(TextLayout,float,float)][Canvas.fillText].
     */
    abstract fun layoutText(text: String, format: TextFormat): TextLayout

    /**
     * Lays out multiple lines of text using the specified format and wrap configuration. The text
     * may subsequently be rendered on a canvas via [(TextLayout,float,float)][Canvas.fillText].
     */
    abstract fun layoutText(text: String, format: TextFormat, wrap: TextWrap): Array<out TextLayout>

    val exec
        get(): Exec {
            return plat.exec
        }

    internal fun colorTex(): Texture {
        if (colorTex == null) {
            val canvas = createCanvas(1f, 1f)
            canvas.setFillColor(0xFFFFFFFF.toInt()).fillRect(0f, 0f, canvas.width, canvas.height)
            colorTex = canvas.toTexture(Texture.Config.UNMANAGED)
        }
        return colorTex!!
    }

    /**
     * Returns the id of the default GL framebuffer. On most platforms this is 0, but not iOS.
     */
    protected fun defaultFramebuffer(): Int {
        return 0
    }

    /**
     * Creates a [Canvas] with the specified pixel size. Because this is used when scaling
     * bitmaps for rendering into POT textures, we need to be precise about the pixel width and
     * height. So make sure this code path uses these exact sizes to make the canvas backing buffer.
     */
    abstract fun createCanvasImpl(scale: Scale, pixelWidth: Int, pixelHeight: Int): Canvas

    /**
     * Informs the graphics system that the main framebuffer scaled has changed.
     */
    protected fun scaleChanged(scale: Scale) {
        // TODO: should we allow this to be reacted to? it only happens on the desktop Java backend...
        this.scale = scale
    }

    /**
     * Informs the graphics system that the main framebuffer size has changed. The supplied size
     * should be in physical pixels.
     */
    protected fun viewportChanged(pixelWidth: Int, pixelHeight: Int) {
        viewPixelWidth = pixelWidth
        viewPixelHeight = pixelHeight
        viewSizeM.width = scale.invScaled(pixelWidth.toFloat())
        viewSizeM.height = scale.invScaled(pixelHeight.toFloat())
        plat.log.info("viewPortChanged ${pixelWidth}x${pixelHeight}/${scale.factor} -> $viewSize")
    }

    internal fun createTexture(config: Texture.Config): Int {
        val id = gl.glGenTexture()
        gl.glBindTexture(GL_TEXTURE_2D, id)
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, config.magFilter)
        val minFilter = mipmapify(config.minFilter, config.mipmaps)
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, minFilter)
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S,
                if (config.repeatX) GL_REPEAT else GL_CLAMP_TO_EDGE)
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T,
                if (config.repeatY) GL_REPEAT else GL_CLAMP_TO_EDGE)
        return id
    }

    companion object {

        protected fun mipmapify(filter: Int, mipmaps: Boolean): Int {
            if (!mipmaps) return filter
            // we don't do trilinear filtering (i.e. GL_LINEAR_MIPMAP_LINEAR);
            // it's expensive and not super useful when only rendering in 2D
            when (filter) {
                GL_NEAREST -> return GL_NEAREST_MIPMAP_NEAREST
                GL_LINEAR -> return GL_LINEAR_MIPMAP_NEAREST
                else -> return filter
            }
        }
    }
}
