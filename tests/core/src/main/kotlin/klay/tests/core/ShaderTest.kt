package klay.tests.core

import klay.core.*
import klay.scene.ImageLayer
import klay.scene.Layer
import pythagoras.f.MathUtil
import react.Connection
import react.Slot

/**
 * Tests custom shader support.
 */
class ShaderTest(game: TestsGame) : Test(game, "Shader", "Tests custom shader support.") {

    override fun init() {
        game.assets.getImage("images/orange.png").state.onSuccess(object : Slot<Image> {
            override fun invoke(orange: Image) {
                val otex = orange.texture()

                // add the normal orange
                val dx = orange.width + 25f
                game.rootLayer.addAt(ImageLayer(otex), 25f, 25f)

                // add a sepia toned orange
                val olayer = ImageLayer(otex)
                olayer.setBatch(createSepiaBatch())
                game.rootLayer.addAt(olayer, 25 + dx, 25f)

                val rotBatch = createRotBatch()
                rotBatch.eyeX = 0f
                rotBatch.eyeY = orange.height / 2f

                // add an image that is rotated around the (3D) y axis
                val canvas = game.graphics.createCanvas(orange.width, orange.height)
                canvas.setFillColor(0xFF99CCFF.toInt()).fillRect(0f, 0f, canvas.width, canvas.height)
                canvas.draw(orange, 0f, 0f)
                val rotlayer = ImageLayer(canvas.toTexture())
                rotlayer.setBatch(rotBatch)
                game.rootLayer.addAt(rotlayer, 25 + 2 * dx + orange.width, 25f)

                // add an immediate layer that draws a quad and an image (which should rotate)
                val irotlayer = object : Layer() {
                    override fun paintImpl(surf: Surface) {
                        surf.setFillColor(0xFFCC99FF.toInt()).fillRect(0f, 0f, otex.displayWidth, otex.displayHeight)
                        surf.draw(otex, 0f, 0f)
                    }
                }
                irotlayer.setBatch(rotBatch)
                game.rootLayer.addAt(irotlayer, 25 + 3 * dx + orange.width, 25f)

                conns.add<Connection>(game.paint.connect { clock: Clock ->
                    rotBatch.elapsed = clock.tick / 1000f
                })
            }
        })
    }

    protected fun createRotBatch(): RotYBatch {
        return RotYBatch(game.graphics.gl)
    }

    // a batch with a shader that rotates things around the (3D) y axis
    protected class RotYBatch(gl: GL20) : TriangleBatch(gl, RotYBatch.Source()) {
        class Source : TriangleBatch.Source() {
            override fun vertex(): String {
                return VERT_UNIFS +
                        "uniform float u_Angle;\n" +
                        "uniform vec2 u_Eye;\n" +
                        VERT_ATTRS +
                        PER_VERT_ATTRS +
                        VERT_VARS +
                        "void main(void) {\n" +
                        VERT_ROTSETPOS +
                        VERT_SETTEX +
                        VERT_SETCOLOR +
                        "}"
            }

            companion object {

                protected val VERT_ROTSETPOS =
                        // Rotate the vertex per our 3D rotation
                        "  float cosa = cos(u_Angle);\n" +
                                "  float sina = sin(u_Angle);\n" +
                                "  mat4 rotmat = mat4(\n" +
                                "    cosa, 0, sina, 0,\n" +
                                "    0,    1, 0,    0,\n" +
                                "   -sina, 0, cosa, 0,\n" +
                                "    0,    0, 0,    1);\n" +
                                "  vec4 pos = rotmat * vec4(a_Position - u_Eye, 0, 1);\n" +

                                // Perspective project the vertex back into the plane
                                "  mat4 persp = mat4(\n" +
                                "    1, 0, 0, 0,\n" +
                                "    0, 1, 0, 0,\n" +
                                "    0, 0, 1, -1.0/200.0,\n" +
                                "    0, 0, 0, 1);\n" +
                                "  pos = persp * pos;\n" +
                                "  pos /= pos.w;\n" +
                                "  pos += vec4(u_Eye, 0, 0);\n;" +

                                // Transform the vertex per the normal screen transform
                                "  mat4 transform = mat4(\n" +
                                "    a_Matrix[0],      a_Matrix[1],      0, 0,\n" +
                                "    a_Matrix[2],      a_Matrix[3],      0, 0,\n" +
                                "    0,                0,                1, 0,\n" +
                                "    a_Translation[0], a_Translation[1], 0, 1);\n" +
                                "  pos = transform * pos;\n" +
                                "  pos.xy /= u_HScreenSize.xy;\n" +
                                "  pos.z  /= u_HScreenSize.y;\n" +
                                "  pos.xy -= 1.0;\n" +
                                "  pos.y *= u_Flip;\n" +
                                "  gl_Position = pos;\n"
            }
        }

        var elapsed: Float = 0.toFloat()
        var eyeX: Float = 0.toFloat()
        var eyeY: Float = 0.toFloat()

        val uAngle: Int
        val uEye: Int

        init {
            uAngle = program.getUniformLocation("u_Angle")
            uEye = program.getUniformLocation("u_Eye")
        }

        override fun begin(fbufWidth: Float, fbufHeight: Float, flip: Boolean) {
            super.begin(fbufWidth, fbufHeight, flip)
            gl.glUniform1f(uAngle, elapsed * MathUtil.PI)
            gl.glUniform2f(uEye, eyeX, eyeY)
        }
    }
}
