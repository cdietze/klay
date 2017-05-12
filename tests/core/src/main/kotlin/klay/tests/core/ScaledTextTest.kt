package klay.tests.core

import klay.core.*
import klay.scene.ImageLayer

class ScaledTextTest(game: TestsGame) : Test(game, "ScaledText", "Tests that text rendering to scaled Canvas works properly.") {

    override fun init() {
        val text = "The quick brown fox jumped over the lazy dog."
        val format = TextFormat(Font("Helvetica", 18f))
        val block = TextBlock(game.graphics.layoutText(text, format, TextWrap(100f)))

        var x = 5f
        for (scale in floatArrayOf(1f, 2f, 3f)) {
            val swidth = block.bounds.width * scale
            val sheight = block.bounds.height * scale
            val canvas = game.graphics.createCanvas(swidth, sheight)
            canvas.setStrokeColor(0xFFFFCCCC.toInt()).strokeRect(0f, 0f, swidth - 0.5f, sheight - 0.5f)
            canvas.scale(scale, scale)
            canvas.setFillColor(0xFF000000.toInt())
            block.fill(canvas, TextBlock.Align.RIGHT, 0f, 0f)
            game.rootLayer.addAt(ImageLayer(canvas.toTexture()), x, 5f)
            addInfo(canvas, x + swidth / 2, sheight + 10)
            x += swidth + 5
        }
    }

    protected fun addInfo(canvas: Canvas, cx: Float, y: Float) {
        val infoFormat = TextFormat(Font("Helvetica", 12f))
        val ilayout = game.graphics.layoutText("${canvas.width}x${canvas.height}", infoFormat)
        val iimage = game.graphics.createCanvas(ilayout.size)
        iimage.setFillColor(0xFF000000.toInt()).fillText(ilayout, 0f, 0f)
        game.rootLayer.addAt(ImageLayer(iimage.toTexture()), cx - iimage.width / 2, y)
    }
}
