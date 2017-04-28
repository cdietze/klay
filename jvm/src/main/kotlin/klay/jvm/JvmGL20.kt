/*******************************************************************************
 * Copyright 2011 See AUTHORS file.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package playn.java

import java.io.UnsupportedEncodingException
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.DoubleBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.nio.ShortBuffer

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.EXTFramebufferObject
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL14
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import org.lwjgl.opengl.GL41
import org.lwjgl.system.MemoryUtil

/**
 * An implementation of the [GL20] interface based on LWJGL3 and OpenGL ~3.0.
 */
internal class LWJGLGL20 : playn.core.GL20(object : Buffers() {
    fun createByteBuffer(size: Int): ByteBuffer {
        return BufferUtils.createByteBuffer(size)
    }
}, java.lang.Boolean.getBoolean("playn.glerrors")) {

    fun glActiveTexture(texture: Int) {
        GL13.glActiveTexture(texture)
    }

    fun glAttachShader(program: Int, shader: Int) {
        GL20.glAttachShader(program, shader)
    }

    fun glBindAttribLocation(program: Int, index: Int, name: String) {
        GL20.glBindAttribLocation(program, index, name)
    }

    fun glBindBuffer(target: Int, buffer: Int) {
        GL15.glBindBuffer(target, buffer)
    }

    fun glBindFramebuffer(target: Int, framebuffer: Int) {
        EXTFramebufferObject.glBindFramebufferEXT(target, framebuffer)
    }

    fun glBindRenderbuffer(target: Int, renderbuffer: Int) {
        EXTFramebufferObject.glBindRenderbufferEXT(target, renderbuffer)
    }

    fun glBindTexture(target: Int, texture: Int) {
        GL11.glBindTexture(target, texture)
    }

    fun glBlendColor(red: Float, green: Float, blue: Float, alpha: Float) {
        GL14.glBlendColor(red, green, blue, alpha)
    }

    fun glBlendEquation(mode: Int) {
        GL14.glBlendEquation(mode)
    }

    fun glBlendEquationSeparate(modeRGB: Int, modeAlpha: Int) {
        GL20.glBlendEquationSeparate(modeRGB, modeAlpha)
    }

    fun glBlendFunc(sfactor: Int, dfactor: Int) {
        GL11.glBlendFunc(sfactor, dfactor)
    }

    fun glBlendFuncSeparate(srcRGB: Int, dstRGB: Int, srcAlpha: Int,
                            dstAlpha: Int) {
        GL14.glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha)
    }

    fun glBufferData(target: Int, size: Int, data: Buffer?, usage: Int) {
        if (data == null) {
            GL15.glBufferData(target, size.toLong(), usage)
            return
        }

        // Limit the buffer to the given size, restoring it afterwards
        val oldLimit = data.limit()
        if (data is ByteBuffer) {
            val subData = data
            subData.limit(subData.position() + size)
            GL15.glBufferData(target, subData, usage)

        } else if (data is IntBuffer) {
            val subData = data
            subData.limit(subData.position() + size / 4)
            GL15.glBufferData(target, subData, usage)

        } else if (data is FloatBuffer) {
            val subData = data
            subData.limit(subData.position() + size / 4)
            GL15.glBufferData(target, subData, usage)

        } else if (data is DoubleBuffer) {
            val subData = data
            subData.limit(subData.position() + size / 8)
            GL15.glBufferData(target, subData, usage)

        } else if (data is ShortBuffer) {
            val subData = data
            subData.limit(subData.position() + size / 2)
            GL15.glBufferData(target, subData, usage)
        }
        data.limit(oldLimit)
    }

    fun glBufferSubData(target: Int, offset: Int, size: Int, data: Buffer) {
        // Limit the buffer to the given size, restoring it afterwards
        val oldLimit = data.limit()
        if (data is ByteBuffer) {
            val subData = data
            subData.limit(subData.position() + size)
            GL15.glBufferSubData(target, offset.toLong(), subData)

        } else if (data is IntBuffer) {
            val subData = data
            subData.limit(subData.position() + size / 4)
            GL15.glBufferSubData(target, offset.toLong(), subData)

        } else if (data is FloatBuffer) {
            val subData = data
            subData.limit(subData.position() + size / 4)
            GL15.glBufferSubData(target, offset.toLong(), subData)

        } else if (data is DoubleBuffer) {
            val subData = data
            subData.limit(subData.position() + size / 8)
            GL15.glBufferSubData(target, offset.toLong(), subData)

        } else if (data is ShortBuffer) {
            val subData = data
            subData.limit(subData.position() + size / 2)
            GL15.glBufferSubData(target, offset.toLong(), subData)
        }
        data.limit(oldLimit)
    }

    fun glCheckFramebufferStatus(target: Int): Int {
        return EXTFramebufferObject.glCheckFramebufferStatusEXT(target)
    }

    fun glClear(mask: Int) {
        GL11.glClear(mask)
    }

    fun glClearColor(red: Float, green: Float, blue: Float, alpha: Float) {
        GL11.glClearColor(red, green, blue, alpha)
    }

    fun glClearDepthf(depth: Float) {
        GL11.glClearDepth(depth.toDouble())
    }

    fun glClearStencil(s: Int) {
        GL11.glClearStencil(s)
    }

    fun glColorMask(red: Boolean, green: Boolean, blue: Boolean,
                    alpha: Boolean) {
        GL11.glColorMask(red, green, blue, alpha)
    }

    fun glCompileShader(shader: Int) {
        GL20.glCompileShader(shader)
    }

    fun glCompressedTexImage2D(target: Int, level: Int, internalformat: Int,
                               width: Int, height: Int, border: Int,
                               imageSize: Int, data: Buffer) {
        throw UnsupportedOperationException("not implemented")
    }

    fun glCompressedTexSubImage2D(target: Int, level: Int, xoffset: Int, yoffset: Int,
                                  width: Int, height: Int, format: Int,
                                  imageSize: Int, data: Buffer) {
        throw UnsupportedOperationException("not implemented")
    }

    fun glCopyTexImage2D(target: Int, level: Int, internalformat: Int,
                         x: Int, y: Int, width: Int, height: Int, border: Int) {
        GL11.glCopyTexImage2D(target, level, internalformat, x, y, width,
                height, border)
    }

    fun glCopyTexSubImage2D(target: Int, level: Int, xoffset: Int,
                            yoffset: Int, x: Int, y: Int, width: Int, height: Int) {
        GL11.glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width,
                height)
    }

    fun glCreateProgram(): Int {
        return GL20.glCreateProgram()
    }

    fun glCreateShader(type: Int): Int {
        return GL20.glCreateShader(type)
    }

    fun glCullFace(mode: Int) {
        GL11.glCullFace(mode)
    }

    fun glDeleteBuffers(n: Int, buffers: IntBuffer) {
        GL15.glDeleteBuffers(buffers)
    }

    fun glDeleteFramebuffers(n: Int, framebuffers: IntBuffer) {
        EXTFramebufferObject.glDeleteFramebuffersEXT(framebuffers)
    }

    fun glDeleteProgram(program: Int) {
        GL20.glDeleteProgram(program)
    }

    fun glDeleteRenderbuffers(n: Int, renderbuffers: IntBuffer) {
        EXTFramebufferObject.glDeleteRenderbuffersEXT(renderbuffers)
    }

    fun glDeleteShader(shader: Int) {
        GL20.glDeleteShader(shader)
    }

    fun glDeleteTextures(n: Int, textures: IntBuffer) {
        GL11.glDeleteTextures(textures)
    }

    fun glDepthFunc(func: Int) {
        GL11.glDepthFunc(func)
    }

    fun glDepthMask(flag: Boolean) {
        GL11.glDepthMask(flag)
    }

    fun glDepthRangef(zNear: Float, zFar: Float) {
        GL11.glDepthRange(zNear.toDouble(), zFar.toDouble())
    }

    fun glDetachShader(program: Int, shader: Int) {
        GL20.glDetachShader(program, shader)
    }

    fun glDisable(cap: Int) {
        GL11.glDisable(cap)
    }

    fun glDisableVertexAttribArray(index: Int) {
        GL20.glDisableVertexAttribArray(index)
    }

    fun glDrawArrays(mode: Int, first: Int, count: Int) {
        GL11.glDrawArrays(mode, first, count)
    }

    fun glDrawElements(mode: Int, count: Int, type: Int, indices: Buffer) {
        if (indices is ShortBuffer && type == GL_UNSIGNED_SHORT)
            GL11.glDrawElements(mode, indices)
        else if (indices is ByteBuffer && type == GL_UNSIGNED_SHORT)
            GL11.glDrawElements(mode, indices.asShortBuffer()) // FIXME
        else if (indices is ByteBuffer && type == GL_UNSIGNED_BYTE)
            GL11.glDrawElements(mode, indices)
        else
            throw RuntimeException(
                    "Can't use "
                            + indices.javaClass.name
                            + " with this method. Use ShortBuffer or ByteBuffer instead. Blame LWJGL")// yay...
    }

    fun glEnable(cap: Int) {
        GL11.glEnable(cap)
    }

    fun glEnableVertexAttribArray(index: Int) {
        GL20.glEnableVertexAttribArray(index)
    }

    fun glFinish() {
        GL11.glFinish()
    }

    fun glFlush() {
        GL11.glFlush()
    }

    fun glFramebufferRenderbuffer(target: Int, attachment: Int,
                                  renderbuffertarget: Int, renderbuffer: Int) {
        EXTFramebufferObject.glFramebufferRenderbufferEXT(
                target, attachment, renderbuffertarget, renderbuffer)
    }

    fun glFramebufferTexture2D(target: Int, attachment: Int,
                               textarget: Int, texture: Int, level: Int) {
        EXTFramebufferObject.glFramebufferTexture2DEXT(target, attachment, textarget, texture, level)
    }

    fun glFrontFace(mode: Int) {
        GL11.glFrontFace(mode)
    }

    fun glGenBuffers(n: Int, buffers: IntBuffer) {
        GL15.glGenBuffers(buffers)
    }

    fun glGenFramebuffers(n: Int, framebuffers: IntBuffer) {
        EXTFramebufferObject.glGenFramebuffersEXT(framebuffers)
    }

    fun glGenRenderbuffers(n: Int, renderbuffers: IntBuffer) {
        EXTFramebufferObject.glGenRenderbuffersEXT(renderbuffers)
    }

    fun glGenTextures(n: Int, textures: IntBuffer) {
        GL11.glGenTextures(textures)
    }

    fun glGenerateMipmap(target: Int) {
        EXTFramebufferObject.glGenerateMipmapEXT(target)
    }

    fun glGetAttribLocation(program: Int, name: String): Int {
        return GL20.glGetAttribLocation(program, name)
    }

    fun glGetBufferParameteriv(target: Int, pname: Int, params: IntBuffer) {
        GL15.glGetBufferParameteriv(target, pname, params)
    }

    fun glGetError(): Int {
        return GL11.glGetError()
    }

    fun glGetFloatv(pname: Int, params: FloatBuffer) {
        GL11.glGetFloatv(pname, params)
    }

    fun glGetFramebufferAttachmentParameteriv(target: Int, attachment: Int, pname: Int,
                                              params: IntBuffer) {
        EXTFramebufferObject.glGetFramebufferAttachmentParameterivEXT(target, attachment, pname, params)
    }

    fun glGetIntegerv(pname: Int, params: IntBuffer) {
        GL11.glGetIntegerv(pname, params)
    }

    fun glGetProgramInfoLog(program: Int): String {
        val buffer = ByteBuffer.allocateDirect(1024 * 10)
        buffer.order(ByteOrder.nativeOrder())
        val tmp = ByteBuffer.allocateDirect(4)
        tmp.order(ByteOrder.nativeOrder())
        val intBuffer = tmp.asIntBuffer()

        GL20.glGetProgramInfoLog(program, intBuffer, buffer)
        val numBytes = intBuffer.get(0)
        val bytes = ByteArray(numBytes)
        buffer.get(bytes)
        return String(bytes)
    }

    fun glGetProgramiv(program: Int, pname: Int, params: IntBuffer) {
        GL20.glGetProgramiv(program, pname, params)
    }

    fun glGetRenderbufferParameteriv(target: Int, pname: Int, params: IntBuffer) {
        EXTFramebufferObject.glGetRenderbufferParameterivEXT(target, pname, params)
    }

    fun glGetShaderInfoLog(shader: Int): String {
        val buffer = ByteBuffer.allocateDirect(1024 * 10)
        buffer.order(ByteOrder.nativeOrder())
        val tmp = ByteBuffer.allocateDirect(4)
        tmp.order(ByteOrder.nativeOrder())
        val intBuffer = tmp.asIntBuffer()

        GL20.glGetShaderInfoLog(shader, intBuffer, buffer)
        val numBytes = intBuffer.get(0)
        val bytes = ByteArray(numBytes)
        buffer.get(bytes)
        return String(bytes)
    }

    fun glGetShaderPrecisionFormat(shadertype: Int, precisiontype: Int,
                                   range: IntBuffer, precision: IntBuffer) {
        glGetShaderPrecisionFormat(shadertype, precisiontype, range, precision)
    }

    fun glGetShaderiv(shader: Int, pname: Int, params: IntBuffer) {
        GL20.glGetShaderiv(shader, pname, params)
    }

    fun glGetString(name: Int): String {
        return GL11.glGetString(name)
    }

    fun glGetTexParameterfv(target: Int, pname: Int, params: FloatBuffer) {
        GL11.glGetTexParameterfv(target, pname, params)
    }

    fun glGetTexParameteriv(target: Int, pname: Int, params: IntBuffer) {
        GL11.glGetTexParameteriv(target, pname, params)
    }

    fun glGetUniformLocation(program: Int, name: String): Int {
        return GL20.glGetUniformLocation(program, name)
    }

    fun glGetUniformfv(program: Int, location: Int, params: FloatBuffer) {
        GL20.glGetUniformfv(program, location, params)
    }

    fun glGetUniformiv(program: Int, location: Int, params: IntBuffer) {
        GL20.glGetUniformiv(program, location, params)
    }

    fun glGetVertexAttribfv(index: Int, pname: Int, params: FloatBuffer) {
        GL20.glGetVertexAttribfv(index, pname, params)
    }

    fun glGetVertexAttribiv(index: Int, pname: Int, params: IntBuffer) {
        GL20.glGetVertexAttribiv(index, pname, params)
    }

    fun glHint(target: Int, mode: Int) {
        GL11.glHint(target, mode)
    }

    fun glIsBuffer(buffer: Int): Boolean {
        return GL15.glIsBuffer(buffer)
    }

    fun glIsEnabled(cap: Int): Boolean {
        return GL11.glIsEnabled(cap)
    }

    fun glIsFramebuffer(framebuffer: Int): Boolean {
        return EXTFramebufferObject.glIsFramebufferEXT(framebuffer)
    }

    fun glIsProgram(program: Int): Boolean {
        return GL20.glIsProgram(program)
    }

    fun glIsRenderbuffer(renderbuffer: Int): Boolean {
        return EXTFramebufferObject.glIsRenderbufferEXT(renderbuffer)
    }

    fun glIsShader(shader: Int): Boolean {
        return GL20.glIsShader(shader)
    }

    fun glIsTexture(texture: Int): Boolean {
        return GL11.glIsTexture(texture)
    }

    fun glLineWidth(width: Float) {
        GL11.glLineWidth(width)
    }

    fun glLinkProgram(program: Int) {
        GL20.glLinkProgram(program)
    }

    fun glPixelStorei(pname: Int, param: Int) {
        GL11.glPixelStorei(pname, param)
    }

    fun glPolygonOffset(factor: Float, units: Float) {
        GL11.glPolygonOffset(factor, units)
    }

    fun glReadPixels(x: Int, y: Int, width: Int, height: Int, format: Int, type: Int,
                     pixels: Buffer) {
        if (pixels is ByteBuffer)
            GL11.glReadPixels(x, y, width, height, format, type,
                    pixels)
        else if (pixels is ShortBuffer)
            GL11.glReadPixels(x, y, width, height, format, type,
                    pixels)
        else if (pixels is IntBuffer)
            GL11.glReadPixels(x, y, width, height, format, type,
                    pixels)
        else if (pixels is FloatBuffer)
            GL11.glReadPixels(x, y, width, height, format, type,
                    pixels)
        else
            throw RuntimeException(
                    "Can't use " + pixels.javaClass.name + " with this method. Use ByteBuffer, " +
                            "ShortBuffer, IntBuffer or FloatBuffer instead. Blame LWJGL")
    }

    fun glReleaseShaderCompiler() {
        // nothing to do here
    }

    fun glRenderbufferStorage(target: Int, internalformat: Int, width: Int, height: Int) {
        EXTFramebufferObject.glRenderbufferStorageEXT(target, internalformat, width, height)
    }

    fun glSampleCoverage(value: Float, invert: Boolean) {
        GL13.glSampleCoverage(value, invert)
    }

    fun glScissor(x: Int, y: Int, width: Int, height: Int) {
        GL11.glScissor(x, y, width, height)
    }

    fun glShaderBinary(n: Int, shaders: IntBuffer, binaryformat: Int, binary: Buffer,
                       length: Int) {
        throw UnsupportedOperationException("unsupported, won't implement")
    }

    fun glShaderSource(shader: Int, string: String) {
        GL20.glShaderSource(shader, string)
    }

    fun glStencilFunc(func: Int, ref: Int, mask: Int) {
        GL11.glStencilFunc(func, ref, mask)
    }

    fun glStencilFuncSeparate(face: Int, func: Int, ref: Int, mask: Int) {
        GL20.glStencilFuncSeparate(face, func, ref, mask)
    }

    fun glStencilMask(mask: Int) {
        GL11.glStencilMask(mask)
    }

    fun glStencilMaskSeparate(face: Int, mask: Int) {
        GL20.glStencilMaskSeparate(face, mask)
    }

    fun glStencilOp(fail: Int, zfail: Int, zpass: Int) {
        GL11.glStencilOp(fail, zfail, zpass)
    }

    fun glStencilOpSeparate(face: Int, fail: Int, zfail: Int, zpass: Int) {
        GL20.glStencilOpSeparate(face, fail, zfail, zpass)
    }

    fun glTexImage2D(target: Int, level: Int, internalformat: Int,
                     width: Int, height: Int, border: Int, format: Int, type: Int,
                     pixels: Buffer?) {
        if (pixels is ByteBuffer || pixels == null)
            GL11.glTexImage2D(target, level, internalformat, width, height,
                    border, format, type, pixels as ByteBuffer?)
        else if (pixels is ShortBuffer)
            GL11.glTexImage2D(target, level, internalformat, width, height,
                    border, format, type, pixels as ShortBuffer?)
        else if (pixels is IntBuffer)
            GL11.glTexImage2D(target, level, internalformat, width, height,
                    border, format, type, pixels as IntBuffer?)
        else if (pixels is FloatBuffer)
            GL11.glTexImage2D(target, level, internalformat, width, height,
                    border, format, type, pixels as FloatBuffer?)
        else if (pixels is DoubleBuffer)
            GL11.glTexImage2D(target, level, internalformat, width, height,
                    border, format, type, pixels as DoubleBuffer?)
        else
            throw RuntimeException(
                    "Can't use " + pixels.javaClass.name + " with this method. Use ByteBuffer, " +
                            "ShortBuffer, IntBuffer, FloatBuffer or DoubleBuffer instead. Blame LWJGL")
    }

    fun glTexParameterf(target: Int, pname: Int, param: Float) {
        GL11.glTexParameterf(target, pname, param)
    }

    fun glTexParameterfv(target: Int, pname: Int, params: FloatBuffer) {
        GL11.glTexParameterfv(target, pname, params)
    }

    fun glTexParameteri(target: Int, pname: Int, param: Int) {
        GL11.glTexParameteri(target, pname, param)
    }

    fun glTexParameteriv(target: Int, pname: Int, params: IntBuffer) {
        GL11.glTexParameteriv(target, pname, params)
    }

    fun glTexSubImage2D(target: Int, level: Int, xoffset: Int,
                        yoffset: Int, width: Int, height: Int, format: Int, type: Int,
                        pixels: Buffer) {
        if (pixels is ByteBuffer)
            GL11.glTexSubImage2D(target, level, xoffset, yoffset, width,
                    height, format, type, pixels)
        else if (pixels is ShortBuffer)
            GL11.glTexSubImage2D(target, level, xoffset, yoffset, width,
                    height, format, type, pixels)
        else if (pixels is IntBuffer)
            GL11.glTexSubImage2D(target, level, xoffset, yoffset, width,
                    height, format, type, pixels)
        else if (pixels is FloatBuffer)
            GL11.glTexSubImage2D(target, level, xoffset, yoffset, width,
                    height, format, type, pixels)
        else if (pixels is DoubleBuffer)
            GL11.glTexSubImage2D(target, level, xoffset, yoffset, width,
                    height, format, type, pixels)
        else
            throw RuntimeException(
                    "Can't use " + pixels.javaClass.name + " with this method. Use ByteBuffer, " +
                            "ShortBuffer, IntBuffer, FloatBuffer or DoubleBuffer instead. Blame LWJGL")
    }

    fun glUniform1f(location: Int, x: Float) {
        GL20.glUniform1f(location, x)
    }

    fun glUniform1fv(location: Int, count: Int, buffer: FloatBuffer) {
        val oldLimit = buffer.limit()
        buffer.limit(buffer.position() + count)
        GL20.glUniform1fv(location, buffer)
        buffer.limit(oldLimit)
    }

    fun glUniform1i(location: Int, x: Int) {
        GL20.glUniform1i(location, x)
    }

    fun glUniform1iv(location: Int, count: Int, buffer: IntBuffer) {
        val oldLimit = buffer.limit()
        buffer.limit(buffer.position() + count)
        GL20.glUniform1iv(location, buffer)
        buffer.limit(oldLimit)
    }

    fun glUniform2f(location: Int, x: Float, y: Float) {
        GL20.glUniform2f(location, x, y)
    }

    fun glUniform2fv(location: Int, count: Int, buffer: FloatBuffer) {
        val oldLimit = buffer.limit()
        buffer.limit(buffer.position() + 2 * count)
        GL20.glUniform2fv(location, buffer)
        buffer.limit(oldLimit)
    }

    fun glUniform2i(location: Int, x: Int, y: Int) {
        GL20.glUniform2i(location, x, y)
    }

    fun glUniform2iv(location: Int, count: Int, buffer: IntBuffer) {
        val oldLimit = buffer.limit()
        buffer.limit(buffer.position() + 2 * count)
        GL20.glUniform2iv(location, buffer)
        buffer.limit(oldLimit)
    }

    fun glUniform3f(location: Int, x: Float, y: Float, z: Float) {
        GL20.glUniform3f(location, x, y, z)
    }

    fun glUniform3fv(location: Int, count: Int, buffer: FloatBuffer) {
        val oldLimit = buffer.limit()
        buffer.limit(buffer.position() + 3 * count)
        GL20.glUniform3fv(location, buffer)
        buffer.limit(oldLimit)
    }

    fun glUniform3i(location: Int, x: Int, y: Int, z: Int) {
        GL20.glUniform3i(location, x, y, z)
    }

    fun glUniform3iv(location: Int, count: Int, buffer: IntBuffer) {
        val oldLimit = buffer.limit()
        buffer.limit(buffer.position() + 3 * count)
        GL20.glUniform3iv(location, buffer)
        buffer.limit(oldLimit)
    }

    fun glUniform4f(location: Int, x: Float, y: Float, z: Float, w: Float) {
        GL20.glUniform4f(location, x, y, z, w)
    }

    fun glUniform4fv(location: Int, count: Int, buffer: FloatBuffer) {
        val oldLimit = buffer.limit()
        buffer.limit(buffer.position() + 4 * count)
        GL20.glUniform4fv(location, buffer)
        buffer.limit(oldLimit)
    }

    fun glUniform4i(location: Int, x: Int, y: Int, z: Int, w: Int) {
        GL20.glUniform4i(location, x, y, z, w)
    }

    fun glUniform4iv(location: Int, count: Int, buffer: IntBuffer) {
        val oldLimit = buffer.limit()
        buffer.limit(buffer.position() + 4 * count)
        GL20.glUniform4iv(location, buffer)
        buffer.limit(oldLimit)
    }

    fun glUniformMatrix2fv(location: Int, count: Int, transpose: Boolean, buffer: FloatBuffer) {
        val oldLimit = buffer.limit()
        buffer.limit(buffer.position() + 2 * 2 * count)
        GL20.glUniformMatrix2fv(location, transpose, buffer)
        buffer.limit(oldLimit)
    }

    fun glUniformMatrix3fv(location: Int, count: Int, transpose: Boolean, buffer: FloatBuffer) {
        val oldLimit = buffer.limit()
        buffer.limit(buffer.position() + 3 * 3 * count)
        GL20.glUniformMatrix3fv(location, transpose, buffer)
        buffer.limit(oldLimit)
    }

    fun glUniformMatrix4fv(location: Int, count: Int, transpose: Boolean, buffer: FloatBuffer) {
        val oldLimit = buffer.limit()
        buffer.limit(buffer.position() + 4 * 4 * count)
        GL20.glUniformMatrix4fv(location, transpose, buffer)
        buffer.limit(oldLimit)
    }

    fun glUseProgram(program: Int) {
        GL20.glUseProgram(program)
    }

    fun glValidateProgram(program: Int) {
        GL20.glValidateProgram(program)
    }

    fun glVertexAttrib1f(indx: Int, x: Float) {
        GL20.glVertexAttrib1f(indx, x)
    }

    fun glVertexAttrib1fv(indx: Int, values: FloatBuffer) {
        GL20.glVertexAttrib1f(indx, values.get())
    }

    fun glVertexAttrib2f(indx: Int, x: Float, y: Float) {
        GL20.glVertexAttrib2f(indx, x, y)
    }

    fun glVertexAttrib2fv(indx: Int, values: FloatBuffer) {
        GL20.glVertexAttrib2f(indx, values.get(), values.get())
    }

    fun glVertexAttrib3f(indx: Int, x: Float, y: Float, z: Float) {
        GL20.glVertexAttrib3f(indx, x, y, z)
    }

    fun glVertexAttrib3fv(indx: Int, values: FloatBuffer) {
        GL20.glVertexAttrib3f(indx, values.get(), values.get(), values.get())
    }

    fun glVertexAttrib4f(indx: Int, x: Float, y: Float, z: Float, w: Float) {
        GL20.glVertexAttrib4f(indx, x, y, z, w)
    }

    fun glVertexAttrib4fv(indx: Int, values: FloatBuffer) {
        GL20.glVertexAttrib4f(indx, values.get(), values.get(), values.get(),
                values.get())
    }

    fun glVertexAttribPointer(indx: Int, size: Int, type: Int, normalized: Boolean, stride: Int,
                              ptr: Buffer) {
        if (ptr is FloatBuffer) {
            GL20.glVertexAttribPointer(indx, size, type, normalized, stride, ptr)
        } else if (ptr is ByteBuffer) {
            GL20.glVertexAttribPointer(indx, size, type, normalized, stride, ptr)
        } else if (ptr is ShortBuffer) {
            GL20.glVertexAttribPointer(indx, size, type, normalized, stride, ptr)
        } else if (ptr is IntBuffer) {
            GL20.glVertexAttribPointer(indx, size, type, normalized, stride, ptr)
        } else {
            throw RuntimeException("NYI for " + ptr.javaClass)
        }
    }

    fun glViewport(x: Int, y: Int, width: Int, height: Int) {
        GL11.glViewport(x, y, width, height)
    }

    fun glDrawElements(mode: Int, count: Int, type: Int, indices: Int) {
        GL11.glDrawElements(mode, count, type, indices.toLong())
    }

    fun glVertexAttribPointer(indx: Int, size: Int, type: Int,
                              normalized: Boolean, stride: Int, ptr: Int) {
        GL20.glVertexAttribPointer(indx, size, type, normalized, stride, ptr.toLong())
    }

    val platformGLExtensions: String
        get() = throw UnsupportedOperationException("NYI - not in LWJGL.")

    val swapInterval: Int
        get() = throw UnsupportedOperationException("NYI - not in LWJGL.")

    fun glClearDepth(depth: Double) {
        GL11.glClearDepth(depth)
    }

    fun glCompressedTexImage2D(target: Int, level: Int, internalformat: Int,
                               width: Int, height: Int, border: Int,
                               data_imageSize: Int, data: Int) {
        GL13.glCompressedTexImage2D(target, level, internalformat, width, height, border, data_imageSize, data.toLong())
    }

    fun glCompressedTexImage3D(target: Int, level: Int, internalformat: Int,
                               width: Int, height: Int, depth: Int, border: Int,
                               imageSize: Int, data: Buffer) {
        GL13.glCompressedTexImage3D(target, level, internalformat, width, height, depth, border,
                imageSize, MemoryUtil.memAddress(data as ByteBuffer))
    }

    fun glCompressedTexImage3D(target: Int, level: Int, internalformat: Int,
                               width: Int, height: Int, depth: Int, border: Int,
                               imageSize: Int, data: Int) {
        GL13.glCompressedTexImage3D(
                target, level, internalformat, width, height, depth, border, imageSize, data.toLong())
    }

    fun glCompressedTexSubImage2D(target: Int, level: Int, xoffset: Int, yoffset: Int,
                                  width: Int, height: Int, format: Int, data_imageSize: Int,
                                  data: Int) {
        GL13.glCompressedTexSubImage2D(
                target, level, xoffset, yoffset, width, height, format, data_imageSize, data.toLong())
    }

    fun glCompressedTexSubImage3D(target: Int, level: Int, xoffset: Int, yoffset: Int, zoffset: Int,
                                  width: Int, height: Int, depth: Int,
                                  format: Int, imageSize: Int, data: Buffer) {
        // imageSize is calculated in glCompressedTexSubImage3D.
        GL13.glCompressedTexSubImage3D(target, level, xoffset, yoffset, zoffset,
                width, height, depth, format, data as ByteBuffer)
    }

    fun glCompressedTexSubImage3D(target: Int, level: Int, xoffset: Int, yoffset: Int, zoffset: Int,
                                  width: Int, height: Int, depth: Int,
                                  format: Int, imageSize: Int, data: Int) {
        val dataBuffer = BufferUtils.createByteBuffer(4)
        dataBuffer.putInt(data)
        dataBuffer.rewind()
        // imageSize is calculated in glCompressedTexSubImage3D.
        GL13.glCompressedTexSubImage3D(
                target, level, xoffset, yoffset, zoffset, width, height, depth, format, dataBuffer)
    }

    fun glCopyTexSubImage3D(target: Int, level: Int, xoffset: Int, yoffset: Int, zoffset: Int,
                            x: Int, y: Int, width: Int, height: Int) {
        GL12.glCopyTexSubImage3D(target, level, xoffset, yoffset, zoffset, x, y, width, height)
    }

    fun glDepthRange(zNear: Double, zFar: Double) {
        GL11.glDepthRange(zNear, zFar)
    }

    fun glFramebufferTexture3D(target: Int, attachment: Int, textarget: Int, texture: Int,
                               level: Int, zoffset: Int) {
        EXTFramebufferObject.glFramebufferTexture3DEXT(
                target, attachment, textarget, texture, level, zoffset)
    }

    fun glGetActiveAttrib(program: Int, index: Int, bufsize: Int, length: IntArray, lengthOffset: Int,
                          size: IntArray, sizeOffset: Int, type: IntArray, typeOffset: Int,
                          name: ByteArray, nameOffset: Int) {
        // http://www.khronos.org/opengles/sdk/docs/man/xhtml/glGetActiveAttrib.xml
        // Returns length, size, type, name
        bufs.resizeIntBuffer(2)

        // Return name, length
        val nameString = GL20.glGetActiveAttrib(program, index, BufferUtils.createIntBuffer(bufsize), bufs.intBuffer)
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

    fun glGetActiveAttrib(program: Int, index: Int, bufsize: Int,
                          length: IntBuffer, size: IntBuffer, type: IntBuffer, name: ByteBuffer) {
        val typeTmp = BufferUtils.createIntBuffer(2)
        GL20.glGetActiveAttrib(program, index, BufferUtils.createIntBuffer(256), typeTmp)
        type.put(typeTmp.get(0))
        type.rewind()
    }

    fun glGetActiveUniform(program: Int, index: Int, bufsize: Int,
                           length: IntArray, lengthOffset: Int, size: IntArray, sizeOffset: Int,
                           type: IntArray, typeOffset: Int, name: ByteArray, nameOffset: Int) {
        bufs.resizeIntBuffer(2)

        // Return name, length
        val nameString = GL20.glGetActiveUniform(program, index, BufferUtils.createIntBuffer(256), bufs.intBuffer)
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

    fun glGetActiveUniform(program: Int, index: Int, bufsize: Int, length: IntBuffer,
                           size: IntBuffer, type: IntBuffer, name: ByteBuffer) {
        val typeTmp = BufferUtils.createIntBuffer(2)
        GL20.glGetActiveAttrib(program, index, BufferUtils.createIntBuffer(256), typeTmp)
        type.put(typeTmp.get(0))
        type.rewind()
    }

    fun glGetAttachedShaders(program: Int, maxcount: Int, count: IntBuffer, shaders: IntBuffer) {
        GL20.glGetAttachedShaders(program, count, shaders)
    }

    fun glGetBoolean(pname: Int): Boolean {
        return GL11.glGetBoolean(pname) == GL_TRUE
    }

    fun glGetBooleanv(pname: Int, params: ByteBuffer) {
        GL11.glGetBooleanv(pname, params)
    }

    fun glGetBoundBuffer(arg0: Int): Int {
        throw UnsupportedOperationException("glGetBoundBuffer not supported in GLES 2.0 or LWJGL.")
    }

    fun glGetFloat(pname: Int): Float {
        return GL11.glGetFloat(pname)
    }

    fun glGetInteger(pname: Int): Int {
        return GL11.glGetInteger(pname)
    }

    fun glGetProgramBinary(program: Int, bufSize: Int, length: IntBuffer,
                           binaryFormat: IntBuffer, binary: Buffer) {
        GL41.glGetProgramBinary(program, length, binaryFormat, binary as ByteBuffer)
    }

    fun glGetProgramInfoLog(program: Int, bufsize: Int, length: IntBuffer, infolog: ByteBuffer) {
        val buffer = ByteBuffer.allocateDirect(1024 * 10)
        buffer.order(ByteOrder.nativeOrder())
        val tmp = ByteBuffer.allocateDirect(4)
        tmp.order(ByteOrder.nativeOrder())
        val intBuffer = tmp.asIntBuffer()
        GL20.glGetProgramInfoLog(program, intBuffer, buffer)
    }

    fun glGetShaderInfoLog(shader: Int, bufsize: Int, length: IntBuffer, infolog: ByteBuffer) {
        GL20.glGetShaderInfoLog(shader, length, infolog)
    }

    fun glGetShaderPrecisionFormat(shadertype: Int, precisiontype: Int,
                                   range: IntArray, rangeOffset: Int,
                                   precision: IntArray, precisionOffset: Int) {
        throw UnsupportedOperationException("NYI")
    }

    fun glGetShaderSource(shader: Int, bufsize: Int, length: IntArray, lengthOffset: Int,
                          source: ByteArray, sourceOffset: Int) {
        throw UnsupportedOperationException("NYI")
    }

    fun glGetShaderSource(shader: Int, bufsize: Int, length: IntBuffer, source: ByteBuffer) {
        throw UnsupportedOperationException("NYI")
    }

    fun glIsVBOArrayEnabled(): Boolean {
        throw UnsupportedOperationException("NYI - not in LWJGL.")
    }

    fun glIsVBOElementEnabled(): Boolean {
        throw UnsupportedOperationException("NYI - not in LWJGL.")
    }

    fun glMapBuffer(target: Int, access: Int): ByteBuffer {
        return GL15.glMapBuffer(target, access, null)
    }

    fun glProgramBinary(program: Int, binaryFormat: Int, binary: Buffer, length: Int) {
        // Length is calculated in glProgramBinary.
        GL41.glProgramBinary(program, binaryFormat, binary as ByteBuffer)
    }

    fun glReadPixels(x: Int, y: Int, width: Int, height: Int, format: Int, type: Int,
                     pixelsBufferOffset: Int) {
        GL11.glReadPixels(x, y, width, height, format, type, pixelsBufferOffset.toLong())
    }

    fun glShaderBinary(n: Int, shaders: IntArray, offset: Int, binaryformat: Int,
                       binary: Buffer, length: Int) {
        throw UnsupportedOperationException("NYI")
    }

    fun glShaderSource(shader: Int, count: Int, strings: Array<String>, length: IntArray, lengthOff: Int) {
        for (str in strings)
            GL20.glShaderSource(shader, str)
    }

    fun glShaderSource(shader: Int, count: Int, strings: Array<String>, length: IntBuffer) {
        for (str in strings)
            GL20.glShaderSource(shader, str)
    }

    fun glTexImage2D(target: Int, level: Int, internalformat: Int, width: Int, height: Int,
                     border: Int, format: Int, type: Int, pixels: Int) {
        GL11.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels.toLong())
    }

    fun glTexImage3D(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int,
                     arg5: Int, arg6: Int, arg7: Int, arg8: Int, arg9: Buffer) {
        if (arg9 !is ByteBuffer)
            throw UnsupportedOperationException("Buffer must be a ByteBuffer.")
        GL12.glTexImage3D(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9)
    }

    fun glTexImage3D(arg0: Int, arg1: Int, arg2: Int, arg3: Int, arg4: Int,
                     arg5: Int, arg6: Int, arg7: Int, arg8: Int, arg9: Int) {
        GL12.glTexImage3D(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9.toLong())
    }

    fun glTexSubImage2D(target: Int, level: Int, xoffset: Int, yoffset: Int, width: Int, height: Int, format: Int,
                        type: Int, pixels: Int) {
        GL11.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels.toLong())
    }

    fun glTexSubImage3D(target: Int, level: Int, xoffset: Int, yoffset: Int, zoffset: Int,
                        width: Int, height: Int, depth: Int, format: Int, type: Int,
                        pixels: Buffer) {
        GL12.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format,
                type, pixels as ByteBuffer)
    }

    fun glTexSubImage3D(target: Int, level: Int, xoffset: Int, yoffset: Int, zoffset: Int,
                        width: Int, height: Int, depth: Int, format: Int, type: Int, pixels: Int) {
        val byteBuffer = BufferUtils.createByteBuffer(1)
        byteBuffer.putInt(pixels)
        byteBuffer.rewind()
        GL12.glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format,
                type, byteBuffer)
    }

    fun glUnmapBuffer(target: Int): Boolean {
        return GL15.glUnmapBuffer(target)
    }

    fun hasGLSL(): Boolean {
        throw UnsupportedOperationException("NYI - not in LWJGL.")
    }

    fun isExtensionAvailable(extension: String): Boolean {
        throw UnsupportedOperationException("NYI - not in LWJGL.")
    }

    fun isFunctionAvailable(function: String): Boolean {
        throw UnsupportedOperationException("NYI - not in LWJGL.")
    }
}
