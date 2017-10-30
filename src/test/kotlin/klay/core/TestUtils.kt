package klay.core

import kotlin.math.abs
import kotlin.test.assertTrue

/** Assert that the difference between `expected` and `actual` is less than or equal to `delta`. */
fun assertEquals(expected: Float, actual: Float, delta: Float, message: String? = null) {
    assertTrue(abs(expected - actual) <= delta, message)
}

/** Assert that the difference between `expected` and `actual` is less than or equal to `delta`. */
fun assertEquals(expected: Double, actual: Double, delta: Double, message: String? = null) {
    assertTrue(abs(expected - actual) <= delta, message)
}

/** @see https://docs.oracle.com/javase/8/docs/api/java/lang/Double.html#MIN_NORMAL */
val Double.Companion.MIN_NORMAL get() = Double.fromBits(0x0010000000000000L)
