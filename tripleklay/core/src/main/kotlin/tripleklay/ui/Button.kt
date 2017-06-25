package tripleklay.ui

import react.SignalView
import react.SignalViewListener

/**
 * A button that displays text, or an icon, or both.
 */
open class Button
/** Creates a button with the supplied text and icon.  */
@JvmOverloads constructor(text: String? = null, icon: Icon? = null) : AbstractTextButton<Button>(text, icon), Clickable<Button> {

    /** Creates a button with the supplied icon.  */
    constructor(icon: Icon) : this(null, icon)

    /** A convenience method for registering a click handler. Assumes you don't need the result of
     * [SignalView.connect], because it throws it away.  */
    fun onClick(onClick: SignalViewListener<Button>): Button {
        clicked().connect(onClick)
        return this
    }

    override fun clicked(): SignalView<Button> {
        return (_behave as Behavior.Click<Button>).clicked
    }

    override fun click() {
        (_behave as Behavior.Click<Button>).click()
    }

    override fun toString(): String {
        return "Button(" + text() + ")"
    }

    override val styleClass: Class<*>
        get() = Button::class.java

    override fun createBehavior(): Behavior<Button>? {
        return Behavior.Click(this)
    }
}
/** Creates a button with no text or icon.  */
/**  Creates a button with the supplied text.  */
