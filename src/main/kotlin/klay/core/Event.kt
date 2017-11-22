package klay.core

/**
 * Defines an event abstraction used in various places.
 */
abstract class Event {

    /** The base for all input events.  */
    open class Input protected constructor(
            /**
             * The flags set for this event. See [.isSet], [.setFlag] and [.clearFlag].
             */
            var flags: Int,
            /**
             * The time at which this event was generated, in milliseconds. This time's magnitude is not
             * portable (i.e. may not be the same across backends), clients must interpret it as only a
             * monotonically increasing value.
             */
            val time: Double) : Event() {

        /** Returns true if the `alt` key was down when this event was generated.  */
        val isAltDown: Boolean
            get() = isSet(F_ALT_DOWN)
        /** Returns true if the `ctrl` key was down when this event was generated.  */
        val isCtrlDown: Boolean
            get() = isSet(F_CTRL_DOWN)
        /** Returns true if the `shift` key was down when this event was generated.  */
        val isShiftDown: Boolean
            get() = isSet(F_SHIFT_DOWN)
        /** Returns true if the `meta` key was down when this event was generated.  */
        val isMetaDown: Boolean
            get() = isSet(F_META_DOWN)

        /** Returns whether the `flag` bit is set.  */
        fun isSet(flag: Int): Boolean {
            return flags and flag != 0
        }

        /** Sets the `flag` bit.  */
        fun setFlag(flag: Int) {
            flags = flags or flag
        }

        /** Clears the `flag` bit.  */
        fun clearFlag(flag: Int) {
            flags = flags and flag.inv()
        }

        /** Sets or clears `flag` based on `on`.  */
        fun updateFlag(flag: Int, on: Boolean) {
            if (on)
                setFlag(flag)
            else
                clearFlag(flag)
        }

        override fun toString(): String {
            val builder = StringBuilder(name()).append('[')
            addFields(builder)
            return builder.append(']').toString()
        }

        protected open fun name(): String {
            return "Input"
        }

        protected open fun addFields(builder: StringBuilder) {
            builder.append("time=").append(time).append(", flags=").append(flags)
        }

        companion object {

            /** A helper function used by platform input code to compose modifier flags.  */
            fun modifierFlags(altP: Boolean, ctrlP: Boolean, metaP: Boolean, shiftP: Boolean): Int {
                var flags = 0
                if (altP) flags = flags or F_ALT_DOWN
                if (ctrlP) flags = flags or F_CTRL_DOWN
                if (metaP) flags = flags or F_META_DOWN
                if (shiftP) flags = flags or F_SHIFT_DOWN
                return flags
            }
        }
    }

    /** The base for all input events with a screen position.  */
    open class XY protected constructor(flags: Int, time: Double,
                                        /** The screen x-coordinate associated with this event.  */
                                        override val x: Float,
                                        /** The screen y-coordinate associated with this event.  */
                                        override val y: Float) : Input(flags, time), euklid.f.XY {

        override fun name(): String {
            return "XY"
        }

        override fun addFields(builder: StringBuilder) {
            super.addFields(builder)
            builder.append(", x=").append(x).append(", y=").append(y)
        }
    }

    companion object {

        /** A flag indicating that the default OS behavior for an event should be prevented.  */
        val F_PREVENT_DEFAULT = 1 shl 0

        protected val F_ALT_DOWN = 1 shl 1
        protected val F_CTRL_DOWN = 1 shl 2
        protected val F_SHIFT_DOWN = 1 shl 3
        protected val F_META_DOWN = 1 shl 4
    }
}
