package tripleklay.game

import klay.core.Clock
import klay.core.Game
import klay.scene.GroupLayer
import klay.scene.Pointer
import pythagoras.f.IDimension
import react.*
import tripleklay.ui.Interface
import tripleklay.ui.Root
import tripleklay.util.Interpolator

import java.util.ArrayList

/**
 * Maintains a 2D layout of [Screen]s. New screens can be introduced in a direction, and the
 * view is scrolled in that direction to focus on the new screen. The user can then slide the view
 * back toward the previous screen (in the opposite direction that it was introduced). If they
 * release their slide with the old screen sufficiently visible, it will be restored to focus.
 */
class ScreenSpace
/** Creates a screen space which will manage screens for `game`.  */
(protected val _game: Game, protected val _rootLayer: GroupLayer) : Iterable<ScreenSpace.Screen> {
    /** The directions in which a new screen can be added.  */
    abstract class Dir : Cloneable {

        /** Returns the horizontal motion of this direction: 1, 0 or -1.  */
        open fun horizComp(): Int {
            return 0
        }

        /** Returns the vertical motion of this direction: 1, 0 or -1.  */
        open fun vertComp(): Int {
            return 0
        }

        /** Returns whether this direction can be manually "untransitioned".  */
        fun canUntrans(): Boolean {
            return horizComp() != 0 || vertComp() != 0
        }

        /** Returns the direction to use when untransing from this dir.  */
        abstract fun untransDir(): Dir

        /** Prepares `oscreen` and `nscreen` to be transitioned. `oscreen` is the
         * currently visible screen and `nscreen` is the screen transitioning into view.  */
        fun init(oscreen: Screen, nscreen: Screen) {
            oscreen.setTransiting(true)
            nscreen.setTransiting(true)
        }

        /** Updates the position of `oscreen` and `nscreen` based on `pct`.
         * @param pct a value ranged `[0,1]` indicating degree of completeness.
         */
        abstract fun update(oscreen: Screen, nscreen: Screen, pct: Float)

        /** Cleans up after a transition. [update] will have been called with `pct`
         * equal to one immediately prior to this call, so this method is only needed when actual
         * cleanup is needed, like the removal of custom shaders, etc.
         *
         *
         * Note also that the old screen's layer will have been made non-visible prior to this
         * call. This call should not restore that visibility.  */
        open fun finish(oscreen: Screen, nscreen: Screen) {
            oscreen.setTransiting(false)
            nscreen.setTransiting(false)
        }

        /** Returns the duration of this transition (in millis).  */
        fun transitionTime(): Float {
            return 500f
        }
    }

    /**
     * A screen that integrates with `ScreenSpace`. The screen lifecycle is:
     * `init [wake gainedFocus lostFocus sleep]+ dispose`.
     *
     *
     * When the screen has the potential to become visible (due to the user scrolling part of
     * the screen into view) it will have been wakened. If the user selects the screen, it will be
     * animated into position and then `gainedFocus` will be called. If the user scrolls a
     * new screen into view, `lostFocus` will be called, the screen will be animated away. If
     * the screen is no longer "at risk" of being shown, `sleep` will be called. When the
     * screen is finally removed from the screen space, `dispose` will be called.
     */
    abstract class Screen(protected val _game: Game) {

        /** Contains the scene graph root for this screen.  */
        val layer = createGroupLayer()

        /** A signal emitted on every simulation update, while this screen is showing.  */
        val update = Signal<Clock>()

        /** A signal emitted on every frame, while this screen is showing.  */
        val paint = Signal<Clock>()

        init {
            layer.setName(this@Screen.toString() + " layer")
        }

        /** Called when this screen is first added to the screen space.  */
        fun init() {
            layer.setSize(size())
        }

        /** Returns the size of this screen, for use by transitions.
         * Defaults to the size of the entire view.  */
        fun size(): IDimension {
            return _game.plat.graphics.viewSize
        }

        /** Returns true when this screen is awake.  */
        fun awake(): Boolean {
            return _flags and AWAKE != 0
        }

        /** Returns true when this screen is in-transition.  */
        fun transiting(): Boolean {
            return _flags and TRANSITING != 0
        }

        /** Called when this screen will potentially be shown.
         * Should create main UI and prepare it for display.  */
        open fun wake() {
            _flags = _flags or AWAKE
        }

        /** Called when this screen has become the active screen.  */
        fun gainedFocus() {
            assert(awake())
        }

        /** Called when some other screen is about to become the active screen. This screen will be
         * animated out of view. This may not be immediately followed by a call to [.sleep]
         * because the screen may remain visible due to incidental scrolling by the user. Only
         * when the screen is separated from the focus screen by at least one screen will it be
         * put to sleep.  */
        fun lostFocus() {}

        /** Called when this screen is no longer at risk of being seen by the user. This should
         * dispose the UI and minimize the screen's memory footprint as much as possible.  */
        open fun sleep() {
            _flags = _flags and AWAKE.inv()
            _closeOnSleep.close()
        }

        /** Called when this screen is removed from the screen space. This will always be preceded
         * by a call to [.sleep], but if there are any resources that the screen retains
         * until it is completely released, this is the place to remove them.  */
        fun dispose() {
            assert(!awake())
        }

        /** Returns whether or not an untransition gesture may be initiated via `dir`.
         *
         *
         * By default this requires that the screen be at its origin in x or y depending on the
         * orientation of `dir`. If a screen uses a `Flicker` to scroll vertically,
         * this will automatically do the right thing. If there are other circumstances in which a
         * screen wishes to prevent the user from initiating an untransition gesture, this is the
         * place to put 'em.
         */
        fun canUntrans(dir: Dir): Boolean {
            if (dir.horizComp() != 0) return layer.tx() == 0f
            return if (dir.vertComp() != 0) layer.ty() == 0f else true
        }

        /**
         * Adds `ac` to a list of closeables which will be closed when this screen goes to
         * sleep.
         */
        fun closeOnSleep(ac: AutoCloseable) {
            _closeOnSleep.add(ac)
        }

        override fun toString(): String {
            val name = javaClass.name
            return name.substring(name.lastIndexOf(".") + 1).intern()
        }

        internal fun setTransiting(transiting: Boolean) {
            if (transiting)
                _flags = _flags or TRANSITING
            else
                _flags = _flags and TRANSITING.inv()
        }

        internal var isActive: Boolean
            get() = _scons !== Closeable.Util.NOOP
            set(active) {
                _scons.close()
                if (active)
                    _scons = Closeable.Util.join(
                            _game.update.connect(update.slot()),
                            _game.paint.connect(paint.slot()))
                else
                    _scons = Closeable.Util.NOOP
                layer.setVisible(active)
            }

        /** Creates the group layer used by this screen. Subclasses may wish to override and use a
         * clipped group layer instead. Note that the size of the group layer will be set to the
         * screen size in [.init].  */
        protected fun createGroupLayer(): GroupLayer {
            return GroupLayer()
        }

        protected var _flags: Int = 0
        protected var _scons = Closeable.Util.NOOP
        protected val _closeOnSleep = Closeable.Set()

        companion object {

            /** Flag: whether this screen is currently awake.  */
            protected val AWAKE = 1 shl 0
            /** Flag: whether this screen is currently transitioning.  */
            protected val TRANSITING = 1 shl 1
        }
    }

    /** A [Screen] that takes care of basic UI setup for you.  */
    abstract class UIScreen protected constructor(game: Game) : Screen(game) {

        /** Manages the main UI elements for this screen.  */
        val iface: Interface

        override fun wake() {
            super.wake()
            createUI()
        }

        override fun sleep() {
            super.sleep()
            iface.disposeRoots()
            // a screen is completely cleared and recreated between sleep/wake calls, so clear the
            // animator after disposeing the root so that unprocessed anims don't hold onto memory
            iface.anim.clear()
        }

        init {
            iface = Interface(game.plat, paint)
        }

        /** Creates the main UI for this screen. Create one or more [Root] instances using
         * [.iface] and they will be disposed in [.sleep].  */
        protected abstract fun createUI()
    }

    override fun iterator(): Iterator<Screen> {
        return object : Iterator<Screen> {
            private var _idx = 0
            override fun hasNext(): Boolean {
                return _idx < screenCount()
            }

            override fun next(): Screen {
                return screen(_idx++)
            }
        }
    }

    /** Returns the number of screens in the space.  */
    fun screenCount(): Int {
        return _screens.size
    }

    /** Returns the screen at `index`.  */
    fun screen(index: Int): Screen {
        return _screens[index].screen
    }

    /** Returns the currently focused screen.  */
    fun focus(): Screen? {
        return if (_current == null) null else _current!!.screen
    }

    /** Returns true if we're transitioning between two screens at this instant. This may either be
     * an animation driven transition, or a manual transition in progress due to a user drag.  */
    val isTransiting: Boolean
        get() = _target != null

    /** Returns the degree of completeness (`[0,1]`) of any in-progress transition, or 0.  */
    fun transPct(): Float {
        return _transPct
    }

    /** Returns the target screen in the current transition, or null.  */
    fun target(): Screen? {
        return if (_target == null) null else _target!!.screen
    }

    /** Adds `screen` to this space but does not activate or wake it. This is intended for
     * prepopulation of a screenstack at app start. This method *must not* be called once
     * the stack is in normal operation. A series of calls to `initAdd` must be followed by
     * one call to [.add] with the screen that is actually to be displayed at app start.  */
    fun initAdd(screen: Screen, dir: Dir) {
        assert(_screens.isEmpty() || !_screens[0].screen.isActive)
        screen.init()
        _screens.add(0, ActiveScreen(screen, dir))
    }

    /** Adds `screen` to this space, positioned `dir`-wise from the current screen. For
     * example, using `RIGHT` will add the screen to the right of the current screen and
     * will slide the view to the right to reveal the new screen. The user would then manually
     * slide the view left to return to the previous screen.  */
    fun add(screen: Screen, dir: Dir) {
        add(screen, dir, false)
    }

    /** Adds `screen` to this space, replacing the current top-level screen. The screen is
     * animated in the same manner as [.add] using the same direction in which the current
     * screen was added. This ensures that the user returns to the previous screen in the same way
     * that they would via the to-be-replaced screen.  */
    fun replace(screen: Screen) {
        if (_screens.isEmpty()) throw IllegalStateException("No current screen to replace()")
        add(screen, _screens[0].dir, true)
    }

    /** Removes `screen` from this space. If it is the top-level screen, an animated
     * transition to the previous screen will be performed. Otherwise the screen will simply be
     * removed.  */
    fun pop(screen: Screen) {
        if (_current!!.screen === screen) {
            if (_screens.size > 1)
                popTrans(0f)
            else {
                val oscr = _screens.removeAt(0)
                takeFocus(oscr)
                oscr.dispose()
                _current = null
                _onPointer = Closeable.Util.close(_onPointer)
            }
        } else {
            // TODO: this screen may be inside UntransListener.previous so we may need to recreate
            // UntransListener with a new previous screen; or maybe just don't support pulling
            // screens out of the middle of the stack; that's kind of wacky; popTop and popTo may
            // be enough
            val idx = indexOf(screen)
            if (idx >= 0) {
                popAt(idx)
                checkSleep() // we may need to wake a new screen
            }
        }
    }

    /** Removes all screens from the space until `screen` is reached. No transitions will be
     * used, all screens will simply be removed and disposed until we reach `screen`, and
     * that screen will be woken and positioned properly.  */
    fun popTo(screen: Screen) {
        if (current() === screen) return  // NOOP!
        var top = _screens[0]
        while (top.screen !== screen) {
            _screens.removeAt(0)
            takeFocus(top)
            top.dispose()
            top = _screens[0]
        }
        checkSleep() // wake up the top screen
        top.screen.layer.setTranslation(0f, 0f) // ensure that it's positioned properly
        top.screen.isActive = true // mark the screen as active
        giveFocus(top)
    }

    /** Returns the current screen, or `null` if this space is empty.  */
    fun current(): Screen? {
        return if (_current == null) null else _current!!.screen
    }

    /** Returns the lowest screen in the stack.  */
    fun bottom(): Screen {
        return _screens[_screens.size - 1].screen
    }

    protected fun indexOf(screen: Screen): Int {
        var ii = 0
        val ll = _screens.size
        while (ii < ll) {
            if (_screens[ii].screen === screen) return ii
            ii++
        }
        return -1
    }

    protected fun add(screen: Screen, dir: Dir, replace: Boolean) {
        screen.init()
        val otop = if (_screens.isEmpty()) null else _screens[0]
        val ntop = ActiveScreen(screen, dir)
        _screens.add(0, ntop)
        ntop.check(true) // wake up the to-be-added screen
        if (otop == null || !otop.screen.isActive) {
            ntop.screen.isActive = true
            giveFocus(ntop)
        } else
            transition(otop, ntop, ntop.dir, 0f).onComplete.connect({
                giveFocus(ntop)
                if (replace) popAt(1)
            })
    }

    protected fun transition(oscr: ActiveScreen, nscr: ActiveScreen, dir: Dir, startPct: Float): Driver {
        takeFocus(oscr)
        return Driver(oscr, nscr, dir, startPct)
    }

    protected fun checkSleep() {
        if (_screens.isEmpty()) return
        // if the top-level screen was introduced via a slide transition, we need to keep the
        // previous screen awake because we could start sliding to it ay any time; otherwise we can
        // put that screen to sleep; all other screens should be sleeping
        val ss = if (_screens[0].dir.canUntrans()) 2 else 1
        var ii = 0
        val ll = _screens.size
        while (ii < ll) {
            _screens[ii].check(ii < ss)
            ii++
        }
    }

    protected fun popTrans(startPct: Float) {
        val oscr = _screens.removeAt(0)
        val dir = oscr.dir.untransDir()
        val nscr = _screens[0]
        nscr.check(true) // wake screen, if necessary
        transition(oscr, nscr, dir, startPct).onComplete.connect({
            giveFocus(nscr)
            oscr.dispose()
        })
    }

    protected fun popAt(index: Int) {
        _screens.removeAt(index).dispose()
    }

    protected fun giveFocus(`as`: ActiveScreen) {
        try {
            _current = `as`
            `as`.screen.gainedFocus()

            // if we have a previous screen, and the direction supports manual untransitioning,
            // set up a listener to handle that
            val previous = if (_screens.size <= 1) null else _screens[1]
            _onPointer.close()
            if (previous == null || !`as`.dir.canUntrans())
                _onPointer = Closeable.Util.NOOP
            else
                _onPointer = `as`.screen.layer.events().connect(UntransListener(`as`, previous))

        } catch (e: Exception) {
            _game.plat.log.warn("Screen choked in gainedFocus() [screen=" + `as`.screen + "]", e)
        }

        checkSleep()
    }

    protected fun takeFocus(`as`: ActiveScreen) {
        try {
            `as`.screen.lostFocus()
        } catch (e: Exception) {
            _game.plat.log.warn("Screen choked in lostFocus() [screen=" + `as`.screen + "]", e)
        }

    }

    protected inner class ActiveScreen(val screen: Screen, val dir: Dir) {

        fun check(awake: Boolean) {
            if (screen.awake() != awake) {
                if (awake) {
                    _rootLayer.add(screen.layer)
                    screen.wake()
                } else {
                    _rootLayer.remove(screen.layer)
                    screen.sleep()
                }
            }
        }

        fun dispose() {
            check(false) // make sure screen is hidden/remove
            screen.dispose()
        }

        override fun toString(): String {
            return screen.toString() + " @ " + dir
        }
    }

    protected inner class UntransListener(private val _cur: ActiveScreen, private val _prev: ActiveScreen) : Pointer.Listener {
        // the start stamp of our gesture, or 0 if we're not in a gesture
        var start: Double = 0.toDouble()
        // whether we're in the middle of an untransition gesture
        var untransing: Boolean = false

        private var _sx: Float = 0.toFloat()
        private var _sy: Float = 0.toFloat()
        private var _offFrac: Float = 0.toFloat()
        private val _udir: Dir = _cur.dir.untransDir() // untrans dir is opposite of trans dir

        override fun onStart(iact: Pointer.Interaction) {
            // if it's not OK to initiate an untransition gesture, or we're already in the middle
            // of animating an automatic transition, ignore this gesture
            if (!_cur.screen.canUntrans(_udir) || _target != null) return
            _sx = iact.event!!.x
            _sy = iact.event!!.y
            start = iact.event!!.time
        }

        override fun onDrag(iact: Pointer.Interaction) {
            if (start == 0.0) return  // ignore if we were disabled at gesture start

            val frac = updateFracs(iact.event!!.x, iact.event!!.y)
            if (!untransing) {
                // if the beginning of the gesture is not "more" in the untrans direction than not,
                // ignore the rest the interaction (note: _offFrac is always positive but frac is
                // positive if it's in the untrans direction and negative otherwise)
                if (_offFrac > frac) {
                    start = 0.0
                    return
                }
                // TODO: we should probably ignore small movements in all directions before we
                // commit to this gesture or not; if someone always jerks their finger to the right
                // before beginning an interaction, that would always disable an up or down gesture
                // before it ever had a chance to get started... oh, humans

                // the first time we start untransing, do _udir.init() & setViz
                untransing = true
                _target = _prev
                _target!!.screen.isActive = true
                _udir.init(_cur.screen, _prev.screen)
                iact.capture()
            }

            _udir.update(_cur.screen, _prev.screen, frac)
            _transPct = frac
        }

        override fun onEnd(iact: Pointer.Interaction) {
            if (start == 0.0 || !untransing) return

            // clean up after our current manual transition because we're going to set up a driver
            // to put the current screen back into place or to pop it off entirely
            _udir.finish(_cur.screen, _prev.screen)

            val frac = updateFracs(iact.event!!.x, iact.event!!.y)
            // compute the "velocity" of this gesture in "screens per second"
            val fvel = 1000 * frac / (iact.event!!.time - start).toFloat()
            // if we've revealed more than 30% of the old screen, or we're going fast enough...
            if (frac > 0.3f || fvel > 1.25f) {
                // ...pop back to the previous screen
                assert(_cur === _screens[0])
                popTrans(frac)
            } else {
                // ...otherwise animate the current screen back into position
                Driver(_prev, _cur, _cur.dir, 1 - frac)
            }
            clear()
        }

        override fun onCancel(iact: Pointer.Interaction?) {
            if (start == 0.0 || !untransing) return

            // snap our screens back to their original positions
            _udir.update(_cur.screen, _prev.screen, 0f)
            _transPct = 0f
            _prev.screen.isActive = false
            _udir.finish(_cur.screen, _prev.screen)
            clear()
        }

        protected fun clear() {
            untransing = false
            start = 0.0
            _target = null
        }

        protected fun updateFracs(cx: Float, cy: Float): Float {
            // project dx/dy along untransition dir's vector
            val dx = cx - _sx
            val dy = cy - _sy
            val hc = _udir.horizComp()
            val vc = _udir.vertComp()
            val ssize = _prev.screen.size()
            val frac: Float
            if (hc != 0) {
                frac = dx * hc / ssize.width
                _offFrac = Math.abs(dy) / ssize.height
            } else {
                frac = dy * vc / ssize.height
                _offFrac = Math.abs(dx) / ssize.width
            }
            return frac
        }

        protected fun computeFrac(cx: Float, cy: Float): Float {
            // project dx/dy along untransition dir's vector
            val dx = cx - _sx
            val dy = cy - _sy
            val tx = dx * _udir.horizComp()
            val ty = dy * _udir.vertComp()
            // the distance we've traveled over the full width/height of the screen is the frac
            return if (tx > 0) tx / _prev.screen.size().width else ty / _prev.screen.size().height
        }
    }

    /** Drives a transition via an animation.  */
    protected inner class Driver(val outgoing: ActiveScreen, val incoming: ActiveScreen, val dir: Dir, val startPct: Float) {
        val duration: Float
        val onComplete = UnitSignal()
        val interp: Interpolator
        val onPaint: Closeable
        var elapsed: Float = 0.toFloat()

        init {
            this.duration = dir.transitionTime()
            // TODO: allow Dir to provide own interpolator?
            this.interp = if (startPct == 0f) Interpolator.EASE_INOUT else Interpolator.EASE_OUT

            // activate the incoming screen (the outgoing will already be active; the incoming one
            // may already be active as well but setActive is idempotent so it's OK)
            incoming.screen.isActive = true
            assert(outgoing.screen.isActive)
            _target = incoming

            dir.init(outgoing.screen, incoming.screen)
            dir.update(outgoing.screen, incoming.screen, startPct)
            _transPct = startPct

            // connect to the paint signal to drive our animation
            onPaint = _game.paint.connect({ clock: Clock ->
                paint(clock)
            })
        }

        protected fun paint(clock: Clock) {
            // if this is our first frame, cap dt at 33ms because the first frame has to eat the
            // initialization time for the to-be-introduced screen, and we don't want that to chew
            // up a bunch of our transition time if it's slow; the user will not have seen
            // anything happen up to now, so this won't cause a jitter in the animation
            if (elapsed == 0f)
                elapsed += Math.min(33, clock.dt).toFloat()
            else
                elapsed += clock.dt.toFloat()
            val pct = Math.min(elapsed / duration, 1f)
            val ipct: Float
            if (startPct >= 0)
                ipct = startPct + (1 - startPct) * interp.apply(pct)
            else
                ipct = 1 - interp.apply(pct)// if we were started with a negative startPct, we're scrolling back from an
            // untranslation gesture that ended on the "opposite" side
            dir.update(outgoing.screen, incoming.screen, ipct)
            _transPct = ipct
            if (pct == 1f) complete()
        }

        fun complete() {
            onPaint.close()
            outgoing.screen.isActive = false
            dir.finish(outgoing.screen, incoming.screen)
            _transPct = 1f
            _target = null
            onComplete.emit()
        }
    }

    protected val _screens: MutableList<ActiveScreen> = ArrayList()
    protected var _transPct: Float = 0.toFloat()
    protected var _current: ActiveScreen? = null
    protected var _target: ActiveScreen? = null
    protected var _onPointer = Closeable.Util.NOOP

    companion object {

        val UP: Dir = object : Dir() {
            override fun vertComp(): Int {
                return -1
            }

            override fun untransDir(): Dir {
                return DOWN
            }

            override fun update(oscreen: Screen, nscreen: Screen, pct: Float) {
                val oheight = oscreen.size().height
                val ostart = 0f
                val nstart = oheight
                val range = -oheight
                val offset = pct * range
                oscreen.layer.setTy(ostart + offset)
                nscreen.layer.setTy(nstart + offset)
            }
        }

        val DOWN: Dir = object : Dir() {
            override fun vertComp(): Int {
                return 1
            }

            override fun untransDir(): Dir {
                return UP
            }

            override fun update(oscreen: Screen, nscreen: Screen, pct: Float) {
                val nheight = nscreen.size().height
                val ostart = 0f
                val nstart = -nheight
                val range = nheight
                val offset = pct * range
                oscreen.layer.setTy(ostart + offset)
                nscreen.layer.setTy(nstart + offset)
            }
        }

        val LEFT: Dir = object : Dir() {
            override fun horizComp(): Int {
                return -1
            }

            override fun untransDir(): Dir {
                return RIGHT
            }

            override fun update(oscreen: Screen, nscreen: Screen, pct: Float) {
                val owidth = oscreen.size().width
                val ostart = 0f
                val nstart = owidth
                val range = -owidth
                val offset = pct * range
                oscreen.layer.setTx(ostart + offset)
                nscreen.layer.setTx(nstart + offset)
            }
        }

        val RIGHT: Dir = object : Dir() {
            override fun untransDir(): Dir {
                return LEFT
            }

            override fun horizComp(): Int {
                return 1
            }

            override fun update(oscreen: Screen, nscreen: Screen, pct: Float) {
                val nwidth = nscreen.size().width
                val ostart = 0f
                val nstart = -nwidth
                val range = nwidth
                val offset = pct * range
                oscreen.layer.setTx(ostart + offset)
                nscreen.layer.setTx(nstart + offset)
            }
        }

        val IN: Dir = object : Dir() {
            override fun untransDir(): Dir {
                return OUT
            }

            override fun update(oscreen: Screen, nscreen: Screen, pct: Float) {
                oscreen.layer.setAlpha(1 - pct)
                nscreen.layer.setAlpha(pct)
                // TODO: scaling
            }

            override fun finish(oscreen: Screen, nscreen: Screen) {
                super.finish(oscreen, nscreen)
                oscreen.layer.setAlpha(1f)
            }
        }

        val OUT: Dir = object : Dir() {
            override fun untransDir(): Dir {
                return IN
            }

            override fun update(oscreen: Screen, nscreen: Screen, pct: Float) {
                oscreen.layer.setAlpha(1 - pct)
                nscreen.layer.setAlpha(pct)
                // TODO: scaling
            }

            override fun finish(oscreen: Screen, nscreen: Screen) {
                super.finish(oscreen, nscreen)
                oscreen.layer.setAlpha(1f)
            }
        }

        val FLIP: Dir = object : Dir() {
            override fun untransDir(): Dir {
                return this
            }

            override fun update(oscreen: Screen, nscreen: Screen, pct: Float) {
                // TODO
            }
        }
    }
}
