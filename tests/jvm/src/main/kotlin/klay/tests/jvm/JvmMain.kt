package klay.tests.jvm

import klay.core.Game
import klay.jvm.JvmPlatform

object JvmMain {

    @JvmStatic
    fun main(args: Array<String>) {
        println("Hello Kotlin on JVM!")
        val game = Game()
        val plat = JvmPlatform()

        plat.run(game)
    }
}
