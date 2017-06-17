package tripleklay.ui

import pythagoras.f.Dimension
import pythagoras.f.IRectangle
import pythagoras.f.Rectangle
import react.Closeable
import react.UnitSlot
import tripleklay.ui.layout.AxisLayout

import java.util.ArrayList

import tripleplay.ui.Log.log

/**
 * A scrolling vertical display, optimized for showing potentially very long lists such as a chat
 * log. Supports:

 *
 *  * addition of new elements on the end
 *  * pruning old elements from the beginning
 *  * progressive rendering of newly visible items in the list
 *  * automatically keeping the last item visible
 *  * purging of old rendered elements that are no longer visible
 *

 * Items are stored in a backing array. Each entry in the array may or may not have a corresponding
 * Element visible (presuming that the rendering and storage of elements is expensive). When the
 * user scrolls, entries are rendered on demand using [.render]. Entries that are not visible
 * use an estimated size for layout purposes. This of course may produce some artifacts during
 * scrolling, which is the penalty of not computing the exact size.

 *
 * NOTE: The elements in the UI (type `W`) must not be mutated after rendering and must
 * have a constant size given a particular item and width. See [.render].

 * @param <T> The type of item backing this history
 * *
 * @param <W> The type of element or widget stored in this history
</W></T> */
abstract class HistoryGroup<T, W : Element<*>>
/** Creates a new history group.  */
protected constructor() : Composite<HistoryGroup<T, W>>() {
    /** A label that exposes the width hint and preferred size.  */
    class RenderableLabel
    /** Creates a new label.  */
    (text: String) : Label(text) {

        /** Calculates the size of the label, using the given width hint.  */
        fun calcSize(hintX: Float): Dimension {
            return Dimension(preferredSize(hintX, 0f))
        }
    }

    /** History group of just labels. This makes some lightweight use cases really easy.  */
    class Labels : HistoryGroup<String, RenderableLabel>() {
        override fun render(entry: Entry) {
            entry.element = RenderableLabel(entry.item)
        }

        override fun calcSize(entry: Entry) {
            entry.size = entry.element!!.calcSize(_entriesWidth)
        }
    }

    /** Tests if the history is currently at the bottom. If the history is at the bottom, then
     * subsequent additions will cause automatic scrolling. By default, new groups are at the
     * bottom. Subsequent upward scrolling will clear this and scrolling to the bottom will
     * set it again.  */
    fun atBottom(): Boolean {
        return _atBottom
    }

    /** Issues a request to scroll to the bottom of the list of history elements.  */
    fun scrollToBottom() {
        _scroller.queueScroll(0f, java.lang.Float.MAX_VALUE)
    }

    /** Prunes the given number of entries from the beginning of the history.  */
    fun pruneOld(adjustment: Int) {
        if (adjustment != 0) {
            _entriesGroup.removeOldEntries(_baseIndex + adjustment)
            _entries.subList(0, Math.min(adjustment, _entries.size)).clear()
            _baseIndex += adjustment
        }
    }

    /** Adds a new item to the end of the history. If the history is at the bottom, the item
     * will be rendered immediately, otherwise the message group is invalidated so that the
     * scroll bounds will be updated.  */
    fun addItem(item: T) {
        // always add a new entry
        val entry = addEntry(item)

        // if we're not currently displayed, do nothing else
        if (!_added) {
            return
        }

        // render immediately if at the bottom
        if (atBottom()) {
            _entriesGroup.addEntry(entry)
        } else {
            _entriesGroup.invalidate()
        }

        // pick up the changes, if any (probably not)
        schedule()

        // keep up with the scrolling
        maybeScrollToBottom()
    }

    /** Sets the vertical gap between history elements. By default, the gap is set to 1.  */
    fun setVerticalGap(vgap: Int) {
        _vgap = vgap
        _entriesGroup.invalidate()
    }

    protected fun update() {
        if (!_added) {
            log.warning("Whassup, scheduled while removed?")
            cancel()
            return
        }

        if (_widthUpdated) {
            // a bit cumbersome, but rare... remove all previously created labels
            _entriesGroup.removeAllEntries()
            _widthUpdated = false
        }

        // maybe wait until next frame to get valid
        if (!isSet(Element.Flag.VALID)) return

        if (_entries.size == 0) {
            // no entries, we're done here
            cancel()
            return
        }

        // walk up from the bottom and render the first null one
        var bottom = findEntry(_scroller.ypos() + _viewHeight)
        var top = bottom
        while (top >= 0) {
            val e = _entries[top]
            if (e.ypos + e.size.height() < _scroller.ypos()) {
                break
            }
            if (e.element == null) {
                // render this one and do more next update
                // TODO: use a maximum frame time
                _entriesGroup.addEntry(e)
                return
            }
            top--
        }

        // all entries in view are rendered, now delete ones that are far away
        val miny = _scroller.ypos() - _viewHeight
        val maxy = _scroller.ypos() + _viewHeight * 2

        // walk up one more view height
        while (top >= 0) {
            val e = _entries[top]
            if (e.bottom() < miny) {
                break
            }
            top--
        }

        // walk down one more view height
        val size = _entries.size
        while (bottom < size) {
            val e = _entries[bottom]
            if (e.ypos >= maxy) {
                break
            }
            bottom++
        }

        _entriesGroup.removeEntriesNotInRange(_baseIndex + top, _baseIndex + bottom)
        cancel()
    }

    init {
        layout = AxisLayout.horizontal().stretchByDefault().offStretch()
        initChildren(_scroller = Scroller(_entriesGroup = EntriesGroup()).setBehavior(Scroller.Behavior.VERTICAL))
    }

    /** Sets up the [Entry.element] member. After this call, the element will be added to
     * the group so that style information is available.
     *
     * Note that the `Element.size()` value is ignored and only the entry size is
     * considered during layout, as determined in [.calcSize].  */
    protected abstract fun render(entry: Entry)

    /** Calculates and sets the [Entry.size] member, according to the current
     * [._entriesWidth]. Normally this must be done using a Widget that exposes its
     * `preferredSize` method and allows a wrap width to be set.
     *
     * This method is called after the [Entry.element] member is added to the group so
     * that style information can be determined.  */
    protected abstract fun calcSize(entry: Entry)

    /** Called during layout after a change to [._entriesWidth] occurs. Subclasses may want
     * to update some internal layout state that relies on the width.  */
    protected fun didUpdateWidth(oldWidth: Float) {}

    /** Sets the estimated height for entries that are currently not in view.  */
    protected fun setEstimatedHeight(height: Float) {
        _estimatedSize = Dimension(1f, height)
        _entriesGroup.invalidate()
    }

    /** Scroll to the bottom if they were already at the bottom.  */
    protected fun maybeScrollToBottom() {
        if (atBottom()) {
            scrollToBottom()
        }
    }

    /** Convenience method to clear out all currently rendered messages and do them again.  */
    protected fun resetEntries() {
        _entriesGroup.removeAllEntries()
        schedule()
    }

    /** Adds a new history entry without the UI check or the scrolling to bottom. Useful for batch
     * additions from the subclass/game model of the backing storage.  */
    protected fun addEntry(item: T): Entry {
        val entry = Entry(item, _baseIndex + _entries.size)
        _entries.add(entry)
        return entry
    }

    override fun wasAdded() {
        super.wasAdded()
        _added = true

        // update the elements for visible entries later
        schedule()
    }

    override fun wasRemoved() {
        _added = false

        // free up the all currently rendered elements for garbage collection
        _entriesGroup.removeAllEntries()

        // kill off task
        cancel()

        super.wasRemoved()
    }

    protected override val styleClass: Class<*>
        get() = HistoryGroup<*, *>::class.java

    protected fun schedule() {
        if (_conn === Closeable.Util.NOOP && _added) {
            _conn = root()!!.iface.frame.connect(object : UnitSlot() {
                fun onEmit() {
                    update()
                }
            })
        }
    }

    protected fun cancel() {
        _conn = Closeable.Util.close(_conn)
    }

    /** Find the index of the entry at the given y position.  */
    protected fun findEntry(ypos: Float): Int {
        val max = _entries.size - 1
        var low = 0
        var high = max
        while (low <= high) {
            val mid = (low + high).ushr(1)
            val midpos = _entries[mid].ypos
            if (ypos > midpos) {
                low = mid + 1
            } else if (ypos < midpos) {
                high = mid - 1
            } else {
                return mid // key found
            }
        }
        return Math.min(low, max)
    }

    /**
     * An item in the history and associated layout info.
     */
    protected inner class Entry
    /**
     * Creates a new entry.
     */
    (
            /** The item.  */
            val item: T,
            /** The unique index for this entry (increases by one per new entry).  */
            val index: Int) {

        /** The last rendered size of the entry, or the estimated size if not in view.  */
        var size = _estimatedSize

        /** The y position of the top of this entry.  */
        var ypos: Float = 0.toFloat()

        /** The rendered element for this entry, if it is currently in view.  */
        var element: W? = null

        /** Do the full on render of everything, if needed.  */
        fun render(): W {
            if (element != null) {
                return element
            }

            this@HistoryGroup.render(this)
            return element
        }

        fun bottom(): Float {
            return ypos + size.height()
        }
    }

    /** Groups the rendered items in the history.  */
    protected inner class EntriesGroup : Group(EntriesLayout()), Scroller.Clippable {

        override fun setViewArea(width: Float, height: Float) {
            _viewHeight = height
            maybeScrollToBottom()
        }

        override fun setPosition(x: Float, y: Float) {
            val bounds = bounds(Rectangle())
            if (_viewHeight > bounds.height()) {
                // nail the group to the bottom of the scroll area.
                layer.setTranslation(x, _viewHeight - bounds.height())
                _atBottom = true
            } else {
                layer.setTranslation(x, Math.floor(y.toDouble()).toFloat())
                _atBottom = -y == bounds.height() - _viewHeight
            }
            schedule()
        }

        fun addEntry(e: Entry) {
            // find the place to insert the entry
            var position = _renderedEntries.size - 1
            while (position >= 0) {
                val test = _renderedEntries[position]
                if (e.index > test.index) {
                    break
                }
                position--
            }

            // add the rendered item to the ui
            position++
            add(position, e.render())
            calcSize(e)

            // keep track of what we've rendered
            _renderedEntries.add(position, e)
        }

        fun removeEntry(e: Entry) {
            val index = _renderedEntries.indexOf(e)
            if (index == -1)
                throw IllegalArgumentException(
                        "Removing entry that isn't in the list: " + e)
            removeUI(index)
        }

        fun removeOldEntries(minIndex: Int) {
            removeEntriesNotInRange(minIndex, Integer.MAX_VALUE)
        }

        fun removeEntriesNotInRange(minIndex: Int, maxIndex: Int) {
            var ii = 0
            while (ii < _renderedEntries.size) {
                val index = _renderedEntries[ii].index
                if (index < minIndex || index > maxIndex) {
                    removeUI(ii)
                } else {
                    ii++
                }
            }
        }

        fun removeAllEntries() {
            while (!_renderedEntries.isEmpty()) {
                removeUI(_renderedEntries.size - 1)
            }
        }

        protected fun removeUI(index: Int) {
            if (childAt(index) !== _renderedEntries[index].element)
                throw IllegalArgumentException("Mismatched entry and element")
            destroyAt(index)
            _renderedEntries[index].element = null
            _renderedEntries.removeAt(index)
        }

        /** List of entries in exact correspondence with _children.  */
        protected var _renderedEntries: MutableList<Entry> = ArrayList()
    }

    /**
     * Lays out the history items.
     */
    protected inner class EntriesLayout : Layout() {
        override fun computeSize(elems: Container<*>, hintX: Float, hintY: Float): Dimension {
            // report a large width since we expect to always be stretched, not fixed
            val size = Dimension(4096f, 0f)
            if (!_entries.isEmpty()) {
                size.height += (_vgap * (_entries.size - 1)).toFloat()
                for (e in _entries) {
                    size.height += e.size.height()
                }
            }
            return size
        }

        override fun layout(elems: Container<*>, left: Float, top: Float, width: Float, height: Float) {
            var top = top
            // deal with width updates
            if (_entriesWidth != width) {
                // update our width
                val old = _entriesWidth
                _entriesWidth = width

                didUpdateWidth(old)

                // schedule the refresh
                _widthUpdated = true
                schedule()
            }

            // update all entries so they have a sensible ypos when needed
            for (e in _entries) {
                val eheight = e.size.height()
                if (e.element != null) {
                    setBounds(e.element, left, top, e.size.width(), eheight)
                }
                e.ypos = top
                top += eheight + _vgap
            }
        }
    }

    /** The scrollable area, our only proper child.  */
    protected var _scroller: Scroller

    /** The rendered items contained in the scrollable area.  */
    protected var _entriesGroup: EntriesGroup

    /** A frame tick registration, or NOOP if we're not updating.  */
    protected var _conn = Closeable.Util.NOOP

    /** The list of history entries.  */
    protected var _entries: MutableList<Entry> = ArrayList()

    /** The current width of the rendered items group, or 0 prior to layout.  */
    protected var _entriesWidth: Float = 0.toFloat()

    /** The vertical gap between history items.  */
    protected var _vgap = 1

    /** The current height of the view area (this can be different from _scroller.size() if it
     * is ever given an inset background.  */
    protected var _viewHeight: Float = 0.toFloat()

    /** Set if we discover a change to the width during layout that needs to update UI on the
     * next update.  */
    protected var _widthUpdated: Boolean = false

    /** Set if we should automatically scroll to show newly added items.  */
    protected var _atBottom = true

    /** Tracks isAdded(), for faster testing.  */
    protected var _added: Boolean = false

    /** The unique index of the 0th entry in the history.  */
    protected var _baseIndex: Int = 0

    /** The size to use for new, unrendered, history entries.  */
    protected var _estimatedSize = Dimension(1f, 18f)
}
