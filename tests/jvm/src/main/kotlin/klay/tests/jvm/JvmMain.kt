package klay.tests.jvm

import klay.core.Game
import klay.jvm.JavaPlatform
import klay.jvm.LWJGLPlatform

object JvmMain {

    @JvmStatic
    fun main(args: Array<String>) {
        println("Hello Kotlin on JVM!")
        val config = JavaPlatform.Config()
        val plat = LWJGLPlatform(config)
        Game(plat)
        plat.start()
    }
}
