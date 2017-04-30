package java.lang

/**
 * Implementation needed for JS backend.
 */
object System {
    /** Not quite the original signature which uses `java.lang.Object`, but we really just want to copy arrays */
    fun arraycopy(src: FloatArray, srcPos: Int, dest: FloatArray, destPos: Int, length: Int): Unit {
        TODO("NYI")
    }
    fun arraycopy(src: IntArray, srcPos: Int, dest: IntArray, destPos: Int, length: Int): Unit {
        TODO("NYI")
    }
    fun arraycopy(src: ByteArray, srcPos: Int, dest: ByteArray, destPos: Int, length: Int): Unit {
        TODO("NYI")
    }
}
