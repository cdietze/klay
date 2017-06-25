package tripleklay.ui

import pythagoras.f.Dimension
import pythagoras.f.IDimension

/**
 * An invisible widget that simply requests a fixed amount of space.
 */
class Shim(size: IDimension) : SizableWidget<Shim>(size) {
    constructor(width: Float, height: Float) : this(Dimension(width, height))

    override val styleClass: Class<*>
        get() = Shim::class.java
}
