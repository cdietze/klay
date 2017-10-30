package klay.core

import react.RFuture
import react.RPromise

/**
 * An implementation detail. Not part of the public API.
 */
abstract class ImageImpl : Image {

    /** Used to provide bitmap data to the abstract image once it's ready.  */
    class Data(val scale: Scale, val bitmap: Any, val pixelWidth: Int, val pixelHeight: Int)

    protected val source: String
    override var scale: Scale
    override var pixelWidth: Int = 0
        protected set
    override var pixelHeight: Int = 0
        protected set

    /** Notifies this image that its implementation bitmap is available.
     * This can be called from any thread.  */
    fun succeed(data: Data) {
        synchronized(this, {
            scale = data.scale
            pixelWidth = data.pixelWidth
            assert(pixelWidth > 0)
            pixelHeight = data.pixelHeight
            assert(pixelHeight > 0)
            setBitmap(data.bitmap)
            (state as RPromise<Image>).succeed(this) // state is a deferred promise
        })
    }

    /** Notifies this image that its implementation bitmap failed to load.
     * This can be called from any thread.  */
    fun fail(error: Throwable) {
        synchronized(this, {
            if (pixelWidth == 0) pixelWidth = 50
            if (pixelHeight == 0) pixelHeight = 50
            setBitmap(createErrorBitmap(pixelWidth, pixelHeight))
            (state as RPromise<Image>).fail(error) // state is a deferred promise
        })
    }

    protected constructor(gfx: Graphics, scale: Scale, pixelWidth: Int, pixelHeight: Int, source: String,
                          bitmap: Any) : super(gfx) {
        if (pixelWidth == 0 || pixelHeight == 0)
            throw IllegalArgumentException(
                    "Invalid size for ready image: " + pixelWidth + "x" + pixelHeight + " bitmap: " + bitmap)
        this.source = source
        this.scale = scale
        this.pixelWidth = pixelWidth
        this.pixelHeight = pixelHeight
        setBitmap(bitmap)
    }

    protected constructor(gfx: Graphics, state: RFuture<Image>, preScale: Scale,
                          preWidth: Int, preHeight: Int, source: String) : super(gfx, state) {
        this.source = source
        this.scale = preScale
        this.pixelWidth = preWidth
        this.pixelHeight = preHeight
    }

    protected constructor(plat: Platform, async: Boolean, preScale: Scale, preWidth: Int, preHeight: Int,
                          source: String) : this(plat.graphics, if (async) plat.exec.deferredPromise() else RPromise.create<Image>(),
            preScale, preWidth, preHeight, source)

    protected abstract fun setBitmap(bitmap: Any)
    protected abstract fun createErrorBitmap(pixelWidth: Int, pixelHeight: Int): Any
}
