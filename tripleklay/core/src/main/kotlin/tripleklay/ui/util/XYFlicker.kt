package tripleklay.ui.util

import klay.scene.Pointer
import pythagoras.f.IPoint
import pythagoras.f.MathUtil
import pythagoras.f.Point
import react.Signal

/**
 * Translates pointer input on a layer into an x, y offset. With a sufficiently large drag delta,
 * calculates a velocity, applies it to the position over time and diminishes its value by
 * friction. For smaller drag deltas, dispatches the pointer end event on the [.clicked]
 * signal.

 *
 * **NOTE:**Clients of this class must call [.update], so that friction and
 * other calculations can be applied. This is normally done within the client's own update method
 * and followed by some usage of the [.position] method. For example:

 * <pre>`XYFlicker flicker = new XYFlicker();
 * Layer layer = ...;
 * { layer.addListener(flicker); }
 * void update (int delta) {
 * flicker.update(delta);
 * layer.setTranslation(flicker.position().x(), flicker.position().y());
 * }
`</pre> *

 * TODO: figure out how to implement with two Flickers. could require some changes therein since
 * you probably don't want them to have differing states, plus 2x clicked signals is wasteful
 */
class XYFlicker : Pointer.Listener() {
    /** Signal dispatched when a pointer usage did not end up being a flick.  */
    var clicked = Signal.create()

    /**
     * Gets the current position.
     */
    fun position(): IPoint {
        return _position
    }

    override fun onStart(iact: Pointer.Interaction) {
        _vel[0f] = 0f
        _maxDeltaSq = 0f
        _origPos.set(_position)
        getPosition(iact.event, _start)
        _prev.set(_start)
        _cur.set(_start)
        _prevStamp = 0.0
        _curStamp = iact.event!!.time
    }

    override fun onDrag(iact: Pointer.Interaction) {
        _prev.set(_cur)
        _prevStamp = _curStamp
        getPosition(iact.event, _cur)
        _curStamp = iact.event!!.time
        var dx = _cur.x - _start.x
        var dy = _cur.y - _start.y
        setPosition(_origPos.x + dx, _origPos.y + dy)
        _maxDeltaSq = Math.max(dx * dx + dy * dy, _maxDeltaSq)

        // for the purposes of capturing the event stream, dx and dy are capped by their ranges
        dx = _position.x - _origPos.x
        dy = _position.y - _origPos.y
        if (dx * dx + dy * dy >= maxClickDeltaSq()) iact.capture()
    }

    override fun onEnd(iact: Pointer.Interaction) {
        // just dispatch a click if the pointer didn't move very far
        if (_maxDeltaSq < maxClickDeltaSq()) {
            clicked.emit(iact.event)
            return
        }
        // if not, maybe impart some velocity
        val dragTime = (_curStamp - _prevStamp).toFloat()
        val delta = Point(_cur.x - _prev.x, _cur.y - _prev.y)
        val dragVel = delta.mult(1 / dragTime)
        var dragSpeed = dragVel.distance(0f, 0f)
        if (dragSpeed > flickSpeedThresh() && delta.distance(0f, 0f) > minFlickDelta()) {
            if (dragSpeed > maxFlickSpeed()) {
                dragVel.multLocal(maxFlickSpeed() / dragSpeed)
                dragSpeed = maxFlickSpeed()
            }
            _vel.set(dragVel)
            _vel.multLocal(flickXfer())
            val sx = Math.signum(_vel.x)
            val sy = Math.signum(_vel.y)
            _accel.x = -sx * friction()
            _accel.y = -sy * friction()
        }
    }

    override fun onCancel(iact: Pointer.Interaction) {
        _vel[0f] = 0f
        _accel[0f] = 0f
    }

    fun update(delta: Float) {
        if (_vel.x == 0f && _vel.y == 0f) return

        _prev.set(_position)

        // apply x and y velocity
        val x = MathUtil.clamp(_position.x + _vel.x * delta, _min.x, _max.x)
        val y = MathUtil.clamp(_position.y + _vel.y * delta, _min.y, _max.y)

        // stop when we hit the edges
        if (x == _position.x) _vel.x = 0f
        if (y == _position.y) _vel.y = 0f
        _position[x] = y

        // apply x and y acceleration
        _vel.x = applyAccelertion(_vel.x, _accel.x, delta)
        _vel.y = applyAccelertion(_vel.y, _accel.y, delta)
    }

    /**
     * Resets the flicker to the given maximum values.
     */
    fun reset(maxX: Float, maxY: Float) {
        _max[maxX] = maxY

        // reclamp the position
        setPosition(_position.x, _position.y)
    }

    /**
     * Sets the flicker position, in the case of a programmatic change.
     */
    fun positionChanged(x: Float, y: Float) {
        setPosition(x, y)
    }

    /** Translates a pointer event into a position.  */
    protected fun getPosition(event: Pointer.Event, dest: Point) {
        dest[-event.x()] = -event.y()
    }

    /** Sets the current position, clamping the values between min and max.  */
    protected fun setPosition(x: Float, y: Float) {
        _position[MathUtil.clamp(x, _min.x, _max.x)] = MathUtil.clamp(y, _min.y, _max.y)
    }

    /** Returns the minimum distance (in pixels) the pointer must have moved to register as a
     * flick.  */
    protected fun minFlickDelta(): Float {
        return 10f
    }

    /** Returns the deceleration (in pixels per ms per ms) applied to non-zero velocity.  */
    protected fun friction(): Float {
        return 0.0015f
    }

    /** Returns the minimum (positive) speed (in pixels per millisecond) at time of touch release
     * required to initiate a flick (i.e. transfer the flick velocity to the entity).  */
    protected fun flickSpeedThresh(): Float {
        return 0.5f
    }

    /** Returns the fraction of flick speed that is transfered to the entity (a value between 0
     * and 1).  */
    protected fun flickXfer(): Float {
        return 0.95f
    }

    /** Returns the maximum flick speed that will be transfered to the entity; limits the actual
     * flick speed at time of release. This value is not adjusted by [.flickXfer].  */
    protected fun maxFlickSpeed(): Float {
        return 1.4f // pixels/ms
    }

    /** Returns the square of the maximum distance (in pixels) the pointer is allowed to travel
     * while pressed and still register as a click.  */
    protected fun maxClickDeltaSq(): Float {
        return 225f
    }

    protected var _maxDeltaSq: Float = 0.toFloat()
    protected val _position = Point()
    protected val _vel = Point()
    protected val _accel = Point()
    protected val _origPos = Point()
    protected val _start = Point()
    protected val _cur = Point()
    protected val _prev = Point()
    protected val _max = Point()
    protected val _min = Point()
    protected var _prevStamp: Double = 0.toDouble()
    protected var _curStamp: Double = 0.toDouble()

    companion object {

        protected fun applyAccelertion(v: Float, a: Float, dt: Float): Float {
            var v = v
            val prev = v
            v += a * dt
            // if we decelerate past zero velocity, stop
            return if (Math.signum(prev) == Math.signum(v)) v else 0
        }
    }
}
