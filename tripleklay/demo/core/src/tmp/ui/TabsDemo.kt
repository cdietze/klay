package tripleklay.demo.ui

import java.util.Random

import react.Slot
import react.UnitSlot

import klay.core.Color
import klay.core.Keyboard

import tripleklay.demo.DemoScreen
import tripleklay.ui.Background
import tripleklay.ui.Button
import tripleklay.ui.Group
import tripleklay.ui.Label
import tripleklay.ui.Root
import tripleklay.ui.Style
import tripleklay.ui.Tabs
import tripleklay.ui.layout.AxisLayout
import tripleklay.util.Colors

class TabsDemo : DemoScreen() {
    protected fun name(): String {
        return "Tabs"
    }

    protected fun title(): String {
        return "UI: Tabs"
    }

    protected fun createIface(root: Root): Group {
        val lastTab = intArrayOf(0)
        val tabs = Tabs().addStyles(Style.BACKGROUND.`is`(
                Background.bordered(Colors.WHITE, Colors.BLACK, 1).inset(1)))
        val moveRight = Button("Move Right").onClick(object : UnitSlot() {
            fun onEmit() {
                val tab = tabs.selected.get()
                if (movable(tab)) {
                    tabs.repositionTab(tab, tab.index() + 1)
                }
            }
        }).setEnabled(false)
        val hide = Button("Hide").onClick(object : UnitSlot() {
            fun onEmit() {
                val tab = tabs.selected.get()
                if (tab != null) {
                    tab!!.isVisible = false
                }
            }
        }).setEnabled(false)
        tabs.selected.connect(object : Slot<Tabs.Tab>() {
            fun onEmit(tab: Tabs.Tab?) {
                moveRight.setEnabled(movable(tab))
                hide.setEnabled(tab != null)
            }
        })
        return Group(AxisLayout.vertical().offStretch()).add(
                Group(AxisLayout.horizontal()).add(
                        Button("Add").onClick(object : UnitSlot() {
                            fun onEmit() {
                                val label = _prefix + ++lastTab[0]
                                tabs.add(label, tabContent(label))
                            }
                        }),
                        Button("Remove...").onClick(object : TabSelector(tabs) {
                            override fun handle(tab: Tabs.Tab) {
                                tabs.destroyTab(tab)
                            }
                        }),
                        Button("Highlight...").onClick(object : TabSelector(tabs) {
                            override fun handle(tab: Tabs.Tab) {
                                tabs.highlighter().highlight(tab, true)
                            }
                        }), moveRight, hide, Button("Show All").onClick(object : UnitSlot() {
                    fun onEmit() {
                        for (ii in 0..tabs.tabCount() - 1) {
                            tabs.tabAt(ii)!!.isVisible = true
                        }
                    }
                })),
                tabs.setConstraint(AxisLayout.stretched()))
    }

    protected fun number(tab: Tabs.Tab): Int {
        return Integer.parseInt(tab.button.text.get().substring(_prefix.length))
    }

    protected fun movable(tab: Tabs.Tab?): Boolean {
        val index = tab?.index() ?: -1
        return index >= 0 && index + 1 < tab!!.parent().tabCount()
    }

    protected fun randColor(): Int {
        return Color.rgb(128 + _rnd.nextInt(127), 128 + _rnd.nextInt(127), 128 + _rnd.nextInt(127))
    }

    protected fun tabContent(label: String): Group {
        return Group(AxisLayout.vertical().offStretch().stretchByDefault()).add(
                Label(label).addStyles(Style.BACKGROUND.`is`(Background.solid(randColor()))))
    }

    protected abstract inner class TabSelector(var tabs: Tabs) : UnitSlot() {

        fun onEmit() {
            var init = ""
            if (tabs.tabCount() > 0) {
                val tab = tabs.tabAt(_rnd.nextInt(tabs.tabCount()))
                init = "" + number(tab)
            }
            input().getText(Keyboard.TextType.NUMBER, "Enter tab number", init).onSuccess(object : Slot<String>() {
                fun onEmit(result: String) {
                    for (ii in 0..tabs.tabCount() - 1) {
                        if (result == "" + number(tabs.tabAt(ii))) {
                            handle(tabs.tabAt(ii))
                            break
                        }
                    }
                }
            })
        }

        abstract fun handle(tab: Tabs.Tab)
    }

    protected var _prefix = "Tab "
    protected var _rnd = Random()
}
