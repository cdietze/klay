package klay.tests.core

import klay.core.Clock
import klay.core.GL20
import klay.core.Pointer
import klay.core.Texture
import klay.scene.ImageLayer
import react.RFuture
import kotlin.math.abs
import kotlin.math.sin

class ImageScalingTest(game: TestsGame) : Test(game, "ImageScaling", "Tests use of min/mag filters and mipmapping when scaling images.") {

    private var paused = false

    override fun init() {
        val princess = game.assets.getImage("images/princess.png")
        val star = game.assets.getImage("images/star.png")

        RFuture.collect(listOf(princess.state, star.state)).onSuccess { _ ->
            // the second princess and (64x64) star images are mipmapped
            val phwidth = princess.width / 2f
            val phheight = princess.height / 2f
            val player1 = ImageLayer(princess)
            player1.setOrigin(phwidth, phheight)
            game.rootLayer.addAt(player1, 100f, 100f)
            val player2 = ImageLayer(princess.createTexture(MIPMAPPED))
            player2.setOrigin(phwidth, phheight)
            game.rootLayer.addAt(player2, 250f, 100f)

            val shwidth = star.width / 2
            val shheight = star.height / 2
            val slayer1 = ImageLayer(star)
            slayer1.setOrigin(shwidth, shheight)
            game.rootLayer.addAt(slayer1, 100f, 250f)
            val slayer2 = ImageLayer(star.createTexture(MIPMAPPED))
            slayer2.setOrigin(shwidth, shheight)
            game.rootLayer.addAt(slayer2, 250f, 250f)

            conns.add(game.pointer.events.connect { event: Pointer.Event ->
                when (event.kind) {
                    Pointer.Event.Kind.START -> paused = true
                    Pointer.Event.Kind.END, Pointer.Event.Kind.CANCEL -> paused = false
                }
            })

            var elapsed: Float = 0.toFloat()
            conns.add(game.paint.connect { clock: Clock ->
                if (!paused) {
                    elapsed += clock.dt / 1000f
                    val scale = abs(sin(elapsed))
                    player1.setScale(scale)
                    player2.setScale(scale)
                    slayer1.setScale(scale)
                    slayer2.setScale(scale)
                }
            })
        }
    }

    companion object {

        private val MIPMAPPED = Texture.Config(
                true, false, false, GL20.GL_LINEAR, GL20.GL_LINEAR, true)
    }
}
