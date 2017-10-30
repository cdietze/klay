package klay.core

/**
 * A gradient fill pattern created by [Canvas.createGradient].
 */
abstract class Gradient {

    /** Used to create gradients.  */
    abstract class Config protected constructor(val colors: IntArray, val positions: FloatArray)

    /** Creates a linear gradient fill pattern. `(x0, y0)` and `(x1, y1)` specify the
     * start and end positions, while `(colors, positions)` specifies the color stops.  */
    class Linear(val x0: Float, val y0: Float, val x1: Float, val y1: Float, colors: IntArray, positions: FloatArray) : Config(colors, positions)

    /** Creates a radial gradient fill pattern. `(x, y, r)` specifies the circle covered by
     * this gradient, while `(colors, positions)` specifies the list of color stops.  */
    class Radial(val x: Float, val y: Float, val r: Float, colors: IntArray, positions: FloatArray) : Config(colors, positions)
}
