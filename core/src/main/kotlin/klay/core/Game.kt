package klay.core

class Game(val plat: Platform) {

    fun sayHello() {
        println("Hi from klay.core.Game!")
        plat.graphics.sayHello()
        println("Game says: FloatBuffer: ${java.nio.FloatBuffer.allocate(100)}")

    }
}
