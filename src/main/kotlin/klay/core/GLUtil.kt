package klay.core

object GLUtil {

    /**
     * Returns the next largest power of two, or zero if x is already a power of two.
     */
    fun nextPowerOfTwo(x: Int): Int {
        assert(x < 0x10000)

        var bit = 0x8000
        var highest = -1
        var count = 0
        var i = 15
        while (i >= 0) {
            if (x and bit != 0) {
                ++count
                if (highest == -1) {
                    highest = i
                }
            }
            --i
            bit = bit shr 1
        }
        if (count <= 1) {
            return 0
        }
        return 1 shl highest + 1
    }
}
