//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

/**
 * Math utility methods.
 */
open class MathUtil {
    companion object {
        /** A small number.  */
        val EPSILON = 0.00001f

        /** The circle constant, Tau (&#964;) http://tauday.com/  */
        val TAU = (Math.PI * 2).toFloat()

        /** Twice Pi.  */
        val TWO_PI = TAU

        /** Pi times one half.  */
        val HALF_PI = (Math.PI * 0.5).toFloat()

        /**
         * A cheaper version of [Math.round] that doesn't handle the special cases.
         */
        fun round(v: Float): Int {
            return if (v < 0f) (v - 0.5f).toInt() else (v + 0.5f).toInt()
        }

        /**
         * Returns the floor of v as an integer without calling the relatively expensive
         * [Math.floor].
         */
        fun ifloor(v: Float): Int {
            val iv = v.toInt()
            return if (v >= 0f || iv.toFloat() == v || iv == Integer.MIN_VALUE) iv else iv - 1
        }

        /**
         * Returns the ceiling of v as an integer without calling the relatively expensive
         * [Math.ceil].
         */
        fun iceil(v: Float): Int {
            val iv = v.toInt()
            return if (v <= 0f || iv.toFloat() == v || iv == Integer.MAX_VALUE) iv else iv + 1
        }

        /**
         * Clamps a value to the range [lower, upper].
         */
        fun clamp(v: Float, lower: Float, upper: Float): Float {
            if (v < lower)
                return lower
            else if (v > upper)
                return upper
            else
                return v
        }

        /**
         * Rounds a value to the nearest multiple of a target.
         */
        fun roundNearest(v: Float, target: Float): Float {
            var target = target
            target = Math.abs(target)
            if (v >= 0) {
                return target * FloatMath.floor((v + 0.5f * target) / target)
            } else {
                return target * FloatMath.ceil((v - 0.5f * target) / target)
            }
        }

        /**
         * Checks whether the value supplied is in [lower, upper].
         */
        fun isWithin(v: Float, lower: Float, upper: Float): Boolean {
            return v >= lower && v <= upper
        }

        /**
         * Returns a random value according to the normal distribution with the provided mean and
         * standard deviation.

         * @param normal a normally distributed random value.
         * *
         * @param mean the desired mean.
         * *
         * @param stddev the desired standard deviation.
         */
        fun normal(normal: Float, mean: Float, stddev: Float): Float {
            return stddev * normal + mean
        }

        /**
         * Returns a random value according to the exponential distribution with the provided mean.

         * @param random a uniformly distributed random value.
         * *
         * @param mean the desired mean.
         */
        fun exponential(random: Float, mean: Float): Float {
            return -FloatMath.log(1f - random) * mean
        }

        /**
         * Linearly interpolates between two angles, taking the shortest path around the circle.
         * This assumes that both angles are in [-pi, +pi].
         */
        fun lerpa(a1: Float, a2: Float, t: Float): Float {
            val ma1 = mirrorAngle(a1)
            val ma2 = mirrorAngle(a2)
            val d = Math.abs(a2 - a1)
            val md = Math.abs(ma1 - ma2)
            return if (d <= md) lerp(a1, a2, t) else mirrorAngle(lerp(ma1, ma2, t))
        }

        /**
         * Linearly interpolates between v1 and v2 by the parameter t.
         */
        fun lerp(v1: Float, v2: Float, t: Float): Float {
            return v1 + t * (v2 - v1)
        }

        /**
         * Determines whether two values are "close enough" to equal.
         */
        fun epsilonEquals(v1: Float, v2: Float): Boolean {
            return Math.abs(v1 - v2) < EPSILON
        }

        /**
         * Returns the (shortest) distance between two angles, assuming that both angles are in
         * [-pi, +pi].
         */
        fun angularDistance(a1: Float, a2: Float): Float {
            val ma1 = mirrorAngle(a1)
            val ma2 = mirrorAngle(a2)
            return Math.min(Math.abs(a1 - a2), Math.abs(ma1 - ma2))
        }

        /**
         * Returns the (shortest) difference between two angles, assuming that both angles are in
         * [-pi, +pi].
         */
        fun angularDifference(a1: Float, a2: Float): Float {
            val ma1 = mirrorAngle(a1)
            val ma2 = mirrorAngle(a2)
            val diff = a1 - a2
            val mdiff = ma2 - ma1
            return if (Math.abs(diff) < Math.abs(mdiff)) diff else mdiff
        }

        /**
         * Returns an angle in the range [-pi, pi).
         */
        fun normalizeAngle(a: Float): Float {
            var a = a
            while (a < -FloatMath.PI) {
                a += TWO_PI
            }
            while (a >= FloatMath.PI) {
                a -= TWO_PI
            }
            return a
        }

        /**
         * Returns an angle in the range [0, 2pi).
         */
        fun normalizeAnglePositive(a: Float): Float {
            var a = a
            while (a < 0f) {
                a += TWO_PI
            }
            while (a >= TWO_PI) {
                a -= TWO_PI
            }
            return a
        }

        /**
         * Returns the mirror angle of the specified angle (assumed to be in [-pi, +pi]). The angle is
         * mirrored around the PI/2 if it is positive, and -PI/2 if it is negative. One can visualize
         * this as mirroring around the "y-axis".
         */
        fun mirrorAngle(a: Float): Float {
            return (if (a > 0f) FloatMath.PI else -FloatMath.PI) - a
        }

        /**
         * Sets the number of decimal places to show when formatting values. By default, they are
         * formatted to three decimal places.
         */
        fun setToStringDecimalPlaces(places: Int) {
            if (places < 0) throw IllegalArgumentException("Decimal places must be >= 0.")
            TO_STRING_DECIMAL_PLACES = places
        }

        /**
         * Formats the supplied floating point value, truncated to the given number of decimal places.
         * The value is also always preceded by a sign (e.g. +1.0 or -0.5).
         */
        @JvmOverloads fun toString(value: Float, decimalPlaces: Int = TO_STRING_DECIMAL_PLACES): String {
            var value = value
            if (java.lang.Float.isNaN(value)) return "NaN"

            val buf = StringBuilder()
            if (value >= 0)
                buf.append("+")
            else {
                buf.append("-")
                value = -value
            }
            var ivalue = value.toInt()
            buf.append(ivalue)
            if (decimalPlaces > 0) {
                buf.append(".")
                for (ii in 0..decimalPlaces - 1) {
                    value = (value - ivalue) * 10
                    ivalue = value.toInt()
                    buf.append(ivalue)
                }
                // trim trailing zeros
                for (ii in 0..decimalPlaces - 1 - 1) {
                    if (buf[buf.length - 1] == '0') {
                        buf.setLength(buf.length - 1)
                    }
                }
            }
            return buf.toString()
        }

        protected var TO_STRING_DECIMAL_PLACES = 3
    }
}
/**
 * Formats the supplied value, truncated to the currently configured number of decimal places.
 * The value is also always preceded by a sign (e.g. +1.0 or -0.5).
 */