package klay.core

import klay.core.buffers.ByteBuffer
import react.RFuture

/**
 * Fetches and returns assets.
 */
abstract class Assets(protected val exec: Exec) {

    /**
     * Synchronously loads and returns an image. The calling thread will block while the image is
     * loaded from disk and decoded. When this call returns, the image's width and height will be
     * valid, and the image can be immediately converted to a texture and drawn into a canvas.

     * @param path the path to the image asset.
     * *
     * @throws UnsupportedOperationException on platforms that cannot support synchronous asset
     * * loading (HTML).
     */
    fun getImageSync(path: String): Image {
        val image = createImage(false, 0, 0, path)
        try {
            image.succeed(load(path))
        } catch (t: Throwable) {
            image.fail(t)
        }

        return image
    }

    /**
     * Asynchronously loads and returns an image. The calling thread will not block. The returned
     * image will not be immediately usable, will not report valid width and height, and cannot be
     * immediately rendered into a canvas or converted into a texture. Use [Image.state] to be
     * notified when loading succeeds or fails.

     * @param path the path to the image asset.
     */
    fun getImage(path: String): Image {
        val image = createImage(true, 0, 0, path)
        exec.invokeAsync({
            try {
                image.succeed(load(path))
            } catch (t: Throwable) {
                image.fail(t)
            }
        })
        return image
    }

    /**
     * Asynchronously loads and returns the image at the specified URL. The width and height of the
     * image will be the supplied `width` and `height` until the image is loaded.
     * *Note:* on non-HTML platforms, this spawns a new thread for each loaded image. Thus,
     * attempts to load large numbers of remote images simultaneously may result in poor performance.
     */
    @JvmOverloads open fun getRemoteImage(url: String, width: Int = 0, height: Int = 0): Image {
        val error = Exception(
                "Remote image loading not yet supported: " + url + "@" + width + "x" + height)
        val image = createImage(false, width, height, url)
        image.fail(error)
        return image
    }

    /**
     * Asynchronously loads and returns a short sound effect.

     *
     *  Note: if a request to play the sound is made before the sound is loaded, it will be noted
     * and the sound will be played when loading has completed.

     * @param path the path to the sound resource. NOTE: this should not include a file extension,
     * * PlayN will automatically add `.mp3`, (or `.caf` on iOS).
     */
    abstract fun getSound(path: String): Sound

    /**
     * Asynchronously loads and returns a music resource. On some platforms, the backend will use a
     * different implementation from [.getSound] which is better suited to the much larger size
     * of music audio data.

     *
     *  Note: if a request to play the sound is made before the sound is loaded, it will be noted
     * and the sound will be played when loading has completed.

     * @param path the path to the sound resource. NOTE: this should not include a file extension,
     * * PlayN will automatically add `.mp3`, (or `.caf` on iOS).
     */
    open fun getMusic(path: String): Sound {
        return getSound(path)
    }

    /**
     * Loads and returns a UTF-8 encoded text asset.

     * @param path the path to the text asset.
     * *
     * @throws Exception if there is an error loading the text (for example, if it does not exist).
     * *
     * @throws UnsupportedOperationException on platforms that cannot support synchronous asset
     * * loading (e.g. HTML5 and Flash).
     */
    @Throws(Exception::class)
    abstract fun getTextSync(path: String): String

    /**
     * Loads UTF-8 encoded text asynchronously. The returned state instance provides a means to
     * listen for the arrival of the text.

     * @param path the path to the text asset.
     */
    fun getText(path: String): RFuture<String> {
        val result = exec.deferredPromise<String>()
        exec.invokeAsync({
            try {
                result.succeed(getTextSync(path))
            } catch (t: Throwable) {
                result.fail(t)
            }
        })
        return result
    }

    /**
     * Loads and returns the raw bytes of the asset - useful for custom binary formatted files.

     * @param path the path to the text asset.
     * *
     * @throws Exception if there is an error loading the data (for example, if it does not exist).
     * *
     * @throws UnsupportedOperationException on platforms that cannot support synchronous asset
     * * loading (e.g. HTML5 and Flash).
     */
    @Throws(Exception::class)
    abstract fun getBytesSync(path: String): ByteBuffer

    /**
     * Loads binary data asynchronously. The returned state instance provides a means to listen for
     * the arrival of the data.

     * @param path the path to the binary asset.
     */
    fun getBytes(path: String): RFuture<ByteBuffer> {
        val result = exec.deferredPromise<ByteBuffer>()
        exec.invokeAsync({
            try {
                result.succeed(getBytesSync(path))
            } catch (t: Throwable) {
                result.fail(t)
            }
        })
        return result
    }

    /**
     * Synchronously loads image data at `path`.
     */
    @Throws(Exception::class)
    protected abstract fun load(path: String): ImageImpl.Data

    /**
     * Creates an image with the specified width and height.
     * @param async whether the image is being loaded synchronously or not. This should be passed
     * * through to the [ImageImpl] constructor.
     */
    protected abstract fun createImage(
            async: Boolean, rawWidth: Int, rawHeight: Int, source: String): ImageImpl

    companion object {

        /**
         * Normalizes the path, by removing `foo/..` pairs until the path contains no `..`s.
         * For example:
         * `foo/bar/../baz/bif/../bonk.png` becomes `foo/baz/bonk.png` and
         * `foo/bar/baz/../../bing.png` becomes `foo/bing.png`.
         */
        protected fun normalizePath(path: String): String {
            var path = path
            var pathLen: Int
            do {
                pathLen = path.length
                path = path.replace("[^/]+/\\.\\./".toRegex(), "")
            } while (path.length != pathLen)
            return path
        }
    }
}
/**
 * Asynchronously loads and returns the image at the specified URL. The width and height of the
 * image will be unset (0) until the image is loaded. *Note:* on non-HTML platforms, this
 * spawns a new thread for each loaded image. Thus, attempts to load large numbers of remote
 * images simultaneously may result in poor performance.
 */
