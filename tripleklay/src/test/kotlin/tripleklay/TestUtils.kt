package tripleklay

import kotlin.math.abs
import kotlin.test.assertTrue

/** Asserts that the difference between `expected` and `actual` is less than or equal to `delta`. */
fun assertEqualsWithDelta(expected: Float, actual: Float, delta: Float, message: String? = null) {
    assertTrue(abs(expected - actual) <= delta, message)
}
