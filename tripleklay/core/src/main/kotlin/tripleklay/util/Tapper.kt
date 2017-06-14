package tripleklay.util

import klay.core.Event
import klay.scene.Pointer
import pythagoras.f.Point

/**
 * Detects taps on a layer. This is a simple implementation using a threshold distance. If the
 * pointer is dragged less than the threshold, a call to [.onTap] is generated.
 */
open class Tapper : Pointer.Listener {

    /** Square of the threshold distance for this tapper, defaults to
     * [.DEFAULT_TAP_DIST_SQ].  */
    var maxTapDistSq = DEFAULT_TAP_DIST_SQ

    /**
     * Called when a tap occurs. This is a simpler version of [.onTap], for
     * subclasses that don't require the event position.
     */
    fun onTap() {}

    /**
     * Called when a tap occurs. By default, this just calls [.onTap]. Subclasses
     * overriding needn't call super.
     * @param where the pointer's end position
     */
    open fun onTap(where: Event.XY) {
        onTap()
    }

    override fun onStart(iact: Pointer.Interaction) {
        _tracking = Tracking(iact.event!!)
    }

    override fun onEnd(iact: Pointer.Interaction) {
        if (_tracking == null) return
        _tracking!!.drag(iact.event!!)
        if (_tracking!!.maxMovedSq < maxTapDistSq) onTap(iact.event!!)
        _tracking = null
    }

    override fun onDrag(iact: Pointer.Interaction) {
        if (_tracking == null) return
        _tracking!!.drag(iact.event!!)
    }

    override fun onCancel(iact: Pointer.Interaction) {
        _tracking = null
    }

    /** Represents tracking info for tap detection.  */
    protected class Tracking(where: Event.XY) {
        var start: Point
        var startTime: Double = 0.toDouble()
        var maxMovedSq: Float = 0.toFloat()

        init {
            start = Point(where.x, where.y)
            startTime = where.time
        }

        fun drag(where: Event.XY) {
            maxMovedSq = Math.max(maxMovedSq, dist(where))
        }

        fun dist(where: Event.XY): Float {
            val x = where.x - start.x
            val y = where.y - start.y
            return x * x + y * y
        }
    }

    /** Data for current tracking, if any.  */
    protected var _tracking: Tracking? = null

    companion object {
        /** Default threshold distance.  */
        val DEFAULT_TAP_DIST = 15f

        /** Default threshold distance, set to [.DEFAULT_TAP_DIST] squared.  */
        val DEFAULT_TAP_DIST_SQ = DEFAULT_TAP_DIST * DEFAULT_TAP_DIST
    }
}
