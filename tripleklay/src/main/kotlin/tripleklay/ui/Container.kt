package tripleklay.ui

import pythagoras.f.Dimension

/**
 * A shared base class for elements which contain other elements.
 */
abstract class Container<T : Container<T>> : Element<T>(), Iterable<Element<*>> {

    /** A container that allows mutation (adding and removal) of its children.  */
    abstract class Mutable<T : Container.Mutable<T>> : Container<T>() {
        /** Removes the specified child from this container.  */
        abstract fun remove(child: Element<*>)

        /** Removes the child at the specified index from this container.  */
        abstract fun removeAt(index: Int)

        /** Removes all children from this container.  */
        abstract fun removeAll()

        /** Removes and destroys the specified child.  */
        abstract fun destroy(child: Element<*>)

        /** Removes and destroys the child at the specified index.  */
        abstract fun destroyAt(index: Int)

        /** Removes and destroys all children from this container.  */
        abstract fun destroyAll()
    }

    /**
     * Returns the stylesheet associated with this container, or null. Styles are resolved by
     * searching up the container hierarchy. Only [Container] actually provides styles to its
     * children.
     */
    abstract fun stylesheet(): Stylesheet?

    /** Returns the number of children contained by this container.  */
    abstract fun childCount(): Int

    /*** Returns the child at the specified index.  */
    abstract fun childAt(index: Int): Element<*>

    /** Returns an unmodifiable iterator over the children of this Container.   */
    abstract override fun iterator(): Iterator<Element<*>>

    protected open fun didAdd(child: Element<*>) {
        layer.add(child.layer)
        child.wasParented(this)
        // bar n-child from being added twice
        if (isAdded && !child.willAdd()) {
            child.set(Element.Flag.IS_ADDING, true)
            child.wasAdded()
        }
    }

    protected open fun didRemove(child: Element<*>, dispose: Boolean) {
        if (dispose) child.set(Element.Flag.WILL_DISPOSE, true)
        layer.remove(child.layer)
        val needsRemove = child.willRemove() // early removal of a scheduled n-child
        child.wasUnparented()
        if (isAdded || needsRemove) {
            child.set(Element.Flag.IS_REMOVING, true)
            child.wasRemoved()
        }
        if (dispose) child.layer.close()
    }

    override fun wasAdded() {
        super.wasAdded()
        var ii = 0
        val count = childCount()
        while (ii < count) {
            val child = childAt(ii)
            child.set(Element.Flag.IS_ADDING, true)
            child.wasAdded()
            ii++
        }
    }

    override fun wasRemoved() {
        super.wasRemoved()
        val willDispose = isSet(Element.Flag.WILL_DISPOSE)
        var ii = 0
        val count = childCount()
        while (ii < count) {
            val child = childAt(ii)
            if (willDispose) child.set(Element.Flag.WILL_DISPOSE, true)
            child.set(Element.Flag.IS_REMOVING, true)
            child.wasRemoved()
            ii++
        }
        // if we're added again, we'll be re-laid-out
    }

    override fun computeSize(ldata: LayoutData, hintX: Float, hintY: Float): Dimension {
        return layout.computeSize(this, hintX, hintY)
    }

    override fun layout(ldata: LayoutData, left: Float, top: Float,
                        width: Float, height: Float) {
        // layout our children
        layout.layout(this, left, top, width, height)
        // layout is only called as part of revalidation, so now we validate our children
        var ii = 0
        val nn = childCount()
        while (ii < nn) {
            childAt(ii).validate()
            ii++
        }
    }

    protected abstract val layout: Layout

    companion object {
        /**
         * Removes and optionally destroys the given element from its parent, if the parent is a
         * mutable container. This is set apart as a utility method since it is not desirable to have
         * on all containers, but is frequently useful to have. The caller is willing to accept the
         * class cast exception if the parent container is not mutable. Does nothing if the element
         * has no parent.
         * @param element the element to remove
         * *
         * @param destroy whether to also destroy the element
         * *
         * @return true if the element had a parent and it was removed or destroyed
         */
        fun removeFromParent(element: Element<*>, destroy: Boolean): Boolean {
            if (element.parent() == null) return false
            val parent = element.parent() as Mutable<*>
            if (destroy)
                parent.destroy(element)
            else
                parent.remove(element)
            return true
        }
    }
}
