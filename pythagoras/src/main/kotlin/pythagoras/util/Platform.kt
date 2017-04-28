//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.util

/**
 * Handles differences between the JDK and GWT platforms.
 */
object Platform {
    /**
     * Returns a hash code for the supplied float value.
     */
    fun hashCode(f1: Float): Int {
        return java.lang.Float.floatToIntBits(f1)
    }

    /**
     * Returns a hash code for the supplied double value.
     */
    fun hashCode(d1: Double): Int {
        val bits = java.lang.Double.doubleToLongBits(d1)
        return (bits xor bits.ushr(32)).toInt()
    }

    /**
     * Clones the supplied array of bytes.
     */
    fun clone(values: ByteArray): ByteArray {
        return values.clone()
    }

    /**
     * Clones the supplied array of ints.
     */
    fun clone(values: IntArray): IntArray {
        return values.clone()
    }

    /**
     * Clones the supplied array of floats.
     */
    fun clone(values: FloatArray): FloatArray {
        return values.clone()
    }

    /**
     * Clones the supplied array of doubles.
     */
    fun clone(values: DoubleArray): DoubleArray {
        return values.clone()
    }
}
