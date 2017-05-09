package klay.core

import react.Signal

/**
 * Abstracts over [Mouse] and [Touch] input, providing a least-common-denominator API
 * which tracks a single "pointer" with simple interactions. If you want global pointer events,
 * you have to create an instance of this class yourself.
 */
class Pointer(private val plat: Platform) {

    /** Contains information on a pointer event.  */
    class Event(flags: Int, time: Double, x: Float, y: Float,
            // NOTE: this enum must match Touch.Event.Kind exactly

                /** Whether this event represents a start, move, etc.  */
                val kind: Event.Kind,
                /** Whether this event originated from a touch event.  */
                var isTouch: Boolean) : klay.core.Event.XY(flags, time, x, y) {

        /** Enumerates the different kinds of pointer event.  */
        enum class Kind private constructor(
                /** Whether this kind starts or ends an interaction.  */
                val isStart: Boolean, val isEnd: Boolean) {
            START(true, false), DRAG(false, false), END(false, true), CANCEL(false, true)
        }

        override fun name(): String {
            return "Pointer"
        }

        override fun addFields(builder: StringBuilder) {
            super.addFields(builder)
            builder.append(", kind=").append(kind)
            builder.append(", touch=").append(isTouch)
        }
    }

    /** Allows pointer interaction to be temporarily disabled.
     * No pointer events will be dispatched whilst this big switch is in the off position.  */
    var enabled = true

    /** A signal which emits pointer events.  */
    var events: Signal<Event> = Signal.create()

    init {

        // if this platform supports touch events, use those
        if (plat.input.hasTouch) {
            var active = -1
            plat.input.touchEvents.connect { events: Array<Touch.Event> ->
                for (event in events) {
                    if (active == -1 && event.kind.isStart) active = event.id
                    if (event.id == active) {
                        forward(Event.Kind.values()[event.kind.ordinal], true, event)
                        if (event.kind.isEnd) active = -1
                    }
                }
            }
        } else if (plat.input.hasMouse) {
            var dragging: Boolean = false
            plat.input.mouseEvents.connect { event: Mouse.Event ->
                if (event is Mouse.MotionEvent) {
                    if (dragging) forward(Event.Kind.DRAG, false, event)
                } else if (event is Mouse.ButtonEvent) {
                    val bevent = event
                    if (bevent.button == Mouse.ButtonEvent.Id.LEFT) {
                        dragging = bevent.down
                        forward(if (bevent.down) Event.Kind.START else Event.Kind.END, false, bevent)
                    }
                }
            }
        } else
            plat.log.warn("Platform has neither mouse nor touch events? [type=${plat.type()}]")// otherwise complain because what's going on?
        // otherwise use mouse events if it has those
    }

    protected fun forward(kind: Event.Kind, isTouch: Boolean, source: klay.core.Event.XY) {
        if (!enabled || !events.hasConnections()) return
        val event = Event(source.flags, source.time, source.x, source.y, kind, isTouch)
        plat.dispatchEvent(events, event)
        // TODO: propagate prevent default back to original event
    }
}
