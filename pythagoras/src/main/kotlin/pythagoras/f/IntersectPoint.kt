//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

/**
 * An internal helper class that represents the intersection point of two edges.
 */
internal class IntersectPoint {
    constructor(begIndex1: Int, endIndex1: Int, begIndex2: Int, endIndex2: Int,
                x: Float, y: Float) {
        this.begIndex1 = begIndex1
        this.endIndex1 = endIndex1
        this.begIndex2 = begIndex2
        this.endIndex2 = endIndex2
        this.x = x
        this.y = y
        this.rule1 = 0
        this.ruleIndex1 = 0
        this.param1 = 0f
        this.rule2 = 0
        this.ruleIndex2 = 0
        this.param2 = 0f
    }

    constructor(begIndex1: Int, endIndex1: Int, rule1: Int, ruleIndex1: Int,
                begIndex2: Int, endIndex2: Int, rule2: Int, ruleIndex2: Int,
                x: Float, y: Float, param1: Float, param2: Float) {
        this.begIndex1 = begIndex1
        this.endIndex1 = endIndex1
        this.rule1 = rule1
        this.ruleIndex1 = ruleIndex1
        this.param1 = param1
        this.begIndex2 = begIndex2
        this.endIndex2 = endIndex2
        this.rule2 = rule2
        this.ruleIndex2 = ruleIndex2
        this.param2 = param2
        this.x = x
        this.y = y
    }

    fun begIndex(isCurrentArea: Boolean): Int {
        return if (isCurrentArea) begIndex1 else begIndex2
    }

    fun endIndex(isCurrentArea: Boolean): Int {
        return if (isCurrentArea) endIndex1 else endIndex2
    }

    fun ruleIndex(isCurrentArea: Boolean): Int {
        return if (isCurrentArea) ruleIndex1 else ruleIndex2
    }

    fun param(isCurrentArea: Boolean): Float {
        return if (isCurrentArea) param1 else param2
    }

    fun rule(isCurrentArea: Boolean): Int {
        return if (isCurrentArea) rule1 else rule2
    }

    fun x(): Float {
        return x
    }

    fun y(): Float {
        return y
    }

    fun setBegIndex1(begIndex: Int) {
        this.begIndex1 = begIndex
    }

    fun setEndIndex1(endIndex: Int) {
        this.endIndex1 = endIndex
    }

    fun setBegIndex2(begIndex: Int) {
        this.begIndex2 = begIndex
    }

    fun setEndIndex2(endIndex: Int) {
        this.endIndex2 = endIndex
    }

    // the edge begin number of first line
    private var begIndex1: Int = 0
    // the edge end number of first line
    private var endIndex1: Int = 0
    // the edge rule of first figure
    private val rule1: Int
    // the index of the first figure rules array
    private val ruleIndex1: Int
    // the parameter value of edge1
    private val param1: Float
    // the edge begin number of second line
    private var begIndex2: Int = 0
    // the edge end number of second line
    private var endIndex2: Int = 0
    // the edge rule of second figure
    private val rule2: Int
    // the index of the second figure rules array
    private val ruleIndex2: Int
    // the absciss coordinate of the point
    private val x: Float
    // the ordinate coordinate of the point
    private val y: Float
    // the parameter value of edge2
    private val param2: Float
}
