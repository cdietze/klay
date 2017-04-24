import klay.core.Game
import klay.js.JsPlatform

fun main(args: Array<String>) {
    println("Hello Kotlin on JavaScript!")

    val game = Game()
    val plat = JsPlatform()
    plat.run(game)

}