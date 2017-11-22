package klay.core

import euklid.f.MathUtil

/**
 * Tinting related utility methods.
 */
object Tint {

    /** A tint that does not change the underlying color.  */
    val NOOP_TINT = 0xFFFFFFFF.toInt()

    /** Returns the combination of `curTint` and `tint`.  */
    fun combine(curTint: Int, tint: Int): Int {
        val newA = (curTint shr 24 and 0xFF) * ((tint shr 24 and 0xFF) + 1) and 0xFF00 shl 16
        if (tint and 0xFFFFFF == 0xFFFFFF) { // fast path to just combine alpha
            return newA or (curTint and 0xFFFFFF)
        }

        // otherwise combine all the channels (beware the bit mask-and-shiftery!)
        val newR = (curTint shr 16 and 0xFF) * ((tint shr 16 and 0xFF) + 1) and 0xFF00 shl 8
        val newG = (curTint shr 8 and 0xFF) * ((tint shr 8 and 0xFF) + 1) and 0xFF00
        val newB = (curTint and 0xFF) * ((tint and 0xFF) + 1) shr 8 and 0xFF
        return newA or newR or newG or newB
    }

    /** Sets the alpha component of `tint` to `alpha`.
     * @return the new tint.
     */
    fun setAlpha(tint: Int, alpha: Float): Int {
        val ialpha = (0xFF * MathUtil.clamp(alpha, 0f, 1f)).toInt()
        return ialpha shl 24 or (tint and 0xFFFFFF)
    }

    /** Returns the alpha component of `tint` as a float between `[0, 1]`.  */
    fun getAlpha(tint: Int): Float {
        return (tint shr 24 and 0xFF) / 255f
    }
}
