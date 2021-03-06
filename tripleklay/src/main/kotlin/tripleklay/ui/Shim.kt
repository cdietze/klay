package tripleklay.ui

import euklid.f.Dimension
import euklid.f.IDimension
import kotlin.reflect.KClass

/**
 * An invisible widget that simply requests a fixed amount of space.
 */
class Shim(size: IDimension) : SizableWidget<Shim>(size) {
    constructor(width: Float, height: Float) : this(Dimension(width, height))

    override val styleClass: KClass<*>
        get() = Shim::class
}
