package tripleklay.sound

import klay.core.Clock
import klay.core.Platform
import klay.core.Sound
import pythagoras.f.MathUtil
import react.Signal
import react.Value
import tripleklay.util.Interpolator

/**
 * Manages sound clips (sfx) and loops (music). Allows for master volume adjustment, and applying
 * adjusted volume to currently playing loops. The expected usage pattern is to create a separate
 * sound board for every collection of sounds that share a single volume control. For example, one
 * might create one board for SFX and one board for music so that each could be volume controlled
 * (and disabled) separately.
 */
class SoundBoard
/** Creates a sound board which will play sounds via `plat` and connect to `paint`
 * to receive per-frame updates.  */
(
        /** The platform on which this sound board is operating.  */
        val plat: Platform, paint: Signal<Clock>) {

    /** Controls the volume of this sound board.  */
    var volume: Value<Float> = object : Value<Float>(1f) {
        override fun updateAndNotifyIf(value: Float): Float {
            return super.updateAndNotifyIf(MathUtil.clamp(value, 0f, 1f))
        }
    }

    /** Controls whether this sound board is muted. When muted, no sounds will play.  */
    var muted = Value(false)

    init {
        paint.connect({ clock: Clock ->
            update(clock.dt)
        })
        volume.connect({ volume: Float ->
            for (active in _active) active.updateVolume(volume)
        })
        muted.connect({ muted: Boolean ->
            for (active in _active) active.fadeForMute(muted)
        })
    }

    /**
     * Creates and returns a clip with the supplied path. This clip will contain its own copy of
     * the sound data at the specified path, and thus should be retained by the caller if it will
     * be used multiple times before being released. Once all references to this clip are released,
     * it will be garbage collected and its sound data unloaded.
     */
    fun getClip(path: String): Clip {
        return object : ClipImpl() {
            override fun path(): String {
                return path
            }
        }
    }

    /**
     * Creates and returns a loop with the supplied path. The loop will contain its own copy of the
     * sound data at the specified path, and thus should be retained by the caller if it will be
     * used multiple times before being released. Once all references to this loop are released, it
     * will be garbage collected and its sound data unloaded.
     */
    fun getLoop(path: String): Loop {
        return object : LoopImpl() {
            override fun path(): String {
                return path
            }
        }
    }

    protected fun shouldPlay(): Boolean {
        return !muted.get() && volume.get() > 0
    }

    protected fun update(delta: Int) {
        // update any active faders
        var ii = 0
        var ll = _faders.size
        while (ii < ll) {
            if (_faders[ii].update(delta)) {
                _faders.removeAt(ii--)
                ll--
            }
            ii++
        }
    }

    protected abstract inner class ClipImpl : LazySound(), Clip {
        override fun preload() {
            if (shouldPlay()) prepareSound()
        }

        override fun play() {
            if (shouldPlay()) prepareSound().play()
        }

        override fun fadeIn(fadeMillis: Float) {
            if (shouldPlay()) startFadeIn(fadeMillis)
        }

        override fun fadeOut(fadeMillis: Float) {
            if (shouldPlay()) startFadeOut(fadeMillis)
        }

        override fun stop() {
            if (isPlaying) sound!!.stop()
        }

        override fun asSound(): Sound {
            return object : Sound() {
                override fun play(): Boolean {
                    this@ClipImpl.play()
                    return true
                }

                override fun stop() {
                    this@ClipImpl.stop()
                }
            }
        }

        override fun toString(): String {
            return "clip:" + sound!!
        }

        override fun loadSound(path: String): Sound {
            return plat.assets.getSound(path)
        }
    }

    protected abstract inner class LoopImpl : LazySound(), Loop {
        fun fadeForMute(muted: Boolean) {
            if (muted)
                startFadeOut(FADE_DURATION)
            else
                startFadeIn(FADE_DURATION)
        }

        override fun play() {
            if (!_active.add(this)) return
            if (shouldPlay() && !isPlaying) prepareSound().play()
        }

        override fun fadeIn(fadeMillis: Float) {
            if (_active.add(this) && shouldPlay()) startFadeIn(fadeMillis)
        }

        override fun fadeOut(fadeMillis: Float) {
            if (_active.remove(this) && shouldPlay()) startFadeOut(fadeMillis)
        }

        override fun stop() {
            if (_active.remove(this) && isPlaying) sound!!.stop()
        }

        override fun toString(): String {
            return "loop:" + sound!!
        }

        override fun prepareSound(): Sound {
            val sound = super.prepareSound()
            sound.setLooping(true)
            return sound
        }

        override fun fadeOutComplete() {
            sound!!.release()
            sound = null
        }

        override fun loadSound(path: String): Sound {
            return plat.assets.getMusic(path)
        }
    }

    protected abstract inner class LazySound : Playable {
        var sound: Sound? = null

        override val isPlaying: Boolean
            get() = if (sound == null) false else sound!!.isPlaying

        override fun setVolume(volume: Float) {
            _volume = volume
            updateVolume(this@SoundBoard.volume.get())
        }

        override fun volume(): Float {
            return _volume
        }

        override fun release() {
            if (sound != null) {
                if (sound!!.isPlaying) sound!!.stop()
                sound!!.release()
                sound = null
            }
        }

        override fun hashCode(): Int {
            return path().hashCode()
        }

        override fun equals(other: Any?): Boolean {
            return if (other === this)
                true
            else
                other != null && other.javaClass == javaClass &&
                        path() == (other as LazySound).path()
        }

        fun updateVolume(volume: Float) {
            if (isPlaying) {
                val effectiveVolume = volume * _volume
                if (effectiveVolume > 0)
                    sound!!.setVolume(effectiveVolume)
                else
                    sound!!.stop()
            }
        }

        protected fun startFadeIn(fadeMillis: Float) {
            cancelFaders()
            if (!isPlaying) prepareSound().play()
            sound!!.setVolume(0f) // start at zero, fade in from there
            _faders.add(object : Fader() {
                override fun update(delta: Int): Boolean {
                    _elapsed += delta
                    val vol = Interpolator.Companion.LINEAR.apply(0f, _range, _elapsed.toFloat(), fadeMillis)
                    updateVolume(vol)
                    return vol >= _range // we're done when the volume hits the target
                }

                override fun cancel() {}
                protected val _range = volume.get()
                protected var _elapsed: Int = 0
            })
        }

        protected fun startFadeOut(fadeMillis: Float) {
            cancelFaders()
            if (isPlaying)
                _faders.add(object : Fader() {
                    override fun update(delta: Int): Boolean {
                        _elapsed += delta
                        val vol = Interpolator.Companion.LINEAR.apply(_start, -_start, _elapsed.toFloat(), fadeMillis)
                        updateVolume(vol)
                        if (vol > 0)
                            return false
                        else { // we're done when the volume hits zero
                            fadeOutComplete()
                            return true
                        }
                    }

                    override fun cancel() {
                        updateVolume(0f)
                        fadeOutComplete()
                    }

                    protected val _start = volume.get()
                    protected var _elapsed: Int = 0
                })
        }

        protected fun cancelFaders() {
            for (fader in _faders) {
                fader.cancel()
            }
            _faders.clear()
        }

        protected open fun prepareSound(): Sound {
            if (sound == null) {
                sound = loadSound(path())
                sound!!.prepare()
            }
            sound!!.setVolume(volume.get() * _volume)
            return sound!!
        }

        protected open fun fadeOutComplete() {}

        protected abstract fun loadSound(path: String): Sound
        protected abstract fun path(): String

        protected var _volume = 1f
    }

    protected abstract inner class Fader {
        abstract fun update(delta: Int): Boolean
        abstract fun cancel()
    }

    protected val _active: MutableSet<LoopImpl> = HashSet()
    protected val _faders: MutableList<Fader> = ArrayList()

    companion object {

        protected val FADE_DURATION = 1000f
    }
}
