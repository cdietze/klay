package klay.jvm

import klay.core.TextFormat
import klay.core.TextWrap
import pythagoras.f.Rectangle
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.font.LineBreakMeasurer
import java.awt.font.TextAttribute
import java.awt.font.TextLayout
import java.text.AttributedString
import java.util.*

internal class JavaTextLayout(text: String, format: TextFormat, private val layout: TextLayout) : klay.core.TextLayout(text, format, JavaTextLayout.computeBounds(layout), layout.ascent + layout.descent) {

    override fun ascent(): Float {
        return layout.ascent
    }

    override fun descent(): Float {
        return layout.descent
    }

    override fun leading(): Float {
        return layout.leading
    }

    fun stroke(gfx: Graphics2D, x: Float, y: Float) {
        paint(gfx, x, y, true)
    }

    fun fill(gfx: Graphics2D, x: Float, y: Float) {
        paint(gfx, x, y, false)
    }

    fun paint(gfx: Graphics2D, x: Float, y: Float, stroke: Boolean) {
        val ohint = gfx.getRenderingHint(RenderingHints.KEY_ANTIALIASING)
        try {
            gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, if (format.antialias)
                RenderingHints.VALUE_ANTIALIAS_ON
            else
                RenderingHints.VALUE_ANTIALIAS_OFF)

            val yoff = y + layout.ascent
            if (stroke) {
                gfx.translate(x.toDouble(), yoff.toDouble())
                gfx.draw(layout.getOutline(null))
                gfx.translate((-x).toDouble(), (-yoff).toDouble())
            } else {
                layout.draw(gfx, x, yoff)
            }

        } finally {
            gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, ohint)
        }
    }

    companion object {

        fun layoutText(gfx: JavaGraphics, text: String, format: TextFormat): JavaTextLayout {
            // we do some fiddling to work around the fact that TextLayout chokes on the empty string
            val astring = AttributedString(if (text.isEmpty()) " " else text)
            format.font?.let {
                astring.addAttribute(TextAttribute.FONT, gfx.resolveFont(it))
            }
            val frc = if (format.antialias) gfx.aaFontContext else gfx.aFontContext
            return JavaTextLayout(text, format, TextLayout(astring.iterator, frc))
        }

        fun layoutText(gfx: JavaGraphics, text: String, format: TextFormat,
                       wrap: TextWrap): Array<JavaTextLayout> {
            var text = text
            // normalize newlines in the text (Windows: CRLF -> LF, Mac OS pre-X: CR -> LF)
            text = normalizeEOL(text)

            // we do some fiddling to work around the fact that TextLayout chokes on the empty string
            val ltext = if (text.isEmpty()) " " else text

            // set up an attributed character iterator so that we can measure the text
            val astring = AttributedString(ltext)
            format.font?.let {
                astring.addAttribute(TextAttribute.FONT, gfx.resolveFont(it))
            }

            val layouts = ArrayList<JavaTextLayout>()
            val frc = if (format.antialias) gfx.aaFontContext else gfx.aFontContext
            val measurer = LineBreakMeasurer(astring.iterator, frc)
            val lastPos = ltext.length
            var curPos = 0
            val eol: Char = '\n'
            while (curPos < lastPos) {
                var nextRet = ltext.indexOf(eol, measurer.position + 1)
                if (nextRet == -1) {
                    nextRet = lastPos
                }
                val layout = measurer.nextLayout(wrap.width, nextRet, false)
                val endPos = measurer.position
                while (curPos < endPos && ltext[curPos] == eol)
                    curPos += 1 // skip over EOLs
                layouts.add(JavaTextLayout(ltext.substring(curPos, endPos), format, layout))
                curPos = endPos
            }
            return layouts.toTypedArray()
        }

        private fun computeBounds(layout: TextLayout): Rectangle {
            val bounds = layout.bounds
            // the y position of the bounds includes a negative ascent, but we don't want that showing up
            // in our bounds since we render from 0 rather than from the baseline
            return Rectangle(bounds.x.toFloat(), bounds.y.toFloat() + layout.ascent,
                    bounds.width.toFloat(), bounds.height.toFloat())
        }
    }
}
