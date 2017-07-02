package tripleklay.game

import klay.core.Clock
import klay.core.Game
import klay.core.Platform
import klay.scene.GroupLayer
import pythagoras.f.IDimension
import react.Closeable
import react.Signal
import tripleklay.game.Log.log
import tripleklay.game.trans.FlipTransition
import tripleklay.game.trans.PageTurnTransition
import tripleklay.game.trans.SlideTransition
import tripleklay.ui.Interface
import java.util.*

/**
 * Manages a stack of screens. The stack supports useful manipulations: pushing a new screen onto
 * the stack, replacing the screen at the top of the stack with a new screen, popping a screen from
 * the stack.

 *
 *  Care is taken to preserve stack invariants even in the face of errors thrown by screens when
 * being added, removed, shown or hidden. Users can override [.handleError] and either simply
 * log the error, or rethrow it if they would prefer that a screen failure render their entire
 * screen stack unusable.
 */
open class ScreenStack
/**
 * Creates a screen stack that manages screens for `game` on `rootLayer`.
 */
(protected val _game: Game, protected val _rootLayer: GroupLayer) {

    /** Displays and manages the lifecycle for a single game screen.  */
    abstract class Screen {

        /** The layer on which all of this screen's UI must be placed.  */
        val layer = GroupLayer()
        /** A signal emitted on every simulation update, while this screen is showing.  */
        val update = Signal<Clock>()
        /** A signal emitted on every frame, while this screen is showing.  */
        val paint = Signal<Clock>()

        // the following methods provide hooks into the visibility lifecycle of a screen, which
        // takes the form: added -> shown -> { hidden -> shown -> ... } -> hidden -> removed

        /** Returns a reference to the game in which this screen is operating.  */
        abstract fun game(): Game

        /** Returns the size of this screen. This is used for transitions.
         * Defaults to the size of the entire view.  */
        fun size(): IDimension {
            return game().plat.graphics.viewSize
        }

        /** Called when a screen is added to the screen stack for the first time.  */
        open fun wasAdded() {}

        /** Called when a screen becomes the top screen, and is therefore made visible.  */
        open fun wasShown() {
            closeOnHide(game().update.connect(update.slot()))
            closeOnHide(game().paint.connect(paint.slot()))
        }

        /** Called when a screen is no longer the top screen (having either been pushed down by
         * another screen, or popped off the stack).  */
        open fun wasHidden() {
            _closeOnHide.close()
        }

        /** Called when a screen has been removed from the stack. This will always be preceeded by
         * a call to [.wasHidden], though not always immediately.  */
        open fun wasRemoved() {}

        /** Called when this screen's transition into view has completed. [.wasShown] is
         * called immediately before the transition begins, and this method is called when it
         * ends.  */
        open fun showTransitionCompleted() {}

        /** Called when this screen's transition out of view has started. [.wasHidden] is
         * called when the hide transition completes.  */
        open fun hideTransitionStarted() {}

        /** Adds `c` to a set to be closed when this screen is hidden.  */
        fun closeOnHide(c: Closeable) {
            _closeOnHide.add(c)
        }

        protected var _closeOnHide = Closeable.Set()
    }

    /** A [Screen] with an [Interface] for doing UI stuff.  */
    abstract class UIScreen(plat: Platform) : Screen() {

        /** Manages the main UI elements for this screen.  */
        val iface: Interface

        init {
            iface = Interface(plat, paint)
        }

        override fun wasHidden() {
            super.wasHidden()
            // clear the animator on hide so that unprocessed anims don't hold onto memory
            iface.anim.clear()
        }
    }

    /** Implements a particular screen transition.  */
    abstract class Transition {

        /** Direction constants, used by transitions.  */
        enum class Dir {
            UP, DOWN, LEFT, RIGHT
        }

        /** Allows the transition to pre-compute useful values. This will immediately be followed
         * by call to [.update] with an elapsed time of zero.  */
        open fun init(plat: Platform, oscreen: Screen, nscreen: Screen) {}

        /** Called every frame to update the transition
         * @param oscreen the outgoing screen.
         * *
         * @param nscreen the incoming screen.
         * *
         * @param elapsed the elapsed time since the transition started (in millis if that's what
         * * your game is sending to [Screen.update]).
         * *
         * @return false if the transition is not yet complete, true when it is complete.
         */
        abstract fun update(oscreen: Screen, nscreen: Screen, elapsed: Float): Boolean

        /** Called when the transition is complete. This is where the transition should clean up
         * any temporary bits and restore the screens to their original state. The stack will
         * automatically destroy/hide the old screen after calling this method. Also note that this
         * method may be called *before* the transition signals completion, if a new
         * transition is started and this transition needs be aborted.  */
        open fun complete(oscreen: Screen, nscreen: Screen) {}
    }

    /** Used to operate on screens. See [.remove].  */
    interface Predicate {
        /** Returns true if the screen matches the predicate.  */
        fun apply(screen: Screen): Boolean
    }

    /** The x-coordinate at which screens are located. Defaults to 0.  */
    var originX = 0f

    /** The y-coordinate at which screens are located. Defaults to 0.  */
    var originY = 0f

    /** Creates a slide transition.  */
    fun slide(): SlideTransition {
        return SlideTransition(this)
    }

    /** Creates a page turn transition.  */
    fun pageTurn(): PageTurnTransition {
        return PageTurnTransition()
    }

    /** Creates a flip transition.  */
    fun flip(): FlipTransition {
        return FlipTransition()
    }

    /**
     * Pushes the supplied screen onto the stack, making it the visible screen. The currently
     * visible screen will be hidden.
     * @throws IllegalArgumentException if the supplied screen is already in the stack.
     */
    @JvmOverloads fun push(screen: Screen, trans: Transition = defaultPushTransition()) {
        if (_screens.isEmpty()) {
            addAndShow(screen)
        } else {
            val otop = top()!!
            transition(object : Transitor(otop, screen, trans) {
                override fun onComplete() {
                    hide(otop)
                }
            })
        }
    }

    /**
     * Pushes the supplied set of screens onto the stack, in order. The last screen to be pushed
     * will also be shown, using the supplied transition. Note that the transition will be from the
     * screen that was on top prior to this call.
     */
    @JvmOverloads fun push(screens: Iterable<Screen>, trans: Transition = defaultPushTransition()) {
        if (!screens.iterator().hasNext()) {
            throw IllegalArgumentException("Cannot push empty list of screens.")
        }
        if (_screens.isEmpty()) {
            for (screen in screens) add(screen)
            justShow(top()!!)
        } else {
            val otop = top()!!
            var last: Screen? = null
            for (screen in screens) {
                if (last != null) add(last)
                last = screen
            }
            transition(object : Transitor(otop, last!!, trans) {
                override fun onComplete() {
                    hide(otop)
                }
            })
        }
    }

    /**
     * Pops the top screen from the stack until the specified screen has become the
     * topmost/visible screen.  If newTopScreen is null or is not on the stack, this will remove
     * all screens.
     */
    @JvmOverloads fun popTo(newTopScreen: Screen, trans: Transition = defaultPopTransition()) {
        // if the desired top screen is already the top screen, then NOOP
        if (top() === newTopScreen) return
        // remove all intervening screens
        while (_screens.size > 1 && _screens[1] !== newTopScreen) {
            justRemove(_screens[1])
        }
        // now just pop the top screen
        remove(top()!!, trans)
    }

    /**
     * Pops the current screen from the top of the stack and pushes the supplied screen on as its
     * replacement.
     * @throws IllegalArgumentException if the supplied screen is already in the stack.
     */
    @JvmOverloads fun replace(screen: Screen, trans: Transition = defaultPushTransition()) {
        if (_screens.isEmpty()) {
            addAndShow(screen)
        } else {
            val otop = _screens.removeAt(0)
            // log.info("Removed " + otop + ", new top " + top());
            transition(object : Transitor(otop, screen, trans) {
                override fun onComplete() {
                    hide(otop)
                    wasRemoved(otop)
                }
            })
        }
    }

    /**
     * Removes the specified screen from the stack. If it is the currently visible screen, it will
     * first be hidden, and the next screen below in the stack will be made visible.

     * @return true if the screen was found in the stack and removed, false if the screen was not
     * * in the stack.
     */
    @JvmOverloads fun remove(screen: Screen, trans: Transition = defaultPopTransition()): Boolean {
        if (top() !== screen) return justRemove(screen)

        if (_screens.size > 1) {
            val otop = _screens.removeAt(0)
            // log.info("Removed " + otop + ", new top " + top());
            transition(object : Untransitor(otop, top()!!, trans) {
                override fun onComplete() {
                    hide(otop)
                    wasRemoved(otop)
                }
            })
        } else {
            hide(screen)
            justRemove(screen)
        }
        return true
    }

    /**
     * Removes all screens that match the supplied predicate, from lowest in the stack to highest.
     * If the top screen is removed (as the last action), the supplied transition will be used.
     */
    @JvmOverloads fun remove(pred: Predicate, trans: Transition = defaultPopTransition()) {
        // first, remove any non-top screens that match the predicate
        if (_screens.size > 1) {
            val iter = _screens.iterator()
            iter.next() // skip top
            while (iter.hasNext()) {
                val screen = iter.next()
                if (pred.apply(screen)) {
                    iter.remove()
                    wasRemoved(screen)
                    // log.info("Pred removed " + screen + ", new top " + top());
                }
            }
        }
        // last, remove the top screen if it matches the predicate
        if (_screens.size > 0 && pred.apply(top()!!)) remove(top()!!, trans)
    }

    /** Returns the top screen on the stack, or null if the stack contains no screens.  */
    fun top(): Screen? {
        return if (_screens.isEmpty()) null else _screens[0]
    }

    /**
     * Searches from the top-most screen to the bottom-most screen for a screen that matches the
     * predicate, returning the first matching screen. `null` is returned if no matching
     * screen is found.
     */
    fun find(pred: Predicate): Screen? {
        for (screen in _screens) if (pred.apply(screen)) return screen
        return null
    }

    /** Returns true if we're currently transitioning between screens.  */
    val isTransiting: Boolean
        get() = _transitor != null

    /** Returns the number of screens on the stack.  */
    fun size(): Int {
        return _screens.size
    }

    protected open fun defaultPushTransition(): Transition {
        return NOOP
    }

    protected open fun defaultPopTransition(): Transition {
        return NOOP
    }

    protected fun add(screen: Screen) {
        if (_screens.contains(screen)) {
            throw IllegalArgumentException("Cannot add screen to stack twice.")
        }
        _screens.add(0, screen)
        // log.info("Added " + screen + ", new top " + top());
        try {
            screen.wasAdded()
        } catch (e: RuntimeException) {
            handleError(e)
        }

    }

    protected fun addAndShow(screen: Screen) {
        add(screen)
        justShow(screen)
    }

    protected fun justShow(screen: Screen) {
        _rootLayer.addAt(screen.layer, originX, originY)
        try {
            screen.wasShown()
        } catch (e: RuntimeException) {
            handleError(e)
        }

    }

    protected fun hide(screen: Screen) {
        _rootLayer.remove(screen.layer)
        try {
            screen.wasHidden()
        } catch (e: RuntimeException) {
            handleError(e)
        }

    }

    protected fun justRemove(screen: Screen): Boolean {
        val removed = _screens.remove(screen)
        if (removed) wasRemoved(screen)
        // log.info("Just removed " + screen + ", new top " + top());
        return removed
    }

    protected fun wasRemoved(screen: Screen) {
        try {
            screen.wasRemoved()
        } catch (e: RuntimeException) {
            handleError(e)
        }

    }

    protected fun transition(transitor: Transitor) {
        if (_transitor != null) _transitor!!.complete()
        _transitor = transitor
        _transitor!!.init()
    }

    /**
     * A hacky mechanism to allow a game to force a transition to skip some number of frames at its
     * start. If a game's screens tend to do a lot of image loading in wasAdded or immediately
     * after, that will cause an unpleasant jerk at the start of the transition as the first frame
     * or two have order of magnitude larger frame deltas than subsequent frames. Having those
     * render as t=0 and then starting the timer after the skipped frames are done delays the
     * transition by a bit, but ensures that when things are actually animating, that they are nice
     * and smooth.
     */
    protected fun transSkipFrames(): Int {
        return 0
    }

    protected fun setInputEnabled(enabled: Boolean) {
        _game.plat.input.mouseEnabled = enabled
        _game.plat.input.touchEnabled = enabled
    }

    protected open inner class Transitor(protected val _oscreen: Screen, protected val _nscreen: Screen, protected val _trans: Transition) {

        fun init() {
            _oscreen.hideTransitionStarted()
            showNewScreen()
            _trans.init(_game.plat, _oscreen, _nscreen)
            setInputEnabled(false)

            // force a complete if the transition is a noop, so that we don't have to wait until
            // the next update; perhaps we should check some property of the transition object
            // rather than compare to noop, in case we have a custom 0-duration transition
            if (_trans === NOOP)
                complete()
            else
                _onPaint = _game.paint.connect(this::paint)
        }

        fun paint(clock: Clock) {
            if (_skipFrames > 0)
                _skipFrames -= 1
            else {
                _elapsed += clock.dt.toFloat()
                if (_trans.update(_oscreen, _nscreen, _elapsed)) complete()
            }
        }

        fun complete() {
            _transitor = null
            _onPaint.close()
            setInputEnabled(true)
            // let the transition know that it's complete
            _trans.complete(_oscreen, _nscreen)
            // make sure the new screen is in the right position
            _nscreen.layer.setTranslation(originX, originY)
            _nscreen.showTransitionCompleted()
            onComplete()
        }

        protected open fun showNewScreen() {
            addAndShow(_nscreen)
        }

        protected open fun onComplete() {}
        protected var _onPaint = Closeable.Util.NOOP
        protected var _skipFrames = transSkipFrames()
        protected var _elapsed: Float = 0.toFloat()
    }

    protected open inner class Untransitor(oscreen: Screen, nscreen: Screen, trans: Transition) : Transitor(oscreen, nscreen, trans) {

        override fun showNewScreen() {
            justShow(_nscreen)
        }
    }

    /** Called if any exceptions are thrown by the screen calldown functions.  */
    protected fun handleError(error: RuntimeException) {
        log.warning("Screen choked", error)
    }

    /** The currently executing transition, or null.  */
    protected var _transitor: Transitor? = null

    /** Containts the stacked screens from top-most, to bottom-most.  */
    protected val _screens: MutableList<Screen> = ArrayList()

    companion object {

        /** Simply puts the new screen in place and removes the old screen.  */
        val NOOP: Transition = object : Transition() {
            override fun update(oscreen: Screen, nscreen: Screen, elapsed: Float): Boolean {
                return true
            }
        }
    }
}
/**
 * [.push] with the default transition.
 */
/**
 * [.push] with the default transition.
 */
/**
 * [.popTo] with the default transition.
 */
/**
 * [.replace] with the default transition.
 */
/**
 * [.remove] with the default transition.
 */
/**
 * [.remove] with the default transition.
 */
