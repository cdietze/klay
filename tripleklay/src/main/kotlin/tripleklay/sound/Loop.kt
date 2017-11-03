package tripleklay.sound

/**
 * Represents a looped sound (i.e. music).
 */
interface Loop : Playable {
    /** A noop loop.  */
    class Silence : Loop {
        override fun volume(): Float {
            return 0f
        }

        override fun setVolume(volume: Float) {}
        override val isPlaying: Boolean
            get() = false

        override fun play() {}
        override fun fadeIn(fadeMillis: Float) {}
        override fun fadeOut(fadeMillis: Float) {}
        override fun stop() {}
        override fun release() {}
    }

    /** Fades this loop in over the specified duration.  */
    fun fadeIn(fadeMillis: Float)

    /** Fades this loop out over the specified duration.  */
    fun fadeOut(fadeMillis: Float)
}
