package klay.core

/**
 * Contains info for laying out and drawing single- or multi-line text to a [Canvas].
 */
class TextFormat
/** Creates a configured text format instance.  */
constructor(
        /** The font in which to render the text (null indicates that the default font is used).  */
        val font: Font? = null,
        /** Whether or not the text should be antialiased. Defaults to true.
         * NOTE: this is not supported by the HTML5 backend.  */
        val antialias: Boolean = true) {

    /** Returns a clone of this text format with the font configured as specified.  */
    fun withFont(font: Font): TextFormat {
        return TextFormat(font, this.antialias)
    }

    /** Returns a clone of this text format with the font configured as specified.  */
    fun withFont(name: String, style: Font.Style, size: Float): TextFormat {
        return withFont(Font(name, style, size))
    }

    /** Returns a clone of this text format with the font configured as specified.  */
    fun withFont(name: String, size: Float): TextFormat {
        return withFont(Font(name, size))
    }

    /** Returns a clone of this text format with [.antialias] configured as specified.  */
    fun withAntialias(antialias: Boolean): TextFormat {
        return TextFormat(this.font, antialias)
    }

    override fun toString(): String {
        return "[font=$font, antialias=$antialias]"
    }

    override fun equals(other: Any?): Boolean {
        if (other is TextFormat) {
            val ofmt = other
            return (font === ofmt.font || font != null && font == ofmt.font) && antialias == ofmt.antialias
        } else {
            return false
        }
    }

    override fun hashCode(): Int {
        var hash = if (antialias) 1 else 0
        if (font != null) hash = hash xor font.hashCode()
        return hash
    }
}
/** Creates a default text format instance.  */
/** Creates a text format instance with the specified font.  */
