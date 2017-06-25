package tripleklay.ui

import klay.core.Canvas
import react.UnitSlot
import tripleklay.util.Glyph

/**
 * Base for widgets that consist of a single glyph. Performs all boilerplate layout stuff and
 * delegates the painting to subclasses: [.paint]. Note this should only be used for
 * widgets that need to do some composition of images or other drawing for their display. Otherwise
 * they should do their own arranging of group and image layers etc.
 */
abstract class GlyphWidget<T : GlyphWidget<T>> : SizableWidget<T> {
    /**
     * Redraws this widget's glyph if the widget is visible and laid out. Called automatically
     * whenever the widget is laid out. Note that this is not the same as [.invalidate].
     * That's protected and causes all parent containers to re-layout. This simply updates the image.
     */
    fun render() {
        if (isShowing && _glyph.layer() != null) {
            val canvas = _glyph.begin()
            paint(canvas)
            _glyph.end()
        }
    }

    /**
     * Creates a new glyph widget with no initial size and optionally interactive. The widget will
     * not be functional until one of the sizing methods is called (in [SizableWidget].
     */
    protected constructor()

    /**
     * Creates a new glyph widget with the given preferred size.
     */
    protected constructor(width: Float, height: Float) {
        preferredSize.update(width, height)
    }

    /**
     * Returns a slot that calls render. Useful if your [.paint] method uses a react
     * value.
     */
    protected fun renderSlot(): UnitSlot = { render() }

    /**
     * Paints this widget onto the given canvas. This is called by render. The canvas is from
     * the [._glyph] member, which is already prepared to the correct laid out size.
     */
    protected abstract fun paint(canvas: Canvas)

    override fun layout(ldata: LayoutData, left: Float, top: Float,
                        width: Float, height: Float) {
        super.layout(ldata, left, top, width, height)

        // prepare the glyph
        if (width == 0f && height == 0f) {
            _glyph.close()
            return
        }

        _glyph.prepare(root()!!.iface.plat.graphics, width, height)
        _glyph.layer()!!.setTranslation(left, top)
        render()
    }

    protected val _glyph = Glyph(layer)
}
