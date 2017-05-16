package klay.core

import pythagoras.f.*
import pythagoras.f.Vector
import pythagoras.i.Rectangle
import react.Closeable
import java.util.*

/**
 * A surface provides a simple drawing API to a GPU accelerated render target. This can be either
 * the main frame buffer, or a frame buffer bound to a texture.

 *
 * Note: all rendering operations to a surface must be enclosed in calls to
 * [Surface.begin] and [Surface.end]. This ensures that the batch into which
 * the surface is rendering is properly flushed to the GPU at the right times.
 */
open class Surface
/**
 * Creates a surface which will render to `target` using `defaultBatch` as its
 * default quad renderer.
 */
(gfx: Graphics, protected val target: RenderTarget, private var batch: QuadBatch) : Closeable {

    private val transformStack = ArrayList<AffineTransform>()
    private val colorTex: Texture

    private val scissors = ArrayList<Rectangle>()
    private var scissorDepth: Int = 0
    private var fillColor: Int = 0
    private var tint = Tint.NOOP_TINT
    private var patternTex: Texture? = null
    private var lastTrans: AffineTransform?

    private var checkIntersection: Boolean = false
    private val intersectionTestPoint = Point()
    private val intersectionTestSize = Vector()

    init {
        lastTrans = AffineTransform()
        transformStack.add(lastTrans!!)
        colorTex = gfx.colorTex()
        scale(target.xscale(), target.yscale())
    }

    /**
     * Configures this surface to check the bounds of drawn [Tile]s to ensure that they
     * intersect our visible bounds before adding them to our GPU batch. If you draw a lot of totally
     * out of bounds images, this may increase your draw performance.
     */
    fun setCheckIntersection(checkIntersection: Boolean) {
        this.checkIntersection = checkIntersection
    }

    /** Starts a series of drawing commands to this surface.  */
    fun begin(): Surface {
        target.bind()
        beginBatch(batch)
        return this
    }

    /** Completes a series of drawing commands to this surface.  */
    fun end(): Surface {
        batch!!.end()
        return this
    }

    /** Configures this surface to use `batch`, if non-null. NOOPs otherwise.
     * @return a batch which should be passed to [.popBatch] when rendering is done with this
     * * batch.
     */
    fun pushBatch(newBatch: QuadBatch?): QuadBatch? {
        if (newBatch == null) return null
        val oldBatch = batch
        batch!!.end()
        batch = beginBatch(newBatch)
        return oldBatch
    }

    /** Restores the batch that was in effect prior to a [.pushBatch] call.  */
    fun popBatch(oldBatch: QuadBatch?) {
        if (oldBatch != null) {
            batch!!.end()
            batch = beginBatch(oldBatch)
        }
    }

    /** Returns the current transform.  */
    fun tx(): AffineTransform {
        return lastTrans!!
    }

    /** Saves the current transform.  */
    fun saveTx(): Surface {
        lastTrans = lastTrans!!.copy()
        transformStack.add(lastTrans!!)
        return this
    }

    /** Restores the transform previously stored by [.saveTx].  */
    fun restoreTx(): Surface {
        var tsSize = transformStack.size
        assert(tsSize > 1) { "Unbalanced save/restore" }
        transformStack.removeAt(--tsSize)
        lastTrans = if (transformStack.isEmpty()) null else transformStack[tsSize - 1]
        return this
    }

    /** Starts a series of drawing commands that are clipped to the specified rectangle (in view
     * coordinates, not OpenGL coordinates). Thus must be followed by a call to [.endClipped]
     * when the clipped drawing commands are done.

     * @return whether the resulting clip rectangle is non-empty. *Note:* the caller may wish
     * * to skip their drawing if this returns false, but they must still call [.endClipped].
     */
    fun startClipped(x: Int, y: Int, width: Int, height: Int): Boolean {
        batch!!.flush() // flush any pending unclipped calls
        val r = pushScissorState(x, if (target.flip()) target.height() - y - height else y, width, height)
        batch!!.gl.glScissor(r.x, r.y, r.width, r.height)
        if (scissorDepth == 1) batch!!.gl.glEnable(GL20.GL_SCISSOR_TEST)
        batch!!.gl.checkError("startClipped")
        return !r.isEmpty
    }

    /** Ends a series of drawing commands that were clipped per a call to [.startClipped].  */
    fun endClipped() {
        batch!!.flush() // flush our clipped calls with SCISSOR_TEST still enabled
        val r = popScissorState()
        if (r == null)
            batch!!.gl.glDisable(GL20.GL_SCISSOR_TEST)
        else
            batch!!.gl.glScissor(r.x, r.y, r.width, r.height)
        batch!!.gl.checkError("endClipped")
    }

    /** Translates the current transformation matrix by the given amount.  */
    fun translate(x: Float, y: Float): Surface {
        tx().translate(x, y)
        return this
    }

    /** Scales the current transformation matrix by the specified amount on each axis.  */
    fun scale(sx: Float, sy: Float): Surface {
        tx().scale(sx, sy)
        return this
    }

    /** Rotates the current transformation matrix by the specified angle in radians.  */
    fun rotate(angle: Float): Surface {
        val sr = Math.sin(angle.toDouble()).toFloat()
        val cr = Math.cos(angle.toDouble()).toFloat()
        transform(cr, sr, -sr, cr, 0f, 0f)
        return this
    }

    /** Multiplies the current transformation matrix by the given matrix.  */
    fun transform(m00: Float, m01: Float, m10: Float, m11: Float, tx: Float, ty: Float): Surface {
        val top = tx()
        Transforms.multiply(top, m00, m01, m10, m11, tx, ty, top)
        return this
    }

    /**
     * Concatenates `xf` onto this surface's transform, accounting for the `origin`.
     */
    fun concatenate(xf: AffineTransform, originX: Float, originY: Float): Surface {
        val txf = tx()
        Transforms.multiply(txf, xf.m00, xf.m01, xf.m10, xf.m11, xf.tx, xf.ty, txf)
        if (originX != 0f || originY != 0f) txf.translate(-originX, -originY)
        return this
    }

    /**
     * Pre-concatenates `xf` onto this surface's transform.
     */
    fun preConcatenate(xf: AffineTransform): Surface {
        val txf = tx()
        Transforms.multiply(xf.m00, xf.m01, xf.m10, xf.m11, xf.tx, xf.ty, txf, txf)
        return this
    }

    /** Returns the currently configured alpha.  */
    fun alpha(): Float {
        return Tint.getAlpha(tint)
    }

    /** Set the alpha component of this surface's current tint. Note that this value will be
     * quantized to an integer between 0 and 255. Also see [.setTint].
     *
     * Values outside the range [0,1] will be clamped to the range [0,1].
     * @param alpha value in range [0,1] where 0 is transparent and 1 is opaque.
     */
    fun setAlpha(alpha: Float): Surface {
        tint = Tint.setAlpha(tint, alpha)
        return this
    }

    /** Returns the currently configured tint.  */
    fun tint(): Int {
        return tint
    }

    /** Sets the tint to be applied to draw operations, as `ARGB`. *NOTE:* this will
     * overwrite any value configured via [.setAlpha]. Either include your desired alpha in
     * the high bits of `tint` or call [.setAlpha] after calling this method.
     */
    fun setTint(tint: Int): Surface {
        this.tint = tint
        return this
    }

    /**
     * Combines `tint` with the current tint via [Tint.combine].
     * @return the tint prior to combination.
     */
    fun combineTint(tint: Int): Int {
        val otint = this.tint
        if (tint != Tint.NOOP_TINT) this.tint = Tint.combine(tint, otint)
        return otint
    }

    /** Sets the color to be used for fill operations. This replaces any existing fill color or
     * pattern.  */
    fun setFillColor(color: Int): Surface {
        // TODO: add this to state stack
        this.fillColor = color
        this.patternTex = null
        return this
    }

    /** Sets the texture to be used for fill operations. This replaces any existing fill color or
     * pattern.  */
    fun setFillPattern(texture: Texture): Surface {
        // TODO: add fill pattern to state stack
        this.patternTex = texture
        return this
    }

    /** Returns whether the given rectangle intersects the render target area of this surface.  */
    fun intersects(x: Float, y: Float, w: Float, h: Float): Boolean {
        tx().transform(intersectionTestPoint.set(x, y), intersectionTestPoint)
        tx().transform(intersectionTestSize.set(w, h), intersectionTestSize)
        val ix = intersectionTestPoint.x
        val iy = intersectionTestPoint.y
        val iw = intersectionTestSize.x
        val ih = intersectionTestSize.y

        if (scissorDepth > 0) {
            val scissor = scissors[scissorDepth - 1]
            return scissor.intersects(ix.toInt(), iy.toInt(), iw.toInt(), ih.toInt())
        }

        val tw = target.width().toFloat()
        val th = target.height().toFloat()
        return ix + iw > 0 && ix < tw && iy + ih > 0 && iy < th
    }

    /** Clears the entire surface to the specified color.
     * The channels are values in the range `[0,1]`.  */
    fun clear(red: Float = 0f, green: Float = 0f, blue: Float = 0f, alpha: Float = 0f): Surface {
        batch!!.gl.glClearColor(red, green, blue, alpha)
        batch!!.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        return this
    }

    /**
     * Draws a tile at the specified location `(x, y)` and size `(w x h)`.
     */
    fun draw(tile: Tile, x: Float, y: Float, w: Float = tile.width(), h: Float = tile.height()): Surface {
        if (!checkIntersection || intersects(x, y, w, h)) {
            tile.addToBatch(batch!!, tint, tx(), x, y, w, h)
        }
        return this
    }

    /**
     * Draws a tile at the specified location `(x, y)` and size `(w x h)`, with tint
     * `tint`. *Note:* this will override any tint and alpha currently configured on
     * this surface.
     */
    fun draw(tile: Tile, tint: Int, x: Float, y: Float, w: Float, h: Float): Surface {
        if (!checkIntersection || intersects(x, y, w, h)) {
            tile.addToBatch(batch!!, tint, tx(), x, y, w, h)
        }
        return this
    }

    /**
     * Draws a scaled subset of an image (defined by `(sx, sy)` and `(w x h)`) at the
     * specified location `(dx, dy)` and size `(dw x dh)`.
     */
    fun draw(tile: Tile, dx: Float, dy: Float, dw: Float, dh: Float,
             sx: Float, sy: Float, sw: Float, sh: Float): Surface {
        if (!checkIntersection || intersects(dx, dy, dw, dh)) {
            tile.addToBatch(batch!!, tint, tx(), dx, dy, dw, dh, sx, sy, sw, sh)
        }
        return this
    }

    /**
     * Draws a scaled subset of an image (defined by `(sx, sy)` and `(w x h)`) at the
     * specified location `(dx, dy)` and size `(dw x dh)`, with tint `tint`.
     * *Note:* this will override any tint and alpha currently configured on this surface.
     */
    fun draw(tile: Tile, tint: Int, dx: Float, dy: Float, dw: Float, dh: Float,
             sx: Float, sy: Float, sw: Float, sh: Float): Surface {
        if (!checkIntersection || intersects(dx, dy, dw, dh)) {
            tile.addToBatch(batch!!, tint, tx(), dx, dy, dw, dh, sx, sy, sw, sh)
        }
        return this
    }

    /**
     * Draws a texture tile, centered at the specified location.
     */
    fun drawCentered(tile: Tile, x: Float, y: Float): Surface {
        return draw(tile, x - tile.width() / 2, y - tile.height() / 2)
    }

    /**
     * Fills a line between the specified coordinates, of the specified display unit width.
     */
    fun drawLine(a: XY, b: XY, width: Float): Surface {
        return drawLine(a.x, a.y, b.x, b.y, width)
    }

    /**
     * Fills a line between the specified coordinates, of the specified display unit width.
     */
    fun drawLine(x0: Float, y0: Float, x1: Float, y1: Float, width: Float): Surface {
        var x0 = x0
        var y0 = y0
        var x1 = x1
        var y1 = y1
        // swap the line end points if x1 is less than x0
        if (x1 < x0) {
            var temp = x0
            x0 = x1
            x1 = temp
            temp = y0
            y0 = y1
            y1 = temp
        }

        val dx = x1 - x0
        val dy = y1 - y0
        val length = MathUtil.sqrt(dx * dx + dy * dy)
        val wx = dx * (width / 2) / length
        val wy = dy * (width / 2) / length

        val xf = AffineTransform()
        xf.setRotation(MathUtil.atan2(dy, dx))
        xf.setTranslation(x0 + wy, y0 - wx)
        Transforms.multiply(tx(), xf, xf)

        if (patternTex != null) {
            batch!!.addQuad(patternTex!!, tint, xf, 0f, 0f, length, width)
        } else {
            batch!!.addQuad(colorTex, Tint.combine(fillColor, tint), xf, 0f, 0f, length, width)
        }
        return this
    }

    /**
     * Fills the specified rectangle.
     */
    fun fillRect(x: Float, y: Float, width: Float, height: Float): Surface {
        if (patternTex != null) {
            batch!!.addQuad(patternTex!!, tint, tx(), x, y, width, height)
        } else {
            batch!!.addQuad(colorTex, Tint.combine(fillColor, tint), tx(), x, y, width, height)
        }
        return this
    }

    override fun close() {
        // nothing; this exists to make life easier for users of TextureSurface
    }

    private fun beginBatch(batch: QuadBatch): QuadBatch {
        batch.begin(target.width().toFloat(), target.height().toFloat(), target.flip())
        return batch
    }

    private fun pushScissorState(x: Int, y: Int, width: Int, height: Int): Rectangle {
        // grow the scissors buffer if necessary
        if (scissorDepth == scissors.size) scissors.add(Rectangle())

        val r = scissors[scissorDepth]
        if (scissorDepth == 0)
            r.setBounds(x, y, width, height)
        else {
            // intersect current with previous
            val pr = scissors[scissorDepth - 1]
            r.setLocation(Math.max(pr.x, x), Math.max(pr.y, y))
            r.setSize(Math.max(Math.min(pr.maxX(), x + width - 1) - r.x, 0),
                    Math.max(Math.min(pr.maxY(), y + height - 1) - r.y, 0))
        }
        scissorDepth++
        return r
    }

    private fun popScissorState(): Rectangle? {
        scissorDepth--
        return if (scissorDepth == 0) null else scissors[scissorDepth - 1]
    }

    // /**
    //  * Fills the supplied batch of triangles with the current fill color or pattern. Note: this
    //  * method is only performant on OpenGL-based backends  (Android, iOS, HTML-WebGL, etc.). On
    //  * non-OpenGL-based backends  (HTML-Canvas, HTML-Flash) it converts the triangles to a path on
    //  * every rendering call.
    //  *
    //  * @param xys the xy coordinates of the triangles, as an array: {@code [x1, y1, x2, y2, ...]}.
    //  * @param indices the index of each vertex of each triangle in the {@code xys} array.
    //  */
    // public Surface fillTriangles (float[] xys, int[] indices) {
    //   return fillTriangles(xys, 0, xys.length, indices, 0, indices.length, 0);
    // }

    // /**
    //  * Fills the supplied batch of triangles with the current fill color or pattern.
    //  *
    //  * <p>Note: this method is only performant on OpenGL-based backends  (Android, iOS, HTML-WebGL,
    //  * etc.). On non-OpenGL-based backends  (HTML-Canvas, HTML-Flash) it converts the triangles to a
    //  * path on every rendering call.</p>
    //  *
    //  * @param xys the xy coordinates of the triangles, as an array: {@code [x1, y1, x2, y2, ...]}.
    //  * @param xysOffset the offset of the coordinates array, must not be negative and no greater than
    //  * {@code xys.length}. Note: this is an absolute offset; since {@code xys} contains pairs of
    //  * values, this will be some multiple of two.
    //  * @param xysLen the number of coordinates to read, must be no less than zero and no greater than
    //  * {@code xys.length - xysOffset}. Note: this is an absolute length; since {@code xys} contains
    //  * pairs of values, this will be some multiple of two.
    //  * @param indices the index of each vertex of each triangle in the {@code xys} array. Because
    //  * this method renders a slice of {@code xys}, one must also specify {@code indexBase} which
    //  * tells us how to interpret indices. The index into {@code xys} will be computed as: {@code
    //  * 2* (indices[ii] - indexBase)}, so if your indices reference vertices relative to the whole
    //  * array you should pass {@code xysOffset/2} for {@code indexBase}, but if your indices reference
    //  * vertices relative to <em>the slice</em> then you should pass zero.
    //  * @param indicesOffset the offset of the indices array, must not be negative and no greater than
    //  * {@code indices.length}.
    //  * @param indicesLen the number of indices to read, must be no less than zero and no greater than
    //  * {@code indices.length - indicesOffset}.
    //  * @param indexBase the basis for interpreting {@code indices}. See the docs for {@code indices}
    //  * for details.
    //  */
    // public Surface fillTriangles (float[] xys, int xysOffset, int xysLen,
    //                               int[] indices, int indicesOffset, int indicesLen,
    //                               int indexBase) {
    //   GLShader shader = ctx.trisShader(this.shader);
    //   if (patternTex != null) {
    //     int tex = patternTex.ensureTexture();
    //     if (tex > 0) {
    //       shader.prepareTexture(tex, tint);
    //       shader.addTriangles(tx(), xys, xysOffset, xysLen,
    //                           patternTex.width(), patternTex.height(),
    //                           indices, indicesOffset, indicesLen, indexBase);
    //     }
    //   } else {
    //     int tex = ctx.fillImage().ensureTexture();
    //     shader.prepareTexture(tex, Tint.combine(fillColor, tint));
    //     shader.addTriangles(tx(), xys, xysOffset, xysLen, 1, 1,
    //                         indices, indicesOffset, indicesLen, indexBase);
    //   }
    //   return this;
    // }

    // /**
    //  * Fills the supplied batch of triangles with the current fill pattern.
    //  *
    //  * <p>Note: this method only honors the texture coordinates on OpenGL-based backends  (Anrdoid,
    //  * iOS, HTML-WebGL, etc.). On non-OpenGL-based backends  (HTML-Canvas, HTML-Flash) it behaves like
    //  * a call to {@link #fillTriangles (float[],int[])}.</p>
    //  *
    //  * @param xys see {@link #fillTriangles (float[],int[])}.
    //  * @param sxys the texture coordinates for each vertex of the triangles, as an array:
    //  * {@code [sx1, sy1, sx2, sy2, ...]}. This must be the same length as {@code xys}.
    //  * @param indices see {@link #fillTriangles (float[],int[])}.
    //  *
    //  * @throws IllegalStateException if no fill pattern is currently set.
    //  */
    // public Surface fillTriangles (float[] xys, float[] sxys, int[] indices) {
    //   return fillTriangles(xys, sxys, 0, xys.length, indices, 0, indices.length, 0);
    // }

    // /**
    //  * Fills the supplied batch of triangles with the current fill pattern.
    //  *
    //  * <p>Note: this method only honors the texture coordinates on OpenGL-based backends  (Anrdoid,
    //  * iOS, HTML-WebGL, etc.). On non-OpenGL-based backends  (HTML-Canvas, HTML-Flash) it behaves like
    //  * a call to {@link #fillTriangles (float[],int[])}.</p>
    //  *
    //  * @param xys see {@link #fillTriangles (float[],int,int,int[],int,int,int)}.
    //  * @param sxys the texture coordinates for each vertex of the triangles, as an array.
    //  * {@code [sx1, sy1, sx2, sy2, ...]}. This must be the same length as {@code xys}.
    //  * @param xysOffset see {@link #fillTriangles (float[],int,int,int[],int,int,int)}.
    //  * @param xysLen see {@link #fillTriangles (float[],int,int,int[],int,int,int)}.
    //  * @param indices see {@link #fillTriangles (float[],int,int,int[],int,int,int)}.
    //  * @param indicesOffset see {@link #fillTriangles (float[],int,int,int[],int,int,int)}.
    //  * @param indicesLen see {@link #fillTriangles (float[],int,int,int[],int,int,int)}.
    //  * @param indexBase see {@link #fillTriangles (float[],int,int,int[],int,int,int)}.
    //  *
    //  * @throws IllegalStateException if no fill pattern is currently set.
    //  */
    // public Surface fillTriangles (float[] xys, float[] sxys, int xysOffset, int xysLen,
    //                               int[] indices, int indicesOffset, int indicesLen,
    //                               int indexBase) {
    //   if (patternTex == null) throw new IllegalStateException("No fill pattern currently set");
    //   int tex = patternTex.ensureTexture();
    //   if (tex > 0) {
    //     GLShader shader = ctx.trisShader(this.shader).prepareTexture(tex, tint);
    //     shader.addTriangles(tx(), xys, sxys, xysOffset, xysLen,
    //                         indices, indicesOffset, indicesLen, indexBase);
    //   }
    //   return this;
    // }
}
/** Clears the entire surface to transparent blackness.  */
/** Draws a tile at the specified location: `x, y`.  */
