package tripleklay.ui

import react.SignalView
import react.Slot
import react.Value

/**
 * Displays a checkbox which can be toggled. The checkbox must be configured with either a
 * font-based checkmark, or a checkmark icon, which will be shown when it is checked.
 */
class CheckBox protected constructor(checkChar: Char, protected val _checkIcon: Icon?) : TextWidget<CheckBox>(), Togglable<CheckBox> {

    /** Creates a checkbox with the supplied check character.  */
    @JvmOverloads constructor(checkChar: Char = '\u2713') : this(checkChar, null as Icon?) {}

    constructor(checkIcon: Icon) : this(0.toChar(), checkIcon) {}

    /**
     * Updates the selected state of this checkbox. This method is called when the user taps and
     * releases the checkbox. One can override this method if they want to react to only
     * user-interaction-initiated changes to the checkbox's state (versus listening to
     * [.selected] which can be updated programmatically).
     */
    fun select(selected: Boolean) {
        selected().update(selected)
    }

    override fun selected(): Value<Boolean> {
        return (_behave as Behavior.Toggle<CheckBox>).selected
    }

    override fun clicked(): SignalView<CheckBox> {
        return (_behave as Behavior.Toggle<CheckBox>).clicked
    }

    override fun click() {
        (_behave as Behavior.Toggle<CheckBox>).click()
    }

    override fun toString(): String {
        return "CheckBox(" + text() + ")"
    }

    init {
        _checkStr = checkChar.toString()
        selected().connect(object : Slot<Boolean>() {
            fun onEmit(checked: Boolean?) {
                updateCheckViz(checked!!)
            }
        })
    }

    protected override val styleClass: Class<*>
        get() = CheckBox::class.java

    override fun text(): String? {
        return if (_checkIcon == null) _checkStr else null
    }

    override fun icon(): Icon? {
        return _checkIcon
    }

    override fun createBehavior(): Behavior<CheckBox>? {
        return Behavior.Toggle(asT())
    }

    override fun layout() {
        super.layout()
        updateCheckViz(selected().get())
    }

    protected fun updateCheckViz(isChecked: Boolean) {
        if (_tglyph.layer() != null) _tglyph.layer()!!.setVisible(isChecked)
        if (_ilayer != null) _ilayer!!.setVisible(isChecked)
    }

    protected val _checkStr: String
}
/** Creates a checkbox using the default check glyph: U+2713.  */
