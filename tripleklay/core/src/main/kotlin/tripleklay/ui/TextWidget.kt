package tripleklay.ui

import klay.core.TextWrap
import klay.scene.Layer
import pythagoras.f.Dimension
import pythagoras.f.MathUtil
import react.Slot
import react.UnitSlot
import tripleklay.util.Glyph
import tripleklay.util.StyledText

/**
 * An abstract base class for widgets that contain text.
 */
abstract class TextWidget<T : TextWidget<T>> : Widget<T>() {
    /**
     * Returns the current text displayed by this widget, or null if it has no text.
     */
    protected abstract fun text(): String?

    /**
     * Returns the current icon displayed by this widget, or null if it has no icon.
     */
    protected abstract fun icon(): Icon?

    /**
     * Returns a slot that subclasses should wire up to their text `Value`.
     */
    protected fun textDidChange(): UnitSlot {
        return invalidateSlot(true)
    }

    /**
     * Returns a slot that subclasses should wire up to their icon `Value`.
     */
    protected fun iconDidChange(): Slot<Icon?> {
        return {
            icon: Icon? ->
            if (icon == null) {
                clearLayoutData()
                invalidate()
            } else {
                icon.state().onSuccess({ resource: Icon ->
                    // clear out the rendered icon in case we got laid out before the async
                    // load finished
                    _renderedIcon = null
                    clearLayoutData()
                    invalidate()
                })
            }
        }
    }

    override fun wasRemoved() {
        super.wasRemoved()
        _tglyph.close()
        if (_ilayer != null) {
            _ilayer!!.close()
            _ilayer = null
        }
        _renderedIcon = null
    }

    override fun createLayoutData(hintX: Float, hintY: Float): LayoutData {
        return TextLayoutData(hintX, hintY)
    }

    protected open inner class TextLayoutData(hintX: Float, hintY: Float) : LayoutData() {
        val halign: Style.HAlign = resolveStyle(Style.HALIGN)
        val valign: Style.VAlign = resolveStyle(Style.VALIGN)
        val iconPos: Style.Pos? = resolveStyle(Style.ICON_POS)
        val iconGap = resolveStyle(Style.ICON_GAP)
        val iconCuddle = resolveStyle(Style.ICON_CUDDLE)
        val iconEffect = resolveStyle(Style.ICON_EFFECT)
        val wrap = resolveStyle(Style.TEXT_WRAP)
        val autoShrink = resolveStyle(Style.AUTO_SHRINK)

        val gfx = root()!!.iface.plat.graphics
        var text: StyledText.Plain? = null // mostly final, only changed by autoShrink
        val icon: Icon?

        init {
            val curtext = text()
            val haveText = curtext != null && curtext.isNotEmpty()

            // start with hints minus background insets
            val hints = bg.insets.subtractFrom(Dimension(hintX, hintY))

            // apply effects to the icon, if we have one
            val icon = icon()
            this.icon = if (icon == null) null else iconEffect.apply(icon)

            // accommodate our icon
            accommodateIcon(hints, haveText)

            // layout our text, if we have any
            if (haveText) {
                val style = Style.createTextStyle(this@TextWidget)
                // TODO: should we do something with a y-hint?
                if (hints.width > 0 && wrap) {
                    text = StyledText.Block(gfx, curtext!!, style, TextWrap(hints.width),
                            Style.toAlignment(resolveStyle(Style.HALIGN)))
                } else {
                    text = StyledText.Span(gfx, curtext!!, style)
                }
            }
        }

        override fun computeSize(hintX: Float, hintY: Float): Dimension {
            if (text != null && autoShrink) {
                var usedWidth = 0f
                // account for the icon width and gap
                if (icon != null && iconPos!!.horizontal()) usedWidth = icon.width() + iconGap
                // if autoShrink is enabled, and our text is too wide, re-lay it out with
                // successively smaller fonts until it fits
                var twidth = textWidth()
                val availWidth = hintX - usedWidth
                if (twidth > availWidth) {
                    while (twidth > availWidth && text!!.style.font!!.size > MIN_FONT_SIZE) {
                        text = text!!.resize(text!!.style.font!!.size - 1)
                        twidth = MathUtil.ceil(textWidth())
                    }
                }
            }

            val size = Dimension()
            addTextSize(size)
            if (icon != null) {
                if (iconPos!!.horizontal()) {
                    size.width += icon.width()
                    if (text != null) size.width += iconGap.toFloat()
                    size.height = Math.max(size.height, icon.height())
                } else {
                    size.width = Math.max(size.width, icon.width())
                    size.height += icon.height()
                    if (text != null) size.height += iconGap.toFloat()
                }
            }

            return size
        }

        override fun layout(left: Float, top: Float, width: Float, height: Float) {
            var tx = left
            var ty = top
            var usedWidth = 0f
            var usedHeight = 0f

            if (icon != null && iconPos != null) {
                var ix = left
                var iy = top
                val iwidth = icon.width()
                val iheight = icon.height()
                when (iconPos) {
                    Style.Pos.LEFT -> {
                        tx += iwidth + iconGap
                        iy += valign.offset(iheight, height)
                        usedWidth = iwidth + iconGap
                    }
                    Style.Pos.ABOVE -> {
                        ty += iheight + iconGap
                        ix += halign.offset(iwidth, width)
                        usedHeight = iheight + iconGap
                    }
                    Style.Pos.RIGHT -> {
                        ix += width - iwidth
                        iy += valign.offset(iheight, height)
                        usedWidth = iwidth + iconGap
                    }
                    Style.Pos.BELOW -> {
                        iy += height - iheight
                        ix += halign.offset(iwidth, width)
                        usedHeight = iheight + iconGap
                    }
                }
                if (_renderedIcon === icon) {
                    // This is the same icon, just reposition its layer
                    _ilayer!!.setTranslation(ix, iy)
                } else {
                    // Otherwise, dispose and recreate
                    if (_ilayer != null) _ilayer!!.close()
                    _ilayer = icon.render()
                    layer.addAt(_ilayer!!, ix, iy)
                }

            } else if (icon == null && _ilayer != null) {
                _ilayer!!.close()
                _ilayer = null
            }
            _renderedIcon = icon

            if (text == null)
                _tglyph.close()
            else {
                updateTextGlyph(tx, ty, width - usedWidth, height - usedHeight)
                // if we're cuddling, adjust icon position based on the now known tex position
                if (_ilayer != null && iconCuddle) {
                    val tlayer = _tglyph.layer()
                    val ctx = tlayer?.tx() ?: 0f
                    val cty = tlayer?.ty() ?: 0f
                    var ix = _ilayer!!.tx()
                    var iy = _ilayer!!.ty()
                    val iwid = icon!!.width()
                    val ihei = icon.height()
                    when (iconPos) {
                        Style.Pos.LEFT -> ix = ctx - iwid - iconGap.toFloat()
                        Style.Pos.ABOVE -> iy = cty - ihei - iconGap.toFloat()
                        Style.Pos.RIGHT -> ix = ctx + textWidth() + iconGap.toFloat()
                        Style.Pos.BELOW -> iy = cty + textHeight() + iconGap.toFloat()
                    }
                    _ilayer!!.setTranslation(ix, iy)
                }
            }
        }

        override fun toString(): String {
            return "TextLayoutData[text=$text, icon=$icon]"
        }

        // this is broken out so that subclasses can extend this action
        protected fun accommodateIcon(hints: Dimension, haveText: Boolean) {
            if (icon != null) {
                // remove the icon space from our hint dimensions
                if (iconPos!!.horizontal()) {
                    hints.width -= icon.width()
                    if (haveText) hints.width -= iconGap.toFloat()
                } else {
                    hints.height -= icon.height()
                    if (haveText) hints.height -= iconGap.toFloat()
                }
            }
        }

        // this is broken out so that subclasses can extend this action
        protected fun addTextSize(size: Dimension) {
            if (_constraint is Constraints.TextConstraint) {
                val tsize = if (text == null) null else Dimension(textWidth(), textHeight())
                (_constraint as Constraints.TextConstraint).addTextSize(size, tsize)
            } else if (text != null) {
                size.width += textWidth()
                size.height += textHeight()
            }
        }

        // this is broken out so that subclasses can extend this action
        protected fun updateTextGlyph(tx: Float, ty: Float, availWidth: Float, availHeight: Float) {
            var twidth = MathUtil.ceil(textWidth())
            var theight = MathUtil.ceil(textHeight())
            val awidth = MathUtil.ceil(availWidth)
            val aheight = MathUtil.ceil(availHeight)
            if (twidth <= 0 || theight <= 0 || awidth <= 0 || aheight <= 0) return

            // if autoShrink is enabled, and our text is too wide, re-lay it out with successively
            // smaller fonts until it fits
            if (autoShrink && twidth > availWidth) {
                while (twidth > availWidth && text!!.style.font!!.size > MIN_FONT_SIZE) {
                    text = text!!.resize(text!!.style.font!!.size - 1)
                    twidth = MathUtil.ceil(textWidth())
                }
                theight = MathUtil.ceil(textHeight())
            }

            // create a canvas no larger than the text, constrained to the available size
            val tgwidth = Math.min(awidth, twidth)
            val tgheight = Math.min(aheight, theight)

            // we do some extra fiddling here because one may want to constrain the height of a
            // button such that the text is actually cut off on the top and/or bottom because fonts
            // may have lots of whitespace above or below and you're trying to squeeze the text
            // snugly into your button
            val ox = MathUtil.ifloor(halign.offset(twidth, awidth)).toFloat()
            val oy = MathUtil.ifloor(valign.offset(theight, aheight)).toFloat()

            // only re-render our text if something actually changed
            if (text != _renderedText || tgwidth != _tglyph.preparedWidth() ||
                    tgheight != _tglyph.preparedHeight()) {
                _tglyph.prepare(root()!!.iface.plat.graphics, tgwidth, tgheight)
                val canvas = _tglyph.begin()
                text!!.render(canvas, Math.min(ox, 0f), Math.min(oy, 0f))
                _tglyph.end()
                _renderedText = text!!
            }

            // always set the translation since other non-text style changes can affect it
            _tglyph.layer()!!.setTranslation(tx + Math.max(ox, 0f) + text!!.style.effect.offsetX(),
                    ty + Math.max(oy, 0f) + text!!.style.effect.offsetY())
        }

        protected fun textWidth(): Float {
            return text!!.width()
        }

        protected fun textHeight(): Float {
            return text!!.height()
        }
    }

    protected val _tglyph = Glyph(layer)
    protected var _renderedText: StyledText.Plain? = null
    protected var _ilayer: Layer? = null
    protected var _renderedIcon: Icon? = null

    companion object {

        protected val MIN_FONT_SIZE = 6f // TODO: make customizable?
    }
}
