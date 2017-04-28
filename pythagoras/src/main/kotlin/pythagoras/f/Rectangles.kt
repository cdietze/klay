//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

/**
 * Rectangle-related utility methods.
 */
object Rectangles {
    /**
     * Intersects the supplied two rectangles, writing the result into `dst`.
     */
    fun intersect(src1: IRectangle, src2: IRectangle, dst: Rectangle) {
        val x1 = Math.max(src1.minX(), src2.minX())
        val y1 = Math.max(src1.minY(), src2.minY())
        val x2 = Math.min(src1.maxX(), src2.maxX())
        val y2 = Math.min(src1.maxY(), src2.maxY())
        dst.setBounds(x1, y1, x2 - x1, y2 - y1)
    }

    /**
     * Unions the supplied two rectangles, writing the result into `dst`.
     */
    fun union(src1: IRectangle, src2: IRectangle, dst: Rectangle) {
        val x1 = Math.min(src1.minX(), src2.minX())
        val y1 = Math.min(src1.minY(), src2.minY())
        val x2 = Math.max(src1.maxX(), src2.maxX())
        val y2 = Math.max(src1.maxY(), src2.maxY())
        dst.setBounds(x1, y1, x2 - x1, y2 - y1)
    }

    /**
     * Computes the point inside the bounds of the rectangle that's closest to the given point,
     * writing the result into `out`.
     * @return `out` for call chaining convenience.
     */
    @JvmOverloads fun closestInteriorPoint(r: IRectangle, p: IPoint, out: Point = Point()): Point {
        out.set(MathUtil.clamp(p.x(), r.minX(), r.maxX()),
                MathUtil.clamp(p.y(), r.minY(), r.maxY()))
        return out
    }

    /**
     * Returns the squared Euclidean distance between the given point and the nearest point inside
     * the bounds of the given rectangle. If the supplied point is inside the rectangle, the
     * distance will be zero.
     */
    fun pointRectDistanceSq(r: IRectangle, p: IPoint): Float {
        val p2 = closestInteriorPoint(r, p)
        return Points.distanceSq(p.x(), p.y(), p2.x, p2.y)
    }

    /**
     * Returns the Euclidean distance between the given point and the nearest point inside the
     * bounds of the given rectangle. If the supplied point is inside the rectangle, the distance
     * will be zero.
     */
    fun pointRectDistance(r: IRectangle, p: IPoint): Float {
        return FloatMath.sqrt(pointRectDistanceSq(r, p))
    }
}
/**
 * Computes and returns the point inside the bounds of the rectangle that's closest to the
 * given point.
 */
