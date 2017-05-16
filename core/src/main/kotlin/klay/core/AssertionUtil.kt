package klay.core

/**
 * Throws an [AssertionError] if [value] is false.
 *
 * We cannot use [kotlin.assert] because it is not supported by the Kotlin JS.
 */
fun assert(value: Boolean): Unit {
    assert(value, { "Assertion failed" })
}

/**
 * Throws an [AssertionError] with [lazyMessage] as message if [value] is false.
 *
 * We cannot use [kotlin.assert] because it is not supported by the Kotlin JS.
 */
fun assert(value: Boolean, lazyMessage: () -> String): Unit {
    if (!value) throw AssertionError(lazyMessage())
}
