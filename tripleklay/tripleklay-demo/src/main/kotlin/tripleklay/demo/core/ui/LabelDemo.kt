package tripleklay.demo.core.ui

import klay.core.Font
import tripleklay.demo.core.DemoScreen
import tripleklay.ui.*
import tripleklay.ui.layout.AxisLayout
import tripleklay.util.Colors

/**
 * Various label tests.
 */
class LabelDemo : DemoScreen() {
    override fun name(): String {
        return "Labels"
    }

    override fun title(): String {
        return "UI: Labels"
    }

    override fun createIface(root: Root): Group {
        val smiley = Icons.image(assets().getImage("images/smiley.png"))
        val greenBg = Styles.make(Style.BACKGROUND.`is`(Background.solid(0xFF99CC66.toInt()).inset(5f)))
        val smallUnderlined = Styles.make(
                Style.FONT.`is`(Font("Times New Roman", 20f)),
                Style.HALIGN.center, Style.UNDERLINE.`is`(true))
        val bigLabel = Styles.make(
                Style.FONT.`is`(Font("Times New Roman", 32f)),
                Style.HALIGN.center)
        return Group(AxisLayout.vertical()).add(
                Shim(15f, 15f),
                Label("Wrapped text").addStyles(Style.HALIGN.center),
                Group(AxisLayout.horizontal(), greenBg.add(Style.VALIGN.top)).add(
                        AxisLayout.stretch(Label(TEXT1, smiley).addStyles(
                                Style.TEXT_WRAP.`is`(true), Style.HALIGN.left,
                                Style.ICON_GAP.`is`(5))),
                        AxisLayout.stretch(Label(TEXT2).addStyles(
                                Style.TEXT_WRAP.`is`(true), Style.HALIGN.center)),
                        AxisLayout.stretch(Label(TEXT3).addStyles(
                                Style.TEXT_WRAP.`is`(true), Style.HALIGN.right))),
                Shim(15f, 15f),
                Label("Styled text").addStyles(Style.HALIGN.center),
                Group(AxisLayout.horizontal().gap(10)).add(
                        Label("Plain").addStyles(bigLabel),
                        Label("Pixel Outline").addStyles(
                                bigLabel.add(Style.TEXT_EFFECT.pixelOutline).add(Style.COLOR.`is`(Colors.WHITE)).add(Style.HIGHLIGHT.`is`(Colors.GRAY))),
                        Label("Vector Outline").addStyles(
                                bigLabel.add(Style.TEXT_EFFECT.vectorOutline,
                                        Style.OUTLINE_WIDTH.`is`(2f))),
                        Label("Shadow").addStyles(
                                bigLabel.add(Style.TEXT_EFFECT.shadow))),
                Label("Underlining").addStyles(Style.HALIGN.center),
                Group(AxisLayout.horizontal().gap(10)).add(
                        Label("Plain").addStyles(smallUnderlined),
                        Label("gjpqy").addStyles(smallUnderlined),
                        Label("Pixel Outline").addStyles(
                                smallUnderlined.add(Style.TEXT_EFFECT.pixelOutline)),
                        Label("gjpqy").addStyles(
                                smallUnderlined.add(Style.TEXT_EFFECT.pixelOutline))),
                Group(AxisLayout.horizontal().gap(10)).add(
                        Label("Vector Outline").addStyles(
                                smallUnderlined.add(Style.TEXT_EFFECT.vectorOutline,
                                        Style.OUTLINE_WIDTH.`is`(2f))),
                        Label("gjpqy").addStyles(
                                smallUnderlined.add(Style.TEXT_EFFECT.vectorOutline,
                                        Style.OUTLINE_WIDTH.`is`(2f))),
                        Label("Shadow").addStyles(
                                smallUnderlined.add(Style.TEXT_EFFECT.shadow)),
                        Label("gjpqy").addStyles(
                                smallUnderlined.add(Style.TEXT_EFFECT.shadow))))
    }

    companion object {

        protected val TEXT1 = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat."
        protected val TEXT2 = "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo."
        protected val TEXT3 = "But I must explain to you how all this mistaken idea of denouncing pleasure and praising pain was born and I will give you a complete account of the system, and expound the actual teachings of the great explorer of the truth, the master-builder of human happiness."
    }
}
