package klay.core

/**
 * Utility methods for working with packed-integer colors.
 */
object Color {

    /**
     * Creates a packed integer color from four ARGB values in the range [0, 255].
     */
    fun argb(a: Int, r: Int, g: Int, b: Int): Int {
        return a shl 24 or (r shl 16) or (g shl 8) or b
    }

    /**
     * Creates a packed integer color from three RGB values in the range [0, 255].
     */
    fun rgb(r: Int, g: Int, b: Int): Int {
        return argb(0xff, r, g, b)
    }

    /**
     * Extracts the alpha, in range [0, 255], from the given packed color.
     */
    fun alpha(color: Int): Int {
        return color shr 24 and 0xFF
    }

    /**
     * Extracts the red component, in range [0, 255], from the given packed color.
     */
    fun red(color: Int): Int {
        return color shr 16 and 0xFF
    }

    /**
     * Extracts the green component, in range [0, 255], from the given packed color.
     */
    fun green(color: Int): Int {
        return color shr 8 and 0xFF
    }

    /**
     * Extracts the blue component, in range [0, 255], from the given packed color.
     */
    fun blue(color: Int): Int {
        return color and 0xFF
    }

    /**
     * Returns a new color that's a copy of the given color, but with the new alpha value, in
     * range [0, 255].
     */
    fun withAlpha(color: Int, alpha: Int): Int {
        return color and 0x00ffffff or (alpha shl 24)
    }

    /**
     * Encodes two [0..1] color values into the format used by the standard shader program. The
     * standard shader program delivers tinting information as two floats per vertex (AR and GB).
     */
    fun encode(upper: Float, lower: Float): Float {
        val upquant = (upper * 255).toInt()
        val lowquant = (lower * 255).toInt()
        return (upquant * 256 + lowquant).toFloat()
    }

    /**
     * Decodes and returns the upper color value in a two color value encoded by [.encode].
     */
    fun decodeUpper(encoded: Float): Float {
        val lower = encoded % 256
        return (encoded - lower) / 255
    }

    /**
     * Decodes and returns the lower color value in a two color value encoded by [.encode].
     */
    fun decodeLower(encoded: Float): Float {
        return encoded % 256 / 255
    }
}
