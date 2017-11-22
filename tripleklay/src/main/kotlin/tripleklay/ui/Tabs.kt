package tripleklay.ui

import klay.core.assert
import react.Value
import tripleklay.ui.layout.AxisLayout
import tripleklay.ui.layout.FlowLayout
import tripleklay.ui.util.Supplier
import kotlin.reflect.KClass

/**
 * A `Composite` that implements tabbing. Has a horizontal row of buttons along
 * the top and a stretching content group underneath. The buttons are instances of [ ]. Each button is associated with a content element. When the button is clicked,
 * its content is shown alone in the content group.

 *
 * This diagram shows a `Tabs` with A and B tabs. When A's button is clicked, A's content
 * is shown. B content is not generated or not visible.
 * <pre>`--------------------------------------
 * |  -----  -----                      |
 * |  |*A*|  | B |                      |  <--- the buttons group, A selected
 * |  -----  -----                      |
 * |------------------------------------|
 * |                                    |  <--- the contentArea group
 * |  --------------------------------  |
 * |  |         A content            |  |
 * |  --------------------------------  |
 * |                                    |
 * --------------------------------------
`</pre> *

 *
 * The tab content associated with a button is supplied on demand via a [Supplier]
 * instance. The contract of `Supplier` is obeyed in that [Supplier.close] is called
 * whenever the associated tab goes out of scope.

 *
 * NOTE: The inheritance from Composite means that child elements may not be added or removed
 * directly. It you need, for example, a title bar, just use a Group with the title bar and tabs.

 * TODO: do we care about scrolling buttons? yes
 */
class Tabs : Composite<Tabs>() {
    /**
     * Defines the highlighting of a tab. A tab button may be highlighted if the application
     * wants to draw attention to it while it is unselected. When a button is selected, it
     * will be unhighlighted automatically. If the highlighter uses an external resource such
     * as a task or animation, it must ensure that the lifetime of the resource is tied to that
     * of the tab's button (in the hierarchy), or its layer.
     */
    interface Highlighter {
        /**
         * Sets the highlight state of the given tab.
         */
        fun highlight(tab: Tab, highlight: Boolean)
    }

    /**
     * Represents a tab: button and content.
     */
    inner class Tab
    /**
     * Creates a new tab with the supplied fields.
     */
    (
            /** The button, which will show this tab's content when clicked.  */
            val button: ToggleButton,
            /** The supplier of this tab's content element.  */
            val _generator: Supplier) {

        /**
         * Selects this tab. This is just a shortcut for [Tabs.selected].update(this).
         */
        fun select() {
            selected.update(this)
        }

        /**
         * Gets this tab's content, creating it if necessary.
         */
        fun content(): Element<*> {
            if (_content == null) _content = _generator.get()
            return _content!!
        }

        fun index(): Int {
            return _index
        }

        var isVisible: Boolean
            get() = button.isVisible
            set(visible) {
                if (!visible && selected.get() === this) selected.update(null)
                button.setVisible(visible)
            }

        fun parent(): Tabs {
            return this@Tabs
        }

        /** Gets the displayed name of the tab. This is a convenience for accessing the text of
         * the [.button].  */
        fun name(): String {
            return button.text.get()!!
        }

        /** The index of this tab in the parent [Tabs] instance.  */
        var _index = -1

        /** The content of this tab, if it has been shown before.  */
        var _content: Element<*>? = null
    }

    /** The row of buttons, one per tab.  */
    val buttons: Group

    /** The content group.  */
    val contentArea: Group

    /** The value containing the currently selected tab.  */
    val selected = Value<Tab?>(null)

    /**
     * Creates a new tabbed container.
     */
    init {
        // use a simple vertical layout
        layout = AxisLayout.vertical().gap(0).offStretch()
        buttons = Group(FlowLayout().gaps(3f))
        contentArea = Group(AxisLayout.horizontal().stretchByDefault().offStretch()).setConstraint(AxisLayout.stretched())
        initChildren(
                buttons,
                contentArea)

        val tabButtonSelector = Selector(buttons, null).preventDeselection()
        tabButtonSelector.selected.connect({ button: Element<*>? ->
            selected.update(forWidget(button!!))
        })

        selected.connect({ selected: Tab?, deselected: Tab? ->
            // remove the deselected content
            if (deselected != null) contentArea.remove(deselected.content())

            // show the new content, creating if necessary
            if (selected != null) {
                // own it baby
                if (selected.content().parent() !== contentArea)
                    contentArea.add(selected.content())
                // unhighlight
                highlighter().highlight(selected, false)
            }
            // now update the button (will noop if we're called from above slot)
            tabButtonSelector.selected.update(selected?.button)
        })
    }

    /**
     * Gets the number of tabs.
     */
    fun tabCount(): Int {
        return _tabs.size
    }

    /**
     * Gets the tab at the given index, or null if the index is out of range.
     */
    fun tabAt(index: Int): Tab? {
        return if (index >= 0 && index <= _tabs.size) _tabs[index] else null
    }

    /**
     * Adds a new tab to the container with the given label and supplier. Adds a new button to
     * the [.buttons] group. The supplier is used to generate an element to put in the
     * [.contentArea] group if and when the tab is selected.
     * @return the newly added tab
     */
    fun add(label: String, supplier: Supplier): Tab {
        return add(label, null, supplier)
    }

    /**
     * Adds a new tab to the container with a pre-constructed element for its content. This is a
     * shortcut for calling [.add] with a [Supplier.auto].
     * @return the newly added tab
     */
    fun add(label: String, panel: Element<*>): Tab {
        return add(label, Supplier.auto(panel))
    }

    /**
     * Adds a new tab to the container with the given label, icon and supplier. Adds a new button
     * to the [.buttons] group. The supplier is used to generate an element to put in the
     * [.contentArea] group when the tab is selected.
     * @return the newly added tab
     */
    fun add(label: String, icon: Icon?, supplier: Supplier): Tab {
        val tab = Tab(ToggleButton(label, icon), supplier)
        tab._index = _tabs.size
        _tabs.add(tab)
        buttons.add(tab.button)
        return tab
    }

    /**
     * Adds a new tab to the container with a pre-constructed element for its content.
     * See [Tabs.add].
     * @return the newly added tab
     */
    fun add(label: String, icon: Icon, panel: Element<*>): Tab {
        return add(label, icon, Supplier.auto(panel))
    }

    /**
     * Moves the given tab into the given position.
     */
    fun repositionTab(tab: Tab, position: Int) {
        val prev = tab.index()
        assert(prev != -1 && position >= 0 && position < _tabs.size)
        if (prev == position) return
        _tabs.removeAt(prev)
        buttons.remove(tab.button)
        _tabs.add(position, tab)
        buttons.add(position, tab.button)
        resetIndices()
    }

    /**
     * Removes the given tab and destroys its resources.
     */
    fun destroyTab(tab: Tab) {
        assert(_tabs.contains(tab)) { "Tab isn't ours" }
        if (tab === selected.get()) selected.update(null)
        _tabs.removeAt(tab.index())
        buttons.destroy(tab.button)
        if (tab._content != null) contentArea.destroy(tab._content!!)
        tab._generator.close()
        tab._index = -1
        resetIndices()
        return
    }

    /**
     * Gets our highlighter. Resolved from the [.HIGHLIGHTER] style.
     */
    fun highlighter(): Highlighter {
        if (_highlighter == null) _highlighter = resolveStyle(HIGHLIGHTER)
        return _highlighter!!
    }

    override fun clearLayoutData() {
        super.clearLayoutData()
        _highlighter = null
    }

    override val styleClass: KClass<*>
        get() = Tabs::class

    override fun wasAdded() {
        super.wasAdded()
        // if we don't have a selected tab, select the first one that's visible
        if (selected.get() == null) {
            for (ii in 0..tabCount() - 1) {
                if (tabAt(ii)!!.isVisible) {
                    selected.update(tabAt(ii))
                    break
                }
            }
        }
    }

    override fun wasRemoved() {
        if (willDispose()) {
            // let go of suppliers
            for (tab in _tabs) {
                tab._generator.close()
            }
            // let go of removed tabs
            for (tab in _tabs) {
                if (tab._content != null && tab._content!!.parent() == null) {
                    tab._content!!.layer.close()
                    tab._content = null
                }
            }
        }
        super.wasRemoved()
    }

    /** Sets the [Tab._index] field of our tabs, after a change to ordering.  */
    private fun resetIndices() {
        for (ii in _tabs.indices) {
            _tabs[ii]._index = ii
        }
    }

    /** Looks up a tab with the given button.  */
    private fun forWidget(widget: Element<*>): Tab? {
        for (tab in _tabs) {
            if (tab.button === widget) {
                return tab
            }
        }
        return null
    }

    private var _tabs: MutableList<Tab> = ArrayList()
    private var _highlighter: Highlighter? = null

    companion object {

        /** A no-op highlighter to use if you want to make highlighting do nothing.  */
        var NOOP_HIGHLIGHTER: Highlighter = object : Highlighter {
            override fun highlight(tab: Tab, highlight: Boolean) {}
        }

        /** Style for highlighting a tab. The default value is a no-op highlighter.  */
        var HIGHLIGHTER = Style.newStyle(true,
                NOOP_HIGHLIGHTER)

        /**
         * Creates a highlighter that will simply change the button's text color.
         * @param originalColor the button text color when unhighlighted
         * *
         * @param highlightColor the button text color when highlighted
         */
        fun textColorHighlighter(
                originalColor: Int, highlightColor: Int): Highlighter {
            return object : Highlighter {
                override fun highlight(tab: Tab, highlight: Boolean) {
                    if (tab.button.isSelected && highlight) return
                    tab.button.addStyles(Style.COLOR.`is`(if (highlight) highlightColor else originalColor))
                }
            }
        }
    }
}
