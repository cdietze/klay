package klay.jvm

import klay.core.Scale
import org.lwjgl.BufferUtils
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWFramebufferSizeCallback
import org.lwjgl.system.MemoryUtil
import pythagoras.f.Dimension
import pythagoras.f.IDimension

class GLFWGraphics(override val plat: JavaPlatform, private val window: Long) : LWJGLGraphics(plat) {

    private val fbSizeCallback = object : GLFWFramebufferSizeCallback() {
        override fun invoke(window: Long, width: Int, height: Int) {
            viewportAndScaleChanged(width, height)
        }
    }

    private val screenSize = Dimension()

    init {
        glfwSetFramebufferSizeCallback(window, fbSizeCallback)
    }

    internal fun shutdown() {
        fbSizeCallback.close()
    }

    override fun setTitle(title: String) {
        if (window != 0L) glfwSetWindowTitle(window, title)
    }

    override fun setSize(width: Int, height: Int, fullscreen: Boolean) {
        var _width = width
        var _height = height
        if (plat.config.fullscreen !== fullscreen) {
            plat.log().warn("fullscreen cannot be changed via setSize, use config.fullscreen instead")
            return
        }
        val vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor())
        if (_width > vidMode.width()) {
            plat.log().debug("Capping window width at desktop width: " + _width + " -> " +
                    vidMode.width())
            _width = vidMode.width()
        }
        if (_height > vidMode.height()) {
            plat.log().debug("Capping window height at desktop height: " + _height + " -> " +
                    vidMode.height())
            _height = vidMode.height()
        }
        glfwSetWindowSize(window, _width, _height)
        // plat.log().info("setSize: " + width + "x" + height);
        viewSizeM.setSize(_width.toFloat(), _height.toFloat())

        val fbSize = BufferUtils.createIntBuffer(2)
        val addr = MemoryUtil.memAddress(fbSize)
        nglfwGetFramebufferSize(window, addr, addr + 4)
        viewportAndScaleChanged(fbSize.get(0), fbSize.get(1))
    }

    override fun screenSize(): IDimension {
        val vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor())
        screenSize.width = vidMode.width().toFloat()
        screenSize.height = vidMode.height().toFloat()
        return screenSize
    }

    private fun viewportAndScaleChanged(fbWidth: Int, fbHeight: Int) {
        val scale = fbWidth / viewSizeM.width
        // plat.log().info("viewportAndScaleChanged: " + fbWidth + "x" + fbHeight + "@" + scale);
        if (scale != scale().factor) scaleChanged(Scale(scale))
        viewportChanged(fbWidth, fbHeight)
    }
}
