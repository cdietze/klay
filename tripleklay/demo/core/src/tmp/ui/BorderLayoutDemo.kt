package tripleklay.demo.ui

import com.google.common.collect.Maps

import react.UnitSlot

import tripleklay.ui.Background
import tripleklay.ui.Button
import tripleklay.ui.Element
import tripleklay.ui.Group
import tripleklay.ui.Label
import tripleklay.ui.Layout
import tripleklay.ui.Root
import tripleklay.ui.Shim
import tripleklay.ui.SizableGroup
import tripleklay.ui.Style
import tripleklay.ui.Styles
import tripleklay.ui.layout.AxisLayout
import tripleklay.ui.layout.BorderLayout
import tripleklay.ui.layout.FlowLayout
import tripleklay.util.DimensionValue

import tripleklay.demo.DemoScreen

/**
 * Displays BorderLayout stuff.
 */
class BorderLayoutDemo : DemoScreen() {
    fun name(): String {
        return "BorderLayout"
    }

    fun title(): String {
        return "UI: BorderLayout"
    }

    protected fun createIface(root: Root): Group {
        val buttons = Group(
                AxisLayout.horizontal(),
                Styles.make(Style.BACKGROUND.`is`(Background.solid(0xFFFFFFFF.toInt()).inset(5))))

        for (edge in Panel.EDGES) {
            buttons.add(Button(edge).onClick(object : UnitSlot() {
                fun onEmit() {
                    _panel!!.toggleEdge(edge)
                }
            }))
        }

        buttons.add(Shim(10f, 1f)).add(Button("Toggle Gaps").onClick(object : UnitSlot() {
            fun onEmit() {
                setPanel(_panel!!.useGroups, (if (_panel!!.gaps == 0f) 5 else 0).toFloat())
            }
        }))

        buttons.add(Shim(10f, 1f)).add(Button("Toggle Sizing").onClick(object : UnitSlot() {
            fun onEmit() {
                setPanel(!_panel!!.useGroups, _panel!!.gaps)
            }
        }))

        _root = Group(AxisLayout.vertical().offStretch())
        _root.add(buttons)
        setPanel(false, 0f)
        return _root
    }

    protected fun setPanel(useGroups: Boolean, gaps: Float) {
        if (_panel != null) _root.remove(_panel!!)
        _panel = Panel(useGroups, gaps)
        _panel!!.setConstraint(AxisLayout.stretched())
        _root.add(0, _panel!!)
    }

    class Panel(val useGroups: Boolean, val gaps: Float) : Group(BorderLayout(gaps)) {
        val edges = Maps.newHashMap()

        init {

            add(newSection(NORTH, BorderLayout.NORTH, 0xFFFFFF00.toInt(), 2).addStyles(Style.VALIGN.top))
            add(newSection(SOUTH, BorderLayout.SOUTH, 0xFFFFCC33.toInt(), 2).addStyles(Style.VALIGN.bottom))
            add(newSection(WEST, BorderLayout.WEST, 0xFF666666.toInt(), 1).addStyles(Style.HALIGN.left))
            add(newSection(EAST, BorderLayout.EAST, 0xFF6699CC.toInt(), 1).addStyles(Style.HALIGN.right))
            add(newSection(CENTER, BorderLayout.CENTER, 0xFFFFCCCC.toInt(), 0))
        }

        fun toggleEdge(name: String) {
            edges.get(name).setVisible(!edges.get(name).isVisible)
        }

        protected fun newSection(text: String, constraint: Layout.Constraint, bgColor: Int,
                                 flags: Int): Element<*> {
            val e: Element<*>
            if (useGroups) {
                val colorBg = Background.solid(bgColor)
                val g = SizableGroup(FlowLayout())
                g.addStyles(Style.BACKGROUND.`is`(colorBg))

                if (flags and 1 != 0) g.add(getSizer(g, "W+", 10f, 0f), getSizer(g, "W-", -10f, 0f))
                if (flags and 2 != 0) g.add(getSizer(g, "H+", 0f, 10f), getSizer(g, "H-", 0f, -10f))
                e = g.setConstraint(constraint)

            } else {
                val colorBg = Background.solid(bgColor).inset(5)
                e = Label(text).addStyles(Style.BACKGROUND.`is`(colorBg)).setConstraint(constraint)
            }
            edges.put(text, e)
            return e
        }

        companion object {
            val NORTH = "North"
            val SOUTH = "South"
            val WEST = "West"
            val EAST = "East"
            val CENTER = "Center"
            val EDGES = arrayOf(NORTH, SOUTH, WEST, EAST, CENTER)
        }
    }

    protected var _root: Group
    protected var _panel: Panel? = null

    companion object {

        protected fun getSizer(g: SizableGroup, text: String, dw: Float, dh: Float): Button {
            return Button(text).onClick(getSizer(g.preferredSize, dw, dh))
        }

        protected fun getSizer(base: DimensionValue, dw: Float, dh: Float): UnitSlot {
            return object : UnitSlot() {
                fun onEmit() {
                    base.update(Math.max(0, base.get().width() + dw),
                            Math.max(0, base.get().height() + dh))
                }
            }
        }
    }
}
