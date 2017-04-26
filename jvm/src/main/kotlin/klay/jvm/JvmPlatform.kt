package klay.jvm

import klay.core.Platform

class JvmPlatform : Platform() {
    override val graphics = JvmGraphics()

    fun start() {
        println("Starting game on JvmPlatform!")
    }
}