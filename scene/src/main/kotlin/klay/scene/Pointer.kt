package klay.scene

import klay.core.Platform
import klay.scene.Pointer.Dispatcher
import pythagoras.f.Point
import react.Slot

/**
 * Integrates the layer system with pointer interactions. This supercedes the
 * [klay.core.Pointer] service. So you simply create a scene `Pointer` instead of a
 * core `Pointer`, and it will dispatch both global and layer-local pointer interactions.
 */
class Pointer
/**
 * Creates a pointer event system which dispatches both global pointer events and per-layer
 * pointer events.

 * @param bubble if true, events are "bubbled" up the layer hierarchy, if false they are
 * * delivered only to the hit layer. See [Dispatcher] for details.
 */
(plat: Platform, root: Layer, bubble: Boolean) : klay.core.Pointer(plat) {

    /** A listener for pointer events with layer info.  */
    abstract class Listener : Slot<Any> {

        /** Notifies listener of a pointer start event.  */
        open fun onStart(iact: Interaction) {}

        /** Notifies listener of a pointer drag (move) event.  */
        open fun onDrag(iact: Interaction) {}

        /** Notifies listener of a pointer end event.  */
        open fun onEnd(iact: Interaction) {}

        /** Notifies listener of a pointer cancel event.  */
        open fun onCancel(iact: Interaction) {}

        override fun invoke(event: Any) {
            if (event is Interaction) {
                when (event.event!!.kind) {
                    Event.Kind.START -> onStart(event)
                    Event.Kind.DRAG -> onDrag(event)
                    Event.Kind.END -> onEnd(event)
                    Event.Kind.CANCEL -> onCancel(event)
                }
            }
        }
    }

    /** Used to dispatch pointer interactions to layers.  */
    class Interaction internal constructor(hitLayer: Layer, bubble: Boolean) : klay.scene.Interaction<Event>(hitLayer, bubble) {

        override fun newCancelEvent(source: Event?): Event {
            return if (source == null)
                Event(0, 0.0, 0f, 0f, Event.Kind.CANCEL, false)
            else
                Event(0, source!!.time, source!!.x, source!!.y, Event.Kind.CANCEL, source!!.isTouch)
        }
    }

    /** Handles the dispatching of pointer events to layers.  */
    class Dispatcher(private val root: Layer, private val bubble: Boolean) : Slot<Event> {
        private val scratch = Point()
        private var currentIact: Interaction? = null

        override fun invoke(event: Event) {
            // start a new interaction on START, if we don't already have one
            if (currentIact == null && event.kind.isStart) {
                val hitLayer = LayerUtil.getHitLayer(root, scratch.set(event.x, event.y))
                if (hitLayer != null) currentIact = Interaction(hitLayer, bubble)
            }
            // dispatch the event to the interaction
            if (currentIact != null) currentIact!!.dispatch(event)
            // if this is END or CANCEL, clear out the current interaction
            if (event.kind.isEnd) currentIact = null
        }
    }

    init {
        events.connect(Dispatcher(root, bubble))
    }
}
