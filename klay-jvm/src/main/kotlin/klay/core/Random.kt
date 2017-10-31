package klay.core

actual class Random actual constructor(seed: Long = 0L) {
    private val r = java.util.Random(seed)
    actual fun nextFloat(): Float = r.nextFloat()
}
