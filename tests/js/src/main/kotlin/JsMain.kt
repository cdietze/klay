import klay.core.Game
import klay.js.JsPlatform

fun main(args: Array<String>) {
    println("Hello Kotlin on JavaScript!")

    val plat = JsPlatform()
    val game = Game(plat)
    plat.run(game)

    game.sayHello()
}