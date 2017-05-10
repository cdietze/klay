package klay.scene

import klay.core.Event
import pythagoras.f.Point
import pythagoras.f.XY

/**
 * Contains information about the interaction of which an event is a part.
 */
abstract class Interaction<E : Event.XY>(
        /** The layer that was hit at the start of this interaction.  */
        val hitLayer: Layer, private val bubble: Boolean) : XY {

    /** Used to configure [.capture].  */
    enum class CaptureMode {
        /** Events are only sent to the capturing layer.  */
        ONLY {
            override fun allow(depth: Depth): Boolean {
                return depth == Depth.AT
            }
        },
        /** Events are sent to the capturing layer and its parents.  */
        ABOVE {
            override fun allow(depth: Depth): Boolean {
                return depth != Depth.BELOW
            }
        },
        /** Events are sent to the capturing layer and its children.  */
        BELOW {
            override fun allow(depth: Depth): Boolean {
                return depth != Depth.ABOVE
            }
        };

        abstract fun allow(depth: Depth): Boolean
    }

    private var canceled: Boolean = false
    private var dispatchLayer: Layer? = null
    private var capturingLayer: Layer? = null
    private var captureMode: CaptureMode? = null

    /** The current event's location, translated into the hit layer's coordinate space.  */
    val local = Point()

    /** The event currently being dispatched in this interaction.  */
    var event: E? = null

    /** Returns [.event]'s x coordinate, for convenience.  */
    override val x: Float
        get() {
            return event!!.x
        }

    /** Returns [.event]'s y coordinate, for convenience.  */
    override val y: Float
        get() {
            return event!!.y
        }

    /** Returns whether this interaction is captured.  */
    fun captured(): Boolean {
        return capturingLayer != null
    }

    /** Captures this interaction in the specified capture mode. Depending on the mode, subsequent
     * events will go only to the current layer, or that layer and its parents, or that layer and
     * its children. Other layers in the interaction will receive a cancellation event and nothing
     * further.  */
    @JvmOverloads fun capture(mode: CaptureMode = CaptureMode.ONLY) {
        assert(dispatchLayer != null)
        if (canceled) throw IllegalStateException("Cannot capture canceled interaction.")
        if (capturingLayer !== dispatchLayer && captured())
            throw IllegalStateException(
                    "Interaction already captured by " + capturingLayer!!)
        capturingLayer = dispatchLayer
        captureMode = mode
        notifyCancel(capturingLayer, captureMode, event)
    }

    /** Cancels this interaction. All layers which normally participate in the action will be
     * notified of the cancellation.  */
    fun cancel() {
        if (!canceled) {
            notifyCancel(null, null, event)
            canceled = true
        }
    }

    override fun toString(): String {
        return "Interaction[bubble=" + bubble + ", canceled=" + canceled +
                ", capmode=" + captureMode + "]" +
                "\n event=" + event + "\n hit=" + hitLayer
    }

    internal fun dispatch(event: E?) {
        // if this interaction has been manually canceled, ignore further dispatch requests
        if (canceled) return

        assert(event != null)
        LayerUtil.screenToLayer(hitLayer, local.set(event!!.x, event.y), local)
        this.event = event
        try {
            if (bubble) {
                var depth = Depth.BELOW
                var target: Layer? = hitLayer
                while (target != null) {
                    if (target === capturingLayer)
                        depth = Depth.AT
                    else if (depth == Depth.AT) depth = Depth.ABOVE
                    if (captureMode != null && !captureMode!!.allow(depth)) {
                        target = target!!.parent()
                        continue
                    }
                    dispatch(target)
                    // the above dispatch may have caused a capture, in which case capturing layer will have
                    // just been set and we need to update our depth accordingly
                    if (target === capturingLayer) depth = Depth.AT
                    target = target!!.parent()
                }
            } else {
                dispatch(hitLayer)
            }
        } finally {
            this.event = null
        }
        local[0f] = 0f
    }

    internal fun dispatch(layer: Layer) {
        if (!layer.hasEventListeners()) return
        val odispatchLayer = dispatchLayer
        dispatchLayer = layer
        try {
            layer.events().emit(this)
        } finally {
            dispatchLayer = odispatchLayer
        }
    }

    /** Creates a cancel event using data from `source` if available. `source` will be
     * null if this cancellation was initiated outside normal event dispatch.  */
    protected abstract fun newCancelEvent(source: E?): E

    private fun notifyCancel(except: Layer?, exceptMode: CaptureMode?, source: E?) {
        val oldEvent = event
        event = newCancelEvent(source)
        try {
            if (bubble) {
                var depth = Depth.BELOW
                var target: Layer? = hitLayer
                while (target != null) {
                    if (target === except)
                        depth = Depth.AT
                    else if (depth == Depth.AT) depth = Depth.ABOVE
                    if (exceptMode != null && exceptMode.allow(depth)) {
                        target = target!!.parent()
                        continue
                    }
                    dispatch(target)
                    target = target!!.parent()
                }
            } else {
                if (hitLayer !== except) dispatch(hitLayer)
            }
        } finally {
            this.event = oldEvent
        }
    }

    enum class Depth {
        BELOW, AT, ABOVE
    }
}
/** Captures this interaction in `ONLY` mode. This causes subsequent events in this
 * interaction to go only to the layer which is currently handling the interaction. Other layers
 * in the interaction will receive a cancellation event and nothing further.  */
