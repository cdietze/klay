package tripleklay.demo.ui

import react.Slot
import react.UnitSlot
import tripleklay.demo.DemoScreen
import tripleklay.ui.Background
import tripleklay.ui.Button
import tripleklay.ui.Field
import tripleklay.ui.Group
import tripleklay.ui.HistoryGroup
import tripleklay.ui.Label
import tripleklay.ui.Root
import tripleklay.ui.SizableGroup
import tripleklay.ui.Slider
import tripleklay.ui.Style
import tripleklay.ui.Stylesheet
import tripleklay.ui.layout.AxisLayout
import tripleklay.ui.layout.BorderLayout
import tripleklay.util.Colors

class HistoryGroupDemo : DemoScreen() {
    protected fun name(): String {
        return "History Group"
    }

    protected fun title(): String {
        return "UI: History Group"
    }

    protected fun createIface(root: Root): Group {
        val prefix = Field("Love Potion Number ")
        val add10 = Button("+10")
        val add100 = Button("+100")
        val history = HistoryGroup.Labels()
        val historyBox = SizableGroup(BorderLayout())
        historyBox.add(history.setConstraint(BorderLayout.CENTER))
        val width = Slider(150f, 25f, 1024f)
        val top = Group(AxisLayout.horizontal()).add(
                prefix.setConstraint(AxisLayout.stretched()), add10, add100, width)
        width.value.connectNotify(object : Slot<Float>() {
            fun onEmit(`val`: Float?) {
                historyBox.preferredSize.updateWidth(`val`!!)
            }
        })
        add10.clicked().connect(addSome(history, prefix, 10))
        add100.clicked().connect(addSome(history, prefix, 100))
        history.setStylesheet(Stylesheet.builder().add(Label::class.java,
                Style.BACKGROUND.`is`(Background.composite(
                        Background.blank().inset(0, 2),
                        Background.bordered(Colors.WHITE, Colors.BLACK, 1).inset(10))),
                Style.TEXT_WRAP.on, Style.HALIGN.left).create())
        history.addStyles(Style.BACKGROUND.`is`(Background.beveled(
                Colors.CYAN, Colors.brighter(Colors.CYAN), Colors.darker(Colors.CYAN)).inset(5)))
        _lastNum = 0
        return Group(AxisLayout.vertical()).add(
                top, historyBox.setConstraint(AxisLayout.stretched())).addStyles(
                Style.BACKGROUND.`is`(Background.blank().inset(5)))
    }

    protected fun addSome(group: HistoryGroup.Labels, prefix: Field, num: Int): UnitSlot {
        return object : UnitSlot() {
            fun onEmit() {
                for (ii in 0..num - 1) {
                    group.addItem(prefix.text.get() + (++_lastNum).toString())
                }
            }
        }
    }

    protected var _lastNum: Int = 0
}
