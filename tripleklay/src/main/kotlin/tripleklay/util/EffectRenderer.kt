package tripleklay.util

import klay.core.Canvas
import klay.core.TextLayout
import klay.core.assert
import kotlin.math.abs

/**
 * Handles the rendering of text with a particular effect (shadow, outline, etc.).
 */
abstract class EffectRenderer {

    open fun adjustWidth(width: Float): Float {
        return width
    }

    open fun adjustHeight(height: Float): Float {
        return height
    }

    fun offsetX(): Float {
        return 0f
    }

    fun offsetY(): Float {
        return 0f
    }

    abstract fun render(canvas: Canvas, layout: TextLayout, textColor: Int,
                        underlined: Boolean, x: Float, y: Float)

    class PixelOutline(val outlineColor: Int) : EffectRenderer() {

        override fun adjustWidth(width: Float): Float {
            return width + 2
        }

        override fun adjustHeight(height: Float): Float {
            return height + 2
        }

        override fun render(canvas: Canvas, text: TextLayout, textColor: Int,
                            underlined: Boolean, x: Float, y: Float) {
            canvas.save()
            if (underlined) {
                val bounds = text.bounds
                val sx = x + bounds.x + 1
                val sy = y + bounds.y + bounds.height + 2
                canvas.setFillColor(outlineColor).fillRect(sx - 1, sy - 1, bounds.width + 3, 3f)
                canvas.setFillColor(textColor).fillRect(sx, sy, bounds.width, 1f)
            }
            canvas.setFillColor(outlineColor)
            canvas.fillText(text, x + 0, y + 0)
            canvas.fillText(text, x + 0, y + 1)
            canvas.fillText(text, x + 0, y + 2)
            canvas.fillText(text, x + 1, y + 0)
            canvas.fillText(text, x + 1, y + 2)
            canvas.fillText(text, x + 2, y + 0)
            canvas.fillText(text, x + 2, y + 1)
            canvas.fillText(text, x + 2, y + 2)
            canvas.setFillColor(textColor)
            canvas.fillText(text, x + 1, y + 1)
            canvas.restore()
        }

        override fun equals(obj: Any?): Boolean {
            if (obj !is PixelOutline) return false
            return outlineColor == obj.outlineColor
        }

        override fun hashCode(): Int {
            return outlineColor
        }
    }

    class VectorOutline constructor(val outlineColor: Int, val outlineWidth: Float,
                                                  val outlineCap: Canvas.LineCap = Canvas.LineCap.ROUND, val outlineJoin: Canvas.LineJoin = Canvas.LineJoin.ROUND) : EffectRenderer() {

        override fun adjustWidth(width: Float): Float {
            return width + 2 * outlineWidth
        }

        override fun adjustHeight(height: Float): Float {
            return height + 2 * outlineWidth
        }

        override fun render(canvas: Canvas, text: TextLayout, textColor: Int,
                            underlined: Boolean, x: Float, y: Float) {
            canvas.save()
            canvas.setStrokeColor(outlineColor)
            canvas.setStrokeWidth(outlineWidth * 2)
            canvas.setLineCap(outlineCap)
            canvas.setLineJoin(outlineJoin)
            canvas.strokeText(text, x + outlineWidth, y + outlineWidth)
            canvas.setFillColor(textColor)
            canvas.fillText(text, x + outlineWidth, y + outlineWidth)
            if (underlined) {
                val bounds = text.bounds
                val sx = x + bounds.x + outlineWidth
                val sy = y + bounds.y + bounds.height + outlineWidth + 1
                canvas.fillRect(sx, sy, bounds.width, 1f)
            }
            canvas.restore()
        }

        override fun equals(obj: Any?): Boolean {
            if (obj !is VectorOutline) return false
            val that = obj
            return outlineColor == that.outlineColor && outlineWidth == that.outlineWidth &&
                    outlineCap === that.outlineCap && outlineJoin === that.outlineJoin
        }

        override fun hashCode(): Int {
            return outlineColor xor outlineWidth.toInt() xor outlineCap.hashCode() xor
                    outlineJoin.hashCode()
        }
    }

    class Shadow(val shadowColor: Int, val shadowX: Float, val shadowY: Float) : EffectRenderer() {

        override fun adjustWidth(width: Float): Float {
            return width + abs(shadowX)
        }

        override fun adjustHeight(height: Float): Float {
            return height + abs(shadowY)
        }

        override fun render(canvas: Canvas, text: TextLayout, textColor: Int,
                            underlined: Boolean, x: Float, y: Float) {
            val tx = if (shadowX < 0) -shadowX else 0f
            val ty = if (shadowY < 0) -shadowY else 0f
            val sx = if (shadowX < 0) 0f else shadowX
            val sy = if (shadowY < 0) 0f else shadowY
            canvas.save()
            if (underlined) {
                val bounds = text.bounds
                canvas.setFillColor(shadowColor).fillRect(
                        sx + bounds.x + x, sy + bounds.y + bounds.height + 1, bounds.width + 1, 1f)
                canvas.setFillColor(textColor).fillRect(
                        tx + bounds.x + x, ty + bounds.y + bounds.height + 1, bounds.width + 1, 1f)
            }
            canvas.setFillColor(shadowColor)
            canvas.fillText(text, x + sx, y + sy)
            canvas.setFillColor(textColor)
            canvas.fillText(text, x + tx, y + ty)
            canvas.restore()
        }

        override fun equals(obj: Any?): Boolean {
            if (obj !is Shadow) return false
            val that = obj
            return shadowColor == that.shadowColor &&
                    shadowX == that.shadowX && shadowY == that.shadowY
        }

        override fun hashCode(): Int {
            return shadowColor xor shadowX.toInt() xor shadowY.toInt()
        }
    }

    class Gradient(val gradientColor: Int, val gradientType: Gradient.Type) : EffectRenderer() {
        /** Defines different types of gradient fills.  */
        enum class Type {
            /** Gradient color on the bottom (default).  */
            BOTTOM,
            /** Gradient color on top.  */
            TOP,
            /** Gradient color in the center.  */
            CENTER
        }

        override fun render(canvas: Canvas, text: TextLayout, textColor: Int,
                            underlined: Boolean, x: Float, y: Float) {

            var colors: IntArray? = null
            var positions: FloatArray? = null

            when (gradientType) {
                EffectRenderer.Gradient.Type.BOTTOM -> {
                    colors = intArrayOf(textColor, gradientColor)
                    positions = floatArrayOf(0f, 1f)
                }
                EffectRenderer.Gradient.Type.TOP -> {
                    colors = intArrayOf(gradientColor, textColor)
                    positions = floatArrayOf(0f, 1f)
                }
                EffectRenderer.Gradient.Type.CENTER -> {
                    colors = intArrayOf(textColor, gradientColor, textColor)
                    positions = floatArrayOf(0f, 0.5f, 1f)
                }
            }

            // The compiler should've warned if new values showed up in the enum, but sanity check
            assert(colors != null) { "Unhandled gradient type: " + gradientType }

            canvas.save()

            canvas.setFillGradient(canvas.createGradient(klay.core.Gradient.Linear(
                    0f, 0f, 0f, text.size.height, colors, positions)))
            canvas.fillText(text, x, y)

            if (underlined) {
                val bounds = text.bounds
                val sx = x + bounds.x
                val sy = y + bounds.y + bounds.height + 1
                canvas.fillRect(sx, sy, bounds.width, 1f)
            }

            canvas.restore()
        }

        override fun equals(obj: Any?): Boolean {
            if (obj !is Gradient) return false
            val that = obj
            return gradientColor == that.gradientColor && gradientType == that.gradientType
        }

        override fun hashCode(): Int {
            return 83 * gradientColor xor 113 * gradientType.ordinal
        }
    }

    companion object {
        /** An "effect" that just renders the text normally.  */
        val NONE: EffectRenderer = object : EffectRenderer() {
            override fun render(canvas: Canvas, layout: TextLayout, textColor: Int,
                                underlined: Boolean, x: Float, y: Float) {
                canvas.save()
                canvas.setFillColor(textColor)
                if (underlined) {
                    val bounds = layout.bounds
                    val sx = x + bounds.x
                    val sy = y + bounds.y + bounds.height + 1
                    canvas.fillRect(sx, sy, bounds.width, 1f)
                }
                canvas.fillText(layout, x, y)
                canvas.restore()
            }
        }
    }
}
