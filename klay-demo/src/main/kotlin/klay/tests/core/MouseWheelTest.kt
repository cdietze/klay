package klay.tests.core

import klay.scene.GroupLayer
import klay.scene.ImageLayer
import klay.scene.Mouse

class MouseWheelTest(game: TestsGame) : Test(game, "MouseWheel", "Tests mouse wheel movement on layers") {

    override fun init() {
        val bgcanvas = game.graphics.createCanvas(WIDTH + 10, HEIGHT)
        bgcanvas.setFillColor(0xff808080.toInt())
        bgcanvas.fillRect(0f, 0f, WIDTH + 10, HEIGHT)
        val bg = ImageLayer(bgcanvas.toTexture())

        val knob = game.graphics.createCanvas(WIDTH, HWIDTH)
        knob.setFillColor(0xffffffff.toInt()).fillRect(0f, 0f, WIDTH, HWIDTH)
        knob.setStrokeColor(0xff000000.toInt()).drawLine(0f, HWIDTH / 2, WIDTH, HWIDTH / 2)
        knob.setStrokeColor(0xffff0000.toInt()).strokeRect(0f, 0f, WIDTH - 1, HWIDTH - 1)

        val il = ImageLayer(knob.toTexture())
        il.setOrigin(0f, HWIDTH / 2).setDepth(1f).setTranslation(0f, HEIGHT / 2)

        val slider = GroupLayer()
        slider.add(bg)
        slider.add(il)
        game.rootLayer.addAt(slider, 25f, 25f)

        bg.events().connect(object : Mouse.Listener {
            override fun onWheel(event: klay.core.Mouse.WheelEvent, iact: Mouse.Interaction) {
                var y = il.ty() + event.velocity
                y = maxOf(0f, minOf(y, HEIGHT))
                il.setTranslation(0f, y)
            }
        })
    }

    companion object {
        private val HEIGHT = 300f
        private val WIDTH = 30f
        private val HWIDTH = WIDTH / 2
    }
}
