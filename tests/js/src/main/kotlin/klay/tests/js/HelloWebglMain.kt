package klay.tests.js

import org.khronos.webgl.WebGLRenderingContext
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLDivElement
import kotlin.browser.document
import kotlin.browser.window

object HelloWebglMain {
    fun main(args: Array<String>) {
        println("hi from webgl main")

        val root: HTMLDivElement = document.getElementById("klay-root") as HTMLDivElement? ?: document.body!!.appendChild(document.createElement("div")) as HTMLDivElement

        val canvas = root.appendChild(document.createElement("canvas")) as HTMLCanvasElement

        canvas.width = 300
        canvas.height = 300

        val GL = canvas.getContext("webgl") as WebGLRenderingContext
        GL.clearColor(1f, 1f, 0f, 1f)

        fun animate() {
            GL.viewport(0, 0, canvas.width, canvas.height)
            GL.clear(WebGLRenderingContext.COLOR_BUFFER_BIT)
            GL.flush()
            window.requestAnimationFrame { animate() }
        }
        animate()
    }
}
