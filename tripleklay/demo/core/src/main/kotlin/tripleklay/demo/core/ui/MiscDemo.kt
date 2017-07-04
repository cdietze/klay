package tripleklay.demo.core.ui

import klay.core.Image
import pythagoras.f.MathUtil
import react.IntValue
import tripleklay.demo.core.DemoScreen
import tripleklay.demo.core.TripleDemo
import tripleklay.ui.*
import tripleklay.ui.layout.AxisLayout
import tripleklay.util.Colors

/**
 * Displays various UI stuff.
 */
class MiscDemo : DemoScreen() {
    override fun name(): String {
        return "General"
    }

    override fun title(): String {
        return "UI: General"
    }

    override fun createIface(root: Root): Group {
        // TODO(cdi) Sporadically the Elements are built before the image is ready resulting in:
        // java.lang.IllegalStateException: Cannot create texture from unready image: Image[src=images/squares.png, img=BufferedImage@704d6e83: type = 3 DirectColorModel: rmask=ff0000 gmask=ff00 bmask=ff amask=ff000000 IntegerInterleavedRaster: width = 96 height = 16 #Bands = 4 xOff = 0 yOff = 0 dataOffset[0] 0]
        // at klay.core.Image.createTexture(Image.kt:159)
        val smiley = Icons.image(assets().getImage("images/smiley.png"))
        val squares = assets().getImage("images/squares.png")
        val capRoot = iface.addRoot(CapturedRoot(
                iface, AxisLayout.horizontal(), stylesheet(), TripleDemo.game!!.defaultBatch))

        val toggle = CheckBox()
        val toggle2 = CheckBox()
        val label2 = Label("Label 2")
        val box: Box = Box(Label("I'm in a box"))
        val editable = Field("Editable text").setConstraint(Constraints.fixedWidth(150f))
        val disabled = Field("Disabled text").setEnabled(false)
        val setField = Button("Set -> ")

        val iface = Group(AxisLayout.horizontal().stretchByDefault()).add(
                // left column
                Group(AxisLayout.vertical()).add(
                        // labels, visibility and icon toggling
                        Label("Toggling visibility"),
                        Group(AxisLayout.horizontal().gap(15), GREENBG).add(
                                Group(AxisLayout.vertical()).add(
                                        Group(AxisLayout.horizontal()).add(
                                                toggle,
                                                Label("Toggle Viz")),
                                        Group(AxisLayout.horizontal()).add(
                                                toggle2,
                                                Label("Toggle Icon"))),
                                Group(AxisLayout.vertical()).add(
                                        Label("Label 1").addStyles(REDBG),
                                        label2,
                                        Label("Label 3", smiley))),
                        Shim(5f, 10f),

                        // labels with varying icon alignment
                        Label("Icon positioning"),
                        Group(AxisLayout.horizontal().gap(10), GREENBG).add(
                                Label("Left", tileIcon(squares, 0)).setStyles(Style.ICON_POS.left),
                                Label("Right", tileIcon(squares, 1)).setStyles(Style.ICON_POS.right),
                                Label("Above", tileIcon(squares, 2)).setStyles(Style.ICON_POS.above,
                                        Style.HALIGN.center),
                                Label("Below", tileIcon(squares, 3)).setStyles(Style.ICON_POS.below,
                                        Style.HALIGN.center)),
                        Shim(5f, 10f),

                        // box transitions
                        Label("Box transitions"),
                        box.addStyles(GREENBG).addStyles(REDBG).setConstraint(Constraints.fixedSize(200f, 40f))// we fix the size of the box because it would otherwise be unconstrained in
                        // this layout; if the box is allowed to change size, the UI will be
                        // revalidated at the end of the transition and it will snap to the size of the
                        // new contents, which is jarring
                        ,
                        Group(AxisLayout.horizontal().gap(5)).add(
                                Button("Fade").onClick({
                                    val nlabel = Label("I'm faded!").addStyles(GREENBG)
                                    box.transition(nlabel, Box.Fade(500))
                                }),
                                Button("Flip").onClick({
                                    val nlabel = Label("I'm flipped!").addStyles(GREENBG)
                                    box.transition(nlabel, Box.Flip(500))
                                })),
                        Shim(5f, 10f),

                        // a captured root's widget
                        Label("Root capture"),
                        Group(AxisLayout.vertical()).addStyles(
                                Style.BACKGROUND.`is`(Background.solid(Colors.RED).inset(10f))).add(
                                capRoot.createWidget())),

                // right column
                Group(AxisLayout.vertical()).add(
                        // buttons, toggle buttons, wirey uppey
                        Label("Buttons"),
                        buttonsSection(squares),
                        Shim(5f, 10f),

                        // an editable text field
                        Label("Text editing"),
                        editable,
                        setField,
                        disabled))

        capRoot.add(Group(AxisLayout.vertical()).addStyles(Style.BACKGROUND.`is`(Background.blank().inset(10f))).add(Label("Captured Root!"), Button("Captured Button"))).pack()

        // add a style animation to the captured root (clicking on cap roots NYI)
        this.iface.anim.repeat(root.layer).delay(1000f).then().action(object : () -> Unit {
            internal var cycle: Int = 0
            override fun invoke() {
                capRoot.addStyles(Style.BACKGROUND.`is`(if (cycle++ % 2 == 0)
                    Background.solid(Colors.WHITE).alpha(.5f)
                else
                    Background.blank()))
            }
        })

        toggle.selected().update(true)
        toggle.selected().connect(label2.visibleSlot())
        toggle2.selected().map({ checked: Boolean ->
            if (checked) tileIcon(squares, 0) else null
        }).connect(label2.icon.slot())

        val source = editable
        val target = disabled
        setField.clicked().connect({
            log().info("Setting text to " + source.text.get())
            target.text.update(source.text.get())
        })
        return iface
    }

    protected fun buttonsSection(squares: Image): Group {
        val toggle3 = ToggleButton("Toggle Enabled")
        val disabled = Button("Disabled")
        toggle3.selected().connectNotify(disabled.enabledSlot())
        toggle3.selected().map({ selected: Boolean ->
            if (selected) "Enabled" else "Disabled"
        }).connectNotify(disabled.text.slot())

        class ThrobButton(title: String) : Button(title) {
            fun throb() {
                root()!!.iface.anim.tweenScale(layer).to(1.2f).`in`(300.0f).easeIn().then().tweenScale(layer).to(1.0f).`in`(300.0f).easeOut()
            }

            override fun layout() {
                super.layout()
                val ox = MathUtil.ifloor(_size.width / 2).toFloat()
                val oy = MathUtil.ifloor(_size.height / 2).toFloat()
                layer.setOrigin(ox, oy)
                layer.transform().translate(ox, oy)
            }
        }

        val throbber = ThrobButton("Throbber")

        val pressResult = Label()
        val clickCount = IntValue(0)
        val box = Box()
        return Group(AxisLayout.vertical().offEqualize()).add(
                Group(AxisLayout.horizontal().gap(15), GREENBG).add(
                        toggle3, AxisLayout.stretch(disabled)),
                Group(AxisLayout.horizontal().gap(15), GREENBG).add(
                        LongPressButton("Long Pressable").onLongPress({
                            pressResult.text.update("Long pressed")
                        }).onClick({
                            pressResult.text.update("Clicked")
                        }), AxisLayout.stretch(pressResult)),
                Group(AxisLayout.horizontal().gap(15), GREENBG).add(
                        Label("Image button"),
                        ImageButton(tile(squares, 0), tile(squares, 1)).onClick({
                            clickCount.increment(1)
                        }),
                        Label(clickCount)),
                Group(AxisLayout.horizontal().gap(15), GREENBG).add(
                        Button("Fill Box").onClick({
                            box.set(Label(if (box.contents() == null) "Filled" else "Refilled"))
                        }),
                        box),
                Group(AxisLayout.horizontal().gap(15), GREENBG).add(
                        throbber.onClick({
                            throbber.throb()
                        }))
        )
    }

    protected fun tile(image: Image, index: Int): Image.Region {
        val iwidth = 16f
        val iheight = 16f
        return image.region(index * iwidth, 0f, iwidth, iheight)
    }

    protected fun tileIcon(image: Image, index: Int): Icon {
        return Icons.image(tile(image, index))
    }

    companion object {

        protected val GREENBG = Styles.make(
                Style.BACKGROUND.`is`(Background.solid(0xFF99CC66.toInt()).inset(5f)))
        protected val REDBG = Styles.make(
                Style.BACKGROUND.`is`(Background.solid(0xFFCC6666.toInt()).inset(5f)))
    }
}
