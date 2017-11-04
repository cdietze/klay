package tripleklay.util

import klay.core.Event

/**
 * Detects double taps on a layer, using a threshold time between taps. Two taps that occur
 * within a time span shorter than the threshold are considered a double tap.
 */
class DoubleTapper : Tapper() {

    override fun onTap(where: Event.XY) {
        super.onTap(where)
        if (where.time - _tapTime < DOUBLE_TIME)
            onDoubleTap(where)
        else
            _tapTime = where.time
    }

    /**
     * Called when a double tap occurs. This is a simpler version of [ ][.onDoubleTap], for subclasses that don't require the event position.
     */
    fun onDoubleTap() {}

    /**
     * Called when a double tap occurs. By default, this just calls [.onDoubleTap].
     * Subclasses overriding this needn't call super.
     * @param where the pointer's end position (for the 2nd tap)
     */
    fun onDoubleTap(where: Event.XY) {
        onDoubleTap()
    }

    /** Last tap time recorded.  */
    protected var _tapTime: Double = 0.toDouble()

    companion object {
        /** Maximum time between taps for the 2nd to be considered a double.  */
        val DOUBLE_TIME = 500.0
    }
}
