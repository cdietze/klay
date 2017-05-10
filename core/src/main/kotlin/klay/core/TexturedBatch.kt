package klay.core

/**
 * A batch that renders textured primitives.
 */
open class TexturedBatch protected constructor(val gl: GL20) : GLBatch() {

    /** Provides some standard bits for a shader program that uses a tint and a texture.  */
    abstract class Source {

        /** Returns the source of the texture fragment shader program. Note that this program
         * *must* preserve the use of the existing varying attributes. You can add new varying
         * attributes, but you cannot remove or change the defaults.  */
        fun fragment(): String {
            val str = StringBuilder(FRAGMENT_PREAMBLE)
            str.append(textureUniforms())
            str.append(textureVaryings())
            str.append("void main(void) {\n")
            str.append(textureColor())
            str.append(textureTint())
            str.append(textureAlpha())
            str.append("  gl_FragColor = textureColor;\n" + "}")
            return str.toString()
        }

        protected fun textureUniforms(): String {
            return "uniform lowp sampler2D u_Texture;\n"
        }

        protected fun textureVaryings(): String {
            return "varying mediump vec2 v_TexCoord;\n" + "varying lowp vec4 v_Color;\n"
        }

        protected fun textureColor(): String {
            return "  vec4 textureColor = texture2D(u_Texture, v_TexCoord);\n"
        }

        protected open fun textureTint(): String {
            return "  textureColor.rgb *= v_Color.rgb;\n"
        }

        protected fun textureAlpha(): String {
            return "  textureColor *= v_Color.a;\n"
        }

        companion object {

            protected val FRAGMENT_PREAMBLE =
                    "#ifdef GL_ES\n" +
                            "precision lowp float;\n" +
                            "#else\n" +
                            // Not all versions of regular OpenGL supports precision qualifiers, define placeholders
                            "#define lowp\n" +
                            "#define mediump\n" +
                            "#define highp\n" +
                            "#endif\n"
        }
    }

    protected var curTexId: Int = 0

    /** Prepares this batch to render using the supplied texture. If pending operations have been
     * added to this batch for a different texture, this call will trigger a [.flush].
     *
     * Note: if you call `add` methods that take a texture, you do not need to call this
     * method manually. Only if you're adding bare primitives is it needed.  */
    fun setTexture(texture: Texture) {
        if (curTexId != 0 && curTexId != texture.id) flush()
        this.curTexId = texture.id
    }

    override fun end() {
        super.end()
        curTexId = 0
    }

    /** Binds our current texture. Subclasses need to call this in [.flush].  */
    protected fun bindTexture() {
        gl.glBindTexture(GL20.GL_TEXTURE_2D, curTexId)
        gl.checkError("QuadBatch glBindTexture")
    }
}
