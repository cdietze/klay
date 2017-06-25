package tripleklay.demo.core.ui

import klay.core.Surface
import klay.scene.Layer
import klay.scene.LayerUtil
import pythagoras.f.IDimension
import react.UnitSlot
import tripleklay.demo.core.DemoScreen
import tripleklay.ui.*
import tripleklay.ui.Scroller.Behavior
import tripleklay.ui.layout.AxisLayout
import tripleklay.util.Colors

class ScrollerDemo : DemoScreen() {
    override fun name(): String {
        return "Scroller"
    }

    override fun title(): String {
        return "UI: Scroller"
    }

    override fun createIface(root: Root): Group {
        val width = Slider(100f, 100f, 5000f)
        val height = Slider(100f, 100f, 5000f)
        val xpos = Slider(0f, 0f, 1f)
        val ypos = Slider(0f, 0f, 1f)
        val content = Content()
        val scroll = Scroller(content)
        val click = Label()

        // updates the size of the content
        val updateSize: UnitSlot = {
            (scroll.content as Content).preferredSize.update(
                    width.value.get(), height.value.get())
        }
        width.value.connect(updateSize)
        height.value.connect(updateSize)

        // updates the scroll offset
        val updatePos: UnitSlot = {
            val x = xpos.value.get() * scroll.hrange.max()
            val y = ypos.value.get() * scroll.vrange.max()
            scroll.scroll(x, y)
        }
        xpos.value.connect(updatePos)
        ypos.value.connect(updatePos)

        val beh = Button(Behavior.BOTH.name).onClick({ source: Button ->
            val behs = Behavior.values()
            var beh = Behavior.valueOf(source.text.get()!!)
            beh = behs[(beh.ordinal + 1) % behs.size]
            scroll.setBehavior(beh)
            source.text.update(beh.name)
            xpos.setVisible(beh.hasHorizontal())
            ypos.setVisible(beh.hasVertical())
            updateSize.invoke(Unit)
        })

        scroll.contentClicked().connect({ e: klay.core.Pointer.Event ->
            val pt = LayerUtil.screenToLayer(content.layer, e.x, e.y)
            click.text.update("${pt.x}, ${pt.y}")
        })

        scroll.addListener(object : Scroller.Listener {
            override fun viewChanged(contentSize: IDimension, scrollSize: IDimension) {}
            override fun positionChanged(x: Float, y: Float) {
                update(xpos, x, scroll.hrange)
                update(ypos, y, scroll.vrange)
            }

            internal fun update(pos: Slider, `val`: Float, range: Scroller.Range) {
                if (range.max() > 0) pos.value.update(`val` / range.max())
            }
        })

        // background so we can see when the content is smaller
        scroll.addStyles(Style.BACKGROUND.`is`(Background.solid(Colors.LIGHT_GRAY).inset(10f)))

        updatePos.invoke(Unit)
        updateSize.invoke(Unit)

        return Group(AxisLayout.vertical().offStretch()).add(
                Group(AxisLayout.horizontal()).add(
                        Label("Size:"), Shim(15f, 1f), width, Label("x"), height, beh),
                Group(AxisLayout.horizontal()).add(
                        Label("Pos:"), Shim(15f, 1f), xpos, ypos),
                Group(AxisLayout.horizontal()).add(
                        Label("Click:"), Shim(15f, 1f), click),
                Group(AxisLayout.horizontal().offStretch()).setConstraint(AxisLayout.stretched()).add(scroll.setConstraint(AxisLayout.stretched())))
    }

    protected class Content : SizableWidget<Content>() {
        val tick = 100f

        init {
            layer.add(object : Layer() {
                override fun paintImpl(surf: Surface) {
                    surf.setFillColor(0xFFFFFFFF.toInt())
                    surf.fillRect(0f, 0f, _size.width, _size.height)

                    val left = 1f
                    val top = 1f
                    val right = _size.width
                    val bot = _size.height
                    surf.setFillColor(0xFF7f7F7F.toInt())
                    var x = 0f
                    while (x < _size.width) {
                        surf.drawLine(x, top, x, bot, 1f)
                        x += tick
                    }
                    var y = 0f
                    while (y < _size.height) {
                        surf.drawLine(left, y, right, y, 1f)
                        y += tick
                    }

                    surf.setFillColor(0xFFFF7F7F.toInt())
                    surf.drawLine(left - 1, top, right, top, 2f)
                    surf.drawLine(right - 1, top - 1, right - 1, bot, 2f)
                    surf.drawLine(left, top - 1, left, bot, 2f)
                    surf.drawLine(left - 1, bot - 1, right, bot - 1, 2f)
                }
            })
        }

        override val styleClass: Class<*>
            get() = Content::class.java
    }
}
