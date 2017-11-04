package klay.core

import react.RFuture

/**
 * A single sound asset, which can be played, looped, etc.
 */
open class Sound {

    /** Represents a sound that failed to load. Reports the supplied error to all listeners.  */
    class Error(error: Exception) : Sound(RFuture.failure<Sound>(error))

    /** Reports the asynchronous loading of this sound. This will be completed with success or
     * failure when the sound's asynchronous load completes.  */
    val state: RFuture<Sound>

    /**
     * Returns whether this sound is fully loaded. In general you'll want to react to [.state]
     * to do things only after a sound is loaded, but this method is useful if you want to just skip
     * playing a sound that's not fully loaded (because playing a sound that's not loaded will defer
     * the play request until it has loaded, which may result in mismatched audio and visuals).

     *
     * Note: this is different from [.prepare]. This has to do with loading the sound bytes
     * from storage (or over the network in the case of the HTML backend). [.prepare] attempts
     * to ensure that the sound bytes are then transferred from CPU memory into the appropriate audio
     * buffers so that they can be played with the lowest possible latency.
     */
    val isLoaded: Boolean
        get() = state.isCompleteNow

    /**
     * Prepares this sound to be played by preloading it into audio buffers. This expresses a desire
     * to have subsequent calls to [.play] start emitting sound with the lowest possible
     * latency.

     * @return true if preloading occurred, false if unsupported or preloading failed
     */
    open fun prepare(): Boolean {
        return false
    }

    /**
     * If possible, begin playback of this audio stream. The audio system will make best efforts to
     * playback this sound. However, lack of audio or codec support, or a (temporary) unavailability
     * of audio channels may prevent playback. If the audio system is certain that audio playback
     * failed, this method will return false. However, a return value of true
     * does not guarantee that playback will in fact succeed.

     * @return true if it's likely that audio playback will proceed
     */
    open fun play(): Boolean {
        return false
    }

    /**
     * Stop playback of the current audio stream as soon as possible, and reset the sound position to
     * its starting position, such that a subsequent call to [.play] will cause the audio file
     * to being playback from the beginning of the audio stream.
     */
    open fun stop() {}

    /**
     * Set whether audio stream playback should be looped indefinitely or not.

     * @param looping true if the audio stream should be looped indefinitely
     */
    open fun setLooping(looping: Boolean) {}

    /**
     * @return the current volume of this sound, a value between 0.0 and 1.0.
     */
    open fun volume(): Float {
        return 0f
    }

    /**
     * @param volume new volume between 0.0 and 1.0
     */
    open fun setVolume(volume: Float) {}

    /**
     * Determine whether this audio stream is currently playing.

     * @return true if the audio stream is currently playing
     */
    open val isPlaying: Boolean
        get() = false

    /**
     * Releases resources used by this sound. It will no longer be usable after release. This will
     * also happen automatically when this sound is garbage collected, but one may need to manually
     * release sounds sooner to avoid running out of audio resources.
     */
    open fun release() {}

    /** Creates the sound of silence.  */
    constructor() {
        this.state = RFuture.success(this)
    }

    protected constructor(state: RFuture<Sound>) {
        this.state = state
    }
}
