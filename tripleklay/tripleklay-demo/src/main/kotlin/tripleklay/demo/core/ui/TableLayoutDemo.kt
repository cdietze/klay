package tripleklay.demo.core.ui

import tripleklay.demo.core.DemoScreen
import tripleklay.ui.*
import tripleklay.ui.layout.AxisLayout
import tripleklay.ui.layout.FlowLayout
import tripleklay.ui.layout.TableLayout
import tripleklay.util.Colors

class TableLayoutDemo : DemoScreen() {
    internal class ExposedColumn(halign: Style.HAlign, stretch: Boolean, weight: Float, minWidth: Float) : TableLayout.Column(halign, stretch, weight, minWidth) {
        fun halign(): Style.HAlign {
            return _halign
        }

        fun weight(): Float {
            return _weight
        }

        fun minWidth(): Float {
            return _minWidth
        }

        val isStretch: Boolean
            get() = _stretch
    }

    internal class ColumnEditor : Group(FlowLayout()) {
        var col = ExposedColumn(Style.HAlign.CENTER, false, 1f, 0f)
        var weight = Slider(col.weight(), 0f, 50f).setIncrement(1f)
        var minWidth = Slider(col.minWidth(), 0f, 150f).setIncrement(1f)
        var stretch = ToggleButton("Stretch")
        var halign = Button(col.halign().name)

        init {
            add(slider("Weight:", weight), slider("Min Width:", minWidth), stretch, halign)
            stretch.selected().update(col.isStretch)
            weight.value.connect({ event: Float? ->
                col = ExposedColumn(col.halign(), col.isStretch, event!!, col.minWidth())
            })
            minWidth.value.connect({ event: Float? ->
                col = ExposedColumn(col.halign(), col.isStretch, col.weight(), event!!)
            })
            stretch.selected().connect({ event: Boolean? ->
                col = ExposedColumn(col.halign(), event!!, col.weight(), col.minWidth())
            })
            halign.clicked().connect({ event: Button ->
                val values = Style.HAlign.values()
                val next = values[(Style.HAlign.valueOf(halign.text.get()!!).ordinal + 1) % values.size]
                halign.text.update(next.name)
                col = ExposedColumn(next, col.isStretch, col.weight(), col.minWidth())
            })
        }
    }

    internal class DemoCell(text: String) : Label(text)

    internal class TableEditor : Group(AxisLayout.vertical().offStretch(), Style.VALIGN.top) {
        var column = ColumnEditor()
        var tableHolder = Group(AxisLayout.horizontal().stretchByDefault().offStretch(),
                Style.BACKGROUND.`is`(Background.bordered(Colors.WHITE, Colors.BLACK, 1f).inset(5f)),
                Style.VALIGN.top)
        var table: Group? = null
        var columns: MutableList<TableLayout.Column> = ArrayList()
        var tableStyles = Styles.make(Style.BACKGROUND.`is`(Background.solid(Colors.LIGHT_GRAY)),
                Style.VALIGN.top)

        init {
            class CellAdder(val count: Int) : Button("+" + count) {
                init {
                    onClick({
                        addCells(this@CellAdder.count)
                    })
                }
            }

            val add = Button("Add").onClick({
                addColumn()
            })
            val reset = Button("Reset").onClick({
                reset()
            })
            add(column,
                    Group(AxisLayout.horizontal()).add(
                            Label("Columns:"), add, reset, Shim(5f, 1f),
                            Label("Cells:"), CellAdder(1), CellAdder(2),
                            CellAdder(5), CellAdder(10)),
                    tableHolder.setConstraint(AxisLayout.stretched()))

            reset()
        }

        fun reset() {
            columns.clear()
            columns.add(column.col)
            refresh()
        }

        fun refresh() {
            val oldTable = table
            if (table != null) tableHolder.remove(table!!)
            table = Group(
                    TableLayout(*columns.toTypedArray()), tableStyles)
            tableHolder.add(table!!)
            if (oldTable != null) {
                while (oldTable.childCount() > 0) table!!.add(oldTable.childAt(0))
            }
        }

        fun addCells(count: Int) {
            var count = count
            while (count-- > 0) {
                table!!.add(DemoCell("Sample").addStyles(Style.BACKGROUND.`is`(
                        Background.solid(0xFFDDDD70.toInt() + table!!.childCount() % 8 * 0x10))))
            }
        }

        fun addColumn() {
            columns.add(column.col)
            refresh()
        }
    }

    override fun name(): String {
        return "TableLayout"
    }

    override fun title(): String {
        return "UI: TableLayout"
    }

    override fun createIface(root: Root): Group {
        return TableEditor()
    }

    companion object {

        internal fun slider(label: String, slider: Slider): Group {
            return Group(AxisLayout.horizontal()).add(Label(label),
                    SizableGroup(AxisLayout.horizontal(), 30f, 0f).add(Label(slider.value)),
                    slider)
        }
    }
}
