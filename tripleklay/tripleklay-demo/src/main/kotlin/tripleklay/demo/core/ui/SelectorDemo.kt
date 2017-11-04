package tripleklay.demo.core.ui

import klay.core.Font
import tripleklay.demo.core.DemoScreen
import tripleklay.ui.*
import tripleklay.ui.layout.AxisLayout

class SelectorDemo : DemoScreen() {
    override fun name(): String {
        return "Selector"
    }

    override fun title(): String {
        return "UI: Selector"
    }

    override fun createIface(root: Root): Group {
        val main = Group(AxisLayout.vertical())
        val buttons1: Group = Group(AxisLayout.horizontal()).add(
                mkButt("A"), mkButt("B"), mkButt("C"))
        val buttons2: Group = Group(AxisLayout.horizontal()).add(
                mkButt("Alvin"), mkButt("Simon"), mkButt("Theodore"),
                mkButt("Alpha"), mkButt("Sigma"), mkButt("Theta"))
        val box = Style.BACKGROUND.`is`(
                Background.bordered(0xffffffff.toInt(), 0xff000000.toInt(), 1f).inset(5f))
        val buttons3: Group = Group(AxisLayout.horizontal()).add(
                Group(AxisLayout.vertical(), box).add(mkButt("R1C1"), mkButt("R2C1")),
                Group(AxisLayout.vertical(), box).add(mkButt("R1C2"), mkButt("R2C2")))
        var sel: Selector

        val font = Style.FONT.getDefault(main).name
        val hdr = Style.FONT.`is`(Font(font, Font.Style.BOLD, 14f))
        main.setStylesheet(Stylesheet.builder().add(
                Label::class, Style.FONT.`is`(Font(font, 12f))).create())

        main.add(Label("Simple").addStyles(hdr))
        main.add(Label("A single parent with buttons - at most one is selected."))
        main.add(buttons1)
        sel = Selector(buttons1, buttons1.childAt(0))
        main.add(hookup("Selection:", sel))
        main.add(Shim(10f, 10f))

        main.add(Label("Mixed").addStyles(hdr))
        main.add(Label("A single parent with two groups - one from each may be selected."))
        main.add(buttons2)
        sel = Selector().add(buttons2.childAt(0), buttons2.childAt(1), buttons2.childAt(2))
        main.add(hookup("Chipmunk:", sel))
        sel = Selector().add(buttons2.childAt(3), buttons2.childAt(4), buttons2.childAt(5))
        main.add(hookup("Greek Letter:", sel))
        main.add(Shim(10f, 10f))

        main.add(Label("Multiple parents").addStyles(hdr))
        main.add(Label("At most one button may be selected."))
        main.add(buttons3)
        sel = Selector().add(buttons3.childAt(0) as Group).add(buttons3.childAt(1) as Group)
        main.add(hookup("Selection:", sel))

        return main
    }

    protected fun hookup(name: String, sel: Selector): Group {
        val label = Label()
        sel.selected.connect({ event: Element<*>? ->
            update(label, event as ToggleButton)
        })
        update(label, sel.selected.get() as ToggleButton?)
        return Group(AxisLayout.horizontal()).add(Label(name), label)
    }

    protected fun update(label: Label, sel: ToggleButton?) {
        label.text.update(if (sel == null) "<None>" else sel.text.get())
    }

    protected fun mkButt(label: String): ToggleButton {
        return ToggleButton(label)
    }
}
