package klay.tests.jvm

import klay.core.Game
import klay.jvm.JvmPlatform

object JvmMain {

    @JvmStatic
    fun main(args: Array<String>) {
        println("Hello Kotlin on JVM!")
        val plat = JvmPlatform()
        Game(plat)
        plat.run()
    }
}
