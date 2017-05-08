package klay.core

import pythagoras.f.IRectangle
import pythagoras.f.Rectangle

/**
 * Encapsulates a block of multi-line text. This code handles all the fiddly "fonts sometimes
 * extend outside their reported bounds" hackery that was once embedded into the various backend
 * code. It also handles text alignment.
 */
class TextBlock
/**
 * Creates a text block with the supplied `lines`.
 */
(
        /** The individual lines of text in this block. Obtained by a call to
         * [Graphics.layoutText].  */
        val lines: Array<TextLayout>) {

    /** Used to align a block of text.  */
    enum class Align {
        LEFT {
            override fun getX(lineWidth: Float, blockWidth: Float): Float {
                return 0f
            }
        },
        CENTER {
            override fun getX(lineWidth: Float, blockWidth: Float): Float {
                return (blockWidth - lineWidth) / 2
            }
        },
        RIGHT {
            override fun getX(lineWidth: Float, blockWidth: Float): Float {
                return blockWidth - lineWidth
            }
        };

        /** Returns the x offset for a line of text with width `lineWidth` rendered as part of a
         * block of width `blockWidth`.  */
        abstract fun getX(lineWidth: Float, blockWidth: Float): Float
    }

    /** The bounds of this block of text. The `x` component of the bounds may be positive,
     * indicating that the text should be rendered at that offset. This is to account for the fact
     * that some text renders to the left of its reported origin due to font extravagance. The [ ][.stroke] and [.fill] methods automatically take into account this x coordinate, the
     * caller need only account for it if they choose to render [.lines] manually.  */
    val bounds: IRectangle

    init {
        this.bounds = getBounds(lines, Rectangle())
    }

    /**
     * Returns the width of the rendered text. This is the width that should be used when computing
     * alignment for text in this block.
     */
    fun textWidth(): Float {
        return bounds.width - bounds.x
    }

    /**
     * Fills `lines` into `canvas` at the specified coordinates, using the specified
     * alignment.
     */
    fun fill(canvas: Canvas, align: Align, x: Float, y: Float) {
        var sy = y + bounds.y
        for (line in lines) {
            val sx = x + bounds.x + align.getX(line.size.width, textWidth())
            canvas.fillText(line, sx, sy)
            sy += line.ascent() + line.descent() + line.leading()
        }
    }

    /**
     * Strokes `lines` into `canvas` at the specified coordinates, using the specified
     * alignment.
     */
    fun stroke(canvas: Canvas, align: Align, x: Float, y: Float) {
        var sy = y + bounds.y
        for (line in lines) {
            val sx = x + bounds.x + align.getX(line.size.width, textWidth())
            canvas.strokeText(line, sx, sy)
            sy += line.ascent() + line.descent() + line.leading()
        }
    }

    /**
     * Creates a canvas image large enough to accommodate this text block and renders the lines into
     * it. The image will include padding around the edge to ensure that antialiasing has a bit of
     * extra space to do its work.
     */
    fun toCanvas(gfx: Graphics, align: Align, fillColor: Int): Canvas {
        val pad = 1 / gfx.scale().factor
        val canvas = gfx.createCanvas(bounds.width + 2 * pad, bounds.height + 2 * pad)
        canvas.setFillColor(fillColor)
        fill(canvas, align, pad, pad)
        return canvas
    }

    companion object {

        /** Computes the bounds of a block of text. The `x` component of the bounds may be
         * positive, indicating that the text should be rendered at that offset. This is to account for
         * the fact that some text renders to the left of its reported origin due to font
         * extravagance.  */
        fun getBounds(lines: Array<TextLayout>, into: Rectangle): Rectangle {
            // some font glyphs start rendering at a negative inset, blowing outside their bounding box
            // (naughty!); in such cases, we use xAdjust to shift everything to the right to ensure that we
            // don't paint outside our reported bounding box (so that someone can create a single canvas of
            // bounding box size and render this text layout into it at (0,0) and nothing will get cut off)
            var xAdjust = 0f
            var twidth = 0f
            var theight = 0f
            for (layout in lines) {
                val bounds = layout.bounds
                xAdjust = maxOf(xAdjust, -minOf(0f, bounds.x))
                // we use layout.width() here not bounds width because layout width includes extra space
                // needed for lines that start rendering at a positive x offset whereas bounds.width() is
                // only the precise width of the rendered text
                twidth = maxOf(twidth, layout.size.width)
                if (layout !== lines[0])
                    theight += layout.leading() // leading only applied to lines after 0
                theight += layout.ascent() + layout.descent()
            }
            into.setBounds(xAdjust, 0f, xAdjust + twidth, theight)
            return into
        }
    }
}
