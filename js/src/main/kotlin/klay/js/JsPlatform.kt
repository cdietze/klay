package klay.js

import klay.core.Game
import klay.core.Graphics
import klay.core.Platform

class JsPlatform : Platform {
    override val graphics: Graphics = object : Graphics {
        override fun sayHello() {
            println("Hi from JS Graphics")
        }
    }

    fun run(game: Game) {
        println("Running game on JsPlatform!")
        println("org.khronos.webgl.Float32Array: ${org.khronos.webgl.Float32Array(19)}")
        println("FloatBuffer: ${java.nio.FloatBuffer.allocate(100)}")

    }
}