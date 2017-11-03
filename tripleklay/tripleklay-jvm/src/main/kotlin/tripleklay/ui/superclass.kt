package tripleklay.ui

import kotlin.reflect.KClass

actual val KClass<*>.superclass: KClass<*>?
    get() = this::class.superclass
