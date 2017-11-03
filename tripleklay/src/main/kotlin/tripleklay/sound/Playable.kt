package tripleklay.sound

/**
 * Shared controls for clips and loops.
 */
interface Playable {
    /** Returns the current volume configured for this clip. Note that the actual volume will be
     * the configured volume multiplied by the master volume of the owning soundboard.  */
    fun volume(): Float

    /** Configures the volume for this clip to a value between 0 and 1. Note that the actual volume
     * will be the configured volume multiplied by the master volume of the owning soundboard.  */
    fun setVolume(volume: Float)

    /** Returns true if this playable is currently playing, false otherwise.  */
    val isPlaying: Boolean

    /** Starts this clip or loop playing. If the sound data is not yet loaded it will be loaded and
     * then played.  */
    fun play()

    /** Stops this clip or loop (fading it out over one second).  */
    fun stop()

    /** Releases this playable when it is no longer needed. This releases any associated audio
     * resources.If this playable is used again, the underlying sound will be reloaded.  */
    fun release()
}
