package tripleklay.ui

import klay.core.*
import klay.scene.GroupLayer
import klay.scene.ImageLayer
import klay.scene.Layer
import pythagoras.f.Dimension
import pythagoras.f.IDimension
import pythagoras.f.MathUtil
import react.Closeable
import tripleklay.ui.bgs.BlankBackground
import tripleklay.ui.bgs.CompositeBackground
import tripleklay.ui.bgs.RoundRectBackground
import tripleklay.ui.bgs.Scale9Background
import tripleklay.ui.util.Insets

/**
 * A background is responsible for rendering a border and a fill. It is used in conjunction with
 * groups and buttons and any other elements that need a background.
 */
abstract class Background {
    /** An instantiation of a particular background template. Backgrounds are configured as a style
     * property; elements instantiate them at specific dimensions when they are actually used. */
    abstract inner class Instance protected constructor(size: IDimension) : Closeable {

        /** The size at which this instance was prepared.  */
        val size: IDimension

        /** Returns the background that created this instance.  */
        fun owner(): Background {
            return this@Background
        }

        /** Adds this background's layers to the specified group at the specified x/y offset.
         * @param depthAdjust an adjustment to the standard depth at which backgrounds are added.
         * * This adjustment is added to the standard background depth (-10). This allows one to
         * * control the rendering order of multiple backgrounds on a single widget.
         */
        abstract fun addTo(parent: GroupLayer, x: Float, y: Float, depthAdjust: Float)

        init {
            this.size = Dimension(size)
        }
    }

    /** The insets of this background, added to get the overall Element size.  */
    var insets = Insets.ZERO

    /** The alpha transparency of this background (or null if no alpha has been configured).  */
    var alpha: Float? = null

    /** Configures insets on this background.  */
    fun insets(insets: Insets): Background {
        this.insets = insets
        return this
    }

    /** Configures uniform insets on this background.  */
    fun inset(uniformInset: Float): Background {
        insets = Insets.uniform(uniformInset)
        return this
    }

    /** Configures horizontal and vertical insets on this background.  */
    fun inset(horiz: Float, vert: Float): Background {
        insets = Insets.symmetric(horiz, vert)
        return this
    }

    /** Configures non-uniform insets on this background.  */
    fun inset(top: Float, right: Float, bottom: Float, left: Float): Background {
        insets = Insets(top, right, bottom, left)
        return this
    }

    /** Sets the left inset for this background.  */
    fun insetLeft(left: Float): Background {
        insets = insets.mutable().left(left)
        return this
    }

    /** Sets the right inset for this background.  */
    fun insetRight(right: Float): Background {
        insets = insets.mutable().right(right)
        return this
    }

    /** Sets the top inset for this background.  */
    fun insetTop(top: Float): Background {
        insets = insets.mutable().top(top)
        return this
    }

    /** Sets the bottom inset for this background.  */
    fun insetBottom(bottom: Float): Background {
        insets = insets.mutable().bottom(bottom)
        return this
    }

    /** Configures the alpha transparency of this background.  */
    fun alpha(alpha: Float): Background {
        this.alpha = alpha
        return this
    }

    /** Instantiates this background using the supplied widget size. The supplied size should
     * include the insets defined for this background.  */
    abstract fun instantiate(size: IDimension): Instance

    protected open inner class LayerInstance(size: IDimension, protected var _layer: Layer) : Instance(size) {
        init {
            if (alpha != null) _layer.setAlpha(alpha!!)
        }

        override fun addTo(parent: GroupLayer, x: Float, y: Float, depthAdjust: Float) {
            _layer.setDepth(BACKGROUND_DEPTH + depthAdjust)
            _layer.transform().translate(x, y) // adjust any existing transform
            parent.add(_layer)
        }

        override fun close() {
            _layer.close()
        }
    }

    companion object {

        /** The (highest) depth at which background layers are rendered. May range from (-11, 10].  */
        val BACKGROUND_DEPTH = -10f

        /**
         * Creates a null background (transparent).
         */
        fun blank(): Background {
            return BlankBackground()
        }

        /** Creates a solid background of the specified color.  */
        fun solid(color: Int): Background {
            return object : Background() {
                override fun instantiate(size: IDimension): Instance {
                    return LayerInstance(size, object : Layer() {
                        override fun paintImpl(surf: Surface) {
                            surf.setFillColor(color).fillRect(0f, 0f, size.width, size.height)
                        }
                    })
                }
            }
        }

        /** Creates a beveled background with the specified colors.  */
        fun beveled(bgColor: Int, ulColor: Int, brColor: Int): Background {
            return object : Background() {
                override fun instantiate(size: IDimension): Instance {
                    return LayerInstance(size, object : Layer() {
                        override fun paintImpl(surf: Surface) {
                            val width = size.width
                            val height = size.height
                            val bot = height
                            val right = width
                            surf.setFillColor(bgColor).fillRect(0f, 0f, width, height)
                            surf.setFillColor(ulColor).drawLine(0f, 0f, right, 0f, 2f).drawLine(0f, 0f, 0f, bot, 2f)
                            surf.setFillColor(brColor).drawLine(right, 0f, right, bot, 1f).drawLine(1f, bot - 1, right - 1, bot - 1, 1f).drawLine(0f, bot, right, bot, 1f).drawLine(right - 1, 1f, right - 1, bot - 1, 1f)
                        }
                    })
                }
            }
        }

        /** Creates a bordered background with the specified colors and thickness.  */
        fun bordered(bgColor: Int, color: Int, thickness: Float): Background {
            return object : Background() {
                override fun instantiate(size: IDimension): Instance {
                    return LayerInstance(size, object : Layer() {
                        override fun paintImpl(surf: Surface) {
                            val width = size.width
                            val height = size.height
                            surf.setFillColor(bgColor).fillRect(0f, 0f, width, height)
                            surf.setFillColor(color).fillRect(0f, 0f, width, thickness).fillRect(0f, 0f, thickness, height).fillRect(width - thickness, 0f, thickness, height).fillRect(0f, height - thickness, width, thickness)
                        }
                    })
                }
            }
        }

        /** Creates a round rect background with the specified color and corner radius.  */
        fun roundRect(gfx: Graphics, bgColor: Int, cornerRadius: Float): Background {
            return RoundRectBackground(gfx, bgColor, cornerRadius)
        }

        /** Creates a round rect background with the specified parameters.  */
        fun roundRect(gfx: Graphics, bgColor: Int, cornerRadius: Float,
                      borderColor: Int, borderWidth: Float): Background {
            return RoundRectBackground(gfx, bgColor, cornerRadius, borderColor, borderWidth)
        }

        /** Creates a background with the specified source.  */
        fun image(source: TileSource): Background {
            return object : Background() {
                override fun instantiate(size: IDimension): Instance {
                    val layer = ImageLayer(source)
                    layer.setSize(size.width, size.height)
                    return LayerInstance(size, layer)
                }
            }
        }

        /** Creates a centered background with the specified texture tile.  */
        fun centered(tile: Tile): Background {
            return object : Background() {
                override fun instantiate(size: IDimension): Instance {
                    val x = MathUtil.ifloor((size.width - tile.width) / 2).toFloat()
                    val y = MathUtil.ifloor((size.height - tile.height) / 2).toFloat()
                    return LayerInstance(size, object : Layer() {
                        override fun paintImpl(surf: Surface) {
                            surf.draw(tile, x, y)
                        }
                    })
                }
            }
        }

        /** Creates a cropped centered background with the specified texture tile.  */
        fun cropped(tile: Tile): Background {
            return object : Background() {
                override fun instantiate(size: IDimension): Instance {
                    val swidth = size.width
                    val sheight = size.height
                    val iwidth = tile.width
                    val iheight = tile.height
                    val cwidth = minOf(swidth, iwidth)
                    val cheight = minOf(sheight, iheight)
                    val sx = if (swidth > iwidth) 0f else (iwidth - swidth) / 2f
                    val sy = if (sheight > iheight) 0f else (iheight - sheight) / 2f
                    return LayerInstance(size, object : Layer() {
                        override fun paintImpl(surf: Surface) {
                            var dy = 0f
                            while (dy < sheight) {
                                val dheight = minOf(cheight, sheight - dy)
                                var dx = 0f
                                while (dx < swidth) {
                                    val dwidth = minOf(cwidth, swidth - dx)
                                    surf.draw(tile, dx, dy, dwidth, dheight, sx, sy, dwidth, dheight)
                                    dx += cwidth
                                }
                                dy += cheight
                            }
                        }
                    })
                }
            }
        }

        /** Creates a tiled background with the specified texture.  */
        fun tiled(tex: Texture): Background {
            return object : Background() {
                override fun instantiate(size: IDimension): Instance {
                    return LayerInstance(size, object : Layer() {
                        override fun paintImpl(surf: Surface) {
                            val width = size.width
                            val height = size.height
                            val twidth = tex.displayWidth
                            val theight = tex.displayHeight
                            var y = 0f
                            while (y < height) {
                                val h = minOf(height - y, theight)
                                var x = 0f
                                while (x < width) {
                                    val w = minOf(width - x, twidth)
                                    surf.draw(tex, x, y, w, h, 0f, 0f, w, h)
                                    x += twidth
                                }
                                y += theight
                            }
                        }
                    })
                }
            }
        }

        /** Creates a scale9 background with the specified texture tile.
         * See [Scale9Background].  */
        fun scale9(tile: Tile): Scale9Background {
            return Scale9Background(tile)
        }

        /** Creates a composite background with the specified backgrounds.
         * See [CompositeBackground].  */
        fun composite(vararg constituents: Background): Background {
            return CompositeBackground(*constituents)
        }

        /** Instantiates a background at the supplied size.  */
        fun instantiate(delegate: Background, size: IDimension): Instance {
            return delegate.instantiate(size)
        }
    }
}
