package klay.core

/**
 * Simple logging interface.
 */
abstract class Log {

    private var collector: Collector? = null
    private var minLevel = Level.DEBUG

    /** Tags a log message with a level.  */
    enum class Level {
        DEBUG, INFO, WARN, ERROR
    }

    /** Allows for collection of log messages (in addition to standard logging).
     * See [.setCollector].  */
    interface Collector {
        /**
         * Called when a message is logged.

         * @param level the level at which the message was logged.
         * *
         * @param msg the message that was logged.
         * *
         * @param e the exception logged with the message, or null.
         */
        fun logged(level: Level, msg: String, e: Throwable?)
    }

    /**
     * Configures a log message collector. This allows games to intercept (and record and submit with
     * bug reports, for example) all messages logged via the Klay logging system. This will include
     * errors logged internally by Klay code.
     */
    fun setCollector(collector: Collector) {
        this.collector = collector
    }

    /**
     * Configures the minimum log level that will be logged. Messages at a level lower than
     * `level` will be suppressed. Note that all messages are still passed to any registered
     * [Collector], but suppressed messages are not sent to the platform logging system.
     */
    fun setMinLevel(level: Level) {
        minLevel = level
    }

    /** Logs `msg` and `e` at the debug level.  */
    fun debug(msg: String, e: Throwable? = null) {
        log(Level.DEBUG, msg, e)
    }

    /** Logs `msg` and `e` at the info level.  */
    fun info(msg: String, e: Throwable? = null) {
        log(Level.INFO, msg, e)
    }

    /** Logs `msg` and `e` at the warn level.  */
    fun warn(msg: String, e: Throwable? = null) {
        log(Level.WARN, msg, e)
    }

    /** Logs `msg` and `e` at the error level.  */
    fun error(msg: String, e: Throwable? = null) {
        log(Level.ERROR, msg, e)
    }

    protected fun log(level: Level, msg: String, e: Throwable?) {
        if (collector != null) collector!!.logged(level, msg, e)
        if (level.ordinal >= minLevel.ordinal) logImpl(level, msg, e)
    }

    protected abstract fun logImpl(level: Level, msg: String, e: Throwable?)
}