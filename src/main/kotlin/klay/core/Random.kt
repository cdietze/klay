package klay.core

expect class Random(seed: Long = 0L) {
    fun nextInt(): Int
    /** @returns a pseudorandom integer between `0` (inclusive) and `high` (exclusive) */
    fun nextInt(high: Int): Int

    fun nextLong(): Long

    fun nextFloat(): Float
    fun nextDouble(): Double

    fun nextBoolean(): Boolean

    fun nextGaussian(): Double
}
