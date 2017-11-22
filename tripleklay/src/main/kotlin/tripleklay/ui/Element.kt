package tripleklay.ui

import euklid.f.*
import klay.core.assert
import klay.scene.GroupLayer
import klay.scene.Layer
import react.*
import tripleklay.util.Ref
import kotlin.reflect.KClass

/**
 * The root of the interface element hierarchy. See [Widget] for the root of all interactive
 * elements, and [Container] for the root of all container elements.

 * @param T used as a "self" type; when subclassing `Element`, T must be the type of the
 * * subclass.
 */
abstract class Element<T : Element<T>> protected constructor() {
    /** The layer associated with this element.  */
    val layer = createLayer()

    init {
        // optimize hit testing by checking our bounds first
        layer.setHitTester(object : Layer.HitTester {
            override fun hitTest(layer: Layer, p: Point): Layer? {
                var hit: Layer? = null
                if (isVisible && contains(p.x, p.y)) {
                    if (isSet(Flag.HIT_DESCEND)) hit = layer.hitTestDefault(p)
                    if (hit == null && isSet(Flag.HIT_ABSORB)) hit = layer
                }
                return hit
            }

            override fun toString(): String {
                return "<" + size() + ">"
            }
        })

        // descend by default
        set(Flag.HIT_DESCEND, true)
    }

    /**
     * Returns this element's x offset relative to its parent.
     */
    fun x(): Float {
        return layer.tx()
    }

    /**
     * Returns this element's y offset relative to its parent.
     */
    fun y(): Float {
        return layer.ty()
    }

    /**
     * Returns the width and height of this element's bounds.
     */
    fun size(): IDimension {
        return _size
    }

    /**
     * Writes the location of this element (relative to its parent) into the supplied point.
     * @return `loc` for convenience.
     */
    fun location(loc: Point): Point {
        return loc.set(x(), y())
    }

    /**
     * Writes the current bounds of this element into the supplied bounds.
     * @return `bounds` for convenience.
     */
    fun bounds(bounds: Rectangle): Rectangle {
        bounds.setBounds(x(), y(), _size.width, _size.height)
        return bounds
    }

    /**
     * Returns the parent of this element, or null.
     */
    fun parent(): Container<*>? {
        return _parent
    }

    /**
     * Returns a signal that will dispatch when this element is added or removed from the
     * hierarchy. The emitted value is true if the element was just added to the hierarchy, false
     * if removed.
     */
    fun hierarchyChanged(): SignalView<Boolean> {
        if (_hierarchyChanged == null) _hierarchyChanged = Signal()
        return _hierarchyChanged!!
    }

    /**
     * Returns the styles configured on this element.
     */
    fun styles(): Styles {
        return _styles
    }

    /**
     * Configures the styles for this element. Any previously configured styles are overwritten.
     * @return this element for convenient call chaining.
     */
    fun setStyles(styles: Styles): T {
        _styles = styles
        clearLayoutData()
        invalidate()
        return asT()
    }

    /**
     * Configures styles for this element (in the DEFAULT mode). Any previously configured styles
     * are overwritten.
     * @return this element for convenient call chaining.
     */
    fun setStyles(vararg styles: Style.Binding<*>): T {
        return setStyles(Styles.make(*styles))
    }

    /**
     * Adds the supplied styles to this element. Where the new styles overlap with existing styles,
     * the new styles are preferred, but non-overlapping old styles are preserved.
     * @return this element for convenient call chaining.
     */
    fun addStyles(styles: Styles): T {
        _styles = _styles.merge(styles)
        clearLayoutData()
        invalidate()
        return asT()
    }

    /**
     * Adds the supplied styles to this element (in the DEFAULT mode). Where the new styles overlap
     * with existing styles, the new styles are preferred, but non-overlapping old styles are
     * preserved.
     * @return this element for convenient call chaining.
     */
    fun addStyles(vararg styles: Style.Binding<*>): T {
        return addStyles(Styles.make(*styles))
    }

    /**
     * Returns `this` cast to `T`.
     */
    protected fun asT(): T {
        return this as T
    }

    /**
     * Returns whether this element is enabled.
     */
    val isEnabled: Boolean
        get() = isSet(Flag.ENABLED)

    /**
     * Enables or disables this element. Disabled elements are not interactive and are usually
     * rendered so as to communicate this state to the user.
     */
    fun setEnabled(enabled: Boolean): T {
        if (enabled != isEnabled) {
            set(Flag.ENABLED, enabled)
            clearLayoutData()
            invalidate()
        }
        return asT()
    }

    /**
     * Returns a slot which can be used to wire the enabled status of this element to a [ ] or [react.Value].
     */
    fun enabledSlot(): Slot<Boolean> {
        return { value: Boolean? ->
            setEnabled(value!!)
        }
    }

    /**
     * Binds the enabledness of this element to the supplied value view. The current enabledness
     * will be adjusted to match the state of `isEnabled`.
     */
    fun bindEnabled(isEnabledV: ValueView<Boolean>): T {
        return addBinding(object : Binding(_bindings) {
            override fun connect(): Closeable {
                return isEnabledV.connectNotify(enabledSlot())
            }

            override fun toString(): String {
                return this@Element.toString() + ".bindEnabled"
            }
        })
    }

    /**
     * Returns whether this element is visible.
     */
    val isVisible: Boolean
        get() = isSet(Flag.VISIBLE)

    /**
     * Configures whether this element is visible. An invisible element is not rendered and
     * consumes no space in a group.
     */
    open fun setVisible(visible: Boolean): T {
        if (visible != isVisible) {
            set(Flag.VISIBLE, visible)
            layer.setVisible(visible)
            invalidate()
        }
        return asT()
    }

    /**
     * Returns a slot which can be used to wire the visible status of this element to a [ ] or [react.Value].
     */
    fun visibleSlot(): Slot<Boolean> {
        return { value: Boolean? ->
            setVisible(value!!)
        }
    }

    /**
     * Binds the visibility of this element to the supplied value view. The current visibility will
     * be adjusted to match the state of `isVisible`.
     */
    fun bindVisible(isVisibleV: ValueView<Boolean>): T {
        return addBinding(object : Binding(_bindings) {
            override fun connect(): Closeable {
                return isVisibleV.connectNotify(visibleSlot())
            }

            override fun toString(): String {
                return this@Element.toString() + ".bindVisible"
            }
        })
    }

    /**
     * Returns true only if this element and all its parents' [.isVisible] return true.
     */
    open val isShowing: Boolean
        get() {
            if (!isVisible) return false
            val parent: Container<*>? = parent()
            return parent != null && parent.isShowing
        }

    /**
     * Returns the layout constraint configured on this element, or null.
     */
    fun constraint(): Layout.Constraint? {
        return _constraint
    }

    /**
     * Configures the layout constraint on this element.
     * @return this element for call chaining.
     */
    fun setConstraint(constraint: Layout.Constraint?): T {
        assert(_constraint == null || constraint == null) { "Cannot set constraint on element which already has a constraint. " + "Layout constraints cannot be automatically composed." }
        constraint?.setElement(this)
        _constraint = constraint
        invalidate()
        return asT()
    }

    /**
     * Returns true if this element is part of an interface heirarchy.
     */
    val isAdded: Boolean
        get() = root() != null

    /**
     * Returns the class of this element for use in computing its style. By default this is the
     * actual class, but you may wish to, for example, extend [Label] with some customization
     * and override this method to return `Label.class` so that your extension has the same
     * styles as Label applied to it.

     * Concrete Element implementations should return the actual class instance instead of
     * getClass(). Returning getClass() means that further subclasses will lose all styles applied
     * to this implementation, probably unintentionally.
     */
    abstract val styleClass: KClass<*>

    /**
     * Called when this element is added to a parent element. If the parent element is already
     * added to a hierarchy with a [Root], this will immediately be followed by a call to
     * [.wasAdded], otherwise the [.wasAdded] call will come later when the parent is
     * added to a root.
     */
    fun wasParented(parent: Container<*>) {
        _parent = parent
    }

    /**
     * Called when this element is removed from its direct parent. If the element was removed from
     * a parent that was connected to a [Root], a call to [.wasRemoved] will
     * immediately follow. Otherwise no call to [.wasRemoved] will be made.
     */
    fun wasUnparented() {
        _parent = null
    }

    /**
     * Called when this element (or its parent element) was added to an interface hierarchy
     * connected to a [Root]. The element will subsequently be validated and displayed
     * (assuming it's visible).
     */
    open fun wasAdded() {
        if (_hierarchyChanged != null) _hierarchyChanged!!.emit(true)
        invalidate()
        set(Flag.IS_ADDING, false)
        var b = _bindings
        while (b !== Binding.NONE) {
            b.bind()
            b = b.next!!
        }
    }

    /**
     * Called when this element (or its parent element) was removed from the interface hierarchy.
     * Also, if the element was removed directly from its parent, then the layer is orphaned prior
     * to this call. Furthermore, if the element is being disposed (see
     * [Container.Mutable.dispose] and other methods), the disposal of the layer will occur
     * **after** this method returns and the [.willDispose] method returns true. This
     * allows subclasses to manage resources as needed.
     *
     ***NOTE**: the base class method must
     * **always** be called for correct operation.
     */
    open fun wasRemoved() {
        _bginst.clear()
        if (_hierarchyChanged != null) _hierarchyChanged!!.emit(false)
        set(Flag.IS_REMOVING, false)
        var b = _bindings
        while (b !== Binding.NONE) {
            b.close()
            b = b.next!!
        }
    }

    /**
     * Returns true if the supplied, element-relative, coordinates are inside our bounds.
     */
    fun contains(x: Float, y: Float): Boolean {
        return !(x < 0 || x > _size.width || y < 0 || y > _size.height)
    }

    /**
     * Returns whether this element is selected. This is only applicable for elements that maintain
     * a selected state, but is used when computing styles for all elements (it is assumed that an
     * element that maintains no selected state will always return false from this method).
     * Elements that do maintain a selected state should override this method and expose it as
     * public.
     */
    val isSelected: Boolean
        get() = isSet(Flag.SELECTED)

    /**
     * An element should call this method when it knows that it has changed in such a way that
     * requires it to recreate its visualization.
     */
    fun invalidate() {
        // note that our preferred size and background are no longer valid
        _preferredSize = null

        if (isSet(Flag.VALID)) {
            set(Flag.VALID, false)
            // invalidate our parent if we've got one
            if (_parent != null) {
                _parent!!.invalidate()
            }
        }
    }

    /**
     * Gets a new slot which will invoke [.invalidate].
     * @param styles if set, the slot will also call [.clearLayoutData] when emitted
     */
    protected fun invalidateSlot(styles: Boolean = false): UnitSlot {
        return {
            invalidate()
            if (styles) clearLayoutData()
        }
    }

    /**
     * Does whatever this element needs to validate itself. This may involve recomputing
     * visualizations, or laying out children, or anything else.
     */
    open fun validate() {
        if (!isSet(Flag.VALID)) {
            layout()
            set(Flag.VALID, true)
            wasValidated()
        }
    }

    /**
     * A hook method called after this element is validated. This chiefly exists for [Root].
     */
    protected open fun wasValidated() {
        // nada by default
    }

    /**
     * Returns the root of this element's hierarchy, or null if the element is not currently added
     * to a hierarchy.
     */
    open fun root(): Root? {
        return if (_parent == null) null else _parent!!.root()
    }

    /**
     * Returns whether the specified flag is set.
     */
    protected fun isSet(flag: Flag): Boolean {
        return flag.mask and _flags != 0
    }

    /**
     * Sets or clears the specified flag.
     */
    fun set(flag: Flag, on: Boolean) {
        if (on) {
            _flags = _flags or flag.mask
        } else {
            _flags = _flags and flag.mask.inv()
        }
    }

    /**
     * Returns this element's preferred size, potentially recomputing it if needed.

     * @param hintX if non-zero, an indication that the element will be constrained in the x
     * * direction to the specified width.
     * *
     * @param hintY if non-zero, an indication that the element will be constrained in the y
     * * direction to the specified height.
     */
    open fun preferredSize(hintX: Float, hintY: Float): IDimension {
        if (_preferredSize == null) _preferredSize = computeSize(hintX, hintY)
        return _preferredSize!!
    }

    /**
     * Configures the location of this element, relative to its parent.
     */
    open fun setLocation(x: Float, y: Float) {
        layer.setTranslation(MathUtil.ifloor(x).toFloat(), MathUtil.ifloor(y).toFloat())
    }

    /**
     * Configures the size of this widget.
     */
    open fun setSize(width: Float, height: Float): T {
        val changed = _size.width != width || _size.height != height
        _size.setSize(width, height)
        // if we have a cached preferred size and this size differs from it, we need to clear our
        // layout data as it may contain computations specific to our preferred size
        if (_preferredSize != null && _size != _preferredSize) clearLayoutData()
        if (changed) invalidate()
        return asT()
    }

    /**
     * Resolves the value for the supplied style. See [Styles.resolveStyle] for the gritty
     * details.
     */
    fun <V> resolveStyle(style: Style<V>): V {
        return Styles.resolveStyle(this, style)
    }

    /**
     * Recomputes this element's preferred size.

     * @param hintX if non-zero, an indication that the element will be constrained in the x
     * * direction to the specified width.
     * *
     * @param hintY if non-zero, an indication that the element will be constrained in the y
     * * direction to the specified height.
     */
    fun computeSize(hintX: Float, hintY: Float): Dimension {
        var hintX = hintX
        var hintY = hintY
        // allow any layout constraint to adjust the layout hints
        if (_constraint != null) {
            hintX = _constraint!!.adjustHintX(hintX)
            hintY = _constraint!!.adjustHintY(hintY)
        }

        // create our layout data and ask it for our preferred size (accounting for our background
        // insets in the process)
        _ldata = createLayoutData(hintX, hintY)
        val ldata = _ldata
        val insets = ldata!!.bg.insets
        val size = computeSize(ldata, hintX - insets.width(), hintY - insets.height())
        insets.addTo(size)

        // allow any layout constraint to adjust the computed preferred size
        if (_constraint != null) _constraint!!.adjustPreferredSize(size, hintX, hintY)

        // round our preferred size up to the nearest whole number; if we allow it to remain
        // fractional, we can run into annoying layout problems where floating point rounding error
        // causes a tiny fraction of a pixel to be shaved off of the preferred size of a text
        // widget, causing it to wrap its text differently and hosing the layout
        size.width = MathUtil.iceil(size.width).toFloat()
        size.height = MathUtil.iceil(size.height).toFloat()

        return size
    }

    /**
     * Computes this element's preferred size, delegating to [LayoutData.computeSize] by
     * default. This is called by [.computeSize] after adjusting the hints based
     * on our layout constraints and insets. The returned dimension will be post facto adjusted to
     * include room for the element's insets (if any) and rounded up to the nearest whole pixel
     * value.
     */
    protected open fun computeSize(ldata: LayoutData, hintX: Float, hintY: Float): Dimension {
        return ldata.computeSize(hintX, hintY)
    }

    /**
     * Handles common element layout (background), then calls
     * [.layout] to do the actual layout.
     */
    protected open fun layout() {
        if (!isVisible) return

        val width = _size.width
        val height = _size.height
        val ldata = if (_ldata != null) _ldata else createLayoutData(width, height)

        // if we have a non-matching background, dispose it (note that if we don't want a bg, any
        // existing bg will necessarily be invalid)
        var bginst: Background.Instance? = _bginst.get()
        val bgok = bginst != null && bginst.owner() === ldata!!.bg &&
                bginst.size == _size
        if (!bgok) _bginst.clear()
        // if we want a background and don't already have one, create it
        if (width > 0 && height > 0 && !bgok) {
            bginst = _bginst.set(ldata!!.bg.instantiate(_size))
            bginst!!.addTo(layer, 0f, 0f, 0f)
        }

        // do our actual layout
        val insets = ldata!!.bg.insets
        layout(ldata, insets.left(), insets.top(),
                width - insets.width(), height - insets.height())

        // finally clear our cached layout data
        clearLayoutData()
    }

    /**
     * Delegates layout to [LayoutData.layout] by default.
     */
    protected open fun layout(ldata: LayoutData, left: Float, top: Float, width: Float, height: Float) {
        ldata.layout(left, top, width, height)
    }

    /**
     * Creates the layout data record used by this element. This record temporarily holds resolved
     * style information between the time that an element has its preferred size computed, and the
     * time that the element is subsequently laid out. Note: `hintX` and `hintY` *do
     * not* yet have the background insets subtracted from them, because the creation of the
     * LayoutData is what resolves the background in the first place.
     */
    protected open fun createLayoutData(hintX: Float, hintY: Float): LayoutData {
        return LayoutData()
    }

    /**
     * Clears out cached layout data. This can be called by methods that change the configuration
     * of the element when they know it will render pre-computed layout info invalid.
     */
    protected open fun clearLayoutData() {
        _ldata = null
    }

    /**
     * Creates the layer to be used by this element. Subclasses may override to use a clipped one.
     */
    protected open fun createLayer(): GroupLayer {
        return object : GroupLayer() {
            override fun name(): String {
                return this@Element.toString() + " layer"
            }
        }
    }

    /**
     * Tests if this element is about to be disposed. Elements are disposed via a call to one of
     * the "dispose" methods such as [Container.Mutable.dispose]. This allows
     * subclasses to manage resources appropriately during their implementation of [ ][.wasRemoved], for example clearing a child cache.
     *
     *NOTE: at the expense of slight semantic
     * dissonance, the flag is not cleared after disposal.
     */
    protected fun willDispose(): Boolean {
        return isSet(Flag.WILL_DISPOSE)
    }

    /**
     * Tests if this element is scheduled to be removed from a root hierarchy.
     */
    fun willRemove(): Boolean {
        return isSet(Flag.IS_REMOVING) || _parent != null && _parent!!.willRemove()
    }

    /**
     * Tests if this element is scheduled to be added to a root hierarchy.
     */
    fun willAdd(): Boolean {
        return isSet(Flag.IS_ADDING) || _parent != null && _parent!!.willAdd()
    }

    protected fun addBinding(binding: Binding): T {
        _bindings = binding
        if (isAdded) binding.bind()
        return asT()
    }

    /** Resolves style and other information needed to layout this element.  */
    open inner class LayoutData {
        /** This element's background style.  */
        val bg = resolveStyle(Style.BACKGROUND)

        /**
         * Computes this element's preferred size, given the supplied hints. The background insets
         * will be automatically added to the returned size.
         */
        open fun computeSize(hintX: Float, hintY: Float): Dimension {
            return Dimension(0f, 0f)
        }

        /**
         * Rebuilds this element's visualization. Called when this element's size has changed. In
         * the case of groups, this will relayout its children, in the case of widgets, this will
         * rerender the widget.
         */
        open fun layout(left: Float, top: Float, width: Float, height: Float) {
            // noop!
        }
    }

    /** Ways in which a preferred and an original dimension can be "taken" to produce a result.
     * The name is supposed to be readable in context and compact, for example
     * `new SizableLayoutData(...).forWidth(Take.MAX).forHeight(Take.MIN, 200)`.  */
    protected enum class Take {
        /** Uses the maximum of the preferred size and original.  */
        MAX {
            override fun apply(preferred: Float, original: Float): Float {
                return maxOf(preferred, original)
            }
        },
        /** Uses the minimum of the preferred size and original.  */
        MIN {
            override fun apply(preferred: Float, original: Float): Float {
                return minOf(preferred, original)
            }
        },
        /** Uses the preferred size if non-zero, otherwise the original. This is the default.  */
        PREFERRED_IF_SET {
            override fun apply(preferred: Float, original: Float): Float {
                return if (preferred == 0f) original else preferred
            }
        };

        abstract fun apply(preferred: Float, original: Float): Float
    }

    /**
     * A layout data that will delegate to another layout data instance, but alter the size
     * computation to optionally use fixed values.
     */
    protected inner class SizableLayoutData : LayoutData {
        /**
         * Creates a new layout with the given delegates and size.
         * @param layoutDelegate the delegate to use during layout. May be null if the element
         * * has no layout
         * *
         * @param sizeDelegate the delegate to use during size computation. May be null if the
         * * size will be completely specified by `prefSize`
         * *
         * @param prefSize overrides the size computation. The width and/or height may be zero,
         * * which indicates the `sizeDelegate`'s result should be used for that axis. Passing
         * * `null` is equivalent to passing a 0x0 dimension
         */
        constructor(layoutDelegate: LayoutData?, sizeDelegate: LayoutData?,
                    prefSize: IDimension?) {
            this.layoutDelegate = layoutDelegate
            this.sizeDelegate = sizeDelegate
            if (prefSize != null) {
                prefWidth = prefSize.width
                prefHeight = prefSize.height
            } else {
                prefHeight = 0f
                prefWidth = prefHeight
            }
        }

        /**
         * Creates a new layout that will defer to the given delegate for layout and size. This is
         * equivalent to `SizableLayoutData(delegate, delegate, prefSize)`.
         * @see .SizableLayoutData
         */
        constructor(delegate: LayoutData, prefSize: IDimension?) {
            this.layoutDelegate = delegate
            this.sizeDelegate = delegate
            if (prefSize != null) {
                prefWidth = prefSize.width
                prefHeight = prefSize.height
            } else {
                prefHeight = 0f
                prefWidth = prefHeight
            }
        }

        /**
         * Sets the way in which widths are combined to calculate the resulting preferred size.
         * For example, `new SizeableLayoutData(...).forWidth(Take.MAX)`.
         */
        fun forWidth(fn: Take): SizableLayoutData {
            widthFn = fn
            return this
        }

        /**
         * Sets the preferred width and how it should be combined with the delegate's preferred
         * width. For example, `new SizeableLayoutData(...).forWidth(Take.MAX, 250)`.
         */
        fun forWidth(fn: Take, pref: Float): SizableLayoutData {
            widthFn = fn
            prefWidth = pref
            return this
        }

        /**
         * Sets the way in which heights are combined to calculate the resulting preferred size.
         * For example, `new SizeableLayoutData(...).forHeight(Take.MAX)`.
         */
        fun forHeight(fn: Take): SizableLayoutData {
            heightFn = fn
            return this
        }

        /**
         * Sets the preferred height and how it should be combined with the delegate's preferred
         * height. For example, `new SizeableLayoutData(...).forHeight(Take.MAX, 250)`.
         */
        fun forHeight(fn: Take, pref: Float): SizableLayoutData {
            heightFn = fn
            prefHeight = pref
            return this
        }

        override fun computeSize(hintX: Float, hintY: Float): Dimension {
            // hint the delegate with our preferred width or height or both,
            // then swap in our preferred function on that (min, max, or subclass)
            return adjustSize(sizeDelegate?.computeSize(resolveHintX(hintX), resolveHintY(hintY)) ?: Dimension(prefWidth, prefHeight))
        }

        override fun layout(left: Float, top: Float, width: Float, height: Float) {
            layoutDelegate?.layout(left, top, width, height)
        }

        /**
         * Refines the given x hint for the delegate to consume. By default uses our configured
         * preferred width if not zero, otherwise the passed-in x hint.
         */
        private fun resolveHintX(hintX: Float): Float {
            return select(prefWidth, hintX)
        }

        /**
         * Refines the given y hint for the delegate to consume. By default uses our configured
         * preferred height if not zero, otherwise the passed-in y hint.
         */
        private fun resolveHintY(hintY: Float): Float {
            return select(prefHeight, hintY)
        }

        /**
         * Adjusts the dimension computed by the delegate to get the final preferred size. By
         * default, uses the previously configured [Take] values.
         */
        private fun adjustSize(dim: Dimension): Dimension {
            dim.width = widthFn.apply(prefWidth, dim.width)
            dim.height = heightFn.apply(prefHeight, dim.height)
            return dim
        }

        private fun select(pref: Float, base: Float): Float {
            return if (pref == 0f) base else pref
        }

        private val layoutDelegate: LayoutData?
        private val sizeDelegate: LayoutData?
        private var prefWidth: Float = 0.toFloat()
        private var prefHeight: Float = 0.toFloat()
        private var widthFn = Take.PREFERRED_IF_SET
        private var heightFn = Take.PREFERRED_IF_SET
    }

    /** Used to track bindings to reactive values, which are established when this element is added
     * to the UI hierarchy and closed when the element is removed. This allows us to provide
     * bindFoo() methods which neither leak connections to reactive values whose lifetimes may
     * exceed that of the element that is displaying them, nor burdens the caller with thinking
     * about and managing this.
     * TODO(cdi) i guess we should use a ADT here (NoneBinding vs SomeBinding) to get rid of nullable type */
    protected abstract class Binding(val next: Binding?) {

        abstract fun connect(): Closeable

        fun bind() {
            assert(_conn === Closeable.Util.NOOP) { "Already bound: " + this }
            _conn = connect()
        }

        fun close() {
            _conn = Closeable.Util.close(_conn)
        }

        protected var _conn = Closeable.Util.NOOP

        companion object {
            val NONE: Binding = object : Binding(null) {
                override fun connect(): Closeable {
                    return Closeable.Util.NOOP
                }
            }
        }
    }

    protected var _flags = Flag.VISIBLE.mask or Flag.ENABLED.mask
    protected var _parent: Container<*>? = null
    protected var _preferredSize: Dimension? = null
    protected var _size = Dimension()
    protected var _styles = Styles.none()
    protected var _constraint: Layout.Constraint? = null
    protected var _hierarchyChanged: Signal<Boolean>? = null
    protected var _bindings = Binding.NONE

    protected var _ldata: LayoutData? = null
    protected val _bginst = Ref.create<Background.Instance>(null)

    enum class Flag(val mask: Int) {
        VALID(1 shl 0), ENABLED(1 shl 1), VISIBLE(1 shl 2), SELECTED(1 shl 3), WILL_DISPOSE(1 shl 4),
        HIT_DESCEND(1 shl 5), HIT_ABSORB(1 shl 6), IS_REMOVING(1 shl 7), IS_ADDING(1 shl 8)
    }
}
/**
 * Gets a new slot which will invoke [.invalidate] when emitted.
 */
