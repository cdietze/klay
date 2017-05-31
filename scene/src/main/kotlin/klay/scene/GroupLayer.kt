package klay.scene

import klay.core.Surface
import pythagoras.f.AffineTransform
import pythagoras.f.MathUtil
import pythagoras.f.Point
import pythagoras.util.NoninvertibleTransformException
import java.util.*

/**
 * GroupLayer creates a Layer hierarchy by maintaining an ordered group of child Layers.
 */
open class GroupLayer : ClippedLayer, Iterable<Layer> {

    private val children = ArrayList<Layer>()
    private val paintTx = AffineTransform()
    private val disableClip: Boolean

    /** Creates an unclipped group layer. Unclipped groups have no defined size.  */
    constructor() : super(0f, 0f) {
        disableClip = true
    }

    /** Creates a clipped group layer with the specified size.  */
    constructor(width: Float, height: Float) : super(width, height) {
        disableClip = false
    }

    /** Returns whether this group has any child layers.  */
    val isEmpty: Boolean
        get() = children.isEmpty()

    /** Returns the number of child layers in this group.  */
    fun children(): Int {
        return children.size
    }

    /**
     * Returns the layer at the specified index. Layers are ordered in terms of their depth and will
     * be returned in this order, with 0 being the layer on bottom.
     */
    fun childAt(index: Int): Layer {
        return children[index]
    }

    /**
     * Adds a layer to the bottom of the group. Because the [Layer] hierarchy is a tree, if
     * `child` is already a child of another [GroupLayer], it will be removed before
     * being added to this [GroupLayer].
     */
    fun add(child: Layer) {
        // optimization if we're requested to add a child that's already added
        val parent = child.parent()
        if (parent === this) return

        // if this child has equal or greater depth to the last child, we can append directly and avoid
        // a log(N) search; this is helpful when all children have the same depth
        val count = children.size
        val index: Int
        if (count == 0 || children[count - 1].depth() <= child.depth())
            index = count
        else
            index = findInsertion(child.depth())// otherwise find the appropriate insertion point via binary search

        // remove the child from any existing parent, preventing multiple parents
        parent?.remove(child)
        children.add(index, child)
        child.setParent(this)
        if (state.get() === State.ADDED) child.onAdd()

        // if this child is active, we need to become active
        if (child.interactive()) setInteractive(true)
    }

    /**
     * Adds all supplied children to this layer, in order. See [.add].
     */
    fun add(child0: Layer, child1: Layer, vararg childN: Layer) {
        add(child0)
        add(child1)
        for (child in childN) add(child)
    }

    /**
     * Adds the supplied layer to this group layer, adjusting its translation (relative to this group
     * layer) to the supplied values.

     *
     * This is equivalent to: `add(child.setTranslation(tx, ty))`.
     */
    fun addAt(child: Layer, tx: Float, ty: Float) {
        add(child.setTranslation(tx, ty))
    }

    /**
     * Adds `child` to this group layer, positioning it such that its center is at (`tx`,
     * `tx`). The layer must report a non-zero size, thus this will not work on an unclipped
     * group layer.

     *
     * This is equivalent to: `add(child.setTranslation(tx - child.width()/2,
     * ty - child.height()/2))`.
     */
    fun addCenterAt(child: Layer, tx: Float, ty: Float) {
        add(child.setTranslation(tx - child.width() / 2, ty - child.height() / 2))
    }

    /**
     * Adds `child` to this group layer, adjusting its translation (relative to this group
     * layer) to `floor(tx), floor(ty)`. This is useful for adding layers which display text a
     * text can become blurry if it is positioned on sub-pixel boundaries.
     */
    fun addFloorAt(child: Layer, tx: Float, ty: Float) {
        add(child.setTranslation(MathUtil.ifloor(tx).toFloat(), MathUtil.ifloor(ty).toFloat()))
    }

    /**
     * Removes a layer from the group.
     */
    fun remove(child: Layer) {
        val index = findChild(child, child.depth())
        if (index < 0) {
            throw UnsupportedOperationException(
                    "Could not remove Layer because it is not a child of the GroupLayer " +
                            "[group=" + this + ", layer=" + child + "]")
        }
        remove(index)
    }

    /**
     * Removes all supplied children from this layer, in order. See [.remove].
     */
    fun remove(child0: Layer, child1: Layer, vararg childN: Layer) {
        remove(child0)
        remove(child1)
        for (child in childN) remove(child)
    }

    /**
     * Removes all child layers from this group.
     */
    fun removeAll() {
        while (!children.isEmpty()) remove(children.size - 1)
    }

    /**
     * Removes and disposes all child layers from this group.
     */
    fun disposeAll() {
        val toDispose : Array<Layer> = children.toTypedArray()
        // remove all of the children efficiently
        removeAll()
        // now that the children have been detached, dispose them
        for (child in toDispose) child.close()
    }

    override fun iterator(): Iterator<Layer> {
        return children.iterator()
    }

    override fun close() {
        super.close()
        disposeAll()
    }

    override fun hitTestDefault(point: Point): Layer? {
        val x = point.x
        val y = point.y
        var sawInteractiveChild = false
        // we check back to front as children are ordered "lowest" first
        for (ii in children.indices.reversed()) {
            val child = children[ii]
            if (!child.interactive()) continue // ignore non-interactive children
            sawInteractiveChild = true // note that we saw an interactive child
            if (!child.visible()) continue // ignore invisible children
            try {
                // transform the point into the child's coordinate system
                child.transform().inverseTransform(point.set(x, y), point)
                point.x += child.originX()
                point.y += child.originY()
                val l = child.hitTest(point)
                if (l != null)
                    return l
            } catch (nte: NoninvertibleTransformException) {
                // Degenerate transform means no hit
                continue
            }

        }
        // if we saw no interactive children and we don't have listeners registered directly on this
        // group, clear our own interactive flag; this lazily deactivates this group after its
        // interactive children have been deactivated or removed
        if (!sawInteractiveChild && !hasEventListeners()) setInteractive(false)
        return super.hitTestDefault(point)
    }

    override fun disableClip(): Boolean {
        return disableClip
    }

    override fun toString(buf: StringBuilder) {
        super.toString(buf)
        buf.append(", children=").append(children.size)
    }

    override fun paintClipped(surf: Surface) {
        // save our current transform and restore it before painting each child
        paintTx.set(surf.tx())
        // iterate manually to avoid creating an Iterator as garbage, this is inner-loop territory
        val children = this.children
        var ii = 0
        val ll = children.size
        while (ii < ll) {
            surf.tx().set(paintTx)
            children[ii].paint(surf)
            ii++
        }
    }

    internal fun depthChanged(child: Layer, oldDepth: Float): Int {
        // locate the child whose depth changed
        val oldIndex = findChild(child, oldDepth)

        // fast path for depth changes that don't change ordering
        val newDepth = child.depth()
        val leftCorrect = oldIndex == 0 || children[oldIndex - 1].depth() <= newDepth
        val rightCorrect = oldIndex == children.size - 1 || children[oldIndex + 1].depth() >= newDepth
        if (leftCorrect && rightCorrect) {
            return oldIndex
        }

        // it would be great if we could move an element from one place in an ArrayList to another
        // (portably), but instead we have to remove and re-add
        children.removeAt(oldIndex)
        val newIndex = findInsertion(newDepth)
        children.add(newIndex, child)
        return newIndex
    }

    override fun onAdd() {
        super.onAdd()
        var ii = 0
        val ll = children.size
        while (ii < ll) {
            children[ii].onAdd()
            ii++
        }
    }

    override fun onRemove() {
        super.onRemove()
        var ii = 0
        val ll = children.size
        while (ii < ll) {
            children[ii].onRemove()
            ii++
        }
    }

    // group layers do not deactivate when their last event listener is removed; they may still have
    // interactive children to which events need to be dispatched; when a hit test is performed on a
    // group layer and it discovers that it has no interactive children, it will deactivate itself
    override fun deactivateOnNoListeners(): Boolean {
        return false
    }

    private fun remove(index: Int) {
        val child = children.removeAt(index)
        child.onRemove()
        child.setParent(null)
    }

    // uses depth to improve upon a full linear search
    private fun findChild(child: Layer, depth: Float): Int {
        // findInsertion will find us some element with the same depth as the to-be-removed child
        val startIdx = findInsertion(depth)
        // search down for our child
        for (ii in startIdx - 1 downTo 0) {
            val c = children[ii]
            if (c === child) {
                return ii
            }
            if (c.depth() !== depth) {
                break
            }
        }
        // search up for our child
        var ii = startIdx
        val ll = children.size
        while (ii < ll) {
            val c = children[ii]
            if (c === child) {
                return ii
            }
            if (c.depth() !== depth) {
                break
            }
            ii++
        }
        return -1
    }

    // who says you never have to write binary search?
    private fun findInsertion(depth: Float): Int {
        var low = 0
        var high = children.size - 1
        while (low <= high) {
            val mid = (low + high).ushr(1)
            val midDepth = children[mid].depth()
            if (depth > midDepth) {
                low = mid + 1
            } else if (depth < midDepth) {
                high = mid - 1
            } else {
                return mid
            }
        }
        return low
    }
}
