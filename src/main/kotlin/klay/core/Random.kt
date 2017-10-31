package klay.core

expect class Random(seed: Long = 0L) {
    fun nextFloat(): Float
}
