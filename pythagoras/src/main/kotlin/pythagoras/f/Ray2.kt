//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f
import java.lang.Math

/**
 * A ray consisting of an origin point and a unit direction vector.
 */
class Ray2 : IRay2 {
    /** The ray's point of origin.  */
    override val origin = Vector()

    /** The ray's unit direction vector.  */
    override val direction = Vector()

    /**
     * Creates a ray with the values contained in the supplied origin point and unit direction
     * vector.
     */
    constructor(origin: Vector, direction: Vector) {
        set(origin, direction)
    }

    /**
     * Copy constructor.
     */
    constructor(other: Ray2) {
        set(other)
    }

    /**
     * Creates an empty (invalid) ray.
     */
    constructor() {}

    /**
     * Copies the parameters of another ray.

     * @return a reference to this ray, for chaining.
     */
    fun set(other: IRay2): Ray2 {
        return set(other.origin, other.direction)
    }

    /**
     * Sets the ray parameters to the values contained in the supplied vectors.

     * @return a reference to this ray, for chaining.
     */
    operator fun set(origin: IVector, direction: IVector): Ray2 {
        this.origin.set(origin)
        this.direction.set(direction)
        return this
    }

    /**
     * Transforms this ray in-place.

     * @return a reference to this ray, for chaining.
     */
    fun transformLocal(transform: Transform): Ray2 {
        return transform(transform, this)
    }

    override // from IRay2
    fun transform(transform: Transform): Ray2 {
        return transform(transform, Ray2())
    }

    override // from IRay2
    fun transform(transform: Transform, result: Ray2): Ray2 {
        transform.transformPoint(origin, result.origin)
        transform.transform(direction, result.direction).normalizeLocal()
        return result
    }

    override // from IRay2
    fun intersects(pt: IVector): Boolean {
        if (Math.abs(direction.x) > Math.abs(direction.y)) {
            val t = (pt.x - origin.x) / direction.x
            return t >= 0f && origin.y + t * direction.y == pt.y
        } else {
            val t = (pt.y - origin.y) / direction.y
            return t >= 0f && origin.x + t * direction.x == pt.x
        }
    }

    override // from IRay2
    fun getIntersection(start: IVector, end: IVector, result: Vector): Boolean {
        // ray is a + t*b, segment is c + s*d
        val ax = origin.x
        val ay = origin.y
        val bx = direction.x
        val by = direction.y
        val cx = start.x
        val cy = start.y
        val dx = end.x - start.x
        val dy = end.y - start.y

        val divisor = bx * dy - by * dx
        if (Math.abs(divisor) < MathUtil.EPSILON) {
            // the lines are parallel (or the segment is zero-length)
            val t = Math.min(getIntersection(start), getIntersection(end))
            val isect = t != Float.MAX_VALUE
            if (isect) {
                origin.addScaled(direction, t, result)
            }
            return isect
        }
        val cxax = cx - ax
        val cyay = cy - ay
        val s = (by * cxax - bx * cyay) / divisor
        if (s < 0f || s > 1f) {
            return false
        }
        val t = (dy * cxax - dx * cyay) / divisor
        val isect = t >= 0f
        if (isect) {
            origin.addScaled(direction, t, result)
        }
        return isect
    }

    override // from IRay2
    fun getIntersection(start: IVector, end: IVector, radius: Float, result: Vector): Boolean {
        val startx = start.x
        val starty = start.y
        // compute the segment's line parameters
        var a = starty - end.y
        var b = end.x - startx
        val len = FloatMath.hypot(a, b)
        if (len < MathUtil.EPSILON) { // start equals end; check as circle
            return getIntersection(start, radius, result)
        }
        val rlen = 1f / len
        a *= rlen
        b *= rlen
        var c = -a * startx - b * starty

        // find out where the origin lies with respect to the top and bottom
        var dist = a * origin.x + b * origin.y + c
        val above = dist > +radius
        val below = dist < -radius
        val x: Float
        val y: Float
        if (above || below) { // check the intersection with the top/bottom boundary
            val divisor = a * direction.x + b * direction.y
            if (Math.abs(divisor) < MathUtil.EPSILON) { // lines are parallel
                return false
            }
            c += if (above) -radius else +radius
            val t = (-a * origin.x - b * origin.y - c) / divisor
            if (t < 0f) { // wrong direction
                return false
            }
            x = origin.x + t * direction.x
            y = origin.y + t * direction.y

        } else { // middle; check the origin
            x = origin.x
            y = origin.y
        }
        // see where the test point lies with respect to the start and end boundaries
        val tmp = a
        a = b
        b = -tmp
        c = -a * startx - b * starty
        dist = a * x + b * y + c
        if (dist < 0f) { // before start
            return getIntersection(start, radius, result)
        } else if (dist > len) { // after end
            return getIntersection(end, radius, result)
        } else { // middle
            result.set(x, y)
            return true
        }
    }

    override // from IRay2
    fun getIntersection(center: IVector, radius: Float, result: Vector): Boolean {
        // see if we start inside the circle
        if (origin.distanceSq(center) <= radius * radius) {
            result.set(origin)
            return true
        }
        // then if we intersect the circle
        val ax = origin.x - center.x
        val ay = origin.y - center.y
        val b = 2f * (direction.x * ax + direction.y * ay)
        val c = ax * ax + ay * ay - radius * radius
        val radicand = b * b - 4f * c
        if (radicand < 0f) {
            return false
        }
        val t = (-b - FloatMath.sqrt(radicand)) * 0.5f
        val isect = t >= 0f
        if (isect) {
            origin.addScaled(direction, t, result)
        }
        return isect
    }

    override // from IRay2
    fun getNearestPoint(point: IVector, result: Vector): Vector {
        var result = result
        if (result == null) {
            result = Vector()
        }
        val r = point.subtract(origin).dot(direction)
        result.set(origin.add(direction.scale(r)))
        return result
    }

    override fun toString(): String {
        return "[origin=$origin, direction=$direction]"
    }

    /**
     * Returns the parameter of the ray when it intersects the supplied point, or
     * [Float.MAX_VALUE] if there is no such intersection.
     */
    protected fun getIntersection(pt: IVector): Float {
        if (Math.abs(direction.x) > Math.abs(direction.y)) {
            val t = (pt.x - origin.x) / direction.x
            return if (t >= 0f && origin.y + t * direction.y == pt.y) t else Float.MAX_VALUE
        } else {
            val t = (pt.y - origin.y) / direction.y
            return if (t >= 0f && origin.x + t * direction.x == pt.x) t else Float.MAX_VALUE
        }
    }
}
