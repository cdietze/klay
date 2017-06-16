package tripleklay.sound

/**
 * Provides a sound clip that can be played multiple times. Callers tell the multiclip to prepare a
 * copy which may result in loading a copy of the sound from a [SoundBoard] if no reserves
 * are available to play it, they then play the sound, and after a configured duration that sound
 * either goes back into the reserves, or is disposed, if the reserves are already full.
 */
class MultiClip
/**
 * Creates a multiclip with the supplied configuration.

 * @param board the soundboard from which to obtain clips.
 * *
 * @param path the path to the underlying sound.
 * *
 * @param reserveCopies the minimum number of copies of the sound to keep in memory.
 * *
 * @param duration the duration of the sound (in seconds). This will be used to determine when
 * * it is safe to reuse a copy of the sound.
 */
(private val _board: SoundBoard, private val _path: String, private val _reserveCopies: Int, duration: Float) {
    /** A handle on a copy of a clip. Used to play it.  */
    interface Copy : Playable {
        /** Plays this copy of the sound and then releases it.  */
        override fun play()

        /** Releases this copy of the sound without playing it.  */
        override fun release()
    }

    private val _duration: Float = duration * 1000
    private val _copies: MutableList<CopyImpl> = ArrayList()

    /**
     * Obtains a copy of the sound (from the reserves if possible or loaded from storage if not).
     * The copy must be [Copy.play]ed or [Copy.release]d by the caller.
     */
    fun reserve(): Copy {
        val now = _board.plat.time()
        var ii = 0
        val ll = _copies.size
        while (ii < ll) {
            val copy = _copies[ii]
            if (copy.releaseTime < now) {
                return _copies.removeAt(ii)
            }
            ii++
        }
        return CopyImpl()
    }

    /**
     * Releases all of the clips obtained by this multiclip, freeing their audio resources.
     */
    fun release() {
        for (copy in _copies) copy.sound.release()
        _copies.clear()
    }

    protected inner class CopyImpl : Copy {
        val sound = _board.getClip(_path)

        init {
            sound.setVolume(1f)
            sound.preload()
        }

        var releaseTime: Double = 0.toDouble()

        override fun volume(): Float {
            return sound.volume()
        }

        override fun setVolume(volume: Float) {
            sound.setVolume(volume)
        }

        override fun play() {
            sound.play()
            if (_copies.size < _reserveCopies) {
                releaseTime = _board.plat.time() + _duration
                _copies.add(this)
            }
        }

        override fun release() {
            if (_copies.size < _reserveCopies) {
                releaseTime = _board.plat.time()
                _copies.add(this)
            }
        }

        override fun stop() {
            sound.stop()
            releaseTime = 0.0 // release immediately
        }

        override val isPlaying: Boolean
            get() = sound.isPlaying
    }
}
