package tripleklay.demo.core.ui

import klay.core.Color
import klay.core.Keyboard
import react.UnitSlot
import tripleklay.demo.core.DemoScreen
import tripleklay.ui.*
import tripleklay.ui.layout.AxisLayout
import tripleklay.util.Colors
import java.util.*

class TabsDemo : DemoScreen() {
    override fun name(): String {
        return "Tabs"
    }

    override fun title(): String {
        return "UI: Tabs"
    }

    override fun createIface(root: Root): Group {
        val lastTab = intArrayOf(0)
        val tabs = Tabs().addStyles(Style.BACKGROUND.`is`(
                Background.bordered(Colors.WHITE, Colors.BLACK, 1f).inset(1f)))
        val moveRight = Button("Move Right").onClick({
            val tab = tabs.selected.get()
            if (movable(tab)) {
                tabs.repositionTab(tab!!, tab.index() + 1)
            }
        }).setEnabled(false)
        val hide = Button("Hide").onClick({
            val tab = tabs.selected.get()
            if (tab != null) {
                tab.isVisible = false
            }
        }).setEnabled(false)
        tabs.selected.connect({ tab: Tabs.Tab? ->
            moveRight.setEnabled(movable(tab))
            hide.setEnabled(tab != null)
        })
        return Group(AxisLayout.vertical().offStretch()).add(
                Group(AxisLayout.horizontal()).add(
                        Button("Add").onClick({
                            val label = _prefix + ++lastTab[0]
                            tabs.add(label, tabContent(label))
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
                        }), moveRight, hide, Button("Show All").onClick({
                    for (ii in 0..tabs.tabCount() - 1) {
                        tabs.tabAt(ii)!!.isVisible = true
                    }
                })),
                tabs.setConstraint(AxisLayout.stretched()))
    }

    protected fun number(tab: Tabs.Tab): Int {
        return Integer.parseInt(tab.button.text.get()!!.substring(_prefix.length))
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

    protected abstract inner class TabSelector(var tabs: Tabs) : UnitSlot {

        override fun invoke(unit: Any?) {
            var init = ""
            if (tabs.tabCount() > 0) {
                val tab = tabs.tabAt(_rnd.nextInt(tabs.tabCount()))
                init = "" + number(tab!!)
            }
            input().getText(Keyboard.TextType.NUMBER, "Enter tab number", init).onSuccess({ result: String ->
                for (ii in 0..tabs.tabCount() - 1) {
                    if (result == "" + number(tabs.tabAt(ii)!!)) {
                        handle(tabs.tabAt(ii)!!)
                        break
                    }
                }
            })
        }

        abstract fun handle(tab: Tabs.Tab)
    }

    protected var _prefix = "Tab "
    protected var _rnd = Random()
}
