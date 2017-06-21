package tripleklay.ui

import java.util.*

/**
 * A container with a fixed list of children, which client code must assume is immutable.
 * Subclasses may or may not expose the children directly. Subclasses may layout via the
 * [Layout] system or roll their own.
 * TODO: why not remove the publicly exposed mutating methods in Container instead of throwing
 * exceptions here?
 */
abstract class Composite<T : Composite<T>>
/**
 * Creates a new composite instance with no children. Subclasses are expected to call
 * [.initChildren] afterwards to supply the enumeration of children.
 */
protected constructor() : Container<T>() {
    /**
     * Sets the stylesheet of this composite.
     */
    fun setStylesheet(stylesheet: Stylesheet): T {
        _stylesheet = stylesheet
        invalidate()
        return asT()
    }

    override fun stylesheet(): Stylesheet? {
        return _stylesheet
    }

    override fun childCount(): Int {
        return _children.size
    }

    override fun childAt(index: Int): Element<*> {
        return _children[index]
    }

    override fun iterator(): Iterator<Element<*>> {
        return _children.iterator()
    }

    init {
        set(Element.Flag.HIT_DESCEND, true)
    }

    /**
     * Creates a new composite instance with the given children. The list is used directly.
     */
    protected constructor(children: List<Element<*>>) : this() {
        initChildren(children)
    }

    /**
     * Creates a new composite instance with the given children. The array is used directly.
     */
    protected constructor(vararg children: Element<*>) : this() {
        initChildren(*children)
    }

    /**
     * Sets the composite children; subclasses are expected to call this during construction or
     * supply children in [.Composite]. The list is used directly.
     */
    protected fun initChildren(children: List<Element<*>>) {
        if (!_children.isEmpty()) throw IllegalStateException()
        setChildren(children, false)
    }

    /**
     * Sets the composite children; subclasses are expected to call this during construction or
     * supply children in [.Composite]. The array is used directly.
     */
    protected fun initChildren(vararg children: Element<*>) {
        initChildren(Arrays.asList(*children))
    }

    /**
     * Sets the composite children; this is probably not needed for most composite types.
     */
    protected fun setChildren(children: List<Element<*>>, destroy: Boolean) {
        for (child in _children) didRemove(child, destroy)
        _children = children
        for (child in _children) didAdd(child)
        invalidate()
    }

    /**
     * Sets the optional layout. If not null, this composite will henceforth lay itself out by
     * delegation to it. If null or not called, subclasses must override
     * [.createLayoutData].
     */
    protected override var layout: Layout
        get() {
            if (_layout == null) throw IllegalStateException()
            return _layout!!
        }
        set(layout) {
            _layout = layout
            invalidate()
        }

    /** Children set by subclass.  */
    protected var _children = emptyList<Element<*>>()

    /** Optional layout set by subclass.  */
    protected var _layout: Layout? = null

    /** Optional stylesheet.  */
    protected var _stylesheet: Stylesheet? = null
}
