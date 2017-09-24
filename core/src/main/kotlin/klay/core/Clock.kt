package klay.core

/**
 * Encapsulates an absolute and delta time.
 * Used by [Game] to emit simulation update signals.
 */
interface Clock {

    /** The number of milliseconds that have elapsed since time 0.  */
    val tick: Int

    /** The number of milliseconds that have elapsed since the last signal.
     * For a simulation update this is equal to [Game.updateRate].
     * For a paint signal, this is the elapsed time since the last paint signal.
     */
    val dt: Int
}

/**
 * Extends the [Clock] interface by an [alpha] value.
 */
interface PaintClock : Clock {
    /**
     * This value represents the fraction of time between the last simulation update and the next scheduled
     * update.
     *
     * For example if the previous update was scheduled to happen at T=500ms and the next update
     * at T=530ms and the actual time at which we are being rendered is T=517ms then alpha will be
     * (517-500)/(530-500) or 17/30. This is usually between 0 and 1, but if your game is running
     * slowly, it can exceed 1. For example, if an update is scheduled to happen at T=500ms and the
     * update actually happens at T=517ms, and the update call itself takes 20ms, the alpha value
     * passed to paint will be (537-500)/(530-500) or 37/30.
     */
    val alpha: Float
}

internal data class UpdateClockImpl(
        override var tick: Int = 0,
        override val dt: Int) : Clock

internal data class PaintClockImpl(
        override var tick: Int = 0,
        override var dt: Int = 0,
        override var alpha: Float = 0f) : PaintClock
