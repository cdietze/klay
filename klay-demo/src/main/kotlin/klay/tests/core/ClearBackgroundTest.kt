package klay.tests.core

import klay.core.Clock
import klay.core.Color
import klay.scene.ImageLayer
import kotlin.math.cos
import kotlin.math.sin

class ClearBackgroundTest(game: TestsGame) : Test(game, "ClearBackground", "Test that the platform correctly clears the background to black between frames, " + "even if nothing is painted.") {

    override fun init() {
        // remove the background layer added by default
        game.rootLayer.disposeAll()

        // add a grey square
        val surf = game.createSurface(width, height)
        surf.begin().setFillColor(Color.rgb(200, 200, 200)).fillRect(0f, 0f, width, height).end().close()
        val square = ImageLayer(surf.texture)
        game.rootLayer.add(square)

        conns.add(game.paint.connect { clock: Clock ->
            val t = clock.tick / 1000f
            val vsize = game.plat.graphics.viewSize
            square.setTranslation((cos(t) + 1) * (vsize.width - width) / 2,
                    (sin(t) + 1) * (vsize.height - height) / 2)
        })
    }

    companion object {

        internal var width = 100f
        internal var height = 100f
    }
}
