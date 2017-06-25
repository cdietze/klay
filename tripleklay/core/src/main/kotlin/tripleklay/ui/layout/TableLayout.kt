package tripleklay.ui.layout

import pythagoras.f.Dimension
import tripleklay.ui.Container
import tripleklay.ui.Element
import tripleklay.ui.Layout
import tripleklay.ui.Style

/**
 * Lays out elements in a simple tabular form, where each row has uniform height.
 * Frills are kept to a minimum.
 */
class TableLayout
/**
 * Creates a table layout with the specified columns.
 */
(vararg columns: Column) : Layout() {

    /** A configurator for a table column. Instances are immutable; all methods return a copy. */
    open class Column(val _halign: Style.HAlign, val _stretch: Boolean, val _weight: Float, val _minWidth: Float) {

        /** Left aligns cells.  */
        fun alignLeft(): Column {
            return Column(Style.HAlign.LEFT, _stretch, _weight, _minWidth)
        }

        /** Right aligns cells.  */
        fun alignRight(): Column {
            return Column(Style.HAlign.RIGHT, _stretch, _weight, _minWidth)
        }

        /** Sets column to always use the width of its widest element. By default, columns are
         * 'free' and may be configured as wider than their default to accommodate excess
         * width available to the table.  */
        fun fixed(): Column {
            return Column(_halign, _stretch, 0f, _minWidth)
        }

        /** Sets column to grow freely when excess width is available. The excess will be divided
         * proportionally amongst all non-fixed colulmns in the table, according to weight. By
         * default, columns are free with weight set to 1.  */
        fun free(weight: Float): Column {
            return Column(_halign, _stretch, weight, _minWidth)
        }

        /** Sets column to stretch the width of its elements to the column width. By default,
         * elements are configured to their preferred width.  */
        fun stretch(): Column {
            return Column(_halign, true, _weight, _minWidth)
        }

        /** Configures the minimum width. The column will not be allowed to shrink below its
         * minimum width unless the total table width is insufficient to satisfy the minimum
         * width requirements of all of its columns.  */
        fun minWidth(minWidth: Float): Column {
            return Column(_halign, _stretch, _weight, minWidth)
        }

        /** Returns `count` copies of this column.  */
        fun copy(count: Int): Array<Column> {
            return Array(count, { this })
        }
    }

    /** Defines a colspan constraint.  */
    class Colspan(
            /** The number of columns spanned by this element.  */
            val colspan: Int) : Layout.Constraint() {

        init {
            assert(colspan >= 1) { "Colspan must be >= 1" }
        }
    }

    private val _columns: Array<out Column> = columns
    private var _rowgap: Int = 0
    private var _colgap: Int = 0
    private var _vstretch: Boolean = false
    private var _rowVAlign: Style.VAlign = Style.VAlign.CENTER

    /**
     * Creates a table layout with the specified number of columns, each with the default
     * configuration.
     */
    constructor(columns: Int) : this(*columns(columns))

    /**
     * Configures the gap between successive rows and successive columns. The default gap is zero.
     */
    fun gaps(rowgap: Int, colgap: Int): TableLayout {
        _rowgap = rowgap
        _colgap = colgap
        return this
    }

    /**
     * Configures the vertical alignment of cells to the top of their row.
     */
    fun alignTop(): TableLayout {
        _rowVAlign = Style.VAlign.TOP
        return this
    }

    /**
     * Configures the vertical alignment of cells to the bottom of their row.
     */
    fun alignBottom(): TableLayout {
        _rowVAlign = Style.VAlign.BOTTOM
        return this
    }

    /**
     * Configures cells to be stretched vertically to take up the entire height of their row.
     */
    fun fillHeight(): TableLayout {
        _vstretch = true
        return this
    }

    /**
     * Returns the number of columns configured for this table.
     */
    fun columns(): Int {
        return _columns.size
    }

    override fun computeSize(elems: Container<*>, hintX: Float, hintY: Float): Dimension {
        val m = computeMetrics(elems, hintX, hintY)
        return Dimension(m.totalWidth(_colgap.toFloat()), m.totalHeight(_rowgap.toFloat()))
    }

    override fun layout(elems: Container<*>,
                        left: Float, top: Float, width: Float, height: Float) {
        val m = computeMetrics(elems, width, height)
        val columns = m.columns()
        var row = 0
        var col = 0

        val naturalWidth = m.totalWidth(_colgap.toFloat())
        val freeWeight = freeWeight()
        val freeExtra = (width - naturalWidth) / freeWeight
        // freeExtra may end up negative; if our natural width is too wide

        val halign = resolveStyle(elems, Style.HALIGN)
        val startX = left + if (freeWeight == 0f) halign.offset(naturalWidth, width) else 0f
        var x = startX

        val valign = resolveStyle(elems, Style.VALIGN)
        var y = top + valign.offset(m.totalHeight(_rowgap.toFloat()), height)

        for (elem in elems) {
            val colspan = colspan(elem)
            assert(col + colspan <= columns)

            var colWidth = 0f
            for (ii in 0..colspan - 1) {
                colWidth += Math.max(0f, m.columnWidths!![col + ii] + if (freeWeight == 0f) 0f else freeExtra * _columns[col + ii]._weight)
            }
            colWidth += ((colspan - 1) * _colgap).toFloat()

            val ccfg = _columns[col]
            val rowHeight = m.rowHeights!![row]
            if (colWidth > 0 && elem.isVisible) {
                val psize = preferredSize(elem, 0f, 0f) // will be cached, hints ignored
                val elemWidth = if (colspan > 1 || ccfg._stretch)
                    colWidth
                else
                    Math.min(psize.width, colWidth)
                val elemHeight = if (_vstretch) rowHeight else Math.min(psize.height, rowHeight)
                setBounds(elem, x + ccfg._halign.offset(elemWidth, colWidth),
                        y + _rowVAlign.offset(elemHeight, rowHeight), elemWidth, elemHeight)
            }
            x += colWidth + _colgap
            col += colspan
            if (col == columns) {
                col = 0
                x = startX
                if (rowHeight > 0) y += rowHeight + _rowgap
                row++
            }
        }
    }

    private fun freeWeight(): Float {
        var freeWeight = 0f
        for (ii in _columns.indices) freeWeight += _columns[ii]._weight
        return freeWeight
    }

    private fun computeMetrics(elems: Container<*>, hintX: Float, hintY: Float): Metrics {
        val columns = _columns.size
        var cells = 0
        for (elem in elems) cells += colspan(elem)
        var rows = cells / columns
        if (cells % columns != 0) rows++

        val metrics = Metrics()
        metrics.columnWidths = FloatArray(columns)
        metrics.rowHeights = FloatArray(rows)

        // note the minimum width constraints
        for (cc in 0..columns - 1) metrics.columnWidths!![cc] = _columns[cc]._minWidth

        // compute the preferred size of the fixed columns
        var ii = 0
        for (elem in elems) {
            val col = ii % columns
            val row = ii / columns
            if (elem.isVisible && _columns[col]._weight == 0f) {
                val psize = preferredSize(elem, hintX, hintY)
                metrics.rowHeights!![row] = Math.max(metrics.rowHeights!![row], psize.height)

                // Elements which stretch across multiple columns shouldn't force their first column
                //  to have a large size. Ideally, this should somehow force the sum of the columns
                //  to be as wide as itself.
                if (colspan(elem) == 1) {
                    metrics.columnWidths!![col] = Math.max(metrics.columnWidths!![col], psize.width)
                }
            }
            ii += colspan(elem)
        }

        // determine the total width needed by the fixed columns, then compute the hint given to
        // free columns based on the remaining space
        var fixedWidth = (_colgap * (columns - 1)).toFloat() // start with gaps, add fixed col widths
        for (cc in 0..columns - 1) fixedWidth += metrics.columnWidths!![cc]
        val freeHintX = (hintX - fixedWidth) / freeWeight()

        ii = 0
        for (elem in elems) {
            val col = ii % columns
            val row = ii / columns
            if (elem.isVisible && _columns[col]._weight > 0) {
                // TODO: supply sane y hint?
                val psize = preferredSize(elem, freeHintX, hintY)
                metrics.rowHeights!![row] = Math.max(metrics.rowHeights!![row], psize.height)
                metrics.columnWidths!![col] = Math.max(metrics.columnWidths!![col], psize.width)
            }
            ii += colspan(elem)
        }

        return metrics
    }

    private class Metrics {
        var columnWidths: FloatArray? = null
        var rowHeights: FloatArray? = null

        fun columns(): Int {
            return columnWidths!!.size
        }

        fun rows(): Int {
            return rowHeights!!.size
        }

        fun totalWidth(gap: Float): Float {
            return Companion.sum(columnWidths!!) + gap * (columns() - 1)
        }

        fun totalHeight(gap: Float): Float {
            return Companion.sum(rowHeights!!) + gap * (rows() - 1)
        }
    }

    companion object {
        /** The default column configuration.  */
        val COL = Column(Style.HAlign.CENTER, false, 1f, 0f)

        /**
         * Creates an array of `columns` columns, each with default configuration.
         */
        fun columns(count: Int): Array<Column> {
            return COL.copy(count)
        }

        /**
         * Configures a colspan constraint on `elem`.
         */
        fun <T : Element<*>> colspan(elem: T, colspan: Int): T {
            elem.setConstraint(Colspan(colspan))
            return elem
        }

        private fun colspan(elem: Element<*>): Int {
            val constraint = elem.constraint()
            return (constraint as? Colspan)?.colspan ?: 1
        }

        private fun sum(values: FloatArray): Float {
            var total = 0f
            for (value in values) {
                total += value
            }
            return total
        }
    }
}
