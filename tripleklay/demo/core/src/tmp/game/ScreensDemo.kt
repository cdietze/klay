package tripleklay.demo.game

import react.UnitSlot

import klay.core.Game

import tripleklay.game.ScreenStack
import tripleklay.ui.Button
import tripleklay.ui.Elements
import tripleklay.ui.Group
import tripleklay.ui.Label
import tripleklay.ui.Root
import tripleklay.ui.layout.AxisLayout

import tripleklay.demo.DemoScreen
import tripleklay.demo.TripleDemo

/**
 * Tests/demonstrates screen-related things.
 */
class ScreensDemo(protected val _stack: ScreenStack) : DemoScreen() {

    protected fun name(): String {
        return "Screens"
    }

    protected fun title(): String {
        return "Screen Stack and Transitions"
    }

    protected fun createIface(root: Root): Group {
        val main = Group(AxisLayout.vertical())
        addUI(this, main, 0)
        return main
    }

    protected fun addUI(screen: ScreenStack.Screen, root: Elements<*>, depth: Int) {
        root.add(Label("Screen " + depth))

        root.add(Button("Slide").onClick(object : UnitSlot() {
            fun onEmit() {
                _stack.push(createScreen(depth + 1), _stack.slide())
            }
        }))
        root.add(Button("Turn").onClick(object : UnitSlot() {
            fun onEmit() {
                _stack.push(createScreen(depth + 1), _stack.pageTurn())
            }
        }))
        root.add(Button("Flip").onClick(object : UnitSlot() {
            fun onEmit() {
                _stack.push(createScreen(depth + 1), _stack.flip())
            }
        }))

        if (depth > 0) {
            root.add(Button("Replace").onClick(object : UnitSlot() {
                fun onEmit() {
                    _stack.replace(createScreen(depth + 1), _stack.slide().left())
                }
            }))
            root.add(Button("Back").onClick(object : UnitSlot() {
                fun onEmit() {
                    _stack.remove(screen, _stack.flip().unflip())
                }
            }))
            root.add(Button("Top").onClick(object : UnitSlot() {
                fun onEmit() {
                    _stack.popTo(this@ScreensDemo, _stack.slide().right())
                }
            }))
        }
    }

    protected fun createScreen(depth: Int): ScreenStack.Screen {
        return object : TestScreen(depth) {
            override fun game(): Game {
                return TripleDemo.game
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
