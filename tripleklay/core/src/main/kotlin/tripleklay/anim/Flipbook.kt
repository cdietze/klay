package tripleklay.anim

import tripleklay.util.Frames

/**
 * Encapsulates a set of frames and timings for animating those frames.
 */
class Flipbook
/**
 * Creates a flipbook with the specified frames.

 * @param frameIndexes an array of frame indexes to be played in the specified order.
 * *
 * @param frameEnds the time (in seconds since animation start) at which the frame specified
 * * at the corresponding position in `frameIndex` ends. The values must be monotonically
 * * increasing (e.g. `(1.5f, 2f, 2.5f, 4f)`.
 */
(
        /** The frames to be animated.  */
        val frames: Frames,
        /** The index of the frames to be shown. The animation will display the frame at the 0th
         * position in this array, then the frame at the 1st position, etc.  */
        val frameIndexes: IntArray,
        /** The timestamp at which to stop playing each frame and move to the next.  */
        val frameEnds: FloatArray) {

    /**
     * Creates a flipbook with the specified frames. The frames will be played in order, each for
     * the specified duration.

     * @param secsPerFrame the number of seconds to display each frame.
     */
    constructor(frames: Frames, secsPerFrame: Float) : this(frames, uniformTimes(frames, secsPerFrame))

    /**
     * Creates a flipbook with the specified frames. The frames will be played in order, each for
     * its associated duration in `frameEnds`.

     * @param frameEnds the time (in seconds since animation start) at which each frame ends. The
     * * values must be monotonically increasing (e.g. `(1.5f, 2f, 2.5f, 4f)`.
     */
    constructor(frames: Frames, frameEnds: FloatArray) : this(frames, uniformOrder(frames), frameEnds)

    companion object {

        private fun uniformTimes(frames: Frames, secsPerFrame: Float): FloatArray {
            val times = FloatArray(frames.count())
            times[0] = secsPerFrame
            var ii = 1
            val ll = times.size
            while (ii < ll) {
                times[ii] = times[ii - 1] + secsPerFrame
                ii++
            }
            return times
        }

        private fun uniformOrder(frames: Frames): IntArray {
            val indexes = IntArray(frames.count())
            var ii = 1
            val ll = indexes.size
            while (ii < ll) {
                indexes[ii] = ii
                ii++
            }
            return indexes
        }
    }
}
