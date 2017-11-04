package tripleklay.anim

import klay.core.Clock
import klay.scene.Pointer
import pythagoras.f.MathUtil
import react.Signal
import react.Slot
import react.Value
import tripleklay.util.Interpolator
import kotlin.math.abs
import kotlin.math.sign

/**
 * Implements click, and scroll/flick gestures for a single variable (y position by default). When
 * the pointer is pressed and dragged, the scroll position is updated to track the pointer. If the
 * last two pointer events describe a motion of sufficiently high velocity, the scroll position is
 * "flicked" and undergoes (friction decelerated) motion. If the pointer is pressed and released
 * without scrolling or flicking, the [.clicked] signal is emitted.

 *
 * To use: create a flicker, configure it as a pointer listener on a layer that represents your
 * touchable area, then use [Flicker.position] to position your flickable layer (or to offset
 * your hand-drawn flickable elements) on every frame tick. You must also connect [ ][Flicker.onPaint] to a paint signal (to process changes due to acceleration and velocity).

 *
 * Various flick parameters can be customized by overriding the appropriate method:
 * [.friction], [.maxFlickVel], etc.
 */
open class Flicker
/**
 * Creates a flicker with the specified initial, minimum and maximum values.
 */
(
        /** The initial position value.  */
        initialPosition: Float,
        /** This flicker's bounds.  */
        var min: Float, var max: Float) : Pointer.Listener {

    /** The current position value.  */
    var position: Float = initialPosition
        set(value) {
            if (value != this.position) {
                field = value
                changed.emit(this)
            }
        }

    /** A signal emitted when this flicker's position has changed.  */
    var changed = Signal<Flicker>()

    /** A signal that is emitted (with the pointer end event) on click.  */
    var clicked = Signal<Pointer.Interaction>()

    /** Whether or not this flicker is enabled (responding to pointer events). Disabling a flicker
     * does not stop any existing physical behavior, it just prevents the user from introducing any
     * further behavior by flicking or tapping.

     *
     * Note that if a pointer interaction has already started when the flicker is disabled, that
     * interaction will be allowed to complete. Otherwise the flicker would be left in an
     * unpredictable state.  */
    var enabled = Value(true)

    /** This must be connected to a paint signal.  */
    var onPaint: Slot<Clock> = { onPaint(it) }

    /** Connects this flicker to the `paint` signal.  */
    fun connect(paint: Signal<Clock>): Flicker {
        paint.connect(onPaint)
        return this
    }

    /** Returns the position of this flicker as an animation value.  */
    fun posValue(): Animation.Value {
        return object : Animation.Value {
            override fun initial(): Float {
                return position
            }

            override fun set(value: Float) {
                position = value
            }
        }
    }

    /** Stops any active movement of this flicker. The position is immediately clamped back into
     * `min/max` which may be jarring if the flicker was in the middle of a rebound and
     * outside its bounds.  */
    fun stop() {
        setState(STOPPED)
    }

    /** Aborts any active movement of this flicker. Zeroes out velocity and resets state to stopped.
     * This differs from [.stop] in that it does not clamp the flicker's position back into
     * `min/max`, so it should only be used if you plan to subsequently manually adjust the
     * position to reflect valid values. Otherwise you may freeze the flicker while it's in a
     * rebound state and not inside its normal bounds.  */
    fun freeze() {
        // don't use setState as we don't want STOPPED to clamp our position into min/max
        _vel = 0f
        _state = STOPPED
    }

    override fun onStart(iact: Pointer.Interaction) {
        if (!enabled.get()) return

        _vel = 0f
        _maxDelta = 0f
        _minFlickExceeded = false
        _origPos = position
        _cur = getPosition(iact.event!!)
        _prev = _cur
        _start = _prev
        _prevStamp = 0.0
        _curStamp = iact.event!!.time
        setState(DRAGGING)
    }

    override fun onDrag(iact: Pointer.Interaction) {
        // check whether we are processing this interaction
        if (_state !== DRAGGING) return

        _prev = _cur
        _prevStamp = _curStamp
        _cur = getPosition(iact.event!!)
        _curStamp = iact.event!!.time

        // update our position based on the drag delta
        val delta = _cur - _start
        position = (_origPos + delta)

        // if we're not allowed to rebound, clamp the position to our bounds
        if (!allowRebound())
            position = (MathUtil.clamp(position, min, max))
        else if (position < min)
            position += (min - position) * overFraction()
        else if (position > max) position -= (position - max) * overFraction()// otherwise if we're exceeding min/max then only use a fraction of the delta

        val absDelta = abs(delta)
        if (!_minFlickExceeded && absDelta > minFlickDelta()) {
            _minFlickExceeded = true
            minFlickExceeded(iact)
        }
        _maxDelta = maxOf(absDelta, _maxDelta)
    }

    override fun onEnd(iact: Pointer.Interaction) {
        // check whether we are processing this interaction
        if (_state !== DRAGGING) return

        // check whether we should call onClick
        if (_maxDelta < maxClickDelta()) {
            clicked.emit(iact)
            setState(STOPPED)
        } else {
            val dragTime = (_curStamp - _prevStamp).toFloat()
            val delta = _cur - _prev
            val signum = delta.sign
            val dragVel = abs(delta) / dragTime
            // if we're outside our bounds, go immediately into easeback mode
            if (position < min || position > max)
                setState(EASEBACK)
            else if (dragVel > flickVelThresh() && _minFlickExceeded) {
                _vel = signum * minOf(maxFlickVel(), dragVel * flickXfer())
                _accel = -signum * friction()
                setState(SCROLLING)
            } else
                setState(STOPPED)// otherwise potentially initiate a flick
        }// if not, determine whether we should impart velocity to the tower
    }

    protected fun onPaint(clock: Clock) {
        val dt = clock.dt
        // update our position based on our velocity
        if (_vel != 0f) position = (position + _vel * dt)
        // let our state handle additional updates
        _state.paint(dt.toFloat())
    }

    /**
     * Extracts the desired position from the pointer event. The default is to use the y-position.
     */
    protected fun getPosition(event: klay.core.Pointer.Event): Float {
        return event.y
    }

    /**
     * Returns the deceleration (in pixels per ms per ms) applied to non-zero velocity.
     */
    protected open fun friction(): Float {
        return 0.0015f
    }

    /**
     * Returns the minimum (positive) velocity (in pixels per millisecond) at time of touch release
     * required to initiate a flick (i.e. transfer the flick velocity to the entity).
     */
    protected fun flickVelThresh(): Float {
        return 0.5f
    }

    /**
     * Returns the fraction of flick velocity that is transfered to the entity (a value between 0
     * and 1).
     */
    protected fun flickXfer(): Float {
        return 0.9f
    }

    /**
     * Returns the maximum flick velocity that will be transfered to the entity; limits the actual
     * flick velocity at time of release. This value is not adjusted by [.flickXfer].
     */
    protected fun maxFlickVel(): Float {
        return 1.4f // pixels/ms
    }

    /**
     * Returns the maximum distance (in pixels) the pointer is allowed to travel while pressed and
     * still register as a click.
     */
    protected fun maxClickDelta(): Float {
        return 5f
    }

    /**
     * Returns the minimum distance (in pixels) the pointer must have moved to register as a flick.
     */
    protected fun minFlickDelta(): Float {
        return 10f
    }

    /**
     * A method called as soon as the minimum flick distance is exceeded.
     * @param iact the pointer interaction being processed at the time we detected this state.
     */
    protected fun minFlickExceeded(iact: Pointer.Interaction) {
        iact.capture() // capture the interaction by default
    }

    /**
     * Determines whether or not the flicker is allowed to scroll past its limits and rebound in a
     * bouncily physical manner (ala iOS). If this is enabled, the flicker position may be
     * temporarily less than [.min] or greater than [.max] while it is rebounding. The
     * user will also be allowed to drag the flicker past the edge up to [.overFraction]
     * times the height of the screen.
     */
    protected fun allowRebound(): Boolean {
        return true
    }

    /**
     * The fraction of the drag distance to use when we've dragged beyond our minimum or maximum
     * value. The default value is `0.5` which seems to be what most inertial scrolling code
     * uses, but you can use an even smaller fraction if you don't want the user to expose so much
     * of your "off-screen" area. If rebounding is disabled, this value is not used and dragging
     * beyond the edges is disallowed.
     */
    protected fun overFraction(): Float {
        return 0.5f
    }

    /**
     * The duration (in milliseconds) over which to animate our ease back to the edge.
     */
    protected fun easebackTime(): Float {
        return 500f
    }

    /**
     * Controls the tightness of the deceleration when we're decelerating after scrolling beyond
     * our minimum or maximum value. Default is 5, smaller values result in tighter snapback.
     */
    protected fun decelerateSnap(): Float {
        return 5f
    }

    protected fun setState(state: State) {
        _state = state
        state.becameActive()
    }

    protected abstract inner class State {
        open fun becameActive() {}
        open fun paint(dt: Float) {}
    }

    protected val DRAGGING: State = object : State() {

        // nada
    }

    protected val SCROLLING: State = object : State() {
        override fun paint(dt: Float) {
            // update our velocity based on the current (friction) acceleration
            val prevVel = _vel
            _vel += _accel * dt

            // if we decelerate to (or rather slightly through) zero velocity, stop
            if (prevVel.sign != _vel.sign)
                setState(STOPPED)
            else if (position < min || position > max)
                setState(
                        if (allowRebound()) DECELERATE else STOPPED)// otherwise, if we move past the edge of our bounds, either stop if rebound is
            // disallowed or go into decelerate mode if rebound is allowed
        }

        override fun toString(): String {
            return "SCROLLING"
        }
    }

    protected val DECELERATE: State = object : State() {
        override fun paint(dt: Float) {
            // update our acceleration based on the pixel distance back to the edge
            val retpix = if (position < min) min - position else max - position
            _accel = retpix / (1000 * decelerateSnap())

            // now update our velocity based on this one
            val prevVel = _vel
            _vel += _accel * dt

            // once we decelerate to zero, switch to snapback mode
            if (prevVel.sign != _vel.sign) setState(SNAPBACK)
        }

        override fun toString(): String {
            return "DECELERATE"
        }
    }

    protected val SNAPBACK: State = object : State() {
        override fun becameActive() {
            _vel = 0f
            _snapdist = if (position < min) min - position else max - position
        }

        override fun paint(dt: Float) {
            // if we're in the first 30% of the snapback, accelerate, otherwise switch to easeback
            val retpix = if (position < min) min - position else max - position
            val retpct = retpix / _snapdist
            if (retpct > 0.7f)
                _vel += _accel * dt
            else
                setState(EASEBACK)
        }

        override fun toString(): String {
            return "SNAPBACK"
        }

        protected var _snapdist: Float = 0.toFloat()
    }

    protected val EASEBACK: State = object : State() {
        override fun becameActive() {
            _vel = 0f // we animate based on timestamps now
            _spos = position
            _delta = 0f
            _time = easebackTime()
        }

        override fun paint(dt: Float) {
            // from here we just interpolate to our final position
            _delta += dt
            val target = if (position <= min) min else max
            if (_delta > _time) {
                this@Flicker.position = target
                setState(STOPPED)
            } else {
                this@Flicker.position = Interpolator.EASE_OUT.apply(_spos, target - _spos, _delta, _time)
            }
        }

        override fun toString(): String {
            return "EASEBACK"
        }

        protected var _time: Float = 0.toFloat()
        protected var _spos: Float = 0.toFloat()
        protected var _delta: Float = 0.toFloat()
    }

    protected val STOPPED: State = object : State() {
        override fun becameActive() {
            position = MathUtil.clamp(this@Flicker.position, min, max)
            _vel = 0f
        }

        override fun toString(): String {
            return "STOPPED"
        }
    }

    protected var _state = STOPPED
    protected var _vel: Float = 0f
    protected var _accel: Float = 0f
    protected var _origPos: Float = 0f
    protected var _start: Float = 0f
    protected var _cur: Float = 0f
    protected var _prev: Float = 0f
    protected var _curStamp: Double = 0.0
    protected var _prevStamp: Double = 0.0
    protected var _maxDelta: Float = 0f
    protected var _minFlickExceeded: Boolean = false
}
