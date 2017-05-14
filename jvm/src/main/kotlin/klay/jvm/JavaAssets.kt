package klay.jvm

import klay.core.*
import klay.core.buffers.ByteBuffer
import pythagoras.f.MathUtil
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.*
import java.net.URL
import java.net.URLDecoder
import java.util.*
import javax.imageio.ImageIO
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem

/**
 * Loads Java assets via the classpath.
 */
class JavaAssets
/**
 * Creates a new java assets.
 */
(private val plat: JavaPlatform) : Assets(plat.exec()) {
    private var directories = arrayOf<File>()

    /**
     * Returns the currently configured path prefix. Note that this value will always have a trailing
     * slash.
     */
    /**
     * Configures the prefix prepended to asset paths before fetching them from the classpath. For
     * example, if your assets are in `src/main/java/com/mygame/assets` (or in `src/main/resources/com/mygame/assets`), you can pass `com/mygame/assets` to this method
     * and then load your assets without prefixing their path with that value every time. The value
     * supplied to this method should not contain leading or trailing slashes. Note that this prefix
     * should always use '/' as a path separator as it is used to construct URLs, not filesystem
     * paths.
     *
     * NOTE: the path prefix is not used when searching extra directories
     */
    var pathPrefix = "assets/"
        set(prefix) {
            if (prefix.startsWith("/") || prefix.endsWith("/")) {
                throw IllegalArgumentException("Prefix must not start or end with '/'.")
            }
            field = if (prefix.length == 0) prefix else prefix + "/"
        }
    private var assetScale: Scale? = null

    /**
     * Adds the given directory to the search path for resources.
     *
     * TODO: remove? get?
     */
    fun addDirectory(dir: File) {
        val ndirs = Array<File>(directories.size, { directories[it] })
        System.arraycopy(directories, 0, ndirs, 0, directories.size)
        ndirs[ndirs.size - 1] = dir
        directories = ndirs
    }

    /**
     * Configures the default scale to use for assets. This allows one to specify an intermediate
     * graphics scale (like 1.5) and scale the 2x imagery down to 1.5x instead of scaling the 1.5x
     * imagery up (or displaying nothing at all).
     */
    fun setAssetScale(scaleFactor: Float) {
        this.assetScale = Scale(scaleFactor)
    }

    /**
     * Loads a Java font from `path`. Currently only TrueType (`.ttf`) fonts are
     * supported.

     * @param path the path to the font resource (relative to the asset manager's path prefix).
     * *
     * @throws Exception if an error occurs loading or decoding the font.
     */
    @Throws(Exception::class)
    fun getFont(path: String): Font {
        return requireResource(path).createFont()
    }

    override fun getRemoteImage(url: String, width: Int, height: Int): Image {
        val image = JavaImage(plat, true, width, height, url)
        exec.invokeAsync(Runnable {
            try {
                val bmp = ImageIO.read(URL(url))
                image.succeed(ImageImpl.Data(Scale.ONE, bmp, bmp.width, bmp.height))
            } catch (error: Exception) {
                image.fail(error)
            }
        })
        return image
    }

    override fun getSound(path: String): Sound {
        return getSound(path, false)
    }

    override fun getMusic(path: String): Sound {
        return getSound(path, true)
    }

    @Throws(Exception::class)
    override fun getTextSync(path: String): String {
        return requireResource(path).readString()
    }

    @Throws(Exception::class)
    override fun getBytesSync(path: String): ByteBuffer {
        return requireResource(path).readBytes()
    }

    protected fun getSound(path: String, music: Boolean): Sound {
        var err: Exception? = null
        for (suff in SUFFIXES) {
            val soundPath = path + suff
            try {
                return plat.audio.createSound(requireResource(soundPath), music)
            } catch (e: Exception) {
                err = e // note the error, and loop through and try the next format
            }

        }
        plat.log().warn("Sound load error $path: $err")
        return Sound.Error(err!!)
    }

    /**
     * Attempts to locate the resource at the given path, and returns a wrapper which allows its data
     * to be efficiently read.

     *
     * First, the path prefix is prepended (see [.setPathPrefix]) and the the class
     * loader checked. If not found, then the extra directories, if any, are checked, in order. If
     * the file is not found in any of the extra directories either, then an exception is thrown.
     */
    @Throws(IOException::class)
    protected fun requireResource(path: String): Resource {
        val url = this::class.java.classLoader.getResource(this.pathPrefix + path)
        if (url != null) {
            return if (url!!.getProtocol() == "file")
                FileResource(File(URLDecoder.decode(url!!.getPath(), "UTF-8")))
            else
                URLResource(url)
        }
        for (dir in directories) {
            val f = File(dir, path).canonicalFile
            if (f.exists()) {
                return FileResource(f)
            }
        }
        throw FileNotFoundException(path)
    }

    protected fun scaleImage(image: BufferedImage, viewImageRatio: Float): BufferedImage {
        val swidth = MathUtil.iceil(viewImageRatio * image.width)
        val sheight = MathUtil.iceil(viewImageRatio * image.height)
        val scaled = BufferedImage(swidth, sheight, BufferedImage.TYPE_INT_ARGB_PRE)
        val gfx = scaled.createGraphics()
        gfx.drawImage(image.getScaledInstance(swidth, sheight, java.awt.Image.SCALE_SMOOTH), 0, 0, null)
        gfx.dispose()
        return scaled
    }

    protected fun assetScale(): Scale {
        return if (assetScale != null) assetScale!! else plat.graphics.scale()
    }

    abstract class Resource {
        @Throws(IOException::class)
        abstract fun readImage(): BufferedImage

        @Throws(IOException::class)
        abstract fun openStream(): InputStream

        @Throws(Exception::class)
        open fun openAudioStream(): AudioInputStream {
            return AudioSystem.getAudioInputStream(openStream())
        }

        @Throws(Exception::class)
        open fun createFont(): Font {
            return Font.createFont(Font.TRUETYPE_FONT, openStream())
        }

        @Throws(IOException::class)
        open fun readBytes(): ByteBuffer {
            return JvmByteBuffer(java.nio.ByteBuffer.wrap(toByteArray(openStream())))
        }

        @Throws(Exception::class)
        fun readString(): String {
            return String(toByteArray(openStream()))
        }
    }

    protected class URLResource(val url: URL) : Resource() {
        @Throws(IOException::class)
        override fun openStream(): InputStream {
            return url.openStream()
        }

        @Throws(IOException::class)
        override fun readImage(): BufferedImage {
            return ImageIO.read(url)
        }
    }

    protected class FileResource(val file: File) : Resource() {
        @Throws(IOException::class)
        override fun openStream(): FileInputStream {
            return FileInputStream(file)
        }

        @Throws(IOException::class)
        override fun readImage(): BufferedImage {
            return ImageIO.read(file)
        }

        @Throws(Exception::class)
        override fun openAudioStream(): AudioInputStream {
            return AudioSystem.getAudioInputStream(file)
        }

        @Throws(Exception::class)
        override fun createFont(): Font {
            return Font.createFont(Font.TRUETYPE_FONT, file)
        }

        @Throws(IOException::class)
        override fun readBytes(): ByteBuffer {
            openStream().use { `in` ->
                `in`.channel.use { fc ->
                    val buf = java.nio.ByteBuffer.allocateDirect(fc.size().toInt()) // no >2GB files
                    fc.read(buf)
                    return JvmByteBuffer(buf)
                }
            }
        }
    }

    @Throws(Exception::class)
    override fun load(path: String): ImageImpl.Data {
        var error: Exception? = null
        for (rsrc in assetScale().getScaledResources(path)) {
            try {
                var image = requireResource(rsrc.path).readImage()
                // if image is at a higher scale factor than the view, scale to the view display factor
                val viewScale = plat.graphics.scale()
                var imageScale = rsrc.scale
                val viewImageRatio = viewScale.factor / imageScale.factor
                if (viewImageRatio < 1) {
                    image = scaleImage(image, viewImageRatio)
                    imageScale = viewScale
                }
                if (plat.config.convertImagesOnLoad) {
                    val convertedImage = JavaGraphics.convertImage(image)
                    if (convertedImage !== image) {
                        plat.log().debug("Converted image: " + path + " [type=" + image.type + "]")
                        image = convertedImage
                    }
                }
                return ImageImpl.Data(imageScale, image, image.width, image.height)
            } catch (fnfe: FileNotFoundException) {
                error = fnfe // keep going, checking for lower resolution images
            }

        }
        plat.log().warn("Could not load image: $path [error=$error]")
        throw if (error != null) error else FileNotFoundException(path)
    }

    override fun createImage(async: Boolean, rwid: Int, rhei: Int, source: String): ImageImpl {
        return JavaImage(plat, async, rwid, rhei, source)
    }

    companion object {

        @Throws(IOException::class)
        internal fun toByteArray(`in`: InputStream): ByteArray {
            try {
                var buffer = ByteArray(512)
                var size = 0
                while (true) {
                    val read = `in`.read(buffer, size, buffer.size - size)
                    if (read <= 0) break
                    size += read
                    if (size == buffer.size) buffer = Arrays.copyOf(buffer, size * 2)
                }
                // trim the zeros from the end of the buffer
                if (size < buffer.size) {
                    buffer = Arrays.copyOf(buffer, size)
                }
                return buffer
            } finally {
                `in`.close()
            }
        }

        protected val SUFFIXES = arrayOf(".wav", ".mp3")
    }
}
