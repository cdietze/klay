package klay.scene

import klay.core.Game
import klay.core.QuadBatch
import klay.core.Surface
import klay.core.Tint
import pythagoras.f.*
import react.Closeable
import react.Signal
import react.SignalViewListener
import react.Value

/**
 * A layer is a node in the scene graph. It has a transformation matrix and other properties which
 * can be manipulated directly and which "take effect" the next time the layer is [.paint]ed.

 *
 * Everything can be accomplished by extending [Layer] and overriding [.paintImpl].
 * However, [GroupLayer], [ImageLayer], [ClippedLayer] etc. are provided to
 * make it easy to implement common use cases "out of the box".
 */
abstract class Layer : Closeable {

    /** Enumerates layer lifecycle states; see [.state].  */
    enum class State {
        REMOVED, ADDED, DISPOSED
    }

    /** Used to configure the origin of a layer based on its width/height.  */
    enum class Origin {
        /** Origin is manually specified via [.setOrigin].  */
        FIXED {
            override fun ox(width: Float): Float {
                return 0f
            } // not used

            override fun oy(height: Float): Float {
                return 0f
            } // not used
        },

        /** Origin is at layer's center.  */
        CENTER {
            override fun ox(width: Float): Float {
                return width / 2
            }

            override fun oy(height: Float): Float {
                return height / 2
            }
        },

        /** Origin is in upper left.  */
        UL {
            override fun ox(width: Float): Float {
                return 0f
            }

            override fun oy(height: Float): Float {
                return 0f
            }
        },

        /** Origin is in upper right.  */
        UR {
            override fun ox(width: Float): Float {
                return width
            }

            override fun oy(height: Float): Float {
                return 0f
            }
        },

        /** Origin is in lower left.  */
        LL {
            override fun ox(width: Float): Float {
                return 0f
            }

            override fun oy(height: Float): Float {
                return height
            }
        },

        /** Origin is in lower right.  */
        LR {
            override fun ox(width: Float): Float {
                return width
            }

            override fun oy(height: Float): Float {
                return height
            }
        },

        /** Origin is at top center.  */
        TC {
            override fun ox(width: Float): Float {
                return width / 2
            }

            override fun oy(height: Float): Float {
                return 0f
            }
        },

        /** Origin is at bottom center.  */
        BC {
            override fun ox(width: Float): Float {
                return width / 2
            }

            override fun oy(height: Float): Float {
                return height
            }
        },

        /** Origin is at left center.  */
        LC {
            override fun ox(width: Float): Float {
                return 0f
            }

            override fun oy(height: Float): Float {
                return height / 2
            }
        },

        /** Origin is at right center.  */
        RC {
            override fun ox(width: Float): Float {
                return width
            }

            override fun oy(height: Float): Float {
                return height / 2
            }
        };

        abstract fun ox(width: Float): Float
        abstract fun oy(height: Float): Float
    }

    /** Used to customize a layer's hit testing mechanism.  */
    interface HitTester {
        /** Returns `layer`, or a child of `layer` if the supplied coordinate (which is in
         * `layer`'s coordinate system) hits `layer`, or one of its children. This allows a
         * layer to customize the default hit testing approach, which is to simply check whether the
         * point intersects a layer's bounds. See [Layer.hitTest].  */
        fun hitTest(layer: Layer, p: Point): Layer
    }

    /**
     * A reactive value which tracks this layer's lifecycle. It starts out [State.REMOVED], and
     * transitions to [State.ADDED] when the layer is added to a scene graph root and back to
     * [State.REMOVED] when removed, until it is finally [.close]d at which point it
     * transitions to [State.DISPOSED].
     */
    val state = Value.create(State.REMOVED)

    /** Creates an unclipped layer. The [.paint] method must be overridden by the creator.  */
    init {
        setFlag(Flag.VISIBLE, true)
    }

    /** Returns the name of this layer. This defaults to the simple name of the class, but can be set
     * programmatically to aid in debugging. See [.setName].  */
    fun name(): String {
        // lazily init name if it's not been set
        if (name == null) {
            name = javaClass.name
            name = name!!.substring(name!!.lastIndexOf(".") + 1).intern()
        }
        return name!!
    }

    /** Sets the name of this layer. See [.name].  */
    fun setName(name: String) {
        this.name = name
    }

    /** Returns the layer that contains this layer, or `null`.  */
    fun parent(): klay.scene.GroupLayer? {
        return parent
    }

    /**
     * Returns a signal via which events may be dispatched "on" this layer. The `Dispatcher`
     * mechanism uses this to dispatch (and listen for) mouse, pointer and touch events to the layers
     * affected by them. A game can also use this to dispatch any other kinds of events on a
     * per-layer basis, with the caveat that all listeners are notified of every event and each must
     * do a type test on the event to determine whether it matches.

     *
     * Also, any layer that has one or more listeners on its events signal is marked as [ ][.interactive]. Further, any [GroupLayer] which has one or more interactive children is
     * also marked as interactive. This allows `Dispatcher`s to be more efficient in their
     * dispatching of UI events.
     */
    fun events(): Signal<Any> {
        if (events == null)
            events = object : Signal<Any>() {
                override fun connectionAdded() {
                    setInteractive(true)
                }

                override fun connectionRemoved() {
                    if (!hasConnections() && deactivateOnNoListeners()) setInteractive(false)
                }
            }
        return events!!
    }

    /** Returns true if [.events] has at least one listener. Use this instead of calling [ ][Signal.hasConnections] on `events` because `events` is created lazily this method
     * avoids creating it unnecessarily.  */
    fun hasEventListeners(): Boolean {
        return events != null && events!!.hasConnections()
    }

    /** Returns whether this layer reacts to clicks and touches. If a layer is interactive, it will
     * respond to [.hitTest], which forms the basis for the click and touch processing
     * provided by the `Dispatcher`s. */
    fun interactive(): Boolean {
        return isSet(Flag.INTERACTIVE)
    }

    /**
     * Configures this layer as reactive to clicks and touches, or not. You usually don't have to do
     * this automatically because a layer is automatically marked as interactive (along with all of
     * its parents) when a listener is added to its [.events] signal.

     *
     * A [GroupLayer] will be made non-interactive automatically if an event is dispatched
     * to it and it discovers that it no longer has any interactive children. Manual management of
     * interactivity is thus generally only useful for "leaf" nodes in the scene graph.

     * @return a reference to this layer for call chaining.
     */
    fun setInteractive(interactive: Boolean): Layer {
        if (interactive() != interactive) {
            // if we're being made interactive, active our parent as well, if we have one
            if (interactive && parent != null) parent!!.setInteractive(interactive)
            setFlag(Flag.INTERACTIVE, interactive)
        }
        return this
    }

    /** Returns true if this layer is visible (i.e. it is being rendered).  */
    fun visible(): Boolean {
        return isSet(Flag.VISIBLE)
    }

    /**
     * Configures this layer's visibility: if true, it will be rendered as normal, if false it and
     * its children will not be rendered.

     * @return a reference to this layer for call chaining.
     */
    fun setVisible(visible: Boolean): Layer {
        setFlag(Flag.VISIBLE, visible)
        return this
    }

    /** Whether this layer has been disposed. If true, the layer can no longer be used.  */
    fun disposed(): Boolean {
        return state.get() == State.DISPOSED
    }

    /** Connects `action` to [.state] such that it is triggered when this layer is added
     * to a rooted scene graph.  */
    fun onAdded(action: SignalViewListener<in Layer>) {
        onState(State.ADDED, action)
    }

    /** Connects `action` to [.state] such that it is triggered when this layer is
     * removed from a rooted scene graph.  */
    fun onRemoved(action: SignalViewListener<in Layer>) {
        onState(State.REMOVED, action)
    }

    /** Connects `action` to [.state] such that it is triggered when this layer is
     * disposed.  */
    fun onDisposed(action: SignalViewListener<in Layer>) {
        onState(State.DISPOSED, action)
    }

    private fun onState(tgtState: State, action: SignalViewListener<in Layer>) {
        state.connect { state: State ->
            if (state == tgtState) action.invoke(this@Layer)
        }
    }

    /**
     * Disposes this layer, removing it from its parent layer. Any resources associated with this
     * layer are freed, and it cannot be reused after being disposed. Disposing a layer that has
     * children will dispose them as well.
     */
    override fun close() {
        if (parent != null) parent!!.remove(this)
        setState(State.DISPOSED)
        setBatch(null)
    }

    /**
     * Returns the layer's current transformation matrix. If any changes have been made to the
     * layer's scale, rotation or translation, they will be applied to the transform matrix before it
     * is returned.

     *
     * *Note:* any direct modifications to this matrix *except* modifications to its
     * translation, will be overwritten if a call is subsequently made to [.setScale],
     * [.setScale], [.setScaleX], [.setScaleY] or [.setRotation].
     * If you intend to manipulate a layer's transform matrix directly, *do not* call those
     * other methods. Also do not expect [.scaleX], [.scaleY], or [.rotation] to
     * reflect the direct changes you've made to the transform matrix. They will not.
     */
    fun transform(): AffineTransform {
        if (isSet(Flag.XFDIRTY)) {
            val sina = MathUtil.sin(rotation)
            val cosa = MathUtil.cos(rotation)
            val m00 = cosa * scaleX
            val m01 = sina * scaleY
            val m10 = -sina * scaleX
            val m11 = cosa * scaleY
            val tx = transform.tx
            val ty = transform.ty
            transform.setTransform(m00, m01, m10, m11, tx, ty)
            setFlag(Flag.XFDIRTY, false)
        }
        return transform
    }

    /**
     * Return the global alpha value for this layer.

     *
     * The global alpha value for a layer controls the opacity of the layer but does not affect
     * the current drawing operation. I.e., when [Game.paint] is called and the [Layer]
     * is drawn, this alpha value is applied to the alpha channel of the Layer.

     *
     * By default, the alpha for a Layer is 1.0 (not transparent).

     * @return alpha in range [0,1] where 0 is transparent and 1 is opaque
     */
    fun alpha(): Float {
        return alpha
    }

    /**
     * Sets the alpha component of this layer's current tint. Note that this value will be quantized
     * to an integer between 0 and 255. Also see [.setTint].

     *
     *  Values outside the range [0,1] will be clamped to the range [0,1].

     * @param alpha alpha value in range [0,1] where 0 is transparent and 1 is opaque.
     * *
     * *
     * @return a reference to this layer for call chaining.
     */
    fun setAlpha(alpha: Float): Layer {
        this.alpha = alpha
        val ialpha = MathUtil.round(0xFF * MathUtil.clamp(alpha, 0f, 1f))
        this.tint = ialpha shl 24 or (tint and 0xFFFFFF)
        return this
    }

    /** Returns the current tint for this layer, as `ARGB`.  */
    fun tint(): Int {
        return tint
    }

    /**
     * Sets the tint for this layer, as `ARGB`.

     *
     *  *NOTE:* this will overwrite any value configured via [.setAlpha]. Either
     * include your desired alpha in the high bits of `tint` or call [.setAlpha] after
     * calling this method.

     *
     *  *NOTE:* the RGB components of a layer's tint only work on GL-based backends. It is
     * not possible to tint layers using the HTML5 canvas and Flash backends.

     *
     *  The tint for a layer controls the opacity of the layer but does not affect the current
     * drawing operation. I.e., when [Game.paint] is called and the [Layer] is drawn,
     * this tint is applied when rendering the layer.

     * @return a reference to this layer for call chaining.
     */
    fun setTint(tint: Int): Layer {
        this.tint = tint
        this.alpha = (tint shr 24 and 0xFF) / 255f
        return this
    }

    /** Returns the x-component of the layer's origin.  */
    fun originX(): Float {
        if (isSet(Flag.ODIRTY)) {
            val width = width()
            if (width > 0) {
                this.originX = origin.ox(width)
                this.originY = origin.oy(height())
                setFlag(Flag.ODIRTY, false)
            }
        }
        return originX
    }

    /** Returns the y-component of the layer's origin.  */
    fun originY(): Float {
        if (isSet(Flag.ODIRTY)) {
            val height = height()
            if (height > 0) {
                this.originX = origin.ox(width())
                this.originY = origin.oy(height)
                setFlag(Flag.ODIRTY, false)
            }
        }
        return originY
    }

    /** Writes this layer's origin into `into`.
     * @return `into` for easy call chaining.
     */
    fun origin(into: Point): Point {
        return into.set(originX(), originY())
    }

    /** Writes this layer's origin into `into`.
     * @return `into` for easy call chaining.
     */
    fun origin(into: Vector): Vector {
        return into.set(originX(), originY())
    }

    /**
     * Sets the origin of the layer to a fixed position. This automatically sets the layer's logical
     * origin to [Origin.FIXED].

     * @param x origin on x axis in display units.
     * *
     * @param y origin on y axis in display units.
     * *
     * *
     * @return a reference to this layer for call chaining.
     */
    fun setOrigin(x: Float, y: Float): Layer {
        this.originX = x
        this.originY = y
        this.origin = Origin.FIXED
        setFlag(Flag.ODIRTY, false)
        return this
    }

    /**
     * Configures the origin of this layer based on a logical location which is recomputed whenever
     * the layer changes size.

     * @return a reference to this layer for call chaining.
     */
    fun setOrigin(origin: Origin): Layer {
        this.origin = origin
        setFlag(Flag.ODIRTY, true)
        return this
    }

    /** Returns this layer's current depth.  */
    fun depth(): Float {
        return depth
    }

    /**
     * Sets the depth of this layer.
     *
     *
     * Within a single [GroupLayer], layers are rendered from lowest depth to highest depth.

     * @return a reference to this layer for call chaining.
     */
    fun setDepth(depth: Float): Layer {
        val oldDepth = this.depth
        if (depth != oldDepth) {
            this.depth = depth
            if (parent != null) parent!!.depthChanged(this, oldDepth)
        }
        return this
    }

    /** Returns this layer's current translation in the x direction.  */
    fun tx(): Float {
        return transform.tx
    }

    /** Returns this layer's current translation in the y direction.  */
    fun ty(): Float {
        return transform.ty
    }

    /** Writes this layer's translation into `into`.
     * @return `into` for easy call chaining.
     */
    fun translation(into: Point): Point {
        return into.set(transform.tx, transform.ty)
    }

    /** Writes this layer's translation into `into`.
     * @return `into` for easy call chaining.
     */
    fun translation(into: Vector): Vector {
        return into.set(transform.tx, transform.ty)
    }

    /**
     * Sets the x translation of this layer.

     *
     * *Note:* all transform changes are deferred until [.transform] is called
     * (which happens during rendering, if not before) at which point the current scale, rotation and
     * translation are composed into an affine transform matrix. This means that, for example,
     * setting rotation and then setting scale will not flip the rotation like it would were these
     * applied to the transform matrix one operation at a time.

     * @return a reference to this layer for call chaining.
     */
    fun setTx(x: Float): Layer {
        transform.setTx(x)
        return this
    }

    /**
     * Sets the y translation of this layer.

     *
     * *Note:* all transform changes are deferred until [.transform] is called
     * (which happens during rendering, if not before) at which point the current scale, rotation and
     * translation are composed into an affine transform matrix. This means that, for example,
     * setting rotation and then setting scale will not flip the rotation like it would were these
     * applied to the transform matrix one operation at a time.

     * @return a reference to this layer for call chaining.
     */
    fun setTy(y: Float): Layer {
        transform.setTy(y)
        return this
    }

    /**
     * Sets the x and y translation of this layer.

     *
     * *Note:* all transform changes are deferred until [.transform] is called
     * (which happens during rendering, if not before) at which point the current scale, rotation and
     * translation are composed into an affine transform matrix. This means that, for example,
     * setting rotation and then setting scale will not flip the rotation like it would were these
     * applied to the transform matrix one operation at a time.

     * @return a reference to this layer for call chaining.
     */
    fun setTranslation(x: Float, y: Float): Layer {
        transform.setTranslation(x, y)
        return this
    }

    /**
     * A variant of [.setTranslation] that takes an `XY`.
     */
    fun setTranslation(trans: XY): Layer {
        return setTranslation(trans.x, trans.y)
    }

    /** Returns this layer's current scale in the x direction.
     *
     * *Note:* this is the most recent value supplied to [.setScale] or
     * [.setScale], it is *not* extracted from the underlying transform.
     * Thus the sign of the scale returned by this method is preserved. It's also substantially
     * cheaper than extracting the scale from the affine transform matrix. This also means that if
     * you change the scale directly on the [.transform] that scale *will not* be
     * returned by this method.  */
    fun scaleX(): Float {
        return scaleX
    }

    /** Returns this layer's current scale in the y direction.
     *
     * *Note:* this is the most recent value supplied to [.setScale] or
     * [.setScale], it is *not* extracted from the underlying transform.
     * Thus the sign of the scale returned by this method is preserved. It's also substantially
     * cheaper than extracting the scale from the affine transform matrix. This also means that if
     * you change the scale directly on the [.transform] that scale *will not* be
     * returned by this method.  */
    fun scaleY(): Float {
        return scaleY
    }

    /** Writes this layer's scale into `into`.
     * @return `into` for easy call chaining.
     */
    fun scale(into: Vector): Vector {
        return into.set(scaleX, scaleY)
    }

    /**
     * Sets the current x and y scale of this layer to `scale`.. Note that a scale of `1`
     * is equivalent to no scale.

     *
     * *Note:* all transform changes are deferred until [.transform] is called
     * (which happens during rendering, if not before) at which point the current scale, rotation and
     * translation are composed into an affine transform matrix. This means that, for example,
     * setting rotation and then setting scale will not flip the rotation like it would were these
     * applied to the transform matrix one operation at a time.

     * @param scale non-zero scale value.
     * *
     * @return a reference to this layer for call chaining.
     */
    fun setScale(scale: Float): Layer {
        return setScale(scale, scale)
    }

    /**
     * Sets the current x scale of this layer. Note that a scale of `1` is equivalent to no
     * scale.

     *
     * *Note:* all transform changes are deferred until [.transform] is called
     * (which happens during rendering, if not before) at which point the current scale, rotation and
     * translation are composed into an affine transform matrix. This means that, for example,
     * setting rotation and then setting scale will not flip the rotation like it would were these
     * applied to the transform matrix one operation at a time.

     * @param sx non-zero scale value.
     * *
     * @return a reference to this layer for call chaining.
     */
    fun setScaleX(sx: Float): Layer {
        if (scaleX != sx) {
            scaleX = sx
            setFlag(Flag.XFDIRTY, true)
        }
        return this
    }

    /**
     * Sets the current y scale of this layer. Note that a scale of `1` is equivalent to no
     * scale.

     *
     * *Note:* all transform changes are deferred until [.transform] is called
     * (which happens during rendering, if not before) at which point the current scale, rotation and
     * translation are composed into an affine transform matrix. This means that, for example,
     * setting rotation and then setting scale will not flip the rotation like it would were these
     * applied to the transform matrix one operation at a time.

     * @param sy non-zero scale value.
     * *
     * @return a reference to this layer for call chaining.
     */
    fun setScaleY(sy: Float): Layer {
        if (scaleY != sy) {
            scaleY = sy
            setFlag(Flag.XFDIRTY, true)
        }
        return this
    }

    /**
     * Sets the current x and y scale of this layer. Note that a scale of `1` is equivalent to
     * no scale.

     *
     * *Note:* all transform changes are deferred until [.transform] is called
     * (which happens during rendering, if not before) at which point the current scale, rotation and
     * translation are composed into an affine transform matrix. This means that, for example,
     * setting rotation and then setting scale will not flip the rotation like it would were these
     * applied to the transform matrix one operation at a time.

     * @param sx non-zero scale value for the x axis.
     * *
     * @param sy non-zero scale value for the y axis.
     * *
     * *
     * @return a reference to this layer for call chaining.
     */
    fun setScale(sx: Float, sy: Float): Layer {
        if (sx != scaleX || sy != scaleY) {
            scaleX = sx
            scaleY = sy
            setFlag(Flag.XFDIRTY, true)
        }
        return this
    }

    /** Returns this layer's current rotation.
     *
     * *Note:* this is the most recent value supplied to [.setRotation], it is
     * *not* extracted from the underlying transform. Thus the value may lie outside the
     * range [-pi, pi] and the most recently set value is preserved. It's also substantially cheaper
     * than extracting the rotation from the affine transform matrix. This also means that if you
     * change the scale directly on the [.transform] that rotation *will not* be
     * returned by this method.  */
    fun rotation(): Float {
        return rotation
    }

    /**
     * Sets the current rotation of this layer, in radians. The rotation is done around the currently
     * set origin, See [Layer.setOrigin].

     *
     * *Note:* all transform changes are deferred until [.transform] is called
     * (which happens during rendering, if not before) at which point the current scale, rotation and
     * translation are composed into an affine transform matrix. This means that, for example,
     * setting rotation and then setting scale will not flip the rotation like it would were these
     * applied to the transform matrix one operation at a time.

     * @param angle angle to rotate, in radians.
     * *
     * *
     * @return a reference to this layer for call chaining.
     */
    fun setRotation(angle: Float): Layer {
        if (rotation != angle) {
            rotation = angle
            setFlag(Flag.XFDIRTY, true)
        }
        return this
    }

    /** Returns the width of this layer.
     * *Note:* not all layers know their size. Those that don't return 0.  */
    open fun width(): Float {
        return 0f
    }

    /** Returns the height of this layer.
     * *Note:* not all layers know their size. Those that don't return 0.  */
    open fun height(): Float {
        return 0f
    }

    /** Returns the width of the layer multiplied by its x scale.
     * *Note:* not all layers know their size. Those that don't return 0.  */
    fun scaledWidth(): Float {
        return scaleX() * width()
    }

    /** Returns the height of the layer multiplied by its y scale.
     * *Note:* not all layers know their size. Those that don't return 0.  */
    fun scaledHeight(): Float {
        return scaleX() * height()
    }

    /**
     * Tests whether the supplied (layer relative) point "hits" this layer or any of its children. By
     * default a hit is any point that falls in a layer's bounding box. A group layer checks its
     * children for hits.

     *
     * Note that this method mutates the supplied point. If a layer is hit, the point will contain
     * the original point as translated into that layer's coordinate system. If no layer is hit, the
     * point will be changed to an undefined value.

     * @return this layer if it was the hit layer, a child of this layer if a child of this layer was
     * * hit, or null if neither this layer, nor its children were hit.
     */
    fun hitTest(p: Point): Layer? {
        return if (hitTester == null) hitTestDefault(p) else hitTester!!.hitTest(this, p)
    }

    /**
     * Like [.hitTest] except that it ignores a configured [HitTester]. This allows one
     * to configure a hit tester which checks custom properties and then falls back on the default
     * hit testing implementation.
     */
    open fun hitTestDefault(p: Point): Layer? {
        return if (p.x >= 0 && p.y >= 0 && p.x < width() && p.y < height()) this else null
    }

    /**
     * Configures a custom hit tester for this layer. May also be called with null to clear out any
     * custom hit tester.

     * @return a reference to this layer for call chaining.
     */
    fun setHitTester(tester: HitTester): Layer {
        hitTester = tester
        return this
    }

    /**
     * Configures a hit tester for this layer which hits this layer any time a hit does not hit a
     * child of this layer. This absorbs all hits that would otherwise propagate up to this layer's
     * parent. Note that this does not do any calculations to determine whether the hit is within the
     * bounds of this layer, as those may or may not be known. *All* all hits that are checked
     * against this layer are absorbed.
     */
    fun absorbHits(): Layer {
        return setHitTester(object : Layer.HitTester {
            override fun hitTest(layer: Layer, p: Point): Layer {
                val hit = hitTestDefault(p)
                return hit ?: this@Layer
            }

            override fun toString(): String {
                return "<all>"
            }
        })
    }

    /**
     * Configures a custom batch (i.e. shader) for use when rendering this layer (and its children).
     * Passing null will cause the default batch to be used. Configuring a batch on a group layer
     * will cause that shader to be used when rendering the group layer's children, unless the child
     * has a custom batch configured itself.

     * @return a reference to this layer for call chaining.
     */
    fun setBatch(batch: QuadBatch?): Layer {
        this.batch = batch
        return this
    }

    /**
     * Renders this layer to `surf`, including its children.
     */
    fun paint(surf: Surface) {
        if (!visible()) return

        val otint = surf.combineTint(tint)
        val obatch = surf.pushBatch(batch)
        surf.concatenate(transform(), originX(), originY())
        try {
            paintImpl(surf)
        } finally {
            surf.popBatch(obatch)
            surf.setTint(otint)
        }
    }

    /**
     * Implements the actual rendering of this layer. The surface will be fully prepared for this
     * layer's rendering prior to calling this method: the layer transform will have been
     * concatenated with the surface transform, the layer's tint will have been applied, and any
     * custom batch will have been pushed onto the layer.
     */
    protected abstract fun paintImpl(surf: Surface)

    protected fun setState(state: State) {
        this.state.update(state)
    }

    override fun toString(): String {
        val buf = StringBuilder(name())
        buf.append(" @ ").append(hashCode()).append(" [")
        toString(buf)
        return buf.append("]").toString()
    }

    protected open fun toString(buf: StringBuilder) {
        buf.append("tx=").append(transform())
        if (hitTester != null) buf.append(", hitTester=").append(hitTester)
    }

    protected var flags: Int = 0
    protected var depth: Float = 0.toFloat()

    private var name: String? = null
    private var parent: klay.scene.GroupLayer? = null
    private var events: Signal<Any>? = null // created lazily
    private var hitTester: HitTester? = null
    private var batch: QuadBatch? = null

    // these values are cached in the layer to make the getters return sane values rather than have
    // to extract the values from the affine transform matrix (which is expensive, doesn't preserve
    // sign, and wraps rotation around at pi)
    private var scaleX = 1f
    private var scaleY = 1f
    private var rotation = 0f
    private val transform = AffineTransform()

    private var origin = Origin.FIXED
    private var originX: Float = 0.toFloat()
    private var originY: Float = 0.toFloat()
    protected var tint = Tint.NOOP_TINT
    // we keep a copy of alpha as a float so that we can return the exact alpha passed to setAlpha()
    // from alpha() to avoid funny business in clients due to the quantization; the actual alpha as
    // rendered by the shader will be quantized, but the eye won't know the difference
    protected var alpha = 1f

    internal open fun onAdd() {
        if (disposed()) throw IllegalStateException("Illegal to use disposed layer: " + this)
        setState(State.ADDED)
    }

    internal open fun onRemove() {
        setState(State.REMOVED)
    }

    internal fun setParent(parent: klay.scene.GroupLayer?) {
        this.parent = parent
    }

    /** Enumerates bit flags tracked by this layer.  */
    protected enum class Flag private constructor(val bitmask: Int) {
        VISIBLE(1 shl 0),
        INTERACTIVE(1 shl 1),
        XFDIRTY(1 shl 2),
        ODIRTY(1 shl 3)
    }

    /** Returns true if `flag` is set.  */
    protected fun isSet(flag: Flag): Boolean {
        return flags and flag.bitmask != 0
    }

    /** Sets `flag` to `active`.  */
    protected fun setFlag(flag: Flag, active: Boolean) {
        if (active) {
            flags = flags or flag.bitmask
        } else {
            flags = flags and flag.bitmask.inv()
        }
    }

    protected fun checkOrigin() {
        if (origin !== Origin.FIXED) setFlag(Flag.ODIRTY, true) // trigger an origin recompute
    }

    /** Whether or not to deactivate this layer when its last event listener is removed.  */
    protected open fun deactivateOnNoListeners(): Boolean {
        return true
    }
}
