package tripleklay.demo.core.ui

import tripleklay.demo.core.DemoScreen
import tripleklay.ui.*
import tripleklay.ui.layout.AxisLayout
import tripleklay.ui.layout.FlowLayout

class FlowLayoutDemo : DemoScreen() {
    override fun name(): String {
        return "FlowLayout"
    }

    override fun title(): String {
        return "UI: FlowLayout"
    }

    override fun createIface(root: Root): Group {
        val main = Group(AxisLayout.vertical().offStretch())

        val panel = Group(FlowLayout(), Styles.make(Style.BACKGROUND.`is`(
                Background.bordered(0xFFFFFFFF.toInt(), 0xff000000.toInt(), 2f).inset(4f))))

        var buttons = Group(AxisLayout.horizontal())
        for (type in ElemType.values()) {
            buttons.add(Button("Add " + type.toString()).onClick({
                panel.add(create(type))
            }))
        }
        main.add(buttons)

        buttons = Group(AxisLayout.horizontal())
        buttons.add(Label("HAlign:"))
        for (halign in Style.HAlign.values()) {
            buttons.add(Button(halign.toString().substring(0, 1)).onClick({
                panel.addStyles(Style.HALIGN.`is`(halign))
            }))
        }

        buttons.add(Label("VAlign:"))
        for (valign in Style.VAlign.values()) {
            buttons.add(Button(valign.toString().substring(0, 1)).onClick({
                panel.addStyles(Style.VALIGN.`is`(valign))
            }))
        }
        main.add(buttons)

        main.add(panel.setConstraint(AxisLayout.stretched()))
        return main
    }

    fun create(type: ElemType): Element<*> {
        when (type) {
            FlowLayoutDemo.ElemType.SMILE -> return Label(_smiley)
            FlowLayoutDemo.ElemType.SMILE_TEXT -> return Label("Some Text", _smiley)
            FlowLayoutDemo.ElemType.TEXT -> return Label("Blah blah blah")
            FlowLayoutDemo.ElemType.BUTTON -> return Button("Click to Foo")
            else -> throw AssertionError()
        }
    }

    enum class ElemType {
        SMILE, TEXT, SMILE_TEXT, BUTTON
    }

    protected var _smiley = Icons.image(assets().getImage("images/smiley.png"))
}
