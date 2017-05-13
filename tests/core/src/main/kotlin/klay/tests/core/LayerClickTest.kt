package klay.tests.core

import klay.core.Input
import klay.scene.ImageLayer
import klay.scene.Layer
import klay.scene.Pointer
import klay.scene.Touch
import pythagoras.f.MathUtil
import pythagoras.f.Vector
import react.Slot

internal class LayerClickTest(game: TestsGame) : Test(game, "LayerClick", "Tests the hit testing and click/touch processing provided for layers.") {

    override fun init() {
        val orange = game.assets.getImage("images/orange.png")

        val l1 = ImageLayer(orange)
        game.rootLayer.addAt(l1.setScale(2f).setRotation(MathUtil.PI / 8), 50f, 50f)
        l1.events().connect(Mover(l1).listener(game.input))

        val l2 = ImageLayer(orange)
        game.rootLayer.addAt(l2.setScale(1.5f).setRotation(MathUtil.PI / 4), 150f, 50f)
        l2.events().connect(Mover(l2).listener(game.input))

        val mdb = game.assets.getRemoteImage("https://graph.facebook.com/10153516625660820/picture")
        val l3 = ImageLayer(mdb)
        game.rootLayer.addAt(l3.setRotation(-MathUtil.PI / 4), 50f, 150f)
        l3.events().connect(Mover(l3).listener(game.input))
    }

    protected class Mover(private val layer: Layer) {

        fun listener(input: Input): Slot<Any> {
            return if (input.hasTouch) touch() else pointer()
        }

        fun pointer(): Pointer.Listener {
            return object : Pointer.Listener() {
                override fun onStart(event: Pointer.Interaction) {
                    doStart(event.x, event.y)
                }

                override fun onDrag(event: Pointer.Interaction) {
                    doMove(event.x, event.y)
                }
            }
        }

        fun touch(): Touch.Listener {
            return object : Touch.Listener() {
                override fun onStart(event: Touch.Interaction) {
                    doStart(event.x, event.y)
                }

                override fun onMove(event: Touch.Interaction) {
                    doMove(event.x, event.y)
                }
            }
        }

        protected fun doStart(x: Float, y: Float) {
            _lstart = layer.transform().translation
            _pstart = Vector(x, y)
        }

        protected fun doMove(x: Float, y: Float) {
            val delta = Vector(x, y).subtractLocal(_pstart!!)
            layer.setTranslation(_lstart!!.x + delta.x, _lstart!!.y + delta.y)
        }

        protected var _lstart: Vector? = null
        protected var _pstart: Vector? = null
    }
}
