package tripleklay.platform

import pythagoras.f.Point

/** Defines application hooks into controlling focus on native text fields.  */
interface KeyboardFocusController {
    /** Return true if the keyboard focus should be relinquished for a pointer that starts at the
     * given location.

     * The default (with no KeyboardFocusController specified) is to relinquish focus for any point
     * that does not start on a native text field. With this method, fine control is possible,
     * allowing some in-game UI to be interacted with without losing focus if desired.  */
    fun unfocusForLocation(location: Point): Boolean

    /** Called each time a field has the return key pressed. Return true to relinquish the
     * keyboard.  */
    fun unfocusForEnter(): Boolean
}
