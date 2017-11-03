package klay.core

actual class Random actual constructor(seed: Long = 0L) {
    private val r = java.util.Random(seed)

    actual fun nextInt(): Int = r.nextInt()
    actual fun nextInt(high: Int): Int = r.nextInt(high)
    actual fun nextLong(): Long = r.nextLong()
    actual fun nextFloat(): Float = r.nextFloat()
    actual fun nextDouble(): Double = r.nextDouble()
    actual fun nextBoolean(): Boolean = r.nextBoolean()
    actual fun nextGaussian(): Double = r.nextGaussian()
}
