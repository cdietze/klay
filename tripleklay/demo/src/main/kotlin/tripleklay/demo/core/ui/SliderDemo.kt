package tripleklay.demo.core.ui

import klay.core.Font
import tripleklay.demo.core.DemoScreen
import tripleklay.ui.*
import tripleklay.ui.layout.AxisLayout

class SliderDemo : DemoScreen() {
    override fun name(): String {
        return "Sliders"
    }

    override fun title(): String {
        return "UI: Sliders"
    }

    override fun createIface(root: Root): Group {
        val iface = Group(AxisLayout.vertical().gap(10)).add(
                Shim(15f, 15f),
                Label("Click and drag the slider to change the value:"),
                sliderAndLabel(Slider(0f, -100f, 100f), "-000"),
                Shim(15f, 15f),
                Label("This one counts by 2s:"),
                sliderAndLabel(Slider(0f, -50f, 50f).setIncrement(2f).addStyles(
                        Behavior.Track.HOVER_LIMIT.`is`(35f)), "-00"),
                Shim(15f, 15f),
                Label("With a background, custom bar and thumb image:"),
                sliderAndLabel(
                        Slider(0f, -50f, 50f).addStyles(
                                Style.BACKGROUND.`is`(Background.roundRect(graphics(), 0xFFFFFFFF.toInt(), 16f).inset(4f)),
                                Slider.THUMB_IMAGE.`is`(Icons.image(assets().getImage("images/smiley.png"))),
                                Slider.BAR_HEIGHT.`is`(18f),
                                Slider.BAR_BACKGROUND.`is`(
                                        Background.roundRect(graphics(), 0xFFFF0000.toInt(), 9f))), "-00"))

        return iface
    }

    protected fun sliderAndLabel(slider: Slider, minText: String): Group {
        val label = Label(slider.value.map({ it.toInt().toString() })).setStyles(Style.HALIGN.right, Style.FONT.`is`(FIXED)).setConstraint(Constraints.minSize(graphics(), minText))
        return Group(AxisLayout.horizontal()).add(slider, label)
    }

    companion object {
        protected var FIXED = Font("Fixed", 16f)
    }
}
