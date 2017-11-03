package tripleklay.util

import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin
import pythagoras.f.MathUtil

/**
 * Abstracts the process of interpolation between two values.
 */
abstract class Interpolator {

    /**
     * Interpolates between zero and one according to this interpolator's function.

     * @param v a value between zero and one (usually `elapsed/total` time).
     */
    abstract fun apply(v: Float): Float

    /**
     * Interpolates between zero and one according to this interpolator's function.

     * @param dt the amount of time that has elapsed.
     * *
     * @param t the total amount of time for the interpolation. If t == 0, the result is undefined.
     */
    fun apply(dt: Float, t: Float): Float {
        return apply(dt / t)
    }

    /**
     * Interpolates between two values, as in [.apply] except that `dt` is
     * clamped to [0..t] to avoid interpolation weirdness if `dt` is ever negative or exceeds
     * `t`.
     */
    fun applyClamp(dt: Float, t: Float): Float {
        return apply(if (dt < 0) 0f else if (dt > 1) 1f else dt, t)
    }

    /**
     * Interpolates between two values.

     * @param start the starting value.
     * *
     * @param range the difference between the ending value and the starting value.
     * *
     * @param dt the amount of time that has elapsed.
     * *
     * @param t the total amount of time for the interpolation. If t == 0, start+range will be
     * * returned.
     */
    fun apply(start: Float, range: Float, dt: Float, t: Float): Float {
        val pos = if (t == 0f) 1f else apply(dt, t)
        return start + range * pos
    }

    /**
     * Interpolates between two values, as in [.apply] except that `dt` is clamped to
     * [0..t] to avoid interpolation weirdness if `dt` is ever negative or exceeds `t`.
     */
    fun applyClamp(start: Float, range: Float, dt: Float, t: Float): Float {
        return apply(start, range, MathUtil.clamp(dt, 0f, t), t)
    }

    companion object {
        /** An interpolator that always returns the starting position.  */
        var NOOP: Interpolator = object : Interpolator() {
            override fun toString(): String {
                return "NOOP"
            }

            override fun apply(v: Float): Float {
                return 0f
            }
        }

        /** A linear interpolator.  */
        var LINEAR: Interpolator = object : Interpolator() {
            override fun toString(): String {
                return "LINEAR"
            }

            override fun apply(v: Float): Float {
                return v
            }
        }

        /** An interpolator that starts to change slowly and ramps up to full speed.  */
        var EASE_IN: Interpolator = object : Interpolator() {
            override fun toString(): String {
                return "EASE_IN"
            }

            override fun apply(v: Float): Float {
                return v * v * v
            }
        }

        /** An interpolator that starts to change quickly and eases into the final value.  */
        var EASE_OUT: Interpolator = object : Interpolator() {
            override fun toString(): String {
                return "EASE_OUT"
            }

            override fun apply(v: Float): Float {
                val vv = v - 1
                return 1 + vv * vv * vv
            }
        }

        /** An interpolator that eases away from the starting value, speeds up, then eases into the
         * final value.  */
        var EASE_INOUT: Interpolator = object : Interpolator() {
            override fun toString(): String {
                return "EASE_INOUT"
            }

            override fun apply(v: Float): Float {
                val v2 = 2 * v
                if (v2 < 1) {
                    return v2 * v2 * v2 / 2
                }
                val ov = v2 - 2
                return (2 + ov * ov * ov) / 2
            }
        }

        /** An interpolator that undershoots the starting value, then speeds up into the final value  */
        var EASE_IN_BACK: Interpolator = object : Interpolator() {
            override fun toString(): String {
                return "EASE_IN_BACK"
            }

            override fun apply(v: Float): Float {
                val curvature = 1.70158f
                return v * v * ((curvature + 1) * v - curvature)
            }
        }

        /** An interpolator that eases into the final value and overshoots it before settling on it.  */
        var EASE_OUT_BACK: Interpolator = object : Interpolator() {
            override fun toString(): String {
                return "EASE_OUT_BACK"
            }

            override fun apply(v: Float): Float {
                val curvature = 1.70158f
                val v1 = v - 1
                return v1 * v1 * ((curvature + 1) * v1 + curvature) + 1
            }
        }

        var BOUNCE_OUT: Interpolator = object : Interpolator() {
            override fun toString(): String {
                return "BOUNCE_OUT"
            }

            override fun apply(v: Float): Float {
                if (v < 1 / 2.75f) {
                    return 7.5625f * v * v
                } else if (v < 2 / 2.75f) {
                    val vBounce = v - 1.5f / 2.75f
                    return 7.5625f * vBounce * vBounce + 0.75f
                } else if (v < 2.5 / 2.75) {
                    val vBounce = v - 2.25f / 2.75f
                    return 7.5625f * vBounce * vBounce + 0.9375f
                } else {
                    val vBounce = v - 2.625f / 2.75f
                    return 7.5625f * vBounce * vBounce + 0.984375f
                }
            }
        }

        /** An interpolator that eases past the final value then back towards it elastically.  */
        var EASE_OUT_ELASTIC: Interpolator = object : Interpolator() {
            override fun toString(): String {
                return "EASE_OUT_ELASTIC"
            }

            override fun apply(v: Float): Float {
                return (2.0.pow(-10.0 * v) * sin(((v - K) * J).toDouble()) + 1).toFloat()
            }

            private val K = 0.3f / 4
            private val J = (2 * PI / 0.3).toFloat()
        }
    }
}
