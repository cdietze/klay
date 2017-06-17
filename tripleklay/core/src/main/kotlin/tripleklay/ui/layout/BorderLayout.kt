package tripleklay.ui.layout

import pythagoras.f.Dimension
import pythagoras.f.IDimension
import pythagoras.f.Rectangle
import tripleklay.ui.Container
import tripleklay.ui.Element
import tripleklay.ui.Layout
import tripleklay.ui.Style

import java.util.HashMap

/**
 * Arranges up to 5 elements, one central and one on each edge. Added elements must have a
 * constraint from the class' listing (e.g. [BorderLayout.CENTER]), which determines the
 * position in the layout and stretching.

 *
 * This is how the layout looks. Note north/south and east/west behavior is not quite symmetric
 * because east and west fit between the bottom of the north and top of the south:

 * <pre>
 * |-----------------------------|
 * |            north            |
 * |-----------------------------|
 * |      |               |      |
 * |      |               |      |
 * |      |               |      |
 * | west |    center     | east |
 * |      |               |      |
 * |      |               |      |
 * |-----------------------------|
 * |            south            |
 * |-----------------------------|
</pre> *

 * When an element is not stretched, it obeys the [tripleplay.ui.Style.HAlign] and [ ] bindings.
 */
class BorderLayout
/**
 * Constructs a new border layout with the specified horizontal and vertical gaps between
 * components.
 */
(
        /** The horizontal gap between components.  */
        val hgap: Float,
        /** The vertical gap between components.  */
        val vgap: Float) : Layout() {

    /**
     * Implements the constraints. Callers do not need to construct instances, but instead use the
     * declared constants and select or deselect the stretching option.
     */
    class Constraint(protected val _pos: Position, val _stretch: Boolean) : Layout.Constraint() {

        /**
         * Returns a new constraint specifying the same position as this, and with stretching.
         */
        fun stretched(): Constraint {
            return _pos.stretched
        }

        /**
         * Returns a new constraint specifying the same position as this, and with no stretching.
         * The element's preferred size will be used and an appropriate alignment.
         */
        fun unstretched(): Constraint {
            return _pos.unstretched
        }

        fun adjust(pref: IDimension, boundary: Rectangle): Dimension {
            val dim = Dimension(pref)
            if (_stretch) {
                if (_pos.orient and 1 != 0) {
                    dim.width = boundary.width
                }
                if (_pos.orient and 2 != 0) {
                    dim.height = boundary.height
                }
            }
            dim.width = Math.min(dim.width, boundary.width)
            dim.height = Math.min(dim.height, boundary.height)
            return dim
        }

        fun align(origin: Float, offset: Float): Float {
            return if (_stretch) origin else origin + offset
        }
    }

    /**
     * Constructs a new border layout with the specified gap between components.
     */
    @JvmOverloads constructor(gaps: Float = 0f) : this(gaps, gaps) {}

    override fun computeSize(elems: Container<*>, hintX: Float, hintY: Float): Dimension {
        return Slots(elems).computeSize(hintX, hintY)
    }

    override fun layout(elems: Container<*>, left: Float, top: Float, width: Float, height: Float) {
        val halign = resolveStyle<HAlign>(elems, Style.HALIGN)
        val valign = resolveStyle<VAlign>(elems, Style.VALIGN)
        val slots = Slots(elems)
        val bounds = Rectangle(left, top, width, height)
        slots.layoutNs(Position.NORTH, halign, bounds)
        slots.layoutNs(Position.SOUTH, halign, bounds)
        slots.layoutWe(Position.WEST, valign, bounds)
        slots.layoutWe(Position.EAST, valign, bounds)

        val p = Position.CENTER
        var dim: IDimension? = slots.size(p, bounds.width, bounds.height) ?: return

        val c = slots.constraint(p)
        dim = c.adjust(dim, bounds)
        slots.setBounds(p,
                c.align(bounds.x, halign.offset(dim.width(), bounds.width)),
                c.align(bounds.y, valign.offset(dim.height(), bounds.height)), dim)
    }

    protected inner class Slots internal constructor(elems: Container<*>) {
        internal val elements: MutableMap<Position, Element<*>> = HashMap()

        init {
            for (elem in elems) {
                if (!elem.isVisible) continue
                val p = Position.positionOf(elem.constraint()) ?: throw IllegalStateException(
                        "Element with a non-BorderLayout constraint: " + elem)
                val existing = elements.put(p, elem)
                if (existing != null) {
                    throw IllegalStateException(
                            "Multiple elements: " + elem + " and " + existing +
                                    " with the same BorderLayout constraint: " + p)
                }
            }
        }

        internal fun computeSize(hintX: Float, hintY: Float): Dimension {
            val wce = count(*WCE)
            val nsSize = Dimension()
            for (pos in NS) {
                val dim = size(pos, hintX, 0f) ?: continue
                nsSize.height += dim.height()
                nsSize.width = Math.max(nsSize.width, dim.width())
                if (wce > 0) {
                    nsSize.height += vgap
                }
            }

            val ehintY = Math.max(0f, hintY - nsSize.height)
            val weSize = Dimension()
            for (pos in WE) {
                val dim = size(pos, 0f, ehintY) ?: continue
                weSize.width += dim.width()
                weSize.height = Math.max(weSize.height, dim.height())
            }

            weSize.width += Math.max(wce - 1, 0) * hgap
            val ehintX = Math.max(0f, hintX - weSize.width)

            val csize = size(Position.CENTER, ehintX, ehintY)
            if (csize != null) {
                weSize.width += csize.width()
                nsSize.height += csize.height()
            }
            return Dimension(
                    Math.max(weSize.width, nsSize.width),
                    Math.max(weSize.height, nsSize.height))
        }

        internal fun layoutNs(p: Position, halign: Style.HAlign, bounds: Rectangle) {
            var dim: IDimension? = size(p, bounds.width, 0f) ?: return
            val c = constraint(p)
            dim = c.adjust(dim, bounds)
            var y = bounds.y
            if (p == Position.NORTH) {
                bounds.y += dim.height() + vgap
            } else {
                y += bounds.height - dim.height()
            }
            bounds.height -= dim.height() + vgap
            setBounds(p, c.align(bounds.x, halign.offset(dim.width(), bounds.width)), y, dim)
        }

        internal fun layoutWe(p: Position, valign: Style.VAlign, bounds: Rectangle) {
            var dim: IDimension? = size(p, 0f, bounds.height) ?: return
            val c = constraint(p)
            dim = c.adjust(dim, bounds)
            var x = bounds.x
            if (p == Position.WEST) {
                bounds.x += dim.width() + hgap
            } else {
                x += bounds.width - dim.width()
            }
            bounds.width -= dim.width() + hgap
            setBounds(p, x, c.align(bounds.y, valign.offset(dim.height(), bounds.height)), dim)
        }

        internal fun setBounds(p: Position, x: Float, y: Float, dim: IDimension) {
            this@BorderLayout.setBounds(get(p), x, y, dim.width(), dim.height())
        }

        internal fun count(vararg ps: Position): Int {
            var count = 0
            for (p in ps) {
                if (elements.containsKey(p)) {
                    count++
                }
            }
            return count
        }

        internal fun stretch(p: Position): Boolean {
            return (get(p).constraint() as Constraint)._stretch
        }

        internal operator fun get(p: Position): Element<*> {
            return elements[p]
        }

        internal fun constraint(p: Position): Constraint {
            return get(p).constraint() as Constraint
        }

        internal fun size(p: Position, hintX: Float, hintY: Float): IDimension? {
            val e = elements[p]
            return if (e == null) null else preferredSize(e, hintX, hintY)
        }
    }

    protected enum class Position private constructor(internal val orient: Int) {
        CENTER(3), NORTH(1), SOUTH(1), EAST(2), WEST(2);

        internal val unstretched: Constraint
        internal val stretched: Constraint

        init {
            unstretched = Constraint(this, false)
            stretched = Constraint(this, true)
        }

        companion object {

            internal fun positionOf(c: Layout.Constraint): Position? {
                for (p in values()) {
                    if (p.unstretched === c || p.stretched === c) {
                        return p
                    }
                }
                return null
            }
        }
    }

    companion object {
        /** Constraint to position an element in the center of its parent. The element is stretched in
         * both directions to take up available space. If [Constraint.unstretched] is used, the
         * element will be aligned in both directions using its preferred size and the [ ] and [tripleplay.ui.Style.VAlign] bindings.  */
        val CENTER = Position.CENTER.stretched

        /** Constraint to position an element along the top edge of its parent. The element is
         * stretched horizontally and uses its preferred height. If [Constraint.unstretched] is
         * used, the element will be aligned horizontally using its preferred size according to the
         * [tripleplay.ui.Style.HAlign] binding.  */
        val NORTH = Position.NORTH.stretched

        /** Constraint to position an element along the bottom edge of its parent. The element is
         * stretched horizontally and uses its preferred height. If [Constraint.unstretched] is
         * used, the element will be aligned horizontally using its preferred size according to the
         * [tripleplay.ui.Style.HAlign] binding.  */
        val SOUTH = Position.SOUTH.stretched

        /** Constraint to position an element along the right edge of its parent. The element is
         * stretched vertically and uses its preferred width. If [Constraint.unstretched] is
         * used, the element will be aligned vertically using its preferred size according to the
         * [tripleplay.ui.Style.VAlign] binding.  */
        val EAST = Position.EAST.stretched

        /** Constraint to position an element along the right edge of its parent. The element is
         * stretched vertically and uses its preferred width. If [Constraint.unstretched] is
         * used, the element will be aligned vertically using its preferred size according to the
         * [tripleplay.ui.Style.VAlign] binding.  */
        val WEST = Position.WEST.stretched

        protected val NS = arrayOf(Position.NORTH, Position.SOUTH)
        protected val WE = arrayOf(Position.WEST, Position.EAST)
        protected val WCE = arrayOf(Position.WEST, Position.CENTER, Position.EAST)
    }
}
/**
 * Constructs a new border layout with no gaps.
 */
