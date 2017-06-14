package klay.tests.core

import klay.core.*
import klay.scene.ImageLayer
import klay.scene.Pointer

class UI(private val game: TestsGame) {
    val BUTTON_FMT: TextFormat
    val TEXT_FMT: TextFormat

    init {
        BUTTON_FMT = TextFormat(Font("Helvetica", 24f))
        TEXT_FMT = TextFormat(Font("Helvetica", 12f))
    }

    fun formatText(format: TextFormat, text: String, border: Boolean): Texture {
        val layout = game.graphics.layoutText(text, format)
        val margin = (if (border) 10 else 0).toFloat()
        val width = layout.size.width + 2 * margin
        val height = layout.size.height + 2 * margin
        val canvas = game.graphics.createCanvas(width, height)
        if (border) canvas.setFillColor(0xFFCCCCCC.toInt()).fillRect(0f, 0f, canvas.width, canvas.height)
        canvas.setFillColor(0xFF000000.toInt()).fillText(layout, margin, margin)
        if (border) canvas.setStrokeColor(0xFF000000.toInt()).strokeRect(0f, 0f, width - 1, height - 1)
        return canvas.toTexture()
    }

    fun formatText(text: String, border: Boolean): Texture {
        return formatText(TEXT_FMT, text, border)
    }

    fun wrapText(text: String, width: Float, align: TextBlock.Align): Texture {
        val layouts = game.graphics.layoutText(text, TEXT_FMT, TextWrap(width))
        val canvas = TextBlock(layouts).toCanvas(game.graphics, align, 0xFF000000.toInt())
        return canvas.toTexture()
    }

    fun formatButton(label: String): Texture {
        return formatText(BUTTON_FMT, label, true)
    }

    fun createButton(label: String, onClick: Runnable): ImageLayer {
        val layer = ImageLayer(formatButton(label))
        layer.events().connect(object : Pointer.Listener {
            override fun onStart(iact: Pointer.Interaction) {
                onClick.run()
            }
        })
        return layer
    }
}
