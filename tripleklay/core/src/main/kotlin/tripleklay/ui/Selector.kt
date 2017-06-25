package tripleklay.ui

import react.Slot
import react.Value
import react.ValueViewListener

/**
 * Maintains a single selected item among a specified set of `Element` instances. The
 * elements may be added individually, or the children of an `Elements` may be tracked
 * automatically.

 *
 * A click on a tracked element that implements `Clickable` makes it the selected item, or
 * `selected` can be used to manually control the selected item.
 */
class Selector
/** Create a selector with a null initial selection.  */
() {
    /** The selected item. May be updated to set the selection manually.  */
    val selected = Value<Element<*>?>(null)

    init {
        selected.connect(object : ValueViewListener<Element<*>?> {
            override fun invoke(selected: Element<*>?, deselected: Element<*>?) {
                if (deselected != null) get(deselected).update(false)
                if (selected != null) get(selected).update(true)
            }
        })
    }

    /** Creates a selector containing the children of elements with initialSelection selected.  */
    constructor(elements: Elements<*>, initialSelection: Element<*>?) : this() {
        add(elements)
        if (initialSelection is Togglable<*>) {
            selected.update(initialSelection)
        }
    }

    /**
     * Tracks the children of `elements` for setting the selection. Children subsequently
     * added or removed from `elements` are automatically handled appropriately.
     */
    fun add(elements: Elements<*>): Selector {
        for (child in elements) {
            _addSlot.invoke(child)
        }
        elements.childAdded().connect(_addSlot)
        elements.childRemoved().connect(_removeSlot)
        return this
    }

    /** Prevent a deselection (null [.selected].get()) occurring as a result of toggling
     * the currently selected button off.  */
    fun preventDeselection(): Selector {
        _preventDeselection = true
        return this
    }

    /**
     * Stops tracking the children of `elements` for setting the selection.
     */
    fun remove(elements: Elements<*>): Selector {
        for (child in elements) {
            _removeSlot.invoke(child)
        }
        elements.childAdded().disconnect(_addSlot)
        elements.childRemoved().disconnect(_removeSlot)
        return this
    }

    /**
     * Tracks one or more elements.
     */
    fun add(elem: Element<*>, vararg more: Element<*>): Selector {
        _addSlot.invoke(elem)
        for (e in more) {
            _addSlot.invoke(e)
        }
        return this
    }

    /**
     * Stops tracking one or more elements.
     */
    fun remove(elem: Element<*>, vararg more: Element<*>): Selector {
        _removeSlot.invoke(elem)
        for (e in more) {
            _removeSlot.invoke(e)
        }
        return this
    }

    /**
     * Internal method to get the selection value of an element (non-null).
     */
    private fun get(elem: Element<*>): Value<Boolean> {
        return (elem as Togglable<*>).selected()
    }

    private val _addSlot: Slot<Element<*>> = { child: Element<*> ->
        if (child is Togglable<*>) {
            (child as Togglable<*>).clicked().connect(_clickSlot)
        }
    }

    private val _removeSlot: Slot<Element<*>> = { removed: Element<*> ->
        if (removed is Togglable<*>) {
            (removed as Togglable<*>).clicked().disconnect(_clickSlot)
        }
        if (selected.get() === removed) selected.update(null)
    }

    private val _clickSlot: Slot<Element<*>> = { clicked: Element<*> ->
        val sel = get(clicked)
        if (_preventDeselection && !sel.get()) {
            sel.update(true)
        } else {
            selected.update(if (sel.get()) clicked else null)
        }
    }

    private var _preventDeselection: Boolean = false
}
