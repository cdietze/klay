package tripleklay.demo.core.game

import klay.core.Game
import tripleklay.demo.core.DemoScreen
import tripleklay.demo.core.TripleDemo
import tripleklay.game.ScreenStack
import tripleklay.ui.*
import tripleklay.ui.layout.AxisLayout

/**
 * Tests/demonstrates screen-related things.
 */
class ScreensDemo(protected val _stack: ScreenStack) : DemoScreen() {

    override fun name(): String {
        return "Screens"
    }

    override fun title(): String {
        return "Screen Stack and Transitions"
    }

    override fun createIface(root: Root): Group {
        val main = Group(AxisLayout.vertical())
        addUI(this, main, 0)
        return main
    }

    protected fun addUI(screen: ScreenStack.Screen, root: Elements<*>, depth: Int) {
        root.add(Label("Screen " + depth))

        root.add(Button("Slide").onClick({
            _stack.push(createScreen(depth + 1), _stack.slide())
        }))
        root.add(Button("Turn").onClick({
            _stack.push(createScreen(depth + 1), _stack.pageTurn())
        }))
        root.add(Button("Flip").onClick({
            _stack.push(createScreen(depth + 1), _stack.flip())
        }))

        if (depth > 0) {
            root.add(Button("Replace").onClick({
                _stack.replace(createScreen(depth + 1), _stack.slide().left())
            }))
            root.add(Button("Back").onClick({
                _stack.remove(screen, _stack.flip().unflip())
            }))
            root.add(Button("Top").onClick({
                _stack.popTo(this@ScreensDemo, _stack.slide().right())
            }))
        }
    }

    protected fun createScreen(depth: Int): ScreenStack.Screen {
        return object : TestScreen(depth) {
            override fun game(): Game {
                return TripleDemo.game!!
            }

            override fun toString(): String {
                return "Screen" + depth
            }

            override fun createIface(root: Root) {
                addUI(this, root, depth)
            }
        }
    }
}
