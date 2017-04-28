//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.i

/**
 * Math utility methods.
 */
object MathUtil {
    /**
     * Clamps the supplied `value` to between `low` and `high` (both inclusive).
     */
    fun clamp(value: Int, low: Int, high: Int): Int {
        if (value < low) return low
        if (value > high) return high
        return value
    }

    /**
     * Computes the floored division `dividend/divisor` which is useful when dividing
     * potentially negative numbers into bins.

     *
     *  For example, the following numbers `floorDiv` 10 are:
     * <pre>
     * -15 -10 -8 -2 0 2 8 10 15
     * -2  -1 -1 -1 0 0 0  1  1
    </pre> *
     */
    fun floorDiv(dividend: Int, divisor: Int): Int {
        val numpos = dividend >= 0
        val denpos = divisor >= 0
        if (numpos == denpos) return dividend / divisor
        return if (denpos) (dividend - divisor + 1) / divisor else (dividend - divisor - 1) / divisor
    }
}
