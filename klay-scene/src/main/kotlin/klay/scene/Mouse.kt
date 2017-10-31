package klay.scene

import pythagoras.f.Point
import react.Slot

/**
 * Integrates the layer system with mouse interactions. To receive mouse events on layers, connect
 * the mouse event dispatcher as a global mouse listener like so:
 * `platform.input().mouseEvents.connect(new Mouse.Dispatcher(...))`.
 */
class Mouse : klay.core.Mouse() {

    /** An event dispatched when the mouse enters or exits a layer.  */
    class HoverEvent(flags: Int, time: Double, x: Float, y: Float,
                     /** Whether the mouse is now inside or outside the layer in question.  */
                     val inside: Boolean) : Event(flags, time, x, y) {

        override fun name(): String {
            return "Hover"
        }

        override fun addFields(builder: StringBuilder) {
            super.addFields(builder)
            builder.append(", inside=").append(inside)
        }
    }

    /** A listener for mouse button, motion and wheel events with layer info.  */
    interface Listener : Slot<Any> {

        /** Notifies listener of a mouse motion event. A motion event is dispatched when no button is
         * currently pressed, in an isolated "one shot" interaction, and always goes to the layer hit
         * by the event coordinates.  */
        fun onMotion(event: MotionEvent, iact: Interaction) {}

        /** Notifies listener of mouse entry or exit. Hover events are dispatched in an isolated "one
         * shot" interaction, regardless of whether there is currently a button-triggered interaction
         * in progress, and always got to the layer whose hover status changed.  */
        fun onHover(event: HoverEvent, iact: Interaction) {}

        /** Notifies listener of a mouse button event. A button down event will start an interaction if
         * no interaction is already in progress, or will be dispatched to the hit layer of the
         * current interaction if an interaction is in progress. If additional buttons are pressed
         * during an interaction, the interaction does not end until *all* of the buttons are
         * released.  */
        fun onButton(event: ButtonEvent, iact: Interaction) {}

        /** Notifies listener of a mouse drag event. A drag event is dispatched when a button event has
         * started an interaction, and always goes to the layer hit by the button event that started
         * the interaction, *not* to the layer intersected by the motion event coordinates.  */
        fun onDrag(event: MotionEvent, iact: Interaction) {}

        /** Notifies listener of a mouse wheel event. If no interaction is in progress, the wheel event
         * is dispatched to the layer intersected by the event coordinates, but if an interaction is
         * in progress, the event goes to the layer hit by the event that started the interaction.  */
        fun onWheel(event: WheelEvent, iact: Interaction) {}

        /** Notifies the listener that the current interaction was canceled. This is dispatched when
         * some other layer that was also privy to this interaction has captured the interaction.  */
        fun onCancel() {}

        override fun invoke(event: Any) {
            if (event is Interaction)
                event.emit(this)
            else if (event === cancelEvent) onCancel()
        }
    }

    /** Used to dispatch mouse interactions to layers.  */
    class Interaction internal constructor(hitLayer: Layer, bubble: Boolean, private val solo: Boolean) : klay.scene.Interaction<Event>(hitLayer, bubble) {

        private var buttons: Int = 0
        internal fun add(button: ButtonEvent.Id) {
            buttons = buttons or (1 shl button.ordinal)
        }

        internal fun remove(button: ButtonEvent.Id): Boolean {
            buttons = buttons and (1 shl button.ordinal).inv()
            return buttons == 0
        }

        internal fun emit(lner: Listener) {
            val mevent = event
            if (mevent is ButtonEvent) {
                lner.onButton(mevent, this)
            } else if (mevent is MotionEvent) {
                if (solo)
                    lner.onMotion(mevent, this)
                else
                    lner.onDrag(mevent, this)
            } else if (mevent is HoverEvent) {
                lner.onHover(mevent, this)
            } else if (mevent is WheelEvent) {
                lner.onWheel(mevent, this)
            }
        }

        override fun newCancelEvent(source: Event?): Event {
            return cancelEvent
        }
    }

    /** Handles the dispatching of mouse events to layers.  */
    class Dispatcher(private val root: Layer, private val bubble: Boolean) : Slot<Event> {
        private val scratch = Point()
        private var currentIact: Interaction? = null
        private var hoverLayer: Layer? = null

        override fun invoke(event: Event) {
            if (event is ButtonEvent) {
                val bevent = event
                if (bevent.down) {
                    // if we have no current interaction, start one
                    if (currentIact == null) {
                        val hitLayer = LayerUtil.getHitLayer(root, scratch.set(event.x, event.y))
                        if (hitLayer != null) currentIact = Interaction(hitLayer, bubble, false)
                    }
                    if (currentIact != null) {
                        currentIact!!.add(bevent.button)
                        currentIact!!.dispatch(event)
                    }
                } else if (currentIact == null)
                    dispatchSolo(event)
                else {
                    val done = currentIact!!.remove(bevent.button)
                    currentIact!!.dispatch(event)
                    if (done) currentIact = null
                }// otherwise dispatch this mouse up to the current interaction and end it if there are no
                // longer any buttons pressed therein
                // if we have no current interaction, that's weird, but maybe the app somehow missed the
                // button down event, so just dispatch this event solo

            } else if (event is MotionEvent) {
                // we always compute the hit layer because we need to hover events
                val hitLayer = LayerUtil.getHitLayer(root, scratch.set(event.x, event.y))
                // if we have a current interaction, dispatch a drag event
                if (currentIact != null)
                    currentIact!!.dispatch(event)
                else if (hitLayer != null) Interaction(hitLayer, bubble, true).dispatch(event)// otherwise dispatch the mouse motion event solo

                // dispatch hover events if the hit layer changed
                if (hitLayer !== hoverLayer) {
                    if (hoverLayer != null) {
                        val hevent = HoverEvent(0, event.time, event.x, event.y, false)
                        Interaction(hoverLayer!!, bubble, true).dispatch(hevent)
                    }
                    hoverLayer = hitLayer
                    if (hitLayer != null) {
                        val hevent = HoverEvent(0, event.time, event.x, event.y, true)
                        Interaction(hitLayer, bubble, true).dispatch(hevent)
                    }
                }

            } else if (event is WheelEvent) {
                // if we have a current interaction, dispatch to that
                if (currentIact != null)
                    currentIact!!.dispatch(event)
                else
                    dispatchSolo(event)// otherwise create a one-shot interaction and dispatch it
            }
        }

        private fun dispatchSolo(event: Event) {
            val hitLayer = LayerUtil.getHitLayer(root, scratch.set(event.x, event.y))
            if (hitLayer != null) Interaction(hitLayer, bubble, true).dispatch(event)
        }
    }

    class CancelEvent : Event(0, 0.0, 0f, 0f)

    companion object {
        private val cancelEvent = CancelEvent()
    }
}
