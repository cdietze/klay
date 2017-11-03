package tripleklay.platform

import klay.core.Image
import pythagoras.f.IRectangle
import pythagoras.f.Rectangle
import react.SignalView
import react.UnitSignal
import react.Value
import react.ValueView
import tripleklay.ui.Field

/**
 * The entry point for per-platform services made available by TripleKlay. This is akin to the
 * mechanism used by PlayN for its per-platform backends, and must be configured in a similar way.
 * In your per-platform bootstrap class, you must initialize the appropriate TripleKlay platform
 * backend if you wish to make use of these services.

 * <pre>`public class TripleDemoJava {
 * public static void main (String[] args) {
 * JavaPlatform platform = JavaPlatform.register();
 * JavaTPPlatform.register(platform);
 * // etc.
 * }
 * }
`</pre> *
 */
abstract class TPPlatform {

    /**
     * Returns true if this platform supports native text fields.
     */
    fun hasNativeTextFields(): Boolean {
        return false
    }

    /**
     * Creates a native text field, if this platform supports it. If the platform requires it,
     * the field's styles may be resolved at this time.

     * @exception UnsupportedOperationException thrown if the platform lacks support for native
     * * text fields, use [.hasNativeTextFields] to check.
     */
    fun createNativeTextField(field: Field.Native): NativeTextField {
        throw UnsupportedOperationException()
    }

    /**
     * Refreshes a native text field to match the current styles of its associated field instance.
     * Depending on the implementation, a new native field may be returned, or the given one
     * adjusted.
     */
    fun refresh(previous: NativeTextField): NativeTextField {
        throw UnsupportedOperationException()
    }

    /** Sets the instance of KeyboardFocusController to use for keyboard focus management, or
     * null for none.  */
    fun setKeyboardFocusController(ctrl: KeyboardFocusController) {
        _kfc = ctrl
    }

    /** Signal emitted when the user interacts with a native text field. This allows games to
     * qualify native text field usage as non-idle user behavior.  */
    fun keyboardActivity(): SignalView<Unit> {
        return _activity
    }

    fun createImageOverlay(image: Image): ImageOverlay {
        throw UnsupportedOperationException()
    }

    /** Gets a view of the Field that is currently in focus. Implemented in iOS and JRE if
     * [.hasNativeTextFields], otherwise remains null. Corresponds to the tripleplay
     * field currently receiving native keyboard input.  */
    fun focus(): ValueView<Field?> {
        return _focus
    }

    /** Clears the currently focused field, if any.  */
    fun clearFocus() {}

    /** Updates the bounds of all known native fields. This is useful for animating a `Root`
     * containing some `Field` instances and changes its transform.  */
    fun refreshNativeBounds() {}

    /** Hides the native widgets under the given screen area, using platform clipping if
     * supported. The PlayN layers underneath the area should show through. Pointer events should
     * also not interact with the native widgets within the area.
     * @param area the new area to hide, in screen coordinates, or null to show all
     */
    fun hideNativeOverlays(area: IRectangle?) {
        if (_hidden == null && area == null || _hidden != null && _hidden == area)
            return
        _hidden = if (area == null) null else Rectangle(area)
        updateHidden()
    }

    protected fun updateHidden() {}

    protected var _focus = Value<Field?>(null)
    protected var _kfc: KeyboardFocusController? = null
    protected var _activity = UnitSignal()
    protected var _hidden: Rectangle? = null

    companion object {
        /** Returns the currently registered TPPlatform instance.  */
        fun instance(): TPPlatform {
            return _instance
        }

        /** Called by the static register methods in the per-platform backends.  */
        internal fun register(instance: TPPlatform) {
            if (_instance !== _default) {
                throw IllegalStateException("TPPlatform instance already registered.")
            }
            _instance = instance
        }

        protected var _default: TPPlatform = object : TPPlatform() {

        }
        protected var _instance = _default
    }
}
