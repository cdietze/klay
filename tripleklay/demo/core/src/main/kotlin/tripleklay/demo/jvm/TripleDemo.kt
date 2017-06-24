package tripleklay.demo.jvm

import klay.core.Platform
import klay.scene.Pointer
import klay.scene.SceneGame

import tripleklay.game.ScreenStack

public class TripleDemo(plat: Platform) : SceneGame(plat, 25) {

    val screens: ScreenStack = object : ScreenStack(this@TripleDemo, rootLayer) {
        override fun defaultPushTransition(): ScreenStack.Transition {
            return slide()
        }

        override fun defaultPopTransition(): ScreenStack.Transition {
            return slide().right()
        }
    }

    init {
        game = this     // jam ourselves into a global variable, woo!
        Pointer(plat, rootLayer, true)        // wire up event dispatch
        screens.push(DemoMenuScreen(screens)) // start off with our menu screen
    }// update our "simulation" 40 times per second

    companion object {

        /** Args from the Java bootstrap class.  */
        public var mainArgs = arrayOf<String>()

        /** A static reference to our game. If we were starting from scratch, I'd pass the game
         * instance to the screen constructor of all of our demo screens, but doing that now is too
         * much pain in my rear.  */
        var game: TripleDemo? = null
    }
}
