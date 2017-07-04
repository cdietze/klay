package tripleklay.ui

import klay.core.Event
import klay.scene.Layer
import klay.scene.LayerUtil
import klay.scene.Mouse
import klay.scene.Pointer
import pythagoras.f.Point
import react.Closeable
import react.Signal
import react.SignalView
import react.Slot
import tripleklay.anim.Animation
import tripleklay.anim.Animator
import tripleklay.ui.MenuItem.ShowText
import java.util.*

/**
 * Holds a collection of [MenuItem]s, dispatching a [Menu.itemTriggered] signal
 * when one is selected and triggered. Normally used in conjunction with [MenuHost] to popup
 * the menu (in its own `Root`), manage animations, track user input, and handle
 * cancellation.
 *
 *
 * Note that a menu can contain arbitrary `Element`s, but only those that are `MenuItem`s are eligible for triggering. Changes to the children of previously added [ ] instances are tracked using [Elements.childAdded] and [ ][Elements.childRemoved].
 *
 *
 * Note about [Container] types other than `Elements`: it is assumed that the
 * children of such containers will NOT change after addition to the menu. Such changes will result
 * in undefined behavior, potentially including memory leaks. [Scroller], for example, is
 * safe to use since it has exactly one child element that doesn't change.
 *
 * TODO: support escape key to cancel; probably in MenuHost
 * TODO: support/implement full screen menus - this is probably what most phone apps will want
 */
open class Menu
/**
 * Creates a new menu using the given layout for its elements.
 */
(layout: Layout) : Elements<Menu>(layout) {
    /**
     * Produces an animation for a menu.
     */
    interface AnimFn {
        /**
         * For the given menu and animator, adds an animation to the given animator and returns it.
         * TODO: how can more than one animation be supported? seems Animation should have a join()
         * method in addition to then()
         */
        fun go(menu: Menu, animator: Animator): Animation
    }

    protected val _descendantAdded: Slot<Element<*>> = object : DescendingSlot() {
        override fun visitElems(elems: Elements<*>) {
            elems.childAdded().connect(this)
            elems.childRemoved().connect(_descendantRemoved)
        }

        override fun visitItem(item: MenuItem) {
            connectItem(item)
        }
    }

    protected val _descendantRemoved: Slot<Element<*>> = object : DescendingSlot() {
        override fun visitElems(elems: Elements<*>) {
            elems.childAdded().disconnect(_descendantAdded)
            elems.childRemoved().disconnect(this)
        }

        override fun visitItem(item: MenuItem) {
            disconnectItem(item)
        }
    }

    init {

        // use a hit tester "eater" to pretend our layer covers all its siblings
        layer.setHitTester(object : Layer.HitTester {
            override fun hitTest(layer: Layer, p: Point): Layer? {
                val descendant = layer.hitTestDefault(p)
                return descendant ?: if (absorbHits()) layer else null
            }
        })

        // deactivate the menu on any pointer events (children will still get theirs)
        layer.events().connect(object : Pointer.Listener {
            override fun onStart(iact: Pointer.Interaction) {
                if (iact.hitLayer === layer) deactivate()
            }
        })

        childAdded().connect(_descendantAdded)
        childRemoved().connect(_descendantRemoved)
    }

    /**
     * Creates a new menu using the given layout and styles.
     */
    constructor(layout: Layout, styles: Styles) : this(layout) {
        setStyles(styles)
    }

    /**
     * Creates a new menu using the given layout and style bindings.
     */
    constructor(layout: Layout, vararg styles: Style.Binding<*>) : this(layout) {
        setStyles(*styles)
    }

    /**
     * Gets the signal that is dispatched when the menu is closed and no longer usable. This
     * occurs if an item is triggered or if the menu is manually cancelled (using
     * [.deactivate]).
     */
    fun deactivated(): SignalView<Menu> {
        return _deactivated
    }

    /**
     * Opens this menu, using an animation created by the resolved [.OPENER] style. Once the
     * animation is finished, the user can view the `MenuItem` choices. When one is selected
     * and dispatched via the [.itemTriggered] signal, the menu is deactivated automatically.
     */
    fun activate() {
        // already active, nothing to do
        if (_active) return

        // Undefunct
        _defunct = false

        val doActivation = {
            // skip to the end!
            fastForward()

            // animate the menu opening
            _complete = { onOpened() }
            _anim = open().then().action(_complete!!).handle()
        }

        // postpone the activation if we need validation
        if (isSet(Element.Flag.VALID))
            doActivation()
        else
            _postLayout = doActivation
    }

    /**
     * Closes this menu, using an animation created by the resolved [.CLOSER] style. This is
     * normally called automatically when the user clicks off the menu or triggers one of its
     * `MenuItem`s. After the animation is complete, the [.deactivated] signal will
     * be dispatched.
     */
    fun deactivate() {
        // skip to the end!
        fastForward()

        // disable input and animate closure
        _active = false
        _defunct = true
        _complete = { onClosed() }
        _anim = close().then().action(_complete!!).handle()
    }

    /**
     * Gets the signal that is dispatched when a menu item is selected.
     */
    fun itemTriggered(): SignalView<MenuItem> {
        return _itemTriggered
    }

    /** Tests if this menu's position should be adjusted by the host such that the menu's bounds
     * lies within the requested area.  */
    fun automaticallyConfine(): Boolean {
        return true
    }

    /** Tests if this menu should trap all positional events.  */
    protected fun absorbHits(): Boolean {
        return true
    }

    override val styleClass: Class<*>
        get() = Menu::class.java

    override fun layout() {
        super.layout()

        // and now activate if it was previously requested and we weren't yet valid
        if (_postLayout != null) {
            _postLayout!!()
            _postLayout = null
        }
    }

    /** Creates an animation to move the menu's layer (and its children) into the open state.
     * By default, simply resolves the [.OPENER] style and calls [AnimFn.go].
     * Subclasses can hook in here if needed.  */
    protected fun open(): Animation {
        return resolveStyle(OPENER).go(this, _animator!!)
    }

    /** Creates an animation to move the menu's layer (and its children) into the open state.
     * By default, simply resolves the [.CLOSER] style and calls [AnimFn.go].
     * Subclasses can hook in here if needed.  */
    protected fun close(): Animation {
        return resolveStyle(CLOSER).go(this, _animator!!)
    }

    /** Called when the animation to open the menu is complete or fast forwarded.  */
    protected fun onOpened() {
        clearAnim()
        _active = true
        val pd = _pendingDrag
        val pe = _pendingEnd
        _pendingEnd = null
        _pendingDrag = _pendingEnd
        if (pe != null)
            onPointerEnd(pe)
        else if (pd != null) onPointerDrag(pd)
    }

    /** Called when the animation to close the menu is complete or fast forwarded.  */
    protected fun onClosed() {
        clearAnim()
        _deactivated.emit(this)
        _selector.selected.update(null)
    }

    /** Runs the animation completion action and cancels the animation.  */
    protected fun fastForward() {
        if (_anim != null) {
            // cancel the animation
            _anim!!.cancel()
            // run our complete logic manually (this will always invoke clearAnim too)
            _complete!!()
            assert(_anim == null && _complete == null)
        }
    }

    /** Clears out members used during animation.  */
    protected fun clearAnim() {
        _complete = null
        _anim = null
    }

    /** Connects up the menu item. This gets called when any descendant is added that is an
     * instance of MenuItem.  */
    protected open fun connectItem(item: MenuItem) {
        _items.add(item)
        item.setRelay(Closeable.Util.join(
                item.layer.events().connect(_itemListener.pointer),
                item.layer.events().connect(_itemListener.mouse)))
    }

    /** Disconnects the menu item. This gets called when any descendant is removed that is an
     * instance of MenuItem.  */
    protected fun disconnectItem(item: MenuItem) {
        val itemIdx = _items.indexOf(item)
        _items.removeAt(itemIdx)
        item.setRelay(Closeable.Util.NOOP)
        didDisconnectItem(item, itemIdx)
    }

    /** Notifes subclasses of item removal, in case they want to know the index.  */
    protected open fun didDisconnectItem(item: MenuItem, itemIdx: Int) {}

    /** Called by the host when the pointer is dragged.  */
    fun onPointerDrag(e: klay.core.Pointer.Event?) {
        if (!_active) {
            _pendingDrag = e
            return
        }

        _selector.selected.update(getHover(e))
    }

    /** Called by the host when the pointer is lifted.  */
    fun onPointerEnd(e: klay.core.Pointer.Event?) {
        if (!_active) {
            _pendingEnd = e
            return
        }

        val hover = getHover(e)
        val selected = _selector.selected.get()
        _selector.selected.update(hover)
        if (hover == null) return

        // trigger if this is the 2nd click -or- we always show text
        if (hover == selected || hover._showText === ShowText.ALWAYS) {
            if (isVisible && hover.isEnabled) {
                hover.trigger()
                _itemTriggered.emit(hover)
                deactivate()
            }
        }
    }

    /** Gets the item underneath the given event.  */
    protected fun getHover(e: Event.XY?): MenuItem? {
        // manual hit detection
        val hit = layer.hitTest(LayerUtil.screenToLayer(layer, e!!.x, e.y))

        for (item in _items) {
            if (item.isVisible && item.layer === hit) {
                return item
            }
        }

        return null
    }

    /** Called by the host when the menu is popped.  */
    fun init(animator: Animator) {
        _animator = animator
    }

    protected abstract inner class DescendingSlot : Slot<Element<*>> {
        override fun invoke(elem: Element<*>) {
            if (elem is Container<*>) {
                for (child in elem) invoke(child)
                if (elem is Elements<*>) visitElems(elem)
            } else if (elem is MenuItem) {
                visitItem(elem)
            }
        }

        protected abstract fun visitElems(elems: Elements<*>)
        protected abstract fun visitItem(item: MenuItem)
    }

    protected inner class ItemListener {
        var mouse: Mouse.Listener = object : Mouse.Listener {
            override fun onHover(event: Mouse.HoverEvent, iact: Mouse.Interaction) {
                if (_active) _selector.selected.update(if (event.inside) getHover(event) else null)
            }
        }
        var pointer: Pointer.Listener = object : Pointer.Listener {
            override fun onStart(iact: Pointer.Interaction) {
                this@Menu.onPointerDrag(iact.event)
            }

            override fun onDrag(iact: Pointer.Interaction) {
                this@Menu.onPointerDrag(iact.event)
            }

            override fun onEnd(iact: Pointer.Interaction) {
                this@Menu.onPointerEnd(iact.event)
            }
        }
    }

    protected var _itemListener = ItemListener()

    /** Dispatched when the menu is deactivated.  */
    protected val _deactivated = Signal<Menu>()

    /** Dispatched when an item in the menu is triggered.  */
    protected val _itemTriggered = Signal<MenuItem>()

    /** Tracks the currently selected menu item (prior to triggering, an item is selected).  */
    protected val _selector = Selector()

    protected val _items: MutableList<MenuItem> = ArrayList()

    /** Animator that runs the menu opening and closing states, usually from Interface.  */
    protected var _animator: Animator? = null

    /** Handle to the current open or close animation, or null if no animation is active.  */
    protected var _anim: Animation.Handle? = null

    /** Stash of the last Animation.Action in case we need to cancel it. For example, if the
     * menu is deactivated before it finished opening.  */
    protected var _complete: (() -> Unit)? = null

    /** Method to execute after layout, used to activate the menu.  */
    protected var _postLayout: (() -> Unit)? = null

    /** Whether the menu is ready for user input.  */
    protected var _active: Boolean = false

    /** Whether the menu is closed.  */
    var _defunct: Boolean = false

    /** Input events that may have occurred prior to the menu being ready.  */
    protected var _pendingDrag: klay.core.Pointer.Event? = null
    protected var _pendingEnd: klay.core.Pointer.Event? = null

    companion object {

        /** Generic animation to fade in a menu using the layer alpha.  */
        var FADE_IN: AnimFn = object : AnimFn {
            override fun go(menu: Menu, animator: Animator): Animation {
                menu.layer.setAlpha(0f)
                return animator.tweenAlpha(menu.layer).to(1f).easeIn().`in`(200f)
            }
        }

        /** Generic animation to fade out a menu using the layer alpha.  */
        var FADE_OUT: AnimFn = object : AnimFn {
            override fun go(menu: Menu, animator: Animator): Animation {
                return animator.tweenAlpha(menu.layer).to(0f).easeIn().`in`(40f)
            }
        }

        /** The opening animation function for the menu.  */
        var OPENER = Style.newStyle(true, FADE_IN)

        /** The closing animation function for the menu.  */
        var CLOSER = Style.newStyle(true, FADE_OUT)
    }
}
