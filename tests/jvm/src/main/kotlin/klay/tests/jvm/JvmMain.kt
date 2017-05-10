package klay.tests.jvm

import klay.core.Game
import klay.core.Platform
import klay.jvm.JavaPlatform
import klay.jvm.LWJGLPlatform
import klay.tests.core.TestsGame

object JvmMain {

    @JvmStatic
    fun main(args: Array<String>) {
        println("Hello Kotlin on JVM!")
        val config = JavaPlatform.Config()
        val plat = LWJGLPlatform(config)
//        MyGame(plat)
        TestsGame(plat, args)
        plat.start()
    }
}

class MyGame(plat: Platform) : Game(plat, 25) {
}