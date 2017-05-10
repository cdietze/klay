package klay.scene

import klay.core.Clock
import klay.core.Log
import pythagoras.f.Point
import pythagoras.f.XY
import pythagoras.util.NoninvertibleTransformException
import react.Closeable
import react.Signal
import react.Slot

/**
 * Utility class for transforming coordinates between [Layer]s.
 */
object LayerUtil {

    /**
     * Converts the supplied point from coordinates relative to the specified layer to screen
     * coordinates. The results are stored into `into`, which is returned for convenience.
     */
    fun layerToScreen(layer: Layer, point: XY, into: Point): Point {
        return layerToParent(layer, null, point, into)
    }

    /**
     * Converts the supplied point from coordinates relative to the specified
     * layer to screen coordinates.
     */
    fun layerToScreen(layer: Layer, x: Float, y: Float): Point {
        val into = Point(x, y)
        return layerToScreen(layer, into, into)
    }

    /**
     * Converts the supplied point from coordinates relative to the specified
     * child layer to coordinates relative to the specified parent layer. The
     * results are stored into `into`, which is returned for convenience.
     */
    fun layerToParent(layer: Layer?, parent: Layer?, point: XY, into: Point): Point {
        var layer = layer
        into.set(point)
        while (layer !== parent) {
            if (layer == null) {
                throw IllegalArgumentException(
                        "Failed to find parent, perhaps you passed parent, layer instead of " + "layer, parent?")
            }
            into.x -= layer!!.originX()
            into.y -= layer!!.originY()
            layer!!.transform().transform(into, into)
            layer = layer!!.parent()
        }
        return into
    }

    /**
     * Converts the supplied point from coordinates relative to the specified
     * child layer to coordinates relative to the specified parent layer.
     */
    fun layerToParent(layer: Layer, parent: Layer, x: Float, y: Float): Point {
        val into = Point(x, y)
        return layerToParent(layer, parent, into, into)
    }

    /**
     * Converts the supplied point from screen coordinates to coordinates
     * relative to the specified layer. The results are stored into `into`
     * , which is returned for convenience.
     */
    fun screenToLayer(layer: Layer, point: XY, into: Point): Point {
        val parent = layer.parent()
        val cur = if (parent == null) point else screenToLayer(parent, point, into)
        return parentToLayer(layer, cur, into)
    }

    /**
     * Converts the supplied point from screen coordinates to coordinates
     * relative to the specified layer.
     */
    fun screenToLayer(layer: Layer, x: Float, y: Float): Point {
        val into = Point(x, y)
        return screenToLayer(layer, into, into)
    }

    /**
     * Converts the supplied point from coordinates relative to its parent
     * to coordinates relative to the specified layer. The results are stored
     * into `into`, which is returned for convenience.
     */
    fun parentToLayer(layer: Layer, point: XY, into: Point): Point {
        layer.transform().inverseTransform(into.set(point), into)
        into.x += layer.originX()
        into.y += layer.originY()
        return into
    }

    /**
     * Converts the supplied point from coordinates relative to the specified parent to coordinates
     * relative to the specified child child. The results are stored into `into`, which is
     * returned for convenience.
     */
    fun parentToLayer(parent: Layer, child: Layer, point: XY, into: Point): Point {
        into.set(point)
        val immediateParent = child.parent()
        if (immediateParent !== parent) parentToLayer(parent, immediateParent ?: error("Supplied layers are not parent and child [parent is not a parent of child [parent=$parent, child=$child]"), into, into)
        parentToLayer(child, into, into)
        return into
    }

    /**
     * Returns the layer hit by (screen) position `p` (or null) in the scene graph rooted at
     * `root`, using [Layer.hitTest]. Note that `p` is mutated by this call.
     */
    fun getHitLayer(root: Layer, p: Point): Layer? {
        root.transform().inverseTransform(p, p)
        p.x += root.originX()
        p.y += root.originY()
        return root.hitTest(p)
    }

    /**
     * Returns true if an [XY] touches a [Layer]. Note: if the supplied layer has no
     * size, this will always return false.
     */
    fun hitTest(layer: Layer, pos: XY): Boolean {
        return hitTest(layer, pos.x, pos.y)
    }

    /**
     * Returns true if a coordinate on the screen touches a [Layer]. Note: if the supplied
     * layer has no size, this will always return false.
     */
    fun hitTest(layer: Layer, x: Float, y: Float): Boolean {
        val point = screenToLayer(layer, x, y)
        return point.x >= 0 && point.y >= 0 &&
                point.x <= layer.width() && point.y <= layer.height()
    }

    /**
     * Gets the layer underneath the given screen coordinates, ignoring hit testers. This is
     * useful for inspecting the scene graph for debugging purposes, and is not intended for use
     * in shipped code. The layer returned is the one that has a size and is the deepest within
     * the graph and contains the coordinate.
     */
    fun layerUnderPoint(root: Layer, x: Float, y: Float): Layer? {
        val p = Point(x, y)
        root.transform().inverseTransform(p, p)
        p.x += root.originX()
        p.y += root.originY()
        return layerUnderPoint(root, p)
    }

    /**
     * Returns the index of the given layer within its parent, or -1 if the parent is null.
     */
    fun indexInParent(layer: Layer): Int {
        val parent = layer.parent() ?: return -1
        for (ii in parent.children() - 1 downTo 0) {
            if (parent.childAt(ii) === layer) return ii
        }
        throw AssertionError()
    }

    /**
     * Automatically connects `onPaint` to `paint` when `layer` is added to a scene
     * graph, and disconnects it when `layer` is removed.
     */
    fun bind(layer: Layer, paint: Signal<Clock>, onPaint: Slot<Clock>) {
        var _pcon = Closeable.Util.NOOP
        layer.state.connectNotify { state: Layer.State ->
            _pcon = Closeable.Util.close(_pcon)
            if (state === Layer.State.ADDED) _pcon = paint.connect(onPaint)
        }
    }

    /**
     * Automatically connects `onUpdate` to `update`, and `onPaint` to `paint` when `layer` is added to a scene graph, and disconnects them when `layer`
     * is removed.
     */
    fun bind(layer: Layer, update: Signal<Clock>, onUpdate: Slot<Clock>,
             paint: Signal<Clock>, onPaint: Slot<Clock>) {
        var _ucon = Closeable.Util.NOOP
        var _pcon = Closeable.Util.NOOP
        layer.state.connectNotify { state: Layer.State ->
            _pcon = Closeable.Util.close(_pcon)
            _ucon = Closeable.Util.close(_ucon)
            if (state === Layer.State.ADDED) {
                _ucon = update.connect(onUpdate)
                _pcon = paint.connect(onPaint)
            }
        }
    }

    /**
     * Returns the depth of the given layer in its local scene graph. A root layer (one with null
     * parent) will always return 0.
     */
    fun graphDepth(layer: Layer?): Int {
        var layer = layer
        var depth = -1
        while (layer != null) {
            layer = layer!!.parent()
            depth++
        }
        return depth
    }

    /**
     * Prints the layer heirarchy starting at `layer`, using [Log.debug].
     */
    fun print(log: Log, layer: Layer) {
        print(log, layer, "")
    }

    /** Performs the recursion for [.layerUnderPoint].  */
    private fun layerUnderPoint(layer: Layer, pt: Point): Layer? {
        val x = pt.x
        val y = pt.y
        if (layer is GroupLayer) {
            val gl = layer as GroupLayer
            for (ii in gl.children() - 1 downTo 0) {
                val child = gl.childAt(ii)
                if (!child.visible()) continue // ignore invisible children
                try {
                    // transform the point into the child's coordinate system
                    child.transform().inverseTransform(pt.set(x, y), pt)
                    pt.x += child.originX()
                    pt.y += child.originY()
                    val l = layerUnderPoint(child, pt)
                    if (l != null)
                        return l
                } catch (nte: NoninvertibleTransformException) {
                    continue
                }

            }
        }
        if (x >= 0 && x < layer.width() && y >= 0 && y < layer.height()) {
            return layer
        }
        return null
    }

    private fun print(log: Log, layer: Layer, prefix: String) {
        log.debug(prefix + layer)
        if (layer is GroupLayer) {
            val gprefix = prefix + "  "
            val glayer = layer as GroupLayer
            var ii = 0
            val ll = glayer.children()
            while (ii < ll) {
                print(log, glayer.childAt(ii), gprefix)
                ii++
            }
        }
    }
}
