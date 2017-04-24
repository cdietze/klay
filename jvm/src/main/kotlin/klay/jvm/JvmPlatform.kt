package klay.jvm

import klay.core.Game

class JvmPlatform {

    fun run(game: Game) {
        println("Running game on JvmPlatform!")
        game.run()
    }
}