package klay.core

import klay.core.buffers.FloatBuffer
import pythagoras.f.*

/**
 * This file contains extension functions for Pythagoras.kt classes.
 */

/**
 * Places the contents of this matrix into the given buffer in the standard OpenGL order.

 * @return a reference to the buffer, for chaining.
 */
fun IMatrix3.get(buf: FloatBuffer): FloatBuffer {
    buf.put(m00).put(m01).put(m02)
    buf.put(m10).put(m11).put(m12)
    buf.put(m20).put(m21).put(m22)
    return buf
}

/**
 * Places the contents of this matrix into the given buffer in the standard OpenGL order.

 * @return a reference to the buffer, for chaining.
 */
fun IMatrix4.get(buf: FloatBuffer): FloatBuffer {
    buf.put(m00).put(m01).put(m02).put(m03)
    buf.put(m10).put(m11).put(m12).put(m13)
    buf.put(m20).put(m21).put(m22).put(m23)
    buf.put(m30).put(m31).put(m32).put(m33)
    return buf
}

/**
 * Creates a matrix from a float buffer.
 */
fun newMatrix(buf: FloatBuffer) = Matrix4().set(buf)

/**
 * Sets the contents of this matrix from the supplied (column-major) buffer.

 * @return a reference to this matrix, for chaining.
 */
fun Matrix4.set(buf: FloatBuffer): Matrix4 {
    m00 = buf.get()
    m01 = buf.get()
    m02 = buf.get()
    m03 = buf.get()
    m10 = buf.get()
    m11 = buf.get()
    m12 = buf.get()
    m13 = buf.get()
    m20 = buf.get()
    m21 = buf.get()
    m22 = buf.get()
    m23 = buf.get()
    m30 = buf.get()
    m31 = buf.get()
    m32 = buf.get()
    m33 = buf.get()
    return this
}

/**
 * Populates the supplied buffer with the contents of this vector.

 * @return a reference to the buffer, for chaining.
 */
fun IVector3.get(buf: FloatBuffer): FloatBuffer = buf.put(x).put(y).put(z)

/**
 * Populates the supplied buffer with the contents of this vector.

 * @return a reference to the buffer, for chaining.
 */
fun IVector4.get(buf: FloatBuffer): FloatBuffer = buf.put(x).put(y).put(z).put(w)

/**
 * Creates a vector from a float buffer.
 */
fun newVector(buf: FloatBuffer): Vector4 = Vector4().set(buf)

/**
 * Sets all of the elements of the vector.

 * @return a reference to this vector, for chaining.
 */
fun Vector4.set(buf: FloatBuffer): Vector4 = set(buf.get(), buf.get(), buf.get(), buf.get())

/**
 * Stores the contents of this plane into the specified buffer.
 */
fun IPlane.get(buf: FloatBuffer): FloatBuffer = buf.put(normal.x).put(normal.y).put(normal.z).put(constant)
