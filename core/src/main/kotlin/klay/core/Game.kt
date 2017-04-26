package klay.core

class Game(val plat: Platform) {

    init {
        println("Hi from klay.core.Game!")

        plat.frameSignal.connect {
            plat.graphics.gl.glClearColor(1f, 0f, 1f, 1f)
        }
    }
}
