package tripleklay.ui

import klay.core.Canvas
import klay.core.Font
import klay.core.Sound
import klay.core.TextBlock
import tripleklay.util.EffectRenderer
import tripleklay.util.EffectRenderer.Gradient
import tripleklay.util.TextStyle

/**
 * Defines style properties for interface elements. Some style properties are inherited, such that
 * a property not specified in a leaf element will be inherited by the nearest parent for which the
 * property is specified. Other style properties are not inherited, and a default value will be
 * used in cases where a leaf element lacks a property. The documentation for each style property
 * indicates whether or not it is inherited.
 */
abstract class Style<V> protected constructor(
        /** Indicates whether or not this style property is inherited.  */
        val inherited: Boolean) {
    /** Defines element modes which can be used to modify an element's styles.  */
    enum class Mode private constructor(
            /** Whether the element is enabled in this mode.  */
            val enabled: Boolean,
            /** Whether the element is selected in this mode.  */
            val selected: Boolean) {
        DEFAULT(true, false),
        DISABLED(false, false),
        SELECTED(true, true),
        DISABLED_SELECTED(false, true)
    }

    /** Used to configure [Styles] instances. See [Styles.add].  */
    class Binding<V>(
            /** The style being configured.  */
            val style: Style<V>,
            /** The value to be bound for the style.  */
            val value: V)

    /** Defines horizontal alignment choices.  */
    enum class HAlign {
        LEFT {
            override fun offset(size: Float, extent: Float): Float {
                return 0f
            }
        },
        RIGHT {
            override fun offset(size: Float, extent: Float): Float {
                return extent - size
            }
        },
        CENTER {
            override fun offset(size: Float, extent: Float): Float {
                return (extent - size) / 2
            }
        };

        abstract fun offset(size: Float, extent: Float): Float
    }

    /** Defines vertical alignment choices.  */
    enum class VAlign {
        TOP {
            override fun offset(size: Float, extent: Float): Float {
                return 0f
            }
        },
        BOTTOM {
            override fun offset(size: Float, extent: Float): Float {
                return extent - size
            }
        },
        CENTER {
            override fun offset(size: Float, extent: Float): Float {
                return (extent - size) / 2
            }
        };

        abstract fun offset(size: Float, extent: Float): Float
    }

    /** Defines icon position choices.  */
    enum class Pos {
        LEFT, ABOVE, RIGHT, BELOW;

        /** Tests if this position is left or right.  */
        fun horizontal(): Boolean {
            return this == LEFT || this == RIGHT
        }
    }

    /** Used to create text effects.  */
    interface EffectFactory {
        /** Creates the effect renderer to be used by this factory.  */
        fun createEffectRenderer(elem: Element<*>): EffectRenderer
    }

    /** Defines supported text effects.  */
    enum class TextEffect : EffectFactory {
        /** Outlines the text in the highlight color.  */
        PIXEL_OUTLINE {
            override fun createEffectRenderer(elem: Element<*>): EffectRenderer {
                return EffectRenderer.PixelOutline(Styles.resolveStyle(elem, Style.HIGHLIGHT))
            }
        },
        /** Outlines the text in the highlight color.  */
        VECTOR_OUTLINE {
            override fun createEffectRenderer(elem: Element<*>): EffectRenderer {
                return EffectRenderer.VectorOutline(
                        Styles.resolveStyle(elem, Style.HIGHLIGHT),
                        Styles.resolveStyle(elem, Style.OUTLINE_WIDTH),
                        Styles.resolveStyle<LineCap>(elem, Style.OUTLINE_CAP),
                        Styles.resolveStyle<LineJoin>(elem, Style.OUTLINE_JOIN))
            }
        },
        /** Draws a shadow below and to the right of the text in the shadow color.  */
        SHADOW {
            override fun createEffectRenderer(elem: Element<*>): EffectRenderer {
                return EffectRenderer.Shadow(Styles.resolveStyle(elem, Style.SHADOW),
                        Styles.resolveStyle(elem, Style.SHADOW_X),
                        Styles.resolveStyle(elem, Style.SHADOW_Y))
            }
        },
        /** Draws a gradient from the font color to the gradient color.  */
        GRADIENT {
            override fun createEffectRenderer(elem: Element<*>): EffectRenderer {
                return Gradient(Styles.resolveStyle(elem, Style.GRADIENT_COLOR),
                        Styles.resolveStyle<Type>(elem, Style.GRADIENT_TYPE))
            }
        },
        /** No text effect.  */
        NONE {
            override fun createEffectRenderer(elem: Element<*>): EffectRenderer {
                return EffectRenderer.NONE
            }
        }
    }

    /** Used to provide concise HAlign style declarations.  */
    class HAlignStyle internal constructor() : Style<HAlign>(false) {
        val left = `is`(HAlign.LEFT)
        val right = `is`(HAlign.RIGHT)
        val center = `is`(HAlign.CENTER)
        override fun getDefault(elem: Element<*>): HAlign {
            return HAlign.CENTER
        }
    }

    /** Used to provide concise VAlign style declarations.  */
    class VAlignStyle internal constructor() : Style<VAlign>(false) {
        val top = `is`(VAlign.TOP)
        val bottom = `is`(VAlign.BOTTOM)
        val center = `is`(VAlign.CENTER)
        override fun getDefault(elem: Element<*>): VAlign {
            return VAlign.CENTER
        }
    }

    /** Used to provide concise Pos style declarations.  */
    class PosStyle internal constructor() : Style<Pos>(false) {
        val left = `is`(Pos.LEFT)
        val above = `is`(Pos.ABOVE)
        val right = `is`(Pos.RIGHT)
        val below = `is`(Pos.BELOW)
        override fun getDefault(elem: Element<*>): Pos {
            return Pos.LEFT
        }
    }

    /** Used to provide concise TextEffect style declarations.  */
    class TextEffectStyle internal constructor() : Style<EffectFactory>(true) {
        val pixelOutline = `is`(TextEffect.PIXEL_OUTLINE)
        val vectorOutline = `is`(TextEffect.VECTOR_OUTLINE)
        val shadow = `is`(TextEffect.SHADOW)
        val gradient = `is`(TextEffect.GRADIENT)
        val none = `is`(TextEffect.NONE)
        fun `is`(renderer: EffectRenderer): Binding<EffectFactory> {
            return `is`(object : EffectFactory {
                override fun createEffectRenderer(elem: Element<*>): EffectRenderer {
                    return renderer
                }
            })
        }

        override fun getDefault(elem: Element<*>): EffectFactory {
            return TextEffect.NONE
        }
    }

    class GradientTypeStyle internal constructor() : Style<Gradient.Type>(true) {
        val bottom: Binding<Gradient.Type> = `is`(Gradient.Type.BOTTOM)
        val top: Binding<Gradient.Type> = `is`(Gradient.Type.TOP)
        val center: Binding<Gradient.Type> = `is`(Gradient.Type.CENTER)
        override fun getDefault(elem: Element<*>): Gradient.Type {
            return Gradient.Type.BOTTOM
        }
    }

    /** A Boolean style, with convenient members for on and off bindings.  */
    class Flag private constructor(inherited: Boolean, protected val _default: Boolean?) : Style<Boolean>(inherited) {
        val off = `is`(false)
        val on = `is`(true)
        override fun getDefault(mode: Element<*>): Boolean? {
            return _default
        }
    }

    /**
     * Returns the default value for this style for the supplied element.
     */
    abstract fun getDefault(mode: Element<*>): V

    /**
     * Returns a [Binding] with this style bound to the specified value.
     */
    fun `is`(value: V): Binding<V> {
        return Binding(this, value)
    }

    companion object {

        /** The foreground color for an element. Inherited.  */
        val COLOR: Style<Int> = object : Style<Int>(true) {
            override fun getDefault(elem: Element<*>): Int? {
                return if (elem.isEnabled) 0xFF000000.toInt() else 0xFF666666.toInt()
            }
        }

        /** The highlight color for an element. Inherited.  */
        val HIGHLIGHT: Style<Int> = object : Style<Int>(true) {
            override fun getDefault(elem: Element<*>): Int? {
                return if (elem.isEnabled) 0xAAFFFFFF.toInt() else 0xAACCCCCC.toInt()
            }
        }

        /** The shadow color for an element. Inherited.  */
        val SHADOW = newStyle(true, 0x55000000)

        /** The shadow offset in pixels. Inherited.  */
        val SHADOW_X = newStyle(true, 2f)

        /** The shadow offset in pixels. Inherited.  */
        val SHADOW_Y = newStyle(true, 2f)

        /** The color of the gradient. Inherited.  */
        val GRADIENT_COLOR = newStyle(true, 0xFFC70000.toInt())

        /** The type of gradient. Inherited.  */
        val GRADIENT_TYPE = GradientTypeStyle()

        /** The stroke width of the outline, when using a vector outline.  */
        val OUTLINE_WIDTH = newStyle(true, 1f)

        /** The line cap for the outline, when using a vector outline.  */
        val OUTLINE_CAP: Style<Canvas.LineCap> = newStyle<LineCap>(true, Canvas.LineCap.ROUND)

        /** The line join for the outline, when using a vector outline.  */
        val OUTLINE_JOIN: Style<Canvas.LineJoin> = newStyle<LineJoin>(true, Canvas.LineJoin.ROUND)

        /** The horizontal alignment of an element. Not inherited.  */
        val HALIGN = HAlignStyle()

        /** The vertical alignment of an element. Not inherited.  */
        val VALIGN = VAlignStyle()

        /** The font used to render text. Inherited.  */
        val FONT = newStyle(true, Font("Helvetica", 16f))

        /** Whether or not to allow text to wrap. When text cannot wrap and does not fit into the
         * allowed space, it is truncated. Not inherited.  */
        val TEXT_WRAP = newFlag(false, false)

        /** The effect to use when rendering text, if any. Inherited.  */
        val TEXT_EFFECT = TextEffectStyle()

        /** Whether or not to underline text. Inherited.  */
        val UNDERLINE = newFlag(true, false)

        /** Whether or not to automatically shrink a text widget's font size until it fits into the
         * horizontal space it has been allotted. Cannot be used with [.TEXT_WRAP]. Not
         * inherited.  */
        val AUTO_SHRINK = newFlag(false, false)

        /** The background for an element. Not inherited.  */
        val BACKGROUND = newStyle(false, Background.blank())

        /** The position relative to the text to render an icon for labels, buttons, etc.  */
        val ICON_POS = PosStyle()

        /** The gap between the icon and text in labels, buttons, etc.  */
        val ICON_GAP = newStyle(false, 2)

        /** If true, the icon is cuddled to the text, with extra space between icon and border, if
         * false, the icon is placed next to the border with extra space between icon and label.  */
        val ICON_CUDDLE = newFlag(false, false)

        /** The effect to apply to the icon.  */
        val ICON_EFFECT = newStyle(false, IconEffect.NONE)

        /** The sound to be played when this element's action is triggered.  */
        val ACTION_SOUND = newStyle<Sound>(false, null as Sound?)

        /**
         * Creates a text style instance based on the supplied element's stylings.
         */
        fun createTextStyle(elem: Element<*>): TextStyle {
            return TextStyle(
                    Styles.resolveStyle(elem, Style.FONT),
                    Styles.resolveStyle(elem, Style.TEXT_EFFECT) !== TextEffect.PIXEL_OUTLINE,
                    Styles.resolveStyle(elem, Style.COLOR),
                    Styles.resolveStyle(elem, Style.TEXT_EFFECT).createEffectRenderer(elem),
                    Styles.resolveStyle(elem, Style.UNDERLINE))
        }

        /**
         * Creates a style identifier with the supplied properties.
         */
        fun <V> newStyle(inherited: Boolean, defaultValue: V): Style<V> {
            return object : Style<V>(inherited) {
                override fun getDefault(elem: Element<*>): V {
                    return defaultValue
                }
            }
        }

        /**
         * Creates a boolean style identifier with the supplied properties.
         */
        fun newFlag(inherited: Boolean, defaultValue: Boolean): Flag {
            return Flag(inherited, defaultValue)
        }

        fun toAlignment(align: HAlign): TextBlock.Align {
            when (align) {
                else, Style.HAlign.LEFT -> return TextBlock.Align.LEFT
                Style.HAlign.RIGHT -> return TextBlock.Align.RIGHT
                Style.HAlign.CENTER -> return TextBlock.Align.CENTER
            }
        }
    }
}
