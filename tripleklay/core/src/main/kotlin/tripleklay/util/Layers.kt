package tripleklay.util

import klay.core.Surface
import klay.scene.GroupLayer
import klay.scene.Layer
import klay.scene.LayerUtil
import klay.scene.Pointer
import pythagoras.f.IPoint
import pythagoras.f.Point
import pythagoras.f.Rectangle

/**
 * Provides utility functions for dealing with Layers
 */
object Layers {
    /** Prevents parent handling for pointer events. This is useful if you have for example a
     * button inside a scrolling container and need to enable event propagation.  */
    val NO_PROPAGATE: Pointer.Listener = object : Pointer.Listener {
        override fun onStart(iact: Pointer.Interaction) {
            stop(iact)
        }

        override fun onEnd(iact: Pointer.Interaction) {
            stop(iact)
        }

        override fun onDrag(iact: Pointer.Interaction) {
            stop(iact)
        }

        override fun onCancel(iact: Pointer.Interaction?) {
            stop(iact)
        }

        internal fun stop(iact: Pointer.Interaction?) {
            // TODO: event.flags().setPropagationStopped(true);
        }
    }

    /**
     * Transforms a point from one Layer's coordinate system to another's.
     */
    @JvmOverloads fun transform(p: IPoint, from: Layer, to: Layer, result: Point = Point()): Point {
        LayerUtil.layerToScreen(from, p, result)
        LayerUtil.screenToLayer(to, result, result)
        return result
    }

    /**
     * Removes `layer` from its current parent and adds it to `target`, modifying its
     * transform in the process so that it stays in the same position on the screen.
     */
    fun reparent(layer: Layer, target: GroupLayer) {
        val pos = Point(layer.tx(), layer.ty())
        LayerUtil.layerToScreen(layer.parent()!!, pos, pos)
        target.add(layer)
        LayerUtil.screenToLayer(layer.parent()!!, pos, pos)
        layer.setTranslation(pos.x, pos.y)
    }

    /**
     * Whether a GroupLayer hierarchy contains another layer somewhere in its depths.
     */
    fun contains(group: GroupLayer, layer: Layer?): Boolean {
        var layer = layer
        while (layer != null) {
            layer = layer!!.parent()
            if (layer === group) return true
        }
        return false
    }

    /**
     * Creates a new group with the given children.
     */
    fun group(vararg children: Layer): GroupLayer {
        val gl = GroupLayer()
        for (l in children) gl.add(l)
        return gl
    }

    /**
     * Adds a child layer to a group and returns the child.
     */
    fun <T : Layer> addChild(parent: GroupLayer, child: T): T {
        parent.add(child)
        return child
    }

    /**
     * Adds a child group to a parent group and returns the child.
     */
    fun addNewGroup(parent: GroupLayer): GroupLayer {
        return addChild(parent, GroupLayer())
    }

    /**
     * Creates a layer that renders a simple rectangle of the given color, width and height.
     */
    fun solid(color: Int, width: Float, height: Float): Layer {
        return object : Layer() {
            override fun width(): Float {
                return width
            }

            override fun height(): Float {
                return height
            }

            override fun paintImpl(surf: Surface) {
                surf.setFillColor(color).fillRect(0f, 0f, width, height)
            }
        }
    }

    /**
     * Computes the total bounds of the layer hierarchy rooted at `root`.
     * The returned Rectangle will be in `root`'s coordinate system.
     */
    fun totalBounds(root: Layer): Rectangle {
        // account for root's origin
        val r = Rectangle(root.originX(), root.originY(), 0f, 0f)
        addBounds(root, root, r, Point())
        return r
    }

    /** Helper function for [.totalBounds].  */
    private fun addBounds(root: Layer, l: Layer, bounds: Rectangle, scratch: Point) {
        val w = l.width()
        val h = l.height()
        if (w != 0f || h != 0f) {
            // grow bounds
            bounds.add(LayerUtil.layerToParent(l, root, scratch.set(0f, 0f), scratch))
            bounds.add(LayerUtil.layerToParent(l, root, scratch.set(w, h), scratch))
        }

        if (l is GroupLayer) {
            var ii = 0
            val ll = l.children()
            while (ii < ll) {
                addBounds(root, l.childAt(ii), bounds, scratch)
                ++ii
            }
        }
    }
}
/**
 * Transforms a point from one Layer's coordinate system to another's.
 */
