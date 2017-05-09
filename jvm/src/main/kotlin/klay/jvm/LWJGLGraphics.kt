package klay.jvm

import klay.core.Scale
import klay.core.Texture
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.awt.image.DataBufferInt
import java.nio.ByteBuffer

abstract class LWJGLGraphics protected constructor(open val jplat: JavaPlatform)// real scale factor set later
    : JavaGraphics(jplat, JvmGL20(), Scale.ONE) {

    override fun upload(img: BufferedImage, tex: Texture) {
        // Convert the bitmap into a format for quick uploading (NOOPs if already optimized)
        val bitmap = convertImage(img)

        val dbuf = bitmap.getRaster().getDataBuffer()
        val bbuf: ByteBuffer
        val format: Int
        val type: Int

        if (bitmap.getType() == BufferedImage.TYPE_INT_ARGB_PRE) {
            val ibuf = dbuf as DataBufferInt
            val iSize = ibuf.size * 4
            bbuf = checkGetImageBuffer(iSize)
            bbuf.asIntBuffer().put(ibuf.data)
            bbuf.position(bbuf.position() + iSize)
            bbuf.flip()
            format = GL12.GL_BGRA
            type = GL12.GL_UNSIGNED_INT_8_8_8_8_REV

        } else if (bitmap.getType() == BufferedImage.TYPE_4BYTE_ABGR) {
            val dbbuf = dbuf as DataBufferByte
            bbuf = checkGetImageBuffer(dbbuf.size)
            bbuf.put(dbbuf.data)
            bbuf.flip()
            format = GL11.GL_RGBA
            type = GL12.GL_UNSIGNED_INT_8_8_8_8

        } else {
            // Something went awry and convertImage thought this image was in a good form already,
            // except we don't know how to deal with it
            throw RuntimeException("Image type wasn't converted to usable: " + bitmap.getType())
        }

        gl.glBindTexture(GL11.GL_TEXTURE_2D, tex.id)
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, bitmap.getWidth(), bitmap.getHeight(),
                0, format, type, bbuf)
        gl.checkError("updateTexture")
    }
}
