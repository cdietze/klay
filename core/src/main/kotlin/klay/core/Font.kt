package klay.core

/**
 * Contains metadata for a font.
 */
class Font
/** Creates a font as specified.  */
(
        /** The name of this font.  */
        val name: String,
        /** The style of this font.  */
        val style: Font.Style,
        /** The point size of this font.  */
        val size: Float) {

    /** The styles that may be requested for a given font.  */
    enum class Style {
        PLAIN, BOLD, ITALIC, BOLD_ITALIC
    }

    /** Creates a font as specified with [Style.PLAIN]..  */
    constructor(name: String, size: Float) : this(name, Style.PLAIN, size) {}

    /** Derives a font with the same name and style as this one, at the specified size.  */
    fun derive(size: Float): Font {
        return Font(name, style, size)
    }

    override fun hashCode(): Int {
        return name.hashCode() xor style.hashCode() xor size.toInt()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Font) return false
        val ofont = other
        return name == ofont.name && style == ofont.style && size == ofont.size
    }

    override fun toString(): String {
        return name + " " + style + " " + size + "pt"
    }
}
