package klay.js

import org.khronos.webgl.WebGLRenderingContext
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLDivElement
import pythagoras.f.Point
import kotlin.browser.document
import kotlin.browser.window

class JsPlatform {

    private var webGL: WebGLRenderingContext? = null

    val width = 300
    val height = 300

    init {
        println("Initializing JsPlatform")
        println("p1: ${Point(1f, 1.234567f)}, p2: ${Point(2f, 1.2000000f)}")

        val root: HTMLDivElement = document.getElementById("klay-root") as HTMLDivElement? ?: document.body!!.appendChild(document.createElement("div")) as HTMLDivElement

        val canvas = root.appendChild(document.createElement("canvas")) as HTMLCanvasElement

        canvas.width = width
        canvas.height = height

        webGL = canvas.getContext("webgl") as WebGLRenderingContext
        webGL!!.clearColor(0f, 0f, 0f, 1f)

    }

    fun run() {
        fun animate() {
            webGL!!.viewport(0, 0, width, height)
            webGL!!.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
            webGL!!.flush()
            window.requestAnimationFrame { animate() }
        }
        animate()
    }
}