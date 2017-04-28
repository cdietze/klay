//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

/**
 * Does something extraordinary.
 */
interface IBox {
    /**
     * Returns a reference to the box's minimum extent.
     */
    fun minimumExtent(): IVector3

    /**
     * Returns a reference to the box's maximum extent.
     */
    fun maximumExtent(): IVector3

    /**
     * Returns the center of the box as a new vector.
     */
    fun center(): Vector3

    /**
     * Places the location of the center of the box into the given result vector.

     * @return a reference to the result vector, for chaining.
     */
    fun center(result: Vector3): Vector3

    /**
     * Returns the length of the box's diagonal (the distance from minimum to maximum extent).
     */
    fun diagonalLength(): Float

    /**
     * Returns the length of the box's longest edge.
     */
    fun longestEdge(): Float

    /**
     * Determines whether the box is empty (whether any of its minima are greater than their
     * corresponding maxima).
     */
    val isEmpty: Boolean

    /**
     * Retrieves one of the eight vertices of the box. The code parameter identifies the vertex
     * with flags indicating which values should be selected from the minimum extent, and which
     * from the maximum extent. For example, the code 011b selects the vertex with the minimum x,
     * maximum y, and maximum z.

     * @return a reference to the result, for chaining.
     */
    fun vertex(code: Int, result: Vector3): Vector3

    /**
     * Determines whether this box contains the specified point.
     */
    operator fun contains(point: IVector3): Boolean

    /**
     * Determines whether this box contains the specified point.
     */
    fun contains(x: Float, y: Float, z: Float): Boolean

    /**
     * Returns the sum of the Manhattan distances between the extents of this box and the
     * specified other box.
     */
    fun extentDistance(other: IBox): Float

    /**
     * Determines whether this box completely contains the specified box.
     */
    operator fun contains(other: IBox): Boolean

    /**
     * Determines whether this box intersects the specified other box.
     */
    fun intersects(other: IBox): Boolean

    /**
     * Expands this box to include the specified point.

     * @return a new box containing the result.
     */
    fun add(point: IVector3): Box

    /**
     * Expands this box to include the specified point, placing the result in the object
     * provided.

     * @return a reference to the result box, for chaining.
     */
    fun add(point: IVector3, result: Box): Box

    /**
     * Expands this box to include the bounds of another box.

     * @return a new box containing the result.
     */
    fun add(other: IBox): Box

    /**
     * Expands this box to include the bounds of another box, placing the result in the object
     * provided.

     * @return a reference to the result box, for chaining.
     */
    fun add(other: IBox, result: Box): Box

    /**
     * Finds the intersection between this box and another box.

     * @return a new box containing the result.
     */
    fun intersect(other: IBox): Box

    /**
     * Finds the intersection between this box and another box and places the result in the
     * provided object.

     * @return a reference to this box, for chaining.
     */
    fun intersect(other: IBox, result: Box): Box

    /**
     * Projects this box.

     * @return a new box containing the result.
     */
    fun project(matrix: IMatrix4): Box

    /**
     * Projects this box, placing the result in the object provided.

     * @return a reference to the result, for chaining.
     */
    fun project(matrix: IMatrix4, result: Box): Box

    /**
     * Expands the box by the specified amounts.

     * @return a new box containing the result.
     */
    fun expand(x: Float, y: Float, z: Float): Box

    /**
     * Expands the box by the specified amounts, placing the result in the object provided.

     * @return a reference to the result box, for chaining.
     */
    fun expand(x: Float, y: Float, z: Float, result: Box): Box

    /**
     * Determines whether the specified ray intersects this box.
     */
    fun intersects(ray: IRay3): Boolean

    /**
     * Finds the location of the (first) intersection between the specified ray and this box. This
     * will be the ray origin if the ray starts inside the box.

     * @param result a vector to hold the location of the intersection.
     * *
     * @return true if the ray intersects the box (in which case the result vector will be
     * * populated with the location of the intersection), false if not.
     */
    fun intersection(ray: IRay3, result: Vector3): Boolean
}
