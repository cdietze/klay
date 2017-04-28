//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

import pythagoras.util.Platform
import java.util.*

/**
 * Stores and manipulates an enclosed area of 2D space.
 * See http://download.oracle.com/javase/6/docs/api/java/awt/geom/Area.html
 */
class Area : IShape, Cloneable {
    /**
     * Creates an empty area.
     */
    constructor() {}

    /**
     * Creates an area from the supplied shape.
     */
    constructor(s: IShape) {
        val segmentCoords = FloatArray(6)
        var lastMoveX = 0f
        var lastMoveY = 0f
        var rulesIndex = 0
        var coordsIndex = 0

        val pi = s.pathIterator(null)
        while (!pi.isDone) {
            _coords = adjustSize(_coords, coordsIndex + 6)
            _rules = adjustSize(_rules, rulesIndex + 1)
            _offsets = adjustSize(_offsets, rulesIndex + 1)
            _rules[rulesIndex] = pi.currentSegment(segmentCoords)
            _offsets[rulesIndex] = coordsIndex

            when (_rules[rulesIndex]) {
                PathIterator.SEG_MOVETO -> {
                    _coords[coordsIndex++] = segmentCoords[0]
                    _coords[coordsIndex++] = segmentCoords[1]
                    lastMoveX = segmentCoords[0]
                    lastMoveY = segmentCoords[1]
                    ++_moveToCount
                }
                PathIterator.SEG_LINETO -> if (segmentCoords[0] != lastMoveX || segmentCoords[1] != lastMoveY) {
                    _coords[coordsIndex++] = segmentCoords[0]
                    _coords[coordsIndex++] = segmentCoords[1]
                } else {
                    --rulesIndex
                }
                PathIterator.SEG_QUADTO -> {
                    System.arraycopy(segmentCoords, 0, _coords, coordsIndex, 4)
                    coordsIndex += 4
                    isPolygonal = false
                }
                PathIterator.SEG_CUBICTO -> {
                    System.arraycopy(segmentCoords, 0, _coords, coordsIndex, 6)
                    coordsIndex += 6
                    isPolygonal = false
                }
                PathIterator.SEG_CLOSE -> {
                }
            }
            ++rulesIndex
            pi.next()
        }

        if (rulesIndex != 0 && _rules[rulesIndex - 1] != PathIterator.SEG_CLOSE) {
            _rules = adjustSize(_rules, rulesIndex + 1)
            _rules[rulesIndex] = PathIterator.SEG_CLOSE
            _offsets = adjustSize(_offsets, rulesIndex + 1)
            _offsets[rulesIndex] = coordsIndex
            ++rulesIndex
        }

        _rulesSize = rulesIndex
        _coordsSize = coordsIndex
    }

    /**
     * Returns true if this area is rectangular.
     */
    val isRectangular: Boolean
        get() = isPolygonal && _rulesSize <= 5 && _coordsSize <= 8 &&
                _coords[1] == _coords[3] && _coords[7] == _coords[5] &&
                _coords[0] == _coords[6] && _coords[2] == _coords[4]

    /**
     * Returns true if this area encloses only a single contiguous space.
     */
    val isSingular: Boolean
        get() = _moveToCount <= 1

    /**
     * Resets this area to empty.
     */
    fun reset() {
        _coordsSize = 0
        _rulesSize = 0
    }

    /**
     * Transforms this area with the supplied transform.
     */
    fun transform(t: Transform) {
        copy(Area(Transforms.createTransformedShape(t, this)), this)
    }

    /**
     * Creates a new area equal to this area transformed by the supplied transform.
     */
    fun createTransformedArea(t: Transform): Area {
        return Area(Transforms.createTransformedShape(t, this))
    }

    /**
     * Adds the supplied area to this area.
     */
    fun add(area: Area?) {
        if (area == null || area.isEmpty) {
            return
        } else if (isEmpty) {
            copy(area, this)
            return
        }

        if (isPolygonal && area.isPolygonal) {
            addPolygon(area)
        } else {
            addCurvePolygon(area)
        }

        if (areaBoundsSquare() < GeometryUtil.EPSILON) {
            reset()
        }
    }

    /**
     * Intersects the supplied area with this area.
     */
    fun intersect(area: Area?) {
        if (area == null) {
            return
        } else if (isEmpty || area.isEmpty) {
            reset()
            return
        }

        if (isPolygonal && area.isPolygonal) {
            intersectPolygon(area)
        } else {
            intersectCurvePolygon(area)
        }

        if (areaBoundsSquare() < GeometryUtil.EPSILON) {
            reset()
        }
    }

    /**
     * Subtracts the supplied area from this area.
     */
    fun subtract(area: Area?) {
        if (area == null || isEmpty || area.isEmpty) {
            return
        }

        if (isPolygonal && area.isPolygonal) {
            subtractPolygon(area)
        } else {
            subtractCurvePolygon(area)
        }

        if (areaBoundsSquare() < GeometryUtil.EPSILON) {
            reset()
        }
    }

    /**
     * Computes the exclusive or of this area and the supplied area and sets this area to the
     * result.
     */
    fun exclusiveOr(area: Area) {
        val a = clone()
        a.intersect(area)
        add(area)
        subtract(a)
    }

    override // from interface IShape
    val isEmpty: Boolean
        get() = _rulesSize == 0 && _coordsSize == 0

    override // from interface IShape
    fun contains(x: Float, y: Float): Boolean {
        return !isEmpty && containsExact(x, y) > 0
    }

    override // from interface IShape
    fun contains(x: Float, y: Float, width: Float, height: Float): Boolean {
        val crossCount = Crossing.intersectPath(pathIterator(null), x, y, width, height)
        return crossCount != Crossing.CROSSING && Crossing.isInsideEvenOdd(crossCount)
    }

    override // from interface IShape
    fun contains(p: XY): Boolean {
        return contains(p.x(), p.y())
    }

    override // from interface IShape
    fun contains(r: IRectangle): Boolean {
        return contains(r.x(), r.y(), r.width(), r.height())
    }

    override // from interface IShape
    fun intersects(x: Float, y: Float, width: Float, height: Float): Boolean {
        if (width <= 0f || height <= 0f) {
            return false
        } else if (!bounds().intersects(x, y, width, height)) {
            return false
        }
        val crossCount = Crossing.intersectShape(this, x, y, width, height)
        return Crossing.isInsideEvenOdd(crossCount)
    }

    override // from interface IShape
    fun intersects(r: IRectangle): Boolean {
        return intersects(r.x(), r.y(), r.width(), r.height())
    }

    override // from interface IShape
    fun bounds(): Rectangle {
        return bounds(Rectangle())
    }

    override // from interface IShape
    fun bounds(target: Rectangle): Rectangle {
        var maxX = _coords[0]
        var maxY = _coords[1]
        var minX = _coords[0]
        var minY = _coords[1]
        var i = 0
        while (i < _coordsSize) {
            minX = Math.min(minX, _coords[i])
            maxX = Math.max(maxX, _coords[i++])
            minY = Math.min(minY, _coords[i])
            maxY = Math.max(maxY, _coords[i++])
        }
        return Rectangle(minX, minY, maxX - minX, maxY - minY)
    }

    override // from interface IShape
    fun pathIterator(t: Transform?): PathIterator {
        return AreaPathIterator(t)
    }

    override // from interface IShape
    fun pathIterator(t: Transform?, flatness: Float): PathIterator {
        return FlatteningPathIterator(pathIterator(t), flatness)
    }

    override // from Object
    fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        } else if (obj !is Area) {
            return false
        }
        val area = clone()
        area.subtract(obj as Area?)
        return area.isEmpty
    }

    // @Override // can't declare @Override due to GWT
    public override fun clone(): Area {
        val area = Area()
        copy(this, area)
        return area
    }

    override // from Object
    fun toString(): String {
        return "Area[coords=" + _coordsSize + ", rules=" + _rulesSize +
                ", isPoly=" + isPolygonal + "]"
    }

    private fun addCurvePolygon(area: Area) {
        val crossHelper = CurveCrossingHelper(
                arrayOf(_coords, area._coords),
                intArrayOf(_coordsSize, area._coordsSize),
                arrayOf(_rules, area._rules),
                intArrayOf(_rulesSize, area._rulesSize),
                arrayOf(_offsets, area._offsets))
        val intersectPoints = crossHelper.findCrossing()

        if (intersectPoints.size == 0) {
            if (area.contains(bounds())) {
                copy(area, this)
            } else if (!contains(area.bounds())) {
                _coords = adjustSize(_coords, _coordsSize + area._coordsSize)
                System.arraycopy(area._coords, 0, _coords, _coordsSize, area._coordsSize)
                _coordsSize += area._coordsSize
                _rules = adjustSize(_rules, _rulesSize + area._rulesSize)
                System.arraycopy(area._rules, 0, _rules, _rulesSize, area._rulesSize)
                _rulesSize += area._rulesSize
                _offsets = adjustSize(_offsets, _rulesSize + area._rulesSize)
                System.arraycopy(area._offsets, 0, _offsets, _rulesSize, area._rulesSize)
            }

            return
        }

        val resultCoords = FloatArray(_coordsSize + area._coordsSize + intersectPoints.size)
        val resultRules = IntArray(_rulesSize + area._rulesSize + intersectPoints.size)
        val resultOffsets = IntArray(_rulesSize + area._rulesSize + intersectPoints.size)
        var resultCoordPos = 0
        var resultRulesPos = 0
        var isCurrentArea = true

        var point = intersectPoints[0]
        resultRules[resultRulesPos] = PathIterator.SEG_MOVETO
        resultOffsets[resultRulesPos++] = resultCoordPos

        do {
            resultCoords[resultCoordPos++] = point.x()
            resultCoords[resultCoordPos++] = point.y()
            val curIndex = point.endIndex(true)
            if (curIndex < 0) {
                isCurrentArea = !isCurrentArea
            } else if (area.containsExact(_coords[2 * curIndex], _coords[2 * curIndex + 1]) > 0) {
                isCurrentArea = false
            } else {
                isCurrentArea = true
            }

            val nextPoint = nextIntersectPoint(intersectPoints, point, isCurrentArea)
            val coords = if (isCurrentArea) this._coords else area._coords
            val offsets = if (isCurrentArea) this._offsets else area._offsets
            val rules = if (isCurrentArea) this._rules else area._rules
            var offset = point.ruleIndex(isCurrentArea)
            var isCopyUntilZero = false
            if (point.ruleIndex(isCurrentArea) > nextPoint.ruleIndex(isCurrentArea)) {
                val rulesSize = if (isCurrentArea) this._rulesSize else area._rulesSize
                resultCoordPos = includeCoordsAndRules(offset + 1, rulesSize, rules, offsets,
                        resultRules, resultOffsets, resultCoords, coords, resultRulesPos,
                        resultCoordPos, point, isCurrentArea, false, 0)
                resultRulesPos += rulesSize - offset - 1
                offset = 1
                isCopyUntilZero = true
            }

            val length = nextPoint.ruleIndex(isCurrentArea) - offset + 1
            if (isCopyUntilZero) {
                offset = 0
            }

            resultCoordPos = includeCoordsAndRules(offset, length, rules, offsets, resultRules,
                    resultOffsets, resultCoords, coords, resultRulesPos, resultCoordPos, point,
                    isCurrentArea, true, 0)
            resultRulesPos += length - offset
            point = nextPoint
        } while (point !== intersectPoints[0])

        resultRules[resultRulesPos++] = PathIterator.SEG_CLOSE
        resultOffsets[resultRulesPos - 1] = resultCoordPos
        this._coords = resultCoords
        this._rules = resultRules
        this._offsets = resultOffsets
        this._coordsSize = resultCoordPos
        this._rulesSize = resultRulesPos
    }

    private fun addPolygon(area: Area) {
        val crossHelper = CrossingHelper(
                arrayOf(_coords, area._coords),
                intArrayOf(_coordsSize, area._coordsSize))
        val intersectPoints = crossHelper.findCrossing()

        if (intersectPoints.size == 0) {
            if (area.contains(bounds())) {
                copy(area, this)
            } else if (!contains(area.bounds())) {
                _coords = adjustSize(_coords, _coordsSize + area._coordsSize)
                System.arraycopy(area._coords, 0, _coords, _coordsSize, area._coordsSize)
                _coordsSize += area._coordsSize
                _rules = adjustSize(_rules, _rulesSize + area._rulesSize)
                System.arraycopy(area._rules, 0, _rules, _rulesSize, area._rulesSize)
                _rulesSize += area._rulesSize
                _offsets = adjustSize(_offsets, _rulesSize + area._rulesSize)
                System.arraycopy(area._offsets, 0, _offsets, _rulesSize, area._rulesSize)
            }
            return
        }

        val resultCoords = FloatArray(_coordsSize + area._coordsSize + intersectPoints.size)
        val resultRules = IntArray(_rulesSize + area._rulesSize + intersectPoints.size)
        val resultOffsets = IntArray(_rulesSize + area._rulesSize + intersectPoints.size)
        var resultCoordPos = 0
        var resultRulesPos = 0
        var isCurrentArea = true

        var point = intersectPoints[0]
        resultRules[resultRulesPos] = PathIterator.SEG_MOVETO
        resultOffsets[resultRulesPos++] = resultCoordPos

        do {
            resultCoords[resultCoordPos++] = point.x()
            resultCoords[resultCoordPos++] = point.y()
            resultRules[resultRulesPos] = PathIterator.SEG_LINETO
            resultOffsets[resultRulesPos++] = resultCoordPos - 2
            val curIndex = point.endIndex(true)
            if (curIndex < 0) {
                isCurrentArea = !isCurrentArea
            } else if (area.containsExact(_coords[2 * curIndex], _coords[2 * curIndex + 1]) > 0) {
                isCurrentArea = false
            } else {
                isCurrentArea = true
            }

            val nextPoint = nextIntersectPoint(intersectPoints, point, isCurrentArea)
            val coords = if (isCurrentArea) this._coords else area._coords
            var offset = 2 * point.endIndex(isCurrentArea)
            if (offset >= 0 && nextPoint.begIndex(isCurrentArea) < point.endIndex(isCurrentArea)) {
                val coordSize = if (isCurrentArea) this._coordsSize else area._coordsSize
                val length = coordSize - offset
                System.arraycopy(coords, offset, resultCoords, resultCoordPos, length)

                for (i in 0..length / 2 - 1) {
                    resultRules[resultRulesPos] = PathIterator.SEG_LINETO
                    resultOffsets[resultRulesPos++] = resultCoordPos
                    resultCoordPos += 2
                }

                offset = 0
            }

            if (offset >= 0) {
                val length = 2 * nextPoint.begIndex(isCurrentArea) - offset + 2
                System.arraycopy(coords, offset, resultCoords, resultCoordPos, length)

                for (i in 0..length / 2 - 1) {
                    resultRules[resultRulesPos] = PathIterator.SEG_LINETO
                    resultOffsets[resultRulesPos++] = resultCoordPos
                    resultCoordPos += 2
                }
            }

            point = nextPoint
        } while (point !== intersectPoints[0])

        resultRules[resultRulesPos - 1] = PathIterator.SEG_CLOSE
        resultOffsets[resultRulesPos - 1] = resultCoordPos
        _coords = resultCoords
        _rules = resultRules
        _offsets = resultOffsets
        _coordsSize = resultCoordPos
        _rulesSize = resultRulesPos
    }

    private fun intersectCurvePolygon(area: Area) {
        val crossHelper = CurveCrossingHelper(
                arrayOf(_coords, area._coords),
                intArrayOf(_coordsSize, area._coordsSize),
                arrayOf(_rules, area._rules),
                intArrayOf(_rulesSize, area._rulesSize),
                arrayOf(_offsets, area._offsets))
        val intersectPoints = crossHelper.findCrossing()
        if (intersectPoints.size == 0) {
            if (contains(area.bounds())) {
                copy(area, this)
            } else if (!area.contains(bounds())) {
                reset()
            }
            return
        }

        val resultCoords = FloatArray(_coordsSize + area._coordsSize + intersectPoints.size)
        val resultRules = IntArray(_rulesSize + area._rulesSize + intersectPoints.size)
        val resultOffsets = IntArray(_rulesSize + area._rulesSize + intersectPoints.size)
        var resultCoordPos = 0
        var resultRulesPos = 0
        var isCurrentArea = true

        var point = intersectPoints[0]
        var nextPoint = intersectPoints[0]
        resultRules[resultRulesPos] = PathIterator.SEG_MOVETO
        resultOffsets[resultRulesPos++] = resultCoordPos

        do {
            resultCoords[resultCoordPos++] = point.x()
            resultCoords[resultCoordPos++] = point.y()

            val curIndex = point.endIndex(true)
            if (curIndex < 0 || area.containsExact(_coords[2 * curIndex], _coords[2 * curIndex + 1]) == 0) {
                isCurrentArea = !isCurrentArea
            } else if (area.containsExact(_coords[2 * curIndex], _coords[2 * curIndex + 1]) > 0) {
                isCurrentArea = true
            } else {
                isCurrentArea = false
            }

            nextPoint = nextIntersectPoint(intersectPoints, point, isCurrentArea)
            val coords = if (isCurrentArea) this._coords else area._coords
            val offsets = if (isCurrentArea) this._offsets else area._offsets
            val rules = if (isCurrentArea) this._rules else area._rules
            var offset = point.ruleIndex(isCurrentArea)
            var isCopyUntilZero = false

            if (point.ruleIndex(isCurrentArea) > nextPoint.ruleIndex(isCurrentArea)) {
                val rulesSize = if (isCurrentArea) this._rulesSize else area._rulesSize
                resultCoordPos = includeCoordsAndRules(
                        offset + 1, rulesSize, rules, offsets, resultRules, resultOffsets,
                        resultCoords, coords, resultRulesPos, resultCoordPos, point, isCurrentArea,
                        false, 1)
                resultRulesPos += rulesSize - offset - 1
                offset = 1
                isCopyUntilZero = true
            }

            var length = nextPoint.ruleIndex(isCurrentArea) - offset + 1

            if (isCopyUntilZero) {
                offset = 0
                isCopyUntilZero = false
            }
            if (length == offset &&
                    nextPoint.rule(isCurrentArea) != PathIterator.SEG_LINETO &&
                    nextPoint.rule(isCurrentArea) != PathIterator.SEG_CLOSE &&
                    point.rule(isCurrentArea) != PathIterator.SEG_LINETO &&
                    point.rule(isCurrentArea) != PathIterator.SEG_CLOSE) {
                isCopyUntilZero = true
                length++
            }

            resultCoordPos = includeCoordsAndRules(
                    offset, length, rules, offsets, resultRules, resultOffsets, resultCoords, coords,
                    resultRulesPos, resultCoordPos, nextPoint, isCurrentArea, true, 1)
            resultRulesPos = if (length <= offset || isCopyUntilZero)
                resultRulesPos + 1
            else
                resultRulesPos + length

            point = nextPoint
        } while (point !== intersectPoints[0])

        if (resultRules[resultRulesPos - 1] == PathIterator.SEG_LINETO) {
            resultRules[resultRulesPos - 1] = PathIterator.SEG_CLOSE
        } else {
            resultCoords[resultCoordPos++] = nextPoint.x()
            resultCoords[resultCoordPos++] = nextPoint.y()
            resultRules[resultRulesPos++] = PathIterator.SEG_CLOSE
        }

        resultOffsets[resultRulesPos - 1] = resultCoordPos
        _coords = resultCoords
        _rules = resultRules
        _offsets = resultOffsets
        _coordsSize = resultCoordPos
        _rulesSize = resultRulesPos
    }

    private fun intersectPolygon(area: Area) {
        val crossHelper = CrossingHelper(
                arrayOf(_coords, area._coords),
                intArrayOf(_coordsSize, area._coordsSize))
        val intersectPoints = crossHelper.findCrossing()
        if (intersectPoints.size == 0) {
            if (contains(area.bounds())) {
                copy(area, this)
            } else if (!area.contains(bounds())) {
                reset()
            }
            return
        }

        val resultCoords = FloatArray(_coordsSize + area._coordsSize + intersectPoints.size)
        val resultRules = IntArray(_rulesSize + area._rulesSize + intersectPoints.size)
        val resultOffsets = IntArray(_rulesSize + area._rulesSize + intersectPoints.size)
        var resultCoordPos = 0
        var resultRulesPos = 0
        var isCurrentArea = true

        var point = intersectPoints[0]
        resultRules[resultRulesPos] = PathIterator.SEG_MOVETO
        resultOffsets[resultRulesPos++] = resultCoordPos

        do {
            resultCoords[resultCoordPos++] = point.x()
            resultCoords[resultCoordPos++] = point.y()
            resultRules[resultRulesPos] = PathIterator.SEG_LINETO
            resultOffsets[resultRulesPos++] = resultCoordPos - 2
            val curIndex = point.endIndex(true)

            if (curIndex < 0 || area.containsExact(_coords[2 * curIndex], _coords[2 * curIndex + 1]) == 0) {
                isCurrentArea = !isCurrentArea
            } else if (area.containsExact(_coords[2 * curIndex], _coords[2 * curIndex + 1]) > 0) {
                isCurrentArea = true
            } else {
                isCurrentArea = false
            }

            val nextPoint = nextIntersectPoint(intersectPoints, point, isCurrentArea)
            val coords = if (isCurrentArea) this._coords else area._coords
            var offset = 2 * point.endIndex(isCurrentArea)
            if (offset >= 0 && nextPoint.begIndex(isCurrentArea) < point.endIndex(isCurrentArea)) {
                val coordSize = if (isCurrentArea) this._coordsSize else area._coordsSize
                val length = coordSize - offset
                System.arraycopy(coords, offset, resultCoords, resultCoordPos, length)

                for (i in 0..length / 2 - 1) {
                    resultRules[resultRulesPos] = PathIterator.SEG_LINETO
                    resultOffsets[resultRulesPos++] = resultCoordPos
                    resultCoordPos += 2
                }

                offset = 0
            }

            if (offset >= 0) {
                val length = 2 * nextPoint.begIndex(isCurrentArea) - offset + 2
                System.arraycopy(coords, offset, resultCoords, resultCoordPos, length)

                for (i in 0..length / 2 - 1) {
                    resultRules[resultRulesPos] = PathIterator.SEG_LINETO
                    resultOffsets[resultRulesPos++] = resultCoordPos
                    resultCoordPos += 2
                }
            }

            point = nextPoint
        } while (point !== intersectPoints[0])

        resultRules[resultRulesPos - 1] = PathIterator.SEG_CLOSE
        resultOffsets[resultRulesPos - 1] = resultCoordPos
        _coords = resultCoords
        _rules = resultRules
        _offsets = resultOffsets
        _coordsSize = resultCoordPos
        _rulesSize = resultRulesPos
    }

    private fun subtractCurvePolygon(area: Area) {
        val crossHelper = CurveCrossingHelper(
                arrayOf(_coords, area._coords),
                intArrayOf(_coordsSize, area._coordsSize),
                arrayOf(_rules, area._rules),
                intArrayOf(_rulesSize, area._rulesSize),
                arrayOf(_offsets, area._offsets))
        val intersectPoints = crossHelper.findCrossing()
        if (intersectPoints.size == 0 && contains(area.bounds())) {
            copy(area, this)
            return
        }

        val resultCoords = FloatArray(_coordsSize + area._coordsSize + intersectPoints.size)
        val resultRules = IntArray(_rulesSize + area._rulesSize + intersectPoints.size)
        val resultOffsets = IntArray(_rulesSize + area._rulesSize + intersectPoints.size)
        var resultCoordPos = 0
        var resultRulesPos = 0
        var isCurrentArea = true

        var point = intersectPoints[0]
        resultRules[resultRulesPos] = PathIterator.SEG_MOVETO
        resultOffsets[resultRulesPos++] = resultCoordPos

        do {
            resultCoords[resultCoordPos++] = point.x()
            resultCoords[resultCoordPos++] = point.y()
            val curIndex = _offsets[point.ruleIndex(true)] % _coordsSize
            if (area.containsExact(_coords[curIndex], _coords[curIndex + 1]) == 0) {
                isCurrentArea = !isCurrentArea
            } else if (area.containsExact(_coords[curIndex], _coords[curIndex + 1]) > 0) {
                isCurrentArea = false
            } else {
                isCurrentArea = true
            }

            val nextPoint = if (isCurrentArea)
                nextIntersectPoint(intersectPoints, point, isCurrentArea)
            else
                prevIntersectPoint(intersectPoints, point, isCurrentArea)
            val coords = if (isCurrentArea) this._coords else area._coords
            val offsets = if (isCurrentArea) this._offsets else area._offsets
            val rules = if (isCurrentArea) this._rules else area._rules
            var offset = if (isCurrentArea)
                point.ruleIndex(isCurrentArea)
            else
                nextPoint.ruleIndex(isCurrentArea)
            var isCopyUntilZero = false

            if (isCurrentArea && point.ruleIndex(isCurrentArea) > nextPoint.ruleIndex(isCurrentArea) || !isCurrentArea && nextPoint.ruleIndex(isCurrentArea) > nextPoint.ruleIndex(isCurrentArea)) {
                val rulesSize = if (isCurrentArea) this._rulesSize else area._rulesSize
                resultCoordPos = includeCoordsAndRules(
                        offset + 1, rulesSize, rules, offsets, resultRules, resultOffsets, resultCoords,
                        coords, resultRulesPos, resultCoordPos, point, isCurrentArea, false, 2)
                resultRulesPos += rulesSize - offset - 1
                offset = 1
                isCopyUntilZero = true
            }

            val length = nextPoint.ruleIndex(isCurrentArea) - offset + 1

            if (isCopyUntilZero) {
                offset = 0
                isCopyUntilZero = false
            }

            resultCoordPos = includeCoordsAndRules(
                    offset, length, rules, offsets, resultRules, resultOffsets, resultCoords, coords,
                    resultRulesPos, resultCoordPos, point, isCurrentArea, true, 2)

            if (length == offset && (rules[offset] == PathIterator.SEG_QUADTO || rules[offset] == PathIterator.SEG_CUBICTO)) {
                resultRulesPos++
            } else {
                resultRulesPos = if (length < offset || isCopyUntilZero)
                    resultRulesPos + 1
                else
                    resultRulesPos + length - offset
            }

            point = nextPoint
        } while (point !== intersectPoints[0])

        resultRules[resultRulesPos++] = PathIterator.SEG_CLOSE
        resultOffsets[resultRulesPos - 1] = resultCoordPos
        _coords = resultCoords
        _rules = resultRules
        _offsets = resultOffsets
        _coordsSize = resultCoordPos
        _rulesSize = resultRulesPos
    }

    private fun subtractPolygon(area: Area) {
        val crossHelper = CrossingHelper(
                arrayOf(_coords, area._coords),
                intArrayOf(_coordsSize, area._coordsSize))
        val intersectPoints = crossHelper.findCrossing()
        if (intersectPoints.size == 0) {
            if (contains(area.bounds())) {
                copy(area, this)
                return
            }
            return
        }

        val resultCoords = FloatArray(
                2 * (_coordsSize + area._coordsSize + intersectPoints.size))
        val resultRules = IntArray(2 * (_rulesSize + area._rulesSize + intersectPoints.size))
        val resultOffsets = IntArray(2 * (_rulesSize + area._rulesSize + intersectPoints.size))
        var resultCoordPos = 0
        var resultRulesPos = 0
        var isCurrentArea = true
        var countPoints = 0
        var curArea = false
        var addArea = false

        var point = intersectPoints[0]
        resultRules[resultRulesPos] = PathIterator.SEG_MOVETO
        resultOffsets[resultRulesPos++] = resultCoordPos

        do {
            resultCoords[resultCoordPos++] = point.x()
            resultCoords[resultCoordPos++] = point.y()
            resultRules[resultRulesPos] = PathIterator.SEG_LINETO
            resultOffsets[resultRulesPos++] = resultCoordPos - 2
            val curIndex = point.endIndex(true)

            if (curIndex < 0 || area.isVertex(_coords[2 * curIndex], _coords[2 * curIndex + 1]) &&
                    crossHelper.containsPoint(floatArrayOf(_coords[2 * curIndex], _coords[2 * curIndex + 1])) &&
                    (_coords[2 * curIndex] != point.x() || _coords[2 * curIndex + 1] != point.y())) {
                isCurrentArea = !isCurrentArea
            } else if (area.containsExact(_coords[2 * curIndex], _coords[2 * curIndex + 1]) > 0) {
                isCurrentArea = false
            } else {
                isCurrentArea = true
            }

            if (countPoints >= intersectPoints.size) {
                isCurrentArea = !isCurrentArea
            }

            if (isCurrentArea) {
                curArea = true
            } else {
                addArea = true
            }

            val nextPoint = if (isCurrentArea)
                nextIntersectPoint(intersectPoints, point, isCurrentArea)
            else
                prevIntersectPoint(intersectPoints, point, isCurrentArea)
            val coords = if (isCurrentArea) this._coords else area._coords

            var offset = if (isCurrentArea)
                2 * point.endIndex(isCurrentArea)
            else
                2 * nextPoint.endIndex(isCurrentArea)

            if (offset > 0 && (isCurrentArea && nextPoint.begIndex(isCurrentArea) < point.endIndex(isCurrentArea) || !isCurrentArea && nextPoint.endIndex(isCurrentArea) < nextPoint.begIndex(isCurrentArea))) {

                val coordSize = if (isCurrentArea) this._coordsSize else area._coordsSize
                val length = coordSize - offset

                if (isCurrentArea) {
                    System.arraycopy(coords, offset, resultCoords, resultCoordPos, length)
                } else {
                    val temp = FloatArray(length)
                    System.arraycopy(coords, offset, temp, 0, length)
                    reverseCopy(temp)
                    System.arraycopy(temp, 0, resultCoords, resultCoordPos, length)
                }

                for (i in 0..length / 2 - 1) {
                    resultRules[resultRulesPos] = PathIterator.SEG_LINETO
                    resultOffsets[resultRulesPos++] = resultCoordPos
                    resultCoordPos += 2
                }

                offset = 0
            }

            if (offset >= 0) {
                val length = if (isCurrentArea)
                    2 * nextPoint.begIndex(isCurrentArea) - offset + 2
                else
                    2 * point.begIndex(isCurrentArea) - offset + 2

                if (isCurrentArea) {
                    System.arraycopy(coords, offset, resultCoords, resultCoordPos, length)
                } else {
                    val temp = FloatArray(length)
                    System.arraycopy(coords, offset, temp, 0, length)
                    reverseCopy(temp)
                    System.arraycopy(temp, 0, resultCoords, resultCoordPos, length)
                }

                for (i in 0..length / 2 - 1) {
                    resultRules[resultRulesPos] = PathIterator.SEG_LINETO
                    resultOffsets[resultRulesPos++] = resultCoordPos
                    resultCoordPos += 2
                }
            }

            point = nextPoint
            countPoints++
        } while (point !== intersectPoints[0] || !(curArea && addArea))

        resultRules[resultRulesPos - 1] = PathIterator.SEG_CLOSE
        resultOffsets[resultRulesPos - 1] = resultCoordPos
        _coords = resultCoords
        _rules = resultRules
        _offsets = resultOffsets
        _coordsSize = resultCoordPos
        _rulesSize = resultRulesPos
    }

    private fun nextIntersectPoint(iPoints: Array<IntersectPoint>,
                                   isectPoint: IntersectPoint,
                                   isCurrentArea: Boolean): IntersectPoint {
        val endIndex = isectPoint.endIndex(isCurrentArea)
        if (endIndex < 0) {
            return iPoints[Math.abs(endIndex) - 1]
        }

        var firstIsectPoint: IntersectPoint? = null
        var nextIsectPoint: IntersectPoint? = null
        for (point in iPoints) {
            val begIndex = point.begIndex(isCurrentArea)
            if (begIndex >= 0) {
                if (firstIsectPoint == null) {
                    firstIsectPoint = point
                } else if (begIndex < firstIsectPoint.begIndex(isCurrentArea)) {
                    firstIsectPoint = point
                }
            }

            if (endIndex <= begIndex) {
                if (nextIsectPoint == null) {
                    nextIsectPoint = point
                } else if (begIndex < nextIsectPoint.begIndex(isCurrentArea)) {
                    nextIsectPoint = point
                }
            }
        }

        return if (nextIsectPoint != null) nextIsectPoint else firstIsectPoint!!
    }

    private fun prevIntersectPoint(iPoints: Array<IntersectPoint>,
                                   isectPoint: IntersectPoint,
                                   isCurrentArea: Boolean): IntersectPoint {
        val begIndex = isectPoint.begIndex(isCurrentArea)
        if (begIndex < 0) {
            return iPoints[Math.abs(begIndex) - 1]
        }

        var firstIsectPoint: IntersectPoint? = null
        var predIsectPoint: IntersectPoint? = null
        for (point in iPoints) {
            val endIndex = point.endIndex(isCurrentArea)
            if (endIndex >= 0) {
                if (firstIsectPoint == null) {
                    firstIsectPoint = point
                } else if (endIndex < firstIsectPoint.endIndex(isCurrentArea)) {
                    firstIsectPoint = point
                }
            }

            if (endIndex <= begIndex) {
                if (predIsectPoint == null) {
                    predIsectPoint = point
                } else if (endIndex > predIsectPoint.endIndex(isCurrentArea)) {
                    predIsectPoint = point
                }
            }
        }

        return if (predIsectPoint != null) predIsectPoint else firstIsectPoint!!
    }

    private fun includeCoordsAndRules(
            offset: Int, length: Int, rules: IntArray, offsets: IntArray, resultRules: IntArray, resultOffsets: IntArray,
            resultCoords: FloatArray, coords: FloatArray, resultRulesPos: Int, resultCoordPos: Int,
            point: IntersectPoint, isCurrentArea: Boolean, way: Boolean, operation: Int): Int {
        var length = length
        var resultRulesPos = resultRulesPos
        var way = way

        val temp = FloatArray(8 * length)
        var coordsCount = 0
        var isMoveIndex = true
        var isMoveLength = true
        var additional = false

        if (length <= offset) {
            for (i in resultRulesPos..resultRulesPos + 1 - 1) {
                resultRules[i] = PathIterator.SEG_LINETO
            }
        } else {
            var j = resultRulesPos
            for (i in offset..length - 1) {
                resultRules[j++] = PathIterator.SEG_LINETO
            }
        }

        if (length == offset && (rules[offset] == PathIterator.SEG_QUADTO || rules[offset] == PathIterator.SEG_CUBICTO)) {
            length++
            additional = true
        }

        for (i in offset..length - 1) {
            var index = offsets[i]
            if (!isMoveIndex) {
                index -= 2
            }

            if (!isMoveLength) {
                length++
                isMoveLength = true
            }

            when (rules[i]) {
                PathIterator.SEG_MOVETO -> {
                    isMoveIndex = false
                    isMoveLength = false
                }

                PathIterator.SEG_LINETO, PathIterator.SEG_CLOSE -> {
                    resultRules[resultRulesPos] = PathIterator.SEG_LINETO
                    resultOffsets[resultRulesPos++] = resultCoordPos + 2
                    var isLeft = CrossingHelper.compare(
                            coords[index], coords[index + 1], point.x(), point.y()) > 0
                    if (way || !isLeft) {
                        temp[coordsCount++] = coords[index]
                        temp[coordsCount++] = coords[index + 1]
                    }
                }

                PathIterator.SEG_QUADTO -> {
                    resultRules[resultRulesPos] = PathIterator.SEG_QUADTO
                    resultOffsets[resultRulesPos++] = resultCoordPos + 4
                    var coefs = floatArrayOf(coords[index - 2], coords[index - 1], coords[index], coords[index + 1], coords[index + 2], coords[index + 3])
                    var isLeft = CrossingHelper.compare(
                            coords[index - 2], coords[index - 1], point.x(), point.y()) > 0

                    if (!additional && (operation == 0 || operation == 2)) {
                        isLeft = !isLeft
                        way = false
                    }
                    GeometryUtil.subQuad(coefs, point.param(isCurrentArea), isLeft)

                    if (way || isLeft) {
                        temp[coordsCount++] = coefs[2]
                        temp[coordsCount++] = coefs[3]
                    } else {
                        System.arraycopy(coefs, 2, temp, coordsCount, 4)
                        coordsCount += 4
                    }
                }

                PathIterator.SEG_CUBICTO -> {
                    resultRules[resultRulesPos] = PathIterator.SEG_CUBICTO
                    resultOffsets[resultRulesPos++] = resultCoordPos + 6
                    var coefs = floatArrayOf(coords[index - 2], coords[index - 1], coords[index], coords[index + 1], coords[index + 2], coords[index + 3], coords[index + 4], coords[index + 5])
                    var isLeft = CrossingHelper.compare(
                            coords[index - 2], coords[index - 1], point.x(), point.y()) > 0
                    GeometryUtil.subCubic(coefs, point.param(isCurrentArea), !isLeft)

                    if (isLeft) {
                        System.arraycopy(coefs, 2, temp, coordsCount, 6)
                        coordsCount += 6
                    } else {
                        System.arraycopy(coefs, 2, temp, coordsCount, 4)
                        coordsCount += 4
                    }
                }
            }
        }

        if (operation == 2 && !isCurrentArea && coordsCount > 2) {
            reverseCopy(temp)
            System.arraycopy(temp, 0, resultCoords, resultCoordPos, coordsCount)
        } else {
            System.arraycopy(temp, 0, resultCoords, resultCoordPos, coordsCount)
        }

        return resultCoordPos + coordsCount
    }

    private fun copy(src: Area, dst: Area) {
        dst._coordsSize = src._coordsSize
        dst._coords = Platform.clone(src._coords)
        dst._rulesSize = src._rulesSize
        dst._rules = Platform.clone(src._rules)
        dst._moveToCount = src._moveToCount
        dst._offsets = Platform.clone(src._offsets)
    }

    private fun containsExact(x: Float, y: Float): Int {
        var pi = pathIterator(null)
        val crossCount = Crossing.crossPath(pi, x, y)
        if (Crossing.isInsideEvenOdd(crossCount)) {
            return 1
        }

        val segmentCoords = FloatArray(6)
        val resultPoints = FloatArray(6)
        var rule: Int
        var curX = -1f
        var curY = -1f
        var moveX = -1f
        var moveY = -1f

        pi = pathIterator(null)
        while (!pi.isDone) {
            rule = pi.currentSegment(segmentCoords)
            when (rule) {
                PathIterator.SEG_MOVETO -> {
                    curX = segmentCoords[0]
                    moveX = curX
                    curY = segmentCoords[1]
                    moveY = curY
                }

                PathIterator.SEG_LINETO -> {
                    if (GeometryUtil.intersectLines(curX, curY, segmentCoords[0], segmentCoords[1], x,
                            y, x, y, resultPoints) != 0) {
                        return 0
                    }
                    curX = segmentCoords[0]
                    curY = segmentCoords[1]
                }

                PathIterator.SEG_QUADTO -> {
                    if (GeometryUtil.intersectLineAndQuad(
                            x, y, x, y, curX, curY, segmentCoords[0], segmentCoords[1],
                            segmentCoords[2], segmentCoords[3], resultPoints) > 0) {
                        return 0
                    }
                    curX = segmentCoords[2]
                    curY = segmentCoords[3]
                }

                PathIterator.SEG_CUBICTO -> {
                    if (GeometryUtil.intersectLineAndCubic(
                            x, y, x, y, curX, curY, segmentCoords[0], segmentCoords[1],
                            segmentCoords[2], segmentCoords[3], segmentCoords[4], segmentCoords[5],
                            resultPoints) > 0) {
                        return 0
                    }
                    curX = segmentCoords[4]
                    curY = segmentCoords[5]
                }

                PathIterator.SEG_CLOSE -> {
                    if (GeometryUtil.intersectLines(
                            curX, curY, moveX, moveY, x, y, x, y, resultPoints) != 0) {
                        return 0
                    }
                    curX = moveX
                    curY = moveY
                }
            }
            pi.next()
        }
        return -1
    }

    private fun reverseCopy(coords: FloatArray) {
        val temp = FloatArray(coords.size)
        System.arraycopy(coords, 0, temp, 0, coords.size)
        var i = 0
        while (i < coords.size) {
            coords[i] = temp[coords.size - i - 2]
            coords[i + 1] = temp[coords.size - i - 1]
            i = i + 2
        }
    }

    private fun areaBoundsSquare(): Float {
        val bounds = bounds()
        return bounds.height() * bounds.width()
    }

    private fun isVertex(x: Float, y: Float): Boolean {
        var i = 0
        while (i < _coordsSize) {
            if (x == _coords[i++] && y == _coords[i++]) {
                return true
            }
        }
        return false
    }

    // the method check up the array size and necessarily increases it.
    private fun adjustSize(array: FloatArray, newSize: Int): FloatArray {
        if (newSize <= array.size) {
            return array
        }
        val newArray = FloatArray(2 * newSize)
        System.arraycopy(array, 0, newArray, 0, array.size)
        return newArray
    }

    private fun adjustSize(array: IntArray, newSize: Int): IntArray {
        if (newSize <= array.size) {
            return array
        }
        val newArray = IntArray(2 * newSize)
        System.arraycopy(array, 0, newArray, 0, array.size)
        return newArray
    }

    // the internal class implements PathIterator
    private inner class AreaPathIterator internal constructor(private val transform: Transform?) : PathIterator {
        private var curRuleIndex = 0
        private var curCoordIndex = 0

        override fun windingRule(): Int {
            return PathIterator.WIND_EVEN_ODD
        }

        override val isDone: Boolean
            get() = curRuleIndex >= _rulesSize

        override fun next() {
            when (_rules[curRuleIndex]) {
                PathIterator.SEG_MOVETO, PathIterator.SEG_LINETO -> curCoordIndex += 2
                PathIterator.SEG_QUADTO -> curCoordIndex += 4
                PathIterator.SEG_CUBICTO -> curCoordIndex += 6
            }
            curRuleIndex++
        }

        override fun currentSegment(c: FloatArray): Int {
            if (isDone) {
                throw NoSuchElementException("Iterator out of bounds")
            }

            var count = 0
            // the fallthrough below is on purpose
            when (_rules[curRuleIndex]) {
                PathIterator.SEG_CUBICTO -> {
                    c[4] = _coords[curCoordIndex + 4]
                    c[5] = _coords[curCoordIndex + 5]
                    count = 1
                    c[2] = _coords[curCoordIndex + 2]
                    c[3] = _coords[curCoordIndex + 3]
                    count += 1
                    c[0] = _coords[curCoordIndex]
                    c[1] = _coords[curCoordIndex + 1]
                    count += 1
                }
                PathIterator.SEG_QUADTO -> {
                    c[2] = _coords[curCoordIndex + 2]
                    c[3] = _coords[curCoordIndex + 3]
                    count += 1
                    c[0] = _coords[curCoordIndex]
                    c[1] = _coords[curCoordIndex + 1]
                    count += 1
                }
                PathIterator.SEG_MOVETO, PathIterator.SEG_LINETO -> {
                    c[0] = _coords[curCoordIndex]
                    c[1] = _coords[curCoordIndex + 1]
                    count += 1
                }
            }

            transform?.transform(c, 0, c, 0, count)

            return _rules[curRuleIndex]
        }
    }

    /** The coordinates array of the shape vertices.  */
    private var _coords = FloatArray(20)

    /** The coordinates quantity.  */
    private var _coordsSize = 0

    /** The _rules array for the drawing of the shape edges.  */
    private var _rules = IntArray(10)

    /** The _rules quantity.  */
    private var _rulesSize = 0

    /** _offsets[i] - index in array of _coords and i - index in array of _rules.  */
    private var _offsets = IntArray(10)

    /** The quantity of MOVETO rule occurrences.  */
    private var _moveToCount = 0

    /** True if the shape is polygonal.  */
    /**
     * Returns true if this area is polygonal.
     */
    var isPolygonal = true
        private set
}
