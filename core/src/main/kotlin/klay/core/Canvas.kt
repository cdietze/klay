package klay.core

/**
 * A 2D drawing canvas. Rendering is performed by the CPU into a bitmap.
 */
abstract class Canvas(val gfx: Graphics,
                      /** The image that underlies this canvas.  */
                      val image: Image) {

    /**
     * Values that may be used with
     * [Canvas.setCompositeOperation].
     */
    enum class Composite {
        /** A (B is ignored). Display the source image instead of the destination image.
         * `[Sa, Sc]`  */
        SRC,

        /** B atop A. Same as source-atop but using the destination image instead of the source image
         * and vice versa. `[Sa, Sa * Dc + Sc * (1 - Da)]`.  */
        DST_ATOP,

        /** A over B. Display the source image wherever the source image is opaque. Display the
         * destination image elsewhere. `[Sa + (1 - Sa)*Da, Rc = Sc + (1 - Sa)*Dc]`.  */
        SRC_OVER,

        /** B over A. Same as source-over but using the destination image instead of the source image
         * and vice versa. `[Sa + (1 - Sa)*Da, Rc = Dc + (1 - Da)*Sc]`.  */
        DST_OVER,

        /** A in B. Display the source image wherever both the source image and destination image are
         * opaque. Display transparency elsewhere. `[Sa * Da, Sc * Da]`.  */
        SRC_IN,

        /** B in A. Same as source-in but using the destination image instead of the
         * source image and vice versa. `[Sa * Da, Sa * Dc]`.  */
        DST_IN,

        /** A out B. Display the source image wherever the source image is opaque and the destination
         * image is transparent. Display transparency elsewhere.
         * `[Sa * (1 - Da), Sc * (1 - Da)]`.  */
        SRC_OUT,

        /** B out A. Same as source-out but using the destination image instead of
         * the source image and vice versa. `[Da * (1 - Sa), Dc * (1 - Sa)]`.  */
        DST_OUT,

        /** A atop B. Display the source image wherever both images are opaque. Display the destination
         * image wherever the destination image is opaque but the source image is transparent. Display
         * transparency elsewhere. `[Da, Sc * Da + (1 - Sa) * Dc]`.  */
        SRC_ATOP,

        /** A xor B. Exclusive OR of the source image and destination image.
         * `[Sa + Da - 2 * Sa * Da, Sc * (1 - Da) + (1 - Sa) * Dc]`.  */
        XOR,

        /** A * B. Multiplies the source and destination images. **NOTE:** this is not supported by
         * the HTML5 and Flash backends. `[Sa * Da, Sc * Dc]`.  */
        MULTIPLY
    }

    /**
     * Values that may be used with [Canvas.setLineCap].
     */
    enum class LineCap {
        BUTT, ROUND, SQUARE
    }

    /**
     * Values that may be used with [Canvas.setLineJoin].
     */
    enum class LineJoin {
        BEVEL, MITER, ROUND
    }

    /** Facilitates drawing images and image regions to a canvas.  */
    interface Drawable {
        val width: Float
        val height: Float
        fun draw(gc: Any, x: Float, y: Float, width: Float, height: Float)
        fun draw(gc: Any, dx: Float, dy: Float, dw: Float, dh: Float,
                 sx: Float, sy: Float, sw: Float, sh: Float)
    }

    /** The width of this canvas.  */
    val width: Float = image.width

    /** The height of this canvas.  */
    val height: Float = image.height

    /**
     * Returns an immutable snapshot of the image that backs this canvas. Subsequent changes to this
     * canvas will not be reflected in the returned image. If you are going to render a canvas
     * image into another canvas image a lot, using a snapshot can improve performance.
     */
    abstract fun snapshot(): Image

    /**
     * Informs the platform that this canvas, and its backing image will no longer be used. On some
     * platforms this can free up memory earlier than if we waited for the canvas to be garbage
     * collected.
     */
    fun close() {} // nada by default

    /** Clears the entire canvas to `rgba(0, 0, 0, 0)`.  */
    abstract fun clear(): Canvas

    /** Clears the specified region to `rgba (0, 0, 0, 0)`.  */
    abstract fun clearRect(x: Float, y: Float, width: Float, height: Float): Canvas

    /** Intersects the current clip with the specified path.  */
    abstract fun clip(clipPath: Path): Canvas

    /** Intersects the current clip with the supplied rectangle.  */
    abstract fun clipRect(x: Float, y: Float, width: Float, height: Float): Canvas

    /** Creates a path object.  */
    abstract fun createPath(): Path

    /** Creates a gradient fill pattern.  */
    abstract fun createGradient(config: Gradient.Config): Gradient

    /**
     * Draws `image` centered at the specified location. Subtracts `image.width/2` from x
     * and `image.height/2` from y.
     */
    fun drawCentered(image: Drawable, x: Float, y: Float): Canvas {
        return draw(image, x - image.width / 2, y - image.height / 2)
    }

    /**
     * Draws a scaled image at the specified location `(x, y)` size `(w x h)`.
     */
    fun draw(image: Drawable, x: Float, y: Float, w: Float = image.width, h: Float = image.height): Canvas {
        image.draw(gc(), x, y, w, h)
        isDirty = true
        return this
    }

    /**
     * Draws a subregion of a image `(sw x sh) @ (sx, sy)` at the specified size
     * `(dw x dh)` and location `(dx, dy)`.

     * TODO (jgw): Document whether out-of-bounds source coordinates clamp, repeat, or do nothing.
     */
    fun draw(image: Drawable, dx: Float, dy: Float, dw: Float, dh: Float,
             sx: Float, sy: Float, sw: Float, sh: Float): Canvas {
        image.draw(gc(), dx, dy, dw, dh, sx, sy, sw, sh)
        isDirty = true
        return this
    }

    /**
     * Draws a line between the two specified points.
     */
    abstract fun drawLine(x0: Float, y0: Float, x1: Float, y1: Float): Canvas

    /**
     * Draws a single point at the specified location.
     */
    abstract fun drawPoint(x: Float, y: Float): Canvas

    /**
     * Draws text at the specified location. The text will be drawn in the current fill color.
     */
    abstract fun drawText(text: String, x: Float, y: Float): Canvas

    /**
     * Fills a circle at the specified center and radius.
     */
    abstract fun fillCircle(x: Float, y: Float, radius: Float): Canvas

    /**
     * Fills the specified path.
     */
    abstract fun fillPath(path: Path): Canvas

    /**
     * Fills the specified rectangle.
     */
    abstract fun fillRect(x: Float, y: Float, width: Float, height: Float): Canvas

    /**
     * Fills the specified rounded rectangle.

     * @param x the x coordinate of the upper left of the rounded rectangle.
     * *
     * @param y the y coordinate of the upper left of the rounded rectangle.
     * *
     * @param width the width of the rounded rectangle.
     * *
     * @param height the width of the rounded rectangle.
     * *
     * @param radius the radius of the circle to use for the corner.
     */
    abstract fun fillRoundRect(x: Float, y: Float, width: Float, height: Float, radius: Float): Canvas

    /**
     * Fills the text at the specified location. The text will use the current fill color.
     */
    abstract fun fillText(text: TextLayout, x: Float, y: Float): Canvas

    /**
     * Restores the canvas's previous state.

     * @see .save
     */
    abstract fun restore(): Canvas

    /**
     * Rotates the current transformation matrix by the specified angle in radians.
     */
    abstract fun rotate(radians: Float): Canvas

    /**
     * The save and restore methods preserve and restore the state of the canvas,
     * but not specific paths or graphics.

     * The following values are saved:
     *
     *  * transformation matrix
     *  * clipping path
     *  * stroke color
     *  * stroke width
     *  * line cap
     *  * line join
     *  * miter limit
     *  * fill color or gradient
     *  * composite operation
     *
     */
    abstract fun save(): Canvas

    /**
     * Scales the current transformation matrix by the specified amount.
     */
    abstract fun scale(x: Float, y: Float): Canvas

    /**
     * Set the global alpha value to be used for all painting.
     *
     *
     * Values outside the range [0,1] will be clamped to the range [0,1].

     * @param alpha alpha value in range [0,1] where 0 is transparent and 1 is opaque
     */
    abstract fun setAlpha(alpha: Float): Canvas

    /**
     * Sets the Porter-Duff composite operation to be used for all painting.
     */
    abstract fun setCompositeOperation(composite: Composite): Canvas

    /**
     * Sets the color to be used for fill operations. This replaces any existing
     * fill gradient or pattern.
     */
    abstract fun setFillColor(color: Int): Canvas

    /**
     * Sets the gradient to be used for fill operations. This replaces any
     * existing fill color or pattern.
     */
    abstract fun setFillGradient(gradient: Gradient): Canvas

    /**
     * Sets the pattern to be used for fill operations. This replaces any existing
     * fill color or gradient.
     */
    abstract fun setFillPattern(pattern: Pattern): Canvas

    /**
     * Sets the line-cap mode for strokes.
     */
    abstract fun setLineCap(cap: LineCap): Canvas

    /**
     * Sets the line-join mode for strokes.
     */
    abstract fun setLineJoin(join: LineJoin): Canvas

    /**
     * Sets the miter limit for strokes.
     */
    abstract fun setMiterLimit(miter: Float): Canvas

    /**
     * Sets the color for strokes.
     */
    abstract fun setStrokeColor(color: Int): Canvas

    /**
     * Sets the width for strokes, in pixels.
     */
    abstract fun setStrokeWidth(strokeWidth: Float): Canvas

    /**
     * Strokes a circle at the specified center and radius.
     */
    abstract fun strokeCircle(x: Float, y: Float, radius: Float): Canvas

    /**
     * Strokes the specified path.
     */
    abstract fun strokePath(path: Path): Canvas

    /**
     * Strokes the specified rectangle.
     */
    abstract fun strokeRect(x: Float, y: Float, width: Float, height: Float): Canvas

    /**
     * Strokes the specified rounded rectangle.

     * @param x the x coordinate of the upper left of the rounded rectangle.
     * *
     * @param y the y coordinate of the upper left of the rounded rectangle.
     * *
     * @param width the width of the rounded rectangle.
     * *
     * @param height the width of the rounded rectangle.
     * *
     * @param radius the radius of the circle to use for the corner.
     */
    abstract fun strokeRoundRect(x: Float, y: Float, width: Float, height: Float,
                                 radius: Float): Canvas

    /**
     * Strokes the text at the specified location. The text will use the current stroke configuration
     * (color, width, etc.).
     */
    abstract fun strokeText(text: TextLayout, x: Float, y: Float): Canvas

    /** A helper function for creating a texture from this canvas's image, and then disposing this
     * canvas. This is useful for situations where you create a canvas, draw something in it, turn
     * it into a texture and then never use the canvas again.  */
    fun toTexture(config: Texture.Config = Texture.Config.DEFAULT): Texture {
        try {
            return image.createTexture(config)
        } finally {
            close()
        }
    }

    /**
     * Multiplies the current transformation matrix by the given matrix.
     */
    abstract fun transform(m11: Float, m12: Float, m21: Float, m22: Float, dx: Float, dy: Float): Canvas

    /**
     * Translates the current transformation matrix by the given amount.
     */
    abstract fun translate(x: Float, y: Float): Canvas

    /** Used to track modifications to our underlying image.  */
    protected var isDirty: Boolean = false

    init {
        if (width <= 0 || height <= 0)
            throw IllegalArgumentException(
                    "Canvas must be > 0 in width and height: " + width + "x" + height)
    }

    /** Returns the platform dependent graphics context for this canvas.  */
    protected abstract fun gc(): Any
}
/**
 * Draws `image` at the specified location `(x, y)`.
 */
/** Calls [.toTexture] with the default texture config.  */
