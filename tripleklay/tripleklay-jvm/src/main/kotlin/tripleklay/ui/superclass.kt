package tripleklay.ui

import kotlin.reflect.KClass
import kotlin.reflect.full.superclasses

actual val KClass<*>.superclass: KClass<*>?
    get() = this.superclasses.firstOrNull()
