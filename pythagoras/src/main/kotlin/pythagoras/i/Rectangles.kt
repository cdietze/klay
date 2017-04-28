//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.i

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
}
