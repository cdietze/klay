package klay.js

import klay.core.GL20
import klay.core.Graphics
import klay.core.Platform
import org.khronos.webgl.WebGLRenderingContext
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLDivElement
import kotlin.browser.document
import kotlin.browser.window

class JsPlatform : Platform() {

    private val webGL: WebGLRenderingContext

    val width = 300
    val height = 300

    init {
        println("Initializing JsPlatform")

        val root: HTMLDivElement = document.getElementById("klay-root") as HTMLDivElement? ?: document.body!!.appendChild(document.createElement("div")) as HTMLDivElement

        val canvas = root.appendChild(document.createElement("canvas")) as HTMLCanvasElement

        canvas.width = width
        canvas.height = height

        webGL = canvas.getContext("webgl") as WebGLRenderingContext
        webGL.clearColor(0f, 0f, 0f, 1f)

    }

    override val graphics: Graphics = object : Graphics {

        override val gl: GL20 get() = TODO("not implemented")
//        override val gl = TODO()
//                object : GL20() {
//            override fun glClearColor(red: Float, green: Float, blue: Float, alpha: Float) {
//                webGL.clearColor(red, green, blue, alpha)
//            }
//        }
    }

    fun run() {
        fun animate() {
            frameSignal.emit(this)
            webGL.viewport(0, 0, width, height)
            webGL.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
            webGL.flush()
            window.requestAnimationFrame { animate() }
        }
        animate()
    }
}