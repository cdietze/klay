package klay.core

import klay.core.react.Signal

abstract class Platform {
    abstract val graphics: Graphics

    val frameSignal: Signal<Platform> = Signal.create()
}
