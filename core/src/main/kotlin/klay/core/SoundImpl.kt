package klay.core

import pythagoras.f.MathUtil
import react.RPromise

/**
 * An implementation detail. Not part of the public API.
 */
abstract class SoundImpl<I>(private val exec: Exec) : Sound(exec.deferredPromise<Sound>()) {

    protected var playing: Boolean = false
    protected var _looping: Boolean = false
    protected var _volume = 1f
    protected var impl: I? = null

    /** Configures this sound with its platform implementation.
     * This may be called from any thread.  */
    @Synchronized
    fun succeed(impl: I) {
        this.impl = impl
        setVolumeImpl(_volume)
        setLoopingImpl(_looping)
        if (playing) playImpl()
        (state as RPromise<Sound>).succeed(this)
    }

    /** Configures this sound with an error in lieu of its platform implementation.
     * This may be called from any thread.  */
    @Synchronized
    fun fail(error: Throwable) {
        (state as RPromise<Sound>).fail(error)
    }

    override fun prepare(): Boolean {
        return if (impl != null) prepareImpl() else false
    }

    override val isPlaying: Boolean
        get() = if (impl != null) playingImpl() else playing

    override fun play(): Boolean {
        this.playing = true
        return if (impl == null) false else playImpl()
    }

    override fun stop() {
        this.playing = false
        if (impl != null) stopImpl()
    }

    override fun setLooping(looping: Boolean) {
        this._looping = looping
        if (impl != null) setLoopingImpl(looping)
    }

    override fun volume(): Float {
        return _volume
    }

    override fun setVolume(volume: Float) {
        this._volume = MathUtil.clamp(volume, 0f, 1f)
        if (impl != null) setVolumeImpl(this._volume)
    }

    override fun release() {
        if (impl != null) {
            releaseImpl()
            impl = null
        }
    }

    @Suppress("unused")
    protected fun finalize() {
        if (impl != null) {
            exec.invokeLater { release() }
        }
    }

    protected fun prepareImpl(): Boolean {
        return false
    }

    protected open fun playingImpl(): Boolean {
        return playing
    }

    protected abstract fun playImpl(): Boolean
    protected abstract fun stopImpl()
    protected abstract fun setLoopingImpl(looping: Boolean)
    protected abstract fun setVolumeImpl(volume: Float)
    protected abstract fun releaseImpl()
}
