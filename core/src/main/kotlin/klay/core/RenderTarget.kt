package klay.core

import klay.core.GL20.Companion.GL_COLOR_ATTACHMENT0
import klay.core.GL20.Companion.GL_FRAMEBUFFER
import klay.core.GL20.Companion.GL_TEXTURE_2D
import react.Closeable

/**
 * Encapsulates an OpenGL render target (i.e. a framebuffer).
 * @see Graphics.defaultRenderTarget
 */
abstract class RenderTarget(
        /** A handle on our graphics services.  */
        val gfx: Graphics) : Closeable {

    /** The framebuffer id.  */
    abstract fun id(): Int

    /** The width of the framebuffer in pixels.  */
    abstract fun width(): Int

    /** The height of the framebuffer in pixels.  */
    abstract fun height(): Int

    /** The x-scale between display units and pixels for this target.  */
    abstract fun xscale(): Float

    /** The y-scale between display units and pixels for this target.  */
    abstract fun yscale(): Float

    /** Whether or not to flip the y-axis when rendering to this target. When rendering to textures
     * we do not want to flip the y-axis, but when rendering to the screen we do (so that the origin
     * is at the upper-left of the screen).  */
    abstract fun flip(): Boolean

    /** Binds the framebuffer.  */
    fun bind() {
        gfx.gl.glBindFramebuffer(GL_FRAMEBUFFER, id())
        gfx.gl.glViewport(0, 0, width(), height())
    }

    /** Deletes the framebuffer associated with this render target.  */
    override fun close() {
        if (!disposed) {
            disposed = true
            gfx.gl.glDeleteFramebuffer(id())
        }
    }

    override fun toString(): String {
        return "[id=" + id() + ", size=" + width() + "x" + height() + " @ " +
                xscale() + "x" + yscale() + ", flip=" + flip() + "]"
    }

    /**
     * Java finalizer, see [Kotlin documentation](https://kotlinlang.org/docs/reference/java-interop.html#finalize)
     */
    @Suppress("unused")
    protected fun finalize() {
        if (!disposed) gfx.queueForDispose(this)
    }

    private var disposed: Boolean = false

    companion object {

        /** Creates a render target that renders to `texture`.  */
        fun create(gfx: Graphics, tex: Texture): RenderTarget {
            val gl = gfx.gl
            val fb = gl.glGenFramebuffer()
            if (fb == 0) throw RuntimeException("Failed to gen framebuffer: " + gl.glGetError())
            gl.glBindFramebuffer(GL_FRAMEBUFFER, fb)
            gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, tex.id, 0)
            gl.checkError("RenderTarget.create")
            return object : RenderTarget(gfx) {
                override fun id(): Int {
                    return fb
                }

                override fun width(): Int {
                    return tex.pixelWidth
                }

                override fun height(): Int {
                    return tex.pixelHeight
                }

                override fun xscale(): Float {
                    return tex.pixelWidth / tex.displayWidth
                }

                override fun yscale(): Float {
                    return tex.pixelHeight / tex.displayHeight
                }

                override fun flip(): Boolean {
                    return false
                }
            }
        }
    }
}
