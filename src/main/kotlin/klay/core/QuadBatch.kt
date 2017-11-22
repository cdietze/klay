package klay.core

import euklid.f.AffineTransform

/**
 * A batch which can render textured quads. Since that's a pretty common thing to do, we factor out
 * this API, and allow for different implementations thereof.
 */
abstract class QuadBatch protected constructor(gl: GL20) : TexturedBatch(gl) {

    /** Adds `tex` as a transformed axis-aligned quad to this batch.
     * `x, y, w, h` define the size and position of the quad.  */
    fun addQuad(tex: Texture, tint: Int, xf: AffineTransform,
                x: Float, y: Float, w: Float, h: Float) {
        setTexture(tex)
        val sr = (if (tex.config.repeatX) w / tex.displayWidth else 1f)
        val sb = (if (tex.config.repeatY) h / tex.displayHeight else 1f)
        addQuad(tint, xf, x, y, x + w, y + h, 0f, 0f, sr, sb)
    }

    /** Adds `tex` as a transformed axis-aligned quad to this batch.
     * `dx, dy, dw, dh` define the size and position of the quad.
     * `sx, sy, sw, sh` define region of the texture which will be displayed in the quad.  */
    fun addQuad(tex: Texture, tint: Int, xf: AffineTransform,
                dx: Float, dy: Float, dw: Float, dh: Float,
                sx: Float, sy: Float, sw: Float, sh: Float) {
        setTexture(tex)
        val texWidth = tex.displayWidth
        val texHeight = tex.displayHeight
        addQuad(tint, xf, dx, dy, dx + dw, dy + dh,
                sx / texWidth, sy / texHeight, (sx + sw) / texWidth, (sy + sh) / texHeight)
    }

    /** Adds a transformed axis-aligned quad to this batch.
     * `left, top, right, bottom` define the bounds of the quad.
     * `sl, st, sr, sb` define the texture coordinates.  */
    fun addQuad(tint: Int, xf: AffineTransform,
                left: Float, top: Float, right: Float, bottom: Float,
                sl: Float, st: Float, sr: Float, sb: Float) {
        addQuad(tint, xf.m00, xf.m01, xf.m10, xf.m11, xf.tx, xf.ty,
                left, top, right, bottom, sl, st, sr, sb)
    }

    /** Adds a transformed axis-aligned quad to this batch.
     * `m00, m01, m10, m11, tx, ty` define the affine transform applied to the quad.
     * `left, top, right, bottom` define the bounds of the quad.
     * `sl, st, sr, sb` define the texture coordinates.  */
    fun addQuad(tint: Int, m00: Float, m01: Float, m10: Float, m11: Float, tx: Float, ty: Float,
                left: Float, top: Float, right: Float, bottom: Float,
                sl: Float, st: Float, sr: Float, sb: Float) {
        addQuad(tint, m00, m01, m10, m11, tx, ty,
                left, top, sl, st,
                right, top, sr, st,
                left, bottom, sl, sb,
                right, bottom, sr, sb)
    }

    /** Adds a transformed axis-aligned quad to this batch.
     * `m00, m01, m10, m11, tx, ty` define the affine transform applied to the quad.
     * `x1, y1, .., x4, y4` define the corners of the quad.
     * `sx1, sy1, .., sx4, sy4` define the texture coordinate of the quad.  */
    abstract fun addQuad(tint: Int,
                         m00: Float, m01: Float, m10: Float, m11: Float, tx: Float, ty: Float,
                         x1: Float, y1: Float, sx1: Float, sy1: Float,
                         x2: Float, y2: Float, sx2: Float, sy2: Float,
                         x3: Float, y3: Float, sx3: Float, sy3: Float,
                         x4: Float, y4: Float, sx4: Float, sy4: Float)
}
