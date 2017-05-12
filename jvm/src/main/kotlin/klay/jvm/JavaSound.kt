package klay.jvm

import klay.core.Exec
import klay.core.SoundImpl
import pythagoras.f.MathUtil

import javax.sound.sampled.Clip
import javax.sound.sampled.FloatControl

class JavaSound(exec: Exec) : SoundImpl<Clip>(exec) {

    override fun playingImpl(): Boolean {
        return impl!!.isActive()
    }

    override fun playImpl(): Boolean {
        impl!!.setFramePosition(0)
        if (_looping) {
            impl!!.loop(Clip.LOOP_CONTINUOUSLY)
        } else {
            impl!!.start()
        }
        return true
    }

    override fun stopImpl() {
        impl!!.stop()
        impl!!.flush()
    }

    override fun setLoopingImpl(looping: Boolean) {
        // nothing to do here, we pass looping to impl.play()
    }

    override fun setVolumeImpl(volume: Float) {
        if (impl!!.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            val volctrl = impl!!.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl
            volctrl.value = toGain(volume, volctrl.minimum, volctrl.maximum)
        }
    }

    override fun releaseImpl() {
        impl!!.close()
    }

    companion object {

        // @Override
        // public float volume() {
        //   FloatControl volctrl = (FloatControl) impl.getControl(FloatControl.Type.MASTER_GAIN);
        //   return toVolume(volctrl.getValue());
        // }

        // protected static float toVolume (float gain) {
        //   return MathUtil.pow(10, gain/20);
        // }

        protected fun toGain(volume: Float, min: Float, max: Float): Float {
            return MathUtil.clamp(20 * MathUtil.log10(volume), min, max)
        }
    }
}
