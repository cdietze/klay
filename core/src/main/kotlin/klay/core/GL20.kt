package klay.core

import klay.core.buffers.*

/**
 * Interface and values for OpenGL ES 2.0, based on the official JOGL GL2ES2 interface.
 */
abstract class GL20 protected constructor(val bufs: GL20.Buffers, val checkErrors: Boolean) {

    /**
     * A helper class for bridging between Java arrays and buffers when implementing [GL20].
     */
    abstract class Buffers {

        var intBuffer = createIntBuffer(32)
        var floatBuffer = createFloatBuffer(32)
        var shortBuffer = createShortBuffer(32)
        var byteBuffer = createByteBuffer(256)

        fun setByteBuffer(source: ByteArray, offset: Int, length: Int) {
            resizeByteBuffer(length - offset)
            byteBuffer.put(source, offset, length)
            byteBuffer.rewind()
        }

        fun setShortBuffer(source: ShortArray, offset: Int, length: Int) {
            resizeShortBuffer(length - offset)
            shortBuffer.put(source, offset, length)
            shortBuffer.rewind()
        }

        fun setIntBuffer(source: IntArray, offset: Int, length: Int) {
            resizeIntBuffer(length - offset)
            intBuffer.put(source, offset, length)
            intBuffer.rewind()
        }

        fun setFloatBuffer(source: FloatArray, offset: Int, length: Int) {
            resizeFloatBuffer(length - offset)
            floatBuffer.put(source, offset, length)
            floatBuffer.rewind()
        }

        fun setShortBuffer(n: Short) {
            shortBuffer.position(0)
            shortBuffer.put(n)
            shortBuffer.rewind()
        }

        fun setIntBuffer(n: Int) {
            intBuffer.position(0)
            intBuffer.put(n)
            intBuffer.rewind()
        }

        fun setFloatBuffer(n: Float) {
            floatBuffer.position(0)
            floatBuffer.put(n)
            floatBuffer.rewind()
        }

        fun resizeByteBuffer(length: Int) {
            val cap = byteBuffer.capacity()
            if (cap < length)
                byteBuffer = createByteBuffer(newCap(cap, length))
            else
                byteBuffer.position(0)
            byteBuffer.limit(length)
        }

        fun resizeShortBuffer(length: Int) {
            val cap = shortBuffer.capacity()
            if (cap < length)
                shortBuffer = createShortBuffer(newCap(cap, length))
            else
                shortBuffer.position(0)
            shortBuffer.limit(length)
        }

        fun resizeIntBuffer(length: Int) {
            val cap = intBuffer.capacity()
            if (cap < length)
                intBuffer = createIntBuffer(newCap(cap, length))
            else
                intBuffer.position(0)
            intBuffer.limit(length)
        }

        fun resizeFloatBuffer(length: Int) {
            val cap = floatBuffer.capacity()
            if (cap < length)
                floatBuffer = createFloatBuffer(newCap(cap, length))
            else
                floatBuffer.position(0)
            floatBuffer.limit(length)
        }

        abstract fun createByteBuffer(size: Int): ByteBuffer
        fun createShortBuffer(size: Int): ShortBuffer {
            return createByteBuffer(size * 2).asShortBuffer()
        }

        fun createIntBuffer(size: Int): IntBuffer {
            return createByteBuffer(size * 4).asIntBuffer()
        }

        fun createFloatBuffer(size: Int): FloatBuffer {
            return createByteBuffer(size * 4).asFloatBuffer()
        }

        private fun newCap(cap: Int, length: Int): Int {
            var newLength = cap shl 1
            while (newLength < length) {
                newLength = newLength shl 1
            }
            return newLength
        }
    }

    /**
     * Checks for any GL error codes and logs them (if [.checkErrors] is true).
     * @return true if any errors were reported.
     */
    fun checkError(op: String): Boolean {
        var reported = 0
        if (checkErrors) {
            while (true) {
                val error = glGetError()
                if (error == GL_NO_ERROR) break
                reported += 1
                println(op + ": glError " + error)
            }
        }
        return reported > 0
    }

    fun glDeleteBuffer(id: Int) {
        bufs.setIntBuffer(id)
        glDeleteBuffers(1, bufs.intBuffer)
    }

    fun glDeleteBuffers(n: Int, buffers: IntArray, offset: Int) {
        bufs.setIntBuffer(buffers, offset, n)
        glDeleteBuffers(n, bufs.intBuffer)
    }

    fun glDeleteFramebuffer(id: Int) {
        bufs.setIntBuffer(id)
        glDeleteFramebuffers(1, bufs.intBuffer)
    }

    fun glDeleteFramebuffers(n: Int, framebuffers: IntArray, offset: Int) {
        bufs.setIntBuffer(framebuffers, offset, n)
        glDeleteFramebuffers(n, bufs.intBuffer)
    }

    fun glDeleteRenderbuffer(id: Int) {
        bufs.setIntBuffer(id)
        glDeleteRenderbuffers(1, bufs.intBuffer)
    }

    fun glDeleteRenderbuffers(n: Int, renderbuffers: IntArray, offset: Int) {
        bufs.setIntBuffer(renderbuffers, offset, n)
        glDeleteRenderbuffers(n, bufs.intBuffer)
    }

    fun glDeleteTexture(id: Int) {
        bufs.setIntBuffer(id)
        glDeleteTextures(1, bufs.intBuffer)
    }

    fun glDeleteTextures(n: Int, textures: IntArray, offset: Int) {
        bufs.setIntBuffer(textures, offset, n)
        glDeleteTextures(n, bufs.intBuffer)
    }

    fun glGenBuffer(): Int {
        bufs.resizeIntBuffer(1)
        glGenBuffers(1, bufs.intBuffer)
        return bufs.intBuffer.get(0)
    }

    fun glGenBuffers(n: Int, buffers: IntArray, offset: Int) {
        bufs.resizeIntBuffer(n)
        glGenBuffers(n, bufs.intBuffer)
        bufs.intBuffer.get(buffers, offset, n)
    }

    fun glGenFramebuffer(): Int {
        bufs.resizeIntBuffer(1)
        glGenFramebuffers(1, bufs.intBuffer)
        return bufs.intBuffer.get(0)
    }

    fun glGenFramebuffers(n: Int, framebuffers: IntArray, offset: Int) {
        bufs.resizeIntBuffer(n)
        glGenFramebuffers(n, bufs.intBuffer)
        bufs.intBuffer.get(framebuffers, offset, n)
    }

    fun glGenRenderbuffer(): Int {
        bufs.resizeIntBuffer(1)
        glGenRenderbuffers(1, bufs.intBuffer)
        return bufs.intBuffer.get(0)
    }

    fun glGenRenderbuffers(n: Int, renderbuffers: IntArray, offset: Int) {
        bufs.resizeIntBuffer(n)
        glGenRenderbuffers(n, bufs.intBuffer)
        bufs.intBuffer.get(renderbuffers, offset, n)
    }

    fun glGenTexture(): Int {
        bufs.resizeIntBuffer(1)
        glGenTextures(1, bufs.intBuffer)
        return bufs.intBuffer.get(0)
    }

    fun glGenTextures(n: Int, textures: IntArray, offset: Int) {
        bufs.resizeIntBuffer(n)
        glGenTextures(n, bufs.intBuffer)
        bufs.intBuffer.get(textures, offset, n)
    }

    fun glGetAttachedShaders(program: Int, maxcount: Int, count: IntArray, countOffset: Int, shaders: IntArray, shadersOffset: Int) {
        val countLength = count.size - countOffset
        bufs.resizeIntBuffer(countLength)
        val shadersLength = shaders.size - shadersOffset
        val intBuffer2 = bufs.createIntBuffer(shadersLength)
        glGetAttachedShaders(program, maxcount, bufs.intBuffer, intBuffer2)
        bufs.intBuffer.get(count, countOffset, countLength)
        intBuffer2.get(shaders, shadersOffset, shadersLength)
    }

    fun glGetBooleanv(pname: Int, params: ByteArray, offset: Int) {
        val length = params.size - offset
        bufs.resizeByteBuffer(length)
        glGetBooleanv(pname, bufs.byteBuffer)
        bufs.byteBuffer.get(params, offset, length)
    }

    fun glGetBufferParameteriv(target: Int, pname: Int, params: IntArray, offset: Int) {
        val length = params.size - offset
        bufs.resizeIntBuffer(length)
        glGetBufferParameteriv(target, pname, bufs.intBuffer)
        bufs.intBuffer.get(params, offset, length)
    }

    fun glGetFloatv(pname: Int, params: FloatArray, offset: Int) {
        val length = params.size - offset
        bufs.resizeFloatBuffer(length)
        glGetFloatv(pname, bufs.floatBuffer)
        bufs.floatBuffer.get(params, offset, length)
    }

    fun glGetFramebufferAttachmentParameteriv(target: Int, attachment: Int, pname: Int, params: IntArray, offset: Int) {
        val length = params.size - offset
        bufs.resizeIntBuffer(length)
        glGetFramebufferAttachmentParameteriv(target, attachment, pname, bufs.intBuffer)
        bufs.intBuffer.get(params, offset, length)
    }

    fun glGetIntegerv(pname: Int, params: IntArray, offset: Int) {
        val length = params.size - offset
        bufs.resizeIntBuffer(length)
        glGetIntegerv(pname, bufs.intBuffer)
        bufs.intBuffer.get(params, offset, length)
    }

    fun glGetProgramBinary(program: Int, bufsize: Int, length: IntArray, lengthOffset: Int, binaryformat: IntArray, binaryformatOffset: Int, binary: ByteBuffer) {
        val lengthLength = bufsize - lengthOffset
        bufs.resizeIntBuffer(lengthLength)

        val binaryformatLength = bufsize - binaryformatOffset
        val intBuffer2 = bufs.createIntBuffer(binaryformatLength)
        glGetProgramBinary(program, bufsize, bufs.intBuffer, intBuffer2, binary)

        // Return length, binaryformat
        bufs.intBuffer.get(length, lengthOffset, lengthLength)
        intBuffer2.get(binaryformat, binaryformatOffset, binaryformatLength)
    }

    fun glGetProgramInfoLog(program: Int, bufsize: Int, length: IntArray, lengthOffset: Int, infolog: ByteArray, infologOffset: Int) {
        val intLength = length.size - lengthOffset
        bufs.resizeIntBuffer(intLength)

        val byteLength = bufsize - infologOffset
        bufs.resizeByteBuffer(byteLength)

        glGetProgramInfoLog(program, bufsize, bufs.intBuffer, bufs.byteBuffer)
        // length is the length of the infoLog string being returned
        bufs.intBuffer.get(length, lengthOffset, intLength)
        // infoLog is the char array of the infoLog
        bufs.byteBuffer.get(infolog, byteLength, infologOffset)
    }

    fun glGetProgramiv(program: Int, pname: Int, params: IntArray, offset: Int) {
        val length = params.size - offset
        bufs.resizeIntBuffer(length)
        glGetProgramiv(program, pname, bufs.intBuffer)
        bufs.intBuffer.get(params, offset, length)
    }

    fun glGetRenderbufferParameteriv(target: Int, pname: Int, params: IntArray, offset: Int) {
        val length = params.size - offset
        bufs.resizeIntBuffer(length)
        glGetRenderbufferParameteriv(target, pname, bufs.intBuffer)
        bufs.intBuffer.get(params, offset, length)
    }

    fun glGetShaderInfoLog(shader: Int, bufsize: Int, length: IntArray, lengthOffset: Int, infolog: ByteArray, infologOffset: Int) {
        val intLength = length.size - lengthOffset
        bufs.resizeIntBuffer(intLength)
        val byteLength = bufsize - infologOffset
        bufs.resizeByteBuffer(byteLength)
        glGetShaderInfoLog(shader, bufsize, bufs.intBuffer, bufs.byteBuffer)
        // length is the length of the infoLog string being returned
        bufs.intBuffer.get(length, lengthOffset, intLength)
        // infoLog is the char array of the infoLog
        bufs.byteBuffer.get(infolog, byteLength, infologOffset)
    }

    fun glGetShaderiv(shader: Int, pname: Int, params: IntArray, offset: Int) {
        val length = params.size - offset
        bufs.resizeIntBuffer(length)
        glGetShaderiv(shader, pname, bufs.intBuffer)
        bufs.intBuffer.get(params, offset, length)
    }

    fun glGetTexParameterfv(target: Int, pname: Int, params: FloatArray, offset: Int) {
        val length = params.size - offset
        bufs.resizeFloatBuffer(length)
        glGetTexParameterfv(target, pname, bufs.floatBuffer)
        bufs.floatBuffer.get(params, offset, length)
    }

    fun glGetTexParameteriv(target: Int, pname: Int, params: IntArray, offset: Int) {
        val length = params.size - offset
        bufs.resizeIntBuffer(length)
        glGetTexParameteriv(target, pname, bufs.intBuffer)
        bufs.intBuffer.get(params, offset, length)
    }

    fun glGetUniformfv(program: Int, location: Int, params: FloatArray, offset: Int) {
        val length = params.size - offset
        bufs.resizeFloatBuffer(length)
        glGetUniformfv(program, location, bufs.floatBuffer)
        bufs.floatBuffer.get(params, offset, length)
    }

    fun glGetUniformiv(program: Int, location: Int, params: IntArray, offset: Int) {
        val length = params.size - offset
        bufs.resizeIntBuffer(length)
        glGetUniformiv(program, location, bufs.intBuffer)
        bufs.intBuffer.get(params, offset, length)
    }

    fun glGetVertexAttribfv(index: Int, pname: Int, params: FloatArray, offset: Int) {
        val length = params.size - offset
        bufs.resizeFloatBuffer(length)
        glGetVertexAttribfv(index, pname, bufs.floatBuffer)
        bufs.floatBuffer.get(params, offset, length)
    }

    fun glGetVertexAttribiv(index: Int, pname: Int, params: IntArray, offset: Int) {
        val length = params.size - offset
        bufs.resizeIntBuffer(length)
        glGetVertexAttribiv(index, pname, bufs.intBuffer)
        bufs.intBuffer.get(params, offset, length)
    }

    fun glTexParameterfv(target: Int, pname: Int, params: FloatArray, offset: Int) {
        val length = params.size - offset
        bufs.setFloatBuffer(params, offset, length)
        glTexParameterfv(target, pname, bufs.floatBuffer)
    }

    fun glTexParameteriv(target: Int, pname: Int, params: IntArray, offset: Int) {
        val length = params.size - offset
        bufs.setIntBuffer(params, offset, length)
        glTexParameteriv(target, pname, bufs.intBuffer)
    }

    fun glUniform1fv(location: Int, count: Int, v: FloatArray, offset: Int) {
        bufs.setFloatBuffer(v, offset, count)
        glUniform1fv(location, count, bufs.floatBuffer)
    }

    fun glUniform1iv(location: Int, count: Int, v: IntArray, offset: Int) {
        bufs.setIntBuffer(v, offset, count)
        glUniform1iv(location, count, bufs.intBuffer)
    }

    fun glUniform2fv(location: Int, count: Int, v: FloatArray, offset: Int) {
        bufs.setFloatBuffer(v, 2 * offset, 2 * count)
        glUniform2fv(location, count, bufs.floatBuffer)
    }

    fun glUniform2iv(location: Int, count: Int, v: IntArray, offset: Int) {
        bufs.setIntBuffer(v, 2 * offset, 2 * count)
        glUniform2iv(location, count, bufs.intBuffer)
    }

    fun glUniform3fv(location: Int, count: Int, v: FloatArray, offset: Int) {
        bufs.setFloatBuffer(v, 3 * offset, 3 * count)
        glUniform3fv(location, count, bufs.floatBuffer)
    }

    fun glUniform3iv(location: Int, count: Int, v: IntArray, offset: Int) {
        bufs.setIntBuffer(v, 3 * offset, 3 * count)
        glUniform3iv(location, count, bufs.intBuffer)
    }

    fun glUniform4fv(location: Int, count: Int, v: FloatArray, offset: Int) {
        bufs.setFloatBuffer(v, 4 * offset, 4 * count)
        glUniform4fv(location, count, bufs.floatBuffer)
    }

    fun glUniform4iv(location: Int, count: Int, v: IntArray, offset: Int) {
        bufs.setIntBuffer(v, 4 * offset, 4 * count)
        glUniform4iv(location, count, bufs.intBuffer)
    }

    fun glUniformMatrix2fv(location: Int, count: Int, transpose: Boolean,
                           value: FloatArray, offset: Int) {
        bufs.setFloatBuffer(value, 2 * 2 * offset, 2 * 2 * count)
        glUniformMatrix2fv(location, count, transpose, bufs.floatBuffer)
    }

    fun glUniformMatrix3fv(location: Int, count: Int, transpose: Boolean,
                           value: FloatArray, offset: Int) {
        bufs.setFloatBuffer(value, 2 * 2 * offset, 3 * 3 * count)
        glUniformMatrix3fv(location, count, transpose, bufs.floatBuffer)
    }

    fun glUniformMatrix4fv(location: Int, count: Int, transpose: Boolean,
                           value: FloatArray, offset: Int) {
        bufs.setFloatBuffer(value, 4 * 4 * offset, 4 * 4 * count)
        glUniformMatrix4fv(location, count, transpose, bufs.floatBuffer)
    }

    fun glVertexAttrib1fv(indx: Int, values: FloatArray, offset: Int) {
        glVertexAttrib1f(indx, values[indx + offset])
    }

    fun glVertexAttrib2fv(indx: Int, values: FloatArray, offset: Int) {
        glVertexAttrib2f(indx, values[indx + offset], values[indx + 1 + offset])
    }

    fun glVertexAttrib3fv(indx: Int, values: FloatArray, offset: Int) {
        glVertexAttrib3f(indx, values[indx + offset], values[indx + 1 + offset], values[indx + 2 + offset])
    }

    fun glVertexAttrib4fv(indx: Int, values: FloatArray, offset: Int) {
        glVertexAttrib4f(indx, values[indx + offset], values[indx + 1 + offset], values[indx + 2 + offset], values[indx + 3 + offset])
    }

    abstract val platformGLExtensions: String
    abstract val swapInterval: Int
    abstract fun glActiveTexture(texture: Int)
    abstract fun glAttachShader(program: Int, shader: Int)
    abstract fun glBindAttribLocation(program: Int, index: Int, name: String)
    abstract fun glBindBuffer(target: Int, buffer: Int)
    abstract fun glBindFramebuffer(target: Int, framebuffer: Int)
    abstract fun glBindRenderbuffer(target: Int, renderbuffer: Int)
    abstract fun glBindTexture(target: Int, texture: Int)
    abstract fun glBlendColor(red: Float, green: Float, blue: Float, alpha: Float)
    abstract fun glBlendEquation(mode: Int)
    abstract fun glBlendEquationSeparate(modeRGB: Int, modeAlpha: Int)
    abstract fun glBlendFunc(sfactor: Int, dfactor: Int)
    abstract fun glBlendFuncSeparate(srcRGB: Int, dstRGB: Int, srcAlpha: Int, dstAlpha: Int)
    abstract fun glBufferData(target: Int, size: Int, data: Buffer, usage: Int)
    abstract fun glBufferSubData(target: Int, offset: Int, size: Int, data: Buffer)
    abstract fun glCheckFramebufferStatus(target: Int): Int
    abstract fun glClear(mask: Int)
    abstract fun glClearColor(red: Float, green: Float, blue: Float, alpha: Float)
    abstract fun glClearDepth(depth: Double)
    abstract fun glClearDepthf(depth: Float)
    abstract fun glClearStencil(s: Int)
    abstract fun glColorMask(red: Boolean, green: Boolean, blue: Boolean, alpha: Boolean)
    abstract fun glCompileShader(shader: Int)

    abstract fun glCompressedTexImage2D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, border: Int, imageSize: Int, data: Buffer)
    abstract fun glCompressedTexImage2D(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: Int, arg6: Int, arg7: Int)
    abstract fun glCompressedTexImage3D(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: Int, arg6: Int, arg7: Int, arg8: ByteBuffer)
    abstract fun glCompressedTexImage3D(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: Int, arg6: Int, arg7: Int, arg8: Int)
    abstract fun glCompressedTexSubImage2D(target: Int, level: Int, xoffset: Int, yoffset: Int, width: Int, height: Int, format: Int, imageSize: Int, data: Buffer)
    abstract fun glCompressedTexSubImage2D(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: Int, arg6: Int, arg7: Int, arg8: Int)
    abstract fun glCompressedTexSubImage3D(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: Int, arg6: Int, arg7: Int, arg8: Int, arg9: Int, arg10: ByteBuffer)
    abstract fun glCompressedTexSubImage3D(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: Int, arg6: Int, arg7: Int, arg8: Int, arg9: Int, arg10: Int)

    abstract fun glCopyTexImage2D(target: Int, level: Int, internalformat: Int, x: Int, y: Int, width: Int, height: Int, border: Int)
    abstract fun glCopyTexSubImage2D(target: Int, level: Int, xoffset: Int, yoffset: Int, x: Int, y: Int, width: Int, height: Int)
    abstract fun glCopyTexSubImage3D(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: Int, arg6: Int, arg7: Int, arg8: Int)
    abstract fun glCreateProgram(): Int
    abstract fun glCreateShader(type: Int): Int
    abstract fun glCullFace(mode: Int)

    abstract fun glDeleteBuffers(n: Int, buffers: IntBuffer)
    abstract fun glDeleteFramebuffers(n: Int, framebuffers: IntBuffer)
    abstract fun glDeleteProgram(program: Int)
    abstract fun glDeleteRenderbuffers(n: Int, renderbuffers: IntBuffer)
    abstract fun glDeleteShader(shader: Int)
    abstract fun glDeleteTextures(n: Int, textures: IntBuffer)

    abstract fun glDepthFunc(func: Int)
    abstract fun glDepthMask(flag: Boolean)
    abstract fun glDepthRange(zNear: Double, zFar: Double)
    abstract fun glDepthRangef(zNear: Float, zFar: Float)
    abstract fun glDetachShader(program: Int, shader: Int)
    abstract fun glDisable(cap: Int)
    abstract fun glDisableVertexAttribArray(index: Int)
    abstract fun glDrawArrays(mode: Int, first: Int, count: Int)
    abstract fun glDrawElements(mode: Int, count: Int, type: Int, indices: Buffer)
    abstract fun glDrawElements(mode: Int, count: Int, type: Int, offset: Int)
    abstract fun glEnable(cap: Int)
    abstract fun glEnableVertexAttribArray(index: Int)
    abstract fun glFinish()
    abstract fun glFlush()
    abstract fun glFramebufferRenderbuffer(target: Int, attachment: Int, renderbuffertarget: Int, renderbuffer: Int)
    abstract fun glFramebufferTexture2D(target: Int, attachment: Int, textarget: Int, texture: Int, level: Int)
    abstract fun glFramebufferTexture3D(target: Int, attachment: Int, textarget: Int, texture: Int, level: Int, zoffset: Int)
    abstract fun glFrontFace(mode: Int)

    abstract fun glGenBuffers(n: Int, buffers: IntBuffer)
    abstract fun glGenerateMipmap(target: Int)
    abstract fun glGenFramebuffers(n: Int, framebuffers: IntBuffer)
    abstract fun glGenRenderbuffers(n: Int, renderbuffers: IntBuffer)
    abstract fun glGenTextures(n: Int, textures: IntBuffer)

    abstract fun glGetActiveAttrib(program: Int, index: Int, bufsize: Int, length: IntArray, lengthOffset: Int, size: IntArray, sizeOffset: Int, type: IntArray, typeOffset: Int, name: ByteArray, nameOffset: Int)
    abstract fun glGetActiveAttrib(program: Int, index: Int, bufsize: Int, length: IntBuffer, size: IntBuffer, type: IntBuffer, name: ByteBuffer)
    abstract fun glGetActiveUniform(program: Int, index: Int, bufsize: Int, length: IntArray, lengthOffset: Int, size: IntArray, sizeOffset: Int, type: IntArray, typeOffset: Int, name: ByteArray, nameOffset: Int)
    abstract fun glGetActiveUniform(program: Int, index: Int, bufsize: Int, length: IntBuffer, size: IntBuffer, type: IntBuffer, name: ByteBuffer)
    abstract fun glGetAttachedShaders(program: Int, maxcount: Int, count: IntBuffer, shaders: IntBuffer)
    abstract fun glGetAttribLocation(program: Int, name: String): Int
    abstract fun glGetBoolean(pname: Int): Boolean
    abstract fun glGetBooleanv(pname: Int, params: ByteBuffer)
    abstract fun glGetBoundBuffer(arg0: Int): Int
    abstract fun glGetBufferParameteriv(target: Int, pname: Int, params: IntBuffer)
    abstract fun glGetError(): Int
    abstract fun glGetFloat(pname: Int): Float
    abstract fun glGetFloatv(pname: Int, params: FloatBuffer)
    abstract fun glGetFramebufferAttachmentParameteriv(target: Int, attachment: Int, pname: Int, params: IntBuffer)
    abstract fun glGetInteger(pname: Int): Int
    abstract fun glGetIntegerv(pname: Int, params: IntBuffer)
    abstract fun glGetProgramBinary(arg0: Int, arg1: Int, arg2: IntBuffer, arg3: IntBuffer, arg4: ByteBuffer)
    abstract fun glGetProgramInfoLog(program: Int, bufsize: Int, length: IntBuffer, infolog: ByteBuffer)
    abstract fun glGetProgramInfoLog(program: Int): String
    abstract fun glGetProgramiv(program: Int, pname: Int, params: IntBuffer)
    abstract fun glGetRenderbufferParameteriv(target: Int, pname: Int, params: IntBuffer)
    abstract fun glGetShaderInfoLog(shader: Int, bufsize: Int, length: IntBuffer, infolog: ByteBuffer)
    abstract fun glGetShaderInfoLog(shader: Int): String
    abstract fun glGetShaderiv(shader: Int, pname: Int, params: IntBuffer)
    abstract fun glGetShaderPrecisionFormat(shadertype: Int, precisiontype: Int, range: IntArray, rangeOffset: Int, precision: IntArray, precisionOffset: Int)
    abstract fun glGetShaderPrecisionFormat(shadertype: Int, precisiontype: Int, range: IntBuffer, precision: IntBuffer)
    abstract fun glGetShaderSource(shader: Int, bufsize: Int, length: IntArray, lengthOffset: Int, source: ByteArray, sourceOffset: Int)
    abstract fun glGetShaderSource(shader: Int, bufsize: Int, length: IntBuffer, source: ByteBuffer)
    abstract fun glGetString(name: Int): String
    abstract fun glGetTexParameterfv(target: Int, pname: Int, params: FloatBuffer)
    abstract fun glGetTexParameteriv(target: Int, pname: Int, params: IntBuffer)
    abstract fun glGetUniformfv(program: Int, location: Int, params: FloatBuffer)
    abstract fun glGetUniformiv(program: Int, location: Int, params: IntBuffer)
    abstract fun glGetUniformLocation(program: Int, name: String): Int
    abstract fun glGetVertexAttribfv(index: Int, pname: Int, params: FloatBuffer)
    abstract fun glGetVertexAttribiv(index: Int, pname: Int, params: IntBuffer)
    abstract fun glHint(target: Int, mode: Int)

    abstract fun glIsBuffer(buffer: Int): Boolean
    abstract fun glIsEnabled(cap: Int): Boolean
    abstract fun glIsFramebuffer(framebuffer: Int): Boolean
    abstract fun glIsProgram(program: Int): Boolean
    abstract fun glIsRenderbuffer(renderbuffer: Int): Boolean
    abstract fun glIsShader(shader: Int): Boolean
    abstract fun glIsTexture(texture: Int): Boolean
    abstract fun glIsVBOArrayEnabled(): Boolean
    abstract fun glIsVBOElementEnabled(): Boolean

    abstract fun glLineWidth(width: Float)
    abstract fun glLinkProgram(program: Int)
    abstract fun glMapBuffer(arg0: Int, arg1: Int): ByteBuffer
    abstract fun glPixelStorei(pname: Int, param: Int)
    abstract fun glPolygonOffset(factor: Float, units: Float)
    abstract fun glProgramBinary(arg0: Int, arg1: Int, arg2: ByteBuffer, arg3: Int)
    abstract fun glReadPixels(x: Int, y: Int, width: Int, height: Int, format: Int, type: Int, pixels: Buffer)
    abstract fun glReadPixels(x: Int, y: Int, width: Int, height: Int, format: Int, type: Int, pixelsBufferOffset: Int)
    abstract fun glReleaseShaderCompiler()
    abstract fun glRenderbufferStorage(target: Int, internalformat: Int, width: Int, height: Int)
    abstract fun glSampleCoverage(value: Float, invert: Boolean)
    abstract fun glScissor(x: Int, y: Int, width: Int, height: Int)

    abstract fun glShaderBinary(n: Int, shaders: IntArray, offset: Int, binaryformat: Int, binary: Buffer, length: Int)
    abstract fun glShaderBinary(n: Int, shaders: IntBuffer, binaryformat: Int, binary: Buffer, length: Int)
    abstract fun glShaderSource(shader: Int, count: Int, strings: Array<String>, length: IntArray, lengthOffset: Int)
    abstract fun glShaderSource(shader: Int, count: Int, strings: Array<String>, length: IntBuffer)
    abstract fun glShaderSource(shader: Int, string: String)

    abstract fun glStencilFunc(func: Int, ref: Int, mask: Int)
    abstract fun glStencilFuncSeparate(face: Int, func: Int, ref: Int, mask: Int)
    abstract fun glStencilMask(mask: Int)
    abstract fun glStencilMaskSeparate(face: Int, mask: Int)
    abstract fun glStencilOp(fail: Int, zfail: Int, zpass: Int)
    abstract fun glStencilOpSeparate(face: Int, fail: Int, zfail: Int, zpass: Int)

    abstract fun glTexImage2D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, border: Int, format: Int, type: Int, pixels: Buffer?)
    abstract fun glTexImage2D(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: Int, arg6: Int, arg7: Int, arg8: Int)
    abstract fun glTexImage3D(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: Int, arg6: Int, arg7: Int, arg8: Int, arg9: Buffer)
    abstract fun glTexImage3D(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: Int, arg6: Int, arg7: Int, arg8: Int, arg9: Int)
    abstract fun glTexParameterf(target: Int, pname: Int, param: Float)
    abstract fun glTexParameterfv(target: Int, pname: Int, params: FloatBuffer)
    abstract fun glTexParameteri(target: Int, pname: Int, param: Int)
    abstract fun glTexParameteriv(target: Int, pname: Int, params: IntBuffer)
    abstract fun glTexSubImage2D(target: Int, level: Int, xoffset: Int, yoffset: Int, width: Int, height: Int, format: Int, type: Int, pixels: Buffer)
    abstract fun glTexSubImage2D(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: Int, arg6: Int, arg7: Int, arg8: Int)
    abstract fun glTexSubImage3D(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: Int, arg6: Int, arg7: Int, arg8: Int, arg9: Int, arg10: ByteBuffer)
    abstract fun glTexSubImage3D(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int, arg5: Int, arg6: Int, arg7: Int, arg8: Int, arg9: Int, arg10: Int)

    abstract fun glUniform1f(location: Int, x: Float)
    abstract fun glUniform1fv(location: Int, count: Int, v: FloatBuffer)
    abstract fun glUniform1i(location: Int, x: Int)
    abstract fun glUniform1iv(location: Int, count: Int, v: IntBuffer)
    abstract fun glUniform2f(location: Int, x: Float, y: Float)
    abstract fun glUniform2fv(location: Int, count: Int, v: FloatBuffer)
    abstract fun glUniform2i(location: Int, x: Int, y: Int)
    abstract fun glUniform2iv(location: Int, count: Int, v: IntBuffer)
    abstract fun glUniform3f(location: Int, x: Float, y: Float, z: Float)
    abstract fun glUniform3fv(location: Int, count: Int, v: FloatBuffer)
    abstract fun glUniform3i(location: Int, x: Int, y: Int, z: Int)
    abstract fun glUniform3iv(location: Int, count: Int, v: IntBuffer)
    abstract fun glUniform4f(location: Int, x: Float, y: Float, z: Float, w: Float)
    abstract fun glUniform4fv(location: Int, count: Int, v: FloatBuffer)
    abstract fun glUniform4i(location: Int, x: Int, y: Int, z: Int, w: Int)
    abstract fun glUniform4iv(location: Int, count: Int, v: IntBuffer)
    abstract fun glUniformMatrix2fv(location: Int, count: Int, transpose: Boolean, value: FloatBuffer)
    abstract fun glUniformMatrix3fv(location: Int, count: Int, transpose: Boolean, value: FloatBuffer)
    abstract fun glUniformMatrix4fv(location: Int, count: Int, transpose: Boolean, value: FloatBuffer)

    abstract fun glUnmapBuffer(arg0: Int): Boolean
    abstract fun glUseProgram(program: Int)
    abstract fun glValidateProgram(program: Int)

    abstract fun glVertexAttrib1f(indx: Int, x: Float)
    abstract fun glVertexAttrib1fv(indx: Int, values: FloatBuffer)
    abstract fun glVertexAttrib2f(indx: Int, x: Float, y: Float)
    abstract fun glVertexAttrib2fv(indx: Int, values: FloatBuffer)
    abstract fun glVertexAttrib3f(indx: Int, x: Float, y: Float, z: Float)
    abstract fun glVertexAttrib3fv(indx: Int, values: FloatBuffer)
    abstract fun glVertexAttrib4f(indx: Int, x: Float, y: Float, z: Float, w: Float)
    abstract fun glVertexAttrib4fv(indx: Int, values: FloatBuffer)
    abstract fun glVertexAttribPointer(indx: Int, size: Int, type: Int, normalized: Boolean, stride: Int, ptr: Buffer)
    abstract fun glVertexAttribPointer(indx: Int, size: Int, type: Int, normalized: Boolean, stride: Int, ptr: Int)

    abstract fun glViewport(x: Int, y: Int, width: Int, height: Int)
    abstract fun hasGLSL(): Boolean
    abstract fun isExtensionAvailable(extension: String): Boolean
    abstract fun isFunctionAvailable(function: String): Boolean

    companion object {

        val GL_ACTIVE_TEXTURE = 0x84E0
        val GL_DEPTH_BUFFER_BIT = 0x00000100
        val GL_STENCIL_BUFFER_BIT = 0x00000400
        val GL_COLOR_BUFFER_BIT = 0x00004000
        val GL_FALSE = 0
        val GL_TRUE = 1
        val GL_POINTS = 0x0000
        val GL_LINES = 0x0001
        val GL_LINE_LOOP = 0x0002
        val GL_LINE_STRIP = 0x0003
        val GL_TRIANGLES = 0x0004
        val GL_TRIANGLE_STRIP = 0x0005
        val GL_TRIANGLE_FAN = 0x0006
        val GL_ZERO = 0
        val GL_ONE = 1
        val GL_SRC_COLOR = 0x0300
        val GL_ONE_MINUS_SRC_COLOR = 0x0301
        val GL_SRC_ALPHA = 0x0302
        val GL_ONE_MINUS_SRC_ALPHA = 0x0303
        val GL_DST_ALPHA = 0x0304
        val GL_ONE_MINUS_DST_ALPHA = 0x0305
        val GL_DST_COLOR = 0x0306
        val GL_ONE_MINUS_DST_COLOR = 0x0307
        val GL_SRC_ALPHA_SATURATE = 0x0308
        val GL_FUNC_ADD = 0x8006
        val GL_BLEND_EQUATION = 0x8009
        val GL_BLEND_EQUATION_RGB = 0x8009 /* == BLEND_EQUATION */
        val GL_BLEND_EQUATION_ALPHA = 0x883D
        val GL_FUNC_SUBTRACT = 0x800A
        val GL_FUNC_REVERSE_SUBTRACT = 0x800B
        val GL_BLEND_DST_RGB = 0x80C8
        val GL_BLEND_SRC_RGB = 0x80C9
        val GL_BLEND_DST_ALPHA = 0x80CA
        val GL_BLEND_SRC_ALPHA = 0x80CB
        val GL_CONSTANT_COLOR = 0x8001
        val GL_ONE_MINUS_CONSTANT_COLOR = 0x8002
        val GL_CONSTANT_ALPHA = 0x8003
        val GL_ONE_MINUS_CONSTANT_ALPHA = 0x8004
        val GL_BLEND_COLOR = 0x8005
        val GL_ARRAY_BUFFER = 0x8892
        val GL_ELEMENT_ARRAY_BUFFER = 0x8893
        val GL_ARRAY_BUFFER_BINDING = 0x8894
        val GL_ELEMENT_ARRAY_BUFFER_BINDING = 0x8895
        val GL_STREAM_DRAW = 0x88E0
        val GL_STATIC_DRAW = 0x88E4
        val GL_DYNAMIC_DRAW = 0x88E8
        val GL_BUFFER_SIZE = 0x8764
        val GL_BUFFER_USAGE = 0x8765
        val GL_CURRENT_VERTEX_ATTRIB = 0x8626
        val GL_FRONT = 0x0404
        val GL_BACK = 0x0405
        val GL_FRONT_AND_BACK = 0x0408
        val GL_TEXTURE_2D = 0x0DE1
        val GL_CULL_FACE = 0x0B44
        val GL_BLEND = 0x0BE2
        val GL_DITHER = 0x0BD0
        val GL_STENCIL_TEST = 0x0B90
        val GL_DEPTH_TEST = 0x0B71
        val GL_SCISSOR_TEST = 0x0C11
        val GL_POLYGON_OFFSET_FILL = 0x8037
        val GL_SAMPLE_ALPHA_TO_COVERAGE = 0x809E
        val GL_SAMPLE_COVERAGE = 0x80A0
        val GL_NO_ERROR = 0
        val GL_INVALID_ENUM = 0x0500
        val GL_INVALID_VALUE = 0x0501
        val GL_INVALID_OPERATION = 0x0502
        val GL_OUT_OF_MEMORY = 0x0505
        val GL_CW = 0x0900
        val GL_CCW = 0x0901
        val GL_LINE_WIDTH = 0x0B21
        val GL_ALIASED_POINT_SIZE_RANGE = 0x846D
        val GL_ALIASED_LINE_WIDTH_RANGE = 0x846E
        val GL_CULL_FACE_MODE = 0x0B45
        val GL_FRONT_FACE = 0x0B46
        val GL_DEPTH_RANGE = 0x0B70
        val GL_DEPTH_WRITEMASK = 0x0B72
        val GL_DEPTH_CLEAR_VALUE = 0x0B73
        val GL_DEPTH_FUNC = 0x0B74
        val GL_STENCIL_CLEAR_VALUE = 0x0B91
        val GL_STENCIL_FUNC = 0x0B92
        val GL_STENCIL_FAIL = 0x0B94
        val GL_STENCIL_PASS_DEPTH_FAIL = 0x0B95
        val GL_STENCIL_PASS_DEPTH_PASS = 0x0B96
        val GL_STENCIL_REF = 0x0B97
        val GL_STENCIL_VALUE_MASK = 0x0B93
        val GL_STENCIL_WRITEMASK = 0x0B98
        val GL_STENCIL_BACK_FUNC = 0x8800
        val GL_STENCIL_BACK_FAIL = 0x8801
        val GL_STENCIL_BACK_PASS_DEPTH_FAIL = 0x8802
        val GL_STENCIL_BACK_PASS_DEPTH_PASS = 0x8803
        val GL_STENCIL_BACK_REF = 0x8CA3
        val GL_STENCIL_BACK_VALUE_MASK = 0x8CA4
        val GL_STENCIL_BACK_WRITEMASK = 0x8CA5
        val GL_VIEWPORT = 0x0BA2
        val GL_SCISSOR_BOX = 0x0C10
        val GL_COLOR_CLEAR_VALUE = 0x0C22
        val GL_COLOR_WRITEMASK = 0x0C23
        val GL_UNPACK_ALIGNMENT = 0x0CF5
        val GL_PACK_ALIGNMENT = 0x0D05
        val GL_MAX_TEXTURE_SIZE = 0x0D33
        val GL_MAX_VIEWPORT_DIMS = 0x0D3A
        val GL_SUBPIXEL_BITS = 0x0D50
        val GL_RED_BITS = 0x0D52
        val GL_GREEN_BITS = 0x0D53
        val GL_BLUE_BITS = 0x0D54
        val GL_ALPHA_BITS = 0x0D55
        val GL_DEPTH_BITS = 0x0D56
        val GL_STENCIL_BITS = 0x0D57
        val GL_POLYGON_OFFSET_UNITS = 0x2A00
        val GL_POLYGON_OFFSET_FACTOR = 0x8038
        val GL_TEXTURE_BINDING_2D = 0x8069
        val GL_SAMPLE_BUFFERS = 0x80A8
        val GL_SAMPLES = 0x80A9
        val GL_SAMPLE_COVERAGE_VALUE = 0x80AA
        val GL_SAMPLE_COVERAGE_INVERT = 0x80AB
        val GL_NUM_COMPRESSED_TEXTURE_FORMATS = 0x86A2
        val GL_COMPRESSED_TEXTURE_FORMATS = 0x86A3
        val GL_DONT_CARE = 0x1100
        val GL_FASTEST = 0x1101
        val GL_NICEST = 0x1102
        val GL_GENERATE_MIPMAP_HINT = 0x8192
        val GL_BYTE = 0x1400
        val GL_UNSIGNED_BYTE = 0x1401
        val GL_SHORT = 0x1402
        val GL_UNSIGNED_SHORT = 0x1403
        val GL_INT = 0x1404
        val GL_UNSIGNED_INT = 0x1405
        val GL_FLOAT = 0x1406
        val GL_FIXED = 0x140C
        val GL_DEPTH_COMPONENT = 0x1902
        val GL_ALPHA = 0x1906
        val GL_RGB = 0x1907
        val GL_RGBA = 0x1908
        val GL_BGRA = 0x80E1
        val GL_LUMINANCE = 0x1909
        val GL_LUMINANCE_ALPHA = 0x190A
        val GL_UNSIGNED_SHORT_4_4_4_4 = 0x8033
        val GL_UNSIGNED_SHORT_5_5_5_1 = 0x8034
        val GL_UNSIGNED_SHORT_5_6_5 = 0x8363
        val GL_UNSIGNED_INT_8_8_8_8 = 0x8035
        val GL_UNSIGNED_INT_8_8_8_8_REV = 0x8367
        val GL_FRAGMENT_SHADER = 0x8B30
        val GL_VERTEX_SHADER = 0x8B31
        val GL_MAX_VERTEX_ATTRIBS = 0x8869
        val GL_MAX_VERTEX_UNIFORM_VECTORS = 0x8DFB
        val GL_MAX_VARYING_VECTORS = 0x8DFC
        val GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS = 0x8B4D
        val GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS = 0x8B4C
        val GL_MAX_TEXTURE_IMAGE_UNITS = 0x8872
        val GL_MAX_FRAGMENT_UNIFORM_VECTORS = 0x8DFD
        val GL_SHADER_TYPE = 0x8B4F
        val GL_DELETE_STATUS = 0x8B80
        val GL_LINK_STATUS = 0x8B82
        val GL_VALIDATE_STATUS = 0x8B83
        val GL_ATTACHED_SHADERS = 0x8B85
        val GL_ACTIVE_UNIFORMS = 0x8B86
        val GL_ACTIVE_UNIFORM_MAX_LENGTH = 0x8B87
        val GL_ACTIVE_ATTRIBUTES = 0x8B89
        val GL_ACTIVE_ATTRIBUTE_MAX_LENGTH = 0x8B8A
        val GL_SHADING_LANGUAGE_VERSION = 0x8B8C
        val GL_CURRENT_PROGRAM = 0x8B8D
        val GL_NEVER = 0x0200
        val GL_LESS = 0x0201
        val GL_EQUAL = 0x0202
        val GL_LEQUAL = 0x0203
        val GL_GREATER = 0x0204
        val GL_NOTEQUAL = 0x0205
        val GL_GEQUAL = 0x0206
        val GL_ALWAYS = 0x0207
        val GL_KEEP = 0x1E00
        val GL_REPLACE = 0x1E01
        val GL_INCR = 0x1E02
        val GL_DECR = 0x1E03
        val GL_INVERT = 0x150A
        val GL_INCR_WRAP = 0x8507
        val GL_DECR_WRAP = 0x8508
        val GL_VENDOR = 0x1F00
        val GL_RENDERER = 0x1F01
        val GL_VERSION = 0x1F02
        val GL_EXTENSIONS = 0x1F03
        val GL_NEAREST = 0x2600
        val GL_LINEAR = 0x2601
        val GL_NEAREST_MIPMAP_NEAREST = 0x2700
        val GL_LINEAR_MIPMAP_NEAREST = 0x2701
        val GL_NEAREST_MIPMAP_LINEAR = 0x2702
        val GL_LINEAR_MIPMAP_LINEAR = 0x2703
        val GL_TEXTURE_MAG_FILTER = 0x2800
        val GL_TEXTURE_MIN_FILTER = 0x2801
        val GL_TEXTURE_WRAP_S = 0x2802
        val GL_TEXTURE_WRAP_T = 0x2803
        val GL_TEXTURE = 0x1702
        val GL_TEXTURE_CUBE_MAP = 0x8513
        val GL_TEXTURE_BINDING_CUBE_MAP = 0x8514
        val GL_TEXTURE_CUBE_MAP_POSITIVE_X = 0x8515
        val GL_TEXTURE_CUBE_MAP_NEGATIVE_X = 0x8516
        val GL_TEXTURE_CUBE_MAP_POSITIVE_Y = 0x8517
        val GL_TEXTURE_CUBE_MAP_NEGATIVE_Y = 0x8518
        val GL_TEXTURE_CUBE_MAP_POSITIVE_Z = 0x8519
        val GL_TEXTURE_CUBE_MAP_NEGATIVE_Z = 0x851A
        val GL_MAX_CUBE_MAP_TEXTURE_SIZE = 0x851C
        val GL_TEXTURE0 = 0x84C0
        val GL_TEXTURE1 = 0x84C1
        val GL_TEXTURE2 = 0x84C2
        val GL_TEXTURE3 = 0x84C3
        val GL_TEXTURE4 = 0x84C4
        val GL_TEXTURE5 = 0x84C5
        val GL_TEXTURE6 = 0x84C6
        val GL_TEXTURE7 = 0x84C7
        val GL_TEXTURE8 = 0x84C8
        val GL_TEXTURE9 = 0x84C9
        val GL_TEXTURE10 = 0x84CA
        val GL_TEXTURE11 = 0x84CB
        val GL_TEXTURE12 = 0x84CC
        val GL_TEXTURE13 = 0x84CD
        val GL_TEXTURE14 = 0x84CE
        val GL_TEXTURE15 = 0x84CF
        val GL_TEXTURE16 = 0x84D0
        val GL_TEXTURE17 = 0x84D1
        val GL_TEXTURE18 = 0x84D2
        val GL_TEXTURE19 = 0x84D3
        val GL_TEXTURE20 = 0x84D4
        val GL_TEXTURE21 = 0x84D5
        val GL_TEXTURE22 = 0x84D6
        val GL_TEXTURE23 = 0x84D7
        val GL_TEXTURE24 = 0x84D8
        val GL_TEXTURE25 = 0x84D9
        val GL_TEXTURE26 = 0x84DA
        val GL_TEXTURE27 = 0x84DB
        val GL_TEXTURE28 = 0x84DC
        val GL_TEXTURE29 = 0x84DD
        val GL_TEXTURE30 = 0x84DE
        val GL_TEXTURE31 = 0x84DF
        val GL_REPEAT = 0x2901
        val GL_CLAMP_TO_EDGE = 0x812F
        val GL_MIRRORED_REPEAT = 0x8370
        val GL_FLOAT_VEC2 = 0x8B50
        val GL_FLOAT_VEC3 = 0x8B51
        val GL_FLOAT_VEC4 = 0x8B52
        val GL_INT_VEC2 = 0x8B53
        val GL_INT_VEC3 = 0x8B54
        val GL_INT_VEC4 = 0x8B55
        val GL_BOOL = 0x8B56
        val GL_BOOL_VEC2 = 0x8B57
        val GL_BOOL_VEC3 = 0x8B58
        val GL_BOOL_VEC4 = 0x8B59
        val GL_FLOAT_MAT2 = 0x8B5A
        val GL_FLOAT_MAT3 = 0x8B5B
        val GL_FLOAT_MAT4 = 0x8B5C
        val GL_SAMPLER_2D = 0x8B5E
        val GL_SAMPLER_CUBE = 0x8B60
        val GL_VERTEX_ATTRIB_ARRAY_ENABLED = 0x8622
        val GL_VERTEX_ATTRIB_ARRAY_SIZE = 0x8623
        val GL_VERTEX_ATTRIB_ARRAY_STRIDE = 0x8624
        val GL_VERTEX_ATTRIB_ARRAY_TYPE = 0x8625
        val GL_VERTEX_ATTRIB_ARRAY_NORMALIZED = 0x886A
        val GL_VERTEX_ATTRIB_ARRAY_POINTER = 0x8645
        val GL_VERTEX_ATTRIB_ARRAY_BUFFER_BINDING = 0x889F
        val GL_IMPLEMENTATION_COLOR_READ_TYPE = 0x8B9A
        val GL_IMPLEMENTATION_COLOR_READ_FORMAT = 0x8B9B
        val GL_COMPILE_STATUS = 0x8B81
        val GL_INFO_LOG_LENGTH = 0x8B84
        val GL_SHADER_SOURCE_LENGTH = 0x8B88
        val GL_SHADER_COMPILER = 0x8DFA
        val GL_SHADER_BINARY_FORMATS = 0x8DF8
        val GL_NUM_SHADER_BINARY_FORMATS = 0x8DF9
        val GL_LOW_FLOAT = 0x8DF0
        val GL_MEDIUM_FLOAT = 0x8DF1
        val GL_HIGH_FLOAT = 0x8DF2
        val GL_LOW_INT = 0x8DF3
        val GL_MEDIUM_INT = 0x8DF4
        val GL_HIGH_INT = 0x8DF5
        val GL_FRAMEBUFFER = 0x8D40
        val GL_RENDERBUFFER = 0x8D41
        val GL_RGBA4 = 0x8056
        val GL_RGB5_A1 = 0x8057
        val GL_RGB565 = 0x8D62
        val GL_DEPTH_COMPONENT16 = 0x81A5
        val GL_STENCIL_INDEX = 0x1901
        val GL_STENCIL_INDEX8 = 0x8D48
        val GL_RENDERBUFFER_WIDTH = 0x8D42
        val GL_RENDERBUFFER_HEIGHT = 0x8D43
        val GL_RENDERBUFFER_INTERNAL_FORMAT = 0x8D44
        val GL_RENDERBUFFER_RED_SIZE = 0x8D50
        val GL_RENDERBUFFER_GREEN_SIZE = 0x8D51
        val GL_RENDERBUFFER_BLUE_SIZE = 0x8D52
        val GL_RENDERBUFFER_ALPHA_SIZE = 0x8D53
        val GL_RENDERBUFFER_DEPTH_SIZE = 0x8D54
        val GL_RENDERBUFFER_STENCIL_SIZE = 0x8D55
        val GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE = 0x8CD0
        val GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME = 0x8CD1
        val GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LEVEL = 0x8CD2
        val GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_CUBE_MAP_FACE = 0x8CD3
        val GL_COLOR_ATTACHMENT0 = 0x8CE0
        val GL_DEPTH_ATTACHMENT = 0x8D00
        val GL_STENCIL_ATTACHMENT = 0x8D20
        val GL_NONE = 0
        val GL_FRAMEBUFFER_COMPLETE = 0x8CD5
        val GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT = 0x8CD6
        val GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT = 0x8CD7
        val GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS = 0x8CD9
        val GL_FRAMEBUFFER_UNSUPPORTED = 0x8CDD
        val GL_FRAMEBUFFER_BINDING = 0x8CA6
        val GL_RENDERBUFFER_BINDING = 0x8CA7
        val GL_MAX_RENDERBUFFER_SIZE = 0x84E8
        val GL_INVALID_FRAMEBUFFER_OPERATION = 0x0506
    }
}
