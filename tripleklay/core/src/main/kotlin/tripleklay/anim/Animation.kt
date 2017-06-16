package tripleklay.anim

import klay.scene.ImageLayer
import klay.scene.Layer
import pythagoras.f.XY
import tripleklay.util.Interpolator
import java.util.*

/**
 * Represents a single component of an animation.
 */
abstract class Animation protected constructor() {
    /** Used by animations to update a target value.  */
    interface Value {
        /** Returns the initial value.  */
        fun initial(): Float

        /** Updates the value.  */
        fun set(value: Float)
    }

    /** Used by animations to update a target value.  */
    interface XYValue {
        /** Returns the initial x value.  */
        fun initialX(): Float

        /** Returns the initial y value.  */
        fun initialY(): Float

        /** Updates the x/y value.  */
        operator fun set(x: Float, y: Float)
    }

    /** Used to cancel animations after they've been started. See [.handle]. *Note:*
     * you cannot mix and match [.cancel] and [.complete]. Use one or the other to
     * achieve early termination of your animations.  */
    interface Handle {
        /** Cancels this animation. It will remove itself from its animator the next frame.  */
        fun cancel()

        /** Completes the animation chain by adjusting all animations to their final state and then
         * canceling them. This method has no effect on animations that have already finished.  */
        fun complete()
    }

    /** Processes a [Flipbook].  */
    class Flip(protected val _target: ImageLayer, protected val _book: Flipbook) : Animation() {

        override fun init(time: Float) {
            super.init(time)
            setFrame(0)
        }

        override fun apply(time: Float): Float {
            val dt = time - _start
            var newIdx = _curIdx
            val frameEnds = _book.frameEnds
            val remain = frameEnds[frameEnds.size - 1] - dt
            if (remain < 0) return remain
            while (frameEnds[newIdx] < dt) newIdx++
            if (newIdx != _curIdx) setFrame(newIdx)
            return remain
        }

        override fun makeComplete() {
            setFrame(_book.frameIndexes.size - 1)
        }

        protected fun setFrame(idx: Int) {
            _book.frames.apply(_book.frameIndexes[idx], _target)
            _curIdx = idx
        }

        protected var _curIdx: Int = 0
    }

    /** A base class for animations that interpolate values.  */
    abstract class Interped<R> : Animation() {
        /** Uses the supplied interpolator for this animation.  */
        fun using(interp: Interpolator): R {
            _interp = interp
            val tthis = this as R
            return tthis
        }

        /** Uses a linear interpolator for this animation.  */
        fun linear(): R {
            return using(Interpolator.LINEAR)
        }

        /** Uses an ease-in interpolator for this animation.  */
        fun easeIn(): R {
            return using(Interpolator.EASE_IN)
        }

        /** Uses an ease-out interpolator for this animation.  */
        fun easeOut(): R {
            return using(Interpolator.EASE_OUT)
        }

        /** Uses an ease-inout interpolator for this animation.  */
        fun easeInOut(): R {
            return using(Interpolator.EASE_INOUT)
        }

        /** Uses an ease-in-back interpolator for this animation.  */
        fun easeInBack(): R {
            return using(Interpolator.EASE_IN_BACK)
        }

        /** Uses an ease-out-back interpolator for this animation.  */
        fun easeOutBack(): R {
            return using(Interpolator.EASE_OUT_BACK)
        }

        /** Uses an ease-out-back interpolator for this animation.  */
        fun bounceOut(): R {
            return using(Interpolator.BOUNCE_OUT)
        }

        /** Uses an ease-out-elastic interpolator for this animation.  */
        fun easeOutElastic(): R {
            return using(Interpolator.EASE_OUT_ELASTIC)
        }

        /** Configures the duration for this animation (in milliseconds). Default: 1000.  */
        fun `in`(duration: Float): R {
            _duration = duration
            val tthis = this as R
            return tthis
        }

        protected var _interp = Interpolator.LINEAR
        protected var _duration = 1000f
    }

    /** Animates a single scalar value.  */
    class One(protected val _target: Value) : Interped<One>() {

        /** Configures the starting value. Default: the value of the scalar at the time that the
         * animation begins.  */
        fun from(value: Float): One {
            _from = value
            return this
        }

        /** Configures the ending value. Default: 0.  */
        fun to(value: Float): One {
            _to = value
            return this
        }

        override fun init(time: Float) {
            super.init(time)
            if (_from == java.lang.Float.MIN_VALUE) _from = _target.initial()
        }

        override fun apply(time: Float): Float {
            val dt = time - _start
            _target.set(if (dt < _duration) _interp.apply(_from, _to - _from, dt, _duration) else _to)
            return _duration - dt
        }

        override fun makeComplete() {
            _target.set(_to)
        }

        override fun toString(): String {
            return javaClass.name + " start:" + _start + " to " + _to
        }

        protected var _from = java.lang.Float.MIN_VALUE
        protected var _to: Float = 0.toFloat()
    }

    /** Animates a pair of scalar values (usually a position).  */
    class Two(protected val _value: XYValue) : Interped<Two>() {

        /** Configures the starting values. Default: the values of the scalar at the time that the
         * animation begins.  */
        fun from(fromx: Float, fromy: Float): Two {
            _fromx = fromx
            _fromy = fromy
            return this
        }

        /** Configures the starting values. Default: the values of the scalar at the time that the
         * animation begins.  */
        fun from(pos: XY): Two {
            return from(pos.x, pos.y)
        }

        /** Configures the ending values. Default: (0, 0).  */
        fun to(tox: Float, toy: Float): Two {
            _tox = tox
            _toy = toy
            return this
        }

        /** Configures the ending values. Default: (0, 0).  */
        fun to(pos: XY): Two {
            return to(pos.x, pos.y)
        }

        override fun init(time: Float) {
            super.init(time)
            if (_fromx == java.lang.Float.MIN_VALUE) _fromx = _value.initialX()
            if (_fromy == java.lang.Float.MIN_VALUE) _fromy = _value.initialY()
        }

        override fun apply(time: Float): Float {
            val dt = time - _start
            if (dt >= _duration)
                _value[_tox] = _toy
            else
                _value[_interp.apply(_fromx, _tox - _fromx, dt, _duration)] = _interp.apply(_fromy, _toy - _fromy, dt, _duration)
            return _duration - dt
        }

        override fun makeComplete() {
            _value[_tox] = _toy
        }

        protected var _fromx = java.lang.Float.MIN_VALUE
        protected var _fromy = java.lang.Float.MIN_VALUE
        protected var _tox: Float = 0.toFloat()
        protected var _toy: Float = 0.toFloat()
    }

    /** Delays a specified number of milliseconds.  */
    class Delay(protected val _duration: Float) : Animation() {

        override fun apply(time: Float): Float {
            return _start + _duration - time
        }
    }

    /** Executes an action and completes immediately.  */
    class Action(protected val _action: Runnable) : Animation() {

        override fun init(time: Float) {
            super.init(time)
            _complete = false
        }

        override fun apply(time: Float): Float {
            makeComplete()
            return _start - time
        }

        override fun makeComplete() {
            if (!_complete) {
                _action.run()
                _complete = true
            }
        }

        protected var _complete: Boolean = false
    }

    /** Repeats its underlying animation over and over again (until removed).  */
    class Repeat(protected var _layer: Layer) : Animation() {

        override fun then(): AnimBuilder {
            return object : ChainBuilder() {
                override operator fun next(): Animation {
                    // set ourselves as the repeat target of this added animation
                    return this@Repeat
                }
            }
        }

        override fun apply(time: Float): Float {
            return _start - time // immediately move to our next animation
        }

        override operator fun next(): Animation? {
            // if our target layer is no longer active, we're done
            return if (_layer.parent() == null) null else _next
        }
    }

    /** An animation that shakes a layer randomly in the x and y directions.  */
    class Shake(protected val _layer: Layer) : Animation.Interped<Shake>() {

        /** Configures the amount under and over the starting x and y allowed when shaking. The
         * animation will shake the layer in the range `x + underX` to `x + overX` and
         * similarly for y, thus `underX` (and `underY`) should be negative.  */
        fun bounds(underX: Float, overX: Float, underY: Float, overY: Float): Shake {
            _underX = underX
            _overX = overX
            _underY = underY
            _overY = overY
            return this
        }

        /** Configures the shake cycle time in the x and y directions.  */
        fun cycleTime(millis: Float): Shake {
            return cycleTime(millis, millis)
        }

        /** Configures the shake cycle time in the x and y directions.  */
        fun cycleTime(millisX: Float, millisY: Float): Shake {
            _cycleTimeX = millisX
            _cycleTimeY = millisY
            return this
        }

        override fun init(time: Float) {
            super.init(time)
            _startX = _layer.tx()
            _startY = _layer.ty()

            // start our X/Y shaking randomly in one direction or the other
            _curMinX = _startX
            if (_overX == 0f)
                _curRangeX = _underX
            else if (_underX == 0f)
                _curRangeX = _overX
            else
                _curRangeX = if (RANDS.nextBoolean()) _overX else _underX
            _curMinY = _startY
            if (_overY == 0f)
                _curRangeY = _underY
            else if (_underY == 0f)
                _curRangeY = _overY
            else
                _curRangeY = if (RANDS.nextBoolean()) _overY else _underY
        }

        override fun apply(time: Float): Float {
            val dt = time - _start
            val nx: Float
            val ny: Float
            if (dt < _duration) {
                val dtx = time - _timeX
                val dty = time - _timeY
                if (dtx < _cycleTimeX)
                    nx = _interp.apply(_curMinX, _curRangeX, dtx, _cycleTimeX)
                else {
                    nx = _curMinX + _curRangeX
                    _curMinX = nx
                    val rangeX = _startX + (if (_curRangeX < 0) _overX else _underX) - nx
                    _curRangeX = rangeX / 2 + RANDS.nextFloat() * rangeX / 2
                    _timeX = time
                }
                if (dty < _cycleTimeY)
                    ny = _interp.apply(_curMinY, _curRangeY, dty, _cycleTimeY)
                else {
                    ny = _curMinY + _curRangeY
                    _curMinY = ny
                    val rangeY = _startY + (if (_curRangeY < 0) _overY else _underY) - ny
                    _curRangeY = rangeY / 2 + RANDS.nextFloat() * rangeY / 2
                    _timeY = time
                }
            } else {
                nx = _startX
                ny = _startY
            }
            _layer.setTranslation(nx, ny)
            return _duration - dt
        }

        override fun makeComplete() {
            _layer.setTranslation(_startX, _startY)
        }

        // parameters initialized by setters or in init()
        protected var _underX = -2f
        protected var _overX = 2f
        protected var _underY = -2f
        protected var _overY = 2f
        protected var _cycleTimeX = 100f
        protected var _cycleTimeY = 100f
        protected var _startX: Float = 0.toFloat()
        protected var _startY: Float = 0.toFloat()

        // parameters used during animation
        protected var _timeX: Float = 0.toFloat()
        protected var _timeY: Float = 0.toFloat()
        protected var _curMinX: Float = 0.toFloat()
        protected var _curRangeX: Float = 0.toFloat()
        protected var _curMinY: Float = 0.toFloat()
        protected var _curRangeY: Float = 0.toFloat()
    }

    /**
     * Returns a builder for constructing an animation that will be queued up for execution when
     * the current animation completes. *Note:* only a single animation can be chained on
     * this returned animation builder. You cannot queue up multiple animations to fire in parallel
     * using the builder returned by this method. Use [AnimGroup] for that.
     */
    open fun then(): AnimBuilder {
        return object : ChainBuilder() {
            override operator fun next(): Animation? {
                // our _next is either null, or it points to the animation to which we should
                // repeat when we reach the end of this chain; so pass the null or the repeat
                // target down to our new next animation
                return _next
            }
        }
    }

    /**
     * Returns a handle on this collection of animations which can be used to cancel the animation.
     * This handle references the root animation in this chain of animations, and will cancel all
     * (as yet uncompleted) animations in the chain.
     */
    fun handle(): Handle {
        return object : Handle {
            override fun cancel() {
                _root.cancel()
            }

            override fun complete() {
                _root.completeChain()
            }

            override fun toString(): String {
                return "handle:" + this@Animation
            }
        }
    }

    open fun init(time: Float) {
        _start = time
        _current = this
    }

    open fun apply(animator: Animator, time: Float): Float {
        // if we're canceled, abandon ship now
        if (_canceled) return 0f

        // if the current animation has completed, move the next one in our chain
        var remain = _current!!.apply(time)
        if (remain > 0) return remain

        while (remain <= 0) {
            // if we've been canceled, return 0 to indicate that we're done
            if (_canceled) return 0f

            // if we have no next animation, return our overflow
            _current = _current!!.next()
            if (_current == null) return remain

            // otherwise init and apply our next animation (accounting for overflow)
            _current!!.init(time + remain)
            remain = _current!!.apply(time)
        }
        return remain
    }

    protected fun cancel() {
        _canceled = true
    }

    /**
     * This will be called when an animation chain is requested to complete immediately. It should
     * configure this animation to its final state. *NOTE*: this method must be idempotent
     * and may be called after the animation is already completed. Thus any "one shot" animations
     * should be sure to track whether they have already completed "naturally" and NOOP if this
     * method is called.
     */
    protected open fun makeComplete() {
        // by default, do nothing
    }

    fun completeChain() {
        // stop if we hit the end of the chain, or we see an animation that we've already seen
        // (indicating a loop in the animation chain, which Repeat animations use)
        val seen = IdentityHashMap<Animation, Boolean>()
        var anim: Animation? = this
        while (anim != null && seen.put(anim, true) == null) {
            if (anim._canceled)
                throw IllegalStateException(
                        "Cannot complete() a canceled animation.")
            anim.makeComplete()
            anim.cancel()
            anim = anim.next()
        }
    }

    protected abstract fun apply(time: Float): Float

    protected open operator fun next(): Animation? {
        return _next
    }

    override fun toString(): String {
        var name = javaClass.name
        name = name.substring(name.lastIndexOf(".") + 1)
        return name + ":" + hashCode() + " start:" + _start
    }

    protected abstract inner class ChainBuilder : AnimBuilder() {
        override fun <T : Animation> add(anim: T): T {
            if (_added)
                throw IllegalStateException(
                        "Cannot add multiple animations off the same then()")
            _added = true

            anim._root = _root
            anim._next = next()
            _next = anim
            return anim
        }

        protected abstract operator fun next(): Animation?
        protected var _added: Boolean = false
    }

    protected var _start: Float = 0.toFloat()
    protected var _root = this
    protected var _current: Animation? = null
    protected var _next: Animation? = null
    protected var _canceled: Boolean = false

    companion object {

        protected val RANDS = Random()
    }
}
