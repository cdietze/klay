package klay.tests.core

import klay.core.Platform
import klay.core.TextFormat
import klay.scene.ImageLayer
import react.Slot
import kotlin.math.round

/**
 * Tests pause/resume notifications.
 */
class PauseResumeTest(game: TestsGame) : Test(game, "PauseResume", "Tests pause/resume notifications.") {

    private val notifications = ArrayList<String>()
    private var layer: ImageLayer? = null

    override fun init() {
        conns.add(game.plat.lifecycle.connect(object : Slot<Platform.Lifecycle> {
            private val start = game.plat.time()
            private fun elapsed(): Int {
                return round((game.plat.time() - start) / 1000).toInt()
            }

            override fun invoke(event: Platform.Lifecycle) {
                when (event) {
                    Platform.Lifecycle.PAUSE -> {
                        game.log.info("Paused " + elapsed())
                        notifications.add("Paused at " + elapsed() + "s")
                    }
                    Platform.Lifecycle.RESUME -> {
                        game.log.info("Resumed " + elapsed())
                        notifications.add("Resumed at " + elapsed() + "s")
                        updateDisplay()
                    }
                    else -> {
                    }
                }// nada
            }
        }))

        layer = ImageLayer()
        game.rootLayer.addAt(layer!!, 15f, 15f)
        updateDisplay()
    }

    private fun updateDisplay() {
        val buf = StringBuilder()
        if (notifications.isEmpty()) {
            buf.append("No notifications. Pause and resume the game to generate some.")
        } else {
            buf.append("Notifications:\n")
            for (note in notifications)
                buf.append(note).append("\n")
        }
        val layout = game.graphics.layoutText(buf.toString(), TextFormat())
        val canvas = game.graphics.createCanvas(layout.size)
        canvas.setFillColor(0xFF000000.toInt()).fillText(layout, 0f, 0f)
        layer!!.setTile(canvas.toTexture())
    }
}
