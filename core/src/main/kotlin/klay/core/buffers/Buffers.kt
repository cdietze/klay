package klay.core.buffers

interface Buffer {
    fun rewind()
    fun limit(): Int
    fun limit(length: Int)
    fun position(): Int
    fun position(i: Int)
    fun capacity(): Int
}

interface ByteBuffer : Buffer {
    fun get(dst: ByteArray, offset: Int, length: Int)
    fun put(src: ByteArray, offset: Int, length: Int)
    fun asShortBuffer(): ShortBuffer
    fun asIntBuffer(): IntBuffer
    fun asFloatBuffer(): FloatBuffer
    fun asDoubleBuffer(): DoubleBuffer
}

interface ShortBuffer : Buffer {
    fun get(i: Int): Short
    fun get(dst: ShortArray, offset: Int, length: Int)
    fun put(n: Short)
    fun put(src: ShortArray, offset: Int, length: Int)
}

interface IntBuffer : Buffer {
    fun get(i: Int): Int
    fun get(dst: IntArray, offset: Int, length: Int)
    fun put(n: Int)
    fun put(src: IntArray, offset: Int, length: Int)
}

interface FloatBuffer : Buffer {
    fun get(i: Int): Float
    fun get(dst: FloatArray, offset: Int, length: Int)
    fun put(n: Float)
    fun put(src: FloatArray, offset: Int, length: Int)
}

interface DoubleBuffer : Buffer {
    fun get(i: Int): Double
    fun get(dst: DoubleArray, offset: Int, length: Int)
    fun put(n: Double)
    fun put(src: DoubleArray, offset: Int, length: Int)
}
