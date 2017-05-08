package klay.core

import klay.core.GL20.Companion.GL_ARRAY_BUFFER
import klay.core.GL20.Companion.GL_ELEMENT_ARRAY_BUFFER
import klay.core.GL20.Companion.GL_MAX_VERTEX_UNIFORM_VECTORS
import klay.core.GL20.Companion.GL_SHORT
import klay.core.GL20.Companion.GL_STATIC_DRAW
import klay.core.GL20.Companion.GL_TEXTURE0
import klay.core.GL20.Companion.GL_TRIANGLES
import klay.core.GL20.Companion.GL_UNSIGNED_SHORT

/**
 * A batch which renders quads by stuffing them into a big(ish) GLSL uniform variable. Turns out to
 * work pretty well for 2D rendering as we rarely render more than a modest number of quads before
 * flushing the shader and it allows us to avoid sending a lot of duplicated data as is necessary
 * when rendering quads via a batch of triangles.
 */
class UniformQuadBatch
/** Creates a uniform quad batch with the supplied custom shader program builder.  */
@JvmOverloads constructor(gl: GL20, source: UniformQuadBatch.Source = UniformQuadBatch.Source()) : QuadBatch(gl) {

    /** The source for the stock quad batch shader program.  */
    class Source : TexturedBatch.Source() {

        /** Returns the source to the vertex shader program.  */
        fun vertex(batch: UniformQuadBatch): String {
            return vertex().replace("_MAX_QUADS_", "" + batch.maxQuads).replace("_VEC4S_PER_QUAD_", "" + batch.vec4sPerQuad())
        }

        /** Returns the raw vertex source, which will have some parameters subbed into it.  */
        protected fun vertex(): String {
            return VERT_UNIFS +
                    VERT_ATTRS +
                    VERT_VARS +
                    "void main(void) {\n" +
                    VERT_EXTRACTDATA +
                    VERT_SETPOS +
                    VERT_SETTEX +
                    VERT_SETCOLOR +
                    "}"
        }

        companion object {

            /** Declares the uniform variables for our shader.  */
            val VERT_UNIFS =
                    "uniform vec2 u_HScreenSize;\n" +
                            "uniform float u_Flip;\n" +
                            "uniform vec4 u_Data[_VEC4S_PER_QUAD_*_MAX_QUADS_];\n"

            /** Declares the attribute variables for our shader.  */
            val VERT_ATTRS = "attribute vec3 a_Vertex;\n"

            /** Declares the varying variables for our shader.  */
            val VERT_VARS = "varying vec2 v_TexCoord;\n" + "varying vec4 v_Color;\n"

            /** Extracts the values from our data buffer.  */
            val VERT_EXTRACTDATA =
                    "int index = _VEC4S_PER_QUAD_*int(a_Vertex.z);\n" +
                            "vec4 mat = u_Data[index+0];\n" +
                            "vec4 txc = u_Data[index+1];\n" +
                            "vec4 tcs = u_Data[index+2];\n"

            /** The shader code that computes `gl_Position`.  */
            val VERT_SETPOS =
                    // Transform the vertex.
                    "mat3 transform = mat3(\n" +
                            "  mat.x, mat.y, 0,\n" +
                            "  mat.z, mat.w, 0,\n" +
                            "  txc.x, txc.y, 1);\n" +
                            "gl_Position = vec4(transform * vec3(a_Vertex.xy, 1.0), 1.0);\n" +
                            // Scale from screen coordinates to [0, 2].
                            "gl_Position.xy /= u_HScreenSize.xy;\n" +
                            // Offset to [-1, 1].
                            "gl_Position.xy -= 1.0;\n" +
                            // If requested, flip the y-axis.
                            "gl_Position.y *= u_Flip;\n"

            /** The shader code that computes `v_TexCoord`.  */
            val VERT_SETTEX = "v_TexCoord = a_Vertex.xy * tcs.xy + txc.zw;\n"

            /** The shader code that computes `v_Color`.  */
            val VERT_SETCOLOR =
                    // tint is encoded as two floats A*R and G*B where A, R, G, B are (0 - 255)
                    "float red = mod(tcs.z, 256.0);\n" +
                            "float alpha = (tcs.z - red) / 256.0;\n" +
                            "float blue = mod(tcs.w, 256.0);\n" +
                            "float green = (tcs.w - blue) / 256.0;\n" +
                            "v_Color = vec4(red / 255.0, green / 255.0, blue / 255.0, alpha / 255.0);\n"
        }
    }

    protected val maxQuads: Short

    protected val program: GLProgram
    protected val uTexture: Int
    protected val uHScreenSize: Int
    protected val uFlip: Int
    protected val uData: Int
    protected val aVertex: Int

    protected val verticesId: Int
    protected val elementsId: Int
    protected val data: FloatArray
    protected var quadCounter: Int = 0

    init {
        val maxVecs = usableMaxUniformVectors(gl) - extraVec4s()
        if (maxVecs < vec4sPerQuad())
            throw RuntimeException(
                    "GL_MAX_VERTEX_UNIFORM_VECTORS too low: have " + maxVecs +
                            ", need at least " + vec4sPerQuad())
        maxQuads = (maxVecs / vec4sPerQuad()).toShort()

        program = GLProgram(gl, source.vertex(this), source.fragment())
        uTexture = program.getUniformLocation("u_Texture")
        uHScreenSize = program.getUniformLocation("u_HScreenSize")
        uFlip = program.getUniformLocation("u_Flip")
        uData = program.getUniformLocation("u_Data")
        aVertex = program.getAttribLocation("a_Vertex")

        // create our stock supply of unit quads and stuff them into our buffers
        val verts = ShortArray(maxQuads * VERTICES_PER_QUAD * VERTEX_SIZE)
        val elems = ShortArray(maxQuads * ELEMENTS_PER_QUAD)
        var vv = 0
        var ee = 0
        for (ii in 0..maxQuads - 1) {
            verts[vv++] = 0
            verts[vv++] = 0
            verts[vv++] = ii.toShort()
            verts[vv++] = 1
            verts[vv++] = 0
            verts[vv++] = ii.toShort()
            verts[vv++] = 0
            verts[vv++] = 1
            verts[vv++] = ii.toShort()
            verts[vv++] = 1
            verts[vv++] = 1
            verts[vv++] = ii.toShort()
            var base = (ii * VERTICES_PER_QUAD).toShort()
            val base0 = base
            val base1 = ++base
            val base2 = ++base
            val base3 = ++base
            elems[ee++] = base0
            elems[ee++] = base1
            elems[ee++] = base2
            elems[ee++] = base1
            elems[ee++] = base3
            elems[ee++] = base2
        }

        data = FloatArray(maxQuads * vec4sPerQuad() * 4)

        // create our GL buffers
        val ids = IntArray(2)
        gl.glGenBuffers(2, ids, 0)
        verticesId = ids[0]
        elementsId = ids[1]

        gl.glBindBuffer(GL_ARRAY_BUFFER, verticesId)
        gl.bufs.setShortBuffer(verts, 0, verts.size)
        gl.glBufferData(GL_ARRAY_BUFFER, verts.size * 2, gl.bufs.shortBuffer, GL_STATIC_DRAW)

        gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, elementsId)
        gl.bufs.setShortBuffer(elems, 0, elems.size)
        gl.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elems.size * 2, gl.bufs.shortBuffer, GL_STATIC_DRAW)

        gl.checkError("UniformQuadBatch end ctor")
    }

    override fun addQuad(tint: Int,
                         m00: Float, m01: Float, m10: Float, m11: Float, tx: Float, ty: Float,
                         x1: Float, y1: Float, sx1: Float, sy1: Float,
                         x2: Float, y2: Float, sx2: Float, sy2: Float,
                         x3: Float, y3: Float, sx3: Float, sy3: Float,
                         x4: Float, y4: Float, sx4: Float, sy4: Float) {
        var pos = quadCounter * vec4sPerQuad() * 4
        val dw = x2 - x1
        val dh = y3 - y1
        data[pos++] = m00 * dw
        data[pos++] = m01 * dw
        data[pos++] = m10 * dh
        data[pos++] = m11 * dh
        data[pos++] = tx + m00 * x1 + m10 * y1
        data[pos++] = ty + m01 * x1 + m11 * y1
        data[pos++] = sx1
        data[pos++] = sy1
        data[pos++] = sx2 - sx1
        data[pos++] = sy3 - sy1
        data[pos++] = (tint shr 16 and 0xFFFF).toFloat()
        data[pos++] = (tint and 0xFFFF).toFloat()
        pos = addExtraQuadData(data, pos)
        quadCounter++

        if (quadCounter >= maxQuads) flush()
    }

    override fun begin(fbufWidth: Float, fbufHeight: Float, flip: Boolean) {
        super.begin(fbufWidth, fbufHeight, flip)
        program.activate()
        // TODO: apparently we can avoid glUniform calls because they're part of the program state; so
        // we can cache the last set values for all these glUniform calls and only set them anew if
        // they differ...
        gl.glUniform2f(uHScreenSize, fbufWidth / 2f, fbufHeight / 2f)
        gl.glUniform1f(uFlip, (if (flip) -1 else 1).toFloat())
        gl.glBindBuffer(GL_ARRAY_BUFFER, verticesId)
        gl.glEnableVertexAttribArray(aVertex)
        gl.glVertexAttribPointer(aVertex, VERTEX_SIZE, GL_SHORT, false, 0, 0)
        gl.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, elementsId)
        gl.glActiveTexture(GL_TEXTURE0)
        gl.glUniform1i(uTexture, 0)
        gl.checkError("UniformQuadBatch begin")
    }

    override fun flush() {
        super.flush()
        if (quadCounter > 0) {
            bindTexture()
            gl.glUniform4fv(uData, quadCounter * vec4sPerQuad(), data, 0)
            gl.glDrawElements(GL_TRIANGLES, quadCounter * ELEMENTS_PER_QUAD, GL_UNSIGNED_SHORT, 0)
            gl.checkError("UniformQuadBatch flush")
            quadCounter = 0
        }
    }

    override fun end() {
        super.end()
        gl.glDisableVertexAttribArray(aVertex)
        gl.checkError("UniformQuadBatch end")
    }

    override fun close() {
        super.close()
        program.close()
        gl.glDeleteBuffers(2, intArrayOf(verticesId, elementsId), 0)
        gl.checkError("UniformQuadBatch close")
    }

    override fun toString(): String {
        return "uquad/" + maxQuads
    }

    protected fun vec4sPerQuad(): Int {
        return BASE_VEC4S_PER_QUAD
    }

    /** Returns how many vec4s this shader uses above and beyond those in the base implementation. If
     * you add any extra attributes or uniforms, your subclass will need to account for them here.  */
    protected fun extraVec4s(): Int {
        return 0
    }

    protected fun addExtraQuadData(data: FloatArray, pos: Int): Int {
        return pos
    }

    companion object {

        /**
         * Returns false if the GL context doesn't support sufficient numbers of vertex uniform vectors
         * to allow this shader to run with good performance, true otherwise.
         */
        fun isLikelyToPerform(gl: GL20): Boolean {
            val maxVecs = usableMaxUniformVectors(gl)
            // assume we're better off with indexed tris if we can't push at least 16 quads at a time
            return maxVecs >= 16 * BASE_VEC4S_PER_QUAD
        }

        private fun usableMaxUniformVectors(gl: GL20): Int {
            // this returns the maximum number of vec4s; then we subtract one vec2 to account for the
            // uHScreenSize uniform, and two more because some GPUs seem to need one for our vec3 attr
            val maxVecs = gl.glGetInteger(GL_MAX_VERTEX_UNIFORM_VECTORS) - 3
            // we have to check errors always in this case, because if GL failed to return a value we would
            // otherwise return the value of uninitialized memory which could be some huge number which we
            // might turn around and try to compile into a shader causing GL to crash (you might think from
            // such a careful description that such a thing has in fact come to pass, and you would not be
            // incorrect)
            val glErr = gl.glGetError()
            if (glErr != GL20.GL_NO_ERROR)
                throw RuntimeException(
                        "Unable to query GL_MAX_VERTEX_UNIFORM_VECTORS,  error " + glErr)
            return maxVecs
        }

        private val VERTICES_PER_QUAD = 4
        private val ELEMENTS_PER_QUAD = 6
        private val VERTEX_SIZE = 3 // 3 floats per vertex
        private val BASE_VEC4S_PER_QUAD = 3 // 3 vec4s per matrix
    }
}
/** Creates a uniform quad batch with the default shader programs.  */
