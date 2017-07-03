package tripleklay.ui

import klay.core.Input
import klay.core.Keyboard
import klay.scene.LayerUtil
import klay.scene.Pointer
import pythagoras.f.Rectangle
import react.Signal
import react.SignalView
import react.Value
import tripleklay.platform.NativeTextField
import tripleklay.platform.TPPlatform

/**
 * Displays text which can be edited via the [Input.getText] popup.
 */
class Field @JvmOverloads constructor(initialText: String = "", styles: Styles = Styles.none()) : TextWidget<Field>() {

    /** Exposes protected field information required for native fields.  */
    inner class Native {
        /** Resolves the given style for the field.  */
        fun <T> resolveStyle(style: Style<T>): T {
            return this@Field.resolveStyle(style)
        }

        /** Tests if the proposed text is valid.  */
        fun isValid(text: String): Boolean {
            return this@Field.textIsValid(text)
        }

        /** Transforms the given text.  */
        fun transform(text: String): String {
            return this@Field.transformText(text)
        }

        /** A signal that is dispatched when the native text field has lost focus. Value is false if
         * editing was canceled  */
        fun finishedEditing(): Signal<Boolean> {
            return _finishedEditing
        }

        /** Refreshes the bounds of this field's native field. Used as a platform callback to
         * support some degree of animation for UI containing native fields.  */
        fun refreshBounds() {
            updateNativeFieldBounds()
        }

        fun field(): Field {
            return this@Field
        }
    }

    /** For native text fields, decides whether to block a keypress based on the proposed content
     * of the field.  */
    interface Validator {
        /** Return false if the keypress causing this text should be blocked.  */
        fun isValid(text: String): Boolean
    }

    /** For native text fields, transforms text during typing.  */
    interface Transformer {
        /** Transform the specified text.  */
        fun transform(text: String): String
    }

    /** Blocks keypresses for a native text field when the length is at a given maximum.  */
    class MaxLength(
            /** The maximum length accepted.  */
            val max: Int) : Validator {
        override fun isValid(text: String): Boolean {
            return text.length <= max
        }
    }

    /** The text displayed by this widget.  */
    val text: Value<String>

    private var _nativeField: NativeTextField? = null
    private var _validator: Validator? = null
    private var _transformer: Transformer? = null
    private var _textType: Keyboard.TextType? = null
    private var _fullTimeNative: Boolean = false
    private val _finishedEditing: Signal<Boolean>

    // used when popping up a text entry interface on mobile platforms
    private var _popupLabel: String? = null

    constructor(styles: Styles) : this("", styles)

    init {
        setStyles(styles)

        text = Value("")
        _finishedEditing = Signal.create()

        if (hasNative()) {
            _finishedEditing.connect({ if (!_fullTimeNative) updateMode(false) })
        }

        this.text.update(initialText)
        this.text.connect(textDidChange())
    }

    /** Returns a signal that is dispatched when text editing is complete.  */
    fun finishedEditing(): SignalView<Boolean> {
        return _finishedEditing
    }

    /**
     * Configures the label to be displayed when text is requested via a popup.
     */
    fun setPopupLabel(label: String): Field {
        _popupLabel = label
        return this
    }

    /**
     * Forcibly notify the NativeTextField backing this field that its screen position has changed.

     * @return this for call chaining.
     */
    fun updateNativeFieldBounds(): Field {
        if (_nativeField != null) _nativeField!!.setBounds(nativeFieldBounds)
        return this
    }

    /** Attempt to focus on this field, if it is backed by a native field. If the platform
     * uses a virtual keyboard, this will cause it slide up, just as though the use had tapped
     * the field. For hardware keyboard, a blinking caret will appear in the field.  */
    fun focus() {
        if (_nativeField != null) _nativeField!!.focus()
    }

    override fun setVisible(visible: Boolean): Field {
        if (_nativeField != null) {
            if (visible) {
                _nativeField!!.add()
            } else {
                _nativeField!!.remove()
            }
        }
        return super.setVisible(visible)
    }

    /** Returns this field's native text field, if it has one, otherwise null.  */
    fun exposeNativeField(): NativeTextField? {
        return _nativeField
    }

    /**
     * Main entry point for deciding whether to reject keypresses on a native field. By default,
     * consults the current validator instance, set up by [.VALIDATOR].
     */
    private fun textIsValid(text: String): Boolean {
        return _validator == null || _validator!!.isValid(text)
    }

    /**
     * Called when the native field's value is changed. Override and return a modified value to
     * perform text transformation while the user is editing the field. By default, consults
     * the current transformer instance, set up by [.TRANSFORMER].
     */
    private fun transformText(text: String): String {
        return if (_transformer == null) text else _transformer!!.transform(text)
    }

    override val styleClass: Class<*>
        get() = Field::class.java

    override fun text(): String? {
        val ctext = text.get()
        // we always want non-empty text so that we force ourselves to always have a text layer and
        // sane dimensions even if the text field contains no text
        return if (ctext == null || ctext.isEmpty()) " " else ctext
    }

    override fun icon(): Icon? {
        return null // fields never have an icon
    }

    override fun wasRemoved() {
        super.wasRemoved()
        // make sure the field is gone
        updateMode(false)
    }

    override fun createBehavior(): Behavior<Field>? {
        return object : Behavior.Select<Field>(this) {
            override fun onClick(iact: Pointer.Interaction) {
                if (!_fullTimeNative) startEdit()
            }
        }
    }

    override fun createLayoutData(hintX: Float, hintY: Float): LayoutData {
        return FieldLayoutData(hintX, hintY)
    }

    private fun startEdit() {
        if (hasNative()) {
            updateMode(true)
            _nativeField!!.focus()

        } else {
            // TODO: multi-line keyboard.getText
            root()!!.iface.plat.input.getText(_textType!!, _popupLabel!!, text.get()).onSuccess({ result: String? ->
                // null result is a canceled entry dialog
                if (result != null) text.update(result)
                _finishedEditing.emit(result != null)
            })
        }
    }

    private val nativeFieldBounds: Rectangle
        get() {
            val insets = resolveStyle(Style.BACKGROUND).insets
            val screenCoords = LayerUtil.layerToScreen(layer, insets.left(), insets.top())
            return Rectangle(screenCoords.x, screenCoords.y,
                    _size.width - insets.width(), _size.height - insets.height())
        }

    private fun updateMode(nativeField: Boolean) {
        if (!hasNative()) return
        if (nativeField) {
            _nativeField = if (_nativeField == null)
                TPPlatform.instance().createNativeTextField(Native())
            else
                TPPlatform.instance().refresh(_nativeField!!)

            _nativeField!!.setEnabled(isEnabled)
            updateNativeFieldBounds()
            _nativeField!!.add()
            setGlyphLayerVisible(false)
        } else if (_nativeField != null) {
            _nativeField!!.remove()
            setGlyphLayerVisible(true)
        }
    }

    private fun setGlyphLayerVisible(visible: Boolean) {
        if (_tglyph.layer() != null) _tglyph.layer()!!.setVisible(visible)
    }

    private inner class FieldLayoutData(hintX: Float, hintY: Float) : TextWidget<Field>.TextLayoutData(hintX, hintY) {

        override fun layout(left: Float, top: Float, width: Float, height: Float) {
            super.layout(left, top, width, height)
            _fullTimeNative = hasNative() && resolveStyle(FULLTIME_NATIVE_FIELD)
            if (_fullTimeNative || _nativeField != null) updateMode(true)

            // make sure our cached bits are up to date
            _validator = resolveStyle(VALIDATOR)
            _transformer = resolveStyle(TRANSFORMER)
            _textType = resolveStyle(TEXT_TYPE)
        }
    }

    companion object {
        /** Creates a style binding for the given maximum length.  */
        fun maxLength(max: Int): Style.Binding<Validator?> {
            return VALIDATOR.`is`(MaxLength(max))
        }

        /** Checks if the platform has native text fields.  */
        fun hasNative(): Boolean {
            return TPPlatform.instance().hasNativeTextFields()
        }

        /** If on a platform that utilizes native fields and this is true, the native field is
         * displayed whenever this Field is visible, and the native field is responsible for all text
         * rendering. If false, the native field is only displayed while actively editing (after a user
         * click).  */
        val FULLTIME_NATIVE_FIELD: Style.Flag = Style.newFlag(false, true)

        /** Controls the behavior of native text fields with respect to auto-capitalization on
         * platforms that support it.  */
        // TODO: iOS supports multiple styles of autocap, support them here?
        val AUTOCAPITALIZATION: Style.Flag = Style.newFlag(false, true)

        /** Controls the behavior of native text fields with respect to auto-correction on platforms
         * that support it.  */
        val AUTOCORRECTION: Style.Flag = Style.newFlag(false, true)

        /** Controls secure text entry on native text fields: typically this will mean dots or asterix
         * displayed instead of the typed character.  */
        val SECURE_TEXT_ENTRY: Style.Flag = Style.newFlag(false, false)

        /** Sets the Keyboard.TextType in use by this Field.  */
        val TEXT_TYPE: Style<Keyboard.TextType> = Style.newStyle(
                false, Keyboard.TextType.DEFAULT)

        /** Sets the validator to use when censoring keypresses into native text fields.
         * @see MaxLength
         */
        val VALIDATOR = Style.newStyle<Validator?>(true, null)

        /** Sets the transformner to use when updating native text fields while being typed into.  */
        val TRANSFORMER = Style.newStyle<Transformer?>(true, null)

        /** Sets the label used on the "return" key of the virtual keyboard on native keyboards. Be
         * aware that some platforms (such as iOS) have a limited number of options. The underlying
         * native implementation is responsible for attempting to match this style, but may be unable
         * to do so. Defaults to null (uses platform default).  */
        val RETURN_KEY_LABEL = Style.newStyle<String?>(false, null)

        /** Sets the field to allow the return key to insert a line break in the text.  */
        val MULTILINE: Style.Flag = Style.newFlag(false, false)
    }
}
