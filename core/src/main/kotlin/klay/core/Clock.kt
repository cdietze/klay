package klay.core

/**
 * Encapsulates an absolute and delta time. Used by [Game] to emit simulation update and
 * frame paint signals. *Note:* these values are exposed as public mutable fields for
 * efficiency. Clients should naturally not mutate these values, only the clock provider.
 */
class Clock {

    /** The number of milliseconds that have elapsed since time 0.  */
    var tick: Int = 0

    /** The number of milliseconds that have elapsed since the last signal.  */
    var dt: Int = 0

    /** If this clock is used by a game with separate simulation and paint schedules, this value
     * represents the fraction of time between the last simulation update and the next scheduled
     * update. This value is only provided for the paint clock.

     *
     * For example if the previous update was scheduled to happen at T=500ms and the next update
     * at T=530ms and the actual time at which we are being rendered is T=517ms then alpha will be
     * (517-500)/(530-500) or 17/30. This is usually between 0 and 1, but if your game is running
     * slowly, it can exceed 1. For example, if an update is scheduled to happen at T=500ms and the
     * update actually happens at T=517ms, and the update call itself takes 20ms, the alpha value
     * passed to paint will be (537-500)/(530-500) or 37/30.
     */
    var alpha: Float = 0f
}
