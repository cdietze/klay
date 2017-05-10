package klay.core

/**
 * Defines and dispatches touch events.
 */
open class Touch {

    /** Contains information on a touch event.  */
    class Event @JvmOverloads constructor(flags: Int, time: Double, x: Float, y: Float,
                                          /** Whether this event represents a start, move, etc.  */
                                          val kind: Event.Kind,
                                          /** The id of the touch associated with this event.  */
                                          val id: Int,
                                          /** The pressure of the touch.  */
                                          val pressure: Float = -1f,
            // TODO(mdb): provide guidance as to range in the docs? 0 to 1?

                                          /** The size of the touch.  */
                                          val size: Float = -1f) : klay.core.Event.XY(flags, time, x, y) {

        /** Enumerates the different kinds of touch event.  */
        enum class Kind private constructor(
                // NOTE: this enum order must match Pointer.Event.Kind exactly

                /** Whether this touch kind starts or ends an interaction.  */
                val isStart: Boolean, val isEnd: Boolean) {
            START(true, false), MOVE(false, false), END(false, true), CANCEL(false, true)
        }

        override fun name(): String {
            return "Touch"
        }

        override fun addFields(builder: StringBuilder) {
            super.addFields(builder)
            builder.append(", kind=").append(kind).append(", id=").append(id).append(", pressure=").append(pressure).append(", size=").append(size)
        }
    }// TODO(mdb): provide more details in the docs? size in pixels?
    // TODO: Implement pressure and size across all platforms that support touch.
}
