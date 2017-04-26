package klay.jvm

import klay.core.GL20
import klay.core.Graphics
import org.lwjgl.opengl.GL11

class JvmGraphics : Graphics {
    override val gl = object : GL20() {
        override fun glClearColor(red: Float, green: Float, blue: Float, alpha: Float) {
            GL11.glClearColor(red, green, blue, alpha)
        }
    }
}
