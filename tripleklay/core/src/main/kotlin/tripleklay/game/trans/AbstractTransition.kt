package tripleklay.game.trans

import klay.core.Platform
import tripleklay.game.ScreenStack
import tripleklay.game.ScreenStack.Screen

/**
 * A base class for transitions that handles duration and the PITA machinery to case return values
 * to the right type.
 */
abstract class AbstractTransition<T : AbstractTransition<T>> : ScreenStack.Transition() {
    /** Configures the duration of the transition.  */
    fun duration(duration: Float): T {
        _duration = duration
        return asT()
    }

    /** Configures an action to be executed when this transition starts.  */
    fun onStart(action: () -> Unit): T {
        assert(_onStart == null) { "onStart action already configured." }
        _onStart = action
        return asT()
    }

    /** Configures an action to be executed when this transition completes.  */
    fun onComplete(action: () -> Unit): T {
        assert(_onComplete == null) { "onComplete action already configured." }
        _onComplete = action
        return asT()
    }

    override fun init(plat: Platform, oscreen: Screen, nscreen: Screen) {
        if (_onStart != null) {
            _onStart!!()
        }
    }

    override fun complete(oscreen: Screen, nscreen: Screen) {
        if (_onComplete != null) {
            _onComplete!!()
        }
    }

    /**
     * Returns `this` cast to `T`.
     */
    protected fun asT(): T {
        return this as T
    }

    protected open fun defaultDuration(): Float {
        return 1000f
    }

    protected var _duration = defaultDuration()
    protected var _onStart: (() -> Unit)? = null
    protected var _onComplete: (() -> Unit)? = null
}
