package klay.jvm

import klay.core.Audio
import klay.core.Exec
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip

class JavaAudio(private val exec: Exec) : Audio() {

    /**
     * Creates a sound instance from the audio data available via `in`.

     * @param rsrc an resource instance via which the audio data can be read.
     * *
     * @param music if true, a custom [Clip] implementation will be used which can handle long
     * * audio clips; if false, the default Java clip implementation is used which cannot handle long
     * * audio clips.
     */
    fun createSound(rsrc: JavaAssets.Resource, music: Boolean): JavaSound {
        val sound = JavaSound(exec)
        exec.invokeAsync(Runnable {
            try {
                var ais = rsrc.openAudioStream()
                var clip = AudioSystem.getClip()
                if (music) {
                    clip = BigClip(clip)
                }
                val baseFormat = ais.format
                if (baseFormat.encoding !== AudioFormat.Encoding.PCM_SIGNED) {
                    val decodedFormat = AudioFormat(
                            AudioFormat.Encoding.PCM_SIGNED,
                            baseFormat.sampleRate,
                            16, // we have to force sample size to 16
                            baseFormat.channels,
                            baseFormat.channels * 2,
                            baseFormat.sampleRate,
                            false // big endian
                    )
                    ais = AudioSystem.getAudioInputStream(decodedFormat, ais)
                }
                clip.open(ais)
                sound.succeed(clip)
            } catch (e: Exception) {
                sound.fail(e)
            }
        })
        return sound
    }
}
