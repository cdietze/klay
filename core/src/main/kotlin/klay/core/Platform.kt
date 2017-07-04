package klay.core

import react.Signal

/**
 * Provides access to all Klay cross-platform services.
 */
abstract class Platform {

    /** Defines the lifecycle events.  */
    enum class Lifecycle {
        PAUSE, RESUME, EXIT
    }

    /** A signal emitted with lifecycle events.  */
    val lifecycle: Signal<Lifecycle> = Signal()

    /** This signal will be emitted at the start of every frame after the platform is started. Games
     * should connect to it to drive their main loop.  */
    val frame: Signal<Platform> = Signal()

    /** Used by [.reportError].  */
    class Error(val message: String, val cause: Throwable)

    /** Any errors reported via [.reportError] will be emitted to this signal in addition to
     * being logged. Games can connect to this signal if they wish to capture and record platform
     * errors.  */
    val errors: Signal<Error> = Signal()

    /** Enumerates the supported platform types.  */
    enum class Type {
        JAVA, HTML, ANDROID, IOS, STUB
    }

    /** Returns the platform [Platform.Type].  */
    abstract fun type(): Platform.Type

    /** Returns the current time, as a double value in millis since January 1, 1970, 00:00:00 GMT.
     * This is equivalent to the standard JRE `new Date().getTime();`, but is terser and
     * avoids the use of `long`, which is best avoided when translating to JavaScript.  */
    abstract fun time(): Double

    /** Returns the number of milliseconds that have elapsed since the game started.  */
    abstract fun tick(): Int

    /** Opens the given URL in the default browser.  */
    abstract fun openURL(url: String)

    /** Returns the [Assets] service.  */
    abstract val assets: Assets

    /** Returns the [Audio] service.  */
    abstract val audio: Audio

    /** Returns the [Exec] service.  */
    abstract val exec: Exec

    /** Returns the [Graphics] service.  */
    abstract val graphics: Graphics

    /** Returns the [Input] service.  */
    abstract val input: Input

    /** Returns the [Json] service.  */
    abstract val json: Json

    /** Returns the [Log] service.  */
    abstract val log: Log

    /** Returns the [Net] service.  */
    abstract val net: Net

    /** Returns the [Storage] storage service.  */
    abstract val storage: Storage

    /**
     * Called when a backend (or other framework code) encounters an exception that it can recover
     * from, but which it would like to report in some orderly fashion. *NOTE:* this method
     * may be called from threads other than the main Klay thread.
     */
    fun reportError(message: String, cause: Throwable) {
        errors.emit(Error(message, cause))
        log.warn(message, cause)
    }

    /**
     * Dispatches `event` on `signal` and catches any error that propagates out of the
     * event dispatch, reporting it via [.reportError].
     */
    fun <E> dispatchEvent(signal: Signal<E>, event: E) {
        try {
            signal.emit(event)
        } catch (cause: Throwable) {
            reportError("Event dispatch failure", cause)
        }

    }

    protected fun emitFrame() {
        try {
            frame.emit(this)
        } catch (e: Throwable) {
            reportError("Frame tick exception", e)
        }

    }
}
