//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f
import java.lang.Math

/**
 * An internal class used to compute crossings.
 */
internal class CurveCrossingHelper(private val coords: Array<FloatArray>, private val sizes: IntArray,
                                   private val rules: Array<IntArray>, private val rulesSizes: IntArray, private val offsets: Array<IntArray>) {
    private val isectPoints = ArrayList<IntersectPoint>()

    fun findCrossing(): Array<IntersectPoint> {
        val edge1 = FloatArray(8)
        val edge2 = FloatArray(8)
        val points = FloatArray(6)
        val params = FloatArray(6)
        val mp1 = FloatArray(2)
        val cp1 = FloatArray(2)
        val mp2 = FloatArray(2)
        val cp2 = FloatArray(2)
        var rule1: Int
        var rule2: Int
        var endIndex1: Int
        var endIndex2: Int
        var ipCount = 0

        for (i in 0..rulesSizes[0] - 1) {
            rule1 = rules[0][i]
            endIndex1 = currentEdge(0, i, edge1, mp1, cp1)
            for (j in 0..rulesSizes[1] - 1) {
                ipCount = 0
                rule2 = rules[1][j]
                endIndex2 = currentEdge(1, j, edge2, mp2, cp2)
                if ((rule1 == PathIterator.SEG_LINETO || rule1 == PathIterator.SEG_CLOSE) && (rule2 == PathIterator.SEG_LINETO || rule2 == PathIterator.SEG_CLOSE)) {
                    ipCount = GeometryUtil.intersectLinesWithParams(
                            edge1[0], edge1[1], edge1[2], edge1[3],
                            edge2[0], edge2[1], edge2[2], edge2[3], params)

                    if (ipCount != 0) {
                        points[0] = GeometryUtil.line(params[0], edge1[0], edge1[2])
                        points[1] = GeometryUtil.line(params[0], edge1[1], edge1[3])
                    }

                } else if ((rule1 == PathIterator.SEG_LINETO || rule1 == PathIterator.SEG_CLOSE) && rule2 == PathIterator.SEG_QUADTO) {
                    ipCount = GeometryUtil.intersectLineAndQuad(
                            edge1[0], edge1[1], edge1[2], edge1[3],
                            edge2[0], edge2[1], edge2[2], edge2[3], edge2[4], edge2[5], params)
                    for (k in 0..ipCount - 1) {
                        points[2 * k] = GeometryUtil.line(params[2 * k], edge1[0], edge1[2])
                        points[2 * k + 1] = GeometryUtil.line(params[2 * k], edge1[1], edge1[3])
                    }

                } else if (rule1 == PathIterator.SEG_QUADTO && (rule2 == PathIterator.SEG_LINETO || rule2 == PathIterator.SEG_CLOSE)) {
                    ipCount = GeometryUtil.intersectLineAndQuad(
                            edge2[0], edge2[1], edge2[2], edge2[3],
                            edge1[0], edge1[1], edge1[2], edge1[3], edge1[4], edge1[5], params)
                    for (k in 0..ipCount - 1) {
                        points[2 * k] = GeometryUtil.line(params[2 * k + 1], edge2[0], edge2[2])
                        points[2 * k + 1] = GeometryUtil.line(
                                params[2 * k + 1], edge2[1], edge2[3])
                    }

                } else if (rule1 == PathIterator.SEG_CUBICTO && (rule2 == PathIterator.SEG_LINETO || rule2 == PathIterator.SEG_CLOSE)) {
                    ipCount = GeometryUtil.intersectLineAndCubic(
                            edge1[0], edge1[1], edge1[2], edge1[3], edge1[4], edge1[5], edge1[6],
                            edge1[7], edge2[0], edge2[1], edge2[2], edge2[3], params)
                    for (k in 0..ipCount - 1) {
                        points[2 * k] = GeometryUtil.line(params[2 * k + 1], edge2[0], edge2[2])
                        points[2 * k + 1] = GeometryUtil.line(
                                params[2 * k + 1], edge2[1], edge2[3])
                    }

                } else if ((rule1 == PathIterator.SEG_LINETO || rule1 == PathIterator.SEG_CLOSE) && rule2 == PathIterator.SEG_CUBICTO) {
                    ipCount = GeometryUtil.intersectLineAndCubic(
                            edge1[0], edge1[1], edge1[2], edge1[3], edge2[0], edge2[1],
                            edge2[2], edge2[3], edge2[4], edge2[5], edge2[6], edge2[7], params)
                    for (k in 0..ipCount - 1) {
                        points[2 * k] = GeometryUtil.line(params[2 * k], edge1[0], edge1[2])
                        points[2 * k + 1] = GeometryUtil.line(params[2 * k], edge1[1], edge1[3])
                    }

                } else if (rule1 == PathIterator.SEG_QUADTO && rule2 == PathIterator.SEG_QUADTO) {
                    ipCount = GeometryUtil.intersectQuads(
                            edge1[0], edge1[1], edge1[2], edge1[3], edge1[4], edge1[5],
                            edge2[0], edge2[1], edge2[2], edge2[3], edge2[4], edge2[5], params)
                    for (k in 0..ipCount - 1) {
                        points[2 * k] = GeometryUtil.quad(
                                params[2 * k], edge1[0], edge1[2], edge1[4])
                        points[2 * k + 1] = GeometryUtil.quad(
                                params[2 * k], edge1[1], edge1[3], edge1[5])
                    }

                } else if (rule1 == PathIterator.SEG_QUADTO && rule2 == PathIterator.SEG_CUBICTO) {
                    ipCount = GeometryUtil.intersectQuadAndCubic(
                            edge1[0], edge1[1], edge1[2], edge1[3], edge1[4], edge1[5],
                            edge2[0], edge2[1], edge2[2], edge2[3], edge2[4], edge2[5],
                            edge2[6], edge2[7], params)
                    for (k in 0..ipCount - 1) {
                        points[2 * k] = GeometryUtil.quad(
                                params[2 * k], edge1[0], edge1[2], edge1[4])
                        points[2 * k + 1] = GeometryUtil.quad(
                                params[2 * k], edge1[1], edge1[3], edge1[5])
                    }

                } else if (rule1 == PathIterator.SEG_CUBICTO && rule2 == PathIterator.SEG_QUADTO) {
                    ipCount = GeometryUtil.intersectQuadAndCubic(
                            edge2[0], edge2[1], edge2[2], edge2[3], edge2[4], edge2[5],
                            edge1[0], edge1[1], edge1[2], edge1[3], edge1[4], edge1[5],
                            edge2[6], edge2[7], params)
                    for (k in 0..ipCount - 1) {
                        points[2 * k] = GeometryUtil.quad(
                                params[2 * k + 1], edge2[0], edge2[2], edge2[4])
                        points[2 * k + 1] = GeometryUtil.quad(
                                params[2 * k + 1], edge2[1], edge2[3], edge2[5])
                    }

                } else if (rule1 == PathIterator.SEG_CUBICTO && rule2 == PathIterator.SEG_CUBICTO) {
                    ipCount = GeometryUtil.intersectCubics(
                            edge1[0], edge1[1], edge1[2], edge1[3], edge1[4], edge1[5], edge1[6],
                            edge1[7], edge2[0], edge2[1], edge2[2], edge2[3], edge2[4], edge2[5],
                            edge2[6], edge2[7], params)
                    for (k in 0..ipCount - 1) {
                        points[2 * k] = GeometryUtil.cubic(
                                params[2 * k], edge1[0], edge1[2], edge1[4], edge1[6])
                        points[2 * k + 1] = GeometryUtil.cubic(
                                params[2 * k], edge1[1], edge1[3], edge1[5], edge1[7])
                    }
                }

                endIndex1 = i
                endIndex2 = j
                var begIndex1 = i - 1
                var begIndex2 = j - 1

                for (k in 0..ipCount - 1) {
                    var ip: IntersectPoint? = null
                    if (!containsPoint(points[2 * k], points[2 * k + 1])) {
                        val iter = isectPoints.iterator()
                        while (iter.hasNext()) {
                            ip = iter.next()
                            if (begIndex1 == ip.begIndex(true) && endIndex1 == ip.endIndex(true)) {
                                if (ip.param(true) > params[2 * k]) {
                                    endIndex1 = -(isectPoints.indexOf(ip) + 1)
                                    ip.setBegIndex1(-(isectPoints.size + 1))
                                } else {
                                    begIndex1 = -(isectPoints.indexOf(ip) + 1)
                                    ip.setEndIndex1(-(isectPoints.size + 1))
                                }
                            }

                            if (begIndex2 == ip.begIndex(false) && endIndex2 == ip.endIndex(false)) {
                                if (ip.param(false) > params[2 * k + 1]) {
                                    endIndex2 = -(isectPoints.indexOf(ip) + 1)
                                    ip.setBegIndex2(-(isectPoints.size + 1))
                                } else {
                                    begIndex2 = -(isectPoints.indexOf(ip) + 1)
                                    ip.setEndIndex2(-(isectPoints.size + 1))
                                }
                            }
                        }

                        if (rule1 == PathIterator.SEG_CLOSE) {
                            rule1 = PathIterator.SEG_LINETO
                        }

                        if (rule2 == PathIterator.SEG_CLOSE) {
                            rule2 = PathIterator.SEG_LINETO
                        }

                        isectPoints.add(IntersectPoint(
                                begIndex1, endIndex1, rule1, i, begIndex2, endIndex2,
                                rule2, j, points[2 * k], points[2 * k + 1],
                                params[2 * k], params[2 * k + 1]))
                    }
                }
            }
        }
        return isectPoints.toTypedArray()
    }

    private fun currentEdge(areaIndex: Int, index: Int, c: FloatArray, mp: FloatArray, cp: FloatArray): Int {
        var endIndex = 0

        when (rules[areaIndex][index]) {
            PathIterator.SEG_MOVETO -> {
                mp[0] = coords[areaIndex][offsets[areaIndex][index]]
                cp[0] = mp[0]
                mp[1] = coords[areaIndex][offsets[areaIndex][index] + 1]
                cp[1] = mp[1]
            }
            PathIterator.SEG_LINETO -> {
                c[0] = cp[0]
                c[1] = cp[1]
                c[2] = coords[areaIndex][offsets[areaIndex][index]]
                cp[0] = c[2]
                c[3] = coords[areaIndex][offsets[areaIndex][index] + 1]
                cp[1] = c[3]
                endIndex = 0
            }
            PathIterator.SEG_QUADTO -> {
                c[0] = cp[0]
                c[1] = cp[1]
                c[2] = coords[areaIndex][offsets[areaIndex][index]]
                c[3] = coords[areaIndex][offsets[areaIndex][index] + 1]
                c[4] = coords[areaIndex][offsets[areaIndex][index] + 2]
                cp[0] = c[4]
                c[5] = coords[areaIndex][offsets[areaIndex][index] + 3]
                cp[1] = c[5]
                endIndex = 2
            }
            PathIterator.SEG_CUBICTO -> {
                c[0] = cp[0]
                c[1] = cp[1]
                c[2] = coords[areaIndex][offsets[areaIndex][index]]
                c[3] = coords[areaIndex][offsets[areaIndex][index] + 1]
                c[4] = coords[areaIndex][offsets[areaIndex][index] + 2]
                c[5] = coords[areaIndex][offsets[areaIndex][index] + 3]
                c[6] = coords[areaIndex][offsets[areaIndex][index] + 4]
                cp[0] = c[6]
                c[7] = coords[areaIndex][offsets[areaIndex][index] + 5]
                cp[1] = c[7]
                endIndex = 4
            }
            PathIterator.SEG_CLOSE -> {
                c[0] = cp[0]
                c[1] = cp[1]
                c[2] = mp[0]
                cp[0] = c[2]
                c[3] = mp[1]
                cp[1] = c[3]
                if (offsets[areaIndex][index] >= sizes[areaIndex]) {
                    endIndex = -sizes[areaIndex]
                } else {
                    endIndex = 0
                }
            }
        }
        return offsets[areaIndex][index] + endIndex
    }

    private fun containsPoint(x: Float, y: Float): Boolean {
        var ipoint: IntersectPoint
        val i = isectPoints.iterator()
        while (i.hasNext()) {
            ipoint = i.next()
            if (Math.abs(ipoint.x() - x) < Math.pow(10.0, -6.0) && Math.abs(ipoint.y() - y) < Math.pow(10.0, -6.0)) {
                return true
            }
        }
        return false
    }
}
