package klay.core

/**
 * A path object created by [Canvas.createPath].
 */
interface Path {

    /**
     * Resets the current path, removing all strokes and moving the position to (0, 0).
     * @return this path for convenient call chaining.
     */
    fun reset(): Path

    /**
     * Closes the path, returning the position to the beginning of the first stroke.
     * @return this path for convenient call chaining.
     */
    fun close(): Path

    /**
     * Moves the position to the given location.
     * @return this path for convenient call chaining.
     */
    fun moveTo(x: Float, y: Float): Path

    /**
     * Adds a line to the path, from the current position to the specified target.
     * @return this path for convenient call chaining.
     */
    fun lineTo(x: Float, y: Float): Path

    /**
     * Adds a quadratic curve to the path, from the current position to the specified target, with
     * the specified control point.
     * @return this path for convenient call chaining.
     */
    fun quadraticCurveTo(cpx: Float, cpy: Float, x: Float, y: Float): Path

    /**
     * Adds a bezier curve to the path, from the current position to the specified target, using the
     * supplied control points.
     * @return this path for convenient call chaining.
     */
    fun bezierTo(c1x: Float, c1y: Float, c2x: Float, c2y: Float, x: Float, y: Float): Path

    // TODO(jgw): fill rules (HTML Canvas doesn't seem to have anything)
    // Android has [inverse] winding, even-odd
    // Flash has even-odd, non-zero
}
