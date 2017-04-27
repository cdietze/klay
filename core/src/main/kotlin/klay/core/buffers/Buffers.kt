package klay.core.buffers

abstract class Buffer {
    abstract fun limit(): Int
    abstract fun position(): Int
}

abstract class ByteBuffer : Buffer() {
    abstract fun put(source: ByteArray, offset: Int, length: Int)
    abstract fun rewind()
    abstract fun capacity(): Int
    abstract fun position(i: Int)
    abstract fun limit(length: Int)
    abstract fun asShortBuffer(): ShortBuffer
    abstract fun asIntBuffer(): IntBuffer
    abstract fun asFloatBuffer(): FloatBuffer
    abstract fun get(params: ByteArray, offset: Int, length: Int)
}

abstract class ShortBuffer : Buffer() {
    abstract fun get(i: Int): Short
    abstract fun put(s: Short)
    abstract fun put(src: ShortArray, offset: Int, length: Int)
    abstract fun rewind()
    abstract fun position(i: Int)
    abstract fun capacity(): Int
    abstract fun limit(length: Int)
}

abstract class IntBuffer : Buffer() {
    abstract fun get(i: Int): Int
    abstract fun get(dst: IntArray, offset: Int, length: Int)
    abstract fun put(n: Int)
    abstract fun put(src: IntArray, offset: Int, length: Int)
    abstract fun rewind()
    abstract fun position(i: Int)
    abstract fun capacity(): Int
    abstract fun limit(length: Int)
}

abstract class FloatBuffer : Buffer() {
    abstract fun get(i: Int): Float
    abstract fun get(dst: FloatArray, offset: Int, length: Int)
    abstract fun put(n: Float)
    abstract fun put(src: FloatArray, offset: Int, length: Int)
    abstract fun rewind()
    abstract fun position(i: Int)
    abstract fun capacity(): Int
    abstract fun limit(length: Int)
}
