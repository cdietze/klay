package tripleklay.ui

import react.Closeable
import react.Value
import react.ValueView
import kotlin.reflect.KClass

/**
 * A widget that displays one or more lines of text and/or an icon image.
 */
open class Label
/** Creates a label with the supplied text and icon.  */
constructor(text: String? = null, icon: Icon? = null) : TextWidget<Label>() {
    /** The text displayed by this widget, or null.  */
    val text = Value<String?>(null)

    /** The icon displayed by this widget, or null.  */
    val icon = Value<Icon?>(null)

    /** Creates a label with the supplied icon.  */
    constructor(icon: Icon) : this(null, icon)

    init {
        this.text.update(text)
        this.text.connect(textDidChange())
        // update after connect so we trigger iconDidChange, in case our icon is a not-ready-image
        this.icon.connect(iconDidChange())
        this.icon.update(icon)
    }

    /** Creates a label and calls [.bindText] with `text`.  */
    constructor(text: ValueView<*>) : this(null, null) {
        bindText(text)
    }

    /**
     * Binds the text of this label to the supplied reactive value. The current text will be
     * adjusted to match the state of `text`.
     */
    fun bindText(textV: ValueView<*>): Label {
        return addBinding(object : Element.Binding(_bindings) {
            override fun connect(): Closeable {
                return textV.map { it.toString() }.connectNotify(text.slot())
            }

            override fun toString(): String {
                return this@Label.toString() + ".bindText"
            }
        })
    }

    /**
     * Binds the icon of this label to the supplied reactive value. The current icon will be
     * adjusted to match the state of `icon`.
     */
    fun bindIcon(iconV: ValueView<Icon>): Label {
        return addBinding(object : Element.Binding(_bindings) {
            override fun connect(): Closeable {
                return iconV.connectNotify(icon.slot())
            }

            override fun toString(): String {
                return this@Label.toString() + ".bindIcon"
            }
        })
    }

    /** Updates the text displayed by this label.  */
    fun setText(text: String): Label {
        this.text.update(text)
        return this
    }

    /** Updates the icon displayed by this label.  */
    fun setIcon(icon: Icon): Label {
        this.icon.update(icon)
        return this
    }

    override fun toString(): String {
        return "Label(" + text.get() + ")"
    }

    override val styleClass: KClass<*>
        get() = Label::class

    override fun text(): String? {
        return text.get()
    }

    override fun icon(): Icon? {
        return icon.get()
    }
}
/** Creates a label with no text or icon.  */
/**  Creates a label with the supplied text.  */
