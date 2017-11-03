package tripleklay.ui.layout

import pythagoras.f.Dimension
import tripleklay.ui.Container
import tripleklay.ui.Layout
import tripleklay.ui.Style

/**
 * Lays out elements in horizontal rows, starting a new row when a width limit is reached. By
 * default, the hint width is used as the limit; this can be overridden with a fixed value.
 *
 * TODO: vertical
 */
class FlowLayout : Layout() {

    /**
     * Sets the maximum width of a row of elements. This should normally be used whenever a flow
     * layout governs `Elements` that have horizontal siblings. By default, the hint width
     * is used.
     */
    fun wrapAt(width: Float): FlowLayout {
        _wrapWidth = width
        return this
    }

    /**
     * Sets the gap, in pixels, to use between rows and between elements within a row.
     */
    fun gaps(gap: Float): FlowLayout {
        _vgap = gap
        _hgap = _vgap
        return this
    }

    /**
     * Sets the gap, in pixels, to use between rows and between elements within a row.
     * @param hgap the gap to use between elements in a row
     * *
     * @param vgap the gap to use between rows
     */
    fun gaps(hgap: Float, vgap: Float): FlowLayout {
        _hgap = hgap
        _vgap = vgap
        return this
    }

    /**
     * Sets the alignment used for positioning elements within their row. By default, elements are
     * not stretched and centered vertically: [tripleplay.ui.Style.VAlign.CENTER].
     */
    fun align(align: Style.VAlign): FlowLayout {
        _valign = align
        return this
    }

    /**
     * Stretch elements vertically to the maximum height of other elements in the same row. This
     * clears any previously set vertical alignment.
     */
    fun stretch(): FlowLayout {
        _valign = null
        return this
    }

    override fun computeSize(elems: Container<*>, hintX: Float, hintY: Float): Dimension {
        val m = computeMetrics(elems, hintX, hintY)
        return m.size
    }

    override fun layout(elems: Container<*>,
                        left: Float, top: Float, width: Float, height: Float) {
        val halign = resolveStyle(elems, Style.HALIGN)
        val m = computeMetrics(elems, width, height)
        var y = top + resolveStyle(elems, Style.VALIGN).offset(m.size.height, height)
        var elemIdx = 0
        var row = 0
        val size = m.rowBreaks.size
        while (row < size) {
            val rowSize = m.rows[row]
            var x = left + halign.offset(rowSize.width, width)
            while (elemIdx < m.rowBreaks[row]) {
                val elem = elems.childAt(elemIdx)
                if (!elem.isVisible) {
                    ++elemIdx
                    continue
                }
                val esize = preferredSize(elem, width, height)
                if (_valign == null) {
                    setBounds(elem, x, y, esize.width, rowSize.height)
                } else {
                    setBounds(elem, x, y + _valign!!.offset(esize.height, rowSize.height),
                            esize.width, esize.height)
                }
                x += esize.width + _hgap
                ++elemIdx
            }
            y += _vgap + rowSize.height
            ++row
        }
    }

    private fun computeMetrics(elems: Container<*>, width: Float, height: Float): Metrics {
        var width = width
        val m = Metrics()

        // adjust our maximum width if appropriate
        if (_wrapWidth != null) width = _wrapWidth!!

        // fill in components horizontally, breaking rows as needed
        var rowSize = Dimension()
        var ii = 0
        val ll = elems.childCount()
        while (ii < ll) {
            val elem = elems.childAt(ii)
            if (!elem.isVisible) {
                ++ii
                continue
            }
            val esize = preferredSize(elem, width, height)
            if (rowSize.width > 0f && width > 0f && rowSize.width + _hgap + esize.width > width) {
                m.addBreak(ii, rowSize)
                rowSize = Dimension(esize)
            } else {
                rowSize.width += (if (rowSize.width > 0f) _hgap else 0f) + esize.width
                rowSize.height = maxOf(esize.height, rowSize.height)
            }
            ++ii
        }
        m.addBreak(elems.childCount(), rowSize)
        return m
    }

    inner class Metrics {
        var size = Dimension()
        var rows: MutableList<Dimension> = ArrayList()
        var rowBreaks: MutableList<Int> = ArrayList()

        fun addBreak(idx: Int, lastRowSize: Dimension) {
            if (lastRowSize.height == 0f && lastRowSize.width == 0f) return
            rowBreaks.add(idx)
            rows.add(lastRowSize)
            size.height += (if (size.height > 0f) _vgap else 0f) + lastRowSize.height
            size.width = maxOf(size.width, lastRowSize.width)
        }
    }

    private var _hgap = DEFAULT_GAP
    private var _vgap = DEFAULT_GAP
    private var _wrapWidth: Float? = null
    private var _valign: Style.VAlign? = Style.VAlign.CENTER

    companion object {
        /** The default gap between rows and elements in a row.  */
        val DEFAULT_GAP = 5f
    }
}
