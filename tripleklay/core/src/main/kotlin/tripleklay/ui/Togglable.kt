package tripleklay.ui

import react.Value

/**
 * Implemented by [Element]s that expose a selected state and can be clicked.
 */
interface Togglable<T : Element<*>> : Clickable<T> {
    /** A value that reflects the current selection state and is updated when said state changes.  */
    fun selected(): Value<Boolean>
}
