package klay.core

import react.Signal

/**
 * Defines a simple game API. It's not necessary to use this abstraction for your Klay games, but
 * it takes care of some standard stuff that most games are likely to want.

 *
 * This implementation separates game processing into two phases: simulation and render. The
 * simulation phase takes place via [.update] and is called with a monotonoically increasing
 * timer at a fixed rate. The interpolation phase takes place via [.paint] and is called
 * every time the game is rendered to the display (which may be more frequently than the simulation
 * is updated). The render phase will generally interpolate the values computed in [.update]
 * to provide smooth rendering based on lower-frequency simulation updates.

 * @see [Understanding the Game Loop](http://playn.io/docs/overview.html#game-loop)
 */
abstract class Game
/** Creates a clocked game with the desired simulation update rate, in ms.  */
(
        /** The platform on which this game is running.  */
        val plat: Platform,
        /** The interval in milliseconds that our game shall update.
         * Defaults to 33ms which is 30fps */
        val updateRate: Int = 33) {

    /** A signal emitted on every simulation update.  */
    val update: Signal<Clock> = Signal()

    /** A signal emitted on every frame.  */
    val paint: Signal<PaintClock> = Signal()

    private val updateClock = UpdateClockImpl(tick = plat.tick(), dt = this.updateRate)
    private val paintClock = PaintClockImpl(tick = updateClock.tick)

    init {
        assert(updateRate > 0) { "updateRate must be greater than zero." }
        plat.frame.connect { _ ->
            onFrame()
        }
    }

    /** Called on every simulation update. The default implementation emits the clock to the [ ][.updateClock] signal, but you can override this method to change or augment this behavior.
     * @param clock a clock configured with the update timing information.
     */
    fun update(clock: Clock) {
        update.emit(clock)
    }

    /** Called on every frame. The default implementation emits the clock to the [.paintClock]
     * signal, but you can override this method to change or augment this behavior.
     * @param clock a clock configured with the frame timing information.
     */
    fun paint(clock: PaintClock) {
        paint.emit(paintClock)
    }

    private fun onFrame() {
        var nextUpdate = updateClock.tick + updateRate
        val updateTick = plat.tick()
        while (updateTick >= nextUpdate) {
            updateClock.tick = nextUpdate
            nextUpdate += updateRate
            update(updateClock)
        }
        val paintTick = plat.tick()
        paintClock.dt = paintTick - paintClock.tick
        paintClock.tick = paintTick
        paintClock.alpha = (paintTick - nextUpdate) / updateRate.toFloat()
        paint(paintClock)
    }
}
