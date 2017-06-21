package tripleklay.ui

import klay.core.Clock
import klay.scene.Pointer
import react.Closeable
import react.Signal
import react.SignalView
import react.SignalViewListener

/**
 * A button that supports an action on a "long press". A long press is when the user holds the
 * button in the armed state for some reasonably lengthy amount of time (the default is 1000ms).
 * This element behaves as a [Button] for style purposes.
 */
class LongPressButton
/** Creates a button with the supplied text and icon.  */
@JvmOverloads constructor(text: String? = null, icon: Icon? = null) : Button(text, icon) {

    /** Creates a button with the supplied icon.  */
    constructor(icon: Icon) : this(null, icon) {}

    /** A signal that is emitted when this button is long pressed.
     * See [.LONG_PRESS_INTERVAL].  */
    fun longPressed(): SignalView<Button> {
        return _longPressed
    }

    /** Programmatically triggers a long press of this button. This triggers the action sound, but
     * does not cause any change in the button's visualization. *Note:* this does not check
     * the button's enabled state, so the caller must handle that if appropriate.  */
    fun longPress() {
        (_behave as Behavior.Click<Button>).soundAction()
        _longPressed.emit(this)
    }

    /** A convenience method for registering a long press handler. Assumes you don't need the
     * result of [SignalView.connect], because it throws it away.  */
    fun onLongPress(onLongPress: SignalViewListener<Button>): LongPressButton {
        longPressed().connect(onLongPress)
        return this
    }

    override fun createBehavior(): Behavior<Button>? {
        return object : Behavior.Click<Button>(this@LongPressButton) {
            override fun layout() {
                super.layout()
                _longPressInterval = resolveStyle(LONG_PRESS_INTERVAL)
            }

            override fun onPress(iact: Pointer.Interaction) {
                super.onPress(iact)
                if (isSelected) startLongPressTimer()
            }

            override fun onHover(iact: Pointer.Interaction, inBounds: Boolean) {
                super.onHover(iact, inBounds)
                if (!inBounds)
                    cancelLongPressTimer()
                else
                    startLongPressTimer()
            }

            override fun onRelease(iact: Pointer.Interaction): Boolean {
                val click = super.onRelease(iact)
                cancelLongPressTimer()
                return click
            }

            protected fun startLongPressTimer() {
                if (_longPressInterval > 0 && _timerReg === Closeable.Util.NOOP) {
                    var _accum: Int = 0
                    _timerReg = root()!!.iface.frame.connect({ clock: Clock ->
                        _accum += clock.dt
                        if (_accum > _longPressInterval) fireLongPress()
                    })
                }
            }

            protected fun cancelLongPressTimer() {
                _timerReg = Closeable.Util.close(_timerReg)
            }

            protected fun fireLongPress() {
                // cancel the current interaction which will disarm the button
                onCancel(null)
                cancelLongPressTimer()
                longPress()
            }

            protected var _longPressInterval: Int = 0
            protected var _timerReg = Closeable.Util.NOOP
        }
    }

    protected val _longPressed = Signal<Button>()

    companion object {
        /** An interval (in milliseconds) after which pressing and holding on a button will be
         * interpreted as a "long press" and fire a clicked event. The button is then disarmed, so that
         * when the button is released after a long press, a normal click event is not reported.
         * Defaults to 1000ms.  */
        var LONG_PRESS_INTERVAL = Style.newStyle(true, 1000)
    }
}
/** Creates a button with no text or icon.  */
/**  Creates a button with the supplied text.  */
