package klay.tests.core

import euklid.f.MathUtil
import klay.core.Clock
import klay.scene.CanvasLayer
import kotlin.math.cos
import kotlin.math.sin

class CanvasStressTest(game: TestsGame) : Test(game, "Canvas Stress", "Animates a full-screen sized canvas, forcing a massive reupload of image data to " + "the GPU on every frame.") {

    override fun init() {
        val clayer = CanvasLayer(game.graphics, game.graphics.viewSize)
        game.rootLayer.add(clayer)

        var noSegs = 30
        var direction = 1
        conns.add(game.update.connect { clock: Clock ->
            val canvas = clayer.begin()
            canvas.clear()
            canvas.setStrokeWidth(3f)
            canvas.setStrokeColor(0x88ff0000.toInt())

            noSegs += direction
            if (noSegs > 50) direction = -1
            if (noSegs < 20) direction = 1

            val r = 100f
            for (ii in 0..noSegs - 1) {
                val angle = 2 * MathUtil.PI * ii / noSegs
                val viewSize = game.plat.graphics.viewSize
                val x = r * cos(angle) + viewSize.width / 2
                val y = r * sin(angle) + viewSize.height / 2
                canvas.strokeCircle(x, y, 100f)
            }

            clayer.end() // reupload the image data
        })
    }
}
