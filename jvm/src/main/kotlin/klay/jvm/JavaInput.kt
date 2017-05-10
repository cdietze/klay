package klay.jvm

import klay.core.*
import pythagoras.f.Point
import java.util.concurrent.ConcurrentLinkedDeque

open class JavaInput(plat: JavaPlatform) : Input(plat) {

    override val plat: JavaPlatform = plat

    // used for injecting keyboard evnets
    private val kevQueue = ConcurrentLinkedDeque<Keyboard.Event>()

    // these are used for touch emulation
    private var mouseDown: Boolean = false
    private var pivot: Point? = null
    private var x: Float = 0.toFloat()
    private var y: Float = 0.toFloat()
    private var currentId: Int = 0

    init {
        // if touch emulation is configured, wire it up
        if (plat.config.emulateTouch) emulateTouch()
    }

    /** Posts a key event received from elsewhere (i.e. a native UI component). This is useful for
     * applications that are using GL in Canvas mode and sharing keyboard focus with other (non-GL)
     * components. The event will be queued and dispatched on the next frame, after GL keyboard
     * events.

     * @param time the time (in millis since epoch) at which the event was generated, or 0 if N/A.
     * *
     * @param key the key that was pressed or released, or null for a char typed event
     * *
     * @param pressed whether the key was pressed or released, ignored if key is null
     * *
     * @param typedCh the character that was typed, ignored if key is not null
     * *
     * @param modFlags modifier key state flags (generated by [.modifierFlags])
     */
    fun postKey(time: Long, key: Key?, pressed: Boolean, typedCh: Char, modFlags: Int) {
        val event = if (key == null)
            Keyboard.TypedEvent(0, time.toDouble(), typedCh)
        else
            Keyboard.KeyEvent(0, time.toDouble(), key, pressed)
        event.setFlag(modFlags)
        kevQueue.add(event)
    }

    protected fun emulateTouch() {
        val pivotKey = plat.config.pivotKey
        keyboardEvents.connect { event: Keyboard.Event ->
            if (event is Keyboard.KeyEvent) {
                val kevent = event as Keyboard.KeyEvent
                if (kevent.key === pivotKey && kevent.down) {
                    pivot = Point(x, y)
                }
            }
        }

        mouseEvents.connect { event: Mouse.Event ->
            if (event is Mouse.ButtonEvent) {
                val bevent = event as Mouse.ButtonEvent
                if (bevent.button === Mouse.ButtonEvent.Id.LEFT) {
                    mouseDown = bevent.down
                    if (mouseDown) {
                        currentId += 2 // skip an id in case of pivot
                        dispatchTouch(event, Touch.Event.Kind.START)
                    } else {
                        pivot = null
                        dispatchTouch(event, Touch.Event.Kind.END)
                    }
                }
            } else if (event is Mouse.MotionEvent) {
                if (mouseDown) dispatchTouch(event, Touch.Event.Kind.MOVE)
                // keep track of the current mouse position for pivot
                x = event.x
                y = event.y
            }
        }

        // TODO: it's pesky that both mouse and touch events are dispatched when touch is emulated, it
        // would be nice to throw away the mouse events and only have touch, but we rely on something
        // generating the mouse events so we can't throw them away just yet... plus it could be useful
        // to keep wheel events... blah
    }

    override val hasMouse = true

    override val hasHardwareKeyboard = true

    override val hasTouch: Boolean
        get() = plat.config.emulateTouch

    internal open fun update() {
        // dispatch any queued keyboard events
        while (true) {
            val kev: Keyboard.Event = kevQueue.poll() ?: break
            plat.dispatchEvent(keyboardEvents, kev)
        }
    }

    private fun dispatchTouch(event: Mouse.Event, kind: Touch.Event.Kind) {
        val ex = event.x
        val ey = event.y
        val main = toTouch(event.time, ex, ey, kind, 0)
        val evs = if (pivot == null)
            arrayOf<Touch.Event>(main)
        else
            arrayOf<Touch.Event>(main, toTouch(event.time, 2 * pivot!!.x - ex, 2 * pivot!!.y - ey, kind, 1))
        plat.dispatchEvent(touchEvents, evs)
    }

    private fun toTouch(time: Double, x: Float, y: Float, kind: Touch.Event.Kind, idoff: Int): Touch.Event {
        return Touch.Event(0, time, x, y, kind, currentId + idoff)
    }
}
