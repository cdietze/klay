package klay.jvm

import klay.core.Canvas
import klay.core.Canvas.*
import java.awt.AlphaComposite
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import java.awt.geom.AffineTransform

internal class JavaCanvasState constructor(var fillColor: Int = 0xff000000.toInt(), var strokeColor: Int = 0xffffffff.toInt(), var fillGradient: JavaGradient? = null, var fillPattern: JavaPattern? = null,
                                           var transform: AffineTransform = AffineTransform(), var strokeWidth: Float = 1.0f, var lineCap: LineCap = LineCap.SQUARE, var lineJoin: LineJoin = LineJoin.MITER,
                                           var miterLimit: Float = 10.0f, var clipper: Clipper = JavaCanvasState.NOCLIP, var composite: Composite = Composite.SRC_OVER, var alpha: Float = 1f) {

    interface Clipper {
        fun setClip(g2d: Graphics2D)
    }

    constructor(toCopy: JavaCanvasState) : this(toCopy.fillColor, toCopy.strokeColor, toCopy.fillGradient, toCopy.fillPattern,
            toCopy.transform, toCopy.strokeWidth, toCopy.lineCap, toCopy.lineJoin, toCopy.miterLimit,
            toCopy.clipper, toCopy.composite, toCopy.alpha)

    // TODO: optimize this so we're not setting this stuff all the time.
    fun prepareClear(gfx: Graphics2D) {
        clipper.setClip(gfx)
    }

    // TODO: optimize this so we're not setting this stuff all the time.
    fun prepareStroke(gfx: Graphics2D) {
        gfx.stroke = BasicStroke(strokeWidth, convertLineCap(), convertLineJoin(), miterLimit)
        gfx.color = convertColor(strokeColor)
        clipper.setClip(gfx)
        gfx.composite = convertComposite(composite, alpha)
    }

    // TODO: optimize this so we're not setting this stuff all the time.
    fun prepareFill(gfx: Graphics2D) {
        if (fillGradient != null)
            gfx.paint = fillGradient!!.paint
        else if (fillPattern != null)
            gfx.paint = fillPattern!!.paint
        else
            gfx.paint = convertColor(fillColor)
        clipper.setClip(gfx)
        gfx.composite = convertComposite(composite, alpha)
    }

    private fun convertComposite(composite: Canvas.Composite, alpha: Float): java.awt.Composite {
        when (composite) {
            Composite.DST_ATOP -> return AlphaComposite.DstAtop.derive(alpha)
            Composite.DST_IN -> return AlphaComposite.DstIn.derive(alpha)
            Composite.DST_OUT -> return AlphaComposite.DstOut.derive(alpha)
            Composite.DST_OVER -> return AlphaComposite.DstOver.derive(alpha)
            Composite.SRC -> return AlphaComposite.Src.derive(alpha)
            Composite.SRC_ATOP -> return AlphaComposite.SrcAtop.derive(alpha)
            Composite.SRC_IN -> return AlphaComposite.SrcIn.derive(alpha)
            Composite.SRC_OUT -> return AlphaComposite.SrcOut.derive(alpha)
            Composite.SRC_OVER -> return AlphaComposite.SrcOver.derive(alpha)
            Composite.XOR -> return AlphaComposite.Xor.derive(alpha)
            Composite.MULTIPLY -> return BlendComposite.Multiply.derive(alpha)
            else -> return AlphaComposite.Src.derive(alpha)
        }
    }

    private fun convertLineCap(): Int {
        when (lineCap) {
            LineCap.BUTT -> return BasicStroke.CAP_BUTT
            LineCap.ROUND -> return BasicStroke.CAP_ROUND
            LineCap.SQUARE -> return BasicStroke.CAP_SQUARE
        }
        return BasicStroke.CAP_SQUARE
    }

    private fun convertLineJoin(): Int {
        when (lineJoin) {
            LineJoin.BEVEL -> return BasicStroke.JOIN_BEVEL
            LineJoin.MITER -> return BasicStroke.JOIN_MITER
            LineJoin.ROUND -> return BasicStroke.JOIN_ROUND
        }
        return BasicStroke.JOIN_MITER
    }

    companion object {

        private val NOCLIP = object : Clipper {
            override fun setClip(gfx: Graphics2D) {
                gfx.clip = null
            }
        }

        fun convertColor(color: Int): Color {
            val a = color.ushr(24) / 255.0f
            val r = (color.ushr(16) and 0xff) / 255.0f
            val g = (color.ushr(8) and 0xff) / 255.0f
            val b = (color and 0xff) / 255.0f

            return Color(r, g, b, a)
        }
    }
}
