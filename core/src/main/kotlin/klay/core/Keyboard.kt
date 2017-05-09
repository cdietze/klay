package klay.core

import klay.core.Keyboard.KeyEvent
import klay.core.Keyboard.TypedEvent
import react.Slot

/**
 * Defines and dispatches keyboard events. Three events are generated by keyboard input:
 *
 *  *  When any key is depressed, a [KeyEvent] is emitted indicating the logical key that
 * was depressed.
 *  *  If the depressed key also corresponds to a printable character ('c' for example, but not
 * shift or alt), a [TypedEvent] is emitted to inform the app of the typed character. The
 * typed character will account for whether the shift key is depressed and will be appropriately
 * mapped to the uppercase equivalent or the appropriate alternate character (for example, # for 3,
 * in the US keyboard layout). The typed event is delivered immediately after the pressed event.
 *
 *  *  When a key is released, a [KeyEvent] is emitted, indicating the logical key that was
 * released.
 *
 */
abstract class Keyboard {

    /** The base class for all keyboard events.  */
    open class Event protected constructor(flags: Int, time: Double) : klay.core.Event.Input(flags, time)

    /** An event dispatched for key press/release.  */
    class KeyEvent(flags: Int, time: Double,
                   /** The key that triggered this event, e.g. [Key.A], etc.  */
                   val key: Key,
                   /** Whether the key is down or up.  */
                   val down: Boolean) : Event(flags, time) {

        override fun name(): String {
            return "Key"
        }

        override fun addFields(builder: StringBuilder) {
            super.addFields(builder)
            builder.append(", key=").append(key).append(", down=").append(down)
        }
    }

    /** An event dispatched when a printable character is typed.  */
    class TypedEvent(flags: Int, time: Double,
                     /** The character typed to trigger this event, e.g. 'c'.  */
                     var typedChar: Char) : Event(flags, time) {

        override fun name(): String {
            return "Typed"
        }

        override fun addFields(builder: StringBuilder) {
            super.addFields(builder)
            builder.append(", typedChar=").append(typedChar)
        }
    }

    companion object {
        /** Converts a slot of [Event]s to a slot which only dispatches on [KeyEvent]s.  */
        fun keySlot(slot: Slot<KeyEvent>): Slot<Event> = { if (it is KeyEvent) slot(it) }

        /** Converts a slot of [Event]s to a slot which only dispatches on [TypedEvent]s.  */
        fun typedSlot(slot: Slot<TypedEvent>): Slot<Event> = { if (it is TypedEvent) slot(it) }

    }

    /** Enumerates the different available mobile keyboard types. See [Input.getText].  */
    enum class TextType {
        DEFAULT, NUMBER, EMAIL, URL
    }
}
