package tripleklay.util

import klay.core.*
import klay.scene.ImageLayer
import pythagoras.f.Rectangle

/**
 * Manages styled text. This comes in many flavors: a single line of plain (uniformly styled) text,
 * multiple lines of plain text, and (coming soon) multiple lines of rich (non-uniformly styled)
 * text.
 */
abstract class StyledText protected constructor(protected val _gfx: Graphics) {
    /** A shared base class for single- and multi-line plain text.  */
    abstract class Plain protected constructor(gfx: Graphics,
                                               /** The text being rendered.  */
                                               val text: String,
                                               /** The stylings applied to this text.  */
                                               val style: TextStyle) : StyledText(gfx) {

        /** Creates a new instance equivalent to this one excepting that the font size is adjusted
         * to `size`. This is useful for auto-shrinking text to fit into fixed space.  */
        abstract fun resize(size: Float): Plain

        override fun toLayer(target: ImageLayer): ImageLayer {
            val canvas = toCanvas()
            target.setTile(canvas.toTexture())
            target.setTranslation(style.effect.offsetX(), style.effect.offsetY())
            return target
        }

        override fun hashCode(): Int {
            return text.hashCode() xor style.hashCode()
        }

        override fun equals(other: Any?): Boolean {
            if (other is Plain) {
                val op = other
                return text == op.text && style == op.style
            } else
                return false
        }
    }

    /** A single line of plain (uniformly styled) text.  */
    class Span(val gfx: Graphics, text: String, style: TextStyle) : Plain(gfx, text, style) {
        private val _layout: TextLayout = gfx.layoutText(text, style)

        override fun width(): Float {
            return style.effect.adjustWidth(_layout.size.width)
        }

        override fun height(): Float {
            return style.effect.adjustHeight(_layout.size.height)
        }

        override fun render(canvas: Canvas, x: Float, y: Float) {
            style.effect.render(canvas, _layout, style.textColor, style.underlined, x, y)
        }

        override fun resize(size: Float): Span {
            return Span(gfx, text, style.withFont(style.font!!.derive(size)))
        }

        override fun equals(other: Any?): Boolean {
            return other is Span && super.equals(other)
        }

        override fun toString(): String {
            return "Span '$text' @ $style"
        }
    }

    /** Multiple lines of plain (uniformly styled) text.  */
    class Block(val gfx: Graphics, text: String, style: TextStyle,
                /** The text wrap configuration, unused if not wrapping.  */
                val wrap: TextWrap,
                /** The alignment of wrapped text, unused if not wrapping.  */
                val align: TextBlock.Align) : Plain(gfx, text, style) {

        private val _layouts: Array<out TextLayout> = gfx.layoutText(text, style, wrap)
        private val _bounds: Rectangle

        init {
            _bounds = TextBlock.getBounds(_layouts, Rectangle())
            _bounds.width = style.effect.adjustWidth(_bounds.width)
            _bounds.height = style.effect.adjustHeight(_bounds.height)
        }

        override fun width(): Float {
            return _bounds.width
        }

        override fun height(): Float {
            return _bounds.height
        }

        override fun render(canvas: Canvas, x: Float, y: Float) {
            val bx = _bounds.x
            var ly = y + _bounds.y
            for (layout in _layouts) {
                val lx = x + bx + align.getX(style.effect.adjustWidth(layout.size.width),
                        _bounds.width - _bounds.x)
                style.effect.render(canvas, layout, style.textColor, style.underlined, lx, ly)
                ly += layout.ascent() + layout.descent() + layout.leading()
            }
        }

        override fun resize(size: Float): Block {
            return Block(gfx, text, style.withFont(style.font!!.derive(size)), wrap, align)
        }

        override fun hashCode(): Int {
            return super.hashCode() xor wrap.hashCode() xor align.hashCode()
        }

        override fun equals(other: Any?): Boolean {
            if (other is Plain) {
                val op = other
                return text == op.text && style == op.style
            } else
                return false
        }

        override fun toString(): String {
            return "Block '$text' @ $style/$wrap/$align"
        }
    }

    /** The width of this styled text when rendered.  */
    abstract fun width(): Float

    /** The height of this styled text when rendered.  */
    abstract fun height(): Float

    /** Renders this styled text into the supplied canvas at the specified offset.  */
    abstract fun render(canvas: Canvas, x: Float, y: Float)

    /** Creates a canvas large enough to accommodate this styled text, and renders it therein. The
     * canvas will include a one pixel border beyond the size of the styled text which is needed
     * to accommodate antialiasing.  */
    fun toCanvas(): Canvas {
        val pad = 1 / _gfx.scale().factor
        val canvas = _gfx.createCanvas(width() + 2 * pad, height() + 2 * pad)
        render(canvas, pad, pad)
        return canvas
    }

    /** Creates an image large enough to accommodate this styled text, renders it therein and
     * returns an image layer with its translation adjusted per the effect renderer.  */
    fun toLayer(): ImageLayer {
        return toLayer(ImageLayer())
    }

    /** Creates an image large enough to accommodate this styled text, renders it therein and
     * applies it to `layer`, adjusting its translation per the effect renderer.  */
    abstract fun toLayer(target: ImageLayer): ImageLayer

    companion object {

        /** Creates a uniformly formatted single-line of text.  */
        fun span(gfx: Graphics, text: String, style: TextStyle): Span {
            return Span(gfx, text, style)
        }

        /** Creates a uniformly formatted multiple-lines of text wrapped at `wrapWidth` and
         * left-aligned.  */
        fun block(gfx: Graphics, text: String, style: TextStyle, wrapWidth: Float): Block {
            return Block(gfx, text, style, TextWrap(wrapWidth), TextBlock.Align.LEFT)
        }
    }
}
