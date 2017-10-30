package klay.core

/**
 * A batch manages the delivery of groups of drawing calls to the GPU. It is usually a combination
 * of a [GLProgram] and one or more buffers.
 */
abstract class GLBatch {

    private var begun: Boolean = false // for great sanity checking

    /**
     * Must be called before this batch is used to accumulate and send drawing commands.

     * @param flip whether or not to flip the y-axis. This is generally true when rendering to the
     * * default frame buffer (the screen), and false when rendering to textures.
     */
    open fun begin(fbufWidth: Float, fbufHeight: Float, flip: Boolean) {
        if (begun) throw IllegalStateException(this::class.simpleName + " mismatched begin()")
        begun = true
    }

    /**
     * Sends any accumulated drawing calls to the GPU. Depending on the nature of the batch, this may
     * be necessary before certain state changes (like switching to a new texture). This should be a
     * NOOP if there's nothing to flush.
     */
    open fun flush() {
        if (!begun)
            throw IllegalStateException(
                    this::class.simpleName + " flush() without begin()")
    }

    /**
     * Must be called when one is done using this batch to accumulate and send drawing commands. The
     * default implementation calls [.flush] and marks this batch as inactive.
     */
    open fun end() {
        if (!begun) throw IllegalStateException(this::class.simpleName + " mismatched end()")
        try {
            flush()
        } finally {
            begun = false
        }
    }

    /**
     * Releases any GPU resources retained by this batch. This should be called when the batch will
     * never again be used.
     */
    open fun close() {
        if (begun)
            throw IllegalStateException(
                    this::class.simpleName + " close() without end()")
    }
}
