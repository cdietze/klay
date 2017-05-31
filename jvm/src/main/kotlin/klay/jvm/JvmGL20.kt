package klay.jvm

import klay.core.buffers.Buffer
import klay.core.buffers.ByteBuffer
import klay.core.buffers.FloatBuffer
import klay.core.buffers.IntBuffer
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.*
import org.lwjgl.system.MemoryUtil
import java.io.UnsupportedEncodingException
import java.nio.ByteOrder

/**
 * An implementation of the [GL20] interface based on LWJGL3 and OpenGL ~3.0.
 */
class JvmGL20 : klay.core.GL20(JvmBuffers(), java.lang.Boolean.getBoolean("klay.glerrors")) {

    override fun glActiveTexture(texture: Int) {
        GL13.glActiveTexture(texture)
    }

    override fun glAttachShader(program: Int, shader: Int) {
        GL20.glAttachShader(program, shader)
    }

    override fun glBindAttribLocation(program: Int, index: Int, name: String) {
        GL20.glBindAttribLocation(program, index, name)
    }

    override fun glBindBuffer(target: Int, buffer: Int) {
        GL15.glBindBuffer(target, buffer)
    }

    override fun glBindFramebuffer(target: Int, framebuffer: Int) {
        EXTFramebufferObject.glBindFramebufferEXT(target, framebuffer)
    }

    override fun glBindRenderbuffer(target: Int, renderbuffer: Int) {
        EXTFramebufferObject.glBindRenderbufferEXT(target, renderbuffer)
    }

    override fun glBindTexture(target: Int, texture: Int) {
        GL11.glBindTexture(target, texture)
    }

    override fun glBlendColor(red: Float, green: Float, blue: Float, alpha: Float) {
        GL14.glBlendColor(red, green, blue, alpha)
    }

    override fun glBlendEquation(mode: Int) {
        GL14.glBlendEquation(mode)
    }

    override fun glBlendEquationSeparate(modeRGB: Int, modeAlpha: Int) {
        GL20.glBlendEquationSeparate(modeRGB, modeAlpha)
    }

    override fun glBlendFunc(sfactor: Int, dfactor: Int) {
        GL11.glBlendFunc(sfactor, dfactor)
    }

    override fun glBlendFuncSeparate(srcRGB: Int, dstRGB: Int, srcAlpha: Int,
                                     dstAlpha: Int) {
        GL14.glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha)
    }

    override fun glBufferData(target: Int, size: Int, data: Buffer, usage: Int) {
        // Limit the buffer to the given size, restoring it afterwards
        val oldLimit = data.limit()
        if (data is JvmByteBuffer) {
            data.limit(data.position() + size)
            GL15.glBufferData(target, data.nioBuffer, usage)
        } else if (data is JvmIntBuffer) {
            data.limit(data.position() + size / 4)
            GL15.glBufferData(target, data.nioBuffer, usage)
        } else if (data is JvmFloatBuffer) {
            data.limit(data.position() + size / 4)
            GL15.glBufferData(target, data.nioBuffer, usage)
        } else if (data is JvmDoubleBuffer) {
            data.limit(data.position() + size / 8)
            GL15.glBufferData(target, data.nioBuffer, usage)
        } else if (data is JvmShortBuffer) {
            data.limit(data.position() + size / 2)
            GL15.glBufferData(target, data.nioBuffer, usage)
        }
        data.limit(oldLimit)
    }

    override fun glBufferSubData(target: Int, offset: Int, size: Int, data: Buffer) {
        // Limit the buffer to the given size, restoring it afterwards
        val oldLimit = data.limit()
        if (data is JvmByteBuffer) {
            data.limit(data.position() + size)
            GL15.glBufferSubData(target, offset.toLong(), data.nioBuffer)
        } else if (data is JvmIntBuffer) {
            data.limit(data.position() + size / 4)
            GL15.glBufferSubData(target, offset.toLong(), data.nioBuffer)
        } else if (data is JvmFloatBuffer) {
            data.limit(data.position() + size / 4)
            GL15.glBufferSubData(target, offset.toLong(), data.nioBuffer)
        } else if (data is JvmDoubleBuffer) {
            data.limit(data.position() + size / 8)
            GL15.glBufferSubData(target, offset.toLong(), data.nioBuffer)
        } else if (data is JvmShortBuffer) {
            data.limit(data.position() + size / 2)
            GL15.glBufferSubData(target, offset.toLong(), data.nioBuffer)
        }
        data.limit(oldLimit)
    }

    override fun glCheckFramebufferStatus(target: Int): Int {
        return EXTFramebufferObject.glCheckFramebufferStatusEXT(target)
    }

    override fun glClear(mask: Int) {
        GL11.glClear(mask)
    }

    override fun glClearColor(red: Float, green: Float, blue: Float, alpha: Float) {
        GL11.glClearColor(red, green, blue, alpha)
    }

    override fun glClearDepthf(depth: Float) {
        GL11.glClearDepth(depth.toDouble())
    }

    override fun glClearStencil(s: Int) {
        GL11.glClearStencil(s)
    }

    override fun glColorMask(red: Boolean, green: Boolean, blue: Boolean,
                             alpha: Boolean) {
        GL11.glColorMask(red, green, blue, alpha)
    }

    override fun glCompileShader(shader: Int) {
        GL20.glCompileShader(shader)
    }

    override fun glCompressedTexImage2D(target: Int, level: Int, internalformat: Int,
                                        width: Int, height: Int, border: Int,
                                        imageSize: Int, data: Buffer) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun glCompressedTexSubImage2D(target: Int, level: Int, xoffset: Int, yoffset: Int,
                                           width: Int, height: Int, format: Int,
                                           imageSize: Int, data: Buffer) {
        throw UnsupportedOperationException("not implemented")
    }

    override fun glCopyTexImage2D(target: Int, level: Int, internalformat: Int,
                                  x: Int, y: Int, width: Int, height: Int, border: Int) {
        GL11.glCopyTexImage2D(target, level, internalformat, x, y, width,
                height, border)
    }

    override fun glCopyTexSubImage2D(target: Int, level: Int, xoffset: Int,
                                     yoffset: Int, x: Int, y: Int, width: Int, height: Int) {
        GL11.glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width,
                height)
    }

    override fun glCreateProgram(): Int {
        return GL20.glCreateProgram()
    }

    override fun glCreateShader(type: Int): Int {
        return GL20.glCreateShader(type)
    }

    override fun glCullFace(mode: Int) {
        GL11.glCullFace(mode)
    }

    override fun glDeleteBuffers(n: Int, buffers: IntBuffer) {
        GL15.glDeleteBuffers((buffers as JvmIntBuffer).nioBuffer)
    }

    override fun glDeleteFramebuffers(n: Int, framebuffers: IntBuffer) {
        EXTFramebufferObject.glDeleteFramebuffersEXT((framebuffers as JvmIntBuffer).nioBuffer)
    }

    override fun glDeleteProgram(program: Int) {
        GL20.glDeleteProgram(program)
    }

    override fun glDeleteRenderbuffers(n: Int, renderbuffers: IntBuffer) {
        EXTFramebufferObject.glDeleteRenderbuffersEXT((renderbuffers as JvmIntBuffer).nioBuffer)
    }

    override fun glDeleteShader(shader: Int) {
        GL20.glDeleteShader(shader)
    }

    override fun glDeleteTextures(n: Int, textures: IntBuffer) {
        GL11.glDeleteTextures((textures as JvmIntBuffer).nioBuffer)
    }

    override fun glDepthFunc(func: Int) {
        GL11.glDepthFunc(func)
    }

    override fun glDepthMask(flag: Boolean) {
        GL11.glDepthMask(flag)
    }

    override fun glDepthRangef(zNear: Float, zFar: Float) {
        GL11.glDepthRange(zNear.toDouble(), zFar.toDouble())
    }

    override fun glDetachShader(program: Int, shader: Int) {
        GL20.glDetachShader(program, shader)
    }

    override fun glDisable(cap: Int) {
        GL11.glDisable(cap)
    }

    override fun glDisableVertexAttribArray(index: Int) {
        GL20.glDisableVertexAttribArray(index)
    }

    override fun glDrawArrays(mode: Int, first: Int, count: Int) {
        GL11.glDrawArrays(mode, first, count)
    }

    override fun glDrawElements(mode: Int, count: Int, type: Int, indices: Buffer) {
        if (indices is JvmShortBuffer && type == GL_UNSIGNED_SHORT)
            GL11.glDrawElements(mode, indices.nioBuffer)
        else if (indices is JvmByteBuffer && type == GL_UNSIGNED_SHORT)
            GL11.glDrawElements(mode, indices.nioBuffer.asShortBuffer()) // FIXME
        // yay...
        else if (indices is JvmByteBuffer && type == GL_UNSIGNED_BYTE)
            GL11.glDrawElements(mode, indices.nioBuffer)
        else
            throw RuntimeException(
                    "Can't use "
                            + indices.javaClass.name
                            + " with this method. Use ShortBuffer or ByteBuffer instead. Blame LWJGL")
    }

    override fun glEnable(cap: Int) {
        GL11.glEnable(cap)
    }

    override fun glEnableVertexAttribArray(index: Int) {
        GL20.glEnableVertexAttribArray(index)
    }

    override fun glFinish() {
        GL11.glFinish()
    }

    override fun glFlush() {
        GL11.glFlush()
    }

    override fun glFramebufferRenderbuffer(target: Int, attachment: Int,
                                           renderbuffertarget: Int, renderbuffer: Int) {
        EXTFramebufferObject.glFramebufferRenderbufferEXT(
                target, attachment, renderbuffertarget, renderbuffer)
    }

    override fun glFramebufferTexture2D(target: Int, attachment: Int,
                                        textarget: Int, texture: Int, level: Int) {
        EXTFramebufferObject.glFramebufferTexture2DEXT(target, attachment, textarget, texture, level)
    }

    override fun glFrontFace(mode: Int) {
        GL11.glFrontFace(mode)
    }

    override fun glGenBuffers(n: Int, buffers: IntBuffer) {
        GL15.glGenBuffers((buffers as JvmIntBuffer).nioBuffer)
    }

    override fun glGenFramebuffers(n: Int, framebuffers: IntBuffer) {
        EXTFramebufferObject.glGenFramebuffersEXT((framebuffers as JvmIntBuffer).nioBuffer)
    }

    override fun glGenRenderbuffers(n: Int, renderbuffers: IntBuffer) {
        EXTFramebufferObject.glGenRenderbuffersEXT((renderbuffers as JvmIntBuffer).nioBuffer)
    }

    override fun glGenTextures(n: Int, textures: IntBuffer) {
        GL11.glGenTextures((textures as JvmIntBuffer).nioBuffer)
    }

    override fun glGenerateMipmap(target: Int) {
        EXTFramebufferObject.glGenerateMipmapEXT(target)
    }

    override fun glGetAttribLocation(program: Int, name: String): Int {
        return GL20.glGetAttribLocation(program, name)
    }

    override fun glGetBufferParameteriv(target: Int, pname: Int, params: IntBuffer) {
        GL15.glGetBufferParameteriv(target, pname, (params as JvmIntBuffer).nioBuffer)
    }

    override fun glGetError(): Int {
        return GL11.glGetError()
    }

    override fun glGetFloatv(pname: Int, params: FloatBuffer) {
        GL11.glGetFloatv(pname, (params as JvmFloatBuffer).nioBuffer)
    }

    override fun glGetFramebufferAttachmentParameteriv(target: Int, attachment: Int, pname: Int,
                                                       params: IntBuffer) {
        EXTFramebufferObject.glGetFramebufferAttachmentParameterivEXT(target, attachment, pname, (params as JvmIntBuffer).nioBuffer)
    }

    override fun glGetIntegerv(pname: Int, params: IntBuffer) {
        GL11.glGetIntegerv(pname, (params as JvmIntBuffer).nioBuffer)
    }

    override fun glGetProgramInfoLog(program: Int): String {
        val buffer = java.nio.ByteBuffer.allocateDirect(1024 * 10)
        buffer.order(ByteOrder.nativeOrder())
        val tmp = java.nio.ByteBuffer.allocateDirect(4)
        tmp.order(ByteOrder.nativeOrder())
        val intBuffer = tmp.asIntBuffer()

        GL20.glGetProgramInfoLog(program, intBuffer, buffer)
        val numBytes = intBuffer.get(0)
        val bytes = ByteArray(numBytes)
        buffer.get(bytes)
        return String(bytes)
    }

    override fun glGetProgramiv(program: Int, pname: Int, params: IntBuffer) {
        GL20.glGetProgramiv(program, pname, (params as JvmIntBuffer).nioBuffer)
    }

    override fun glGetRenderbufferParameteriv(target: Int, pname: Int, params: IntBuffer) {
        EXTFramebufferObject.glGetRenderbufferParameterivEXT(target, pname, (params as JvmIntBuffer).nioBuffer)
    }

    override fun glGetShaderInfoLog(shader: Int): String {
        val buffer = java.nio.ByteBuffer.allocateDirect(1024 * 10)
        buffer.order(ByteOrder.nativeOrder())
        val tmp = java.nio.ByteBuffer.allocateDirect(4)
        tmp.order(ByteOrder.nativeOrder())
        val intBuffer = tmp.asIntBuffer()

        GL20.glGetShaderInfoLog(shader, intBuffer, buffer)
        val numBytes = intBuffer.get(0)
        val bytes = ByteArray(numBytes)
        buffer.get(bytes)
        return String(bytes)
    }

    override fun glGetShaderPrecisionFormat(shadertype: Int, precisiontype: Int,
                                            range: IntBuffer, precision: IntBuffer) {
        glGetShaderPrecisionFormat(shadertype, precisiontype, range, precision)
    }

    override fun glGetShaderiv(shader: Int, pname: Int, params: IntBuffer) {
        GL20.glGetShaderiv(shader, pname, (params as JvmIntBuffer).nioBuffer)
    }

    override fun glGetString(name: Int): String {
        return GL11.glGetString(name)
    }

    override fun glGetTexParameterfv(target: Int, pname: Int, params: FloatBuffer) {
        GL11.glGetTexParameterfv(target, pname, (params as JvmFloatBuffer).nioBuffer)
    }

    override fun glGetTexParameteriv(target: Int, pname: Int, params: IntBuffer) {
        GL11.glGetTexParameteriv(target, pname, (params as JvmIntBuffer).nioBuffer)
    }

    override fun glGetUniformLocation(program: Int, name: String): Int {
        return GL20.glGetUniformLocation(program, name)
    }

    override fun glGetUniformfv(program: Int, location: Int, params: FloatBuffer) {
        GL20.glGetUniformfv(program, location, (params as JvmFloatBuffer).nioBuffer)
    }

    override fun glGetUniformiv(program: Int, location: Int, params: IntBuffer) {
        GL20.glGetUniformiv(program, location, (params as JvmIntBuffer).nioBuffer)
    }

    override fun glGetVertexAttribfv(index: Int, pname: Int, params: FloatBuffer) {
        GL20.glGetVertexAttribfv(index, pname, (params as JvmFloatBuffer).nioBuffer)
    }

    override fun glGetVertexAttribiv(index: Int, pname: Int, params: IntBuffer) {
        GL20.glGetVertexAttribiv(index, pname, (params as JvmIntBuffer).nioBuffer)
    }

    override fun glHint(target: Int, mode: Int) {
        GL11.glHint(target, mode)
    }

    override fun glIsBuffer(buffer: Int): Boolean {
        return GL15.glIsBuffer(buffer)
    }

    override fun glIsEnabled(cap: Int): Boolean {
        return GL11.glIsEnabled(cap)
    }

    override fun glIsFramebuffer(framebuffer: Int): Boolean {
        return EXTFramebufferObject.glIsFramebufferEXT(framebuffer)
    }

    override fun glIsProgram(program: Int): Boolean {
        return GL20.glIsProgram(program)
    }

    override fun glIsRenderbuffer(renderbuffer: Int): Boolean {
        return EXTFramebufferObject.glIsRenderbufferEXT(renderbuffer)
    }

    override fun glIsShader(shader: Int): Boolean {
        return GL20.glIsShader(shader)
    }

    override fun glIsTexture(texture: Int): Boolean {
        return GL11.glIsTexture(texture)
    }

    override fun glLineWidth(width: Float) {
        GL11.glLineWidth(width)
    }

    override fun glLinkProgram(program: Int) {
        GL20.glLinkProgram(program)
    }

    override fun glPixelStorei(pname: Int, param: Int) {
        GL11.glPixelStorei(pname, param)
    }

    override fun glPolygonOffset(factor: Float, units: Float) {
        GL11.glPolygonOffset(factor, units)
    }

    override fun glReadPixels(x: Int, y: Int, width: Int, height: Int, format: Int, type: Int,
                              pixels: Buffer) {
        if (pixels is JvmByteBuffer)
            GL11.glReadPixels(x, y, width, height, format, type, pixels.nioBuffer)
        else if (pixels is JvmShortBuffer)
            GL11.glReadPixels(x, y, width, height, format, type, pixels.nioBuffer)
        else if (pixels is JvmIntBuffer)
            GL11.glReadPixels(x, y, width, height, format, type, pixels.nioBuffer)
        else if (pixels is JvmFloatBuffer)
            GL11.glReadPixels(x, y, width, height, format, type, pixels.nioBuffer)
        else
            throw RuntimeException(
                    "Can't use " + pixels.javaClass.name + " with this method. Use ByteBuffer, " +
                            "ShortBuffer, IntBuffer or FloatBuffer instead. Blame LWJGL")
    }

    override fun glReleaseShaderCompiler() {
        // nothing to do here
    }

    override fun glRenderbufferStorage(target: Int, internalformat: Int, width: Int, height: Int) {
        EXTFramebufferObject.glRenderbufferStorageEXT(target, internalformat, width, height)
    }

    override fun glSampleCoverage(value: Float, invert: Boolean) {
        GL13.glSampleCoverage(value, invert)
    }

    override fun glScissor(x: Int, y: Int, width: Int, height: Int) {
        GL11.glScissor(x, y, width, height)
    }

    override fun glShaderBinary(n: Int, shaders: IntBuffer, binaryformat: Int, binary: Buffer,
                                length: Int) {
        throw UnsupportedOperationException("unsupported, won't implement")
    }

    override fun glShaderSource(shader: Int, string: String) {
        GL20.glShaderSource(shader, string)
    }

    override fun glStencilFunc(func: Int, ref: Int, mask: Int) {
        GL11.glStencilFunc(func, ref, mask)
    }

    override fun glStencilFuncSeparate(face: Int, func: Int, ref: Int, mask: Int) {
        GL20.glStencilFuncSeparate(face, func, ref, mask)
    }

    override fun glStencilMask(mask: Int) {
        GL11.glStencilMask(mask)
    }

    override fun glStencilMaskSeparate(face: Int, mask: Int) {
        GL20.glStencilMaskSeparate(face, mask)
    }

    override fun glStencilOp(fail: Int, zfail: Int, zpass: Int) {
        GL11.glStencilOp(fail, zfail, zpass)
    }

    override fun glStencilOpSeparate(face: Int, fail: Int, zfail: Int, zpass: Int) {
        GL20.glStencilOpSeparate(face, fail, zfail, zpass)
    }

    override fun glTexImage2D(target: Int, level: Int, internalformat: Int,
                              width: Int, height: Int, border: Int, format: Int, type: Int,
                              pixels: Buffer?) {
        if (pixels == null)
            GL11.glTexImage2D(target, level, internalformat, width, height,
                    border, format, type, null as java.nio.ByteBuffer?)
        else if (pixels is JvmByteBuffer)
            GL11.glTexImage2D(target, level, internalformat, width, height,
                    border, format, type, pixels.nioBuffer)
        else if (pixels is JvmShortBuffer)
            GL11.glTexImage2D(target, level, internalformat, width, height,
                    border, format, type, pixels.nioBuffer)
        else if (pixels is JvmIntBuffer)
            GL11.glTexImage2D(target, level, internalformat, width, height,
                    border, format, type, pixels.nioBuffer)
        else if (pixels is JvmFloatBuffer)
            GL11.glTexImage2D(target, level, internalformat, width, height,
                    border, format, type, pixels.nioBuffer)
        else if (pixels is JvmDoubleBuffer)
            GL11.glTexImage2D(target, level, internalformat, width, height,
                    border, format, type, pixels.nioBuffer)
        else
            throw RuntimeException(
                    "Can't use " + pixels.javaClass.name + " with this method. Use ByteBuffer, " +
                            "ShortBuffer, IntBuffer, FloatBuffer or DoubleBuffer instead. Blame LWJGL")
    }

    override fun glTexParameterf(target: Int, pname: Int, param: Float) {
        GL11.glTexParameterf(target, pname, param)
    }

    override fun glTexParameterfv(target: Int, pname: Int, params: FloatBuffer) {
        GL11.glTexParameterfv(target, pname, (params as JvmFloatBuffer).nioBuffer)
    }

    override fun glTexParameteri(target: Int, pname: Int, param: Int) {
        GL11.glTexParameteri(target, pname, param)
    }

    override fun glTexParameteriv(target: Int, pname: Int, params: IntBuffer) {
        GL11.glTexParameteriv(target, pname, (params as JvmIntBuffer).nioBuffer)
    }

    override fun glTexSubImage2D(target: Int, level: Int, xoffset: Int,
                                 yoffset: Int, width: Int, height: Int, format: Int, type: Int,
                                 pixels: Buffer) {
        if (pixels is JvmByteBuffer)
            GL11.glTexSubImage2D(target, level, xoffset, yoffset, width,
                    height, format, type, pixels.nioBuffer)
        else if (pixels is JvmShortBuffer)
            GL11.glTexSubImage2D(target, level, xoffset, yoffset, width,
                    height, format, type, pixels.nioBuffer)
        else if (pixels is JvmIntBuffer)
            GL11.glTexSubImage2D(target, level, xoffset, yoffset, width,
                    height, format, type, pixels.nioBuffer)
        else if (pixels is JvmFloatBuffer)
            GL11.glTexSubImage2D(target, level, xoffset, yoffset, width,
                    height, format, type, pixels.nioBuffer)
        else if (pixels is JvmDoubleBuffer)
            GL11.glTexSubImage2D(target, level, xoffset, yoffset, width,
                    height, format, type, pixels.nioBuffer)
        else
            throw RuntimeException(
                    "Can't use " + pixels.javaClass.name + " with this method. Use ByteBuffer, " +
                            "ShortBuffer, IntBuffer, FloatBuffer or DoubleBuffer instead. Blame LWJGL")
    }

    override fun glUniform1f(location: Int, x: Float) {
        GL20.glUniform1f(location, x)
    }

    override fun glUniform1fv(location: Int, count: Int, buffer: FloatBuffer) {
        val oldLimit = buffer.limit()
        buffer.limit(buffer.position() + count)
        GL20.glUniform1fv(location, (buffer as JvmFloatBuffer).nioBuffer)
        buffer.limit(oldLimit)
    }

    override fun glUniform1i(location: Int, x: Int) {
        GL20.glUniform1i(location, x)
    }

    override fun glUniform1iv(location: Int, count: Int, value: IntBuffer) {
        val oldLimit = value.limit()
        value.limit(value.position() + count)
        GL20.glUniform1iv(location, (value as JvmIntBuffer).nioBuffer)
        value.limit(oldLimit)
    }

    override fun glUniform2f(location: Int, x: Float, y: Float) {
        GL20.glUniform2f(location, x, y)
    }

    override fun glUniform2fv(location: Int, count: Int, value: FloatBuffer) {
        val oldLimit = value.limit()
        value.limit(value.position() + 2 * count)
        GL20.glUniform2fv(location, (value as JvmFloatBuffer).nioBuffer)
        value.limit(oldLimit)
    }

    override fun glUniform2i(location: Int, x: Int, y: Int) {
        GL20.glUniform2i(location, x, y)
    }

    override fun glUniform2iv(location: Int, count: Int, buffer: IntBuffer) {
        val oldLimit = buffer.limit()
        buffer.limit(buffer.position() + 2 * count)
        GL20.glUniform2iv(location, (buffer as JvmIntBuffer).nioBuffer)
        buffer.limit(oldLimit)
    }

    override fun glUniform3f(location: Int, x: Float, y: Float, z: Float) {
        GL20.glUniform3f(location, x, y, z)
    }

    override fun glUniform3fv(location: Int, count: Int, buffer: FloatBuffer) {
        val oldLimit = buffer.limit()
        buffer.limit(buffer.position() + 3 * count)
        GL20.glUniform3fv(location, (buffer as JvmFloatBuffer).nioBuffer)
        buffer.limit(oldLimit)
    }

    override fun glUniform3i(location: Int, x: Int, y: Int, z: Int) {
        GL20.glUniform3i(location, x, y, z)
    }

    override fun glUniform3iv(location: Int, count: Int, buffer: IntBuffer) {
        val oldLimit = buffer.limit()
        buffer.limit(buffer.position() + 3 * count)
        GL20.glUniform3iv(location, (buffer as JvmIntBuffer).nioBuffer)
        buffer.limit(oldLimit)
    }

    override fun glUniform4f(location: Int, x: Float, y: Float, z: Float, w: Float) {
        GL20.glUniform4f(location, x, y, z, w)
    }

    override fun glUniform4fv(location: Int, count: Int, buffer: FloatBuffer) {
        val oldLimit = buffer.limit()
        buffer.limit(buffer.position() + 4 * count)
        GL20.glUniform4fv(location, (buffer as JvmFloatBuffer).nioBuffer)
        buffer.limit(oldLimit)
    }

    override fun glUniform4i(location: Int, x: Int, y: Int, z: Int, w: Int) {
        GL20.glUniform4i(location, x, y, z, w)
    }

    override fun glUniform4iv(location: Int, count: Int, buffer: IntBuffer) {
        val oldLimit = buffer.limit()
        buffer.limit(buffer.position() + 4 * count)
        GL20.glUniform4iv(location, (buffer as JvmIntBuffer).nioBuffer)
        buffer.limit(oldLimit)
    }

    override fun glUniformMatrix2fv(location: Int, count: Int, transpose: Boolean, buffer: FloatBuffer) {
        val oldLimit = buffer.limit()
        buffer.limit(buffer.position() + 2 * 2 * count)
        GL20.glUniformMatrix2fv(location, transpose, (buffer as JvmFloatBuffer).nioBuffer)
        buffer.limit(oldLimit)
    }

    override fun glUniformMatrix3fv(location: Int, count: Int, transpose: Boolean, buffer: FloatBuffer) {
        val oldLimit = buffer.limit()
        buffer.limit(buffer.position() + 3 * 3 * count)
        GL20.glUniformMatrix3fv(location, transpose, (buffer as JvmFloatBuffer).nioBuffer)
        buffer.limit(oldLimit)
    }

    override fun glUniformMatrix4fv(location: Int, count: Int, transpose: Boolean, buffer: FloatBuffer) {
        val oldLimit = buffer.limit()
        buffer.limit(buffer.position() + 4 * 4 * count)
        GL20.glUniformMatrix4fv(location, transpose, (buffer as JvmFloatBuffer).nioBuffer)
        buffer.limit(oldLimit)
    }

    override fun glUseProgram(program: Int) {
        GL20.glUseProgram(program)
    }

    override fun glValidateProgram(program: Int) {
        GL20.glValidateProgram(program)
    }

    override fun glVertexAttrib1f(index: Int, x: Float) {
        GL20.glVertexAttrib1f(index, x)
    }

    override fun glVertexAttrib1fv(index: Int, values: FloatBuffer) {
        GL20.glVertexAttrib1f(index, (values as JvmFloatBuffer).nioBuffer.get())
    }

    override fun glVertexAttrib2f(index: Int, x: Float, y: Float) {
        GL20.glVertexAttrib2f(index, x, y)
    }

    override fun glVertexAttrib2fv(index: Int, values: FloatBuffer) {
        val nioBuffer = (values as JvmFloatBuffer).nioBuffer
        GL20.glVertexAttrib2f(index, nioBuffer.get(), nioBuffer.get())
    }

    override fun glVertexAttrib3f(index: Int, x: Float, y: Float, z: Float) {
        GL20.glVertexAttrib3f(index, x, y, z)
    }

    override fun glVertexAttrib3fv(index: Int, values: FloatBuffer) {
        val nioBuffer = (values as JvmFloatBuffer).nioBuffer
        GL20.glVertexAttrib3f(index, nioBuffer.get(), nioBuffer.get(), nioBuffer.get())
    }

    override fun glVertexAttrib4f(index: Int, x: Float, y: Float, z: Float, w: Float) {
        GL20.glVertexAttrib4f(index, x, y, z, w)
    }

    override fun glVertexAttrib4fv(index: Int, values: FloatBuffer) {
        val nioBuffer = (values as JvmFloatBuffer).nioBuffer
        GL20.glVertexAttrib4f(index, nioBuffer.get(), nioBuffer.get(), nioBuffer.get(), nioBuffer.get())
    }

    override fun glVertexAttribPointer(index: Int, size: Int, type: Int, normalized: Boolean, stride: Int,
                                       ptr: Buffer) {
        if (ptr is JvmFloatBuffer) {
            GL20.glVertexAttribPointer(index, size, type, normalized, stride, ptr.nioBuffer)
        } else if (ptr is JvmByteBuffer) {
            GL20.glVertexAttribPointer(index, size, type, normalized, stride, ptr.nioBuffer)
        } else if (ptr is JvmShortBuffer) {
            GL20.glVertexAttribPointer(index, size, type, normalized, stride, ptr.nioBuffer)
        } else if (ptr is JvmIntBuffer) {
            GL20.glVertexAttribPointer(index, size, type, normalized, stride, ptr.nioBuffer)
        } else {
            throw RuntimeException("NYI for " + ptr.javaClass)
        }
    }

    override fun glViewport(x: Int, y: Int, width: Int, height: Int) {
        GL11.glViewport(x, y, width, height)
    }

    override fun glDrawElements(mode: Int, count: Int, type: Int, indices: Int) {
        GL11.glDrawElements(mode, count, type, indices.toLong())
    }

    override fun glVertexAttribPointer(index: Int, size: Int, type: Int,
                                       normalized: Boolean, stride: Int, ptr: Int) {
        GL20.glVertexAttribPointer(index, size, type, normalized, stride, ptr.toLong())
    }

    override val platformGLExtensions: String
        get() = throw UnsupportedOperationException("NYI - not in LWJGL.")

    override val swapInterval: Int
        get() = throw UnsupportedOperationException("NYI - not in LWJGL.")

    override fun glClearDepth(depth: Double) {
        GL11.glClearDepth(depth)
    }

    override fun glCompressedTexImage2D(target: Int, level: Int, internalformat: Int,
                                        width: Int, height: Int, border: Int,
                                        imageSize: Int, data: Int) {
        GL13.glCompressedTexImage2D(target, level, internalformat, width, height, border, imageSize, data.toLong())
    }

    override fun glCompressedTexImage3D(target: Int, level: Int, internalformat: Int,
                                        width: Int, height: Int, depth: Int, border: Int,
                                        imageSize: Int, data: ByteBuffer) {
        GL13.glCompressedTexImage3D(target, level, internalformat, width, height, depth, border,
                imageSize, MemoryUtil.memAddress((data as JvmByteBuffer).nioBuffer))
    }

    override fun glCompressedTexImage3D(target: Int, level: Int, internalFormat: Int,
                                        width: Int, height: Int, depth: Int, border: Int,
                                        imageSize: Int, data: Int) {
        GL13.glCompressedTexImage3D(
                target, level, internalFormat, width, height, depth, border, imageSize, data.toLong())
    }

    override fun glCompressedTexSubImage2D(target: Int, level: Int, xoffset: Int, yoffset: Int,
                                           width: Int, height: Int, format: Int, imageSize: Int,
                                           data: Int) {
        GL13.glCompressedTexSubImage2D(
                target, level, xoffset, yoffset, width, height, format, imageSize, data.toLong())
    }

    override fun glCompressedTexSubImage3D(target: Int, level: Int, xoffset: Int, yoffset: Int, zoffset: Int,
                                           width: Int, height: Int, depth: Int,
                                           format: Int, imageSize: Int, data: ByteBuffer) {
        // imageSize is calculated in glCompressedTexSubImage3D.
        GL13.glCompressedTexSubImage3D(target, level, xoffset, yoffset, zoffset,
                width, height, depth, format, (data as JvmByteBuffer).nioBuffer)
    }

    override fun glCompressedTexSubImage3D(target: Int, level: Int, xoffset: Int, yoffset: Int, zoffset: Int,
                                           width: Int, height: Int, depth: Int,
                                           format: Int, imageSize: Int, data: Int) {
        val dataBuffer = BufferUtils.createByteBuffer(4)
        dataBuffer.putInt(data)
        dataBuffer.rewind()
        // imageSize is calculated in glCompressedTexSubImage3D.
        GL13.glCompressedTexSubImage3D(
                target, level, xoffset, yoffset, zoffset, width, height, depth, format, dataBuffer)
    }

    override fun glCopyTexSubImage3D(target: Int, level: Int, xoffset: Int, yoffset: Int, zoffset: Int,
                                     x: Int, y: Int, width: Int, height: Int) {
        GL12.glCopyTexSubImage3D(target, level, xoffset, yoffset, zoffset, x, y, width, height)
    }

    override fun glDepthRange(zNear: Double, zFar: Double) {
        GL11.glDepthRange(zNear, zFar)
    }

    override fun glFramebufferTexture3D(target: Int, attachment: Int, textarget: Int, texture: Int,
                                        level: Int, zoffset: Int) {
        EXTFramebufferObject.glFramebufferTexture3DEXT(
                target, attachment, textarget, texture, level, zoffset)
    }

    override fun glGetActiveAttrib(program: Int, index: Int, bufsize: Int, length: IntArray, lengthOffset: Int,
                                   size: IntArray, sizeOffset: Int, type: IntArray, typeOffset: Int,
                                   name: ByteArray, nameOffset: Int) {
        // http://www.khronos.org/opengles/sdk/docs/man/xhtml/glGetActiveAttrib.xml
        // Returns length, size, type, name
        bufs.resizeIntBuffer(2)

        // Return name, length
        val nameString = GL20.glGetActiveAttrib(program, index, BufferUtils.createIntBuffer(bufsize), (bufs.intBuffer as JvmIntBuffer).nioBuffer)
        try {
            val nameBytes = nameString.toByteArray(charset("UTF-8"))
            val nameLength = nameBytes.size - nameOffset
            bufs.setByteBuffer(nameBytes, nameOffset, nameLength)
            bufs.byteBuffer.get(name, nameOffset, nameLength)
            length[lengthOffset] = nameLength
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        // Return size, type
        bufs.intBuffer.get(size, 0, 1)
        bufs.intBuffer.get(type, 0, 1)
    }

    override fun glGetActiveAttrib(program: Int, index: Int, bufsize: Int,
                                   length: IntBuffer, size: IntBuffer, type: IntBuffer, name: ByteBuffer) {
        val typeTmp = BufferUtils.createIntBuffer(2)
        GL20.glGetActiveAttrib(program, index, BufferUtils.createIntBuffer(256), typeTmp)
        type.put(typeTmp.get(0))
        type.rewind()
    }

    override fun glGetActiveUniform(program: Int, index: Int, bufsize: Int,
                                    length: IntArray, lengthOffset: Int, size: IntArray, sizeOffset: Int,
                                    type: IntArray, typeOffset: Int, name: ByteArray, nameOffset: Int) {
        bufs.resizeIntBuffer(2)

        // Return name, length
        val nameString = GL20.glGetActiveUniform(program, index, BufferUtils.createIntBuffer(256), (bufs.intBuffer as JvmIntBuffer).nioBuffer)
        try {
            val nameBytes = nameString.toByteArray(charset("UTF-8"))
            val nameLength = nameBytes.size - nameOffset
            bufs.setByteBuffer(nameBytes, nameOffset, nameLength)
            bufs.byteBuffer.get(name, nameOffset, nameLength)
            length[lengthOffset] = nameLength
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        // Return size, type
        bufs.intBuffer.get(size, 0, 1)
        bufs.intBuffer.get(type, 0, 1)
    }

    override fun glGetActiveUniform(program: Int, index: Int, bufsize: Int, length: IntBuffer,
                                    size: IntBuffer, type: IntBuffer, name: ByteBuffer) {
        val typeTmp = BufferUtils.createIntBuffer(2)
        GL20.glGetActiveAttrib(program, index, BufferUtils.createIntBuffer(256), typeTmp)
        type.put(typeTmp.get(0))
        type.rewind()
    }

    override fun glGetAttachedShaders(program: Int, maxcount: Int, count: IntBuffer, shaders: IntBuffer) {
        GL20.glGetAttachedShaders(program, (count as JvmIntBuffer).nioBuffer, (shaders as JvmIntBuffer).nioBuffer)
    }

    override fun glGetBoolean(pname: Int): Boolean {
        return GL11.glGetBoolean(pname)
    }

    override fun glGetBooleanv(pname: Int, params: ByteBuffer) {
        GL11.glGetBooleanv(pname, (params as JvmByteBuffer).nioBuffer)
    }

    override fun glGetBoundBuffer(target: Int): Int {
        throw UnsupportedOperationException("glGetBoundBuffer not supported in GLES 2.0 or LWJGL.")
    }

    override fun glGetFloat(pname: Int): Float {
        return GL11.glGetFloat(pname)
    }

    override fun glGetInteger(pname: Int): Int {
        return GL11.glGetInteger(pname)
    }

    override fun glGetProgramBinary(program: Int, bufSize: Int, length: IntBuffer,
                                    binaryFormat: IntBuffer, binary: ByteBuffer) {
        GL41.glGetProgramBinary(program, (length as JvmIntBuffer).nioBuffer, (binaryFormat as JvmIntBuffer).nioBuffer, (binary as JvmByteBuffer).nioBuffer)
    }

    override fun glGetProgramInfoLog(program: Int, bufsize: Int, length: IntBuffer, infolog: ByteBuffer) {
        val buffer = java.nio.ByteBuffer.allocateDirect(1024 * 10)
        buffer.order(ByteOrder.nativeOrder())
        val tmp = java.nio.ByteBuffer.allocateDirect(4)
        tmp.order(ByteOrder.nativeOrder())
        val intBuffer = tmp.asIntBuffer()
        GL20.glGetProgramInfoLog(program, intBuffer, buffer)
    }

    override fun glGetShaderInfoLog(shader: Int, bufsize: Int, length: IntBuffer, infolog: ByteBuffer) {
        GL20.glGetShaderInfoLog(shader, (length as JvmIntBuffer).nioBuffer, (infolog as JvmByteBuffer).nioBuffer)
    }

    override fun glGetShaderPrecisionFormat(shadertype: Int, precisiontype: Int,
                                            range: IntArray, rangeOffset: Int,
                                            precision: IntArray, precisionOffset: Int) {
        throw UnsupportedOperationException("NYI")
    }

    override fun glGetShaderSource(shader: Int, bufsize: Int, length: IntArray, lengthOffset: Int,
                                   source: ByteArray, sourceOffset: Int) {
        throw UnsupportedOperationException("NYI")
    }

    override fun glGetShaderSource(shader: Int, bufsize: Int, length: IntBuffer, source: ByteBuffer) {
        throw UnsupportedOperationException("NYI")
    }

    override fun glIsVBOArrayEnabled(): Boolean {
        throw UnsupportedOperationException("NYI - not in LWJGL.")
    }

    override fun glIsVBOElementEnabled(): Boolean {
        throw UnsupportedOperationException("NYI - not in LWJGL.")
    }

    override fun glMapBuffer(target: Int, access: Int): ByteBuffer {
        return JvmByteBuffer(GL15.glMapBuffer(target, access, null))
    }

    override fun glProgramBinary(program: Int, binaryFormat: Int, binary: ByteBuffer, length: Int) {
        // Length is calculated in glProgramBinary.
        GL41.glProgramBinary(program, binaryFormat, (binary as JvmByteBuffer).nioBuffer)
    }

    override fun glReadPixels(x: Int, y: Int, width: Int, height: Int, format: Int, type: Int,
                              pixelsBufferOffset: Int) {
        GL11.glReadPixels(x, y, width, height, format, type, pixelsBufferOffset.toLong())
    }

    override fun glShaderBinary(n: Int, shaders: IntArray, offset: Int, binaryformat: Int,
                                binary: Buffer, length: Int) {
        throw UnsupportedOperationException("NYI")
    }

    override fun glShaderSource(shader: Int, count: Int, strings: Array<String>, length: IntArray, lengthOffset: Int) {
        for (str in strings)
            GL20.glShaderSource(shader, str)
    }

    override fun glShaderSource(shader: Int, count: Int, strings: Array<String>, length: IntBuffer) {
        for (str in strings)
            GL20.glShaderSource(shader, str)
    }

    override fun glTexImage2D(target: Int, level: Int, internalFormat: Int, width: Int, height: Int,
                              border: Int, format: Int, type: Int, pixels: Int) {
        GL11.glTexImage2D(target, level, internalFormat, width, height, border, format, type, pixels.toLong())
    }

    override fun glTexImage3D(target: Int, level: Int, internalFormat: Int, width: Int, height: Int,
                              depth: Int, border: Int, format: Int, type: Int, pixels: Buffer) {
        if (pixels !is JvmByteBuffer)
            throw UnsupportedOperationException("Buffer must be a ByteBuffer.")
        GL12.glTexImage3D(target, level, internalFormat, width, height, depth, border, format, type, pixels.nioBuffer)
    }

    override fun glTexImage3D(target: Int, level: Int, internalFormat: Int, width: Int, height: Int,
                              depth: Int, border: Int, format: Int, type: Int, pixels: Int) {
        GL12.glTexImage3D(target, level, internalFormat, width, height, depth, border, format, type, pixels.toLong())
    }

    override fun glTexSubImage2D(target: Int, level: Int, xoffset: Int, yoffset: Int, width: Int, height: Int, format: Int,
                                 type: Int, pixels: Int) {
        GL11.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels.toLong())
    }

    override fun glTexSubImage3D(target: Int, level: Int, xoffset: Int, yoffset: Int, zoffset: Int,
                                 width: Int, height: Int, depth: Int, format: Int, type: Int,
                                 pixels: ByteBuffer) {
        GL12.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format,
                type, (pixels as JvmByteBuffer).nioBuffer)
    }

    override fun glTexSubImage3D(target: Int, level: Int, xoffset: Int, yoffset: Int, zoffset: Int,
                                 width: Int, height: Int, depth: Int, format: Int, type: Int, pixels: Int) {
        val byteBuffer = BufferUtils.createByteBuffer(1)
        byteBuffer.putInt(pixels)
        byteBuffer.rewind()
        GL12.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format,
                type, byteBuffer)
    }

    override fun glUnmapBuffer(target: Int): Boolean {
        return GL15.glUnmapBuffer(target)
    }

    override fun hasGLSL(): Boolean {
        throw UnsupportedOperationException("NYI - not in LWJGL.")
    }

    override fun isExtensionAvailable(extension: String): Boolean {
        throw UnsupportedOperationException("NYI - not in LWJGL.")
    }

    override fun isFunctionAvailable(function: String): Boolean {
        throw UnsupportedOperationException("NYI - not in LWJGL.")
    }
}
