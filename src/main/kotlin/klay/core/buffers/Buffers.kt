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
    fun get(): Byte
    fun get(dst: ByteArray, offset: Int, length: Int)
    fun put(src: ByteArray, offset: Int, length: Int)
    fun asShortBuffer(): ShortBuffer
    fun asIntBuffer(): IntBuffer
    fun asFloatBuffer(): FloatBuffer
    fun asDoubleBuffer(): DoubleBuffer
}

interface ShortBuffer : Buffer {
    fun get(): Short
    fun get(i: Int): Short
    fun get(dst: ShortArray, offset: Int, length: Int)
    fun put(n: Short): ShortBuffer
    fun put(src: ShortArray, offset: Int, length: Int): ShortBuffer
}

interface IntBuffer : Buffer {
    fun get(): Int
    fun get(i: Int): Int
    fun get(dst: IntArray, offset: Int, length: Int)
    fun put(n: Int): IntBuffer
    fun put(src: IntArray, offset: Int, length: Int): IntBuffer
}

interface FloatBuffer : Buffer {
    fun get(): Float
    fun get(i: Int): Float
    fun get(dst: FloatArray, offset: Int, length: Int)
    fun put(n: Float): FloatBuffer
    fun put(src: FloatArray, offset: Int, length: Int): FloatBuffer
}

interface DoubleBuffer : Buffer {
    fun get(): Double
    fun get(i: Int): Double
    fun get(dst: DoubleArray, offset: Int, length: Int)
    fun put(n: Double): DoubleBuffer
    fun put(src: DoubleArray, offset: Int, length: Int): DoubleBuffer
}
