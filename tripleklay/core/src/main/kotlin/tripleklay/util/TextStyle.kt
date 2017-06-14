package tripleklay.util

import klay.core.Font
import klay.core.TextFormat

/**
 * Describes everything needed to style a particular run of text.
 */
data class TextStyle
/**
 * Creates a text style with the specified configuration.
 */
(override val font: Font?, override val antialias: Boolean,
 /** The color used to render the text.  */
 val textColor: Int,
 /** The text effect used when rendering the text.  */
 val effect: EffectRenderer,
 /** Whether or not the text is underlined.  */
 val underlined: Boolean) : TextFormat(font, antialias) {

    init {
        assert(effect != null)
    }

    override fun withFont(font: Font): TextStyle {
        return TextStyle(font, antialias, textColor, effect, underlined)
    }

    override fun withAntialias(antialias: Boolean): TextStyle {
        return TextStyle(font, antialias, textColor, effect, underlined)
    }

    /**
     * Returns a copy of this text style with the color configured as `textColor`.
     */
    fun withTextColor(textColor: Int): TextStyle {
        return TextStyle(font, antialias, textColor, effect, underlined)
    }

    /**
     * Returns a copy of this text style with the effect configured as `effect`.
     */
    fun withEffect(effect: EffectRenderer): TextStyle {
        return TextStyle(font, antialias, textColor, effect, underlined)
    }

    /**
     * Returns a copy of this text style with a shadow text effect.
     */
    fun withShadow(shadowColor: Int, shadowX: Float, shadowY: Float): TextStyle {
        return withEffect(EffectRenderer.Shadow(shadowColor, shadowX, shadowY))
    }

    /**
     * Returns a copy of this text style with a pixel outline text effect.
     */
    fun withOutline(outlineColor: Int): TextStyle {
        return withEffect(EffectRenderer.PixelOutline(outlineColor))
    }

    /**
     * Returns a copy of this text style with a vector outline text effect.
     */
    fun withOutline(outlineColor: Int, outlineWidth: Float): TextStyle {
        return withEffect(EffectRenderer.VectorOutline(outlineColor, outlineWidth))
    }

    /**
     * Returns a copy of this text style with (or without) underlining.
     */
    fun withUnderline(underlined: Boolean): TextStyle {
        return TextStyle(font, antialias, textColor, effect, underlined)
    }

    override fun hashCode(): Int {
        return super.hashCode() xor textColor xor effect.hashCode() xor if (underlined) 1 else 0
    }

    override fun equals(other: Any?): Boolean {
        if (other !is TextStyle) return false
        val os = other
        return super.equals(other) && textColor == os.textColor && effect == os.effect &&
                underlined == os.underlined
    }

    companion object {
        /** A default text style from which custom styles can be derived.  */
        var DEFAULT = TextStyle(null, true, 0xFF000000.toInt(), EffectRenderer.NONE, false)

        /**
         * Creates a text style with the specified configuration using default anti-aliasing (true)
         * and no underline.
         */
        @JvmOverloads fun normal(font: Font, textColor: Int, effect: EffectRenderer = EffectRenderer.NONE): TextStyle {
            return TextStyle(font, true, textColor, effect, false)
        }
    }
}
/**
 * Creates a text style with the specified configuration using default anti-aliasing, no effect
 * and no underline.
 */
