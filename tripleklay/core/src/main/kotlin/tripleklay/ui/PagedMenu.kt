package tripleklay.ui

import react.Slot
import react.UnitSlot
import react.Value
import react.ValueView

/**
 * A menu that is also capable of showing one page of its items at a time. Note that the caller
 * must connect buttons or sliders or some other UI elements within the menu to perform paging.
 *
 * Note that this implementation assumes items are added in order of their page. Removal and
 * of items and addition of items to the end of the last page is naturally supported.
 * TODO: support insertion of items in the middle of a page
 */
class PagedMenu
/**
 * Creates a new paged menu with the given layout and number of items per page.
 */
(layout: Layout,
 /** Number of items on a page is constant.  */
 val itemsPerPage: Int) : Menu(layout) {

    /**
     * Gets a view of the current page value.
     */
    fun page(): ValueView<Int> {
        return _page
    }

    /**
     * Gets a view of the number of pages value.
     */
    fun numPages(): ValueView<Int> {
        return _numPages
    }

    /**
     * Gets a slot that will update the page when emitted.
     */
    fun pageSlot(): Slot<Int> {
        return object : Slot<Int>() {
            fun onEmit(page: Int?) {
                setPage(page!!)
            }
        }
    }

    /**
     * Gets a slot that will increment the page by the given delta when emitted.
     */
    fun incrementPage(delta: Int): UnitSlot {
        return object : UnitSlot() {
            fun onEmit() {
                setPage(_page.get() + delta)
            }
        }
    }

    /**
     * Gets the current page.
     */
    val page: Int
        get() = _page.get().toInt()

    /**
     * Sets the current page. Items on the page are shown. All others are hidden.
     */
    fun setPage(page: Int): PagedMenu {
        val oldPage = _page.get()
        if (page != oldPage) {
            _page.update(page)
            updateVisibility(oldPage, oldPage)
            updateVisibility(page, page)
        }
        return this
    }

    protected fun pageOfItem(itemIdx: Int): Int {
        return itemIdx / itemsPerPage
    }

    protected fun updateNumPages() {
        val numItems = _items.size()
        _numPages.update(if (numItems == 0) 0 else (numItems - 1) / itemsPerPage + 1)
    }

    protected fun updateVisibility(fromPage: Int, toPage: Int) {
        var itemIdx = fromPage * itemsPerPage
        val size = _items.size()
        for (pp in fromPage..toPage) {
            val vis = pp == _page.get()
            for (ii in 0..itemsPerPage - 1) {
                if (itemIdx >= size) break
                _items.get(itemIdx++).setVisible(vis)
            }
        }
    }

    protected fun connectItem(item: MenuItem) {
        val itemIdx = _items.size()
        super.connectItem(item)
        updateNumPages()
        val page = pageOfItem(itemIdx)
        if (page != _page.get()) item.isVisible = false
        if (page <= _page.get()) updateVisibility(_page.get(), _page.get() + 1)
    }

    protected fun didDisconnectItem(item: MenuItem, itemIdx: Int) {
        updateNumPages()
        if (_page.get() === _numPages.get())
            incrementPage(-1)
        else {
            val page = pageOfItem(itemIdx)
            if (page < _page.get())
                updateVisibility(_page.get() - 1, _page.get())
            else if (page == _page.get()) updateVisibility(_page.get(), _page.get())
        }
    }

    protected var _page = Value.create(0)
    protected var _numPages = Value.create(0)
}
