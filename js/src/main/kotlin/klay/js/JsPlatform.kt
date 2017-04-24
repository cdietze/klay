package klay.js

import klay.core.Game

class JsPlatform {
    fun run(game: Game) {
        println("Running game on JsPlatform!")
        game.run()
    }
}