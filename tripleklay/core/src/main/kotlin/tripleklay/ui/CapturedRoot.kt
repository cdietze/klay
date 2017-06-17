package tripleklay.ui

import klay.core.QuadBatch
import klay.core.Texture
import klay.core.TextureSurface
import klay.scene.ImageLayer
import klay.scene.Layer
import pythagoras.f.Dimension
import pythagoras.f.Point
import react.Closeable
import react.Slot
import react.Value
import react.ValueView

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
(iface: Interface, layout: Layout, sheet: Stylesheet, protected val _defaultBatch: QuadBatch) : Root(iface, layout, sheet) {

    /**
     * Gets the texture into which the root is rendered. This may be null if no validation has yet
     * occurred and may change value when the root's size changes.
     */
    fun texture(): ValueView<Texture> {
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
        if (old == null || old!!.displayWidth != width || old!!.displayHeight != height) {
            _texture.update(iface.plat.graphics().createTexture(width, height, textureConfig()))
        }
        return this
    }

    public override fun layout() {
        super.layout()
        val texture = _texture.get()
        val surf = TextureSurface(iface.plat.graphics(), _defaultBatch, texture)
        surf.begin().clear()
        layer.paint(surf)
        surf.end().close()
    }

    /**
     * Returns the configuration to use when creating our backing texture.
     */
    protected fun textureConfig(): Texture.Config {
        return Texture.Config.DEFAULT
    }

    /**
     * Wraps this captured root in a Widget, using the root's image for size computation and
     * displaying the root's image on its layer.
     */
    protected inner class Embedded : Widget<Embedded>() {
        init {
            layer.setHitTester(object : Layer.HitTester {
                override fun hitTest(layer: Layer, point: Point): Layer? {
                    return this@CapturedRoot.layer.hitTest(point)
                }
            })
            layer.setInteractive(true)
        }

        protected override val styleClass: Class<*>
            get() = Embedded::class.java

        override fun createLayoutData(hintX: Float, hintY: Float): Element.LayoutData {
            return object : Element.LayoutData() {
                override fun computeSize(hintX: Float, hintY: Float): Dimension {
                    val tex = _texture.get()
                    return if (tex == null)
                        Dimension(0f, 0f)
                    else
                        Dimension(
                                tex!!.displayWidth, tex!!.displayHeight)
                }
            }
        }

        override fun wasAdded() {
            super.wasAdded()
            // update our layer when the texture is regenerated
            _conn = _texture.connectNotify(object : Slot<Texture>() {
                fun onEmit(tex: Texture) {
                    update(tex)
                    invalidate()
                }
            })
        }

        override fun wasRemoved() {
            super.wasRemoved()
            update(null)
            _conn = Closeable.Util.close(_conn)
        }

        protected fun update(tex: Texture?) {
            if (tex == null) {
                // we should never be going back to null but handle it anyway
                if (_ilayer != null) _ilayer!!.close()
                _ilayer = null
                return
            }
            if (_ilayer == null) layer.add(_ilayer = ImageLayer())
            _ilayer!!.setTile(tex)
        }

        /** The captured root image layer, if set.  */
        protected var _ilayer: ImageLayer? = null

        /** The connection to the captured root's image, or null if we're not added.  */
        protected var _conn = Closeable.Util.NOOP
    }

    /** The texure to with the layer is rendered.  */
    protected var _texture = Value.create(null)
}
