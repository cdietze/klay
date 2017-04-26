package klay.jvm

import klay.core.GL20
import klay.core.Graphics

class JvmGraphics : Graphics {
    override val gl: GL20
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun sayHello() {
        println("Hi from JvmGraphics")
    }
}
