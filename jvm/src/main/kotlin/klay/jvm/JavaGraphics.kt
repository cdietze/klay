package klay.jvm

import klay.core.*
import java.awt.RenderingHints
import java.awt.font.FontRenderContext
import java.awt.image.BufferedImage
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

abstract class JavaGraphics(override val plat: Platform, gl20: GL20, scale: Scale) : Graphics(plat, gl20, scale) {

    private val fonts = HashMap<String, java.awt.Font>()
    private var imgBuf: ByteBuffer? = null

    /**
     * Antialiased font context.
     * Initialized lazily to avoid doing any AWT stuff during startup
     */
    internal val aaFontContext: FontRenderContext by lazy {
        // set up the dummy font contexts
        val aaGfx = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics()
        aaGfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        aaGfx.fontRenderContext
    }

    /**
     * Aliased font context.
     * Initialized lazily to avoid doing any AWT stuff during startup
     */
    internal val aFontContext: FontRenderContext by lazy {
        val aGfx = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).createGraphics()
        aGfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF)
        aGfx.fontRenderContext
    }

    /** Sets the title of the window.  */
    internal abstract fun setTitle(title: String)

    /** Uploads the image data in `img` into `tex`.  */
    internal abstract fun upload(img: BufferedImage, tex: Texture)

    /**
     * Registers a font with the graphics system.

     * @param name the name under which to register the font.
     * *
     * @param font the Java font, which can be loaded from a path via [JavaAssets.getFont].
     */
    fun registerFont(name: String, font: java.awt.Font?) {
        if (font == null) throw NullPointerException()
        fonts.put(name, font)
    }

    /**
     * Changes the size of the window. The supplied size is in display units, it will be
     * converted to pixels based on the display scale factor.
     */
    abstract fun setSize(width: Int, height: Int, fullscreen: Boolean)

    override fun layoutText(text: String, format: TextFormat): TextLayout {
        return JavaTextLayout.layoutText(this, text, format)
    }

    override fun layoutText(text: String, format: TextFormat, wrap: TextWrap): Array<out TextLayout> {
        return JavaTextLayout.layoutText(this, text, format, wrap)
    }

    override fun createCanvasImpl(scale: Scale, pixelWidth: Int, pixelHeight: Int): Canvas {
        val bitmap = BufferedImage(
                pixelWidth, pixelHeight, BufferedImage.TYPE_INT_ARGB_PRE)
        return JavaCanvas(this, JavaImage(this, scale, bitmap, "<canvas>"))
    }

    internal fun resolveFont(font: Font): java.awt.Font {
        var jfont: java.awt.Font? = fonts[font.name]
        // if we don't have a custom font registered for this name, assume it's a platform font
        if (jfont == null) {
            jfont = java.awt.Font(font.name, java.awt.Font.PLAIN, 12)
            fonts.put(font.name, jfont)
        }
        // derive a font instance at the desired style and size
        return jfont.deriveFont(STYLE_TO_JAVA[font.style.ordinal], font.size)
    }

    internal fun checkGetImageBuffer(byteSize: Int): ByteBuffer {
        if (imgBuf == null) imgBuf = createImageBuffer(Math.max(1024, byteSize))
        if (imgBuf!!.capacity() >= byteSize)
            imgBuf!!.clear() // reuse it!
        else
            imgBuf = createImageBuffer(byteSize)
        return imgBuf!!
    }

    companion object {

        /** Converts the given image into a format for quick upload to the GPU.  */
        internal fun convertImage(image: BufferedImage): BufferedImage {
            when (image.type) {
                BufferedImage.TYPE_INT_ARGB_PRE -> return image // Already good to go
                BufferedImage.TYPE_4BYTE_ABGR -> {
                    image.coerceData(true) // Just premultiply the alpha and it's fine
                    return image
                }
            }

            // Didn't know an easy thing to do, so create a whole new image in our preferred format
            val convertedImage = BufferedImage(image.width, image.height,
                    BufferedImage.TYPE_INT_ARGB_PRE)
            val g = convertedImage.createGraphics()
            g.color = java.awt.Color(0f, 0f, 0f, 0f)
            g.fillRect(0, 0, image.width, image.height)
            g.drawImage(image, 0, 0, null)
            g.dispose()

            return convertedImage
        }

        private fun createImageBuffer(byteSize: Int): ByteBuffer {
            return ByteBuffer.allocateDirect(byteSize).order(ByteOrder.nativeOrder())
        }

        // this matches the order in Font.Style
        private val STYLE_TO_JAVA = intArrayOf(java.awt.Font.PLAIN, java.awt.Font.BOLD, java.awt.Font.ITALIC, java.awt.Font.BOLD or java.awt.Font.ITALIC)
    }
}
