package tripleklay.demo.ui

import react.RFuture
import react.Slot

import klay.core.Image
import klay.core.Texture

import tripleklay.ui.Background
import tripleklay.ui.Group
import tripleklay.ui.Label
import tripleklay.ui.Root
import tripleklay.ui.Style
import tripleklay.ui.layout.TableLayout
import tripleklay.util.Colors
import tripleklay.demo.DemoScreen

class BackgroundDemo : DemoScreen() {
    protected fun name(): String {
        return "Backgrounds"
    }

    protected fun title(): String {
        return "UI: Backgrounds"
    }

    protected fun createIface(root: Root): Group {
        val s91: Label
        val s92: Label
        val testBg = assets().getImage("images/background.png")
        val group = Group(TableLayout(3).gaps(5, 5)).add(
                label("Beveled", Background.beveled(0xFFCCFF99.toInt(), 0xFFEEFFBB.toInt(), 0xFFAADD77.toInt()).inset(10)),
                label("Beveled (no inset)", Background.beveled(0xFFCCFF99.toInt(), 0xFFEEFFBB.toInt(), 0xFFAADD77.toInt())),
                label("Composite", Background.composite(
                        Background.solid(Colors.BLUE).inset(5),
                        Background.bordered(Colors.WHITE, Colors.BLACK, 2).inset(12),
                        Background.image(testBg).inset(5))),
                label("Solid", Background.solid(0xFFCCFF99.toInt()).inset(10)),
                label("Solid (no inset)", Background.solid(0xFFCCFF99.toInt())),
                Label(),
                label("Null", Background.blank().inset(10)),
                label("Null (no inset)", Background.blank()),
                Label(),
                label("Image", Background.image(testBg).inset(10)),
                label("Image (no inset)", Background.image(testBg)),
                Label(),
                s91 = label("Scale 9"),
                s92 = label("Scale 9\nSomewhat\nTaller\nAnd\nWider"),
                Label(),
                label("Bordered (inset 10)", Background.bordered(0xFFEEEEEE.toInt(), 0xFFFFFF00.toInt(), 2).inset(10)),
                label("Bordered (inset 2)", Background.bordered(0xFFEEEEEE.toInt(), 0xFFFFFF00.toInt(), 2).inset(2)),
                label("Bordered (no inset)", Background.bordered(0xFFEEEEEE.toInt(), 0xFFFFFF00.toInt(), 2)),
                label("Round rect", Background.roundRect(
                        graphics(), 0xFFEEEEEE.toInt(), 10, 0xFFFFFF00.toInt(), 5).inset(10)),
                label("Round rect (no inset)", Background.roundRect(
                        graphics(), 0xFFEEEEEE.toInt(), 10, 0xFFFFFF00.toInt(), 5)),
                label("Round rect (no border)", Background.roundRect(
                        graphics(), 0xFFEEEEEE.toInt(), 10).inset(10)))

        // we have to wait for our scale9 image to load before we can create its bg
        assets().getImage("images/scale9.png").textureAsync().onSuccess(object : Slot<Texture>() {
            fun onEmit(tex: Texture) {
                val bg = Background.scale9(tex).inset(5)
                s91.addStyles(Style.BACKGROUND.`is`(bg))
                s92.addStyles(Style.BACKGROUND.`is`(bg))
            }
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
