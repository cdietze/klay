package klay.core

import pythagoras.f.AffineTransform
import pythagoras.f.Points
import react.RFuture

/**
 * Represents a square region of a texture. This makes it easy to render tiles from texture
 * atlases.
 */
abstract class Tile : TileSource {

    /** The texture which contains this tile.  */
    abstract val texture: Texture

    /** The width of this tile (in display units).  */
    abstract val width: Float

    /** The height of this tile (in display units).  */
    abstract val height: Float

    /** Returns the `s` texture coordinate for the x-axis.  */
    abstract val sx: Float

    /** Returns the `s` texture coordinate for the y-axis.  */
    abstract val sy: Float

    /** Returns the `t` texture coordinate for the x-axis.  */
    abstract val tx: Float

    /** Returns the `t` texture coordinate for the y-axis.  */
    abstract val ty: Float

    override val isLoaded: Boolean
        get() = true

    /** Adds this tile to the supplied quad batch.  */
    abstract fun addToBatch(batch: QuadBatch, tint: Int, tx: AffineTransform,
                            x: Float, y: Float, width: Float, height: Float)

    /** Adds this tile to the supplied quad batch.  */
    abstract fun addToBatch(batch: QuadBatch, tint: Int, tx: AffineTransform,
                            dx: Float, dy: Float, dw: Float, dh: Float,
                            sx: Float, sy: Float, sw: Float, sh: Float)

    override fun tile(): Tile {
        return this
    }

    override fun tileAsync(): RFuture<Tile> {
        return RFuture.success(this)
    }

    override fun toString(): String {
        return "Tile[" + width + "x" + height +
                "/" + Points.pointToString(sx, sy) + "/" + Points.pointToString(tx, ty) +
                "] <- " + texture
    }
}
