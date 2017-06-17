package tripleklay.ui

import react.Slot
import react.Value
import react.ValueView

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
    val selected = Value.create(null)

    init {
        selected.connect(object : ValueView.Listener<Element<*>>() {
            fun onChange(selected: Element<*>?, deselected: Element<*>?) {
                if (deselected != null) get(deselected).update(false)
                if (selected != null) get(selected).update(true)
            }
        })
    }

    /** Creates a selector containing the children of elements with initialSelection selected.  */
    constructor(elements: Elements<*>, initialSelection: Element<*>) : this() {
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
            _addSlot.onEmit(child)
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
            _removeSlot.onEmit(child)
        }
        elements.childAdded().disconnect(_addSlot)
        elements.childRemoved().disconnect(_removeSlot)
        return this
    }

    /**
     * Tracks one or more elements.
     */
    fun add(elem: Element<*>, vararg more: Element<*>): Selector {
        _addSlot.onEmit(elem)
        for (e in more) {
            _addSlot.onEmit(e)
        }
        return this
    }

    /**
     * Stops tracking one or more elements.
     */
    fun remove(elem: Element<*>, vararg more: Element<*>): Selector {
        _removeSlot.onEmit(elem)
        for (e in more) {
            _removeSlot.onEmit(e)
        }
        return this
    }

    /**
     * Internal method to get the selection value of an element (non-null).
     */
    protected operator fun get(elem: Element<*>): Value<Boolean> {
        return (elem as Togglable<*>).selected()
    }

    protected val _addSlot: Slot<Element<*>> = object : Slot<Element<*>>() {
        fun onEmit(child: Element<*>) {
            if (child is Togglable<*>) {
                (child as Togglable<*>).clicked().connect(_clickSlot)
            }
        }
    }

    protected val _removeSlot: Slot<Element<*>> = object : Slot<Element<*>>() {
        fun onEmit(removed: Element<*>) {
            if (removed is Togglable<*>) {
                (removed as Togglable<*>).clicked().disconnect(_clickSlot)
            }
            if (selected.get() === removed) selected.update(null)
        }
    }

    protected val _clickSlot: Slot<Element<*>> = object : Slot<Element<*>>() {
        fun onEmit(clicked: Element<*>) {
            val sel = get(clicked)
            if (_preventDeselection) {
                if (!sel.get()) {
                    sel.update(true)
                    return
                }
            }
            selected.update(if (sel.get()) clicked else null)
        }
    }

    protected var _preventDeselection: Boolean = false
}
