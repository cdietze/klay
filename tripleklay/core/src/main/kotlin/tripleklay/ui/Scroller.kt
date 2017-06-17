package tripleklay.ui

import klay.core.Clock
import klay.core.Color
import klay.core.Surface
import klay.scene.*
import pythagoras.f.Dimension
import pythagoras.f.IDimension
import pythagoras.f.IPoint
import pythagoras.f.Point
import react.*
import tripleklay.ui.layout.AxisLayout
import tripleklay.ui.util.XYFlicker
import tripleklay.util.Colors
import tripleklay.util.Layers

import java.util.ArrayList

/**
 * A composite element that manages horizontal and vertical scrolling of a single content element.
 * As shown below, the content can be thought of as moving around behind the scroll group, which is
 * clipped to create a "view" to the content. Methods [.xpos] and [.ypos] allow reading
 * the current position of the view. The view position can be set with [.scroll]. The view
 * size and content size are available via [.viewSize] and [.contentSize].

 * <pre>`Scrolled view (xpos,ypos>0)       View unscrolled (xpos,ypos=0)
 * ---------------------------        ---------------------------
 * |                :        |        | Scroll  |               |
 * |   content      : ypos   |        | Group   |               |
 * |                :        |        |  "view" |               |
 * |           -----------   |        |----------               |
 * |           | Scroll  |   |        |                         |
 * |---xpos--->| Group   |   |        |                         |
 * |           |  "view" |   |        |         content         |
 * |           -----------   |        |                         |
 * ---------------------------        ---------------------------
`</pre> *

 *
 * Scroll bars are configurable via the [.BAR_TYPE] style.

 *
 * NOTE: `Scroller` is a composite container, so callers can't add to or remove from it.
 * To "add" elements, callers should set [.content] to a `Group` and add things to it
 * instead.

 *
 * NOTE: scrolling is done by pointer events; there are two ways to provide interactive
 * (clickable) content.

 *  * The first way is to pass `bubble=true` to [Pointer.Dispatcher]'s
 * constructor. This allows any descendants within the content to be clicked normally. With this
 * approach, after the pointer has been dragged more than a minimum distance, the `Scroller`
 * calls [Interaction.capture], which will cancel all other pointer interactions, including
 * clickable descendants. For buttons or toggles, this causes the element to be deselected,
 * corresponding to popular mobile OS conventions.

 *  * The second way is to use the [.contentClicked] signal. This is more lightweight but
 * only emits after the pointer is released less than a minimum distance away from its starting
 * position.

 * TODO: some way to handle keyboard events (complicated by lack of a focus element)
 * TODO: more fine-grained setPropagateEvents (add a flag to klay Layer?)
 * TODO: temporarily allow drags past the min/max scroll positions and bounce back
 */
class Scroller
/**
 * Creates a new scroller containing the given content and with [Scroller.Behavior.BOTH].
 *
 * If the content is an instance of [Clippable], then translation will occur via that
 * interface. Otherwise, the content's layer translation will be set directly. Graphics level
 * clipping is always performed.
 */
(content: Element<*>) : Composite<Scroller>() {

    /**
     * Interface for customizing how content is clipped and translated.
     * @see Scroller.Scroller
     */
    interface Clippable {
        /**
         * Sets the size of the area the content should clip to. In the default clipping, this
         * has no effect (it relies solely on the clipped group surrounding the content).
         * This will always be called prior to `setPosition`.
         */
        fun setViewArea(width: Float, height: Float)

        /**
         * Sets the translation of the content, based on scroll bar positions. Both numbers will
         * be non-positive, up to the maximum position of the content such that its right or
         * bottom edge aligns with the width or height of the view area, respectively. For the
         * default clipping, this just sets the translation of the content's layer.
         */
        fun setPosition(x: Float, y: Float)
    }

    /**
     * Handles creating the scroll bars.
     */
    abstract class BarType {
        /**
         * Creates the scroll bars.
         */
        abstract fun createBars(scroller: Scroller): Bars
    }

    /**
     * Listens for changes to the scrolling area or offset.
     */
    interface Listener {
        /**
         * Notifies this listener of changes to the content size or scroll size. Normally this
         * happens when either the content or scroll group is validated.
         * @param contentSize the new size of the content
         * *
         * @param scrollSize the new size of the viewable area
         */
        fun viewChanged(contentSize: IDimension, scrollSize: IDimension)

        /**
         * Notifies this listener of changes to the content offset. Note the offset values are
         * positive numbers, so correspond to the position of the view area over the content.
         * @param xpos the horizontal amount by which the view is offset
         * *
         * @param ypos the vertical amount by which the view is offset
         */
        fun positionChanged(xpos: Float, ypos: Float)
    }

    /**
     * Defines the directions available for scrolling.
     */
    enum class Behavior {
        HORIZONTAL, VERTICAL, BOTH;

        fun hasHorizontal(): Boolean {
            return this == HORIZONTAL || this == BOTH
        }

        fun hasVertical(): Boolean {
            return this == VERTICAL || this == BOTH
        }
    }

    /**
     * A range along an axis for representing scroll bars. Using the content and view extent,
     * calculates the relative sizes.
     */
    class Range {
        /**
         * Returns the maximum value that this range can have, in content offset coordinates.
         */
        fun max(): Float {
            return _max
        }

        /**
         * Tests if the range is currently active. A range is inactive if it's turned off
         * explicitly or if the view size is larger than the content size.
         */
        fun active(): Boolean {
            return _max != 0f
        }

        /** Gets the size of the content along this range's axis.  */
        fun contentSize(): Float {
            return if (_on) _csize else _size
        }

        /** Gets the size of the view along this scroll bar's axis.  */
        fun viewSize(): Float {
            return _size
        }

        /** Gets the current content offset.  */
        fun contentPos(): Float {
            return _cpos
        }

        fun setOn(on: Boolean) {
            _on = on
        }

        fun on(): Boolean {
            return _on
        }

        /** Set the view size and content size along this range's axis.  */
        fun setRange(viewSize: Float, contentSize: Float): Float {
            _size = viewSize
            _csize = contentSize
            if (!_on || _size >= _csize) {
                // no need to render, clear fields
                _cpos = 0f
                _pos = _cpos
                _extent = _pos
                _max = _extent
                return 0f

            } else {
                // prepare rendering fields
                _max = _csize - _size
                _extent = _size * _size / _csize
                _pos = Math.min(_pos, _size - _extent)
                _cpos = _pos / (_size - _extent) * _max
                return _cpos
            }
        }

        /** Sets the position of the content along this range's axis.  */
        fun set(cpos: Float): Boolean {
            if (cpos == _cpos) return false
            _cpos = cpos
            _pos = if (_max == 0f) 0 else cpos / _max * (_size - _extent)
            return true
        }

        /** During size computation, extends the provided hint.  */
        fun extendHint(hint: Float): Float {
            // we want the content to take up as much space as it wants if this bar is on
            // TODO: use Float.MAX? that may cause trouble in other layout code
            return if (_on) 100000 else hint
        }

        /** If this range is in use. Set according to [Scroller.Behavior].  */
        protected var _on = true

        /** View size.  */
        var _size: Float = 0.toFloat()

        /** Content size.  */
        protected var _csize: Float = 0.toFloat()

        /** Bar offset.  */
        var _pos: Float = 0.toFloat()

        /** Content offset.  */
        var _cpos: Float = 0.toFloat()

        /** Thumb size.  */
        var _extent: Float = 0.toFloat()

        /** The maximum position the content can have.  */
        var _max: Float = 0.toFloat()
    }

    /**
     * Handles the appearance and animation of scroll bars.
     */
    abstract class Bars
    /**
     * Creates new bars for the given `Scroller`.
     */
    protected constructor(protected val _scroller: Scroller) : Closeable {
        /**
         * Updates the scroll bars to match the current view and content size. This will be
         * called during layout, prior to the call to [.layer].
         */
        fun updateView() {}

        /**
         * Gets the layer to display the scroll bars. It gets added to the same parent as the
         * content's.
         */
        abstract fun layer(): Layer

        /**
         * Updates the scroll bars' time based animation, if any, after the given time delta.
         */
        open fun update(dt: Float) {}

        /**
         * Updates the scroll bars' positions. Not necessary for immediate layer bars.
         */
        open fun updatePosition() {}

        /**
         * Destroys the resources created by the bars.
         */
        override fun close() {
            layer().close()
        }

        /**
         * Space consumed by active scroll bars.
         */
        fun size(): Float {
            return 0f
        }
    }

    /**
     * Plain rectangle scroll bars that overlay the content area, consume no additional screen
     * space, and fade out after inactivity. Ideal for drag scrolling on a mobile device.
     */
    class TouchBars(scroller: Scroller, protected var _color: Int, protected var _size: Float,
                    protected var _topAlpha: Float, protected var _fadeSpeed: Float) : Bars(scroller) {
        init {
            _layer = object : Layer() {
                override fun paintImpl(surface: Surface) {
                    surface.saveTx()
                    surface.setFillColor(_color)
                    val h = _scroller.hrange
                    val v = _scroller.vrange
                    if (h.active()) drawBar(surface, h._pos, v._size - _size, h._extent, _size)
                    if (v.active()) drawBar(surface, h._size - _size, v._pos, _size, v._extent)
                    surface.restoreTx()
                }
            }
        }

        override fun update(delta: Float) {
            // fade out the bars
            if (_alpha > 0 && _fadeSpeed > 0) setBarAlpha(_alpha - _fadeSpeed * delta)
        }

        override fun updatePosition() {
            // whenever the position changes, update to full visibility
            setBarAlpha(_topAlpha)
        }

        override fun layer(): Layer {
            return _layer
        }

        protected fun setBarAlpha(alpha: Float) {
            _alpha = Math.min(_topAlpha, Math.max(0f, alpha))
            _layer.setAlpha(Math.min(_alpha, 1f))
            _layer.setVisible(_alpha > 0)
        }

        protected fun drawBar(surface: Surface, x: Float, y: Float, w: Float, h: Float) {
            surface.fillRect(x, y, w, h)
        }

        protected var _alpha: Float = 0.toFloat()
        protected var _layer: Layer
    }

    /** The content contained in the scroller.  */
    val content: Element<*>

    /** Scroll ranges.  */
    val hrange = createRange()
    val vrange = createRange()

    init {
        layout = AxisLayout.horizontal().stretchByDefault().offStretch().gap(0)
        // our only immediate child is the _scroller, and that contains the content
        initChildren(_scroller = object : Group(ScrollLayout()) {
            override fun createLayer(): GroupLayer {
                // use 1, 1 so we don't crash. the real size is set on validation
                return GroupLayer(1f, 1f)
            }

            override fun layout() {
                super.layout()
                // do this after children have validated their bounding boxes
                updateVisibility()
            }
        })

        _scroller.add(this.content = content)

        // use the content's clipping method if it is Clippable
        if (content is Clippable) {
            _clippable = content

        } else {
            // otherwise, clip using layer translation
            _clippable = object : Clippable {
                override fun setViewArea(width: Float, height: Float) { /* noop */
                }

                override fun setPosition(x: Float, y: Float) {
                    this@Scroller.content.layer.setTranslation(x, y)
                }
            }
        }

        // absorb clicks so that pointer drag can always scroll
        set(Element.Flag.HIT_ABSORB, true)

        // handle mouse wheel
        layer.events().connect(object : Mouse.Listener {
            override fun onWheel(event: Mouse.WheelEvent, iact: Mouse.Interaction) {
                // scale so each wheel notch is 1/4 the screen dimension
                val delta = event.velocity * .25f
                if (vrange.active())
                    scrollY(ypos() + (delta * viewSize().height()) as Int)
                else
                    scrollX(xpos() + (delta * viewSize().width()) as Int)
            }
        })

        // handle drag scrolling
        layer.events().connect(_flicker = XYFlicker())
    }

    /**
     * Sets the behavior of this scroller.
     */
    fun setBehavior(beh: Behavior): Scroller {
        hrange.setOn(beh.hasHorizontal())
        vrange.setOn(beh.hasVertical())
        invalidate()
        return this
    }

    /**
     * Adds a listener to be notified of this scroller's changes.
     */
    fun addListener(lner: Listener) {
        if (_lners == null) _lners = ArrayList<Listener>()
        _lners!!.add(lner)
    }

    /**
     * Removes a previously added listener from this scroller.
     */
    fun removeListener(lner: Listener) {
        if (_lners != null) _lners!!.remove(lner)
    }

    /**
     * Returns the offset of the left edge of the view area relative to that of the content.
     */
    fun xpos(): Float {
        return hrange._cpos
    }

    /**
     * Returns the offset of the top edge of the view area relative to that of the content.
     */
    fun ypos(): Float {
        return vrange._cpos
    }

    /**
     * Sets the left edge of the view area relative to that of the content. The value is clipped
     * to be within its valid range.
     */
    fun scrollX(x: Float) {
        scroll(x, ypos())
    }

    /**
     * Sets the top edge of the view area relative to that of the content. The value is clipped
     * to be within its valid range.
     */
    fun scrollY(y: Float) {
        scroll(xpos(), y)
    }

    /**
     * Sets the left and top of the view area relative to that of the content. The values are
     * clipped to be within their respective valid ranges.
     */
    fun scroll(x: Float, y: Float) {
        var x = x
        var y = y
        x = Math.max(0f, Math.min(x, hrange._max))
        y = Math.max(0f, Math.min(y, vrange._max))
        _flicker.positionChanged(x, y)
    }

    /**
     * Sets the left and top of the view area relative to that of the content the next time the
     * container is laid out. This is needed if the caller invalidates the content and needs
     * to then set a scroll position which may be out of range for the old size.
     */
    fun queueScroll(x: Float, y: Float) {
        _queuedScroll = Point(x, y)
    }

    /**
     * Gets the size of the content that we are responsible for scrolling. Scrolling is active for
     * a given axis when this is larger than [.viewSize] along that axis.
     */
    fun contentSize(): IDimension {
        return _contentSize
    }

    /**
     * Gets the size of the view which renders some portion of the content.
     */
    fun viewSize(): IDimension {
        return _scroller.size()
    }

    /**
     * Gets the signal dispatched when a pointer click occurs in the scroller. This happens
     * only when the drag was not far enough to cause appreciable scrolling.
     */
    fun contentClicked(): Signal<Pointer.Event> {
        return _flicker.clicked
    }

    /** Prepares the scroller for the next frame, at t = t + delta.  */
    protected fun update(delta: Float) {
        _flicker.update(delta)
        update(false)
        if (_bars != null) _bars!!.update(delta)
    }

    /** Updates the position of the content to match the flicker. If force is set, then the
     * relevant values will be updated even if there was no change.  */
    protected fun update(force: Boolean) {
        val pos = _flicker.position()
        val dx = hrange.set(pos.x())
        val dy = vrange.set(pos.y())
        if (dx || dy || force) {
            _clippable.setPosition(-pos.x(), -pos.y())

            // now check the child elements for visibility
            if (!force) updateVisibility()

            firePositionChange()
            if (_bars != null) _bars!!.updatePosition()
        }
    }

    /**
     * A method for creating our `Range` instances. This is called once each for `hrange` and `vrange` at creation time. Overriding this method will allow subclasses
     * to customize `Range` behavior.
     */
    protected fun createRange(): Range {
        return Range()
    }

    /** Extends the usual layout with scroll bar setup.  */
    protected inner class BarsLayoutData : Element.LayoutData() {
        val barType = resolveStyle(BAR_TYPE)
    }

    override fun createLayoutData(hintX: Float, hintY: Float): Element.LayoutData {
        return BarsLayoutData()
    }

    protected override val styleClass: Class<*>
        get() = Scroller::class.java

    override fun wasAdded() {
        super.wasAdded()
        _upconn = root()!!.iface.frame.connect(object : Slot<Clock>() {
            fun onEmit(clock: Clock) {
                update(clock.dt.toFloat())
            }
        })
        invalidate()
    }

    override fun wasRemoved() {
        _upconn.close()
        updateBars(null) // make sure bars get destroyed in case we don't get added again
        super.wasRemoved()
    }

    /** Hides the layers of any children of the content that are currently visible but outside
     * the clipping area.  */
    // TODO: can we get the performance win without being so intrusive?
    protected fun updateVisibility() {
        // only Container can participate, others must implement Clippable and do something else
        if (content !is Container<*>) {
            return
        }

        // hide the layer of any child of content that isn't in bounds
        val x = hrange._cpos
        val y = vrange._cpos
        val wid = hrange._size
        val hei = vrange._size
        val bx = _elementBuffer.width()
        val by = _elementBuffer.height()
        for (child in content) {
            val size = child.size()
            if (child.isVisible)
                child.layer.setVisible(
                        child.x() - bx < x + wid && child.x() + size.width() + bx > x &&
                                child.y() - by < y + hei && child.y() + size.height() + by > y)
        }
    }

    /** Dispatches a [Listener.viewChanged] to listeners.  */
    protected fun fireViewChanged() {
        if (_lners == null) return
        val csize = contentSize()
        val ssize = viewSize()
        for (lner in _lners!!) {
            lner.viewChanged(csize, ssize)
        }
    }

    /** Dispatches a [Listener.positionChanged] to listeners.  */
    protected fun firePositionChange() {
        if (_lners == null) return
        for (lner in _lners!!) {
            lner.positionChanged(xpos(), ypos())
        }
    }

    protected fun updateBars(barType: BarType?) {
        if (_bars != null) {
            if (_barType === barType) return
            _bars!!.close()
            _bars = null
        }
        _barType = barType
        if (_barType != null) _bars = _barType!!.createBars(this)
    }

    override fun layout(ldata: Element.LayoutData, left: Float, top: Float,
                        width: Float, height: Float) {
        // set the bars and element buffer first so the ScrollLayout can use them
        _elementBuffer = resolveStyle(ELEMENT_BUFFER)
        updateBars((ldata as BarsLayoutData).barType)
        super.layout(ldata, left, top, width, height)
        if (_bars != null) layer.add(_bars!!.layer().setDepth(1f).setTranslation(left, top))
    }

    /** Lays out the internal scroller group that contains the content. Performs all the jiggery
     * pokery necessary to make the content think it is in a large area and update the outer
     * `Scroller` instance.  */
    protected inner class ScrollLayout : Layout() {
        override fun computeSize(elems: Container<*>, hintX: Float, hintY: Float): Dimension {
            // the content is always the 1st child, get the preferred size with extended hints
            assert(elems.childCount() == 1 && elems.childAt(0) === content)
            _contentSize.setSize(preferredSize(elems.childAt(0),
                    hrange.extendHint(hintX), vrange.extendHint(hintY)))
            return Dimension(_contentSize)
        }

        override fun layout(elems: Container<*>, left: Float, top: Float, width: Float,
                            height: Float) {
            var left = left
            var top = top
            var width = width
            var height = height
            assert(elems.childCount() == 1 && elems.childAt(0) === content)

            // if we're going to have H or V scrolling, make room on the bottom and/or right
            if (hrange.on() && _contentSize.width > width) height -= _bars!!.size()
            if (vrange.on() && _contentSize.height > height) width -= _bars!!.size()

            // reset ranges
            left = hrange.setRange(width, _contentSize.width)
            top = vrange.setRange(height, _contentSize.height)

            // let the bars know about the range change
            if (_bars != null) _bars!!.updateView()

            // set the content bounds to the large virtual area starting at 0, 0
            setBounds(content, 0f, 0f, hrange.contentSize(), vrange.contentSize())

            // clip the content in its own special way
            _clippable.setViewArea(width, height)

            // clip the scroller layer too, can't hurt
            _scroller.layer.setSize(width, height)

            // reset the flicker (it retains its current position)
            _flicker.reset(hrange.max(), vrange.max())

            // scroll the content
            if (_queuedScroll != null) {
                scroll(_queuedScroll!!.x, _queuedScroll!!.y)
                _queuedScroll = null
            } else {
                scroll(left, top)
            }

            // force an update so the scroll bars have properly aligned positions
            update(true)

            // notify listeners of a view change
            fireViewChanged()
        }
    }

    protected val _scroller: Group
    protected val _flicker: XYFlicker
    protected val _clippable: Clippable
    protected val _contentSize = Dimension()
    protected var _upconn: Connection
    protected var _queuedScroll: Point? = null
    protected var _lners: MutableList<Listener>? = null

    /** Scroll bar type, used to determine if the bars need to be recreated.  */
    protected var _barType: BarType? = null

    /** Scroll bars, created during layout, based on the [BarType].  */
    protected var _bars: Bars? = null

    /** Region around elements when updating visibility.  */
    protected var _elementBuffer: IDimension

    companion object {
        /** The type of bars to use. By default, uses an instance of [TouchBars].  */
        val BAR_TYPE = Style.newStyle<BarType>(true, object : BarType() {
            override fun createBars(scroller: Scroller): Bars {
                return TouchBars(scroller, Color.withAlpha(Colors.BLACK, 128), 5f, 3f, 1.5f / 1000)
            }
        })

        /** The buffer around a child element when updating visibility ([.updateVisibility].
         * The default value (0x0) causes any elements whose exact bounds lie outside the clipped
         * area to be culled. If elements are liable to have overhanging layers, the value can be set
         * larger appropriately.  */
        val ELEMENT_BUFFER = Style.newStyle<IDimension>(true, Dimension(0f, 0f))

        /**
         * Finds the closest ancestor of the given element that is a `Scroller`, or null if
         * there isn't one. This uses the tripleplay ui hierarchy.
         */
        fun findScrollParent(elem: Element<*>?): Scroller? {
            var elem = elem
            while (elem != null && elem !is Scroller) {
                elem = elem.parent()
            }
            return elem as Scroller?
        }

        /**
         * Attempts to scroll the given element into view.
         * @return true if successful
         */
        fun makeVisible(elem: Element<*>): Boolean {
            val scroller = findScrollParent(elem) ?: return false

            // the element in question may have been added and then immediately scrolled to, which
            // means it hasn't been laid out yet and does not have its proper position; in that case
            // defer this process a tick to allow it to be laid out
            if (!scroller.isSet(Element.Flag.VALID)) {
                elem.root()!!.iface.frame.connect(object : UnitSlot() {
                    fun onEmit() {
                        makeVisible(elem)
                    }
                }).once()
                return true
            }

            val offset = Layers.transform(Point(0f, 0f), elem.layer, scroller.content.layer)
            scroller.scroll(offset.x, offset.y)
            return true
        }
    }
}
