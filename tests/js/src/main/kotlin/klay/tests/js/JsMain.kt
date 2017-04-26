package klay.tests.js

import klay.core.Game
import klay.js.JsPlatform

fun main(args: Array<String>) {
    println("Hello Kotlin on JavaScript!")
    val plat = JsPlatform()
    Game(plat)
    plat.run()
}

//fun main(args: Array<String>) = HelloWebglMain.main(args)
