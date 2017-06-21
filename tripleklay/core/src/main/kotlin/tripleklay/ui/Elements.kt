package tripleklay.ui

import react.Signal
import react.SignalView

import java.util.*

/**
 * Contains other elements and lays them out according to a layout policy.
 */
abstract class Elements<T : Elements<T>>
/**
 * Creates a collection with the specified layout.
 */
(protected override val layout: Layout) : Container.Mutable<T>() {
    init {
        set(Element.Flag.HIT_DESCEND, true)
    }

    /** Emitted after a child has been added to this Elements.  */
    fun childAdded(): SignalView<Element<*>> {
        return _childAdded
    }

    /** Emitted after a child has been removed from this Elements.  */
    fun childRemoved(): SignalView<Element<*>> {
        return _childRemoved
    }

    /**
     * Returns the stylesheet configured for this group, or null.
     */
    override fun stylesheet(): Stylesheet? {
        return _sheet
    }

    /**
     * Configures the stylesheet to be used by this group.
     */
    fun setStylesheet(sheet: Stylesheet): T {
        _sheet = sheet
        return asT()
    }

    fun add(vararg children: Element<*>): T {
        // remove the children from existing parents, if any
        for (child in children) {
            Container.removeFromParent(child, false)
        }

        _children.addAll(Arrays.asList(*children))
        for (child in children) {
            didAdd(child)
        }
        invalidate()
        return asT()
    }

    fun add(index: Int, child: Element<*>): T {
        // remove the child from an existing parent, if it has one
        Container.removeFromParent(child, false)

        _children.add(index, child)
        didAdd(child)
        invalidate()
        return asT()
    }

    override fun childCount(): Int {
        return _children.size
    }

    override fun childAt(index: Int): Element<*> {
        return _children[index]
    }

    override fun iterator(): Iterator<Element<*>> {
        return Collections.unmodifiableList(_children).iterator()
    }

    override fun remove(child: Element<*>) {
        if (_children.remove(child)) {
            didRemove(child, false)
            invalidate()
        }
    }

    override fun removeAt(index: Int) {
        didRemove(_children.removeAt(index), false)
        invalidate()
    }

    override fun removeAll() {
        while (!_children.isEmpty()) {
            removeAt(_children.size - 1)
        }
        invalidate()
    }

    override fun destroy(child: Element<*>) {
        if (_children.remove(child)) {
            didRemove(child, true)
            invalidate()
        } else {
            child.layer.close()
        }
    }

    override fun destroyAt(index: Int) {
        didRemove(_children.removeAt(index), true)
        invalidate()
    }

    override fun destroyAll() {
        while (!_children.isEmpty()) {
            destroyAt(_children.size - 1)
        }
        invalidate()
    }

    override fun didAdd(child: Element<*>) {
        super.didAdd(child)
        _childAdded.emit(child)
    }

    override fun didRemove(child: Element<*>, destroy: Boolean) {
        super.didRemove(child, destroy)
        _childRemoved.emit(child)
    }

    protected val _children: MutableList<Element<*>> = ArrayList()

    protected val _childAdded = Signal<Element<*>>()
    protected val _childRemoved = Signal<Element<*>>()

    protected var _sheet: Stylesheet? = null
}
