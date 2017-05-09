package klay.jvm

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.GL_FALSE

/**
 * Implements the Klay platform for Java, based on LWJGL and GLFW.

 * Due to the way LWJGL works, a game must create the platform instance, then perform any of its
 * own initialization that requires access to GL resources, and then call [.start] to start
 * the game loop. The [.start] call does not return until the game exits.
 */
class LWJGLPlatform(config: Config) : JavaPlatform(config) {

    // we have to keep strong references to GLFW callbacks
    private val errorCallback: GLFWErrorCallback

    override val graphics: GLFWGraphics
    override val input: GLFWInput

    /** The handle on our GLFW window; also used by GLFWInput.  */
    private val window: Long

    init {
        // on the Mac we have to force AWT into headless mode to avoid conflicts with GLFW
        if (needsHeadless()) {
            System.setProperty("java.awt.headless", "true")
        }

        errorCallback = object : GLFWErrorCallback() {
            override fun invoke(error: Int, description: Long) {
                log().error("GL Error (" + error + "):" + GLFWErrorCallback.getDescription(description))
            }
        }
        glfwSetErrorCallback(errorCallback)
        if (!glfwInit()) throw RuntimeException("Failed to init GLFW.")

        var monitor = glfwGetPrimaryMonitor()
        val vidMode = glfwGetVideoMode(monitor)

        var width = config.width
        var height = config.height
        if (config.fullscreen) {
            width = vidMode.width()
            height = vidMode.height()
        } else {
            monitor = 0 // monitor == 0 means non-fullscreen window
        }

        // NOTE: it's easier to co-exist with GSLES2 if we leave the GLContext in "old and busted"
        // mode; so all the GL3.2 "new hotness" is commented out
        glfwDefaultWindowHints()
        // glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        // glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        // glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, 1);
        // glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE)
        glfwWindowHint(GLFW_RESIZABLE, GL_FALSE)
        window = glfwCreateWindow(width, height, config.appName, monitor, 0)
        if (window == 0L) throw RuntimeException("Failed to create window; see error log.")

        graphics = GLFWGraphics(this, window)
        input = GLFWInput(this, window)

        glfwSetWindowPos(window, (vidMode.width() - width) / 2, (vidMode.height() - height) / 2)
        glfwMakeContextCurrent(window)
        glfwSwapInterval(1)
        graphics.setSize(config.width, config.height, config.fullscreen)
        glfwShowWindow(window)

        GL.createCapabilities()
        // IntBuffer vao = BufferUtils.createIntBuffer(1);
        // GL30.glGenVertexArrays(vao);
        // GL30.glBindVertexArray(vao.get(0));
    }

    internal fun needsHeadless(): Boolean {
        return System.getProperty("os.name") == "Mac OS X"
    }

    override fun loop() {
        var wasActive = glfwGetWindowAttrib(window, GLFW_VISIBLE) > 0
        while (!glfwWindowShouldClose(window)) {
            // notify the app if lose or regain focus (treat said as pause/resume)
            val newActive = glfwGetWindowAttrib(window, GLFW_VISIBLE) > 0
            if (wasActive != newActive) {
                dispatchEvent(lifecycle, if (wasActive) Lifecycle.PAUSE else Lifecycle.RESUME)
                wasActive = newActive
            }
            // process frame, if we don't need to provide true pausing
            if (newActive || !config.truePause) {
                processFrame()
            }
            // sleep until it's time for the next frame
            glfwSwapBuffers(window)
        }
        input.shutdown()
        graphics.shutdown()
        errorCallback.close()
        glfwDestroyWindow(window)
        glfwTerminate()
    }
}
