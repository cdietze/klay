package tripleklay.demo.ui

import react.UnitSlot

import tripleklay.ui.Background
import tripleklay.ui.Button
import tripleklay.ui.Element
import tripleklay.ui.Group
import tripleklay.ui.Icon
import tripleklay.ui.Icons
import tripleklay.ui.Label
import tripleklay.ui.Root
import tripleklay.ui.Style
import tripleklay.ui.Styles
import tripleklay.ui.layout.AxisLayout
import tripleklay.ui.layout.FlowLayout

import tripleklay.demo.DemoScreen

class FlowLayoutDemo : DemoScreen() {
    protected fun name(): String {
        return "FlowLayout"
    }

    protected fun title(): String {
        return "UI: FlowLayout"
    }

    protected fun createIface(root: Root): Group {
        val main = Group(AxisLayout.vertical().offStretch())

        val panel = Group(FlowLayout(), Styles.make(Style.BACKGROUND.`is`(
                Background.bordered(0xFFFFFFFF.toInt(), 0xff000000.toInt(), 2).inset(4))))

        var buttons = Group(AxisLayout.horizontal())
        for (type in ElemType.values()) {
            buttons.add(Button("Add " + type.toString()).onClick(object : UnitSlot() {
                fun onEmit() {
                    panel.add(create(type))
                }
            }))
        }
        main.add(buttons)

        buttons = Group(AxisLayout.horizontal())
        buttons.add(Label("HAlign:"))
        for (halign in Style.HAlign.values()) {
            buttons.add(Button(halign.toString().substring(0, 1)).onClick(object : UnitSlot() {
                fun onEmit() {
                    panel.addStyles(Style.HALIGN.`is`(halign))
                }
            }))
        }

        buttons.add(Label("VAlign:"))
        for (valign in Style.VAlign.values()) {
            buttons.add(Button(valign.toString().substring(0, 1)).onClick(object : UnitSlot() {
                fun onEmit() {
                    panel.addStyles(Style.VALIGN.`is`(valign))
                }
            }))
        }
        main.add(buttons)

        main.add(panel.setConstraint(AxisLayout.stretched()))
        return main
    }

    fun create(type: ElemType): Element<*> {
        when (type) {
            ElemType.SMILE -> return Label(_smiley)
            ElemType.SMILE_TEXT -> return Label("Some Text", _smiley)
            ElemType.TEXT -> return Label("Blah blah blah")
            ElemType.BUTTON -> return Button("Click to Foo")
            else -> throw AssertionError()
        }
    }

    protected enum class ElemType {
        SMILE, TEXT, SMILE_TEXT, BUTTON
    }

    protected var _smiley = Icons.image(assets().getImage("images/smiley.png"))
}
