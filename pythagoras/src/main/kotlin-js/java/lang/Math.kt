package java.lang

import kotlin.js.Math as JsMath

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
        return JsMath.abs(x)
    }

    fun abs(x: Float): Float {
        return JsMath.abs(x.toDouble()).toFloat()
    }

    fun abs(x: Int): Int {
        return if (x < 0) -x else x
    }

    fun abs(x: Long): Long {
        return if (x < 0) -x else x
    }

    fun acos(x: Double): Double {
        return JsMath.acos(x)
    }

    fun asin(x: Double): Double {
        return JsMath.asin(x)
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
        return JsMath.atan(x)
    }

    fun atan2(y: Double, x: Double): Double {
        return JsMath.atan2(y, x)
    }

    fun cbrt(x: Double): Double {
        return if (x == 0.0 || !x.isFinite()) x else JsMath.pow(x, 1.0 / 3.0)
    }

    fun ceil(x: Double): Double {
        return JsMath.ceil(x).toDouble()
    }

    fun copySign(magnitude: Double, sign: Double): Double {
        return if (isNegative(sign)) -JsMath.abs(magnitude) else JsMath.abs(magnitude)
    }

    private fun isNegative(d: Double): Boolean {
        return d < 0 || 1 / d < 0
    }

    fun copySign(magnitude: Float, sign: Float): Float {
        return copySign(magnitude.toDouble(), sign.toDouble()).toFloat()
    }

    fun cos(x: Double): Double {
        return JsMath.cos(x)
    }

    fun cosh(x: Double): Double {
        return (JsMath.exp(x) + JsMath.exp(-x)) / 2
    }

    fun decrementExact(x: Int): Int {
        checkCriticalArithmetic(x != Int.MIN_VALUE)
        return x - 1
    }

    fun decrementExact(x: Long): Long {
        checkCriticalArithmetic(x != Long.MIN_VALUE)
        return x - 1
    }

    fun exp(x: Double): Double {
        return JsMath.exp(x)
    }

    fun expm1(d: Double): Double {
        return if (d == 0.0) d else JsMath.exp(d) - 1
    }

    fun floor(x: Double): Double {
        return JsMath.floor(x).toDouble()
    }

    fun floorDiv(dividend: Int, divisor: Int): Int {
        checkCriticalArithmetic(divisor != 0)
        // round down division if the signs are different and modulo not zero
        return if (dividend xor divisor >= 0) dividend / divisor else (dividend + 1) / divisor - 1
    }

    fun floorDiv(dividend: Long, divisor: Long): Long {
        checkCriticalArithmetic(divisor != 0L)
        // round down division if the signs are different and modulo not zero
        return if (dividend xor divisor >= 0) dividend / divisor else (dividend + 1) / divisor - 1
    }

    fun floorMod(dividend: Int, divisor: Int): Int {
        checkCriticalArithmetic(divisor != 0)
        return (dividend % divisor + divisor) % divisor
    }

    fun floorMod(dividend: Long, divisor: Long): Long {
        checkCriticalArithmetic(divisor != 0L)
        return (dividend % divisor + divisor) % divisor
    }

    fun hypot(x: Double, y: Double): Double {
        return if (x.isInfinite() || y.isInfinite())
            Double.POSITIVE_INFINITY
        else
            JsMath.sqrt(x * x + y * y)
    }

    fun incrementExact(x: Int): Int {
        checkCriticalArithmetic(x != Int.MAX_VALUE)
        return x + 1
    }

    fun incrementExact(x: Long): Long {
        checkCriticalArithmetic(x != Long.MAX_VALUE)
        return x + 1
    }

    fun log(x: Double): Double {
        return JsMath.log(x)
    }

    fun log10(x: Double): Double {
        return JsMath.log(x) * LOG10E
    }

    fun log1p(x: Double): Double {
        return if (x == 0.0) x else JsMath.log(x + 1)
    }

    fun max(x: Double, y: Double): Double {
        return JsMath.max(x, y)
    }

    fun max(x: Float, y: Float): Float {
        return JsMath.max(x.toDouble(), y.toDouble()).toFloat()
    }

    fun max(x: Int, y: Int): Int {
        return if (x > y) x else y
    }

    fun max(x: Long, y: Long): Long {
        return if (x > y) x else y
    }

    fun min(x: Double, y: Double): Double {
        return JsMath.min(x, y)
    }

    fun min(x: Float, y: Float): Float {
        return JsMath.min(x.toDouble(), y.toDouble()).toFloat()
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
        if (y == -1L) {
            return negateExact(x)
        }
        if (y == 0L) {
            return 0
        }
        val r = x * y
        checkCriticalArithmetic(r / y == x)
        return r
    }

    fun negateExact(x: Int): Int {
        checkCriticalArithmetic(x != Int.MIN_VALUE)
        return -x
    }

    fun negateExact(x: Long): Long {
        checkCriticalArithmetic(x != Long.MIN_VALUE)
        return -x
    }

    fun pow(x: Double, exp: Double): Double {
        return JsMath.pow(x, exp)
    }

    fun random(): Double {
        return JsMath.random()
    }

    fun rint(x: Double): Double {
        // Floating point has a mantissa with an accuracy of 52 bits so
        // any number bigger than 2^52 is effectively a finite integer value.
        // This case also filters out NaN and infinite values.
        if (JsMath.abs(x) < (1L shl 52).toDouble()) {
            val mod2 = x % 2
            if (mod2 == -1.5 || mod2 == 0.5) {
                return JsMath.floor(x).toDouble()
            } else {
                return JsMath.round(x).toDouble()
            }
        }
        return x
    }

    fun round(x: Double): Long {
        return JsMath.round(x).toLong()
    }

    fun round(x: Float): Int {
        return JsMath.round(x.toDouble()).toInt()
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
            return d * JsMath.pow(2.0, scaleFactor.toDouble())
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
        if (d == 0.0 || d.isNaN()) {
            return d
        } else {
            return (if (d < 0) -1 else 1).toDouble()
        }
    }

    fun signum(f: Float): Float {
        return signum(f.toDouble()).toFloat()
    }

    fun sin(x: Double): Double {
        return JsMath.sin(x)
    }

    fun sinh(x: Double): Double {
        return if (x == 0.0) x else (JsMath.exp(x) - JsMath.exp(-x)) / 2
    }

    fun sqrt(x: Double): Double {
        return JsMath.sqrt(x)
    }

    fun tan(x: Double): Double {
        return JsMath.tan(x)
    }

    fun tanh(x: Double): Double {
        if (x == 0.0) {
            return x
        } else if (x.isInfinite()) {
            return signum(x)
        } else {
            val e2x = JsMath.exp(2 * x)
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
        return Int.MIN_VALUE <= value && value <= Int.MAX_VALUE
    }
}

private fun checkCriticalArithmetic(expression: Boolean) {
    require(expression, { -> "ArithmeticException" })
}

// Missing in the kotlin mappings, so we define it ourself
private val LOG10E: Double = 0.4342944819032518