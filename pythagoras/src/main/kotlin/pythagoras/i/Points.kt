//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.i

/**
 * Point-related utility methods.
 */
object Points {
    /**
     * Returns the squared Euclidian distance between the specified two points.
     */
    fun distanceSq(x1: Int, y1: Int, x2: Int, y2: Int): Int {
        var x2 = x2
        var y2 = y2
        x2 -= x1
        y2 -= y1
        return x2 * x2 + y2 * y2
    }

    /**
     * Returns the Euclidian distance between the specified two points, truncated to the nearest
     * integer.
     */
    fun distance(x1: Int, y1: Int, x2: Int, y2: Int): Int {
        return Math.sqrt(distanceSq(x1, y1, x2, y2).toDouble()).toInt()
    }

    /**
     * Returns the Manhattan distance between the specified two points.
     */
    fun manhattanDistance(x1: Int, y1: Int, x2: Int, y2: Int): Int {
        return Math.abs(x2 - x1) + Math.abs(y2 - y1)
    }

    /**
     * Returns a string describing the supplied point, of the form `+x+y`, `+x-y`,
     * `-x-y`, etc.
     */
    fun pointToString(x: Int, y: Int): String {
        val buf = StringBuilder()
        if (x >= 0) buf.append("+")
        buf.append(x)
        if (y >= 0) buf.append("+")
        buf.append(y)
        return buf.toString()
    }
}
