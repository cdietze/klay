package klay.tests.core

import klay.core.*
import klay.scene.*

import java.util.ArrayList

/**
 * Tests sound playback support.
 */
class SoundTest(game: TestsGame) : Test(game, "Sound", "Tests playing and looping sounds.") {

    override fun init() {
        var x = 50f
        val actions = CanvasLayer(game.graphics, 300f, 300f)

        val fanfare = loadSound("sounds/fanfare")
        x = addButton("Play Fanfare", Runnable {
            fanfare.play()
            addAction(actions, "Played Fanfare.")
        }, x, 100f)

        val lfanfare = loadSound("sounds/fanfare")
        lfanfare.setLooping(true)
        x = addLoopButtons(actions, "Fanfare", lfanfare, x)

        val bling = loadSound("sounds/bling")
        bling.setLooping(true)
        x = addLoopButtons(actions, "Bling", bling, x)

        game.rootLayer.addAt(actions, 50f, 150f)
    }

    private fun loadSound(path: String): Sound {
        val sound = game.assets.getSound(path)
        sound.state.onFailure(logFailure("Sound loading error: " + path))
        return sound
    }

    private fun addLoopButtons(actions: CanvasLayer, name: String, sound: Sound,
                               x: Float): Float {
        var x = x
        x = addButton("Loop " + name, Runnable {
            if (!sound.isPlaying) {
                sound.play()
                addAction(actions, "Starting looping $name.")
            }
        }, x, 100f)
        x = addButton("Stop Loop " + name, Runnable {
            if (sound.isPlaying) {
                sound.stop()
                addAction(actions, "Stopped looping $name.")
            }
        }, x, 100f)
        return x
    }

    private fun addAction(actions: CanvasLayer, action: String) {
        _actions.add(0, action)
        if (_actions.size > 10)
            _actions.subList(10, _actions.size).clear()

        val canvas = actions.begin()
        canvas.clear()
        val buf = StringBuilder()
        for (a in _actions) {
            if (buf.isNotEmpty()) buf.append("\n")
            buf.append(a)
        }
        canvas.setFillColor(0xFF000000.toInt())

        var y = 0f
        for (layout in game.graphics.layoutText(
                buf.toString(), game.ui.TEXT_FMT, TextWrap(300f))) {
            canvas.fillText(layout, 0f, y)
            y += layout.ascent() + layout.descent() + layout.leading()
        }
        actions.end()
    }

    private val _actions: MutableList<String> = ArrayList()
}
