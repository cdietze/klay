package klay.jvm

import klay.core.*
import klay.core.Graphics
import klay.core.Image
import pythagoras.f.MathUtil
import java.awt.*
import java.awt.geom.AffineTransform
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage

class JavaImage : ImageImpl {
    private var img: BufferedImage? = null

    constructor(gfx: Graphics, scale: Scale, img: BufferedImage, source: String) : super(gfx, scale, img.width, img.height, source, img)

    constructor(plat: JavaPlatform, async: Boolean, preWidth: Int, preHeight: Int, source: String) : super(plat, async, Scale.ONE, preWidth, preHeight, source)

    /**
     * Returns the [BufferedImage] that underlies this image. This is for games that need to
     * write custom backend code to do special stuff. No promises are made, caveat coder.
     */
    fun bufferedImage(): BufferedImage? {
        return img
    }

    override fun createPattern(repeatX: Boolean, repeatY: Boolean): Pattern {
        assert(img !=
                null) { "Cannot generate a pattern from unready image." }
        val rect = Rectangle2D.Float(0f, 0f, width, height)
        return JavaPattern(repeatX, repeatY, TexturePaint(img, rect))
    }

    override fun getRgb(startX: Int, startY: Int, width: Int, height: Int,
                        rgbArray: IntArray, offset: Int, scanSize: Int) {
        img!!.getRGB(startX, startY, width, height, rgbArray, offset, scanSize)
    }

    override fun setRgb(startX: Int, startY: Int, width: Int, height: Int,
                        rgbArray: IntArray, offset: Int, scanSize: Int) {
        img!!.setRGB(startX, startY, width, height, rgbArray, offset, scanSize)
    }

    override fun transform(xform: BitmapTransformer): Image {
        return JavaImage(gfx, scale, (xform as JavaBitmapTransformer).transform(img!!), source)
    }

    override fun draw(gc: Any, x: Float, y: Float, width: Float, height: Float) {
        // using img.getWidth/Height here accounts for ctx.scale.factor
        val tx = AffineTransform(width / img!!.width, 0f, 0f,
                height / img!!.height, x, y)
        (gc as Graphics2D).drawImage(img, tx, null)
    }

    override fun draw(gc: Any, dx: Float, dy: Float, dw: Float, dh: Float,
                      sx: Float, sy: Float, sw: Float, sh: Float) {
        // adjust our source rect to account for the scale factor
        val f = scale.factor
        val _sx = sx * f
        val _sy = sy * f
        val _sw = sw * f
        val _sh = sh * f
        // now render the image through a clip and with a scaling transform, so that only the desired
        // source rect is rendered, and is rendered into the desired target region
        val scaleX = dw / _sw
        val scaleY = dh / _sh
        val gfx = gc as Graphics2D
        val oclip = gfx.clip
        gfx.clipRect(MathUtil.ifloor(dx), MathUtil.ifloor(dy), MathUtil.iceil(dw), MathUtil.iceil(dh))
        gfx.drawImage(img, AffineTransform(scaleX, 0f, 0f, scaleY, dx - _sx * scaleX, dy - _sy * scaleY), null)
        gfx.clip = oclip
    }

    override fun toString(): String {
        return "Image[src=$source, img=$img]"
    }

    override fun upload(gfx: Graphics, tex: Texture) {
        (gfx as JavaGraphics).upload(img!!, tex)
    }

    override fun setBitmap(bitmap: Any) {
        img = bitmap as BufferedImage
    }

    override fun createErrorBitmap(pixelWidth: Int, pixelHeight: Int): Any {
        val img = BufferedImage(pixelWidth, pixelHeight, BufferedImage.TYPE_INT_ARGB_PRE)
        val g = img.createGraphics()
        try {
            g.color = java.awt.Color.red
            for (yy in 0..pixelHeight / 15) {
                for (xx in 0..pixelWidth / 45) {
                    g.drawString("ERROR", xx * 45, yy * 15)
                }
            }
        } finally {
            g.dispose()
        }
        return img
    }
}
