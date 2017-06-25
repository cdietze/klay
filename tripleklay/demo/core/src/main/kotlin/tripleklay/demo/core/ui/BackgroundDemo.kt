package tripleklay.demo.core.ui

import klay.core.Texture
import tripleklay.demo.core.DemoScreen
import tripleklay.ui.*
import tripleklay.ui.layout.TableLayout
import tripleklay.util.Colors

class BackgroundDemo : DemoScreen() {
    override fun name(): String {
        return "Backgrounds"
    }

    override fun title(): String {
        return "UI: Backgrounds"
    }

    override fun createIface(root: Root): Group {
        val s91: Label = label("Scale 9")
        val s92: Label = label("Scale 9\nSomewhat\nTaller\nAnd\nWider")
        val testBg = assets().getImage("images/background.png")
        val group = Group(TableLayout(3).gaps(5, 5)).add(
                label("Beveled", Background.beveled(0xFFCCFF99.toInt(), 0xFFEEFFBB.toInt(), 0xFFAADD77.toInt()).inset(10f)),
                label("Beveled (no inset)", Background.beveled(0xFFCCFF99.toInt(), 0xFFEEFFBB.toInt(), 0xFFAADD77.toInt())),
                label("Composite", Background.composite(
                        Background.solid(Colors.BLUE).inset(5f),
                        Background.bordered(Colors.WHITE, Colors.BLACK, 2f).inset(12f),
                        Background.image(testBg).inset(5f))),
                label("Solid", Background.solid(0xFFCCFF99.toInt()).inset(10f)),
                label("Solid (no inset)", Background.solid(0xFFCCFF99.toInt())),
                Label(),
                label("Null", Background.blank().inset(10f)),
                label("Null (no inset)", Background.blank()),
                Label(),
                label("Image", Background.image(testBg).inset(10f)),
                label("Image (no inset)", Background.image(testBg)),
                Label(),
                s91,
                s92,
                Label(),
                label("Bordered (inset 10)", Background.bordered(0xFFEEEEEE.toInt(), 0xFFFFFF00.toInt(), 2f).inset(10f)),
                label("Bordered (inset 2)", Background.bordered(0xFFEEEEEE.toInt(), 0xFFFFFF00.toInt(), 2f).inset(2f)),
                label("Bordered (no inset)", Background.bordered(0xFFEEEEEE.toInt(), 0xFFFFFF00.toInt(), 2f)),
                label("Round rect", Background.roundRect(
                        graphics(), 0xFFEEEEEE.toInt(), 10f, 0xFFFFFF00.toInt(), 5f).inset(10f)),
                label("Round rect (no inset)", Background.roundRect(
                        graphics(), 0xFFEEEEEE.toInt(), 10f, 0xFFFFFF00.toInt(), 5f)),
                label("Round rect (no border)", Background.roundRect(
                        graphics(), 0xFFEEEEEE.toInt(), 10f).inset(10f)))

        // we have to wait for our scale9 image to load before we can create its bg
        assets().getImage("images/scale9.png").textureAsync().onSuccess({ tex: Texture ->
            val bg = Background.scale9(tex).inset(5f)
            s91.addStyles(Style.BACKGROUND.`is`(bg))
            s92.addStyles(Style.BACKGROUND.`is`(bg))
        })

        return group
    }

    protected fun label(text: String): Label {
        return Label(text).addStyles(Style.HALIGN.center, Style.TEXT_WRAP.on)
    }

    protected fun label(text: String, bg: Background): Label {
        return label(text).addStyles(Style.BACKGROUND.`is`(bg))
    }
}
