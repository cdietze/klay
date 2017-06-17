package tripleklay.ui.util

import klay.scene.LayerUtil
import pythagoras.f.IDimension
import pythagoras.f.Point
import tripleklay.ui.Element
import tripleklay.ui.Style.HAlign
import tripleklay.ui.Style.VAlign

/** Defines a point relative to a box.  */
class BoxPoint
/** Creates a new box point that will resolve to the given normalized coordinates plus
 * the given absolute coordinates.  */
@JvmOverloads constructor(
        /** Normalized x, y coordinates. For example, nx = 1 is the right edge.  */
        val nx: Float, val ny: Float,
        /** Absolute x, y offsets.  */
        val ox: Float = 0f, val oy: Float = 0f) {

    /** Creates a new box point that is equivalent to this one except with an x coordinate that
     * will resolve to the left edge of the box.  */
    fun left(): BoxPoint {
        return nx(0f)
    }

    /** Creates a new box point that is equivalent to this one except with an x coordinate that
     * will resolve to the right edge of the box.  */
    fun right(): BoxPoint {
        return nx(1f)
    }

    /** Creates a new box point that is equivalent to this one except with a y coordinate that
     * will resolve to the top edge of the box.  */
    fun top(): BoxPoint {
        return ny(0f)
    }

    /** Creates a new box point that is equivalent to this one except with a y coordinate that
     * will resolve to the top bottom of the box.  */
    fun bottom(): BoxPoint {
        return ny(1f)
    }

    /** Creates a new box point that is equivalent to this one except with x, y coordinates that
     * will resolve to the center of the box.  */
    fun center(): BoxPoint {
        return BoxPoint(.5f, .5f, ox, oy)
    }

    /** Creates a new box point that is equivalent to this one except with given offset
     * coordinates.  */
    fun offset(x: Float, y: Float): BoxPoint {
        return BoxPoint(nx, ny, x, y)
    }

    /** Creates a new box point that is equivalent to this one except with the given normalized
     * y coordinate.  */
    fun ny(ny: Float): BoxPoint {
        return BoxPoint(nx, ny, ox, oy)
    }

    /** Creates a new box point that is equivalent to this one except with the given horizontal
     * and vertical alignment.  */
    fun align(halign: HAlign, valign: VAlign): BoxPoint {
        return BoxPoint(halign.offset(0f, 1f), valign.offset(0f, 1f), ox, oy)
    }

    /** Creates a new box point that is equivalent to this one except with the given y alignment.
     * This is a shortcut for calling [.ny] with 0, .5, or 1.  */
    fun valign(valign: VAlign): BoxPoint {
        return ny(valign.offset(0f, 1f))
    }

    /** Creates a new box point that is equivalent to this one except with the given normalized
     * x coordinate.  */
    fun nx(nx: Float): BoxPoint {
        return BoxPoint(nx, ny, ox, oy)
    }

    /** Creates a new box point that is equivalent to this one except with the given x alignment.
     * This is a shortcut for calling [.nx] with 0, .5, or 1.  */
    fun halign(halign: HAlign): BoxPoint {
        return nx(halign.offset(0f, 1f))
    }

    /** Finds the screen coordinates of the point, using the given element as the box.  */
    fun resolve(elem: Element<*>, dest: Point): Point {
        LayerUtil.layerToScreen(elem.layer, dest.set(0f, 0f), dest)
        return resolve(dest.x, dest.y, elem.size().width(), elem.size().height(), dest)
    }

    /** Finds the coordinates of the point, using the box defined by the given coordinates.  */
    fun resolve(x: Float, y: Float, width: Float, height: Float, dest: Point): Point {
        return dest.set(x + ox + nx * width, y + oy + ny * height)
    }

    /** Finds the coordinates of the point, using the box with top left of 0, 0 and the given
     * dimension.  */
    fun resolve(size: IDimension, dest: Point): Point {
        return resolve(0f, 0f, size.width(), size.height(), dest)
    }

    companion object {
        /** The top left corner.  */
        val TL = BoxPoint(0f, 0f)

        /** The bottom left corner.  */
        val BL = BoxPoint(0f, 1f)

        /** The top right corner.  */
        val TR = BoxPoint(1f, 0f)

        /** The bottom right corner.  */
        val BR = BoxPoint(1f, 1f)

        /** The center of the box.  */
        val CENTER = BoxPoint(.5f, .5f)
    }
}
/** Creates a new box point that will resolve to the given normalized coordinates.  */
