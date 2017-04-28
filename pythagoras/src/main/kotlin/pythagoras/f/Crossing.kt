//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

/**
 * Internal utility methods for computing intersections and containment.
 */
internal object Crossing {
    /** Return value indicating that a crossing was found.  */
    val CROSSING = 255

    /** Return value indicating the crossing result is unknown.  */
    val UNKNOWN = 254

    /**
     * Solves quadratic equation

     * @param eqn the coefficients of the equation
     * *
     * @param res the roots of the equation
     * *
     * @return a number of roots
     */
    fun solveQuad(eqn: FloatArray, res: FloatArray): Int {
        val a = eqn[2]
        val b = eqn[1]
        val c = eqn[0]
        var rc = 0
        if (a == 0f) {
            if (b == 0f) {
                return -1
            }
            res[rc++] = -c / b
        } else {
            var d = b * b - 4f * a * c
            // d < 0f
            if (d < 0f) {
                return 0
            }
            d = FloatMath.sqrt(d)
            res[rc++] = (-b + d) / (a * 2f)
            // d != 0f
            if (d != 0f) {
                res[rc++] = (-b - d) / (a * 2f)
            }
        }
        return fixRoots(res, rc)
    }

    /**
     * Solves cubic equation

     * @param eqn the coefficients of the equation
     * *
     * @param res the roots of the equation
     * *
     * @return a number of roots
     */
    fun solveCubic(eqn: FloatArray, res: FloatArray): Int {
        val d = eqn[3]
        if (d == 0f) {
            return solveQuad(eqn, res)
        }
        val a = eqn[2] / d
        val b = eqn[1] / d
        val c = eqn[0] / d
        var rc = 0

        val Q = (a * a - 3f * b) / 9f
        val R = (2f * a * a * a - 9f * a * b + 27f * c) / 54f
        val Q3 = Q * Q * Q
        val R2 = R * R
        val n = -a / 3f

        if (R2 < Q3) {
            val t = FloatMath.acos(R / FloatMath.sqrt(Q3)) / 3f
            val p = 2f * FloatMath.PI / 3f
            val m = -2f * FloatMath.sqrt(Q)
            res[rc++] = m * FloatMath.cos(t) + n
            res[rc++] = m * FloatMath.cos(t + p) + n
            res[rc++] = m * FloatMath.cos(t - p) + n
        } else {
            // Debug.println("R2 >= Q3 (" + R2 + "/" + Q3 + ")");
            var A = FloatMath.pow(Math.abs(R) + FloatMath.sqrt(R2 - Q3), 1f / 3f)
            if (R > 0f) {
                A = -A
            }
            // if (A == 0f) {
            if (-ROOT_DELTA < A && A < ROOT_DELTA) {
                res[rc++] = n
            } else {
                val B = Q / A
                res[rc++] = A + B + n
                // if (R2 == Q3) {
                val delta = R2 - Q3
                if (-ROOT_DELTA < delta && delta < ROOT_DELTA) {
                    res[rc++] = -(A + B) / 2f + n
                }
            }

        }
        return fixRoots(res, rc)
    }

    /**
     * Excludes double roots. Roots are double if they lies enough close with each other.

     * @param res the roots
     * *
     * @param rc the roots count
     * *
     * @return new roots count
     */
    protected fun fixRoots(res: FloatArray, rc: Int): Int {
        var tc = 0
        for (i in 0..rc - 1) {
            out@ run {
                for (j in i + 1..rc - 1) {
                    if (isZero(res[i] - res[j])) {
                        break@out
                    }
                }
                res[tc++] = res[i]
            }
        }
        return tc
    }

    /**
     * QuadCurve class provides basic functionality to find curve crossing and calculating bounds
     */
    class QuadCurve(x1: Float, y1: Float, cx: Float, cy: Float, x2: Float, y2: Float) {
        internal var ax: Float = 0.toFloat()
        internal var ay: Float = 0.toFloat()
        internal var bx: Float = 0.toFloat()
        internal var by: Float = 0.toFloat()
        internal var Ax: Float = 0.toFloat()
        internal var Ay: Float = 0.toFloat()
        internal var Bx: Float = 0.toFloat()
        internal var By: Float = 0.toFloat()

        init {
            ax = x2 - x1
            ay = y2 - y1
            bx = cx - x1
            by = cy - y1

            Bx = bx + bx // Bx = 2f * bx
            Ax = ax - Bx // Ax = ax - 2f * bx

            By = by + by // By = 2f * by
            Ay = ay - By // Ay = ay - 2f * by
        }

        fun cross(res: FloatArray, rc: Int, py1: Float, py2: Float): Int {
            var cross = 0

            for (i in 0..rc - 1) {
                val t = res[i]

                // CURVE-OUTSIDE
                if (t < -DELTA || t > 1 + DELTA) {
                    continue
                }
                // CURVE-START
                if (t < DELTA) {
                    if (py1 < 0f && (if (bx != 0f) bx else ax - bx) < 0f) {
                        cross--
                    }
                    continue
                }
                // CURVE-END
                if (t > 1 - DELTA) {
                    if (py1 < ay && (if (ax != bx) ax - bx else bx) > 0f) {
                        cross++
                    }
                    continue
                }
                // CURVE-INSIDE
                val ry = t * (t * Ay + By)
                // ry = t * t * Ay + t * By
                if (ry > py2) {
                    val rxt = t * Ax + bx
                    // rxt = 2f * t * Ax + Bx = 2f * t * Ax + 2f * bx
                    if (rxt > -DELTA && rxt < DELTA) {
                        continue
                    }
                    cross += if (rxt > 0f) 1 else -1
                }
            } // for

            return cross
        }

        fun solvePoint(res: FloatArray, px: Float): Int {
            val eqn = floatArrayOf(-px, Bx, Ax)
            return solveQuad(eqn, res)
        }

        fun solveExtreme(res: FloatArray): Int {
            var rc = 0
            if (Ax != 0f) {
                res[rc++] = -Bx / (Ax + Ax)
            }
            if (Ay != 0f) {
                res[rc++] = -By / (Ay + Ay)
            }
            return rc
        }

        fun addBound(bound: FloatArray, bc: Int, res: FloatArray, rc: Int, minX: Float, maxX: Float,
                     changeId: Boolean, id: Int): Int {
            var bc = bc
            var id = id
            for (i in 0..rc - 1) {
                val t = res[i]
                if (t > -DELTA && t < 1 + DELTA) {
                    val rx = t * (t * Ax + Bx)
                    if (minX <= rx && rx <= maxX) {
                        bound[bc++] = t
                        bound[bc++] = rx
                        bound[bc++] = t * (t * Ay + By)
                        bound[bc++] = id.toFloat()
                        if (changeId) {
                            id++
                        }
                    }
                }
            }
            return bc
        }
    }

    /** CubicCurve helper for finding curve crossing and calculating bounds.  */
    class CubicCurveH(x1: Float, y1: Float, cx1: Float, cy1: Float, cx2: Float, cy2: Float,
                      x2: Float, y2: Float) {
        internal var ax: Float = 0.toFloat()
        internal var ay: Float = 0.toFloat()
        internal var bx: Float = 0.toFloat()
        internal var by: Float = 0.toFloat()
        internal var cx: Float = 0.toFloat()
        internal var cy: Float = 0.toFloat()
        internal var Ax: Float = 0.toFloat()
        internal var Ay: Float = 0.toFloat()
        internal var Bx: Float = 0.toFloat()
        internal var By: Float = 0.toFloat()
        internal var Cx: Float = 0.toFloat()
        internal var Cy: Float = 0.toFloat()
        internal var Ax3: Float = 0.toFloat()
        internal var Bx2: Float = 0.toFloat()

        init {
            ax = x2 - x1
            ay = y2 - y1
            bx = cx1 - x1
            by = cy1 - y1
            cx = cx2 - x1
            cy = cy2 - y1

            Cx = bx + bx + bx // Cx = 3f * bx
            Bx = cx + cx + cx - Cx - Cx // Bx = 3f * cx - 6f * bx
            Ax = ax - Bx - Cx // Ax = ax - 3f * cx + 3f * bx

            Cy = by + by + by // Cy = 3f * by
            By = cy + cy + cy - Cy - Cy // By = 3f * cy - 6f * by
            Ay = ay - By - Cy // Ay = ay - 3f * cy + 3f * by

            Ax3 = Ax + Ax + Ax
            Bx2 = Bx + Bx
        }

        fun cross(res: FloatArray, rc: Int, py1: Float, py2: Float): Int {
            var cross = 0
            for (i in 0..rc - 1) {
                val t = res[i]

                // CURVE-OUTSIDE
                if (t < -DELTA || t > 1 + DELTA) {
                    continue
                }
                // CURVE-START
                if (t < DELTA) {
                    if (py1 < 0f && (if (bx != 0f) bx else if (cx != bx) cx - bx else ax - cx) < 0f) {
                        cross--
                    }
                    continue
                }
                // CURVE-END
                if (t > 1 - DELTA) {
                    if (py1 < ay && (if (ax != cx) ax - cx else if (cx != bx) cx - bx else bx) > 0f) {
                        cross++
                    }
                    continue
                }
                // CURVE-INSIDE
                val ry = t * (t * (t * Ay + By) + Cy)
                // ry = t * t * t * Ay + t * t * By + t * Cy
                if (ry > py2) {
                    var rxt = t * (t * Ax3 + Bx2) + Cx
                    // rxt = 3f * t * t * Ax + 2f * t * Bx + Cx
                    if (rxt > -DELTA && rxt < DELTA) {
                        rxt = t * (Ax3 + Ax3) + Bx2
                        // rxt = 6f * t * Ax + 2f * Bx
                        if (rxt < -DELTA || rxt > DELTA) {
                            // Inflection point
                            continue
                        }
                        rxt = ax
                    }
                    cross += if (rxt > 0f) 1 else -1
                }
            } // for

            return cross
        }

        fun solvePoint(res: FloatArray, px: Float): Int {
            val eqn = floatArrayOf(-px, Cx, Bx, Ax)
            return solveCubic(eqn, res)
        }

        fun solveExtremeX(res: FloatArray): Int {
            val eqn = floatArrayOf(Cx, Bx2, Ax3)
            return solveQuad(eqn, res)
        }

        fun solveExtremeY(res: FloatArray): Int {
            val eqn = floatArrayOf(Cy, By + By, Ay + Ay + Ay)
            return solveQuad(eqn, res)
        }

        fun addBound(bound: FloatArray, bc: Int, res: FloatArray, rc: Int, minX: Float, maxX: Float,
                     changeId: Boolean, id: Int): Int {
            var bc = bc
            var id = id
            for (i in 0..rc - 1) {
                val t = res[i]
                if (t > -DELTA && t < 1 + DELTA) {
                    val rx = t * (t * (t * Ax + Bx) + Cx)
                    if (minX <= rx && rx <= maxX) {
                        bound[bc++] = t
                        bound[bc++] = rx
                        bound[bc++] = t * (t * (t * Ay + By) + Cy)
                        bound[bc++] = id.toFloat()
                        if (changeId) {
                            id++
                        }
                    }
                }
            }
            return bc
        }
    }

    /**
     * Returns how many times ray from point (x,y) cross line.
     */
    fun crossLine(x1: Float, y1: Float, x2: Float, y2: Float, x: Float, y: Float): Int {
        // LEFT/RIGHT/UP/EMPTY
        if (x < x1 && x < x2 || x > x1 && x > x2 || y > y1 && y > y2 || x1 == x2) {
            return 0
        }

        // DOWN
        if (y < y1 && y < y2) {
        } else {
            // INSIDE
            if ((y2 - y1) * (x - x1) / (x2 - x1) <= y - y1) {
                // INSIDE-UP
                return 0
            }
        }

        // START
        if (x == x1) {
            return if (x1 < x2) 0 else -1
        }

        // END
        if (x == x2) {
            return if (x1 < x2) 1 else 0
        }

        // INSIDE-DOWN
        return if (x1 < x2) 1 else -1
    }

    /**
     * Returns how many times ray from point (x,y) cross quard curve
     */
    fun crossQuad(x1: Float, y1: Float, cx: Float, cy: Float, x2: Float, y2: Float,
                  x: Float, y: Float): Int {
        // LEFT/RIGHT/UP/EMPTY
        if (x < x1 && x < cx && x < x2 || x > x1 && x > cx && x > x2
                || y > y1 && y > cy && y > y2 || x1 == cx && cx == x2) {
            return 0
        }

        // DOWN
        if (y < y1 && y < cy && y < y2 && x != x1 && x != x2) {
            if (x1 < x2) {
                return if (x1 < x && x < x2) 1 else 0
            }
            return if (x2 < x && x < x1) -1 else 0
        }

        // INSIDE
        val c = QuadCurve(x1, y1, cx, cy, x2, y2)
        val px = x - x1
        val py = y - y1
        val res = FloatArray(3)
        val rc = c.solvePoint(res, px)
        return c.cross(res, rc, py, py)
    }

    /**
     * Returns how many times ray from point (x,y) cross cubic curve
     */
    fun crossCubic(x1: Float, y1: Float, cx1: Float, cy1: Float, cx2: Float,
                   cy2: Float, x2: Float, y2: Float, x: Float, y: Float): Int {
        // LEFT/RIGHT/UP/EMPTY
        if (x < x1 && x < cx1 && x < cx2 && x < x2 || x > x1 && x > cx1 && x > cx2 && x > x2
                || y > y1 && y > cy1 && y > cy2 && y > y2
                || x1 == cx1 && cx1 == cx2 && cx2 == x2) {
            return 0
        }

        // DOWN
        if (y < y1 && y < cy1 && y < cy2 && y < y2 && x != x1 && x != x2) {
            if (x1 < x2) {
                return if (x1 < x && x < x2) 1 else 0
            }
            return if (x2 < x && x < x1) -1 else 0
        }

        // INSIDE
        val c = CubicCurveH(x1, y1, cx1, cy1, cx2, cy2, x2, y2)
        val px = x - x1
        val py = y - y1
        val res = FloatArray(3)
        val rc = c.solvePoint(res, px)
        return c.cross(res, rc, py, py)
    }

    /**
     * Returns how many times ray from point (x,y) cross path
     */
    fun crossPath(p: PathIterator, x: Float, y: Float): Int {
        var cross = 0
        var mx: Float
        var my: Float
        var cx: Float
        var cy: Float
        cy = 0f
        cx = cy
        my = cx
        mx = my
        val coords = FloatArray(6)

        while (!p.isDone) {
            when (p.currentSegment(coords)) {
                PathIterator.SEG_MOVETO -> {
                    if (cx != mx || cy != my) {
                        cross += crossLine(cx, cy, mx, my, x, y)
                    }
                    cx = coords[0]
                    mx = cx
                    cy = coords[1]
                    my = cy
                }
                PathIterator.SEG_LINETO -> cross += crossLine(cx, cy, cx = coords[0], cy = coords[1], x, y)
                PathIterator.SEG_QUADTO -> cross += crossQuad(cx, cy, coords[0], coords[1], cx = coords[2], cy = coords[3], x,
                        y)
                PathIterator.SEG_CUBICTO -> cross += crossCubic(cx, cy, coords[0], coords[1], coords[2], coords[3],
                        cx = coords[4], cy = coords[5], x, y)
                PathIterator.SEG_CLOSE -> if (cy != my || cx != mx) {
                    cross += crossLine(cx, cy, cx = mx, cy = my, x, y)
                }
            }

            // checks if the point (x,y) is the vertex of shape with PathIterator p
            if (x == cx && y == cy) {
                cross = 0
                cy = my
                break
            }
            p.next()
        }
        if (cy != my) {
            cross += crossLine(cx, cy, mx, my, x, y)
        }
        return cross
    }

    /**
     * Returns how many times a ray from point (x,y) crosses a shape.
     */
    fun crossShape(s: IShape, x: Float, y: Float): Int {
        if (!s.bounds().contains(x, y)) {
            return 0
        }
        return crossPath(s.pathIterator(null), x, y)
    }

    /**
     * Returns true if value is close enough to zero.
     */
    fun isZero(`val`: Float): Boolean {
        return -DELTA < `val` && `val` < DELTA
    }

    /**
     * Returns how many times rectangle stripe cross line or the are intersect
     */
    fun intersectLine(x1: Float, y1: Float, x2: Float, y2: Float, rx1: Float,
                      ry1: Float, rx2: Float, ry2: Float): Int {
        // LEFT/RIGHT/UP
        if (rx2 < x1 && rx2 < x2 || rx1 > x1 && rx1 > x2 || ry1 > y1 && ry1 > y2) {
            return 0
        }

        // DOWN
        if (ry2 < y1 && ry2 < y2) {

        } else {
            // INSIDE
            if (x1 == x2) {
                return CROSSING
            }

            // Build bound
            val bx1: Float
            val bx2: Float
            if (x1 < x2) {
                bx1 = if (x1 < rx1) rx1 else x1
                bx2 = if (x2 < rx2) x2 else rx2
            } else {
                bx1 = if (x2 < rx1) rx1 else x2
                bx2 = if (x1 < rx2) x1 else rx2
            }
            val k = (y2 - y1) / (x2 - x1)
            val by1 = k * (bx1 - x1) + y1
            val by2 = k * (bx2 - x1) + y1

            // BOUND-UP
            if (by1 < ry1 && by2 < ry1) {
                return 0
            }

            // BOUND-DOWN
            if (by1 > ry2 && by2 > ry2) {
            } else {
                return CROSSING
            }
        }

        // EMPTY
        if (x1 == x2) {
            return 0
        }

        // CURVE-START
        if (rx1 == x1) {
            return if (x1 < x2) 0 else -1
        }

        // CURVE-END
        if (rx1 == x2) {
            return if (x1 < x2) 1 else 0
        }

        if (x1 < x2) {
            return if (x1 < rx1 && rx1 < x2) 1 else 0
        }
        return if (x2 < rx1 && rx1 < x1) -1 else 0
    }

    /**
     * Returns how many times rectangle stripe cross quad curve or the are
     * intersect
     */
    fun intersectQuad(x1: Float, y1: Float, cx: Float, cy: Float, x2: Float,
                      y2: Float, rx1: Float, ry1: Float, rx2: Float, ry2: Float): Int {
        // LEFT/RIGHT/UP ------------------------------------------------------
        if (rx2 < x1 && rx2 < cx && rx2 < x2 || rx1 > x1 && rx1 > cx && rx1 > x2 ||
                ry1 > y1 && ry1 > cy && ry1 > y2) {
            return 0
        }

        // DOWN ---------------------------------------------------------------
        if (ry2 < y1 && ry2 < cy && ry2 < y2 && rx1 != x1 && rx1 != x2) {
            if (x1 < x2) {
                return if (x1 < rx1 && rx1 < x2) 1 else 0
            }
            return if (x2 < rx1 && rx1 < x1) -1 else 0
        }

        // INSIDE -------------------------------------------------------------
        val c = QuadCurve(x1, y1, cx, cy, x2, y2)
        val px1 = rx1 - x1
        val py1 = ry1 - y1
        val px2 = rx2 - x1
        val py2 = ry2 - y1

        val res1 = FloatArray(3)
        val res2 = FloatArray(3)
        val rc1 = c.solvePoint(res1, px1)
        var rc2 = c.solvePoint(res2, px2)

        // INSIDE-LEFT/RIGHT
        if (rc1 == 0 && rc2 == 0) {
            return 0
        }

        // Build bound --------------------------------------------------------
        val minX = px1 - DELTA
        val maxX = px2 + DELTA
        val bound = FloatArray(28)
        var bc = 0
        // Add roots
        bc = c.addBound(bound, bc, res1, rc1, minX, maxX, false, 0)
        bc = c.addBound(bound, bc, res2, rc2, minX, maxX, false, 1)
        // Add extremal points
        rc2 = c.solveExtreme(res2)
        bc = c.addBound(bound, bc, res2, rc2, minX, maxX, true, 2)
        // Add start and end
        if (rx1 < x1 && x1 < rx2) {
            bound[bc++] = 0f
            bound[bc++] = 0f
            bound[bc++] = 0f
            bound[bc++] = 4f
        }
        if (rx1 < x2 && x2 < rx2) {
            bound[bc++] = 1f
            bound[bc++] = c.ax
            bound[bc++] = c.ay
            bound[bc++] = 5f
        }
        // End build bound ----------------------------------------------------

        val cross = crossBound(bound, bc, py1, py2)
        if (cross != UNKNOWN) {
            return cross
        }
        return c.cross(res1, rc1, py1, py2)
    }

    /**
     * Returns how many times rectangle stripe cross cubic curve or the are
     * intersect
     */
    fun intersectCubic(x1: Float, y1: Float, cx1: Float, cy1: Float,
                       cx2: Float, cy2: Float, x2: Float, y2: Float,
                       rx1: Float, ry1: Float, rx2: Float, ry2: Float): Int {
        // LEFT/RIGHT/UP
        if (rx2 < x1 && rx2 < cx1 && rx2 < cx2 && rx2 < x2
                || rx1 > x1 && rx1 > cx1 && rx1 > cx2 && rx1 > x2
                || ry1 > y1 && ry1 > cy1 && ry1 > cy2 && ry1 > y2) {
            return 0
        }

        // DOWN
        if (ry2 < y1 && ry2 < cy1 && ry2 < cy2 && ry2 < y2 && rx1 != x1 && rx1 != x2) {
            if (x1 < x2) {
                return if (x1 < rx1 && rx1 < x2) 1 else 0
            }
            return if (x2 < rx1 && rx1 < x1) -1 else 0
        }

        // INSIDE
        val c = CubicCurveH(x1, y1, cx1, cy1, cx2, cy2, x2, y2)
        val px1 = rx1 - x1
        val py1 = ry1 - y1
        val px2 = rx2 - x1
        val py2 = ry2 - y1

        val res1 = FloatArray(3)
        val res2 = FloatArray(3)
        val rc1 = c.solvePoint(res1, px1)
        var rc2 = c.solvePoint(res2, px2)

        // LEFT/RIGHT
        if (rc1 == 0 && rc2 == 0) {
            return 0
        }

        val minX = px1 - DELTA
        val maxX = px2 + DELTA

        // Build bound --------------------------------------------------------
        val bound = FloatArray(40)
        var bc = 0
        // Add roots
        bc = c.addBound(bound, bc, res1, rc1, minX, maxX, false, 0)
        bc = c.addBound(bound, bc, res2, rc2, minX, maxX, false, 1)
        // Add extremal points
        rc2 = c.solveExtremeX(res2)
        bc = c.addBound(bound, bc, res2, rc2, minX, maxX, true, 2)
        rc2 = c.solveExtremeY(res2)
        bc = c.addBound(bound, bc, res2, rc2, minX, maxX, true, 4)
        // Add start and end
        if (rx1 < x1 && x1 < rx2) {
            bound[bc++] = 0f
            bound[bc++] = 0f
            bound[bc++] = 0f
            bound[bc++] = 6f
        }
        if (rx1 < x2 && x2 < rx2) {
            bound[bc++] = 1f
            bound[bc++] = c.ax
            bound[bc++] = c.ay
            bound[bc++] = 7f
        }
        // End build bound ----------------------------------------------------

        val cross = crossBound(bound, bc, py1, py2)
        if (cross != UNKNOWN) {
            return cross
        }
        return c.cross(res1, rc1, py1, py2)
    }

    /**
     * Returns how many times rectangle stripe cross path or the are intersect
     */
    fun intersectPath(p: PathIterator, x: Float, y: Float, w: Float, h: Float): Int {
        var cross = 0
        var count: Int
        var mx: Float
        var my: Float
        var cx: Float
        var cy: Float
        cy = 0f
        cx = cy
        my = cx
        mx = my
        val coords = FloatArray(6)

        val rx1 = x
        val ry1 = y
        val rx2 = x + w
        val ry2 = y + h

        while (!p.isDone) {
            count = 0
            when (p.currentSegment(coords)) {
                PathIterator.SEG_MOVETO -> {
                    if (cx != mx || cy != my) {
                        count = intersectLine(cx, cy, mx, my, rx1, ry1, rx2, ry2)
                    }
                    cx = coords[0]
                    mx = cx
                    cy = coords[1]
                    my = cy
                }
                PathIterator.SEG_LINETO -> count = intersectLine(cx, cy, cx = coords[0], cy = coords[1], rx1, ry1, rx2, ry2)
                PathIterator.SEG_QUADTO -> count = intersectQuad(cx, cy, coords[0], coords[1], cx = coords[2], cy = coords[3],
                        rx1, ry1, rx2, ry2)
                PathIterator.SEG_CUBICTO -> count = intersectCubic(cx, cy, coords[0], coords[1], coords[2], coords[3],
                        cx = coords[4], cy = coords[5], rx1, ry1, rx2, ry2)
                PathIterator.SEG_CLOSE -> {
                    if (cy != my || cx != mx) {
                        count = intersectLine(cx, cy, mx, my, rx1, ry1, rx2, ry2)
                    }
                    cx = mx
                    cy = my
                }
            }
            if (count == CROSSING) {
                return CROSSING
            }
            cross += count
            p.next()
        }
        if (cy != my) {
            count = intersectLine(cx, cy, mx, my, rx1, ry1, rx2, ry2)
            if (count == CROSSING) {
                return CROSSING
            }
            cross += count
        }
        return cross
    }

    /**
     * Returns how many times rectangle stripe cross shape or the are intersect
     */
    fun intersectShape(s: IShape, x: Float, y: Float, w: Float, h: Float): Int {
        if (!s.bounds().intersects(x, y, w, h)) {
            return 0
        }
        return intersectPath(s.pathIterator(null), x, y, w, h)
    }

    /**
     * Returns true if cross count correspond inside location for non zero path
     * rule
     */
    fun isInsideNonZero(cross: Int): Boolean {
        return cross != 0
    }

    /**
     * Returns true if cross count correspond inside location for even-odd path
     * rule
     */
    fun isInsideEvenOdd(cross: Int): Boolean {
        return cross and 1 != 0
    }

    /**
     * Sorts a bound array.
     */
    protected fun sortBound(bound: FloatArray, bc: Int) {
        var i = 0
        while (i < bc - 4) {
            var k = i
            var j = i + 4
            while (j < bc) {
                if (bound[k] > bound[j]) {
                    k = j
                }
                j += 4
            }
            if (k != i) {
                var tmp = bound[i]
                bound[i] = bound[k]
                bound[k] = tmp
                tmp = bound[i + 1]
                bound[i + 1] = bound[k + 1]
                bound[k + 1] = tmp
                tmp = bound[i + 2]
                bound[i + 2] = bound[k + 2]
                bound[k + 2] = tmp
                tmp = bound[i + 3]
                bound[i + 3] = bound[k + 3]
                bound[k + 3] = tmp
            }
            i += 4
        }
    }

    /**
     * Returns whether bounds intersect a rectangle or not.
     */
    protected fun crossBound(bound: FloatArray, bc: Int, py1: Float, py2: Float): Int {
        // LEFT/RIGHT
        if (bc == 0) {
            return 0
        }

        // Check Y coordinate
        var up = 0
        var down = 0
        run {
            var i = 2
            while (i < bc) {
                if (bound[i] < py1) {
                    up++
                    i += 4
                    continue
                }
                if (bound[i] > py2) {
                    down++
                    i += 4
                    continue
                }
                return CROSSING
                i += 4
            }
        }

        // UP
        if (down == 0) {
            return 0
        }

        if (up != 0) {
            // bc >= 2
            sortBound(bound, bc)
            var sign = bound[2] > py2
            var i = 6
            while (i < bc) {
                val sign2 = bound[i] > py2
                if (sign != sign2 && bound[i + 1] != bound[i - 3]) {
                    return CROSSING
                }
                sign = sign2
                i += 4
            }
        }
        return UNKNOWN
    }

    /** Allowable tolerance for bounds comparison  */
    protected val DELTA = 1E-5f

    /** If roots have distance less then `ROOT_DELTA` they are double  */
    protected val ROOT_DELTA = 1E-10f
}
