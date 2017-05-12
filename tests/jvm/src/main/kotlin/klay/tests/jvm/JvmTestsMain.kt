package klay.tests.jvm

import klay.core.Game
import klay.core.Platform
import klay.jvm.JavaPlatform
import klay.jvm.LWJGLPlatform
import klay.tests.core.TestsGame

object JvmTestsMain {

    @JvmStatic
    fun main(args: Array<String>) {
        println("Hello Kotlin on JVM!")
        val config = JavaPlatform.Config()
        config.width = 800
        config.height = 600
        config.appName = "Klay-JvmTests"
        val plat = LWJGLPlatform(config)
        TestsGame(plat, args)
        plat.start()
    }
}
