package tripleklay.util

import klay.core.Color

/**
 * Utilities and constants for colors.
 */
object Colors {
    /** Named versions of commonly used colors.  */
    val WHITE = Color.rgb(255, 255, 255)
    val LIGHT_GRAY = Color.rgb(192, 192, 192)
    val GRAY = Color.rgb(128, 128, 128)
    val DARK_GRAY = Color.rgb(64, 64, 64)
    val BLACK = Color.rgb(0, 0, 0)
    val RED = Color.rgb(255, 0, 0)
    val PINK = Color.rgb(255, 175, 175)
    val ORANGE = Color.rgb(255, 200, 0)
    val YELLOW = Color.rgb(255, 255, 0)
    val GREEN = Color.rgb(0, 255, 0)
    val MAGENTA = Color.rgb(255, 0, 255)
    val CYAN = Color.rgb(0, 255, 255)
    val BLUE = Color.rgb(0, 0, 255)

    /**
     * Blends two colors.
     * @return a color halfway between the two colors.
     */
    fun blend(c1: Int, c2: Int): Int {
        return Color.rgb(Color.red(c1) + Color.red(c2) shr 1,
                Color.green(c1) + Color.green(c2) shr 1,
                Color.blue(c1) + Color.blue(c2) shr 1)
    }

    /**
     * Blends two colors proportionally.
     * @param p1 The percentage of the first color to use, from 0.0f to 1.0f inclusive.
     */
    fun blend(c1: Int, c2: Int, p1: Float): Int {
        val p2 = 1 - p1
        return Color.rgb((Color.red(c1) * p1 + Color.red(c2) * p2) as Int,
                (Color.green(c1) * p1 + Color.green(c2) * p2) as Int,
                (Color.blue(c1) * p1 + Color.blue(c2) * p2) as Int)
    }

    /**
     * Creates a new darkened version of the given color. This is implemented by composing a new
     * color consisting of the components of the original color, each multiplied by the dark factor.
     * The alpha channel is copied from the original.
     */
    fun darker(color: Int, darkFactor: Float = DARK_FACTOR): Int {
        return Color.argb(Color.alpha(color),
                maxOf((Color.red(color) * darkFactor).toInt(), 0),
                maxOf((Color.green(color) * darkFactor).toInt(), 0),
                maxOf((Color.blue(color) * darkFactor).toInt(), 0))
    }

    /**
     * Creates a new brightened version of the given color. This is implemented by composing a new
     * color consisting of the components of the original color, each multiplied by 10/7, with
     * exceptions for zero-valued components. The alpha channel is copied from the original.
     */
    fun brighter(color: Int): Int {
        val a = Color.alpha(color)
        var r = Color.red(color)
        var g = Color.green(color)
        var b = Color.blue(color)

        // black is a special case the just goes to dark gray
        if (r == 0 && g == 0 && b == 0) return Color.argb(a, MIN_BRIGHT, MIN_BRIGHT, MIN_BRIGHT)

        // bump each component up to the minumum, unless it is absent
        if (r != 0) r = maxOf(MIN_BRIGHT, r)
        if (g != 0) g = maxOf(MIN_BRIGHT, g)
        if (b != 0) b = maxOf(MIN_BRIGHT, b)

        // scale
        return Color.argb(a,
                minOf((r * BRIGHT_FACTOR).toInt(), 255),
                minOf((g * BRIGHT_FACTOR).toInt(), 255),
                minOf((b * BRIGHT_FACTOR).toInt(), 255))
    }

    private val DARK_FACTOR = 0.7f
    private val BRIGHT_FACTOR = 1 / DARK_FACTOR
    private val MIN_BRIGHT = 3 // (int)(1.0 / (1.0 - DARK_FACTOR));
}
/**
 * Creates a new darkened version of the given color with the default DARK_FACTOR.
 */
