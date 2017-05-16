package klay.core

import react.RPromise

/**
 * Handles execution of units of code, both on background threads ([.invokeAsync]) and on the
 * main Klay game thread ([.invokeLater]).
 */
abstract class Exec {

    /** A default exec implementation which processes [.invokeLater] via the frame tick.  */
    open class Default(protected val plat: Platform) : Exec() {
        private val pending = ArrayList<() -> Unit>()
        private val running = ArrayList<() -> Unit>()

        init {
            plat.frame.connect { dispatch() }.atPrio(Short.MAX_VALUE.toInt())
        }

        override val isAsyncSupported: Boolean
            get() = false

        override fun invokeAsync(action: () -> Unit) {
            throw UnsupportedOperationException()
        }

        @Synchronized override fun invokeLater(action: () -> Unit) {
            pending.add(action)
        }

        private fun dispatch() {
            synchronized(this) {
                running.addAll(pending)
                pending.clear()
            }

            var ii = 0
            val ll = running.size
            while (ii < ll) {
                val action = running[ii]
                try {
                    action()
                } catch (e: Throwable) {
                    plat.reportError("invokeLater Runnable failed: " + action, e)
                }

                ii++
            }
            running.clear()
        }
    }

    /**
     * Invokes `action` on the next [Platform.frame] signal. The default implementation
     * listens to the frame signal at a very high priority so that invoke later actions will run
     * before the game's normal callbacks.
     */
    abstract fun invokeLater(action: () -> Unit)

    /**
     * Creates a promise which defers notification of success or failure to the game thread,
     * regardless of what thread on which it is completed. Note that even if it is completed on the
     * game thread, it will still defer completion until the next frame.
     */
    fun <T> deferredPromise(): RPromise<T> {
        return object : RPromise<T>() {
            override fun succeed(value: T) {
                invokeLater({ superSucceed(value) })
            }

            override fun fail(cause: Throwable) {
                invokeLater({ superFail(cause) })
            }

            private fun superSucceed(value: T) {
                super.succeed(value)
            }

            private fun superFail(cause: Throwable) {
                super.fail(cause)
            }
        }
    }

    /**
     * Returns whether this platform supports async (background) operations.
     * HTML doesn't, most other platforms do.
     */
    abstract val isAsyncSupported: Boolean

    /**
     * Invokes the supplied action on a separate thread.
     * @throws UnsupportedOperationException if the platform does not support async operations.
     */
    open fun invokeAsync(action: () -> Unit) {
        throw UnsupportedOperationException()
    }
}
