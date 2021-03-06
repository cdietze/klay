package tripleklay.ui

import euklid.f.Dimension
import euklid.f.Point
import klay.core.QuadBatch
import klay.core.Texture
import klay.core.TextureSurface
import klay.scene.ImageLayer
import klay.scene.Layer
import react.Closeable
import react.Value
import react.ValueView
import kotlin.reflect.KClass

/**
 * A root that renders everything into a single texture. Takes care of hooking into the layout
 * system and updating the image size appropriately. This trades off real-time rendering
 * performance (which is much improved because the entire UI is one texture), with memory use (a
 * backing texture is needed for the whole UI) and the expense of re-rendering the entire UI
 * whenever anything changes.
 */
class CapturedRoot
/**
 * Creates a new captured root with the given values.

 * @param defaultBatch the quad batch to use when capturing the UI scene graph. This is
 * * usually your game's default quad batch.
 */
(iface: Interface, layout: Layout, sheet: Stylesheet, private val _defaultBatch: QuadBatch) : Root(iface, layout, sheet) {

    /**
     * Gets the texture into which the root is rendered. This may be null if no validation has yet
     * occurred and may change value when the root's size changes.
     */
    fun texture(): ValueView<Texture?> {
        return _texture
    }

    /**
     * Creates a widget that will display this root in an image layer. The computed size of the
     * returned widget will be the size of this root, but the widget's layout will not affect the
     * root.
     */
    fun createWidget(): Element<*> {
        return Embedded()
    }

    override fun setSize(width: Float, height: Float): Root {
        super.setSize(width, height)
        // update the image to the new size, if it's changed
        val old = _texture.get()
        if (old == null || old.displayWidth != width || old.displayHeight != height) {
            _texture.update(iface.plat.graphics.createTexture(width, height, textureConfig()))
        }
        return this
    }

    public override fun layout() {
        super.layout()
        val texture = _texture.get()
        val surf = TextureSurface(iface.plat.graphics, _defaultBatch, texture!!)
        surf.begin().clear()
        layer.paint(surf)
        surf.end().close()
    }

    /**
     * Returns the configuration to use when creating our backing texture.
     */
    private fun textureConfig(): Texture.Config {
        return Texture.Config.DEFAULT
    }

    /**
     * Wraps this captured root in a Widget, using the root's image for size computation and
     * displaying the root's image on its layer.
     */
    private inner class Embedded : Widget<Embedded>() {
        init {
            layer.setHitTester(object : Layer.HitTester {
                override fun hitTest(layer: Layer, point: Point): Layer? {
                    return this@CapturedRoot.layer.hitTest(point)
                }
            })
            layer.setInteractive(true)
        }

        override val styleClass: KClass<*>
            get() = Embedded::class

        override fun createLayoutData(hintX: Float, hintY: Float): LayoutData {
            return object : LayoutData() {
                override fun computeSize(hintX: Float, hintY: Float): Dimension {
                    val tex = _texture.get()
                    return if (tex == null)
                        Dimension(0f, 0f)
                    else
                        Dimension(
                                tex.displayWidth, tex.displayHeight)
                }
            }
        }

        override fun wasAdded() {
            super.wasAdded()
            // update our layer when the texture is regenerated
            _conn = _texture.connectNotify({ tex: Texture? ->
                update(tex)
                invalidate()
            })
        }

        override fun wasRemoved() {
            super.wasRemoved()
            update(null)
            _conn = Closeable.Util.close(_conn)
        }

        private fun update(tex: Texture?) {
            if (tex == null) {
                // we should never be going back to null but handle it anyway
                if (_ilayer != null) _ilayer!!.close()
                _ilayer = null
                return
            }
            if (_ilayer == null) {
                _ilayer = ImageLayer()
                layer.add(_ilayer!!)
            }
            _ilayer!!.setTile(tex)
        }

        /** The captured root image layer, if set.  */
        private var _ilayer: ImageLayer? = null

        /** The connection to the captured root's image, or null if we're not added.  */
        private var _conn = Closeable.Util.NOOP
    }

    /** The texure to with the layer is rendered.  */
    private var _texture = Value<Texture?>(null)
}
