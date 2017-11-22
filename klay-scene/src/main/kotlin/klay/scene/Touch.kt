package klay.scene

import euklid.f.Point
import react.Slot

/**
 * Integrates the layer system with touch interactions. To receive touch events on layers, connect
 * the touch event dispatcher as a global touch listener like so:
 * `platform.input().touchEvents.connect(new Touch.Dispatcher(...))`.
 */
class Touch : klay.core.Touch() {

    /** A listener for touch events with layer info.  */
    interface Listener : Slot<Any> {

        /** Notifies listener of a touch start event.  */
        fun onStart(iact: Interaction) {}

        /** Notifies listener of a touch move event.  */
        fun onMove(iact: Interaction) {}

        /** Notifies listener of a touch end event.  */
        fun onEnd(iact: Interaction) {}

        /** Notifies listener of a touch cancel event.  */
        fun onCancel(iact: Interaction) {}

        override fun invoke(event: Any) {
            if (event is Interaction) {
                when (event.event!!.kind) {
                    Event.Kind.START -> onStart(event)
                    Event.Kind.MOVE -> onMove(event)
                    Event.Kind.END -> onEnd(event)
                    Event.Kind.CANCEL -> onCancel(event)
                    else -> {
                    }
                }
            }
        }
    }

    /** Used to dispatch touch interactions to layers.  */
    class Interaction internal constructor(hitLayer: Layer, bubble: Boolean) : klay.scene.Interaction<Event>(hitLayer, bubble) {

        override fun newCancelEvent(source: Event?): Event {
            return if (source == null)
                Event(0, 0.0, 0f, 0f, Event.Kind.CANCEL, 0)
            else
                Event(0, source.time, source.x, source.y, Event.Kind.CANCEL,
                        source.id, source.pressure, source.size)
        }
    }

    /** Handles the dispatching of touch events to layers.  */
    class Dispatcher(private val root: Layer, private val bubble: Boolean) : Slot<Array<Event>> {
        private val scratch = Point()
        private val activeIacts = HashMap<Int, Interaction>()

        override fun invoke(events: Array<Event>) {
            // each event has an id which defines the interaction of which it is a part
            for (event in events) {
                // start a new interaction for this id if START and we don't already have one
                var iact: Interaction? = activeIacts[event.id]
                if (iact == null && event.kind.isStart) {
                    val hitLayer = LayerUtil.getHitLayer(root, scratch.set(event.x, event.y))
                    if (hitLayer != null) {
                        iact = Interaction(hitLayer, bubble)
                        activeIacts.put(event.id, iact)
                    }
                }

                // dispatch the event to the interaction
                if (iact != null) iact.dispatch(event)

                // if this is END or CANCEL, clear out the interaction for this id
                if (event.kind.isEnd) activeIacts.remove(event.id)
            }
        }
    }
}
