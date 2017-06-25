package tripleklay.demo.core.ui

import react.UnitSlot
import tripleklay.demo.core.DemoScreen
import tripleklay.ui.*
import tripleklay.ui.layout.AxisLayout
import tripleklay.ui.layout.BorderLayout
import tripleklay.util.Colors

class HistoryGroupDemo : DemoScreen() {
    override fun name(): String {
        return "History Group"
    }

    override fun title(): String {
        return "UI: History Group"
    }

    override fun createIface(root: Root): Group {
        val prefix = Field("Love Potion Number ")
        val add10 = Button("+10")
        val add100 = Button("+100")
        val history = HistoryGroup.Labels()
        val historyBox = SizableGroup(BorderLayout())
        historyBox.add(history.setConstraint(BorderLayout.CENTER))
        val width = Slider(150f, 25f, 1024f)
        val top = Group(AxisLayout.horizontal()).add(
                prefix.setConstraint(AxisLayout.stretched()), add10, add100, width)
        width.value.connectNotify({ value: Float? ->
            historyBox.preferredSize.updateWidth(value!!)
        })
        add10.clicked().connect(addSome(history, prefix, 10))
        add100.clicked().connect(addSome(history, prefix, 100))
        history.setStylesheet(Stylesheet.builder().add(Label::class.java,
                Style.BACKGROUND.`is`(Background.composite(
                        Background.blank().inset(0f, 2f),
                        Background.bordered(Colors.WHITE, Colors.BLACK, 1f).inset(10f))),
                Style.TEXT_WRAP.on, Style.HALIGN.left).create())
        history.addStyles(Style.BACKGROUND.`is`(Background.beveled(
                Colors.CYAN, Colors.brighter(Colors.CYAN), Colors.darker(Colors.CYAN)).inset(5f)))
        _lastNum = 0
        return Group(AxisLayout.vertical()).add(
                top, historyBox.setConstraint(AxisLayout.stretched())).addStyles(
                Style.BACKGROUND.`is`(Background.blank().inset(5f)))
    }

    protected fun addSome(group: HistoryGroup.Labels, prefix: Field, num: Int): UnitSlot {
        return {
            for (ii in 0..num - 1) {
                group.addItem(prefix.text.get() + (++_lastNum).toString())
            }
        }
    }

    protected var _lastNum: Int = 0
}
