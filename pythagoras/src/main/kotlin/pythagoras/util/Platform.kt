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
        return f1.hashCode()
    }

    /**
     * Returns a hash code for the supplied double value.
     */
    fun hashCode(d1: Double): Int {
        return d1.hashCode()
    }

    /**
     * Clones the supplied array of bytes.
     */
    fun clone(values: ByteArray): ByteArray {
        return values.copyOf()
    }

    /**
     * Clones the supplied array of ints.
     */
    fun clone(values: IntArray): IntArray {
        return values.copyOf()
    }

    /**
     * Clones the supplied array of floats.
     */
    fun clone(values: FloatArray): FloatArray {
        return values.copyOf()
    }

    /**
     * Clones the supplied array of doubles.
     */
    fun clone(values: DoubleArray): DoubleArray {
        return values.copyOf()
    }
}
