package tripleklay.util

/**
 * Utilities that apply to all objects. This will mostly reflect ideas originally consolidated
 * in com.google.common.base, but not roping in the whole of guava.
 */
object Objects {
    /**
     * Tests if two objects match according to reference equality, or [Object.equals]
     * if both are non-null.
     */
    fun equal(o1: Any?, o2: Any): Boolean {
        return o1 === o2 || o1 != null && o1 == o2
    }
}
