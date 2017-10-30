package klay.core

/**
 * A bitmap fill pattern created by [Image.createPattern].
 */
abstract class Pattern protected constructor(
        /** Whether this pattern repeats in the x-direction.  */
        val repeatX: Boolean,
        /** Whether this pattern repeats in the y-direction.  */
        val repeatY: Boolean)
