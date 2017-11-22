package tripleklay.demo.core.ui

import klay.core.Surface
import klay.scene.Layer
import euklid.f.Dimension
import euklid.f.Point
import react.UnitSlot
import react.Value
import tripleklay.demo.core.DemoScreen
import tripleklay.ui.*
import tripleklay.ui.layout.AbsoluteLayout
import tripleklay.ui.layout.AxisLayout
import tripleklay.ui.util.BoxPoint
import tripleklay.util.Colors
import kotlin.reflect.KClass

class AbsoluteLayoutDemo : DemoScreen() {
    override fun name(): String {
        return "AbsoluteLayout"
    }

    override fun title(): String {
        return "UI: Absolute Layout"
    }

    override fun createIface(root: Root): Group? {
        val position = BoxPointWidget("Position")
        val origin = BoxPointWidget("Origin")
        val width = Slider(50f, 10f, 150f)
        val height = Slider(50f, 10f, 150f)
        val sizeCtrl = Group(AxisLayout.horizontal()).add(Label("Size:"), width, height)
        val group = Group(AbsoluteLayout())
        group.layer.add(object : Layer() {
            private val pt = Point()
            override fun paintImpl(surface: Surface) {
                val size = group.size()
                position.point.get().resolve(size, pt)
                surface.saveTx()
                surface.setFillColor(Colors.BLACK)
                surface.fillRect(pt.x - 2, pt.y - 2, 5f, 5f)
                surface.restoreTx()
            }
        }.setDepth(1f))
        group.addStyles(Style.BACKGROUND.`is`(Background.solid(Colors.WHITE)))
        val widget = Group(AxisLayout.horizontal()).addStyles(
                Style.BACKGROUND.`is`(Background.solid(Colors.CYAN)))
        group.add(widget)
        val updateConstraint: UnitSlot = {
            widget.setConstraint(AbsoluteLayout.Constraint(
                    position.point.get(), origin.point.get(),
                    Dimension(width.value.get(), height.value.get())))
        }
        width.value.connect(updateConstraint)
        height.value.connect(updateConstraint)
        position.point.connect(updateConstraint)
        origin.point.connect(updateConstraint)
        updateConstraint.invoke(Unit)
        return Group(AxisLayout.vertical().offStretch()).add(
                Label("Move the sliders to play with the constraint"),
                Group(AxisLayout.horizontal()).add(position, origin),
                sizeCtrl, group.setConstraint(AxisLayout.stretched()))
    }

    protected class BoxPointWidget(label: String) : Composite<BoxPointWidget>() {
        val point = Value(BoxPoint.TL)
        val nx = Slider(0f, 0f, 1f)
        val ny = Slider(0f, 0f, 1f)
        val ox = Slider(0f, 0f, 100f)
        val oy = Slider(0f, 0f, 100f)

        init {
            layout = AxisLayout.vertical()
            initChildren(Label(label),
                    Group(AxisLayout.horizontal()).add(Label("N:"), nx, ny),
                    Group(AxisLayout.horizontal()).add(Label("O:"), ox, oy))
            val update: UnitSlot = {
                point.update(BoxPoint(
                        nx.value.get(), ny.value.get(), ox.value.get(), oy.value.get()))
            }
            nx.value.connect(update)
            ny.value.connect(update)
            ox.value.connect(update)
            oy.value.connect(update)
            addStyles(Style.BACKGROUND.`is`(Background.solid(Colors.LIGHT_GRAY)))
        }

        override val styleClass: KClass<*>
            get() = BoxPointWidget::class
    }
}
