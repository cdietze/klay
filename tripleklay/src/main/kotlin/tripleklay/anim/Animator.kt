package tripleklay.anim

import klay.core.Clock
import react.Signal
import react.Slot

/**
 * Handles creation and management of animations. Animations may involve the tweening of a
 * geometric property of a layer (x, y, rotation, scale, alpha), or simple delays, or performing
 * actions. Animations can also be sequenced to orchestrate complex correlated actions.

 *
 *  [.onPaint] should be connected to a `paint` signal to drive the animations.
 */
class Animator : AnimBuilder {
    /**
     * Performs per-frame animation processing. This should be connected to a `paint` signal.
     */
    val onPaint: Slot<Clock> = { onPaint(it) }

    /** Creates an animator which is not connected to a frame signal. You must manually connect
     * [.onPaint] to a frame signal to drive this animator.  */
    constructor()

    /** Creates an animator which is permanently connected to `paint`. This is useful when
     * you are connecting to a paint signal whose lifetime is the same as this animator because
     * there's no way to ever disconnect the animator from the signal.  */
    constructor(paint: Signal<Clock>) {
        paint.connect(onPaint)
    }

    /**
     * Causes this animator to delay the start of any subsequently registered animations until the
     * specified delay (in milliseconds) has elapsed *after this barrier becomes active*.
     * Any previously registered barriers must first expire and this barrier must move to the head
     * of the list before its delay timer will be started. This is probably what you want.
     */
    fun addBarrier(delay: Float = 0f) {
        val barrier = Barrier(delay)
        _barriers.add(barrier)
        // pushing a barrier causes subsequent animations to be accumulated separately
        _accum = barrier.accum
    }

    /**
     * Clears out any pending animations. *NOTE* all animations simply disappear. Any queued
     * animations that invoked actions will not execute, nor will the cleanup actions of any
     * animations that involve cleanup. This should only be invoked if you know the layers involved
     * in animations will be destroyed separately.
     */
    fun clear() {
        _anims.clear()
        _nanims.clear()
        _barriers.clear()
        _accum = _nanims
    }

    /**
     * Registers an animation with this animator. It will be started on the next frame and continue
     * until cancelled or it reports that it has completed.
     */
    override fun <T : Animation> add(anim: T): T {
        _accum.add(anim)
        return anim
    }

    private fun onPaint(clock: Clock) {
        val time = clock.tick.toFloat()

        // if we have any animations queued up to be added, add those now
        if (!_nanims.isEmpty()) {
            var ii = 0
            val ll = _nanims.size
            while (ii < ll) {
                _nanims[ii].init(time)
                ii++
            }
            _anims.addAll(_nanims)
            _nanims.clear()
        }

        // now process all of our registered animations
        var ii = 0
        var ll = _anims.size
        while (ii < ll) {
            if (_anims[ii].apply(this, time) <= 0) {
                _anims.removeAt(ii--)
                ll -= 1
            }
            ii++
        }

        // process our barriers; if...
        if (!_barriers.isEmpty() && // we have at least one barrier

                _anims.isEmpty() && _nanims.isEmpty() && // we have no active animations

                _barriers[0].expired(time)) {        // the top barrier is expired
            val barrier = _barriers.removeAt(0)
            _nanims.addAll(barrier.accum)
            // if we just unblocked the last barrier, start accumulating back on _nanims
            if (_barriers.isEmpty()) {
                _accum = _nanims
            }
        }
    }

    /** Implementation details, avert your eyes.  */
    private class Barrier(val expireDelay: Float) {
        val accum: MutableList<Animation> = ArrayList()
        var absoluteExpireTime: Float = 0.toFloat()

        fun expired(time: Float): Boolean {
            if (absoluteExpireTime == 0f) {
                absoluteExpireTime = time + expireDelay
            }
            return time >= absoluteExpireTime
        }
    }

    private var _anims: MutableList<Animation> = ArrayList()
    private var _nanims: MutableList<Animation> = ArrayList()
    private var _accum: MutableList<Animation> = _nanims
    private var _barriers: MutableList<Barrier> = ArrayList()
}
/**
 * Causes this animator to delay the start of any subsequently registered animations until all
 * currently registered animations are complete.
 */
