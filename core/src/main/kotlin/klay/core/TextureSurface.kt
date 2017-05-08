package klay.core

/**
 * A [Surface] which renders to a [Texture] instead of to the default frame buffer.

 *
 * Note: a `TextureSurface` makes use of three GPU resources: a framebuffer, a quad batch
 * and a texture. The framebuffer's lifecycle is tied to the lifecycle of the `TextureSurface`. When you [close] it the framebuffer is disposed.

 *
 * The quad batch's lifecycle is independent of the `TextureSurface`. Most likely you will
 * use the default quad batch for your game which lives for the lifetime of your game.

 *
 * The texture's lifecycle is also independent of the `TextureSurface` and is managed by
 * reference counting. The texture is neither referenced, nor released by the `TextureSurface`. It is assumed that the texture will be stuffed into an `ImageLayer` or
 * used for rendering elsewhere and that code will manage the texture's lifecycle (even if the
 * texture is created by `TextureSurface` in the first place).
 */
class TextureSurface
/** Creates a texture surface which renders to `texture`.  */
(gfx: Graphics, defaultBatch: QuadBatch,
 /** The texture into which we're rendering.  */
 val texture: Texture) : Surface(gfx, RenderTarget.create(gfx, texture), defaultBatch) {

    /** Creates a texture surface which is `width x height` in display units.
     * A managed backing texture will be automatically created.  */
    constructor(gfx: Graphics, defaultBatch: QuadBatch, width: Float, height: Float) : this(gfx, defaultBatch, gfx.createTexture(width, height, Texture.Config.DEFAULT)) {}

    override fun close() {
        super.close()
        target.close()
    }
}
