//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.d

/**
 * Cubic curve-related utility methods.
 */
object CubicCurves {
    fun flatnessSq(x1: Double, y1: Double, ctrlx1: Double, ctrly1: Double,
                   ctrlx2: Double, ctrly2: Double, x2: Double, y2: Double): Double {
        return Math.max(Lines.pointSegDistSq(ctrlx1, ctrly1, x1, y1, x2, y2),
                Lines.pointSegDistSq(ctrlx2, ctrly2, x1, y1, x2, y2))
    }

    fun flatnessSq(coords: DoubleArray, offset: Int): Double {
        return flatnessSq(coords[offset + 0], coords[offset + 1], coords[offset + 2],
                coords[offset + 3], coords[offset + 4], coords[offset + 5],
                coords[offset + 6], coords[offset + 7])
    }

    fun flatness(x1: Double, y1: Double, ctrlx1: Double, ctrly1: Double,
                 ctrlx2: Double, ctrly2: Double, x2: Double, y2: Double): Double {
        return Math.sqrt(flatnessSq(x1, y1, ctrlx1, ctrly1, ctrlx2, ctrly2, x2, y2))
    }

    fun flatness(coords: DoubleArray, offset: Int): Double {
        return flatness(coords[offset + 0], coords[offset + 1], coords[offset + 2],
                coords[offset + 3], coords[offset + 4], coords[offset + 5],
                coords[offset + 6], coords[offset + 7])
    }

    fun subdivide(src: ICubicCurve, left: CubicCurve?, right: CubicCurve?) {
        val x1 = src.x1()
        val y1 = src.y1()
        var cx1 = src.ctrlX1()
        var cy1 = src.ctrlY1()
        var cx2 = src.ctrlX2()
        var cy2 = src.ctrlY2()
        val x2 = src.x2()
        val y2 = src.y2()
        var cx = (cx1 + cx2) / 2f
        var cy = (cy1 + cy2) / 2f
        cx1 = (x1 + cx1) / 2f
        cy1 = (y1 + cy1) / 2f
        cx2 = (x2 + cx2) / 2f
        cy2 = (y2 + cy2) / 2f
        val ax = (cx1 + cx) / 2f
        val ay = (cy1 + cy) / 2f
        val bx = (cx2 + cx) / 2f
        val by = (cy2 + cy) / 2f
        cx = (ax + bx) / 2f
        cy = (ay + by) / 2f
        left?.setCurve(x1, y1, cx1, cy1, ax, ay, cx, cy)
        right?.setCurve(cx, cy, bx, by, cx2, cy2, x2, y2)
    }

    fun subdivide(src: DoubleArray, srcOff: Int, left: DoubleArray?, leftOff: Int,
                  right: DoubleArray?, rightOff: Int) {
        val x1 = src[srcOff + 0]
        val y1 = src[srcOff + 1]
        var cx1 = src[srcOff + 2]
        var cy1 = src[srcOff + 3]
        var cx2 = src[srcOff + 4]
        var cy2 = src[srcOff + 5]
        val x2 = src[srcOff + 6]
        val y2 = src[srcOff + 7]
        var cx = (cx1 + cx2) / 2f
        var cy = (cy1 + cy2) / 2f
        cx1 = (x1 + cx1) / 2f
        cy1 = (y1 + cy1) / 2f
        cx2 = (x2 + cx2) / 2f
        cy2 = (y2 + cy2) / 2f
        val ax = (cx1 + cx) / 2f
        val ay = (cy1 + cy) / 2f
        val bx = (cx2 + cx) / 2f
        val by = (cy2 + cy) / 2f
        cx = (ax + bx) / 2f
        cy = (ay + by) / 2f
        if (left != null) {
            left[leftOff + 0] = x1
            left[leftOff + 1] = y1
            left[leftOff + 2] = cx1
            left[leftOff + 3] = cy1
            left[leftOff + 4] = ax
            left[leftOff + 5] = ay
            left[leftOff + 6] = cx
            left[leftOff + 7] = cy
        }
        if (right != null) {
            right[rightOff + 0] = cx
            right[rightOff + 1] = cy
            right[rightOff + 2] = bx
            right[rightOff + 3] = by
            right[rightOff + 4] = cx2
            right[rightOff + 5] = cy2
            right[rightOff + 6] = x2
            right[rightOff + 7] = y2
        }
    }

    @JvmOverloads fun solveCubic(eqn: DoubleArray, res: DoubleArray = eqn): Int {
        return Crossing.solveCubic(eqn, res)
    }
}
