//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

/**
 * Line-related utility methods.
 */
object Lines {
    /**
     * Returns true if the specified two line segments intersect.
     */
    fun linesIntersect(x1: Float, y1: Float, x2: Float, y2: Float,
                       x3: Float, y3: Float, x4: Float, y4: Float): Boolean {
        var x2 = x2
        var y2 = y2
        var x3 = x3
        var y3 = y3
        var x4 = x4
        var y4 = y4
        // A = (x2-x1, y2-y1)
        // B = (x3-x1, y3-y1)
        // C = (x4-x1, y4-y1)
        // D = (x4-x3, y4-y3) = C-B
        // E = (x1-x3, y1-y3) = -B
        // F = (x2-x3, y2-y3) = A-B
        //
        // Result is ((AxB) * (AxC) <= 0) and ((DxE) * (DxF) <= 0)
        //
        // DxE = (C-B)x(-B) = BxB-CxB = BxC
        // DxF = (C-B)x(A-B) = CxA-CxB-BxA+BxB = AxB+BxC-AxC
        x2 -= x1 // A
        y2 -= y1
        x3 -= x1 // B
        y3 -= y1
        x4 -= x1 // C
        y4 -= y1

        val AvB = x2 * y3 - x3 * y2
        val AvC = x2 * y4 - x4 * y2

        // online
        if (AvB == 0f && AvC == 0f) {
            if (x2 != 0f) {
                return x4 * x3 <= 0 || x3 * x2 >= 0 && if (x2 > 0) x3 <= x2 || x4 <= x2 else x3 >= x2 || x4 >= x2
            }
            if (y2 != 0f) {
                return y4 * y3 <= 0 || y3 * y2 >= 0 && if (y2 > 0) y3 <= y2 || y4 <= y2 else y3 >= y2 || y4 >= y2
            }
            return false
        }

        val BvC = x3 * y4 - x4 * y3
        return AvB * AvC <= 0 && BvC * (AvB + BvC - AvC) <= 0
    }

    /**
     * Returns true if the specified line segment intersects the specified rectangle.
     */
    fun lineIntersectsRect(x1: Float, y1: Float, x2: Float, y2: Float,
                           rx: Float, ry: Float, rw: Float, rh: Float): Boolean {
        val rr = rx + rw
        val rb = ry + rh
        return rx <= x1 && x1 <= rr && ry <= y1 && y1 <= rb
                || rx <= x2 && x2 <= rr && ry <= y2 && y2 <= rb
                || linesIntersect(rx, ry, rr, rb, x1, y1, x2, y2)
                || linesIntersect(rr, ry, rx, rb, x1, y1, x2, y2)
    }

    /**
     * Returns the square of the distance from the specified point to the specified line.
     */
    fun pointLineDistSq(px: Float, py: Float,
                        x1: Float, y1: Float, x2: Float, y2: Float): Float {
        var px = px
        var py = py
        var x2 = x2
        var y2 = y2
        x2 -= x1
        y2 -= y1
        px -= x1
        py -= y1
        val s = px * y2 - py * x2
        return s * s / (x2 * x2 + y2 * y2)
    }

    /**
     * Returns the distance from the specified point to the specified line.
     */
    fun pointLineDist(px: Float, py: Float, x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return FloatMath.sqrt(pointLineDistSq(px, py, x1, y1, x2, y2))
    }

    /**
     * Returns the square of the distance between the specified point and the specified line
     * segment.
     */
    fun pointSegDistSq(px: Float, py: Float,
                       x1: Float, y1: Float, x2: Float, y2: Float): Float {
        var px = px
        var py = py
        var x2 = x2
        var y2 = y2
        // A = (x2 - x1, y2 - y1)
        // P = (px - x1, py - y1)
        x2 -= x1 // A = (x2, y2)
        y2 -= y1
        px -= x1 // P = (px, py)
        py -= y1
        var dist: Float
        if (px * x2 + py * y2 <= 0.0) { // P*A
            dist = px * px + py * py
        } else {
            px = x2 - px // P = A - P = (x2 - px, y2 - py)
            py = y2 - py
            if (px * x2 + py * y2 <= 0.0) { // P*A
                dist = px * px + py * py
            } else {
                dist = px * y2 - py * x2
                dist = dist * dist / (x2 * x2 + y2 * y2) // pxA/|A|
            }
        }
        if (dist < 0) {
            dist = 0f
        }
        return dist
    }

    /**
     * Returns the distance between the specified point and the specified line segment.
     */
    fun pointSegDist(px: Float, py: Float, x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return FloatMath.sqrt(pointSegDistSq(px, py, x1, y1, x2, y2))
    }

    /**
     * Returns an indicator of where the specified point (px,py) lies with respect to the line
     * segment from (x1,y1) to (x2,y2).

     * See http://download.oracle.com/javase/6/docs/api/java/awt/geom/Line2D.html
     */
    fun relativeCCW(px: Float, py: Float, x1: Float, y1: Float, x2: Float, y2: Float): Int {
        var px = px
        var py = py
        var x2 = x2
        var y2 = y2
        // A = (x2-x1, y2-y1)
        // P = (px-x1, py-y1)
        x2 -= x1
        y2 -= y1
        px -= x1
        py -= y1
        var t = px * y2 - py * x2 // PxA
        if (t == 0f) {
            t = px * x2 + py * y2 // P*A
            if (t > 0f) {
                px -= x2 // B-A
                py -= y2
                t = px * x2 + py * y2 // (P-A)*A
                if (t < 0f) {
                    t = 0f
                }
            }
        }
        return if (t < 0f) -1 else if (t > 0f) 1 else 0
    }
}
