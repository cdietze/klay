package tripleklay.ui

import klay.core.Event
import klay.scene.GroupLayer
import klay.scene.Layer
import klay.scene.LayerUtil
import klay.scene.Pointer
import pythagoras.f.*
import react.Closeable
import react.UnitSlot
import tripleklay.platform.TPPlatform
import tripleklay.ui.Log.log
import tripleklay.ui.util.BoxPoint

/**
 * Provides a context for popping up a menu.
 */
class MenuHost
/**
 * Creates a new menu host using the given values. The stylesheet is set to an empty
 * one and can be changed via the [.setStylesheet] method.
 */
(
        /** The interface we use to create the menu's root and do animation.  */
        val iface: Interface,
        /** The root layer that will contain all menus that pop up. It should normally be close to the
         * top of the hierarchy so that it draws on top of everything.  */
        val rootLayer: GroupLayer) {
    /** Defines how to obtain the point on a trigger where a menu popup originates.  */
    interface TriggerPoint {
        /** For the given trigger and pointer position, gets the screen coordinates where the
         * menu popup should originate.  */
        fun getLocation(trigger: Element<*>, pointer: IPoint?): Point
    }

    /**
     * An event type for triggering a menu popup.
     */
    class Pop
    /** Creates a new event and initializes [.trigger] and [.menu].  */
    constructor(
            /** The element that triggered the popup.  */
            val trigger: Element<*>,
            /** The menu to show.  */
            val menu: Menu, pointer: Event.XY? = null) {

        /** The position of the pointer, if given during construction, otherwise null.  */
        val pointer: IPoint?

        /** The bounds to confine the menu, in screen coordinates; usually the whole screen.  */
        var bounds: IRectangle? = null

        init {
            this.pointer = if (pointer == null) null else Point(pointer)
        }

        /**
         * Causes the menu to handle further events on the given layer. This is usually the layer
         * handling a pointer start that caused the popup. A listener will be added to the layer
         * and the menu notified of pointer drag and end events.
         */
        fun relayEvents(layer: Layer): Pop {
            _relayTarget = layer
            return this
        }

        /**
         * Flags this `Pop` event so that the menu will not be destroyed automatically when
         * it is deactivated. Returns this instance for chaining.
         */
        fun retainMenu(): Pop {
            _retain = true
            return this
        }

        /**
         * Optionally confines the menu area to the given screen area. By default the menu is
         * confined by the host's screen area (see [MenuHost.getScreenArea]).
         */
        fun inScreenArea(area: IRectangle): Pop {
            bounds = Rectangle(area)
            return this
        }

        /**
         * Optionally confines the menu area to the given element. By default the menu is confined
         * by the host's screen area (see [MenuHost.getScreenArea]).
         */
        fun inElement(elem: Element<*>): Pop {
            val tl = LayerUtil.layerToScreen(elem.layer, 0f, 0f)
            val br = LayerUtil.layerToScreen(
                    elem.layer, elem.size().width, elem.size().height)
            bounds = Rectangle(tl.x, tl.y, br.x - tl.x, br.y - tl.y)
            return this
        }

        /**
         * Pops up this instance on the trigger's root's menu host. See [MenuHost.popup].
         * For convenience, returns the host that was used to perform the popup.
         */
        fun popup(): MenuHost? {
            val root = trigger.root()
            if (root == null) {
                Log.log.warning("Menu trigger not on a root?", "trigger", trigger)
                return null
            }
            root.menuHost.popup(this)
            return root.menuHost
        }

        /** Whether we should keep the menu around (i.e. not destroy it).  */
        var _retain: Boolean = false

        /** The layer that will be sending pointer drag and end events to us.  */
        var _relayTarget: Layer? = null
    }
    /** Creates a new event and initializes [.trigger] and [.menu].  */

    /**
     * Creates a menu host using the given values. The root layer is set to the layer of the given
     * root and the stylesheet to its stylesheet.
     */
    constructor(iface: Interface, root: Elements<*>) : this(iface, root.layer) {
        _stylesheet = root.stylesheet()!!
        _screenArea.setSize(iface.plat.graphics.viewSize)
    }

    /**
     * Sets the stylesheet for menus popped by this host.
     */
    fun setStylesheet(sheet: Stylesheet): MenuHost {
        _stylesheet = sheet
        return this
    }

    /**
     * Deactivates the current menu, if any is showing.
     */
    fun deactivate() {
        if (_active != null) {
            _active!!.pop.menu!!.deactivate()
        }
    }

    /**
     * Gets the area to which menus should be confined when there isn't any other associated
     * bounds. By default, the entire available area is used, as given by
     * [playn.core.Graphics].
     */
    /**
     * Sets the area to which menus should be confined when there isn't any other associated
     * bounds.
     */
    var screenArea: IRectangle
        get() = _screenArea
        set(screenArea) = _screenArea.setBounds(screenArea)

    /**
     * Displays the menu specified by the given pop, incorporating all the configured attributes
     * therein.
     */
    fun popup(pop: Pop) {
        // if there is no explicit constraint area requested, use the graphics
        if (pop.bounds == null) pop.inScreenArea(_screenArea)

        // set up the menu root, the RootLayout will do the complicated bounds jockeying
        val menuRoot = iface.addRoot(MenuRoot(iface, _stylesheet, pop))
        rootLayer.add(menuRoot.layer)
        menuRoot.pack()

        // set up the activation
        val activation = Activation(pop)

        // cleanup
        val cleanup: UnitSlot = {
            // check parentage, it's possible the menu has been repopped already
            if (pop.menu!!.parent() === menuRoot) {
                // free the constraint to gc
                pop.menu.setConstraint(null)

                // remove or destroy it, depending on the caller's preference
                if (pop._retain)
                    menuRoot.remove(pop.menu)
                else
                    menuRoot.destroy(pop.menu)

                // remove the hidden area we added
                TPPlatform.instance().hideNativeOverlays(null)
            }

            // clear all connections
            activation.clear()

            // always kill off the transient root
            iface.disposeRoot(menuRoot)

            // if this was our active menu, clear it
            if (_active != null && _active!!.pop === pop) _active = null
        }

        // connect to deactivation signal and do our cleanup
        activation.deactivated = pop.menu.deactivated().connect({
            // due to animations, deactivation can happen during layout, so do it next frame
            iface.frame.connect(cleanup).once()
        })

        // close the menu any time the trigger is removed from the hierarchy
        activation.triggerRemoved = pop.trigger.hierarchyChanged().connect({ event: Boolean ->
            if (!event) pop.menu.deactivate()
        })

        // deactivate the old menu
        if (_active != null) _active!!.pop.menu.deactivate()

        // pass along the animator
        pop.menu.init(iface.anim)

        // activate
        _active = activation
        pop.menu.activate()
    }

    fun activePop(): Pop? {
        return if (_active != null) _active!!.pop else null
    }

    fun active(): Menu? {
        return if (_active != null) _active!!.pop.menu else null
    }

    protected class MenuRoot(iface: Interface, sheet: Stylesheet, val pop: Pop) : Root(iface, RootLayout(iface.plat.input.hasTouch), sheet) {

        init {
            layer.setDepth(1f)
            layer.setHitTester(null) // get hits from out of bounds
            add(pop.menu)
        }
    }

    /** Simple layout for positioning the menu within the transient `Root`.  */
    protected class RootLayout(private val _hasTouch: Boolean) : Layout() {

        override fun computeSize(elems: Container<*>, hintX: Float, hintY: Float): Dimension {
            return Dimension(preferredSize(elems.childAt(0), hintX, hintY))
        }

        override fun layout(elems: Container<*>, left: Float, top: Float,
                            width: Float, height: Float) {
            if (elems.childCount() == 0) return

            val menuRoot = elems as MenuRoot
            val pop = menuRoot.pop

            // get the trigger point from the trigger
            val position = resolveStyle(pop.trigger, TRIGGER_POINT)

            // get the origin point from the menu
            val origin = resolveStyle(pop.trigger, POPUP_ORIGIN)

            // get the desired position, may be relative to trigger or pointer
            val tpos = position.getLocation(pop.trigger, pop.pointer)
            val mpos = origin.resolve(0f, 0f, width, height, Point())

            // figure out the best place to put the menu, in screen coordinates; starting with
            // the requested popup position
            val bounds = Rectangle(tpos.x - mpos.x, tpos.y - mpos.y, width, height)

            // make sure the menu lies inside the requested bounds if the menu doesn't do
            // that itself
            if (pop.menu.automaticallyConfine()) {
                confine(pop.bounds, bounds)

                // fudge is the number of pixels around the menu that we don't need to avoid
                // TODO: can we get the menu's Background's insets?
                val fudge = 2f

                // TODO: do we need any of this finger avoidance stuff if the popup is not
                // relative to the pointer? E.g. a combo box with its menu off to the right

                // keep the bounds from overlapping the position
                if (bounds.width > fudge * 2 && bounds.height > fudge * 2) {
                    val ibounds = Rectangle(bounds)
                    ibounds.grow(-fudge, -fudge)

                    // set up the fingerprint
                    val fingerRadius = (if (_hasTouch) 10 else 3).toFloat()
                    val fingerPos = pop.pointer ?: tpos
                    val fingerBox = Rectangle(
                            fingerPos.x - fingerRadius, fingerPos.y - fingerRadius,
                            fingerRadius * 2, fingerRadius * 2)

                    // try and place the menu so it isn't under the finger
                    if (!avoidPoint(pop.bounds, ibounds, fingerBox)) {
                        log.warning("Oh god, menu doesn't fit", "menu", pop.menu)
                    }
                    bounds.setLocation(ibounds.x - fudge, ibounds.y - fudge)
                }
            }

            // save a copy of bounds in screen coordinates
            val screenBounds = Rectangle(bounds)

            // relocate to layer coordinates
            bounds.setLocation(LayerUtil.screenToLayer(elems.layer, bounds.x, bounds.y))

            // set the menu bounds
            setBounds(elems.childAt(0), bounds.x, bounds.y, bounds.width, bounds.height)

            // check if menu is closed (layout can still occur in this state)
            if (!pop.menu._defunct) {
                // tell the UI overlay to let the real dimensions of the menu through
                // TODO: this looks wrong if the menu has any transparent border - fix
                // by using an image overlay instead, with the root captured onto it
                TPPlatform.instance().hideNativeOverlays(screenBounds)
            }
        }
    }

    /** Holds a few variables related to the menu's activation.  */
    protected class Activation
    /** Creates a new activation.  */
    (
            /** The configuration of the menu.  */
            val pop: Pop) {

        /** Connects to the pointer events from the relay.  */
        var pointerRelay = Closeable.Util.NOOP

        /** Connection to the trigger's hierarchy change.  */
        var triggerRemoved: Closeable? = null

        /** Connection to the menu's deactivation.  */
        var deactivated: Closeable? = null

        init {

            // handle pointer events from the relay
            val target = pop._relayTarget
            if (target != null) pointerRelay = relayEvents(target, pop.menu)
        }

        /** Clears out the connections.  */
        fun clear() {
            if (triggerRemoved != null) triggerRemoved!!.close()
            if (deactivated != null) deactivated!!.close()
            pointerRelay = Closeable.Util.close(pointerRelay)
            triggerRemoved = null
            deactivated = null
        }
    }

    protected class Absolute(x: Float, y: Float) : TriggerPoint {
        val pos: Point

        init {
            pos = Point(x, y)
        }

        override fun getLocation(trigger: Element<*>, pointer: IPoint?): Point {
            return Point(pos)
        }
    }

    /** The stylesheet used for popped menus.  */
    protected var _stylesheet = Stylesheet.builder().create()

    /** Currently active.  */
    protected var _active: Activation? = null

    /** When confining the menu to the graphics' bounds, use this.  */
    protected val _screenArea = Rectangle()

    companion object {

        /** Gets a trigger point relative to an element using the given box point.  */
        fun relative(location: BoxPoint): TriggerPoint {
            return object : TriggerPoint {
                override fun getLocation(trigger: Element<*>, pointer: IPoint?): Point {
                    return location.resolve(trigger, Point())
                }
            }
        }

        /** Gets a fixed trigger point for the given screen coordinates.  */
        fun absolute(x: Float, y: Float): TriggerPoint {
            return Absolute(x, y)
        }

        /** Gets a trigger point exactly under the pointer position.  */
        fun pointer(): TriggerPoint {
            return object : TriggerPoint {
                override fun getLocation(trigger: Element<*>, pointer: IPoint?): Point {
                    return Point(pointer!!)
                }
            }
        }

        /** The point on an element where menus should be placed, subject to boundary constraints.
         * This is only used if the element is set to a [Pop.trigger]. By default, uses the
         * top left corner of the trigger.  */
        val TRIGGER_POINT = Style.newStyle(true, relative(BoxPoint.TL))

        /** The point on the menu that should be placed directly on top of the trigger point, subject
         * to bounding constraints. This is only used if the element is set to a [Pop.trigger].
         * By default, the top, left corner is the origin.  */
        val POPUP_ORIGIN = Style.newStyle(true, BoxPoint.TL)

        fun relayEvents(from: Layer, to: Menu): Closeable {
            return from.events().connect(object : Pointer.Listener {
                override fun onDrag(pi: Pointer.Interaction) {
                    to.onPointerDrag(pi.event)
                }

                override fun onEnd(pi: Pointer.Interaction) {
                    to.onPointerEnd(pi.event)
                }
            })
        }

        /** Tries to place the inner bounds within the outer bounds, such that the inner bounds does
         * not contain the position.  */
        protected fun avoidPoint(outer: IRectangle?, inner: Rectangle, fingerprint: IRectangle): Boolean {
            val checkBounds = Rectangle()
            val best = Rectangle(inner)
            var bestDist = Float.MAX_VALUE
            var edge: Float

            // confine to the left
            edge = fingerprint.x
            checkBounds.setBounds(outer!!.x, outer.y, edge - outer.x, outer.height)
            bestDist = compareAndConfine(checkBounds, inner, best, bestDist)

            // right
            edge = fingerprint.maxX
            checkBounds.setBounds(edge, outer.y, outer.width - edge, outer.height)
            bestDist = compareAndConfine(checkBounds, inner, best, bestDist)

            // top
            edge = fingerprint.y
            checkBounds.setBounds(outer.x, outer.y, outer.width, edge - outer.y)
            bestDist = compareAndConfine(checkBounds, inner, best, bestDist)

            // bottom
            edge = fingerprint.maxY
            checkBounds.setBounds(outer.x, edge, outer.width, outer.height - edge)
            bestDist = compareAndConfine(checkBounds, inner, best, bestDist)

            inner.setBounds(best)
            return bestDist < Float.MAX_VALUE
        }

        /** Confines a rectangle and updates the current best fit based on the moved distance.  */
        protected fun compareAndConfine(
                outer: IRectangle, inner: IRectangle, best: Rectangle, bestDist: Float): Float {
            var bestDist = bestDist

            // don't bother if there isn't even enough space
            if (outer.width <= inner.width || outer.height < inner.height) return bestDist

            // confine
            val confined = confine(outer, Rectangle(inner))

            // check distance and overwrite the best fit if we have a new winner
            val dx = confined.x - inner.x
            val dy = confined.y - inner.y
            val dist = dx * dx + dy * dy
            if (dist < bestDist) {
                best.setBounds(confined)
                bestDist = dist
            }

            return bestDist
        }

        /** Moves ths given inner rectangle such that it lies within the given outer rectangle.
         * The results are undefined if either the inner width or height is greater that the outer's
         * width or height, respectively.  */
        protected fun confine(outer: IRectangle?, inner: Rectangle): Rectangle {
            var dx = outer!!.x - inner.x
            var dy = outer.y - inner.y
            if (dx <= 0) dx = minOf(0f, outer.maxX - inner.maxX)
            if (dy <= 0) dy = minOf(0f, outer.maxY - inner.maxY)
            inner.translate(dx, dy)
            return inner
        }
    }
}
