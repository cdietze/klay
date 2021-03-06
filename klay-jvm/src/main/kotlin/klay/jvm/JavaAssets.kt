package klay.jvm

import euklid.f.MathUtil
import klay.core.*
import klay.core.buffers.ByteBuffer
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream
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
            field = if (prefix.isEmpty()) prefix else prefix + "/"
        }
    private var assetScale: Scale? = null

    /**
     * Adds the given directory to the search path for resources.
     *
     * TODO: remove? get?
     */
    fun addDirectory(dir: File) {
        val ndirs = Array(directories.size, { directories[it] })
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
    fun getFont(path: String): Font {
        return requireResource(path).createFont()
    }

    override fun getRemoteImage(url: String, width: Int, height: Int): Image {
        val image = JavaImage(plat, true, width, height, url)
        exec.invokeAsync({
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

    override fun getTextSync(path: String): String {
        return requireResource(path).readString()
    }

    override fun getBytesSync(path: String): ByteBuffer {
        return requireResource(path).readBytes()
    }

    private fun getSound(path: String, music: Boolean): Sound {
        var err: Exception? = null
        SUFFIXES
                .map { path + it }
                .forEach {
                    try {
                        return plat.audio.createSound(requireResource(it), music)
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
    private fun requireResource(path: String): Resource {
        val url = this::class.java.classLoader.getResource(this.pathPrefix + path)
        if (url != null) {
            return if (url.protocol == "file")
                FileResource(File(URLDecoder.decode(url.path, "UTF-8")))
            else
                URLResource(url)
        }
        directories
                .map { File(it, path).canonicalFile }
                .filter { it.exists() }
                .forEach { return FileResource(it) }
        throw FileNotFoundException(path)
    }

    private fun scaleImage(image: BufferedImage, viewImageRatio: Float): BufferedImage {
        val swidth = MathUtil.iceil(viewImageRatio * image.width)
        val sheight = MathUtil.iceil(viewImageRatio * image.height)
        val scaled = BufferedImage(swidth, sheight, BufferedImage.TYPE_INT_ARGB_PRE)
        val gfx = scaled.createGraphics()
        gfx.drawImage(image.getScaledInstance(swidth, sheight, java.awt.Image.SCALE_SMOOTH), 0, 0, null)
        gfx.dispose()
        return scaled
    }

    private fun assetScale(): Scale {
        return if (assetScale != null) assetScale!! else plat.graphics.scale()
    }

    abstract class Resource {
        abstract fun readImage(): BufferedImage

        abstract fun openStream(): InputStream

        open fun openAudioStream(): AudioInputStream {
            return AudioSystem.getAudioInputStream(openStream())
        }

        open fun createFont(): Font {
            return Font.createFont(Font.TRUETYPE_FONT, openStream())
        }

        open fun readBytes(): ByteBuffer {
            return JvmByteBuffer(java.nio.ByteBuffer.wrap(toByteArray(openStream())))
        }

        fun readString(): String {
            return String(toByteArray(openStream()))
        }
    }

    private class URLResource(val url: URL) : Resource() {
        override fun openStream(): InputStream {
            return url.openStream()
        }

        override fun readImage(): BufferedImage {
            return ImageIO.read(url)
        }
    }

    private class FileResource(val file: File) : Resource() {
        override fun openStream(): FileInputStream {
            return FileInputStream(file)
        }

        override fun readImage(): BufferedImage {
            return ImageIO.read(file)
        }

        override fun openAudioStream(): AudioInputStream {
            return AudioSystem.getAudioInputStream(file)
        }

        override fun createFont(): Font {
            return Font.createFont(Font.TRUETYPE_FONT, file)
        }

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

    override fun createImage(async: Boolean, rawWidth: Int, rawHeight: Int, source: String): ImageImpl {
        return JavaImage(plat, async, rawWidth, rawHeight, source)
    }

    companion object {

        internal fun toByteArray(`in`: InputStream): ByteArray {
            `in`.use { `in` ->
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
            }
        }

        private val SUFFIXES = arrayOf(".wav", ".mp3")
    }
}
