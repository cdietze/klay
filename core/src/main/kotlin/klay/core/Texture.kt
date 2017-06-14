package klay.core

import klay.core.GL20.Companion.GL_LINEAR
import klay.core.GL20.Companion.GL_TEXTURE_2D
import pythagoras.f.AffineTransform
import pythagoras.f.IRectangle
import react.Closeable
import react.Slot
import react.UnitSlot

/**
 * A handle to an OpenGL texture. A texture is also a [Tile] which contains the entire
 * texture, which allows rendering methods to operate uniformly on tiles.
 */
class Texture(// needed to access GL20 and to queue our destruction on finalize
        private val gfx: Graphics,
        /** The GL texture handle.  */
        val id: Int,
        /** This texture's configuration.  */
        val config: Texture.Config,
        /** The width of this texture in pixels.  */
        val pixelWidth: Int,
        /** The height of this texture in pixels.  */
        val pixelHeight: Int,
        /** The scale factor used by this texture.  */
        val scale: Scale,
        /** The width of this texture in display units.  */
        val displayWidth: Float,
        /** The height of this texture in display units.  */
        val displayHeight: Float) : Tile(), Closeable {

    /** Used to configure texture at creation time.  */
    class Config(
            /** Whether or not texture's lifecycle is automatically managed via reference counting. If the
             * texture will be used in an `ImageLayer`, it should be reference counted unless you
             * are doing something special. Otherwise you can decide whether you want to use the reference
             * counting mechanism or not.  */
            val managed: Boolean,
            /** Whether texture is configured to repeat in this direction.  */
            val repeatX: Boolean, val repeatY: Boolean,
            /** The filter to use when this texture is scaled: `GL_LINEAR` or `GL_NEAREST`.  */
            val minFilter: Int, val magFilter: Int,
            /** Whether texture has mipmaps generated.  */
            val mipmaps: Boolean) {

        /** Returns a copy of this config with `repeatX`, `repeatY` set as specified.  */
        fun repeat(repeatX: Boolean, repeatY: Boolean): Config {
            return Config(managed, repeatX, repeatY, minFilter, magFilter, mipmaps)
        }

        /** Returns `sourceWidth` rounded up to a POT if necessary.  */
        fun toTexWidth(sourceWidth: Int): Int {
            return if (repeatX || mipmaps) nextPOT(sourceWidth) else sourceWidth
        }

        /** Returns `sourceHeight` rounded up to a POT if necessary.  */
        fun toTexHeight(sourceHeight: Int): Int {
            return if (repeatY || mipmaps) nextPOT(sourceHeight) else sourceHeight
        }

        override fun toString(): String {
            val repstr = (if (repeatX) "x" else "") + if (repeatY) "y" else ""
            return "[managed=" + managed + ", repeat=" + repstr +
                    ", filter=" + minFilter + "/" + magFilter + ", mipmaps=" + mipmaps + "]"
        }

        companion object {

            /** Default managed texture configuration: managed, no mipmaps, no repat, linear filters.  */
            var DEFAULT = Config(true, false, false, GL_LINEAR, GL_LINEAR, false)
            /** Default unmanaged texture configuration: unmanaged, no mipmaps, no repat, linear filters.  */
            var UNMANAGED = Config(false, false, false, GL_LINEAR, GL_LINEAR, false)
        }
    }

    private var refs: Int = 0
    private var disposed: Boolean = false

    /** Increments this texture's reference count. NOOP unless [Config.managed].  */
    fun reference() {
        if (config.managed) refs++
    }

    /** Decrements this texture's reference count. If the reference count of a managed texture goes
     * to zero, the texture is disposed (and is no longer usable).  */
    fun release() {
        if (config.managed) {
            assert(refs > 0) { "Released a texture with no references!" }
            if (--refs == 0) close()
        }
    }

    /** Uploads `image` to this texture's GPU memory. `image` must have the exact same
     * size as this texture and must be fully loaded. This is generally useful for updating a
     * texture which was created from a canvas when the canvas has been changed.  */
    fun update(image: Image) {
        // if we're a repeating texture (or we want mipmaps) and this image is non-POT on the relevant
        // axes, we need to scale it before we upload it; we'll just do this on the CPU since it feels
        // like creating a second texture, a frame buffer to render into it, sending a GPU batch and
        // doing all the blah blah blah is going to be more expensive overall
        if (config.repeatX || config.repeatY || config.mipmaps) {
            val pixWidth = image.pixelWidth
            val pixHeight = image.pixelHeight
            val potWidth = config.toTexWidth(pixWidth)
            val potHeight = config.toTexWidth(pixHeight)
            if (potWidth != pixWidth || potHeight != pixHeight) {
                val scaled = gfx.createCanvasImpl(Scale.ONE, potWidth, potHeight)
                scaled.draw(image, 0f, 0f, potWidth.toFloat(), potHeight.toFloat())
                scaled.image.upload(gfx, this)
                scaled.close()
            } else
                image.upload(gfx, this) // fast path, woo!
        } else
            image.upload(gfx, this) // fast path, woo!
        if (config.mipmaps) gfx.gl.glGenerateMipmap(GL_TEXTURE_2D)
    }

    /**
     * Returns an instance that can be used to render a sub-region of this texture.
     */
    fun tile(region: IRectangle): Tile {
        return tile(region.x, region.y, region.width, region.height)
    }

    /**
     * Returns an instance that can be used to render a sub-region of this texture.
     */
    fun tile(x: Float, y: Float, width: Float, height: Float): Tile {
        val tileX = x
        val tileY = y
        val tileWidth = width
        val tileHeight = height
        return object : Tile() {
            override val texture: Texture = this@Texture

            override val width: Float = tileWidth

            override val height: Float = tileHeight

            override val sx: Float = tileX / displayWidth

            override val sy: Float = tileY / displayHeight

            override val tx: Float = (tileX + tileWidth) / displayHeight

            override val ty: Float = (tileY + tileWidth) / displayHeight

            override fun addToBatch(batch: QuadBatch, tint: Int, tx: AffineTransform,
                                    x: Float, y: Float, width: Float, height: Float) {
                batch.addQuad(texture, tint, tx, x, y, width, height, tileX, tileY, tileWidth, tileHeight)
            }

            override fun addToBatch(batch: QuadBatch, tint: Int, tx: AffineTransform,
                                    dx: Float, dy: Float, dw: Float, dh: Float,
                                    sx: Float, sy: Float, sw: Float, sh: Float) {
                batch.addQuad(texture, tint, tx, dx, dy, dw, dh, tileX + sx, tileY + sy, sw, sh)
            }
        }
    }

    /** Returns whether this texture is been disposed.  */
    fun disposed(): Boolean {
        return disposed
    }

    /** Returns a [Slot] that will dispose this texture when triggered.

     *
     * This is useful when you want to manually bind the lifecycle of an unmanaged texture to the
     * lifecycle of a layer. Simply `layer.onDisposed(texture.disposeSlot())`.
     */
    fun disposeSlot(): UnitSlot {
        return { close() }
    }

    override val texture: Texture = this

    override val width: Float = displayWidth

    override val height: Float = displayHeight

    override val sx: Float = 0f

    override val sy: Float = 0f

    override val tx: Float = 1f

    override val ty: Float = 1f

    override fun addToBatch(batch: QuadBatch, tint: Int, tx: AffineTransform,
                            x: Float, y: Float, width: Float, height: Float) {
        batch.addQuad(this, tint, tx, x, y, width, height)
    }

    override fun addToBatch(batch: QuadBatch, tint: Int, tx: AffineTransform,
                            dx: Float, dy: Float, dw: Float, dh: Float,
                            sx: Float, sy: Float, sw: Float, sh: Float) {
        batch.addQuad(this, tint, tx, dx, dy, dw, dh, sx, sy, sw, sh)
    }

    /** Deletes this texture's GPU resources and renders it unusable.  */
    override fun close() {
        if (!disposed) {
            disposed = true
            gfx.gl.glDeleteTexture(id)
        }
    }

    override fun toString(): String {
        return "Texture[id=" + id + ", psize=" + pixelWidth + "x" + pixelHeight +
                ", dsize=" + displayWidth + "x" + displayHeight + " @ " + scale + ", config=" + config + "]"
    }

    /**
     * Java finalizer, see [Kotlin documentation](https://kotlinlang.org/docs/reference/java-interop.html#finalize)
     */
    @Suppress("unused", "ProtectedInFinal")
    protected fun finalize() {
        // if we're not yet disposed, queue ourselves up to be disposed on the next frame tick
        if (!disposed) gfx.queueForDispose(this)
    }

    companion object {

        /**
         * Returns next largest power of two, or `value` if `value` is already a POT. Note:
         * this is limited to values less than `0x10000`.
         */
        fun nextPOT(value: Int): Int {
            assert(value < 0x10000)
            var bit = 0x8000
            var highest = -1
            var count = 0
            var ii = 15
            while (ii >= 0) {
                if (value and bit == 0) {
                    ii--
                    bit = bit shr 1
                    continue
                }
                count++
                if (highest == -1) highest = ii
                ii--
                bit = bit shr 1
            }
            return if (count > 1) 1 shl highest + 1 else value
        }
    }
}
