package tripleklay.ui

import react.Closeable
import react.Value
import react.ValueView

/**
 * An abstract base class for buttons with text labels.
 */
abstract class AbstractTextButton<T : AbstractTextButton<T>> protected constructor(text: String?, icon: Icon?) : TextWidget<T>() {
    /** The text displayed by this button, or null.  */
    val text = Value<String?>(null)

    /** The icon displayed by this button, or null.  */
    val icon = Value<Icon?>(null)

    /**
     * Binds the text of this button to the supplied reactive value. The current text will be
     * adjusted to match the state of `text`.
     */
    fun bindText(textV: ValueView<String>): T {
        return addBinding(object : Element.Binding(_bindings) {
            override fun connect(): Closeable {
                return textV.connectNotify(text.slot())
            }

            override fun toString(): String {
                return this@AbstractTextButton.toString() + ".bindText"
            }
        })
    }

    /**
     * Binds the icon of this button to the supplied reactive value. The current icon will be
     * adjusted to match the state of `icon`.
     */
    fun bindIcon(iconV: ValueView<Icon>): T {
        return addBinding(object : Element.Binding(_bindings) {
            override fun connect(): Closeable {
                return iconV.connectNotify(icon.slot())
            }

            override fun toString(): String {
                return this@AbstractTextButton.toString() + ".bindIcon"
            }
        })
    }

    /** Updates the text displayed by this button.  */
    fun setText(text: String): T {
        this.text.update(text)
        return asT()
    }

    /** Updates the icon displayed by this button.  */
    fun setIcon(icon: Icon): T {
        this.icon.update(icon)
        return asT()
    }

    init {
        this.text.update(text)
        this.text.connect(textDidChange())
        // update after connect so we trigger iconDidChange, in case our icon is a not-ready-image
        this.icon.connect(iconDidChange())
        this.icon.update(icon)
    }

    override fun text(): String? {
        return text.get()
    }

    override fun icon(): Icon? {
        return icon.get()
    }
}
