package klay.jvm

import klay.core.Graphics

class JvmGraphics : Graphics {
    override val gl = JvmGL20()
}
