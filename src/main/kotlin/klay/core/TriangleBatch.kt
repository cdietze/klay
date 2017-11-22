package klay.core

import euklid.f.AffineTransform
import klay.core.GL20.Companion.GL_ARRAY_BUFFER
import klay.core.GL20.Companion.GL_ELEMENT_ARRAY_BUFFER
import klay.core.GL20.Companion.GL_FLOAT
import klay.core.GL20.Companion.GL_STREAM_DRAW
import klay.core.GL20.Companion.GL_TEXTURE0
import klay.core.GL20.Companion.GL_TRIANGLES
import klay.core.GL20.Companion.GL_UNSIGNED_SHORT

/**
 * A batch which renders indexed triangles. It serves as a [QuadBatch], but can also render
 * arbitrary triangles via [.addTris].
 */
open class TriangleBatch
/** Creates a triangle batch with the supplied custom shader program.  */
constructor(gl: GL20, source: TriangleBatch.Source = TriangleBatch.Source()) : QuadBatch(gl) {

    /** The source for the stock triangle batch shader program.  */
    open class Source : TexturedBatch.Source() {

        /** Returns the source of the vertex shader program.  */
        open fun vertex(): String {
            return VERT_UNIFS +
                    VERT_ATTRS +
                    PER_VERT_ATTRS +
                    VERT_VARS +
                    "void main(void) {\n" +
                    VERT_SETPOS +
                    VERT_SETTEX +
                    VERT_SETCOLOR +
                    "}"
        }

        companion object {

            /** Declares the uniform variables for our shader.  */
            val VERT_UNIFS = "uniform vec2 u_HScreenSize;\n" + "uniform float u_Flip;\n"

            /** The same-for-all-verts-in-a-quad attribute variables for our shader.  */
            val VERT_ATTRS =
                    "attribute vec4 a_Matrix;\n" +
                            "attribute vec2 a_Translation;\n" +
                            "attribute vec2 a_Color;\n"

            /** The varies-per-vert attribute variables for our shader.  */
            val PER_VERT_ATTRS = "attribute vec2 a_Position;\n" + "attribute vec2 a_TexCoord;\n"

            /** Declares the varying variables for our shader.  */
            val VERT_VARS = "varying vec2 v_TexCoord;\n" + "varying vec4 v_Color;\n"

            /** The shader code that computes `gl_Position`.  */
            val VERT_SETPOS =
                    // Transform the vertex.
                    "mat3 transform = mat3(\n" +
                            "  a_Matrix[0],      a_Matrix[1],      0,\n" +
                            "  a_Matrix[2],      a_Matrix[3],      0,\n" +
                            "  a_Translation[0], a_Translation[1], 1);\n" +
                            "gl_Position = vec4(transform * vec3(a_Position, 1.0), 1);\n" +
                            // Scale from screen coordinates to [0, 2].
                            "gl_Position.xy /= u_HScreenSize.xy;\n" +
                            // Offset to [-1, 1].
                            "gl_Position.xy -= 1.0;\n" +
                            // If requested, flip the y-axis.
                            "gl_Position.y *= u_Flip;\n"

            /** The shader code that computes `v_TexCoord`.  */
            val VERT_SETTEX = "v_TexCoord = a_TexCoord;\n"

            /** The shader code that computes `v_Color`.  */
            val VERT_SETCOLOR =
                    // tint is encoded as two floats A*R and G*B where A, R, G, B are (0 - 255)
                    "float red = mod(a_Color.x, 256.0);\n" +
                            "float alpha = (a_Color.x - red) / 256.0;\n" +
                            "float blue = mod(a_Color.y, 256.0);\n" +
                            "float green = (a_Color.y - blue) / 256.0;\n" +
                            "v_Color = vec4(red / 255.0, green / 255.0, blue / 255.0, alpha / 255.0);\n"
        }
    }

    private val delayedBinding: Boolean = "Intel" == gl.glGetString(GL20.GL_VENDOR)

    protected val program: GLProgram
    protected val uTexture: Int
    protected val uHScreenSize: Int
    protected val uFlip: Int
    protected val aMatrix: Int
    protected val aTranslation: Int
    protected val aColor: Int // stable (same for whole quad)
    protected val aPosition: Int
    protected val aTexCoord: Int // changing (varies per quad vertex)

    protected val verticesId: Int
    protected val elementsId: Int
    protected val stableAttrs: FloatArray
    protected var vertices: FloatArray
    protected var elements: ShortArray
    protected var vertPos: Int = 0
    protected var elemPos: Int = 0

    init {

        program = GLProgram(gl, source.vertex(), source.fragment())
        uTexture = program.getUniformLocation("u_Texture")
        uHScreenSize = program.getUniformLocation("u_HScreenSize")
        uFlip = program.getUniformLocation("u_Flip")
        aMatrix = program.getAttribLocation("a_Matrix")
        aTranslation = program.getAttribLocation("a_Translation")
        aColor = program.getAttribLocation("a_Color")
        aPosition = program.getAttribLocation("a_Position")
        aTexCoord = program.getAttribLocation("a_TexCoord")

        // create our vertex and index buffers
        stableAttrs = FloatArray(stableAttrsSize())
        vertices = FloatArray(START_VERTS * vertexSize())
        elements = ShortArray(START_ELEMS)

        // create our GL buffers
        val ids = IntArray(2)
        gl.glGenBuffers(2, ids, 0)
        verticesId = ids[0]
        elementsId = ids[1]

        gl.checkError("TriangleBatch end ctor")
    }

    /**
     * Prepares to add primitives with the specified tint and transform. This configures
     * [.stableAttrs] with all of the attributes that are the same for every vertex.
     */
    fun prepare(tint: Int, xf: AffineTransform) {
        prepare(tint, xf.m00, xf.m01, xf.m10, xf.m11, xf.tx, xf.ty)
    }

    /**
     * See [.prepare].
     */
    fun prepare(tint: Int, m00: Float, m01: Float, m10: Float, m11: Float, tx: Float, ty: Float) {
        val stables = stableAttrs
        stables[0] = m00
        stables[1] = m01
        stables[2] = m10
        stables[3] = m11
        stables[4] = tx
        stables[5] = ty
        stables[6] = (tint shr 16 and 0xFFFF).toFloat() // ar
        stables[7] = (tint shr 0 and 0xFFFF).toFloat() // gb
        addExtraStableAttrs(stables, 8)
    }

    /**
     * Adds a collection of textured triangles to the current render operation.
     * @param xys a list of x/y coordinates as: `[x1, y1, x2, y2, ...]`.
     *
     * @param xysOffset the offset of the coordinates array, must not be negative and no greater than
     * `xys.length`. Note: this is an absolute offset; since `xys` contains pairs of
     * values, this will be some multiple of two.
     *
     * @param xysLen the number of coordinates to read, must be no less than zero and no greater than
     * `xys.length - xysOffset`. Note: this is an absolute length; since `xys` contains
     * pairs of values, this will be some multiple of two.
     *
     * @param tw the width of the texture for which we will auto-generate texture coordinates.
     *
     * @param th the height of the texture for which we will auto-generate texture coordinates.
     *
     * @param indices the index of the triangle vertices in the `xys` array. Because this
     * method renders a slice of `xys`, one must also specify `indexBase` which tells us
     * how to interpret indices. The index into `xys` will be computed as:
     * `2*(indices[ ii ] - indexBase)`, so if your indices reference vertices relative to the
     * whole array you should pass `xysOffset/2` for `indexBase`, but if your indices
     * reference vertices relative to *the slice* then you should pass zero.
     *
     * @param indicesOffset the offset of the indices array, must not be negative and no greater than
     * `indices.length`.
     *
     * @param indicesLen the number of indices to read, must be no less than zero and no greater than
     * `indices.length - indicesOffset`.
     *
     * @param indexBase the basis for interpreting `indices`. See the docs for `indices`
     * for details.
     */
    fun addTris(tex: Texture, tint: Int, xf: AffineTransform,
                xys: FloatArray, xysOffset: Int, xysLen: Int, tw: Float, th: Float,
                indices: IntArray, indicesOffset: Int, indicesLen: Int, indexBase: Int) {
        setTexture(tex)
        prepare(tint, xf)
        addTris(xys, xysOffset, xysLen, tw, th, indices, indicesOffset, indicesLen, indexBase)
    }

    /**
     * Adds a collection of textured triangles to the current render operation. See
     * [.addTris]
     * for parameter documentation.

     * @param sxys a list of sx/sy texture coordinates as: `[sx1, sy1, sx2, sy2, ...]`. This
     * * must be of the same length as `xys`.
     */
    fun addTris(tex: Texture, tint: Int, xf: AffineTransform,
                xys: FloatArray, sxys: FloatArray, xysOffset: Int, xysLen: Int,
                indices: IntArray, indicesOffset: Int, indicesLen: Int, indexBase: Int) {
        setTexture(tex)
        prepare(tint, xf)
        addTris(xys, sxys, xysOffset, xysLen, indices, indicesOffset, indicesLen, indexBase)
    }

    /**
     * Adds triangle primitives to a prepared batch. This must be preceded by calls to
     * [.setTexture] and [.prepare] to configure the texture and stable attributes.
     */
    fun addTris(xys: FloatArray, xysOffset: Int, xysLen: Int, tw: Float, th: Float,
                indices: IntArray, indicesOffset: Int, indicesLen: Int, indexBase: Int) {
        val vertIdx = beginPrimitive(xysLen / 2, indicesLen)
        var offset = vertPos
        val verts = vertices
        val stables = stableAttrs
        var ii = xysOffset
        val ll = ii + xysLen
        while (ii < ll) {
            val x = xys[ii]
            val y = xys[ii + 1]
            offset = add(verts, add(verts, offset, stables), x, y, x / tw, y / th)
            ii += 2
        }
        vertPos = offset

        addElems(vertIdx, indices, indicesOffset, indicesLen, indexBase)
    }

    /**
     * Adds triangle primitives to a prepared batch. This must be preceded by calls to
     * [.setTexture] and [.prepare] to configure the texture and stable attributes.
     */
    fun addTris(xys: FloatArray, sxys: FloatArray, xysOffset: Int, xysLen: Int,
                indices: IntArray, indicesOffset: Int, indicesLen: Int, indexBase: Int) {
        val vertIdx = beginPrimitive(xysLen / 2, indicesLen)
        var offset = vertPos
        val verts = vertices
        val stables = stableAttrs
        var ii = xysOffset
        val ll = ii + xysLen
        while (ii < ll) {
            offset = add(verts, add(verts, offset, stables), xys[ii], xys[ii + 1], sxys[ii], sxys[ii + 1])
            ii += 2
        }
        vertPos = offset

        addElems(vertIdx, indices, indicesOffset, indicesLen, indexBase)
    }

    override fun addQuad(tint: Int,
                         m00: Float, m01: Float, m10: Float, m11: Float, tx: Float, ty: Float,
                         x1: Float, y1: Float, sx1: Float, sy1: Float,
                         x2: Float, y2: Float, sx2: Float, sy2: Float,
                         x3: Float, y3: Float, sx3: Float, sy3: Float,
                         x4: Float, y4: Float, sx4: Float, sy4: Float) {
        prepare(tint, m00, m01, m10, m11, tx, ty)

        val vertIdx = beginPrimitive(4, 6)
        var offset = vertPos
        val verts = vertices
        val stables = stableAttrs
        offset = add(verts, add(verts, offset, stables), x1, y1, sx1, sy1)
        offset = add(verts, add(verts, offset, stables), x2, y2, sx2, sy2)
        offset = add(verts, add(verts, offset, stables), x3, y3, sx3, sy3)
        offset = add(verts, add(verts, offset, stables), x4, y4, sx4, sy4)
        vertPos = offset

        addElems(vertIdx, QUAD_INDICES, 0, QUAD_INDICES.size, 0)
    }

    override fun begin(fbufWidth: Float, fbufHeight: Float, flip: Boolean) {
        super.begin(fbufWidth, fbufHeight, flip)
        program.activate()
        gl.glUniform2f(uHScreenSize, fbufWidth / 2f, fbufHeight / 2f)
        gl.glUniform1f(uFlip, (if (flip) -1 else 1).toFloat())
        // certain graphics cards (I'm looking at you, Intel) exhibit broken behavior if we bind our
        // attributes once during activation, so for those cards we bind every time in flush()
        if (!delayedBinding) bindAttribsBufs()
        gl.checkError("TriangleBatch begin")
    }

    private fun bindAttribsBufs() {
        gl.glBindBuffer(GL_ARRAY_BUFFER, verticesId)

        // bind our stable vertex attributes
        val stride = vertexStride()
        glBindVertAttrib(aMatrix, 4, GL_FLOAT, stride, 0)
        glBindVertAttrib(aTranslation, 2, GL_FLOAT, stride, 16)
        glBindVertAttrib(aColor, 2, GL_FLOAT, stride, 24)

        // bind our changing vertex attributes
        val offset = stableAttrsSize() * FLOAT_SIZE_BYTES
        glBindVertAttrib(aPosition, 2, GL_FLOAT, stride, offset)
        glBindVertAttrib(aTexCoord, 2, GL_FLOAT, stride, offset + 8)

        gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, elementsId)
        gl.glActiveTexture(GL_TEXTURE0)
        gl.glUniform1i(uTexture, 0)
    }

    override fun flush() {
        super.flush()
        if (vertPos > 0) {
            bindTexture()

            if (delayedBinding) {
                bindAttribsBufs() // see comments in activate()
                gl.checkError("TriangleBatch.flush bind")
            }

            gl.bufs.setFloatBuffer(vertices, 0, vertPos)
            gl.glBufferData(GL_ARRAY_BUFFER, vertPos * 4, gl.bufs.floatBuffer, GL_STREAM_DRAW)

            gl.bufs.setShortBuffer(elements, 0, elemPos)
            gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elemPos * 2, gl.bufs.shortBuffer, GL_STREAM_DRAW)
            gl.checkError("TriangleBatch.flush BufferData")

            gl.glDrawElements(GL_TRIANGLES, elemPos, GL_UNSIGNED_SHORT, 0)
            gl.checkError("TriangleBatch.flush DrawElements")

            vertPos = 0
            elemPos = 0
        }
    }

    override fun end() {
        super.end()
        gl.glDisableVertexAttribArray(aMatrix)
        gl.glDisableVertexAttribArray(aTranslation)
        gl.glDisableVertexAttribArray(aColor)
        gl.glDisableVertexAttribArray(aPosition)
        gl.glDisableVertexAttribArray(aTexCoord)
        gl.checkError("TriangleBatch end")
    }

    override fun close() {
        super.close()
        program.close()
        gl.glDeleteBuffers(2, intArrayOf(verticesId, elementsId), 0)
        gl.checkError("TriangleBatch close")
    }

    override fun toString(): String {
        return "tris/" + elements.size / QUAD_INDICES.size
    }

    /** Returns the size (in floats) of the stable attributes. If a custom shader adds additional
     * stable attributes, it should use this to determine the offset at which to bind them, and
     * override this method to return the new size including their attributes.  */
    protected fun stableAttrsSize(): Int {
        return 8
    }

    protected fun vertexSize(): Int {
        return stableAttrsSize() + 4
    }

    protected fun vertexStride(): Int {
        return vertexSize() * FLOAT_SIZE_BYTES
    }

    protected fun addExtraStableAttrs(buf: FloatArray, sidx: Int): Int {
        return sidx
    }

    protected fun beginPrimitive(vertexCount: Int, elemCount: Int): Int {
        // check whether we have enough room to hold this primitive
        val vertIdx = vertPos / vertexSize()
        val verts = vertIdx + vertexCount
        val elems = elemPos + elemCount
        val availVerts = vertices.size / vertexSize()
        val availElems = elements.size
        if (verts <= availVerts && elems <= availElems) return vertIdx

        // otherwise, flush and expand our buffers if needed
        flush()
        if (verts > availVerts) expandVerts(verts)
        if (elems > availElems) expandElems(elems)
        return 0
    }

    protected fun glBindVertAttrib(loc: Int, size: Int, type: Int, stride: Int, offset: Int) {
        gl.glEnableVertexAttribArray(loc)
        gl.glVertexAttribPointer(loc, size, type, false, stride, offset)
    }

    protected fun addElems(vertIdx: Int, indices: IntArray, indicesOffset: Int, indicesLen: Int,
                           indexBase: Int) {
        val data = elements
        var offset = elemPos
        var ii = indicesOffset
        val ll = ii + indicesLen
        while (ii < ll) {
            data[offset++] = (vertIdx + indices[ii] - indexBase).toShort()
            ii++
        }
        elemPos = offset
    }

    private fun expandVerts(vertCount: Int) {
        var newVerts = vertices.size / vertexSize()
        while (newVerts < vertCount) newVerts += EXPAND_VERTS
        vertices = FloatArray(newVerts * vertexSize())
    }

    private fun expandElems(elemCount: Int) {
        var newElems = elements.size
        while (newElems < elemCount) newElems += EXPAND_ELEMS
        elements = ShortArray(newElems)
    }

    protected fun add(into: FloatArray, offset: Int, stables: FloatArray): Int {
        gl.bufs.arrayCopy(stables, 0, into, offset, stables.size)
        return offset + stables.size
    }

    protected fun add(into: FloatArray, offset: Int, stables: FloatArray, soff: Int, slen: Int): Int {
        gl.bufs.arrayCopy(stables, soff, into, offset, slen)
        return offset + slen
    }

    protected fun add(into: FloatArray, offset: Int, x: Float, y: Float, sx: Float, sy: Float): Int {
        var offset = offset
        into[offset++] = x
        into[offset++] = y
        into[offset++] = sx
        into[offset++] = sy
        return offset
    }

    companion object {

        private val START_VERTS = 16 * 4
        private val EXPAND_VERTS = 16 * 4
        private val START_ELEMS = 6 * START_VERTS / 4
        private val EXPAND_ELEMS = 6 * EXPAND_VERTS / 4
        private val FLOAT_SIZE_BYTES = 4

        protected val QUAD_INDICES = intArrayOf(0, 1, 2, 1, 3, 2)
    }
}
/** Creates a triangle batch with the default shader program.  */
