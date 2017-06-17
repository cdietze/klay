package tripleklay.ui

import react.Closeable
import react.ValueView

@Deprecated("")
@Deprecated("Use {@link Label#Label(ValueView)}.")
class ValueLabel
/** Creates a label with the supplied value. The value will be converted to a string for
 * display as this label's text.  */
(
        /** The source for the text of this label.  */
        val text: ValueView<*>) : TextWidget<ValueLabel>() {

    override fun wasAdded() {
        super.wasAdded()
        _conn = text.connect(textDidChange())
    }

    override fun wasRemoved() {
        super.wasRemoved()
        _conn.close()
    }

    override fun toString(): String {
        return "VLabel(" + text.get() + ")"
    }

    protected override val styleClass: Class<*>
        get() = Label::class.java

    override fun text(): String? {
        return text.get().toString()
    }

    override fun icon(): Icon? {
        return null
    }

    protected var _conn: Closeable
}
