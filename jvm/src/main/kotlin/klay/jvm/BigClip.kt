package klay.jvm

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import javax.sound.sampled.*

/**
 * Reformatted and adapted version of BigClip as posted to:
 * http://stackoverflow.com/questions/9470148/how-do-you-play-a-long-audio-clip-in-java

 * An implementation of the javax.sound.sampled.Clip that is designed to handle Clips of arbitrary
 * size, limited only by the amount of memory available to the app. It uses the post 1.4 thread
 * behaviour (daemon thread) that will stop the sound running after the main has exited.
 *  * 2012-12-18 - Fixed bug with LOOP_CONTINUOUSLY and some bugs with drain() and buffer sizes.
 *  * 2012-02-29 - Reworked play/loop to fix several bugs.
 *  * 2009-09-01 - Fixed bug that had clip ..clipped at the end, by calling drain() (before calling
 * stop()) on the dataline after the play loop was complete. Improvement to frame and microsecond
 * position determination.
 *  * 2009-08-17 - added convenience constructor that accepts a Clip. Changed the private
 * convertFrameToM..seconds methods from 'micro' to 'milli' to reflect that they were dealing with
 * units of 1000/th of a second.
 *  * 2009-08-14 - got rid of flush() after the sound loop, as it was cutting off tracks just
 * before the end, and was found to be not needed for the fast-forward/rewind functionality it was
 * introduced to support.
 *  * 2009-08-11 - First binary release.  N.B. Remove @Override notation and logging to use in
 * 1.3+
 * @since 1.5
 * *
 * @version 2012-12-18
 * @author Andrew Thompson
 * @author Alejandro Garcia
 * @author Michael Thomas
 */
internal class BigClip : Clip, LineListener {

    /** The DataLine used by this Clip.  */
    private var dataLine: SourceDataLine? = null

    /** The raw bytes of the audio data.  */
    /**
     * Provides the entire audio buffer of this clip.
     * @return audioData byte[] The bytes of the audio data that is loaded in this Clip.
     */
    var audioData: ByteArray? = null
        private set

    /** The stream wrapper for the audioData.  */
    private var inputStream: ByteArrayInputStream? = null

    /** Loop count set by the calling code.  */
    private var loopCount = 1
    /** Internal count of how many loops to go.  */
    private var countDown = 1
    /** The start of a loop point. Defaults to 0.  */
    private var loopPointStart: Int = 0
    /** The end of a loop point. Defaults to the end of the Clip.  */
    private var loopPointEnd: Int = 0

    /** Stores the current frame position of the clip.  */
    private var framePosition: Int = 0

    /** Thread used to run() sound.  */
    private var thread: Thread? = null
    /** Whether the sound is currently playing or active.  */
    private var active: Boolean = false
    /** Stores the last time bytes were dumped to the audio stream.  */
    private var timelastPositionSet: Long = 0

    private val bufferUpdateFactor = 2

    /**
     * Default constructor for a BigClip. Does nothing. Information from the AudioInputStream passed
     * in open() will be used to get an appropriate SourceDataLine.
     */
    constructor() {}

    /**
     * There are a number of AudioSystem methods that will return a configured Clip. This
     * convenience constructor allows us to obtain a SourceDataLine for the BigClip that uses the
     * same AudioFormat as the original Clip.
     * @param clip Clip The Clip used to configure the BigClip.
     */
    constructor(clip: Clip) {
        dataLine = AudioSystem.getSourceDataLine(clip.format)
    }

    /** Converts a frame count to a duration in milliseconds.  */
    private fun convertFramesToMilliseconds(frames: Int): Long {
        return frames / dataLine!!.format.sampleRate.toLong() * 1000
    }

    /** Converts a duration in milliseconds to a frame count.  */
    private fun convertMillisecondsToFrames(milliseconds: Long): Int {
        return (milliseconds / dataLine!!.format.sampleRate).toInt()
    }

    override fun update(le: LineEvent) {
        //PlayN.log().debug("update: " + le);
    }

    override fun loop(count: Int) {
        //PlayN.log().debug("loop(" + count + ") - framePosition: " + framePosition);
        loopCount = count
        countDown = count
        active = true
        inputStream!!.reset()

        start()
    }

    override fun setLoopPoints(start: Int, end: Int) {
        if (start < 0 || start > audioData!!.size - 1 || end < 0 || end > audioData!!.size) {
            throw IllegalArgumentException("Loop points '" + start + "' and '" + end
                    + "' cannot be set for buffer of size " + audioData!!.size)
        }
        if (start > end) {
            throw IllegalArgumentException("End position " + end + " preceeds start position "
                    + start)
        }

        loopPointStart = start
        framePosition = loopPointStart
        loopPointEnd = end
    }

    override fun setMicrosecondPosition(milliseconds: Long) {
        framePosition = convertMillisecondsToFrames(milliseconds)
    }

    override fun getMicrosecondPosition(): Long {
        return convertFramesToMilliseconds(getFramePosition())
    }

    override fun getMicrosecondLength(): Long {
        return convertFramesToMilliseconds(frameLength)
    }

    override fun setFramePosition(frames: Int) {
        framePosition = frames
        val offset = framePosition * _format!!.frameSize
        try {
            inputStream!!.reset()
            inputStream!!.read(ByteArray(offset))
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun getFramePosition(): Int {
        val timeSinceLastPositionSet = System.currentTimeMillis() - timelastPositionSet
        var size = dataLine!!.bufferSize * (_format!!.channels / 2) / bufferUpdateFactor

        // Step down to the next whole frame.
        size /= dataLine!!.format.frameSize
        size *= dataLine!!.format.frameSize

        val framesSinceLast = (timeSinceLastPositionSet / 1000f * dataLine!!.format.frameRate).toInt()
        val framesRemainingTillTime = size - framesSinceLast
        return framePosition - framesRemainingTillTime
    }

    override fun getFrameLength(): Int {
        return audioData!!.size / _format!!.frameSize
    }

    override fun getFormat(): AudioFormat? = _format

    private var _format: AudioFormat? = null

    override fun open(stream: AudioInputStream) {

        val is1: AudioInputStream
        _format = stream.format

        if (_format!!.encoding !== AudioFormat.Encoding.PCM_SIGNED) {
            is1 = AudioSystem.getAudioInputStream(AudioFormat.Encoding.PCM_SIGNED, stream)
        } else {
            is1 = stream
        }
        _format = is1.format
        val is2 = is1

        val buf = ByteArray(1 shl 16)
        var numRead = 0
        val baos = ByteArrayOutputStream()
        numRead = is2.read(buf)
        while (numRead > -1) {
            baos.write(buf, 0, numRead)
            numRead = is2.read(buf, 0, buf.size)
        }
        is2.close()
        audioData = baos.toByteArray()
        val afTemp: AudioFormat
        if (_format!!.channels < 2) {
            val frameSize = _format!!.sampleSizeInBits * 2 / 8
            afTemp = AudioFormat(_format!!.encoding, _format!!.sampleRate,
                    _format!!.sampleSizeInBits, 2, frameSize,
                    _format!!.frameRate, _format!!.isBigEndian)
        } else {
            afTemp = _format!!
        }

        setLoopPoints(0, audioData!!.size)
        dataLine = AudioSystem.getSourceDataLine(afTemp)
        dataLine!!.open()
        inputStream = ByteArrayInputStream(audioData!!)
    }

    override fun open(format: AudioFormat, data: ByteArray, offset: Int, bufferSize: Int) {
        val input = ByteArray(bufferSize)
        for (ii in input.indices) {
            input[ii] = data[offset + ii]
        }
        val inputStream = ByteArrayInputStream(input)
        try {
            val ais1 = AudioSystem.getAudioInputStream(inputStream)
            val ais2 = AudioSystem.getAudioInputStream(format, ais1)
            open(ais2)
        } catch (uafe: UnsupportedAudioFileException) {
            throw IllegalArgumentException(uafe)
        } catch (ioe: IOException) {
            throw IllegalArgumentException(ioe)
        }

        // TODO - throw IAE for invalid frame size, format.
    }

    override fun getLevel(): Float {
        return dataLine!!.level
    }

    override fun getLongFramePosition(): Long {
        return dataLine!!.longFramePosition * 2 / _format!!.channels
    }

    override fun available(): Int {
        return dataLine!!.available()
    }

    override fun getBufferSize(): Int {
        return dataLine!!.bufferSize
    }

    override fun isActive(): Boolean {
        return dataLine!!.isActive
    }

    override fun isRunning(): Boolean {
        return dataLine!!.isRunning
    }

    override fun isOpen(): Boolean {
        return dataLine!!.isOpen
    }

    override fun stop() {
        //PlayN.log().debug("BigClip.stop()");
        active = false
        // why did I have this commented out?
        dataLine!!.stop()
        if (thread != null) {
            try {
                active = false
                thread!!.join()
            } catch (wakeAndContinue: InterruptedException) {
            }

        }
    }

    fun convertMonoToStereo(data: ByteArray, bytesRead: Int): ByteArray {
        val tempData = ByteArray(bytesRead * 2)
        if (_format!!.sampleSizeInBits == 8) {
            for (ii in 0..bytesRead - 1) {
                val b = data[ii]
                tempData[ii * 2] = b
                tempData[ii * 2 + 1] = b
            }
        } else {
            var ii = 0
            while (ii < bytesRead - 1) {
                val b1 = data[ii]
                val b2 = data[ii + 1]
                tempData[ii * 2] = b1
                tempData[ii * 2 + 1] = b2
                tempData[ii * 2 + 2] = b1
                tempData[ii * 2 + 3] = b2
                ii += 2
            }
        }
        return tempData
    }

    override fun start() {
        val r = Runnable {
            dataLine!!.start()

            active = true

            var bytesRead = 0
            val frameSize = dataLine!!.format.frameSize
            val bufSize = dataLine!!.bufferSize
            var startOrMove = true
            var data = ByteArray(bufSize)
            val offset = framePosition * frameSize
            bytesRead = inputStream!!.read(ByteArray(offset), 0, offset)
            // PlayN.log().debug("bytesRead " + bytesRead);
            bytesRead = inputStream!!.read(data, 0, data.size)

            // PlayN.log().debug("loopCount " + loopCount);
            // PlayN.log().debug("countDown " + countDown);
            // PlayN.log().debug("bytesRead " + bytesRead);

            while (bytesRead != -1 && (loopCount == Clip.LOOP_CONTINUOUSLY || countDown > 0)
                    && active) {
                // PlayN.log().debug("BigClip.start() loop " + framePosition);
                val framesRead: Int
                val tempData: ByteArray
                if (_format!!.channels < 2) {
                    tempData = convertMonoToStereo(data, bytesRead)
                    framesRead = bytesRead / _format!!.frameSize
                    bytesRead *= 2
                } else {
                    framesRead = bytesRead / dataLine!!.format.frameSize
                    tempData = Arrays.copyOfRange(data, 0, bytesRead)
                }

                framePosition += framesRead
                if (framePosition >= loopPointEnd) {
                    framePosition = loopPointStart
                    inputStream!!.reset()
                    countDown--
                    // PlayN.log().debug("Loop Count: " + countDown);
                }
                timelastPositionSet = System.currentTimeMillis()

                val newData = tempData
                dataLine!!.write(newData, 0, newData.size)
                if (startOrMove) {
                    var len = bufSize / bufferUpdateFactor

                    // Step down to the next whole frame.
                    len /= frameSize
                    len *= frameSize

                    data = ByteArray(len)
                    startOrMove = false
                }

                bytesRead = inputStream!!.read(data, 0, data.size)
                if (bytesRead < 0 && (--countDown > 0 || loopCount == Clip.LOOP_CONTINUOUSLY)) {
                    inputStream!!.read(ByteArray(offset), 0, offset)
                    // PlayN.log().debug("loopCount " + loopCount);
                    // PlayN.log().debug("countDown " + countDown);
                    inputStream!!.reset()
                    bytesRead = inputStream!!.read(data, 0, data.size)
                }
            }

            // PlayN.log().debug("BigClip.start() loop ENDED" + framePosition);
            active = false
            countDown = 1
            framePosition = 0
            inputStream!!.reset()
            dataLine!!.stop()
        }
        thread = Thread(r)
        // makes thread behaviour compatible with JavaSound post 1.4
        thread!!.isDaemon = true
        thread!!.start()
    }

    override fun flush() {
        dataLine!!.flush()
    }

    override fun drain() {
        dataLine!!.drain()
    }

    override fun removeLineListener(listener: LineListener) {
        dataLine!!.removeLineListener(listener)
    }

    override fun addLineListener(listener: LineListener) {
        dataLine!!.addLineListener(listener)
    }

    override fun getControl(control: Control.Type): Control {
        return dataLine!!.getControl(control)
    }

    override fun getControls(): Array<Control> {
        if (dataLine == null) {
            return emptyArray()
        } else {
            return dataLine!!.controls
        }
    }

    override fun isControlSupported(control: Control.Type): Boolean {
        return dataLine!!.isControlSupported(control)
    }

    override fun close() {
        dataLine!!.close()
    }

    override fun open() {
        throw IllegalArgumentException("illegal call to open() in interface Clip")
    }

    override fun getLineInfo(): Line.Info {
        return dataLine!!.lineInfo
    }
}