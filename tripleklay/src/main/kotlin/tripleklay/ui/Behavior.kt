package tripleklay.ui

import klay.core.Clock
import klay.core.Sound
import klay.scene.Pointer
import pythagoras.f.Point
import react.Closeable
import react.Signal
import react.Slot
import react.Value

/**
 * Controls the behavior of a widget (how it responds to pointer events).
 */
abstract class Behavior<T : Element<T>>(protected val _owner: T) : Pointer.Listener {

    /** Implements button-like behavior: selects the element when the pointer is in bounds, and
     * deselects on release. This is a pretty common case and inherited by [Click].  */
    open class Select<T : Element<T>>(owner: T) : Behavior<T>(owner) {

        override fun onPress(iact: Pointer.Interaction) {
            updateSelected(true)
        }

        override fun onHover(iact: Pointer.Interaction, inBounds: Boolean) {
            updateSelected(inBounds)
        }

        override fun onRelease(iact: Pointer.Interaction): Boolean {
            // it's a click if we ended in bounds
            return updateSelected(false)
        }

        override fun onCancel(iact: Pointer.Interaction?) {
            updateSelected(false)
        }

        override fun onClick(iact: Pointer.Interaction) {
            // nothing by default, subclasses wire this up as needed
        }
    }

    /** A behavior that ignores everything. This allows subclasses to easily implement a single
     * `onX` method.  */
    open class Ignore<T : Element<T>>(owner: T) : Behavior<T>(owner) {
        override fun onPress(iact: Pointer.Interaction) {}
        override fun onHover(iact: Pointer.Interaction, inBounds: Boolean) {}
        override fun onRelease(iact: Pointer.Interaction): Boolean {
            return false
        }

        override fun onCancel(iact: Pointer.Interaction?) {}
        override fun onClick(iact: Pointer.Interaction) {}
    }

    /** Implements clicking behavior.  */
    open class Click<T : Element<T>>(owner: T) : Select<T>(owner) {

        /** A signal emitted with our owner when clicked.  */
        var clicked = Signal<T>()

        /** Triggers a click.  */
        fun click() {
            soundAction()
            clicked.emit(_owner) // emit a click event
        }

        override fun layout() {
            super.layout()
            _debounceDelay = resolveStyle(DEBOUNCE_DELAY)
        }

        override fun onPress(iact: Pointer.Interaction) {
            // ignore press events if we're still in our debounce interval
            if (iact.event!!.time - _lastClickStamp > _debounceDelay) super.onPress(iact)
        }

        override fun onClick(iact: Pointer.Interaction) {
            _lastClickStamp = iact.event!!.time
            click()
        }

        protected var _debounceDelay: Int = 0
        protected var _lastClickStamp: Double = 0.toDouble()

        companion object {
            /** A delay (in milliseconds) during which the owner will remain unclickable after it has
             * been clicked. This ensures that users don't hammer away at a widget, triggering
             * multiple responses (which code rarely protects against). Inherited.  */
            var DEBOUNCE_DELAY = Style.newStyle(true, 500)
        }
    }

    /** Implements toggling behavior.  */
    open class Toggle<T : Element<T>>(owner: T) : Behavior<T>(owner) {
        /** A signal emitted with our owner when clicked.  */
        val clicked = Signal<T>()

        /** Indicates whether our owner is selected. It may be listened to, and updated.  */
        val selected = Value(false)

        init {
            selected.connect(selectedDidChange())
        }

        /** Triggers a click.  */
        fun click() {
            soundAction()
            clicked.emit(_owner) // emit a click event
        }

        override fun onPress(iact: Pointer.Interaction) {
            _anchorState = _owner.isSelected
            selected.update(!_anchorState)
        }

        override fun onHover(iact: Pointer.Interaction, inBounds: Boolean) {
            selected.update(if (inBounds) !_anchorState else _anchorState)
        }

        override fun onRelease(iact: Pointer.Interaction): Boolean {
            return _anchorState != _owner.isSelected
        }

        override fun onCancel(iact: Pointer.Interaction?) {
            selected.update(_anchorState)
        }

        override fun onClick(iact: Pointer.Interaction) {
            click()
        }

        protected var _anchorState: Boolean = false
    }

    /**
     * Tracks the pressed position as an anchor and delegates to subclasses to update state based
     * on anchor and drag position.
     */
    abstract class Track<T : Element<T>> protected constructor(owner: T) : Ignore<T>(owner) {

        /** Holds the necessary data for the currently active press. `Track` subclasses can
         * derive if more transient information is needed.  */
        inner class State
        /** Creates a new tracking state with the given starting press event.  */
        (iact: Pointer.Interaction) {
            /** Time the press started.  */
            val pressTime: Double = iact.event!!.time

            /** The press and drag positions.  */
            val press: Point = iact.local.copy()
            val drag: Point

            /** How far the pointer strayed from the starting point, squared.  */
            var maxDistanceSq: Float = 0.toFloat()

            init {
                drag = Point(press)
            }

            /** Updates the state to the current event value and called [Track.onTrack].  */
            fun update(iact: Pointer.Interaction) {
                var cancel = false
                toPoint(iact, drag)
                if (_hoverLimit != null) {
                    val lim = _hoverLimit!!
                    val size = _owner.size()
                    cancel = drag.x + lim < 0 || drag.y + lim < 0 ||
                            drag.x - lim >= size.width || drag.y - lim >= size.height
                }
                maxDistanceSq = maxOf(maxDistanceSq, press.distanceSq(drag))
                onTrack(press, if (cancel) press else drag)
            }
        }

        /**
         * Called when the pointer is dragged. After cancel or if the pointer goes outside the
         * hover limit, drag will be equal to anchor.
         * @param anchor the pointer position when initially pressed
         * *
         * @param drag the current pointer position
         */
        protected abstract fun onTrack(anchor: Point, drag: Point)

        /**
         * Creates the state instance for the given press. Subclasses may return an instance
         * of a derived `State` if more information is needed during tracking.
         */
        protected fun createState(press: Pointer.Interaction): State {
            return State(press)
        }

        /**
         * Converts an event to coordinates consumed by [.onTrack]. By default, simply uses
         * the local x, y.
         */
        protected fun toPoint(iact: Pointer.Interaction, dest: Point) {
            dest.set(iact.local)
        }

        override fun onPress(iact: Pointer.Interaction) {
            _state = createState(iact)
        }

        override fun onHover(iact: Pointer.Interaction, inBounds: Boolean) {
            if (_state != null) _state!!.update(iact)
        }

        override fun onRelease(iact: Pointer.Interaction): Boolean {
            _state = null
            return false
        }

        override fun onCancel(iact: Pointer.Interaction?) {
            // track to the press position to cancel
            if (_state != null) onTrack(_state!!.press, _state!!.press)
            _state = null
        }

        override fun layout() {
            super.layout()
            _hoverLimit = resolveStyle(HOVER_LIMIT)
        }

        protected var _state: State? = null
        protected var _hoverLimit: Float? = null

        companion object {
            /** A distance, in event coordinates, used to decide if tracking should be temporarily
             * cancelled. If the pointer is hovered more than this distance outside of the owner's
             * bounds, the tracking will revert to the anchor position, just like when the pointer is
             * cancelled. A null value indicates that the tracking will be unconfined in this way.
             * TODO: default to 35 if no Slider uses are relying on lack of hover limit.  */
            var HOVER_LIMIT: Style<Float?> = Style.newStyle(true, null)
        }
    }

    /** A click behavior that captures the pointer and optionally issues clicks based on some time
     * based function.  */
    abstract class Capturing<T : Element<T>> protected constructor(owner: T) : Click<T>(owner) {

        override fun onPress(iact: Pointer.Interaction) {
            super.onPress(iact)
            iact.capture()
            _conn = _owner.root()!!.iface.frame.connect({ update(it) })
        }

        override fun onRelease(iact: Pointer.Interaction): Boolean {
            super.onRelease(iact)
            cancel()
            return false
        }

        override fun onCancel(iact: Pointer.Interaction?) {
            super.onCancel(iact)
            cancel()
        }

        /** Called on every frame while this behavior is active.  */
        protected abstract fun update(clock: Clock)

        /** Cancels this time-based behavior. Called automatically on release and cancel events.  */
        protected fun cancel() {
            _conn = Closeable.Util.close(_conn)
        }

        protected var _conn = Closeable.Util.NOOP
    }

    /** Captures the pointer and dispatches one click on press, a second after an initial delay
     * and at regular intervals after that.  */
    class RapidFire<T : Element<T>>
    /** Creates a new rapid fire behavior for the given owner.  */
    (owner: T) : Capturing<T>(owner) {

        override fun onPress(iact: Pointer.Interaction) {
            super.onPress(iact)
            _timeInBounds = 0
            click()
        }

        override fun onHover(iact: Pointer.Interaction, inBounds: Boolean) {
            super.onHover(iact, inBounds)
            if (!inBounds)
                _timeInBounds = -1
            else if (_timeInBounds < 0) {
                _timeInBounds = 0
                click()
            }
        }

        override fun update(clock: Clock) {
            if (_timeInBounds < 0) return
            val was = _timeInBounds
            _timeInBounds += clock.dt
            val limit = if (was < _initDelay)
                _initDelay
            else
                _initDelay + _repDelay * ((was - _initDelay) / _repDelay + 1)
            if (limit in (was + 1).._timeInBounds) click()
        }

        override fun layout() {
            super.layout()
            _initDelay = _owner.resolveStyle(INITIAL_DELAY)
            _repDelay = _owner.resolveStyle(REPEAT_DELAY)
        }

        private var _initDelay: Int = 0
        private var _repDelay: Int = 0
        private var _timeInBounds: Int = 0

        companion object {
            /** Milliseconds after the first click that the second click is dispatched.  */
            val INITIAL_DELAY = Style.newStyle(true, 200)

            /** Milliseconds between repeated click dispatches.  */
            val REPEAT_DELAY = Style.newStyle(true, 75)
        }
    }

    override fun onStart(iact: Pointer.Interaction) {
        if (_owner.isEnabled) onPress(iact)
    }

    override fun onDrag(iact: Pointer.Interaction) {
        if (_owner.isEnabled) onHover(iact, _owner.contains(iact.local.x, iact.local.y))
    }

    override fun onEnd(iact: Pointer.Interaction) {
        if (onRelease(iact)) onClick(iact)
    }

    /** Called when our owner is laid out. If the behavior needs to resolve configuration via
     * styles, this is where it should do it.  */
    open fun layout() {
        _actionSound = resolveStyle(Style.ACTION_SOUND)
    }

    /** Emits the action sound for our owner, if one is configured.  */
    fun soundAction() {
        if (_actionSound != null) _actionSound!!.play()
    }

    /** Called when the pointer is pressed down on our element.  */
    abstract fun onPress(iact: Pointer.Interaction)

    /** Called as the user drags the pointer around after pressing. Derived classes map this onto
     * the widget state, such as updating selectedness.  */
    abstract fun onHover(iact: Pointer.Interaction, inBounds: Boolean)

    /** Called when the pointer is released after having been pressed on this widget. This should
     * return true if the gesture is considered a click, in which case [.onClick] will
     * be called automatically.  */
    abstract fun onRelease(iact: Pointer.Interaction): Boolean

    /** Called when the pointer is released and the subclass decides that it is a click, i.e.
     * returns true from [.onRelease].  */
    abstract fun onClick(iact: Pointer.Interaction)

    /** Resolves the value for the supplied style via our owner.  */
    protected fun <V> resolveStyle(style: Style<V>): V {
        return Styles.resolveStyle(_owner, style)
    }

    /** Returns the [Root] to which our owning element is added, or null.  */
    protected fun root(): Root? {
        return _owner.root()
    }

    /** Updates the selected state of our owner, invalidating if selectedness changes.
     * @return true if the owner was selected on entry.
     */
    protected fun updateSelected(selected: Boolean): Boolean {
        val wasSelected = _owner.isSelected
        if (selected != wasSelected) {
            _owner.set(Element.Flag.SELECTED, selected)
            _owner.invalidate()
        }
        return wasSelected
    }

    /** Slot for calling [.updateSelected].  */
    protected fun selectedDidChange(): Slot<Boolean> {
        return { updateSelected(it) }
    }

    protected var _actionSound: Sound? = null
}
