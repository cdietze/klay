package tripleklay.ui

import pythagoras.f.IDimension
import react.Closeable
import react.Signal
import kotlin.reflect.KClass
/**
 * The root of a display hierarchy. An application can have one or more roots, but they should not
 * overlap and will behave as if oblivious to one another's existence.
 */
open class Root
/** Creates a new root with the provided layout and stylesheet. a */
(
        /** The interface of which this root is a part.  */
        val iface: Interface, layout: Layout, sheet: Stylesheet) : Elements<Root>(layout), Closeable {

    /** A signal emitted when this root is validated.  */
    val validated = Signal<Root>()

    init {
        setStylesheet(sheet)

        set(Element.Flag.HIT_ABSORB, true)
    }

    /** Sizes this element to its preferred size, computed using the supplied hints.  */
    fun pack(widthHint: Float = 0f, heightHint: Float = 0f): Root {
        val psize = preferredSize(widthHint, heightHint)
        setSize(psize.width, psize.height)
        return this
    }

    /** Sizes this root element to the specified width and its preferred height.  */
    fun packToWidth(width: Float): Root {
        val psize = preferredSize(width, 0f)
        setSize(width, psize.height)
        return this
    }

    /** Sizes this root element to the specified height and its preferred width.  */
    fun packToHeight(height: Float): Root {
        val psize = preferredSize(0f, height)
        setSize(psize.width, height)
        return this
    }

    /** Sets the size of this root element.  */
    override fun setSize(width: Float, height: Float): Root {
        _size.setSize(width, height)
        invalidate()
        return this
    }

    /** Sets the size of this root element.  */
    fun setSize(size: IDimension): Root {
        return setSize(size.width, size.height)
    }

    /** Sets the size of this root element and its translation from its parent.  */
    fun setBounds(x: Float, y: Float, width: Float, height: Float): Root {
        setSize(width, height)
        setLocation(x, y)
        return this
    }

    /** Configures the location of this root, relative to its parent layer.  */
    override fun setLocation(x: Float, y: Float) {
        super.setLocation(x, y)
    }

    override val isShowing: Boolean
        get() = isVisible

    /** See [Interface.disposeRoot].  */
    override fun close() {
        iface.disposeRoot(this)
    }

    /**
     * Computes the preferred size of this root. In general, one should use [.pack] or one of
     * the related pack methods, but if one has special sizing requirements, they may wish to call
     * `preferredSize` directly, followed by [.setSize].

     * @param hintX the width hint (a width in which the layout will attempt to fit itself), or 0
     * * to allow the layout to use unlimited width.
     * *
     * @param hintY the height hint (a height in which the layout will attempt to fit itself), or 0
     * * to allow the layout to use unlimited height.
     */
    override fun preferredSize(hintX: Float, hintY: Float): IDimension {
        return super.preferredSize(hintX, hintY)
    }

    /**
     * Applies the root size to all descendants. The size normally comes from a call to
     * [.pack] or a related method. Validation is performed automatically by
     * [Interface.paint] if the root is created via [Interface].
     */
    override fun validate() {
        super.validate()
    }

    /**
     * By default, all clicks that fall within a root's bounds are dispatched to the root's layer
     * if they do not land on an interactive child element. This prevents clicks from "falling
     * through" to lower roots, which are visually obscured by this root. Call this method with
     * false if you want this root not to absorb clicks (if it's "transparent").
     */
    fun setAbsorbsClicks(absorbsClicks: Boolean): Root {
        set(Element.Flag.HIT_ABSORB, absorbsClicks)
        return this
    }

    /**
     * Gets this Root's menu host, creating it if necessary.
     */
    /**
     * Sets this Root's menu host, allowing an application to more manage multiple roots with
     * a single menu host.
     */
    var menuHost: MenuHost
        get() {
            if (_menuHost == null) {
                _menuHost = MenuHost(iface, this)
            }
            return _menuHost!!
        }
        set(host) {
            if (_menuHost != null) {
                _menuHost!!.deactivate()
            }
            _menuHost = host
        }

    override val styleClass: KClass<*>
        get() = Root::class

    override fun root(): Root? {
        return this
    }

    override fun wasValidated() {
        super.wasValidated()
        validated.emit(this)
    }

    protected var _active: Element<*>? = null
    protected var _menuHost: MenuHost? = null
}
/** Sizes this root element to its preferred size.  */
