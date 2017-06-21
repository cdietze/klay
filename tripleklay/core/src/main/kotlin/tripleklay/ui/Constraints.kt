package tripleklay.ui

import klay.core.Graphics
import klay.core.TextLayout
import pythagoras.f.Dimension
import pythagoras.f.IDimension

/**
 * Provides various user interface constraints.
 */
object Constraints {
    /** A special layout constraint used by [TextWidget]s which adjusts only the text size of
     * the widget, leaving the remaining dimensions (icon, insets, etc.) unmodified. This is an
     * implementation detail that can be safely ignored unless you are implementing your own custom
     * text constraints.  */
    abstract class TextConstraint : Layout.Constraint() {
        /** Adds the appropriate text dimensions to the supplied size.
         * @param into the constrained size will be written into this instance.
         * *
         * @param lsize the size of the currently laid out text, may be null.
         */
        abstract fun addTextSize(into: Dimension, lsize: IDimension?)
    }

    /**
     * Returns a layout constraint that forces the widget's preferred width to the specified value.
     */
    fun fixedWidth(width: Float): Layout.Constraint {
        return object : Layout.Constraint() {
            override fun adjustHintX(hintX: Float): Float {
                return Math.min(width, hintX)
            }

            override fun adjustPreferredSize(psize: Dimension, hintX: Float, hintY: Float) {
                psize.width = width
            }
        }
    }

    /**
     * Returns a layout constraint that forces the widget's preferred height to the specified value.
     */
    fun fixedHeight(height: Float): Layout.Constraint {
        return object : Layout.Constraint() {
            override fun adjustHintY(hintY: Float): Float {
                return Math.min(height, hintY)
            }

            override fun adjustPreferredSize(psize: Dimension, hintX: Float, hintY: Float) {
                psize.height = height
            }
        }
    }

    /**
     * Returns a layout constraint that forces the widget's preferred width height to the specified
     * values.
     */
    fun fixedSize(width: Float, height: Float): Layout.Constraint {
        return object : Layout.Constraint() {
            override fun adjustHintX(hintX: Float): Float {
                return Math.min(width, hintX)
            }

            override fun adjustHintY(hintY: Float): Float {
                return Math.min(height, hintY)
            }

            override fun adjustPreferredSize(psize: Dimension, hintX: Float, hintY: Float) {
                psize.width = width
                psize.height = height
            }
        }
    }

    /**
     * Returns a layout constraint that forces the widget's preferred width to be no more than the
     * specified value.
     */
    fun maxWidth(width: Float): Layout.Constraint {
        return object : Layout.Constraint() {
            override fun adjustHintX(hintX: Float): Float {
                return Math.min(width, hintX)
            }

            override fun adjustPreferredSize(psize: Dimension, hintX: Float, hintY: Float) {
                psize.width = Math.min(psize.width, width)
            }
        }
    }

    /**
     * Returns a layout constraint that forces the widget's preferred height to be no more than the
     * specified value.
     */
    fun maxHeight(height: Float): Layout.Constraint {
        return object : Layout.Constraint() {
            override fun adjustHintY(hintY: Float): Float {
                return Math.min(height, hintY)
            }

            override fun adjustPreferredSize(psize: Dimension, hintX: Float, hintY: Float) {
                psize.height = Math.min(psize.height, height)
            }
        }
    }

    /**
     * Returns a layout constraint that forces the widget's preferred width height to be no more
     * than the specified values.
     */
    fun maxSize(width: Float, height: Float): Layout.Constraint {
        return object : Layout.Constraint() {
            override fun adjustHintX(hintX: Float): Float {
                return Math.min(width, hintX)
            }

            override fun adjustHintY(hintY: Float): Float {
                return Math.min(height, hintY)
            }

            override fun adjustPreferredSize(psize: Dimension, hintX: Float, hintY: Float) {
                psize.width = Math.min(psize.width, width)
                psize.height = Math.min(psize.height, height)
            }
        }
    }

    /**
     * Returns a layout constraint that forces the widget's preferred width to be no less than the
     * specified value.
     */
    fun minWidth(width: Float): Layout.Constraint {
        return object : Layout.Constraint() {
            override fun adjustPreferredSize(psize: Dimension, hintX: Float, hintY: Float) {
                psize.width = Math.max(psize.width, width)
            }
        }
    }

    /**
     * Returns a layout constraint that forces the widget's preferred height to be no less than the
     * specified value.
     */
    fun minHeight(height: Float): Layout.Constraint {
        return object : Layout.Constraint() {
            override fun adjustPreferredSize(psize: Dimension, hintX: Float, hintY: Float) {
                psize.height = Math.max(psize.height, height)
            }
        }
    }

    /**
     * Returns a layout constraint that forces the widget's preferred width height to be no less
     * than the specified values.
     */
    fun minSize(width: Float, height: Float): Layout.Constraint {
        return object : Layout.Constraint() {
            override fun adjustPreferredSize(psize: Dimension, hintX: Float, hintY: Float) {
                psize.width = Math.max(psize.width, width)
                psize.height = Math.max(psize.height, height)
            }
        }
    }

    /**
     * Returns a layout constriant that forces the widget's preferred size to be no less than what
     * is needed to accommodate the supplied template text string. This is useful for configuring
     * the size of text widgets to be that of a largest-possible value.
     */
    fun minSize(gfx: Graphics, text: String): Layout.Constraint {
        return object : TemplateTextConstraint(gfx, text) {
            override fun addTextSize(
                    into: Dimension, lsize: IDimension?, tmplLayout: TextLayout) {
                val lwidth = lsize?.width ?: 0f
                val lheight = lsize?.height ?: 0f
                into.width += Math.max(lwidth, tmplLayout.size.width)
                into.height += Math.max(lheight, tmplLayout.size.height)
            }
        }
    }

    /**
     * Returns a layout constriant that forces the widget's preferred size to be precisely what is
     * needed to accommodate the supplied template text string. This is useful for configuring the
     * size of text widgets to be that of a largest-possible value.
     */
    fun fixedSize(gfx: Graphics, text: String): Layout.Constraint {
        return object : TemplateTextConstraint(gfx, text) {
            override fun addTextSize(
                    into: Dimension, lsize: IDimension?, tmplLayout: TextLayout) {
                into.width += tmplLayout.size.width
                into.height += tmplLayout.size.height
            }
        }
    }

    abstract class TemplateTextConstraint(protected val _gfx: Graphics, protected val _tmpl: String) : TextConstraint() {

        override fun setElement(elem: Element<*>) {
            _elem = elem
        }

        override fun addTextSize(into: Dimension, lsize: IDimension?) {
            val style = Style.createTextStyle(_elem!!)
            addTextSize(into, lsize, _gfx.layoutText(_tmpl, style))
        }

        protected abstract fun addTextSize(
                into: Dimension, lsize: IDimension?, tmplLayout: TextLayout)

        protected var _elem: Element<*>? = null
    }
}
