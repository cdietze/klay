package klay.jvm

import java.awt.Composite
import java.awt.CompositeContext
import java.awt.RenderingHints
import java.awt.image.ColorModel
import java.awt.image.Raster
import java.awt.image.WritableRaster

/**
 * Handles non-Porter-Duff image composition.
 */
class BlendComposite(val blender: BlendComposite.Blender, private val alpha: Float = 1f) : Composite {

    private val context = object : CompositeContext {
        override fun compose(src: Raster, dstIn: Raster, dstOut: WritableRaster) {
            val width = Math.min(src.width, dstIn.width)
            val height = Math.min(src.height, dstIn.height)
            val srcPixels = IntArray(width)
            val dstPixels = IntArray(width)
            for (yy in 0..height - 1) {
                src.getDataElements(0, yy, width, 1, srcPixels)
                dstIn.getDataElements(0, yy, width, 1, dstPixels)
                blender.blend(srcPixels, dstPixels, width, alpha)
                dstOut.setDataElements(0, yy, width, 1, dstPixels)
            }
        }

        override fun dispose() {
            // nada
        }
    }

    /** Returns a derived composite with the specified alpha.  */
    fun derive(alpha: Float): BlendComposite {
        return if (alpha == this.alpha) this else BlendComposite(blender, alpha)
    }

    override fun createContext(srcColorModel: ColorModel, dstColorModel: ColorModel,
                               hints: RenderingHints): CompositeContext {
        return context // our context maintains no state
    }

    abstract class Blender {
        fun blend(srcPixels: IntArray, dstPixels: IntArray, width: Int, alpha: Float) {
            for (xx in 0..width - 1) {
                // pixels are stored as INT_ARGB
                val srcARGB = srcPixels[xx]
                val dstARGB = dstPixels[xx]
                val srcA = srcARGB shr 24 and 0xFF
                val dstA = dstARGB shr 24 and 0xFF
                val srcR = srcARGB shr 16 and 0xFF
                val dstR = dstARGB shr 16 and 0xFF
                val srcG = srcARGB shr 8 and 0xFF
                val dstG = dstARGB shr 8 and 0xFF
                val srcB = srcARGB and 0xFF
                val dstB = dstARGB and 0xFF
                dstPixels[xx] = blend(srcA, srcR, srcG, srcB, dstA, dstR, dstG, dstB, alpha)
            }
        }

        protected abstract fun blend(srcA: Int, srcR: Int, srcG: Int, srcB: Int,
                                     dstA: Int, dstR: Int, dstG: Int, dstB: Int, alpha: Float): Int

        protected fun compose(a: Int, r: Int, g: Int, b: Int, dstA: Int, dstR: Int, dstG: Int, dstB: Int,
                              alpha: Float): Int {
            return 0xFF and (dstA + (a - dstA) * alpha).toInt() shl 24 or (
                    0xFF and (dstR + (r - dstR) * alpha).toInt() shl 16) or (
                    0xFF and (dstG + (g - dstG) * alpha).toInt() shl 8) or
                    (0xFF and (dstB + (b - dstB) * alpha).toInt())
        }
    }

    companion object {

        /** A blend composite that yields [ Sa * Da, Sc * Dc].  */
        val Multiply = BlendComposite(object : Blender() {
            override fun blend(srcA: Int, srcR: Int, srcG: Int, srcB: Int,
                               dstA: Int, dstR: Int, dstG: Int, dstB: Int, alpha: Float): Int {
                return compose(srcA + dstA - srcA * dstA / 255,
                        srcR * dstR shr 8,
                        srcG * dstG shr 8,
                        srcB * dstB shr 8,
                        dstA, dstR, dstG, dstB, alpha)
            }
        })
    }
}
