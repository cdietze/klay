package java.lang

import javaemul.internal.InternalPreconditions.checkCriticalArithmetic

import jsinterop.annotations.JsPackage
import jsinterop.annotations.JsType

/**
 * Math utility methods and constants.
 */
object Math {
    // The following methods are not implemented because JS doesn't provide the
    // necessary pieces:
    //   public static double ulp (double x)
    //   public static float ulp (float x)
    //   public static int getExponent (double d)
    //   public static int getExponent (float f)
    //   public static double IEEEremainder(double f1, double f2)
    //   public static double nextAfter(double start, double direction)
    //   public static float nextAfter(float start, float direction)
    //   public static double nextUp(double start) {
    //     return nextAfter(start, 1.0d);
    //   }
    //   public static float nextUp(float start) {
    //     return nextAfter(start,1.0f);
    //   }

    val E = 2.7182818284590452354
    val PI = 3.14159265358979323846

    private val PI_OVER_180 = PI / 180.0
    private val PI_UNDER_180 = 180.0 / PI

    fun abs(x: Double): Double {
        return NativeMath.abs(x)
    }

    fun abs(x: Float): Float {
        return NativeMath.abs(x.toDouble()).toFloat()
    }

    fun abs(x: Int): Int {
        return if (x < 0) -x else x
    }

    fun abs(x: Long): Long {
        return if (x < 0) -x else x
    }

    fun acos(x: Double): Double {
        return NativeMath.acos(x)
    }

    fun asin(x: Double): Double {
        return NativeMath.asin(x)
    }

    fun addExact(x: Int, y: Int): Int {
        val r = x.toDouble() + y.toDouble()
        checkCriticalArithmetic(isSafeIntegerRange(r))
        return r.toInt()
    }

    fun addExact(x: Long, y: Long): Long {
        val r = x + y
        // "Hacker's Delight" 2-12 Overflow if both arguments have the opposite sign of the result
        checkCriticalArithmetic(x xor r and (y xor r) >= 0)
        return r
    }

    fun atan(x: Double): Double {
        return NativeMath.atan(x)
    }

    fun atan2(y: Double, x: Double): Double {
        return NativeMath.atan2(y, x)
    }

    fun cbrt(x: Double): Double {
        return if (x == 0.0 || !java.lang.Double.isFinite(x)) x else NativeMath.pow(x, 1.0 / 3.0)
    }

    fun ceil(x: Double): Double {
        return NativeMath.ceil(x)
    }

    fun copySign(magnitude: Double, sign: Double): Double {
        return if (isNegative(sign)) -NativeMath.abs(magnitude) else NativeMath.abs(magnitude)
    }

    private fun isNegative(d: Double): Boolean {
        return d < 0 || 1 / d < 0
    }

    fun copySign(magnitude: Float, sign: Float): Float {
        return copySign(magnitude.toDouble(), sign.toDouble()).toFloat()
    }

    fun cos(x: Double): Double {
        return NativeMath.cos(x)
    }

    fun cosh(x: Double): Double {
        return (NativeMath.exp(x) + NativeMath.exp(-x)) / 2
    }

    fun decrementExact(x: Int): Int {
        checkCriticalArithmetic(x != Integer.MIN_VALUE)
        return x - 1
    }

    fun decrementExact(x: Long): Long {
        checkCriticalArithmetic(x != java.lang.Long.MIN_VALUE)
        return x - 1
    }

    fun exp(x: Double): Double {
        return NativeMath.exp(x)
    }

    fun expm1(d: Double): Double {
        return if (d == 0.0) d else NativeMath.exp(d) - 1
    }

    fun floor(x: Double): Double {
        return NativeMath.floor(x)
    }

    fun floorDiv(dividend: Int, divisor: Int): Int {
        checkCriticalArithmetic(divisor != 0)
        // round down division if the signs are different and modulo not zero
        return if (dividend xor divisor >= 0) dividend / divisor else (dividend + 1) / divisor - 1
    }

    fun floorDiv(dividend: Long, divisor: Long): Long {
        checkCriticalArithmetic(divisor != 0)
        // round down division if the signs are different and modulo not zero
        return if (dividend xor divisor >= 0) dividend / divisor else (dividend + 1) / divisor - 1
    }

    fun floorMod(dividend: Int, divisor: Int): Int {
        checkCriticalArithmetic(divisor != 0)
        return (dividend % divisor + divisor) % divisor
    }

    fun floorMod(dividend: Long, divisor: Long): Long {
        checkCriticalArithmetic(divisor != 0)
        return (dividend % divisor + divisor) % divisor
    }

    fun hypot(x: Double, y: Double): Double {
        return if (java.lang.Double.isInfinite(x) || java.lang.Double.isInfinite(y))
            java.lang.Double.POSITIVE_INFINITY
        else
            NativeMath.sqrt(x * x + y * y)
    }

    fun incrementExact(x: Int): Int {
        checkCriticalArithmetic(x != Integer.MAX_VALUE)
        return x + 1
    }

    fun incrementExact(x: Long): Long {
        checkCriticalArithmetic(x != java.lang.Long.MAX_VALUE)
        return x + 1
    }

    fun log(x: Double): Double {
        return NativeMath.log(x)
    }

    fun log10(x: Double): Double {
        return NativeMath.log(x) * NativeMath.LOG10E
    }

    fun log1p(x: Double): Double {
        return if (x == 0.0) x else NativeMath.log(x + 1)
    }

    fun max(x: Double, y: Double): Double {
        return NativeMath.max(x, y)
    }

    fun max(x: Float, y: Float): Float {
        return NativeMath.max(x.toDouble(), y.toDouble()).toFloat()
    }

    fun max(x: Int, y: Int): Int {
        return if (x > y) x else y
    }

    fun max(x: Long, y: Long): Long {
        return if (x > y) x else y
    }

    fun min(x: Double, y: Double): Double {
        return NativeMath.min(x, y)
    }

    fun min(x: Float, y: Float): Float {
        return NativeMath.min(x.toDouble(), y.toDouble()).toFloat()
    }

    fun min(x: Int, y: Int): Int {
        return if (x < y) x else y
    }

    fun min(x: Long, y: Long): Long {
        return if (x < y) x else y
    }

    fun multiplyExact(x: Int, y: Int): Int {
        val r = x.toDouble() * y.toDouble()
        checkCriticalArithmetic(isSafeIntegerRange(r))
        return r.toInt()
    }

    fun multiplyExact(x: Long, y: Long): Long {
        if (y == -1) {
            return negateExact(x)
        }
        if (y == 0) {
            return 0
        }
        val r = x * y
        checkCriticalArithmetic(r / y == x)
        return r
    }

    fun negateExact(x: Int): Int {
        checkCriticalArithmetic(x != Integer.MIN_VALUE)
        return -x
    }

    fun negateExact(x: Long): Long {
        checkCriticalArithmetic(x != java.lang.Long.MIN_VALUE)
        return -x
    }

    fun pow(x: Double, exp: Double): Double {
        return NativeMath.pow(x, exp)
    }

    fun random(): Double {
        return NativeMath.random()
    }

    fun rint(x: Double): Double {
        var x = x
        // Floating point has a mantissa with an accuracy of 52 bits so
        // any number bigger than 2^52 is effectively a finite integer value.
        // This case also filters out NaN and infinite values.
        if (NativeMath.abs(x) < (1L shl 52).toDouble()) {
            val mod2 = x % 2
            if (mod2 == -1.5 || mod2 == 0.5) {
                x = NativeMath.floor(x)
            } else {
                x = NativeMath.round(x)
            }
        }
        return x
    }

    fun round(x: Double): Long {
        return NativeMath.round(x).toLong()
    }

    fun round(x: Float): Int {
        return NativeMath.round(x.toDouble()).toInt()
    }

    fun subtractExact(x: Int, y: Int): Int {
        val r = x.toDouble() - y.toDouble()
        checkCriticalArithmetic(isSafeIntegerRange(r))
        return r.toInt()
    }

    fun subtractExact(x: Long, y: Long): Long {
        val r = x - y
        // "Hacker's Delight" Overflow if the arguments have different signs and
        // the sign of the result is different than the sign of x
        checkCriticalArithmetic(x xor y and (x xor r) >= 0)
        return r
    }

    fun scalb(d: Double, scaleFactor: Int): Double {
        if (scaleFactor >= 31 || scaleFactor <= -31) {
            return d * NativeMath.pow(2.0, scaleFactor.toDouble())
        } else if (scaleFactor > 0) {
            return d * (1 shl scaleFactor)
        } else if (scaleFactor == 0) {
            return d
        } else {
            return d / (1 shl -scaleFactor)
        }
    }

    fun scalb(f: Float, scaleFactor: Int): Float {
        return scalb(f.toDouble(), scaleFactor).toFloat()
    }

    fun signum(d: Double): Double {
        if (d == 0.0 || java.lang.Double.isNaN(d)) {
            return d
        } else {
            return (if (d < 0) -1 else 1).toDouble()
        }
    }

    fun signum(f: Float): Float {
        return signum(f.toDouble()).toFloat()
    }

    fun sin(x: Double): Double {
        return NativeMath.sin(x)
    }

    fun sinh(x: Double): Double {
        return if (x == 0.0) x else (NativeMath.exp(x) - NativeMath.exp(-x)) / 2
    }

    fun sqrt(x: Double): Double {
        return NativeMath.sqrt(x)
    }

    fun tan(x: Double): Double {
        return NativeMath.tan(x)
    }

    fun tanh(x: Double): Double {
        if (x == 0.0) {
            return x
        } else if (java.lang.Double.isInfinite(x)) {
            return signum(x)
        } else {
            val e2x = NativeMath.exp(2 * x)
            return (e2x - 1) / (e2x + 1)
        }
    }

    fun toDegrees(x: Double): Double {
        return x * PI_UNDER_180
    }

    fun toIntExact(x: Long): Int {
        val ix = x.toInt()
        checkCriticalArithmetic(ix.toLong() == x)
        return ix
    }

    fun toRadians(x: Double): Double {
        return x * PI_OVER_180
    }

    private fun isSafeIntegerRange(value: Double): Boolean {
        return Integer.MIN_VALUE <= value && value <= Integer.MAX_VALUE
    }

    @JsType(isNative = true, name = "Math", namespace = JsPackage.GLOBAL)
    private object NativeMath {
        var LOG10E: Double = 0.toDouble()
        external fun abs(x: Double): Double
        external fun acos(x: Double): Double
        external fun asin(x: Double): Double
        external fun atan(x: Double): Double
        external fun atan2(y: Double, x: Double): Double
        external fun ceil(x: Double): Double
        external fun cos(x: Double): Double
        external fun exp(x: Double): Double
        external fun floor(x: Double): Double
        external fun log(x: Double): Double
        external fun max(x: Double, y: Double): Double
        external fun min(x: Double, y: Double): Double
        external fun pow(x: Double, exp: Double): Double
        external fun random(): Double
        external fun round(x: Double): Double
        external fun sin(x: Double): Double
        external fun sqrt(x: Double): Double
        external fun tan(x: Double): Double
    }
}