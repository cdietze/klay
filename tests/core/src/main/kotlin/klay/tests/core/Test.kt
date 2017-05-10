package klay.tests.core

import klay.core.QuadBatch
import klay.core.TextBlock
import klay.core.TriangleBatch
import klay.scene.ImageLayer
import klay.scene.Layer
import pythagoras.f.MathUtil

abstract class Test(protected val game: TestsGame, val name: String, val descrip: String) {
    protected val conns = react.Closeable.Set()

    open fun init() {}

    fun dispose() {
        conns.close()
    }

    fun usesPositionalInputs(): Boolean {
        return false
    }

    fun available(): Boolean {
        return true
    }

    @JvmOverloads protected fun addTest(lx: Float, ly: Float, layer: Layer, descrip: String, twidth: Float = layer.width()): Float {
        return addTest(lx, ly, layer, layer.width(), layer.height(), descrip, twidth)
    }

    @JvmOverloads protected fun addTest(lx: Float, ly: Float, layer: Layer, lwidth: Float, lheight: Float,
                                        descrip: String, twidth: Float = lwidth): Float {
        game.rootLayer.addAt(layer, lx + (twidth - lwidth) / 2, ly)
        return addDescrip(descrip, lx, ly + lheight + 5f, twidth)
    }

    protected fun addDescrip(descrip: String, x: Float, y: Float, width: Float): Float {
        val layer = createDescripLayer(descrip, width)
        game.rootLayer.addAt(layer, MathUtil.round(x + (width - layer.width()) / 2).toFloat(), y)
        return y + layer.height()
    }

    protected fun createDescripLayer(descrip: String, width: Float): ImageLayer {
        return ImageLayer(game.ui.wrapText(descrip, width, TextBlock.Align.CENTER))
    }

    protected fun addButton(text: String, onClick: Runnable, x: Float, y: Float): Float {
        val button = game.ui.createButton(text, onClick)
        game.rootLayer.addAt(button, x, y)
        return x + button.width() + 10
    }

    protected fun createSepiaBatch(): QuadBatch {
        return TriangleBatch(game.graphics.gl, object : TriangleBatch.Source() {
            override fun textureTint(): String {
                return super.textureTint() +
                        "  float grey = dot(textureColor.rgb, vec3(0.299, 0.587, 0.114));\n" +
                        "  textureColor = vec4(grey * vec3(1.2, 1.0, 0.8), textureColor.a);\n"
            }
        })
    }

    protected fun logFailure(message: String): react.Slot<Throwable> {
        return { cause: Throwable ->
            game.log.warn(message, cause)
        }
    }

    companion object {
        val UPDATE_RATE = 25
    }
}
