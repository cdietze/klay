package tripleklay.ui.util

/**
 * Facilitates the rendering of "scale-9" images, that is, images that are designed as a 3x3 grid
 * such that each of the 9 pieces is fixed or stretched in one or both directions to fit a
 * designated area. The corners are drawn without scaling, the top and bottom center pieces are
 * scaled horizontally, the left and right center pieces are scaled vertically, and the center
 * piece is scaled both horizontally and vertically.

 *
 * By default, the cells are assumed to be of equal size (hence scale-9 image dimensions are
 * normally a multiple of 3). By using [.xaxis] and [.yaxis], this partitioning can be
 * controlled directly.

 *
 * Here's a diagram showing the stretching and axes, H = horizontally stretched, V = vertically
 * stretched, U = unstretched.

 * <pre>`xaxis

 * 0          1          2
 * ---------------------------------
 * |          |          |          |
 * 0  |    U     |    H     |    U     |
 * |          |          |          |
 * ---------------------------------
 * |          |          |          |
 * yaxis  1  |    V     |   H&V    |    V     |
 * |          |          |          |
 * ---------------------------------
 * |          |          |          |
 * 2  |    U     |    H     |    U     |
 * |          |          |          |
 * ---------------------------------
`</pre> *

 *
 * *Example 1*: the horizontal middle of an image is a single pixel. This code will do
 * that and automatically grow the left and right columns:<pre>
 * Scale9 s9 = ...;
 * s9.xaxis.resize(1, 1);</pre>

 *
 * *Example 2*: there are no top and bottom rows. This code will stretch all of the image
 * vertically, but keep the left and right third of the image fixed horizontally:<pre>
 * Scale9 s9 = ...;
 * s9.yaxis.resize(0, 0).resize(2, 0);</pre>
 */
class Scale9 {
    /** A horizontal or vertical axis, broken up into 3 chunks.  */
    class Axis {
        /** Creates a new axis equally splitting the given length.  */
        constructor(length: Float) {
            val d = length / 3
            _lengths = floatArrayOf(d, length - 2 * d, d)
            _offsets = floatArrayOf(0f, _lengths[0], _lengths[0] + _lengths[1])
        }

        /** Creates a new axis with the given total length and 0th and 2nd lengths copied from a
         * source axis.  */
        constructor(length: Float, src: Axis) {
            _lengths = floatArrayOf(src.size(0), length - (src.size(0) + src.size(2)), src.size(2))
            _offsets = floatArrayOf(0f, src.size(0), length - src.size(2))
        }

        /** Returns the coordinate of the given chunk, 0 - 2.  */
        fun coord(idx: Int): Float {
            return _offsets[idx]
        }

        /** Returns the size of the given chunk, 0 - 2.  */
        fun size(idx: Int): Float {
            return _lengths[idx]
        }

        /** Sets the size and location of the given chunk, 0 - 2.  */
        operator fun set(idx: Int, coord: Float, size: Float): Axis {
            _offsets[idx] = coord
            _lengths[idx] = size
            return this
        }

        /** Sets the size of the given chunk, shifting neighbors.  */
        fun resize(idx: Int, size: Float): Axis {
            val excess = _lengths[idx] - size
            _lengths[idx] = size
            when (idx) {
                0 -> {
                    _offsets[1] -= excess
                    _lengths[1] += excess
                }
                1 -> {
                    val half = excess * .5f
                    _lengths[0] += half
                    _lengths[2] += half
                    _offsets[1] += half
                    _offsets[2] -= half
                }
                2 -> {
                    _offsets[2] += excess
                    _lengths[1] += excess
                }
            }
            return this
        }

        /** The positions of the 3 chunks.  */
        protected val _offsets: FloatArray

        /** The lengths of the 3 chunks.  */
        protected val _lengths: FloatArray
    }

    /** The axes of the 3x3 grid.  */
    val xaxis: Axis
    val yaxis: Axis

    /** Creates a new scale to match the given width and height. Each horizontal and vertical
     * sequence is divided equally between the given values.  */
    constructor(width: Float, height: Float) {
        xaxis = Axis(width)
        yaxis = Axis(height)
    }

    /** Creates a new scale to render the given scale onto a target of the given width and
     * height.  */
    constructor(width: Float, height: Float, source: Scale9) {
        clamp(xaxis = Axis(width, source.xaxis), width)
        clamp(yaxis = Axis(height, source.yaxis), height)
    }

    companion object {

        /**
         * Ensures that the `Axis` passed in does not exceed the length given. An equal chunk
         * will be removed from the outer chunks if it is too long. The given axis is modified and
         * returned.
         */
        fun clamp(axis: Axis, length: Float): Axis {
            val left = axis.size(0)
            val middle = axis.size(1)
            val right = axis.size(2)
            if (left + middle + right > length && middle > 0 && left + right < length) {
                // the special case where for some reason the total is too wide, but the middle is non
                // zero, and it can absorb the extra all on its own.
                axis[1, left] = length - left - right
                axis[2, length - right] = right
            } else if (left + right > length) {
                // eat equal chunks out of each end so that we don't end up overlapping
                val remove = (left + right - length) / 2
                axis[0, 0f] = left - remove
                axis[1, left - remove] = 0f
                axis[2, left - remove] = right - remove
            }
            return axis
        }
    }
}
