package klay.core

import klay.core.Keyboard.Event
import react.Slot

/**
 * Defines and dispatches mouse events.
 */
class Mouse {

    /** The base class for all mouse events.  */
    open class Event protected constructor(flags: Int, time: Double, x: Float, y: Float) : klay.core.Event.XY(flags, time, x, y)

    /** The event dispatched for mouse input.  */
    class ButtonEvent(flags: Int, time: Double, x: Float, y: Float,
                      /** The id of the button associated with this event.  */
                      val button: ButtonEvent.Id,
                      /** True if the button was just pressed, false if it was just released.  */
                      var down: Boolean) : Event(flags, time, x, y) {

        /** Enumerates the supported mouse buttons.  */
        enum class Id {
            LEFT, RIGHT, MIDDLE, X1, X2
        }

        override fun name(): String {
            return "Button"
        }

        override fun addFields(builder: StringBuilder) {
            super.addFields(builder)
            builder.append(", id=").append(button).append(", down=").append(down)
        }
    }

    /** An event dispatched when the mouse is moved.  */
    class MotionEvent(flags: Int, time: Double, x: Float, y: Float,
                      /** The amount by which the mouse moved on the x axis.  */
                      val dx: Float,
                      /** The amount by which the mouse moved on the y axis.  */
                      val dy: Float) : Event(flags, time, x, y) {

        override fun name(): String {
            return "MotionEvent"
        }

        override fun addFields(builder: StringBuilder) {
            super.addFields(builder)
            builder.append(", dx=").append(dx).append(", dy=").append(dy)
        }
    }

    /** An event dispatched when the mouse wheel is scrolled.  */
    class WheelEvent(flags: Int, time: Double, x: Float, y: Float,
                     /** The velocity of the scroll wheel. Negative velocity corresponds to scrolling north/up. Each
                      * scroll 'click' is 1 velocity.  */
                     val velocity: Float) : Event(flags, time, x, y) {

        override fun name(): String {
            return "Wheel"
        }

        override fun addFields(builder: StringBuilder) {
            super.addFields(builder)
            builder.append(", velocity=").append(velocity)
        }
    }

    companion object {
        /** Converts a slot of [Event]s to a slot which only dispatches on [ButtonEvent]s.  */
        fun buttonSlot(slot: Slot<ButtonEvent>): Slot<Event> = { if (it is ButtonEvent) slot(it) }

        /** Converts a slot of [Event]s to a slot which only dispatches on [MotionEvent]s.  */
        fun motionSlot(slot: Slot<MotionEvent>): Slot<Event> = { if (it is MotionEvent) slot(it) }

        /** Converts a slot of [Event]s to a slot which only dispatches on [WheelEvent]s.  */
        fun wheelSlot(slot: Slot<WheelEvent>): Slot<Event> = { if (it is WheelEvent) slot(it) }
    }
}
