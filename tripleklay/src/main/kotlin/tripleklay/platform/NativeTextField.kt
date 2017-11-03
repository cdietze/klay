package tripleklay.platform

/**
 * Provides access to a platform-native text field, which can be overlaid onto a PlayN game.
 * A TP Field is required for integration. See [TPPlatform.createNativeTextField].
 */
interface NativeTextField : NativeOverlay {
    /** Sets the enabled state of the field.  */
    fun setEnabled(enabled: Boolean)

    /** Request focus for the native text field  */
    fun focus()

    /** Inserts the given text at the current caret position, or if there is a selected region,
     * replaces the region with the given text.
     * @return true if the operation was successful
     */
    fun insert(text: String): Boolean
}
