//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.d

/**
 * Quad curve-related utility methods.
 */
object QuadCurves {
    fun flatnessSq(x1: Double, y1: Double, ctrlx: Double, ctrly: Double,
                   x2: Double, y2: Double): Double {
        return Lines.pointSegDistSq(ctrlx, ctrly, x1, y1, x2, y2)
    }

    fun flatnessSq(coords: DoubleArray, offset: Int): Double {
        return Lines.pointSegDistSq(coords[offset + 2], coords[offset + 3],
                coords[offset + 0], coords[offset + 1],
                coords[offset + 4], coords[offset + 5])
    }

    fun flatness(x1: Double, y1: Double, ctrlx: Double, ctrly: Double,
                 x2: Double, y2: Double): Double {
        return Lines.pointSegDist(ctrlx, ctrly, x1, y1, x2, y2)
    }

    fun flatness(coords: DoubleArray, offset: Int): Double {
        return Lines.pointSegDist(coords[offset + 2], coords[offset + 3],
                coords[offset + 0], coords[offset + 1],
                coords[offset + 4], coords[offset + 5])
    }

    fun subdivide(src: IQuadCurve, left: QuadCurve?, right: QuadCurve?) {
        val x1 = src.x1()
        val y1 = src.y1()
        var cx = src.ctrlX()
        var cy = src.ctrlY()
        val x2 = src.x2()
        val y2 = src.y2()
        val cx1 = (x1 + cx) / 2f
        val cy1 = (y1 + cy) / 2f
        val cx2 = (x2 + cx) / 2f
        val cy2 = (y2 + cy) / 2f
        cx = (cx1 + cx2) / 2f
        cy = (cy1 + cy2) / 2f
        left?.setCurve(x1, y1, cx1, cy1, cx, cy)
        right?.setCurve(cx, cy, cx2, cy2, x2, y2)
    }

    fun subdivide(src: DoubleArray, srcoff: Int,
                  left: DoubleArray?, leftOff: Int, right: DoubleArray?, rightOff: Int) {
        val x1 = src[srcoff + 0]
        val y1 = src[srcoff + 1]
        var cx = src[srcoff + 2]
        var cy = src[srcoff + 3]
        val x2 = src[srcoff + 4]
        val y2 = src[srcoff + 5]
        val cx1 = (x1 + cx) / 2f
        val cy1 = (y1 + cy) / 2f
        val cx2 = (x2 + cx) / 2f
        val cy2 = (y2 + cy) / 2f
        cx = (cx1 + cx2) / 2f
        cy = (cy1 + cy2) / 2f
        if (left != null) {
            left[leftOff + 0] = x1
            left[leftOff + 1] = y1
            left[leftOff + 2] = cx1
            left[leftOff + 3] = cy1
            left[leftOff + 4] = cx
            left[leftOff + 5] = cy
        }
        if (right != null) {
            right[rightOff + 0] = cx
            right[rightOff + 1] = cy
            right[rightOff + 2] = cx2
            right[rightOff + 3] = cy2
            right[rightOff + 4] = x2
            right[rightOff + 5] = y2
        }
    }

    @JvmOverloads fun solveQuadratic(eqn: DoubleArray, res: DoubleArray = eqn): Int {
        return Crossing.solveQuad(eqn, res)
    }
}
