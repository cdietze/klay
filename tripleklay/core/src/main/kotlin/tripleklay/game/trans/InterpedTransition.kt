package tripleklay.game.trans

import tripleklay.util.Interpolator

/**
 * Handles shared code for transitions that use an interpolation.
 */
abstract class InterpedTransition<T : InterpedTransition<T>> : AbstractTransition<T>() {
    fun linear(): T {
        return interp(Interpolator.LINEAR)
    }

    fun easeIn(): T {
        return interp(Interpolator.EASE_IN)
    }

    fun easeOut(): T {
        return interp(Interpolator.EASE_OUT)
    }

    fun easeInOut(): T {
        return interp(Interpolator.EASE_INOUT)
    }

    fun interp(interp: Interpolator): T {
        _interp = interp
        return asT()
    }

    protected open fun defaultInterpolator(): Interpolator {
        return Interpolator.EASE_INOUT
    }

    protected var _interp = defaultInterpolator()
}
