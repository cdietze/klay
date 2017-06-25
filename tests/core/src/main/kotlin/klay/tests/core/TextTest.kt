package klay.tests.core

import klay.core.*
import klay.scene.ImageLayer
import pythagoras.f.Rectangle

class TextTest(game: TestsGame) : Test(game, "Text", "Tests various text rendering features.") {

    private inner class NToggle<T>(name: String, vararg values: T) : TestsGame.NToggle<T>(name, *values) {
        override fun set(idx: Int) {
            super.set(idx)
            update()
        }
    }

    private var style: NToggle<Font.Style>? = null
    private var draw: NToggle<String>? = null
    private var effect: NToggle<String>? = null
    private var align: NToggle<TextBlock.Align>? = null
    private var font: NToggle<String>? = null
    private var wrap: NToggle<Int>? = null
    private var lineBounds: NToggle<Boolean>? = null
    private val outlineWidth = 2f
    private var sample = "The quick brown fox\njumped over the lazy dog.\nEvery good boy deserves fudge."
    private var text: ImageLayer? = null
    private var row: Rectangle? = null

    override fun init() {
        row = Rectangle(5f, 5f, 0f, 0f)
        style = NToggle("Style", *Font.Style.values())
        addToRow(style!!.layer)
        draw = NToggle("Draw", "Fill", "Stroke")
        addToRow(draw!!.layer)
        effect = NToggle(
                "Effect", "None", "ShadowUL", "ShadowLR", "Outline")
        addToRow(effect!!.layer)
        wrap = NToggle("Wrap", 0, 20, 50, 100)
        addToRow(wrap!!.layer)
        align = NToggle(
                "Align", TextBlock.Align.LEFT, TextBlock.Align.CENTER, TextBlock.Align.RIGHT)
        addToRow(align!!.layer)
        font = NToggle("Font", "Times New Roman", "Helvetica")
        addToRow(font!!.layer)

        val layer = game.ui.createButton("Set Text", Runnable {
            game.input.getText(Keyboard.TextType.DEFAULT, "Test text", sample.replace("\n", "\\n")).onSuccess { text: String? ->
                if (text == null) return@onSuccess
                // parse \n to allow testing line breaks
                sample = text.replace("\\n", "\n")
                update()
            }
        })
        addToRow(layer)
        lineBounds = NToggle("Lines", java.lang.Boolean.FALSE, java.lang.Boolean.TRUE)
        addToRow(lineBounds!!.layer)

        // test laying out the empty string
        val layout = game.graphics.layoutText("", TextFormat())
        val empty = ImageLayer(makeLabel(
                "Empty string size " + layout.size.width + "x" + layout.size.height))
        newRow()
        addToRow(empty)

        newRow()

        text = ImageLayer(makeTextImage())
        addToRow(text!!)
    }

    private fun addToRow(layer: ImageLayer) {
        game.rootLayer.add(layer.setTranslation(row!!.x + row!!.width, row!!.y))
        row!!.width += layer.width() + 45
        row!!.height = Math.max(row!!.height, layer.height())
        if (row!!.width > game.graphics.viewSize.width * .6f) newRow()
    }

    private fun newRow() {
        row!!.x = 5f
        row!!.y += row!!.height + 5
        row!!.height = 0f
        row!!.width = row!!.height
    }

    private fun update() {
        if (text == null) return
        text!!.setTile(makeTextImage())
    }

    private fun makeLabel(label: String): Texture {
        val layout = game.graphics.layoutText(label, TextFormat())
        val canvas = game.graphics.createCanvas(layout.size)
        canvas.setFillColor(0xFF000000.toInt()).fillText(layout, 0f, 0f)
        return canvas.toTexture()
    }

    private fun makeTextImage(): Texture {
        val format = TextFormat(Font(font!!.value(), style!!.value(), 24f))
        val wrapWidth = if (wrap!!.value() == 0)
            java.lang.Float.MAX_VALUE
        else
            game.graphics.viewSize.width * wrap!!.value() / 100
        val block = TextBlock(
                game.graphics.layoutText(sample, format, TextWrap(wrapWidth)))
        val awidth = adjustWidth(block.bounds.width)
        val aheight = adjustHeight(block.bounds.height)
        val pad = 1 / game.graphics.scale().factor
        val canvas = game.graphics.createCanvas(awidth + 2 * pad, aheight + 2 * pad)
        canvas.translate(pad, pad)
        canvas.setStrokeColor(0xFFFFCCCC.toInt()).strokeRect(0f, 0f, awidth, aheight)
        render(canvas, block, align!!.value(), lineBounds!!.value())
        return canvas.toTexture()
    }

    private fun adjustDim(value: Float): Float {
        var value = value
        val effect = this.effect!!.value()
        if (effect.startsWith("Shadow")) {
            value += 2f
        } else if (effect == "Outline") {
            value += outlineWidth * 2
        }
        return value
    }

    private fun adjustWidth(width: Float): Float {
        return adjustDim(width)
    }

    private fun adjustHeight(height: Float): Float {
        return adjustDim(height)
    }

    private fun render(canvas: Canvas, strokeFill: String, block: TextBlock, align: TextBlock.Align,
                       color: Int, x: Float, y: Float, showBounds: Boolean) {
        var sy = y + block.bounds.y
        for (layout in block.lines) {
            val sx = x + block.bounds.x + align.getX(
                    layout.size.width, block.bounds.width - block.bounds.x)
            if (showBounds) {
                val lbounds = layout.bounds
                canvas.setStrokeColor(0xFFFFCCCC.toInt()).setStrokeWidth(1f)
                canvas.strokeRect(sx + lbounds.x, sy + lbounds.y, lbounds.width, lbounds.height)
            }
            if (strokeFill == "Fill") {
                canvas.setFillColor(color).fillText(layout, sx, sy)
            } else {
                canvas.setStrokeColor(color).strokeText(layout, sx, sy)
            }
            sy += layout.ascent() + layout.descent() + layout.leading()
        }
    }

    private fun render(canvas: Canvas, block: TextBlock, align: TextBlock.Align, showBounds: Boolean) {
        val effect = this.effect!!.value()
        val strokeFill = draw!!.value()
        if (effect == "ShadowUL") {
            render(canvas, strokeFill, block, align, 0xFFCCCCCC.toInt(), 0f, 0f, showBounds)
            render(canvas, strokeFill, block, align, 0xFF6699CC.toInt(), 2f, 2f, false)
        } else if (effect == "ShadowLR") {
            render(canvas, strokeFill, block, align, 0xFFCCCCCC.toInt(), 2f, 2f, false)
            render(canvas, strokeFill, block, align, 0xFF6699CC.toInt(), 0f, 0f, showBounds)
        } else if (effect == "Outline") {
            canvas.setStrokeWidth(2 * outlineWidth)
            canvas.setLineCap(Canvas.LineCap.ROUND)
            canvas.setLineJoin(Canvas.LineJoin.ROUND)
            render(canvas, "Stroke", block, align, 0xFF336699.toInt(),
                    outlineWidth, outlineWidth, false)
            render(canvas, "Fill", block, align, 0xFF6699CC.toInt(),
                    outlineWidth, outlineWidth, showBounds)
        } else {
            render(canvas, strokeFill, block, align, 0xFF6699CC.toInt(), 0f, 0f, showBounds)
        }
    }
}
