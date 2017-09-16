package klay.jvm

import klay.core.*
import klay.core.json.JsonImpl
import java.awt.Desktop
import java.net.URI
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Implements the base Java platform which is then shared by LWJGL, LWJGL+SWT, and JOGL
 * implementations.
 */
abstract class JavaPlatform(val config: JavaPlatform.Config) : Platform() {

    /** Defines JavaPlatform configurable parameters.  */
    class Config {

        /** Dictates the name of the temporary file used by [JavaStorage]. Configure this if you
         * want to run multiple sessions without overwriting one another's storage.  */
        var storageFileName = "klay"

        /** The width of the window, in pixels.  */
        var width = 640

        /** The height of the window, in pixels.  */
        var height = 480

        /** Whether or not to run the game in fullscreen mode. *Note:* this is not well tested,
         * so you may discover issues. Consider yourself warned.  */
        var fullscreen: Boolean = false

        /** If set, emulates Touch and disables Mouse. For testing.  */
        var emulateTouch: Boolean = false

        /** If {link #emulateTouch} is set, sets the pivot for a two-finger touch when pressed.  */
        var pivotKey = Key.F11

        /** If set, toggles the activation mode when pressed. This is for emulating the active
         * state found in `IOSGameView`.  */
        var activationKey: Key? = null

        /** If set, converts images into a format for fast GPU uploads when initially loaded versus
         * doing it on demand when displayed. Assuming asynchronous image loads, this keeps that effort
         * off the main thread so it doesn't cause slow frames.
         */
        var convertImagesOnLoad = true

        /** If supported by the backend and platform, configures the application's name and initial
         * window title. Currently only supported for SWT backend.  */
        var appName = "Game"

        /** Stop processing frames while the app is "inactive", to better emulate iOS.  */
        var truePause: Boolean = false
    }

    private val start = System.nanoTime()

    private lateinit var mainThread: Thread
    private var active = true

    private val pool = Executors.newFixedThreadPool(4)
    override val exec = object : Exec.Default(this) {
        override fun isMainThread() = Thread.currentThread() == mainThread

        override val isAsyncSupported: Boolean
            get() = true

        override fun invokeAsync(action: () -> Unit) {
            pool.execute(action)
        }
    }

    override val log = JavaLog()

    override val assets = JavaAssets(this)
    override val audio = JavaAudio(exec)
    override val storage = JavaStorage(log, config.storageFileName)
    override val net = JavaNet(exec)
    override val json: Json = JsonImpl()

    // TODO(cdi) re-add Headless mode. We cannot just pass null for the GL20 reference and making it a GL20?
    // seems too far-ranging for something that is actually just for testing
//    class Headless(config: Config) : JavaPlatform(config) {
//        override val graphics = object : JavaGraphics(this@Headless, null, Scale.ONE) {
//            /*ctor*/ init {
//                setSize(config.width, config.height, config.fullscreen)
//            }
//
//            override fun setSize(width: Int, height: Int, fullscreen: Boolean) {
//                viewportChanged(width, height)
//            }
//
//            fun screenSize(): IDimension {
//                return Dimension(config.width.toFloat(), config.height.toFloat())
//            }
//
//            internal override fun setTitle(title: String) {} // noop!
//            internal override fun upload(img: BufferedImage, tex: Texture) {} // noop!
//        }
//        private val input = JavaInput(this)
//        override fun graphics(): JavaGraphics {
//            return graphics
//        }
//
//        override fun input(): JavaInput {
//            return input
//        }
//
//        override fun loop() {} // noop!
//    }

    init {
        // storage = JavaStorage(log, config.storageFileName)
    }

    /** Sets the title of the window to `title`.  */
    fun setTitle(title: String) {
        graphics.setTitle(title)
    }

    /** Starts the game loop. This method will not return until the game exits.  */
    fun start() {
        if (config.activationKey != null) {
            input.keyboardEvents.connect { event: Keyboard.Event ->
                if (event is Keyboard.KeyEvent) {
                    val kevent = event
                    if (kevent.key === config.activationKey && kevent.down) {
                        toggleActivation()
                    }
                }
            }
        }

        // make a note of the main thread
        synchronized(this) {
            mainThread = Thread.currentThread()
        }

        // run the game loop
        loop()

        // let the game run any of its exit hooks
        dispatchEvent(lifecycle, Lifecycle.EXIT)

        // shutdown our thread pool
        try {
            pool.shutdown()
            pool.awaitTermination(1, TimeUnit.SECONDS)
        } catch (ie: InterruptedException) {
            // nothing to do here except go ahead and exit
        }

        // and finally stick a fork in the JVM
        System.exit(0)
    }

    override fun time(): Double {
        return System.currentTimeMillis().toDouble()
    }

    override fun type(): Type {
        return Type.JAVA
    }

    override fun tick(): Int {
        return ((System.nanoTime() - start) / 1000000L).toInt()
    }

    fun exec(): Exec {
        return exec
    }

    abstract override val graphics: JavaGraphics
    abstract override val input: JavaInput

    fun log(): Log {
        return log
    }

    override fun openURL(url: String) {
        try {
            Desktop.getDesktop().browse(URI.create(url))
        } catch (e: Exception) {
            reportError("Failed to open URL [url=$url]", e)
        }

    }

    /** Runs the game loop.  */
    protected abstract fun loop()

    protected fun processFrame() {
        input.update() // event handling
        emitFrame()
    }

    protected fun toggleActivation() {
        active = !active
    }
}
