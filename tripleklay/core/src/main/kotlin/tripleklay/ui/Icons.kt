package tripleklay.ui

import klay.core.Surface
import klay.core.TileSource
import klay.scene.GroupLayer
import klay.scene.ImageLayer
import klay.scene.Layer
import react.RFuture

/**
 * Contains icon related utility classes and methods, mostly basic icon factories.
 */
object Icons {
    /**
     * Defers to another icon. Subclasses decide how to modify the width and height and how to use
     * the rendered layer. The base takes care of the callback. By default, returns the size and
     * layer without modification.
     */
    abstract class Aggregated
    /** Creates a new aggregated icon that defers to the given one.  */
    (
            /** Icon that is deferred to.  */
            val icon: Icon) : Icon {

        override fun width(): Float {
            return icon.width()
        }

        override fun height(): Float {
            return icon.height()
        }

        override fun render(): Layer {
            return icon.render()
        }

        override fun state(): RFuture<Icon> {
            return icon.state()
        }
    }

    /** Creates an icon using the supplied texture tile `source`.  */
    fun image(source: TileSource): Icon {
        return object : Icon {
            override fun width(): Float {
                return (if (source.isLoaded) source.tile().width else 0f).toFloat()
            }

            override fun height(): Float {
                return (if (source.isLoaded) source.tile().height else 0f).toFloat()
            }

            override fun render(): Layer {
                return ImageLayer(source)
            }

            override fun state(): RFuture<Icon> {
                return source.tileAsync().map({ this })
            }
        }
    }

    /**
     * Creates an icon that applies the given scale to the given icon.
     */
    fun scaled(icon: Icon, scale: Float): Icon {
        return object : Aggregated(icon) {
            override fun width(): Float {
                return super.width() * scale
            }

            override fun height(): Float {
                return super.height() * scale
            }

            override fun render(): Layer {
                return super.render().setScale(scale)
            }
        }
    }

    /**
     * Creates an icon that nests and offsets the given icon by the given translation.
     */
    fun offset(icon: Icon, tx: Float, ty: Float): Icon {
        return object : Aggregated(icon) {
            override fun render(): Layer {
                val layer = GroupLayer()
                layer.addAt(super.render(), tx, ty)
                return layer
            }
        }
    }

    /**
     * Creates a solid square icon of the given size.
     */
    fun solid(color: Int, size: Float): Icon {
        return object : Icon {
            override fun width(): Float {
                return size
            }

            override fun height(): Float {
                return size
            }

            override fun state(): RFuture<Icon> {
                return RFuture.success<Icon>(this)
            }

            override fun render(): Layer {
                return object : Layer() {
                    override fun paintImpl(surf: Surface) {
                        surf.setFillColor(color).fillRect(0f, 0f, size, size)
                    }
                }
            }
        }
    }
}
