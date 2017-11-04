package klay.core

import react.RFuture
import react.Signal

/**
 * Provides information about user input: mouse, touch, and keyboard. This class provides the
 * platform-specific code, and events are dispatched via the platform-independent [Mouse],
 * [Touch] and [Keyboard] classes.
 */
open class Input(open val plat: Platform) {

    /** Enables or disables mouse interaction.
     * No mouse events will be dispatched whilst this big switch is in the off position.  */
    var mouseEnabled = true

    /** Enables or disables touch interaction.
     * No touch events will be dispatched whilst this big switch is in the off position.  */
    var touchEnabled = true

    /** Enables or disables keyboard interaction.
     * No keyboard events will be dispatched whilst this big switch is in the off position.  */
    var keyboardEnabled = true

    /** A signal which emits mouse events.  */
    var mouseEvents: Signal<Mouse.Event> = Signal()

    /** A signal via which touch events are emitted.  */
    var touchEvents: Signal<Array<Touch.Event>> = Signal()

    /** A signal via which keyboard events are emitted.  */
    var keyboardEvents: Signal<Keyboard.Event> = Signal()

    /** Returns true if this platform has mouse input.  */
    open val hasMouse: Boolean = false

    /** Returns true if this platform has touch input.  */
    open val hasTouch: Boolean = false

    /**
     * Returns true if this device has a hardware keyboard, false if not. Devices that lack a
     * hardware keyboard will generally not generate keyboard events. Older Android devices that
     * support four hardware buttons are an exception. Use [.getText] for text entry on a
     * non-hardware-keyboard having device.
     */
    open val hasHardwareKeyboard: Boolean = false

    /**
     * Returns true if this platform supports mouse locking. The user may still block it when it is
     * requested, or detection may be broken for some browsers.
     */
    open val hasMouseLock: Boolean = false

    /**
     * Returns whether the mouse is currently locked.
     */
    /**
     * Lock or unlock the mouse. When the mouse is locked, mouse events are still received even when
     * the pointer leaves the game window.
     */
    open var isMouseLocked: Boolean
        get() = false
        set(locked) {} // noop!

    /**
     * Requests a line of text from the user. On platforms that have only a virtual keyboard, this
     * will display a text entry interface, obtain the line of text, and dismiss the text entry
     * interface when finished.

     * @param textType the expected type of text. On mobile devices this hint may be used to display a
     * * keyboard customized to the particular type of text.
     * *
     * @param label a label to display over the text entry interface, may be null.
     * *
     * @param initialValue the initial value to display in the text input field, may be null.
     * *
     * *
     * @return a future which provides the text when it becomes available. If the user cancels the
     * * text entry process, null is supplied. Otherwise the entered text is supplied.
     */
    open fun getText(textType: Keyboard.TextType, label: String, initialValue: String): RFuture<String> {
        return RFuture.failure(Exception("getText not supported"))
    }

    /**
     * Displays a system dialog with the specified title and text, an OK button and optionally a
     * Cancel button.

     * @param title the title for the dialog window. Note: some platforms (mainly mobile) do not
     * * display the title, so be sure your dialog makes sense if only `text` is showing.
     * *
     * @param text the text of the dialog. The text will be wrapped by the underlying platform, but
     * * Klay will do its utmost to ensure that newlines are honored by the platform in question so
     * * that hard line breaks and blank lines are reproduced correctly.
     * *
     * @param ok the text of the button which will deliver a `true` result and be placed in
     * * "OK" position for the platform. Note: the HTML platform does not support customizing this
     * * label, so on that platform the label will be "OK". Yay for HTML5.
     * *
     * @param cancel the text of the button that will deliver a `false` result and be placed in
     * * "Cancel" position. If `null` is supplied, the dialog will only have an OK button. Note:
     * * the HTML platform does not support customizing this label, so on that platform a non-null
     * * cancel string will result in the button reading "Cancel". Yay for HTML5.
     * *
     * *
     * @return a future which delivers `true` or `false` when the user clicks the OK or
     * * cancel buttons respectively. If some unexpected error occurs displaying the dialog (unlikley),
     * * it will be reported by failing the future.
     */
    open fun sysDialog(title: String, text: String, ok: String, cancel: String?): RFuture<Boolean> {
        return RFuture.failure(Exception("sysDialog not supported"))
    }

    protected fun modifierFlags(altP: Boolean, ctrlP: Boolean, metaP: Boolean, shiftP: Boolean): Int {
        return Event.Input.modifierFlags(altP, ctrlP, metaP, shiftP)
    }

    protected fun emitKeyPress(time: Double, key: Key, down: Boolean, flags: Int) {
        val event = Keyboard.KeyEvent(0, time, key, down)
        event.setFlag(flags)
        plat.dispatchEvent(keyboardEvents, event)
    }

    protected fun emitKeyTyped(time: Double, keyChar: Char) {
        plat.dispatchEvent(keyboardEvents, Keyboard.TypedEvent(0, time, keyChar))
    }

    protected fun emitMouseButton(time: Double, x: Float, y: Float, btn: Mouse.ButtonEvent.Id,
                                  down: Boolean, flags: Int) {
        val event = Mouse.ButtonEvent(0, time, x, y, btn, down)
        event.setFlag(flags)
        plat.dispatchEvent(mouseEvents, event)
    }

    protected fun emitMouseMotion(time: Double, x: Float, y: Float, dx: Float, dy: Float, flags: Int) {
        val event = Mouse.MotionEvent(0, time, x, y, dx, dy)
        event.setFlag(flags)
        plat.dispatchEvent(mouseEvents, event)
    }

    protected fun emitMouseWheel(time: Double, x: Float, y: Float, delta: Int, flags: Int) {
        val event = Mouse.WheelEvent(0, time, x, y, delta.toFloat())
        event.setFlag(flags)
        plat.dispatchEvent(mouseEvents, event)
    }
}
