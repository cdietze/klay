package klay.core

import react.Signal

abstract class Platform {
    abstract val graphics: Graphics

    val frameSignal: Signal<Platform> = Signal.create()
}
