package tripleklay.ui.layout

import pythagoras.f.Dimension
import tripleklay.ui.Container
import tripleklay.ui.Element
import tripleklay.ui.Layout
import tripleklay.ui.Style

/**
 * Lays out elements in a horizontal or vertical group. Separate policies are enforced for on-axis
 * and off-axis sizing.

 *
 *  On-axis, the available space is divided up as follows: non-stretched elements are given
 * their preferred size, and remaining space is divided up among the stretched elements
 * proportional to their configured weight (which defaults to one). If no stretched elements exist,
 * elements are aligned per the [tripleplay.ui.Style.HAlign] and
 * [tripleplay.ui.Style.VAlign] properties on the containing group.

 *
 *  Off-axis sizing can be configured to either size elements to their preferred size, stretch
 * them all to a uniform size (equal to the preferred size of the largest element), or to stretch
 * them all to the size allotted to the container. When elements are not stretched to fill the size
 * allotted to the container, they may be aligned as above.
 */
abstract class AxisLayout : Layout() {
    /** Specifies the off-axis layout policy.  */
    enum class Policy {
        DEFAULT {
            override fun computeSize(size: Float, maxSize: Float, extent: Float): Float {
                return Math.min(size, extent)
            }
        },
        STRETCH {
            override fun computeSize(size: Float, maxSize: Float, extent: Float): Float {
                return extent
            }
        },
        EQUALIZE {
            override fun computeSize(size: Float, maxSize: Float, extent: Float): Float {
                return Math.min(maxSize, extent)
            }
        },
        CONSTRAIN {
            override fun computeSize(size: Float, maxSize: Float, extent: Float): Float {
                return Math.min(size, extent)
            }
        };

        abstract fun computeSize(size: Float, maxSize: Float, extent: Float): Float
    }

    /** Defines axis layout constraints.  */
    class Constraint(val stretch: Boolean, val weight: Float) : Layout.Constraint() {

        fun computeSize(size: Float, totalWeight: Float, availSize: Float): Float {
            return if (stretch) availSize * weight / totalWeight else size
        }
    }

    /** A vertical axis layout.  */
    class Vertical : AxisLayout() {
        override fun computeSize(elems: Container<*>, hintX: Float, hintY: Float): Dimension {
            val m = computeMetrics(elems, hintX, hintY, true)
            return Dimension(m.maxWidth, m.prefHeight + m.gaps(_gap.toFloat()))
        }

        override fun layout(elems: Container<*>,
                            left: Float, top: Float, width: Float, height: Float) {
            val halign = resolveStyle(elems, Style.HALIGN)
            val valign = resolveStyle(elems, Style.VALIGN)
            val m = computeMetrics(elems, width, height, true)
            val stretchHeight = Math.max(0f, height - m.gaps(_gap.toFloat()) - m.fixHeight)
            var y = top + if (m.stretchers > 0f)
                0f
            else
                valign.offset(m.fixHeight + m.gaps(_gap.toFloat()), height)
            for (elem in elems) {
                if (!elem.isVisible) continue
                val psize = preferredSize(elem, width, height) // will be cached
                val c = constraint(elem)
                val ewidth = _offPolicy.computeSize(psize.width, m.maxWidth, width)
                val eheight = c.computeSize(psize.height, m.totalWeight, stretchHeight)
                setBounds(elem, left + halign.offset(ewidth, width), y, ewidth, eheight)
                y += eheight + _gap
            }
        }
    }

    /** A horizontal axis layout.  */
    class Horizontal : AxisLayout() {
        override fun computeSize(elems: Container<*>, hintX: Float, hintY: Float): Dimension {
            val m = computeMetrics(elems, hintX, hintY, false)
            return Dimension(m.prefWidth + m.gaps(_gap.toFloat()), m.maxHeight)
        }

        override fun layout(elems: Container<*>,
                            left: Float, top: Float, width: Float, height: Float) {
            val halign = resolveStyle(elems, Style.HALIGN)
            val valign = resolveStyle(elems, Style.VALIGN)
            val m = computeMetrics(elems, width, height, false)
            val stretchWidth = Math.max(0f, width - m.gaps(_gap.toFloat()) - m.fixWidth)
            var x = left + if (m.stretchers > 0f)
                0f
            else
                halign.offset(m.fixWidth + m.gaps(_gap.toFloat()), width)
            for (elem in elems) {
                if (!elem.isVisible) continue
                val psize = preferredSize(elem, width, height) // will be cached
                val c = constraint(elem)
                val ewidth = c.computeSize(psize.width, m.totalWeight, stretchWidth)
                val eheight = _offPolicy.computeSize(psize.height, m.maxHeight, height)
                setBounds(elem, x, top + valign.offset(eheight, height), ewidth, eheight)
                x += ewidth + _gap
            }
        }
    }

    /**
     * Configures the default constraint for elements added to this layout to be stretched. This
     * is equivalent to calling [Element.setConstraint] with
     * [.stretched] for each element added to the parent container.
     */
    fun stretchByDefault(): AxisLayout {
        _stretchByDefault = true
        return this
    }

    /**
     * Configures the off-axis sizing policy for this layout.
     */
    fun offPolicy(policy: Policy): AxisLayout {
        _offPolicy = policy
        return this
    }

    /**
     * Configures this layout to stretch all elements to the available size on the off-axis.
     */
    fun offStretch(): AxisLayout {
        return offPolicy(Policy.STRETCH)
    }

    /**
     * Configures this layout to stretch all elements to the size of the largest element on the
     * off-axis.
     */
    fun offEqualize(): AxisLayout {
        return offPolicy(Policy.EQUALIZE)
    }

    /**
     * Configures this layout to constrain elements to the size of this container on the off-axis,
     * leaving their size alone if it is smaller.
     */
    fun offConstrain(): AxisLayout {
        return offPolicy(Policy.CONSTRAIN)
    }

    /**
     * Configures the inter-element gap, in pixels.
     */
    fun gap(gap: Int): AxisLayout {
        _gap = gap
        return this
    }

    protected fun computeMetrics(elems: Container<*>, hintX: Float, hintY: Float,
                                 vert: Boolean): Metrics {
        val m = Metrics()
        for (elem in elems) {
            if (!elem.isVisible) continue
            m.count++

            // only compute the preferred size for the fixed elements in this pass
            val c = constraint(elem)
            if (!c.stretch) {
                val psize = preferredSize(elem, hintX, hintY)
                val pwidth = psize.width
                val pheight = psize.height
                m.prefWidth += pwidth
                m.prefHeight += pheight
                m.maxWidth = Math.max(m.maxWidth, pwidth)
                m.maxHeight = Math.max(m.maxHeight, pheight)
                m.fixWidth += pwidth
                m.fixHeight += pheight
            } else {
                m.stretchers++
                m.totalWeight += c.weight
            }
        }

        // now compute the preferred size for the stretched elements, providing them with more
        // accurate width/height hints
        for (elem in elems) {
            if (!elem.isVisible) continue
            val c = constraint(elem)
            if (!c.stretch) continue

            // the first argument to computeSize is not used for stretched elements
            val availX = hintX - m.gaps(_gap.toFloat())
            val availY = hintY - m.gaps(_gap.toFloat())
            val ehintX = if (vert) availX else c.computeSize(0f, m.totalWeight, availX - m.fixWidth)
            val ehintY = if (vert) c.computeSize(0f, m.totalWeight, availY - m.fixHeight) else availY
            val psize = preferredSize(elem, ehintX, ehintY)
            val pwidth = psize.width
            val pheight = psize.height
            m.unitWidth = Math.max(m.unitWidth, pwidth / c.weight)
            m.unitHeight = Math.max(m.unitHeight, pheight / c.weight)
            m.maxWidth = Math.max(m.maxWidth, pwidth)
            m.maxHeight = Math.max(m.maxHeight, pheight)
        }
        m.prefWidth += m.stretchers * m.unitWidth
        m.prefHeight += m.stretchers * m.unitHeight

        return m
    }

    protected fun constraint(elem: Element<*>): Constraint {
        val c = elem.constraint()
        return c as? Constraint ?: if (_stretchByDefault) UNIFORM_STRETCHED else UNSTRETCHED
    }

    protected class Metrics {
        var count: Int = 0

        var prefWidth: Float = 0.toFloat()
        var prefHeight: Float = 0.toFloat()

        var maxWidth: Float = 0.toFloat()
        var maxHeight: Float = 0.toFloat()

        var fixWidth: Float = 0.toFloat()
        var fixHeight: Float = 0.toFloat()

        var unitWidth: Float = 0.toFloat()
        var unitHeight: Float = 0.toFloat()

        var stretchers: Int = 0
        var totalWeight: Float = 0.toFloat()

        fun gaps(gap: Float): Float {
            return gap * (count - 1)
        }
    }

    protected var _gap = 5
    protected var _stretchByDefault: Boolean = false
    protected var _offPolicy = Policy.DEFAULT

    companion object {

        /**
         * Creates a vertical axis layout with default gap (5), and off-axis sizing policy (preferred
         * size).
         */
        fun vertical(): Vertical {
            return Vertical()
        }

        /**
         * Creates a horizontal axis layout with default gap (5), and off-axis sizing policy (preferred
         * size).
         */
        fun horizontal(): Horizontal {
            return Horizontal()
        }

        /**
         * Returns a layout constraint indicating that the associated element should be stretched to
         * consume extra space, with weight 1.
         */
        fun stretched(): Constraint {
            return UNIFORM_STRETCHED
        }

        /**
         * Returns a layout constraint indicating that the associated element should not be stretched.
         */
        fun fixed(): Constraint {
            return UNSTRETCHED
        }

        /**
         * Returns a layout constraint indicating that the associated element should be stretched to
         * consume extra space, with the specified weight.
         */
        fun stretched(weight: Float): Constraint {
            return Constraint(true, weight)
        }

        /**
         * Configures the supplied element with a [.stretched] constraint.
         */
        fun <T : Element<*>> stretch(elem: T): T {
            elem.setConstraint(stretched())
            return elem
        }

        /**
         * Configures the supplied element with a weighted [.stretched] constraint.
         */
        fun <T : Element<*>> stretch(elem: T, weight: Float): T {
            elem.setConstraint(stretched(weight))
            return elem
        }

        protected val UNSTRETCHED = Constraint(false, 1f)
        protected val UNIFORM_STRETCHED = Constraint(true, 1f)
    }
}
