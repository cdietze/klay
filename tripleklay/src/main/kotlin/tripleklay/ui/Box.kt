package tripleklay.ui

import klay.core.Clock
import klay.scene.GroupLayer
import klay.scene.LayerUtil
import pythagoras.f.Dimension
import pythagoras.f.MathUtil
import react.Closeable
import react.Slot
import react.Value
import tripleklay.shaders.RotateYBatch
import tripleklay.util.Interpolator
import kotlin.reflect.KClass
/**
 * A container that holds zero or one widget. The box delegates everything to its current contents
 * (its preferred size is its content's preferred size, it sizes its contents to its size).
 */
open class Box
/** Creates a box with the specified starting contents.  */
constructor(contents: Element<*>? = null) : Container.Mutable<Box>() {

    /** A `Box` which draws its children clipped to their preferred size.  */
    class Clipped
    /** Creates a clipped box with the specified starting contents.  */
    constructor(contents: Element<*>? = null) : Box() {

        init {
            set(contents)
        }

        override fun createLayer(): GroupLayer {
            return GroupLayer(1f, 1f)
        }

        override fun wasValidated() {
            layer.setSize(size().width, size().height)
        }
    }
    /** Creates an empty clipped box.  */

    /** Manages transitions for [.transition].  */
    abstract class Trans protected constructor(duration: Int) : Slot<Clock> {

        /** Indicates whether this transition is in process.  */
        var active = Value(false)

        protected var _ocontents: Element<*>? = null
        protected var _ncontents: Element<*>? = null

        private val _duration: Float = duration.toFloat() // ms
        private var _elapsed: Float = 0.toFloat()
        private var _box: Box? = null
        private var _interp = Interpolator.LINEAR
        private var _conn: Closeable? = null

        /** Configures the interpolator to use for the transition.  */
        fun interp(interp: Interpolator): Trans {
            _interp = interp
            return this
        }

        internal fun start(box: Box, ncontents: Element<*>) {
            if (active.get())
                throw IllegalStateException(
                        "Cannot reuse transition until it has completed.")

            _box = box
            _ocontents = box.contents()
            _ncontents = ncontents
            _box!!.didAdd(_ncontents!!)
            _ncontents!!.setLocation(_ocontents!!.x(), _ocontents!!.y())
            _ncontents!!.setSize(_ocontents!!.size().width, _ocontents!!.size().height)
            _ncontents!!.validate()

            _conn = box.root()!!.iface.frame.connect(this)
            _elapsed = -1f
            init()
            update(0f)
            active.update(true)
        }

        override fun invoke(clock: Clock) {
            // a minor hack which causes us to skip the frame on which we validated the new
            // contents and generally did potentially expensive things; that keeps us from jumping
            // into the transition with a big first time step
            if (_elapsed == -1f)
                _elapsed = 0f
            else
                _elapsed += clock.dt.toFloat()

            val pct = minOf(_elapsed / _duration, 1f)
            // TODO: interp!
            update(_interp.apply(pct))
            if (pct == 1f) {
                _box!!.set(_ncontents) // TODO: avoid didAdd
                _conn!!.close()
                _box = null
                cleanup()
                _ocontents = null
                _ncontents = null
                active.update(false)
            }
        }

        protected open fun init() {}
        protected abstract fun update(pct: Float)
        protected open fun cleanup() {}
    }

    /** A transition that fades from the old contents to the new.  */
    class Fade(duration: Int) : Trans(duration) {

        override fun update(pct: Float) {
            _ocontents!!.layer.setAlpha(1 - pct)
            _ncontents!!.layer.setAlpha(pct)
        }

        override fun cleanup() {
            _ocontents!!.layer.setAlpha(1f)
        }
    }

    class Flip(duration: Int) : Trans(duration) {

        private lateinit var _obatch: RotateYBatch
        private lateinit var _nbatch: RotateYBatch

        override fun init() {
            // TODO: compute the location of the center of the box in screen coordinates, place
            // the eye there in [0, 1] coords
            val gfx = _ocontents!!.root()!!.iface.plat.graphics
            val eye = LayerUtil.layerToScreen(
                    _ocontents!!.layer, _ocontents!!.size().width / 2, _ocontents!!.size().height / 2)
            eye.x /= gfx.viewSize.width
            eye.y /= gfx.viewSize.height
            _obatch = RotateYBatch(gfx.gl, eye.x, eye.y, 1f)
            _nbatch = RotateYBatch(gfx.gl, eye.x, eye.y, 1f)
            _ocontents!!.layer.setBatch(_obatch)
            _ncontents!!.layer.setBatch(_nbatch)
        }

        override fun update(pct: Float) {
            _obatch.angle = MathUtil.PI * pct
            _nbatch.angle = -MathUtil.PI * (1 - pct)
            _ocontents!!.layer.setVisible(pct < 0.5f)
            _ncontents!!.layer.setVisible(pct >= 0.5f)
        }

        override fun cleanup() {
            _ocontents!!.layer.setBatch(null)
            _ncontents!!.layer.setBatch(null)
        }
    }

    init {
        set(contents)
    }

    /** Returns the box's current contents.  */
    fun contents(): Element<*>? {
        return _contents
    }

    /** Updates the box's contents. The previous contents, if any, is removed but not destroyed.
     * To destroy the old contents and set the new, use `destroyContents().set(contents)`. */
    fun set(contents: Element<*>?): Box {
        if (contents !== _contents) set(contents, false)
        return this
    }

    /** Performs an animated transition from the box's current contents to `contents`.
     * @param trans describes and manages the transition (duration, style, etc.).
     */
    fun transition(contents: Element<*>, trans: Trans): Box {
        trans.start(this, contents)
        return this
    }

    /** Clears out the box's current contents.  */
    fun clear(): Box {
        return set(null)
    }

    /** Clears out the box's current contents and destroys it immediately.  */
    fun destroyContents(): Box {
        return set(null as Element<*>?, true)
    }

    override fun stylesheet(): Stylesheet? {
        return null // boxes provide no styles
    }

    override fun childCount(): Int {
        return if (_contents == null) 0 else 1
    }

    override fun childAt(index: Int): Element<*> {
        if (_contents == null || index != 0) throw IndexOutOfBoundsException()
        return _contents!!
    }

    override fun iterator(): Iterator<Element<*>> {
        return if (_contents == null)
            emptyList<Element<*>>().iterator()
        else
            setOf(_contents!!).iterator()
    }

    override fun remove(child: Element<*>) {
        if (_contents === child) clear()
    }

    override fun removeAt(index: Int) {
        if (_contents == null || index != 0) throw IndexOutOfBoundsException()
        clear()
    }

    override fun removeAll() {
        clear()
    }

    override fun destroy(child: Element<*>) {
        if (_contents === child) destroyContents()
    }

    override fun destroyAt(index: Int) {
        if (_contents == null || index != 0) throw IndexOutOfBoundsException()
        destroyContents()
    }

    override fun destroyAll() {
        destroyContents()
    }

    override val styleClass: KClass<*>
        get() = Box::class

    override fun computeSize(ldata: LayoutData, hintX: Float, hintY: Float): Dimension {
        return if (_contents == null) Dimension() else _contents!!.computeSize(hintX, hintY)
    }

    override fun layout(ldata: LayoutData, left: Float, top: Float,
                        width: Float, height: Float) {
        if (_contents != null) {
            _contents!!.setSize(width, height)
            _contents!!.setLocation(left, top)
            _contents!!.validate()
        }
    }

    override // not used
    val layout: Layout
        get() = throw UnsupportedOperationException()

    protected fun set(contents: Element<*>?, destroy: Boolean): Box {
        if (_contents != null) {
            didRemove(_contents!!, destroy)
        }
        _contents = contents
        if (contents != null) {
            didAdd(contents)
        }
        invalidate()
        return this
    }

    protected var _contents: Element<*>? = null
}
/** Creates an empty box.  */
