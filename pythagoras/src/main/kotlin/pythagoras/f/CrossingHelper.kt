//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

/**
 * An internal class used to compute crossings.
 */
internal class CrossingHelper(private val coords: Array<FloatArray>, private val sizes: IntArray) {
    private val isectPoints = ArrayList<IntersectPoint>()

    fun findCrossing(): Array<IntersectPoint> {
        val pointCount1 = sizes[0] / 2
        val pointCount2 = sizes[1] / 2
        val indices = IntArray(pointCount1 + pointCount2)
        for (i in 0..pointCount1 + pointCount2 - 1) {
            indices[i] = i
        }

        sort(coords[0], pointCount1, coords[1], pointCount2, indices)
        // the set for the shapes edges storing
        val edges = ArrayList<Edge>()
        var edge: Edge
        var begIndex: Int
        var endIndex: Int
        var areaNumber: Int

        for (i in indices.indices) {
            if (indices[i] < pointCount1) {
                begIndex = indices[i]
                endIndex = indices[i] - 1
                if (endIndex < 0) {
                    endIndex = pointCount1 - 1
                }
                areaNumber = 0

            } else if (indices[i] < pointCount1 + pointCount2) {
                begIndex = indices[i] - pointCount1
                endIndex = indices[i] - 1 - pointCount1
                if (endIndex < 0) {
                    endIndex = pointCount2 - 1
                }
                areaNumber = 1

            } else {
                throw IndexOutOfBoundsException()
            }

            if (!removeEdge(edges, begIndex, endIndex)) {
                edge = Edge(begIndex, endIndex, areaNumber)
                intersectShape(edges, coords[0], pointCount1, coords[1], pointCount2, edge)
                edges.add(edge)
            }

            begIndex = indices[i]
            endIndex = indices[i] + 1

            if (begIndex < pointCount1 && endIndex == pointCount1) {
                endIndex = 0
            } else if (begIndex >= pointCount1 && endIndex == pointCount2 + pointCount1) {
                endIndex = pointCount1
            }

            if (endIndex < pointCount1) {
                areaNumber = 0
            } else {
                areaNumber = 1
                endIndex -= pointCount1
                begIndex -= pointCount1
            }

            if (!removeEdge(edges, begIndex, endIndex)) {
                edge = Edge(begIndex, endIndex, areaNumber)
                intersectShape(edges, coords[0], pointCount1, coords[1], pointCount2, edge)
                edges.add(edge)
            }
        }

        return isectPoints.toTypedArray()
    }

    private fun removeEdge(edges: MutableList<Edge>, begIndex: Int, endIndex: Int): Boolean {
        for (edge in edges) {
            if (edge.reverseCompare(begIndex, endIndex)) {
                edges.remove(edge)
                return true
            }
        }
        return false
    }

    // return the quantity of intersect points
    private fun intersectShape(edges: List<Edge>, coords1: FloatArray, length1: Int,
                               coords2: FloatArray, length2: Int, initEdge: Edge) {
        val areaOfEdge1: Int
        var areaOfEdge2: Int
        var initBegin: Int
        var initEnd: Int
        var addBegin: Int
        var addEnd: Int
        val x1: Float
        val y1: Float
        val x2: Float
        val y2: Float
        var x3: Float
        var y3: Float
        var x4: Float
        var y4: Float
        val point = FloatArray(2)
        var edge: Edge

        if (initEdge.areaNumber == 0) {
            x1 = coords1[2 * initEdge.begIndex]
            y1 = coords1[2 * initEdge.begIndex + 1]
            x2 = coords1[2 * initEdge.endIndex]
            y2 = coords1[2 * initEdge.endIndex + 1]
            areaOfEdge1 = 0
        } else {
            x1 = coords2[2 * initEdge.begIndex]
            y1 = coords2[2 * initEdge.begIndex + 1]
            x2 = coords2[2 * initEdge.endIndex]
            y2 = coords2[2 * initEdge.endIndex + 1]
            areaOfEdge1 = 1
        }

        val iter = edges.iterator()
        while (iter.hasNext()) {
            edge = iter.next()

            if (edge.areaNumber == 0) {
                x3 = coords1[2 * edge.begIndex]
                y3 = coords1[2 * edge.begIndex + 1]
                x4 = coords1[2 * edge.endIndex]
                y4 = coords1[2 * edge.endIndex + 1]
                areaOfEdge2 = 0
            } else {
                x3 = coords2[2 * edge.begIndex]
                y3 = coords2[2 * edge.begIndex + 1]
                x4 = coords2[2 * edge.endIndex]
                y4 = coords2[2 * edge.endIndex + 1]
                areaOfEdge2 = 1
            }

            if (areaOfEdge1 != areaOfEdge2 &&
                    GeometryUtil.intersectLines(x1, y1, x2, y2, x3, y3, x4, y4, point) == 1 &&
                    !containsPoint(point)) {

                if (initEdge.areaNumber == 0) {
                    initBegin = initEdge.begIndex
                    initEnd = initEdge.endIndex
                    addBegin = edge.begIndex
                    addEnd = edge.endIndex
                } else {
                    initBegin = edge.begIndex
                    initEnd = edge.endIndex
                    addBegin = initEdge.begIndex
                    addEnd = initEdge.endIndex
                }

                if (initEnd == length1 - 1 && initBegin == 0 && initEnd > initBegin || (initEnd != length1 - 1 || initBegin != 0) &&
                        (initBegin != length1 - 1 || initEnd != 0) && initBegin > initEnd) {
                    val temp = initBegin
                    initBegin = initEnd
                    initEnd = temp
                }

                if (addEnd == length2 - 1 && addBegin == 0 && addEnd > addBegin || (addEnd != length2 - 1 || addBegin != 0) &&
                        (addBegin != length2 - 1 || addEnd != 0) && addBegin > addEnd) {
                    val temp = addBegin
                    addBegin = addEnd
                    addEnd = temp
                }

                var ip: IntersectPoint
                val i = isectPoints.iterator()
                while (i.hasNext()) {
                    ip = i.next()
                    if (initBegin == ip.begIndex(true) && initEnd == ip.endIndex(true)) {
                        if (compare(ip.x(), ip.y(), point[0], point[1]) > 0) {
                            initEnd = -(isectPoints.indexOf(ip) + 1)
                            ip.setBegIndex1(-(isectPoints.size + 1))
                        } else {
                            initBegin = -(isectPoints.indexOf(ip) + 1)
                            ip.setEndIndex1(-(isectPoints.size + 1))
                        }
                    }

                    if (addBegin == ip.begIndex(false) && addEnd == ip.endIndex(false)) {
                        if (compare(ip.x(), ip.y(), point[0], point[1]) > 0) {
                            addEnd = -(isectPoints.indexOf(ip) + 1)
                            ip.setBegIndex2(-(isectPoints.size + 1))
                        } else {
                            addBegin = -(isectPoints.indexOf(ip) + 1)
                            ip.setEndIndex2(-(isectPoints.size + 1))
                        }
                    }
                }

                isectPoints.add(IntersectPoint(initBegin, initEnd, addBegin, addEnd,
                        point[0], point[1]))
            }
        }
    }

    fun containsPoint(point: FloatArray): Boolean {
        var ipoint: IntersectPoint
        val i = isectPoints.iterator()
        while (i.hasNext()) {
            ipoint = i.next()
            if (ipoint.x() == point[0] && ipoint.y() == point[1]) {
                return true
            }
        }
        return false
    }

    private class Edge internal constructor(internal val begIndex: Int, internal val endIndex: Int, internal val areaNumber: Int) {

        internal fun reverseCompare(begIndex: Int, endIndex: Int): Boolean {
            return this.begIndex == endIndex && this.endIndex == begIndex
        }
    }

    companion object {

        // the array sorting
        private fun sort(coords1: FloatArray, length1: Int,
                         coords2: FloatArray, length2: Int, array: IntArray) {
            var temp: Int
            val length = length1 + length2
            var x1: Float
            var y1: Float
            var x2: Float
            var y2: Float

            for (i in 1..length - 1) {
                if (array[i - 1] < length1) {
                    x1 = coords1[2 * array[i - 1]]
                    y1 = coords1[2 * array[i - 1] + 1]
                } else {
                    x1 = coords2[2 * (array[i - 1] - length1)]
                    y1 = coords2[2 * (array[i - 1] - length1) + 1]
                }
                if (array[i] < length1) {
                    x2 = coords1[2 * array[i]]
                    y2 = coords1[2 * array[i] + 1]
                } else {
                    x2 = coords2[2 * (array[i] - length1)]
                    y2 = coords2[2 * (array[i] - length1) + 1]
                }
                var j = i
                while (j > 0 && compare(x1, y1, x2, y2) <= 0) {
                    temp = array[j]
                    array[j] = array[j - 1]
                    array[j - 1] = temp
                    j--
                    if (j > 0) {
                        if (array[j - 1] < length1) {
                            x1 = coords1[2 * array[j - 1]]
                            y1 = coords1[2 * array[j - 1] + 1]
                        } else {
                            x1 = coords2[2 * (array[j - 1] - length1)]
                            y1 = coords2[2 * (array[j - 1] - length1) + 1]
                        }
                        if (array[j] < length1) {
                            x2 = coords1[2 * array[j]]
                            y2 = coords1[2 * array[j] + 1]
                        } else {
                            x2 = coords2[2 * (array[j] - length1)]
                            y2 = coords2[2 * (array[j] - length1) + 1]
                        }
                    }
                }
            }
        }

        fun compare(x1: Float, y1: Float, x2: Float, y2: Float): Int {
            if (x1 < x2 || x1 == x2 && y1 < y2) {
                return 1
            } else if (x1 == x2 && y1 == y2) {
                return 0
            }
            return -1
        }
    }
}
