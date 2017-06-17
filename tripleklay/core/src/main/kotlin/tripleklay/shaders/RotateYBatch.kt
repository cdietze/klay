package tripleklay.shaders

import klay.core.GL20
import klay.core.TriangleBatch

/**
 * Rotates the view through a 3D transform around the Y axis. Can be used to simulate a (rigid)
 * page turn.
 */
class RotateYBatch(gl: GL20, eyeX: Float, eyeY: Float, zScale: Float) : TriangleBatch(gl, object : Source() {
    override fun vertex(): String {
        return RotateYBatch.vertex(eyeX, eyeY, zScale)
    }
}) {
    /** The angle of rotation.  */
    var angle: Float = 0.toFloat()

    private val uAngle: Int = program.getUniformLocation("u_Angle")

    override fun begin(fbufWidth: Float, fbufHeight: Float, flip: Boolean) {
        super.begin(fbufWidth, fbufHeight, flip)
        program.activate()
        gl.glUniform1f(uAngle, angle)
    }

    companion object {

        private fun vertex(eyeX: Float, eyeY: Float, zScale: Float): String {
            return TriangleBatch.Source.VERT_UNIFS +
                    "uniform float u_Angle;\n" +
                    TriangleBatch.Source.VERT_ATTRS +
                    TriangleBatch.Source.PER_VERT_ATTRS +
                    TriangleBatch.Source.VERT_VARS +

                    "void main(void) {\n" +
                    // Transform the vertex per the normal screen transform
                    "  mat4 transform = mat4(\n" +
                    "    a_Matrix[0],      a_Matrix[1],      0, 0,\n" +
                    "    a_Matrix[2],      a_Matrix[3],      0, 0,\n" +
                    "    0,                0,                1, 0,\n" +
                    "    a_Translation[0], a_Translation[1], 0, 1);\n" +
                    "  vec4 pos = transform * vec4(a_Position, 0, 1);\n" +

                    // Rotate the vertex per our 3D rotation
                    "  float cosa = cos(u_Angle);\n" +
                    "  float sina = sin(u_Angle);\n" +
                    "  mat4 rotmat = mat4(\n" +
                    "    cosa, 0, sina, 0,\n" +
                    "    0,    1, 0,    0,\n" +
                    "   -sina, 0, cosa, 0,\n" +
                    "    0,    0, 0,    1);\n" +
                    "  pos = rotmat * vec4(pos.x - " + ShaderUtil.format(eyeX) + " * 2.*u_HScreenSize.x,\n" +
                    "                      pos.y - " + ShaderUtil.format(eyeY) + " * 2.*u_HScreenSize.y,\n" +
                    "                      0, 1);\n" +

                    // Perspective project the vertex back into the plane
                    "  mat4 persp = mat4(\n" +
                    "    1, 0, 0, 0,\n" +
                    "    0, 1, 0, 0,\n" +
                    "    0, 0, 1, -1.0/2000.0,\n" +
                    "    0, 0, 0, 1);\n" +
                    "  pos = persp * pos;\n" +
                    "  pos += vec4(" + ShaderUtil.format(eyeX) + " * 2.*u_HScreenSize.x,\n" +
                    "              " + ShaderUtil.format(eyeY) + " * 2.*u_HScreenSize.y, 0, 0);\n" +

                    // Finally convert the coordinates into OpenGL space
                    "  pos.xy /= u_HScreenSize.xy;\n" +
                    "  pos.z  /= (u_HScreenSize.x * " + ShaderUtil.format(zScale) + ");\n" +
                    "  pos.xy -= 1.0;\n" +
                    // z may already be rotated into negative space so we don't shift it
                    "  pos.y  *= u_Flip;\n" +
                    "  gl_Position = pos;\n" +

                    TriangleBatch.Source.VERT_SETTEX +
                    TriangleBatch.Source.VERT_SETCOLOR +
                    "}"
        }
    }
}
