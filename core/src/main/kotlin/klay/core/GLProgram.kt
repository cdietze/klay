package klay.core

import react.Closeable

/**
 * Encapsulates a GL vertex and fragment shader program pair.
 */
class GLProgram
/**
 * Compiles and links the shader program described by `vertexSource` and
 * `fragmentSource`.
 * @throws RuntimeException if the program fails to compile or link.
 */
(private val gl: GL20, vertexSource: String, fragmentSource: String) : Closeable {
    private val vertexShader: Int
    private val fragmentShader: Int

    /** The GL id of this shader program.  */
    val id: Int

    init {

        var id = 0
        var vertexShader = 0
        var fragmentShader = 0
        try {
            id = gl.glCreateProgram()
            if (id == 0) throw RuntimeException("Failed to create program: " + gl.glGetError())
            gl.checkError("glCreateProgram")

            vertexShader = compileShader(GL20.GL_VERTEX_SHADER, vertexSource)
            gl.glAttachShader(id, vertexShader)
            gl.checkError("glAttachShader / vertex")

            fragmentShader = compileShader(GL20.GL_FRAGMENT_SHADER, fragmentSource)
            gl.glAttachShader(id, fragmentShader)
            gl.checkError("glAttachShader / fragment")

            gl.glLinkProgram(id)
            val linkStatus = IntArray(1)
            gl.glGetProgramiv(id, GL20.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] == GL20.GL_FALSE) {
                val log = gl.glGetProgramInfoLog(id)
                gl.glDeleteProgram(id)
                throw RuntimeException("Failed to link program: " + log)
            }

            this.id = id
            this.vertexShader = vertexShader
            this.fragmentShader = fragmentShader
            fragmentShader = 0
            vertexShader = fragmentShader
            id = vertexShader

        } finally {
            if (id != 0) gl.glDeleteProgram(id)
            if (vertexShader != 0) gl.glDeleteShader(vertexShader)
            if (fragmentShader != 0) gl.glDeleteShader(fragmentShader)
        }
    }

    /**
     * Returns the uniform location with the specified `name`.
     */
    fun getUniformLocation(name: String): Int {
        val loc = gl.glGetUniformLocation(id, name)
        assert(loc >= 0) { "Failed to get $name uniform" }
        return loc
    }

    /**
     * Returns the attribute location with the specified `name`.
     */
    fun getAttribLocation(name: String): Int {
        val loc = gl.glGetAttribLocation(id, name)
        assert(loc >= 0) { "Failed to get $name uniform" }
        return loc
    }

    /** Binds this shader program, in preparation for rendering.  */
    fun activate() {
        gl.glUseProgram(id)
    }

    /** Frees this program and associated compiled shaders.
     * The program must not be used after closure.  */
    override fun close() {
        gl.glDeleteShader(vertexShader)
        gl.glDeleteShader(fragmentShader)
        gl.glDeleteProgram(id)
    }

    private fun compileShader(type: Int, shaderSource: String): Int {
        val shader = gl.glCreateShader(type)
        if (shader == 0)
            throw RuntimeException(
                    "Failed to create shader (" + type + "): " + gl.glGetError())
        gl.glShaderSource(shader, shaderSource)
        gl.glCompileShader(shader)
        val compiled = IntArray(1)
        gl.glGetShaderiv(shader, GL20.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == GL20.GL_FALSE) {
            val log = gl.glGetShaderInfoLog(shader)
            gl.glDeleteShader(shader)
            throw RuntimeException("Failed to compile shader ($type): $log")
        }
        return shader
    }
}
