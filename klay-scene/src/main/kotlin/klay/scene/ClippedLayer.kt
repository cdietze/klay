package klay.scene

import euklid.f.IDimension
import euklid.f.Point
import euklid.f.Vector
import klay.core.Surface
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

/**
 * A layer whose rendering is (usually) clipped to a rectangle. The clipping rectangle is defined
 * to be the layer's `x, y` coordinate (as adjusted by its origin) extended to the layer's
 * scaled width and height.

 *
 * NOTE: clipping rectangles cannot be rotated. If the layer has a rotation, the clipping region
 * will be undefined (and most certainly wacky).
 */
abstract class ClippedLayer(private var width: Float, private var height: Float) : Layer() {

    private val pos = Point()
    private val size = Vector()

    override fun width(): Float {
        return this.width
    }

    override fun height(): Float {
        return this.height
    }

    /** Updates the size of this clipped layer, and hence its clipping rectangle.  */
    fun setSize(width: Float, height: Float): ClippedLayer {
        this.width = width
        this.height = height
        checkOrigin()
        return this
    }

    /** Updates the size of this clipped layer, and hence its clipping rectangle.  */
    fun setSize(size: IDimension): ClippedLayer {
        return setSize(size.width, size.height)
    }

    /** Updates the width of this group layer, and hence its clipping rectangle.  */
    fun setWidth(width: Float): ClippedLayer {
        this.width = width
        checkOrigin()
        return this
    }

    /** Updates the height of this group layer, and hence its clipping rectangle.  */
    fun setHeight(height: Float): ClippedLayer {
        this.height = height
        checkOrigin()
        return this
    }

    protected open fun disableClip(): Boolean {
        return false
    }

    override fun paintImpl(surf: Surface) {
        if (disableClip())
            paintClipped(surf)
        else {
            val tx = surf.tx()
            val originX = originX()
            val originY = originY()
            tx.translate(originX, originY)
            tx.transform(pos.set(-originX, -originY), pos)
            tx.transform(size.set(width, height), size)
            tx.translate(-originX, -originY)
            val nonEmpty = surf.startClipped(
                    pos.x.toInt(), pos.y.toInt(), size.x.absoluteValue.roundToInt(), size.y.absoluteValue.roundToInt())
            try {
                if (nonEmpty) paintClipped(surf)
            } finally {
                surf.endClipped()
            }
        }
    }

    /**
     * Renders this layer with the clipping region in effect. NOTE: this layer's transform will
     * already have been applied to the surface.
     */
    protected abstract fun paintClipped(surf: Surface)
}
