package tripleklay.ui

import pythagoras.f.IDimension
import tripleklay.ui.Log.log
import tripleklay.util.DimensionValue
import tripleklay.util.Glyph

/**
 * A widget that allows configuring its preferred size. The size is always returned when the size
 * of the widget is calculated, but the widget may end up being stretched when contained in a
 * layout that does so.
 */
abstract class SizableWidget<T : SizableWidget<T>>
/** Creates the sizable widget with preferred width and height.  */
@JvmOverloads constructor(width: Float = 0f, height: Float = 0f) : Widget<T>() {
    /** The preferred size of this widget. Update at will.  */
    val preferredSize = DimensionValue(0f, 0f)

    /** Creates the sizable widget with the given preferred size.  */
    constructor(size: IDimension) : this(size.width, size.height) {}

    init {
        preferredSize.update(width, height)
        preferredSize.connect(invalidateSlot())
    }

    /** Creates the layout to which the widget's [Element.SizableLayoutData] will delegate.  */
    protected fun createBaseLayoutData(hintX: Float, hintY: Float): LayoutData? {
        return null
    }

    override fun createLayoutData(hintX: Float, hintY: Float): LayoutData {
        // use a sizable layout data with our preferred size and delegate to the base, if any
        return SizableLayoutData(createBaseLayoutData(hintX, hintY)!!, null, preferredSize.get())
    }

    /**
     * Prepares the given [Glyph] (or creates a new one if null) that has been prepared to
     * this SizableWidget's [.preferredSize]. If that size is 0 in either dimension, a warning
     * is logged and null is returned.
     */
    @JvmOverloads protected fun prepareGlyph(glyph: Glyph? = null): Glyph? {
        var glyph = glyph
        val size = preferredSize.get()
        if (size.width === 0f || size.height === 0f) {
            log.warning("SizableWidget cannot prepare a glyph with a 0 dimension", "size", size)
            return null
        }

        glyph = if (glyph == null) Glyph(layer) else glyph
        glyph.prepare(root()!!.iface.plat.graphics, size)
        return glyph
    }
}
/** Creates the sizable widget with preferred width and height of 0. Note that this will
 * cause the base layout preferred size to be used, if overridden.  */
/**
 * Returns a new [Glyph] that has been prepared to this SizableWidget's
 * [.preferredSize]. If that size is 0 in either dimension, a warning is logged and null
 * is returned.
 */
