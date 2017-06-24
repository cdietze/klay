package tripleklay.demo.ui

import react.Slot

import klay.core.Font

import tripleklay.demo.DemoScreen
import tripleklay.ui.Background
import tripleklay.ui.Element
import tripleklay.ui.Group
import tripleklay.ui.Label
import tripleklay.ui.Root
import tripleklay.ui.Selector
import tripleklay.ui.Shim
import tripleklay.ui.Style
import tripleklay.ui.Stylesheet
import tripleklay.ui.ToggleButton
import tripleklay.ui.layout.AxisLayout

class SelectorDemo : DemoScreen() {
    protected fun name(): String {
        return "Selector"
    }

    protected fun title(): String {
        return "UI: Selector"
    }

    protected fun createIface(root: Root): Group {
        val main = Group(AxisLayout.vertical())
        val buttons: Group
        var sel: Selector

        val font = Style.FONT.getDefault(main).name
        val hdr = Style.FONT.`is`(Font(font, Font.Style.BOLD, 14f))
        main.setStylesheet(Stylesheet.builder().add(
                Label::class.java, Style.FONT.`is`(Font(font, 12f))).create())

        main.add(Label("Simple").addStyles(hdr))
        main.add(Label("A single parent with buttons - at most one is selected."))
        main.add(buttons = Group(AxisLayout.horizontal()).add(
                mkButt("A"), mkButt("B"), mkButt("C")))
        sel = Selector(buttons, buttons.childAt(0))
        main.add(hookup("Selection:", sel))
        main.add(Shim(10f, 10f))

        main.add(Label("Mixed").addStyles(hdr))
        main.add(Label("A single parent with two groups - one from each may be selected."))
        main.add(buttons = Group(AxisLayout.horizontal()).add(
                mkButt("Alvin"), mkButt("Simon"), mkButt("Theodore"),
                mkButt("Alpha"), mkButt("Sigma"), mkButt("Theta")))
        sel = Selector().add(buttons.childAt(0), buttons.childAt(1), buttons.childAt(2))
        main.add(hookup("Chipmunk:", sel))
        sel = Selector().add(buttons.childAt(3), buttons.childAt(4), buttons.childAt(5))
        main.add(hookup("Greek Letter:", sel))
        main.add(Shim(10f, 10f))

        val box = Style.BACKGROUND.`is`(
                Background.bordered(0xffffffff.toInt(), 0xff000000.toInt(), 1).inset(5))
        main.add(Label("Multiple parents").addStyles(hdr))
        main.add(Label("At most one button may be selected."))
        main.add(buttons = Group(AxisLayout.horizontal()).add(
                Group(AxisLayout.vertical(), box).add(mkButt("R1C1"), mkButt("R2C1")),
                Group(AxisLayout.vertical(), box).add(mkButt("R1C2"), mkButt("R2C2"))))
        sel = Selector().add(buttons.childAt(0) as Group).add(buttons.childAt(1) as Group)
        main.add(hookup("Selection:", sel))

        return main
    }

    protected fun hookup(name: String, sel: Selector): Group {
        val label = Label()
        sel.selected.connect(object : Slot<Element<*>>() {
            fun onEmit(event: Element<*>) {
                update(label, event as ToggleButton)
            }
        })
        update(label, sel.selected.get() as ToggleButton)
        return Group(AxisLayout.horizontal()).add(Label(name), label)
    }

    protected fun update(label: Label, sel: ToggleButton?) {
        label.text.update(if (sel == null) "<None>" else sel.text.get())
    }

    protected fun mkButt(label: String): ToggleButton {
        return ToggleButton(label)
    }
}
