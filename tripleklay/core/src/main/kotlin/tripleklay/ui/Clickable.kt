package tripleklay.ui

import react.SignalView

/**
 * Implemented by [Element]s that can be clicked.
 */
interface Clickable<T : Element<*>> {
    /** A signal that is emitted when this element is clicked.  */
    fun clicked(): SignalView<T>

    /** Programmatically triggers a click of this element.  */
    fun click()
}
