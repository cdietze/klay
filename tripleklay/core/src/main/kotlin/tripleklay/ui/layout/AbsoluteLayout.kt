package tripleklay.ui.layout

import pythagoras.f.*
import tripleklay.ui.Container
import tripleklay.ui.Element
import tripleklay.ui.Layout
import tripleklay.ui.Style.HAlign
import tripleklay.ui.Style.VAlign
import tripleklay.ui.util.BoxPoint

/**
 * A layout that positions elements at absolute coordinates (at either their preferred size or at a
 * manually specified size). Constraints are specified like so:
 * <pre>`Group group = new Group(new AbsoluteLayout()).add(
 * AbsoluteLayout.at(new Label("+50+50"), 50, 50),
 * AbsoluteLayout.at(new Button("100x50+25+25"), 25, 25, 100, 50)
 * );
`</pre> *
 */
class AbsoluteLayout : Layout() {
    /** Defines absolute layout constraints.  */
    class Constraint(val position: BoxPoint, val origin: BoxPoint, val size: IDimension) : Layout.Constraint() {

        constructor(position: IPoint, size: IDimension, halign: HAlign, valign: VAlign) : this(BoxPoint.TL.offset(position.x, position.y),
                BoxPoint.TL.align(halign, valign), size) {
        }

        fun psize(layout: AbsoluteLayout, elem: Element<*>): IDimension {
            val fwidth = size.width
            val fheight = size.height
            if (fwidth > 0 && fheight > 0) return size
            // if either forced width or height is zero, use preferred size in that dimension
            val psize = layout.preferredSize(elem, fwidth, fheight)
            if (fwidth > 0)
                return Dimension(fwidth, psize.height)
            else if (fheight > 0)
                return Dimension(psize.width, fheight)
            else
                return psize
        }

        fun pos(width: Float, height: Float, prefSize: IDimension): IPoint {
            val position = this.position.resolve(0f, 0f, width, height, Point())
            val origin = this.origin.resolve(prefSize, Point())
            position.x -= origin.x
            position.y -= origin.y
            return position
        }
    }

    override fun computeSize(elems: Container<*>, hintX: Float, hintY: Float): Dimension {
        // report a size large enough to contain all of our elements
        val bounds = Rectangle()
        for (elem in elems) {
            if (!elem.isVisible) continue
            val c = constraint(elem)
            val psize = c.psize(this, elem)
            bounds.add(Rectangle(c.pos(0f, 0f, psize), psize))
        }
        return Dimension(bounds.width, bounds.height)
    }

    override fun layout(elems: Container<*>,
                        left: Float, top: Float, width: Float, height: Float) {
        for (elem in elems) {
            if (!elem.isVisible) continue
            val c = constraint(elem)
            val psize = c.psize(this, elem) // this should return a cached size
            val pos = c.pos(width, height, psize)
            setBounds(elem, left + pos.x, top + pos.y, psize.width, psize.height)
        }
    }

    companion object {

        /**
         * Creates a constraint to position an element uniformly. The given box point is used for both
         * the position and the origin. For example, if `BoxPoint.BR` is used, then the element
         * will be positioned such that its bottom right corner is over the bottom right corner of
         * the group.
         */
        fun uniform(where: BoxPoint): Constraint {
            return Constraint(where, where, ZERO)
        }

        /**
         * Positions `elem` at the specified position, in its preferred size.
         */
        fun <T : Element<*>> at(elem: T, x: Float, y: Float): T {
            return at(elem, Point(x, y))
        }

        /**
         * Positions `elem` at the specified position, in its preferred size.
         */
        fun <T : Element<*>> at(elem: T, position: IPoint): T {
            return at(elem, position, ZERO)
        }

        /**
         * Constrains `elem` to the specified position and size.
         */
        fun <T : Element<*>> at(elem: T, x: Float, y: Float, width: Float, height: Float): T {
            return at(elem, Point(x, y), Dimension(width, height))
        }

        /**
         * Constrains `elem` to the specified position and size.
         */
        fun <T : Element<*>> at(elem: T, position: IPoint, size: IDimension): T {
            elem.setConstraint(Constraint(position, size, HAlign.LEFT, VAlign.TOP))
            return elem
        }

        /**
         * Positions `elem` relative to the given position using the given alignments.
         */
        fun <T : Element<*>> at(elem: T, x: Float, y: Float,
                                halign: HAlign, valign: VAlign): T {
            return at(elem, Point(x, y), ZERO, halign, valign)
        }

        /**
         * Positions `elem` relative to the given position using the given alignments.
         */
        fun <T : Element<*>> at(elem: T, position: IPoint,
                                halign: HAlign, valign: VAlign): T {
            return at(elem, position, ZERO, halign, valign)
        }

        /**
         * Constrains `elem` to the specified size and aligns it relative to the given position
         * using the given alignments.
         */
        fun <T : Element<*>> at(elem: T, x: Float, y: Float, width: Float, height: Float,
                                halign: HAlign, valign: VAlign): T {
            return at(elem, Point(x, y), Dimension(width, height), halign, valign)
        }

        /**
         * Constrains `elem` to the specified size and aligns it relative to the given position
         * using the given alignments.
         */
        fun <T : Element<*>> at(elem: T, position: IPoint, size: IDimension,
                                halign: HAlign, valign: VAlign): T {
            elem.setConstraint(Constraint(position, size, halign, valign))
            return elem
        }

        /**
         * Centers `elem` on the specified position, in its preferred size.
         */
        fun <T : Element<*>> centerAt(elem: T, x: Float, y: Float): T {
            return centerAt(elem, Point(x, y))
        }

        /**
         * Centers `elem` on the specified position, in its preferred size.
         */
        fun <T : Element<*>> centerAt(elem: T, position: IPoint): T {
            elem.setConstraint(Constraint(position, ZERO, HAlign.CENTER, VAlign.CENTER))
            return elem
        }

        protected fun constraint(elem: Element<*>): Constraint {
            assert(elem.constraint() != null) { "Elements in AbsoluteLayout must have a constraint." }
            return elem.constraint() as Constraint
        }

        protected val ZERO = Dimension(0f, 0f)
    }
}
