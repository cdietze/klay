//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

/**
 * Various geometry utility methods.
 */
object GeometryUtil {
    val EPSILON = FloatMath.pow(10f, -14f)

    fun intersectLinesWithParams(x1: Float, y1: Float, x2: Float, y2: Float,
                                 x3: Float, y3: Float, x4: Float, y4: Float,
                                 params: FloatArray): Int {
        val dx = x4 - x3
        val dy = y4 - y3
        val d = dx * (y2 - y1) - dy * (x2 - x1)
        // float comparison
        if (Math.abs(d) < EPSILON) {
            return 0
        }

        params[0] = (-dx * (y1 - y3) + dy * (x1 - x3)) / d

        if (dx != 0f) {
            params[1] = (line(params[0], x1, x2) - x3) / dx
        } else if (dy != 0f) {
            params[1] = (line(params[0], y1, y2) - y3) / dy
        } else {
            params[1] = 0f
        }

        if (params[0] >= 0 && params[0] <= 1 && params[1] >= 0 && params[1] <= 1) {
            return 1
        }

        return 0
    }

    /**
     * Checks whether line (x1, y1) - (x2, y2) and line (x3, y3) - (x4, y4) intersect. If lines
     * intersect then the result parameters are saved to point array. The size of `point`
     * must be at least 2.

     * @return 1 if two lines intersect in the defined interval, otherwise 0.
     */
    fun intersectLines(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float,
                       x4: Float, y4: Float, point: FloatArray): Int {
        val A1 = -(y2 - y1)
        val B1 = x2 - x1
        val C1 = x1 * y2 - x2 * y1
        val A2 = -(y4 - y3)
        val B2 = x4 - x3
        val C2 = x3 * y4 - x4 * y3
        val coefParallel = A1 * B2 - A2 * B1
        // float comparison
        if (x3 == x4 && y3 == y4 && A1 * x3 + B1 * y3 + C1 == 0f && x3 >= Math.min(x1, x2) &&
                x3 <= Math.max(x1, x2) && y3 >= Math.min(y1, y2) && y3 <= Math.max(y1, y2)) {
            return 1
        }
        if (Math.abs(coefParallel) < EPSILON) {
            return 0
        }
        point[0] = (B1 * C2 - B2 * C1) / coefParallel
        point[1] = (A2 * C1 - A1 * C2) / coefParallel
        if (point[0] >= Math.min(x1, x2) && point[0] >= Math.min(x3, x4) &&
                point[0] <= Math.max(x1, x2) && point[0] <= Math.max(x3, x4) &&
                point[1] >= Math.min(y1, y2) && point[1] >= Math.min(y3, y4) &&
                point[1] <= Math.max(y1, y2) && point[1] <= Math.max(y3, y4)) {
            return 1
        }
        return 0
    }

    /**
     * Checks whether there is intersection of the line (x1, y1) - (x2, y2) and the quad curve
     * (qx1, qy1) - (qx2, qy2) - (qx3, qy3). The parameters of the intersection area saved to
     * `params`. Therefore `params` must be of length at least 4.

     * @return the number of roots that lie in the defined interval.
     */
    fun intersectLineAndQuad(x1: Float, y1: Float, x2: Float, y2: Float,
                             qx1: Float, qy1: Float, qx2: Float, qy2: Float,
                             qx3: Float, qy3: Float, params: FloatArray): Int {
        val eqn = FloatArray(3)
        val t = FloatArray(2)
        val s = FloatArray(2)
        val dy = y2 - y1
        val dx = x2 - x1
        var quantity = 0
        var count = 0

        eqn[0] = dy * (qx1 - x1) - dx * (qy1 - y1)
        eqn[1] = 2f * dy * (qx2 - qx1) - 2f * dx * (qy2 - qy1)
        eqn[2] = dy * (qx1 - 2 * qx2 + qx3) - dx * (qy1 - 2 * qy2 + qy3)

        if ((count = Crossing.solveQuad(eqn, t)) == 0) {
            return 0
        }

        for (i in 0..count - 1) {
            if (dx != 0f) {
                s[i] = (quad(t[i], qx1, qx2, qx3) - x1) / dx
            } else if (dy != 0f) {
                s[i] = (quad(t[i], qy1, qy2, qy3) - y1) / dy
            } else {
                s[i] = 0f
            }
            if (t[i] >= 0 && t[i] <= 1 && s[i] >= 0 && s[i] <= 1) {
                params[2 * quantity] = t[i]
                params[2 * quantity + 1] = s[i]
                ++quantity
            }
        }

        return quantity
    }

    /**
     * Checks whether the line (x1, y1) - (x2, y2) and the cubic curve (cx1, cy1) - (cx2, cy2) -
     * (cx3, cy3) - (cx4, cy4) intersect. The points of intersection are saved to `points`.
     * Therefore `points` must be of length at least 6.

     * @return the numbers of roots that lie in the defined interval.
     */
    fun intersectLineAndCubic(x1: Float, y1: Float, x2: Float, y2: Float,
                              cx1: Float, cy1: Float, cx2: Float, cy2: Float,
                              cx3: Float, cy3: Float, cx4: Float, cy4: Float,
                              params: FloatArray): Int {
        val eqn = FloatArray(4)
        val t = FloatArray(3)
        val s = FloatArray(3)
        val dy = y2 - y1
        val dx = x2 - x1
        var quantity = 0
        var count = 0

        eqn[0] = (cy1 - y1) * dx + (x1 - cx1) * dy
        eqn[1] = -3f * (cy1 - cy2) * dx + 3f * (cx1 - cx2) * dy
        eqn[2] = (3 * cy1 - 6 * cy2 + 3 * cy3) * dx - (3 * cx1 - 6 * cx2 + 3 * cx3) * dy
        eqn[3] = (-3 * cy1 + 3 * cy2 - 3 * cy3 + cy4) * dx + (3 * cx1 - 3 * cx2 + 3 * cx3 - cx4) * dy

        if ((count = Crossing.solveCubic(eqn, t)) == 0) {
            return 0
        }

        for (i in 0..count - 1) {
            if (dx != 0f) {
                s[i] = (cubic(t[i], cx1, cx2, cx3, cx4) - x1) / dx
            } else if (dy != 0f) {
                s[i] = (cubic(t[i], cy1, cy2, cy3, cy4) - y1) / dy
            } else {
                s[i] = 0f
            }
            if (t[i] >= 0 && t[i] <= 1 && s[i] >= 0 && s[i] <= 1) {
                params[2 * quantity] = t[i]
                params[2 * quantity + 1] = s[i]
                ++quantity
            }
        }

        return quantity
    }

    /**
     * Checks whether two quads (x1, y1) - (x2, y2) - (x3, y3) and (qx1, qy1) - (qx2, qy2) - (qx3,
     * qy3) intersect. The result is saved to `params`. Thus `params` must be of length
     * at least 4.

     * @return the number of roots that lie in the interval.
     */
    fun intersectQuads(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float,
                       qx1: Float, qy1: Float, qx2: Float, qy2: Float, qx3: Float,
                       qy3: Float, params: FloatArray): Int {
        val initParams = FloatArray(2)
        val xCoefs1 = FloatArray(3)
        val yCoefs1 = FloatArray(3)
        val xCoefs2 = FloatArray(3)
        val yCoefs2 = FloatArray(3)
        var quantity = 0

        xCoefs1[0] = x1 - 2 * x2 + x3
        xCoefs1[1] = -2 * x1 + 2 * x2
        xCoefs1[2] = x1

        yCoefs1[0] = y1 - 2 * y2 + y3
        yCoefs1[1] = -2 * y1 + 2 * y2
        yCoefs1[2] = y1

        xCoefs2[0] = qx1 - 2 * qx2 + qx3
        xCoefs2[1] = -2 * qx1 + 2 * qx2
        xCoefs2[2] = qx1

        yCoefs2[0] = qy1 - 2 * qy2 + qy3
        yCoefs2[1] = -2 * qy1 + 2 * qy2
        yCoefs2[2] = qy1

        // initialize params[0] and params[1]
        params[1] = 0.25f
        params[0] = params[1]
        quadNewton(xCoefs1, yCoefs1, xCoefs2, yCoefs2, initParams)
        if (initParams[0] <= 1 && initParams[0] >= 0 && initParams[1] >= 0 && initParams[1] <= 1) {
            params[2 * quantity] = initParams[0]
            params[2 * quantity + 1] = initParams[1]
            ++quantity
        }
        // initialize params
        params[1] = 0.75f
        params[0] = params[1]
        quadNewton(xCoefs1, yCoefs1, xCoefs2, yCoefs2, params)
        if (initParams[0] <= 1 && initParams[0] >= 0 && initParams[1] >= 0 && initParams[1] <= 1) {
            params[2 * quantity] = initParams[0]
            params[2 * quantity + 1] = initParams[1]
            ++quantity
        }

        return quantity
    }

    /**
     * Checks whether the quad (x1, y1) - (x2, y2) - (x3, y3) and the cubic (cx1, cy1) - (cx2, cy2)
     * - (cx3, cy3) - (cx4, cy4) curves intersect. The points of the intersection are saved to
     * `params`. Thus `params` must be of length at least 6.

     * @return the number of intersection points that lie in the interval.
     */
    fun intersectQuadAndCubic(qx1: Float, qy1: Float, qx2: Float, qy2: Float,
                              qx3: Float, qy3: Float, cx1: Float, cy1: Float,
                              cx2: Float, cy2: Float, cx3: Float, cy3: Float,
                              cx4: Float, cy4: Float, params: FloatArray): Int {
        var quantity = 0
        val initParams = FloatArray(3)
        val xCoefs1 = FloatArray(3)
        val yCoefs1 = FloatArray(3)
        val xCoefs2 = FloatArray(4)
        val yCoefs2 = FloatArray(4)
        xCoefs1[0] = qx1 - 2 * qx2 + qx3
        xCoefs1[1] = 2 * qx2 - 2 * qx1
        xCoefs1[2] = qx1

        yCoefs1[0] = qy1 - 2 * qy2 + qy3
        yCoefs1[1] = 2 * qy2 - 2 * qy1
        yCoefs1[2] = qy1

        xCoefs2[0] = -cx1 + 3 * cx2 - 3 * cx3 + cx4
        xCoefs2[1] = 3 * cx1 - 6 * cx2 + 3 * cx3
        xCoefs2[2] = -3 * cx1 + 3 * cx2
        xCoefs2[3] = cx1

        yCoefs2[0] = -cy1 + 3 * cy2 - 3 * cy3 + cy4
        yCoefs2[1] = 3 * cy1 - 6 * cy2 + 3 * cy3
        yCoefs2[2] = -3 * cy1 + 3 * cy2
        yCoefs2[3] = cy1

        // initialize params[0] and params[1]
        params[1] = 0.25f
        params[0] = params[1]
        quadAndCubicNewton(xCoefs1, yCoefs1, xCoefs2, yCoefs2, initParams)
        if (initParams[0] <= 1 && initParams[0] >= 0 && initParams[1] >= 0 && initParams[1] <= 1) {
            params[2 * quantity] = initParams[0]
            params[2 * quantity + 1] = initParams[1]
            ++quantity
        }
        // initialize params
        params[1] = 0.5f
        params[0] = params[1]
        quadAndCubicNewton(xCoefs1, yCoefs1, xCoefs2, yCoefs2, params)
        if (initParams[0] <= 1 && initParams[0] >= 0 && initParams[1] >= 0 && initParams[1] <= 1) {
            params[2 * quantity] = initParams[0]
            params[2 * quantity + 1] = initParams[1]
            ++quantity
        }

        params[1] = 0.75f
        params[0] = params[1]
        quadAndCubicNewton(xCoefs1, yCoefs1, xCoefs2, yCoefs2, params)
        if (initParams[0] <= 1 && initParams[0] >= 0 && initParams[1] >= 0 && initParams[1] <= 1) {
            params[2 * quantity] = initParams[0]
            params[2 * quantity + 1] = initParams[1]
            ++quantity
        }
        return quantity
    }

    /**
     * Checks whether two cubic curves (x1, y1) - (x2, y2) - (x3, y3) - (x4, y4) and (cx1, cy1) -
     * (cx2, cy2) - (cx3, cy3) - (cx4, cy4) intersect. The result is saved to `params`. Thus
     * `params` must be of length at least 6.

     * @return the number of intersection points that lie in the interval.
     */
    fun intersectCubics(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float,
                        x4: Float, y4: Float, cx1: Float, cy1: Float,
                        cx2: Float, cy2: Float, cx3: Float, cy3: Float,
                        cx4: Float, cy4: Float, params: FloatArray): Int {
        var quantity = 0
        val initParams = FloatArray(3)
        val xCoefs1 = FloatArray(4)
        val yCoefs1 = FloatArray(4)
        val xCoefs2 = FloatArray(4)
        val yCoefs2 = FloatArray(4)
        xCoefs1[0] = -x1 + 3 * x2 - 3 * x3 + x4
        xCoefs1[1] = 3 * x1 - 6 * x2 + 3 * x3
        xCoefs1[2] = -3 * x1 + 3 * x2
        xCoefs1[3] = x1

        yCoefs1[0] = -y1 + 3 * y2 - 3 * y3 + y4
        yCoefs1[1] = 3 * y1 - 6 * y2 + 3 * y3
        yCoefs1[2] = -3 * y1 + 3 * y2
        yCoefs1[3] = y1

        xCoefs2[0] = -cx1 + 3 * cx2 - 3 * cx3 + cx4
        xCoefs2[1] = 3 * cx1 - 6 * cx2 + 3 * cx3
        xCoefs2[2] = -3 * cx1 + 3 * cx2
        xCoefs2[3] = cx1

        yCoefs2[0] = -cy1 + 3 * cy2 - 3 * cy3 + cy4
        yCoefs2[1] = 3 * cy1 - 6 * cy2 + 3 * cy3
        yCoefs2[2] = -3 * cy1 + 3 * cy2
        yCoefs2[3] = cy1

        // TODO
        params[1] = 0.25f
        params[0] = params[1]
        cubicNewton(xCoefs1, yCoefs1, xCoefs2, yCoefs2, initParams)
        if (initParams[0] <= 1 && initParams[0] >= 0 && initParams[1] >= 0 && initParams[1] <= 1) {
            params[2 * quantity] = initParams[0]
            params[2 * quantity + 1] = initParams[1]
            ++quantity
        }

        params[1] = 0.5f
        params[0] = params[1]
        cubicNewton(xCoefs1, yCoefs1, xCoefs2, yCoefs2, params)
        if (initParams[0] <= 1 && initParams[0] >= 0 && initParams[1] >= 0 && initParams[1] <= 1) {
            params[2 * quantity] = initParams[0]
            params[2 * quantity + 1] = initParams[1]
            ++quantity
        }

        params[1] = 0.75f
        params[0] = params[1]
        cubicNewton(xCoefs1, yCoefs1, xCoefs2, yCoefs2, params)
        if (initParams[0] <= 1 && initParams[0] >= 0 && initParams[1] >= 0 && initParams[1] <= 1) {
            params[2 * quantity] = initParams[0]
            params[2 * quantity + 1] = initParams[1]
            ++quantity
        }
        return quantity
    }

    fun line(t: Float, x1: Float, x2: Float): Float {
        return x1 * (1f - t) + x2 * t
    }

    fun quad(t: Float, x1: Float, x2: Float, x3: Float): Float {
        return x1 * (1f - t) * (1f - t) + 2f * x2 * t * (1f - t) + x3 * t * t
    }

    fun cubic(t: Float, x1: Float, x2: Float, x3: Float, x4: Float): Float {
        return x1 * (1f - t) * (1f - t) * (1f - t) + 3f * x2 * (1f - t) * (1f - t) * t + 3f * x3 *
                (1f - t) * t * t + x4 * t * t * t
    }

    // x, y - the coordinates of new vertex
    // t0 - ?
    fun subQuad(coef: FloatArray, t0: Float, left: Boolean) {
        if (left) {
            coef[2] = (1 - t0) * coef[0] + t0 * coef[2]
            coef[3] = (1 - t0) * coef[1] + t0 * coef[3]
        } else {
            coef[2] = (1 - t0) * coef[2] + t0 * coef[4]
            coef[3] = (1 - t0) * coef[3] + t0 * coef[5]
        }
    }

    fun subCubic(coef: FloatArray, t0: Float, left: Boolean) {
        if (left) {
            coef[2] = (1 - t0) * coef[0] + t0 * coef[2]
            coef[3] = (1 - t0) * coef[1] + t0 * coef[3]
        } else {
            coef[4] = (1 - t0) * coef[4] + t0 * coef[6]
            coef[5] = (1 - t0) * coef[5] + t0 * coef[7]
        }
    }

    private fun cubicNewton(xCoefs1: FloatArray, yCoefs1: FloatArray,
                            xCoefs2: FloatArray, yCoefs2: FloatArray, params: FloatArray) {
        val t = 0f
        val s = 0f
        var t1 = params[0]
        var s1 = params[1]
        var d: Float
        var dt: Float
        var ds: Float

        while (Math.sqrt(((t - t1) * (t - t1) + (s - s1) * (s - s1)).toDouble()) > EPSILON) {
            d = -(3f * t * t * xCoefs1[0] + 2f * t * xCoefs1[1] + xCoefs1[2]) * (3f * s * s * yCoefs2[0] + 2f * s * yCoefs2[1] + yCoefs2[2]) + (3f * t * t * yCoefs1[0] + 2f * t * yCoefs1[1] + yCoefs1[2]) * (3f * s * s * xCoefs2[0] + 2f * s * xCoefs2[1] + xCoefs2[2])

            dt = (t * t * t * xCoefs1[0] + t * t * xCoefs1[1] + t * xCoefs1[2] + xCoefs1[3] -
                    s * s * s * xCoefs2[0] - s * s * xCoefs2[1] - s * xCoefs2[2] - xCoefs2[3]) * (-3f * s * s * yCoefs2[0] - 2f * s * yCoefs2[1] - yCoefs2[2]) + (t * t * t * yCoefs1[0] + t * t * yCoefs1[1] + t * yCoefs1[2] + yCoefs1[3] -
                    s * s * s * yCoefs2[0] - s * s * yCoefs2[1] - s * yCoefs2[2] - yCoefs2[3]) * (3f * s * s * xCoefs2[0] + 2f * s * xCoefs2[1] + xCoefs2[2])

            ds = (3f * t * t * xCoefs1[0] + 2f * t * xCoefs1[1] + xCoefs1[2]) * (t * t * t * yCoefs1[0] + t * t * yCoefs1[1] + t * yCoefs1[2] + yCoefs1[3] -
                    s * s * s * yCoefs2[0] - s * s * yCoefs2[1] - s * yCoefs2[2] - yCoefs2[3]) - (3f * t * t * yCoefs1[0] + 2f * t * yCoefs1[1] + yCoefs1[2]) * (t * t * t * xCoefs1[0] + t * t * xCoefs1[1] + t * xCoefs1[2] + xCoefs1[3] -
                    s * s * s * xCoefs2[0] - s * s * xCoefs2[1] - s * xCoefs2[2] - xCoefs2[3])

            t1 = t - dt / d
            s1 = s - ds / d
        }
        params[0] = t1
        params[1] = s1
    }

    private fun quadAndCubicNewton(xCoefs1: FloatArray, yCoefs1: FloatArray,
                                   xCoefs2: FloatArray, yCoefs2: FloatArray, params: FloatArray) {
        val t = 0f
        val s = 0f
        var t1 = params[0]
        var s1 = params[1]
        var d: Float
        var dt: Float
        var ds: Float

        while (Math.sqrt(((t - t1) * (t - t1) + (s - s1) * (s - s1)).toDouble()) > EPSILON) {
            d = -(2f * t * xCoefs1[0] + xCoefs1[1]) * (3f * s * s * yCoefs2[0] + 2f * s * yCoefs2[1] + yCoefs2[2]) + (2f * t * yCoefs1[0] + yCoefs1[1]) * (3f * s * s * xCoefs2[0] + 2f * s * xCoefs2[1] + xCoefs2[2])

            dt = (t * t * xCoefs1[0] + t * xCoefs1[1] + xCoefs1[2] + -s * s * s * xCoefs2[0] -
                    s * s * xCoefs2[1] - s * xCoefs2[2] - xCoefs2[3]) * (-3f * s * s * yCoefs2[0] - 2f * s * yCoefs2[1] - yCoefs2[2]) + (t * t * yCoefs1[0] + t * yCoefs1[1] + yCoefs1[2] - s * s * s * yCoefs2[0] -
                    s * s * yCoefs2[1] - s * yCoefs2[2] - yCoefs2[3]) * (3f * s * s * xCoefs2[0] + 2f * s * xCoefs2[1] + xCoefs2[2])

            ds = (2f * t * xCoefs1[0] + xCoefs1[1]) * (t * t * yCoefs1[0] + t * yCoefs1[1] + yCoefs1[2] - s * s * s * yCoefs2[0] -
                    s * s * yCoefs2[1] - s * yCoefs2[2] - yCoefs2[3]) - (2f * t * yCoefs1[0] + yCoefs1[1]) * (t * t * xCoefs1[0] + t * xCoefs1[1] + xCoefs1[2] - s * s * s * xCoefs2[0] -
                    s * s * xCoefs2[1] - s * xCoefs2[2] - xCoefs2[3])

            t1 = t - dt / d
            s1 = s - ds / d
        }
        params[0] = t1
        params[1] = s1
    }

    private fun quadNewton(xCoefs1: FloatArray, yCoefs1: FloatArray,
                           xCoefs2: FloatArray, yCoefs2: FloatArray, params: FloatArray) {
        var t = 0f
        var s = 0f
        var t1 = params[0]
        var s1 = params[1]
        var d: Float
        var dt: Float
        var ds: Float

        while (Math.sqrt(((t - t1) * (t - t1) + (s - s1) * (s - s1)).toDouble()) > EPSILON) {
            t = t1
            s = s1
            d = -(2f * t * xCoefs1[0] + xCoefs1[1]) * (2f * s * yCoefs2[0] + yCoefs2[1]) + (2f * s * xCoefs2[0] + xCoefs2[1]) * (2f * t * yCoefs1[0] + yCoefs1[1])

            dt = -(t * t * xCoefs1[0] + t * xCoefs1[1] + xCoefs1[1] - s * s * xCoefs2[0] -
                    s * xCoefs2[1] - xCoefs2[2]) * (2f * s * yCoefs2[0] + yCoefs2[1]) + (2f * s * xCoefs2[0] + xCoefs2[1]) * (t * t * yCoefs1[0] + t * yCoefs1[1] + yCoefs1[2] - s * s * yCoefs2[0] -
                    s * yCoefs2[1] - yCoefs2[2])

            ds = (2f * t * xCoefs1[0] + xCoefs1[1]) * (t * t * yCoefs1[0] + t * yCoefs1[1] + yCoefs1[2] - s * s * yCoefs2[0] -
                    s * yCoefs2[1] - yCoefs2[2]) - (2f * t * yCoefs1[0] + yCoefs1[1]) * (t * t * xCoefs1[0] + t * xCoefs1[1] + xCoefs1[2] - s * s * xCoefs2[0] -
                    s * xCoefs2[1] - xCoefs2[2])

            t1 = t - dt / d
            s1 = s - ds / d
        }
        params[0] = t1
        params[1] = s1
    }
}
