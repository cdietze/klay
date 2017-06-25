package tripleklay.ui

import react.SignalView
import react.Value

/**
 * A toggle button that displays text, or an icon, or both. Clicking the button toggles it from
 * selected to unselected, and vice versa.
 */
class ToggleButton
/** Creates a button with the supplied text and icon.  */
@JvmOverloads constructor(text: String? = null, icon: Icon? = null) : AbstractTextButton<ToggleButton>(text, icon), Togglable<ToggleButton> {

    /** Creates a button with the supplied icon.  */
    constructor(icon: Icon) : this(null, icon)

    override fun selected(): Value<Boolean> {
        return (_behave as Behavior.Toggle<ToggleButton>).selected
    }

    override fun clicked(): SignalView<ToggleButton> {
        return (_behave as Behavior.Toggle<ToggleButton>).clicked
    }

    override fun click() {
        (_behave as Behavior.Toggle<ToggleButton>).click()
    }

    override fun toString(): String {
        return "ToggleButton(" + text() + ")"
    }

    override val styleClass: Class<*>
        get() = ToggleButton::class.java

    override fun createBehavior(): Behavior<ToggleButton>? {
        return Behavior.Toggle(asT())
    }
}
/** Creates a button with no text or icon.  */
/**  Creates a button with the supplied text.  */
