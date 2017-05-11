package klay.jvm

import klay.core.buffers.*
import java.nio.ByteOrder

abstract class JvmBuffer : Buffer {
    abstract val nioBuffer: java.nio.Buffer

    override fun rewind(): Unit {
        nioBuffer.rewind()
    }

    override fun limit(): Int = nioBuffer.limit()
    override fun limit(length: Int) {
        nioBuffer.limit(length)
    }

    override fun position(): Int = nioBuffer.position()

    override fun position(i: Int) {
        nioBuffer.position(i)
    }

    override fun capacity(): Int = nioBuffer.capacity()
}

class JvmBuffers : klay.core.GL20.Buffers() {
    override fun createByteBuffer(size: Int): ByteBuffer {
        return JvmByteBuffer(java.nio.ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder()))
    }
}

class JvmByteBuffer(override val nioBuffer: java.nio.ByteBuffer) : JvmBuffer(), ByteBuffer {
    override fun get(): Byte = nioBuffer.get()

    override fun get(dst: ByteArray, offset: Int, length: Int) {
        nioBuffer.get(dst, offset, length)
    }

    override fun put(src: ByteArray, offset: Int, length: Int) {
        nioBuffer.put(src, offset, length)
    }

    override fun asShortBuffer(): ShortBuffer = JvmShortBuffer(nioBuffer.asShortBuffer())
    override fun asIntBuffer(): IntBuffer = JvmIntBuffer(nioBuffer.asIntBuffer())
    override fun asFloatBuffer(): FloatBuffer = JvmFloatBuffer(nioBuffer.asFloatBuffer())
    override fun asDoubleBuffer(): DoubleBuffer = JvmDoubleBuffer(nioBuffer.asDoubleBuffer())
}

class JvmIntBuffer(private val intBuffer: java.nio.IntBuffer) : JvmBuffer(), IntBuffer {
    override val nioBuffer: java.nio.IntBuffer = intBuffer
    override fun get(): Int = intBuffer.get()
    override fun get(i: Int): Int = intBuffer.get(i)
    override fun get(dst: IntArray, offset: Int, length: Int) {
        intBuffer.get(dst, offset, length)
    }

    override fun put(n: Int): IntBuffer {
        intBuffer.put(n)
        return this
    }

    override fun put(src: IntArray, offset: Int, length: Int): IntBuffer {
        intBuffer.put(src, offset, length)
        return this
    }
}

class JvmShortBuffer(private val shortBuffer: java.nio.ShortBuffer) : JvmBuffer(), ShortBuffer {
    override val nioBuffer: java.nio.ShortBuffer = shortBuffer
    override fun get(): Short = shortBuffer.get()
    override fun get(i: Int): Short = shortBuffer.get(i)
    override fun get(dst: ShortArray, offset: Int, length: Int) {
        shortBuffer.get(dst, offset, length)
    }

    override fun put(n: Short): ShortBuffer {
        shortBuffer.put(n)
        return this
    }

    override fun put(src: ShortArray, offset: Int, length: Int): ShortBuffer {
        shortBuffer.put(src, offset, length)
        return this
    }
}

class JvmFloatBuffer(private val floatBuffer: java.nio.FloatBuffer) : JvmBuffer(), FloatBuffer {
    override val nioBuffer: java.nio.FloatBuffer = floatBuffer
    override fun get(): Float = floatBuffer.get()
    override fun get(i: Int): Float = floatBuffer.get(i)
    override fun get(dst: FloatArray, offset: Int, length: Int) {
        floatBuffer.get(dst, offset, length)
    }

    override fun put(n: Float): FloatBuffer {
        floatBuffer.put(n)
        return this
    }

    override fun put(src: FloatArray, offset: Int, length: Int): FloatBuffer {
        floatBuffer.put(src, offset, length)
        return this
    }
}

class JvmDoubleBuffer(private val doubleBuffer: java.nio.DoubleBuffer) : JvmBuffer(), DoubleBuffer {
    override val nioBuffer: java.nio.DoubleBuffer = doubleBuffer
    override fun get(): Double = doubleBuffer.get()
    override fun get(i: Int): Double = doubleBuffer.get(i)
    override fun get(dst: DoubleArray, offset: Int, length: Int) {
        doubleBuffer.get(dst, offset, length)
    }

    override fun put(n: Double): DoubleBuffer {
        doubleBuffer.put(n)
        return this
    }

    override fun put(src: DoubleArray, offset: Int, length: Int): DoubleBuffer {
        doubleBuffer.put(src, offset, length)
        return this
    }
}