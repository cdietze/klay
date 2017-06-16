package tripleklay.sound

import klay.core.Sound

/**
 * Represents a one-shot clip.
 */
interface Clip : Playable {
    /** A noop clip.  */
    class Silence : Clip {
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
        override fun preload() {}
        override fun asSound(): Sound {
            return Sound()
        }
    }

    /** Fades this clip in over the specified duration.  */
    fun fadeIn(fadeMillis: Float)

    /** Fades this clip out over the specified duration.  */
    fun fadeOut(fadeMillis: Float)

    /** Preloads this clip's underlying audio data.  */
    fun preload()

    /** Views this clip as a [Sound]. Only the [Sound.play] and [Sound.stop]
     * methods can be used. Useful for passing a clip into code that expects [Sound].  */
    fun asSound(): Sound
}
