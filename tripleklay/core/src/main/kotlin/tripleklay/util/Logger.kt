package tripleklay.util

import klay.core.Platform
import java.util.*

/**
 * Provides logging services that are routed to the appropriate logging destination on the client
 * or server. A useful usage pattern is for a library or game to define a shared `Log` class
 * like so:

 * <pre>`import tripleklay.util.Logger;
 * public class Log {
 * public static final Logger log = new Logger("libident");
 * }
`</pre> *

 * and then import `Log.log` statically into classes so that they can invoke `log.info`, etc.
 */
class Logger
/**
 * Creates a logger with the specified ident string.
 */
(protected val _ident: String) {
    /** Tags a log message with a particular severity level.  */
    enum class Level {
        DEBUG, INFO, WARNING, ERROR, OFF
    }

    /**
     * Wires the logging front-end to the logging back-end. See [.setImpl].
     */
    interface Impl {
        /** Logs the supplied message at the supplied level.  */
        fun log(level: Level, ident: String, message: String, t: Throwable?)
    }

    /** Manages the target log levels for a given ident.  */
    class Levels {
        /** Configures the default log level. Messages with severity lower than this level will not
         * be logged unless a specific level is set for their identifier.  */
        fun setDefault(level: Level): Levels {
            _defaultLevel = level
            return this
        }

        /** Configures the log level for messages with the supplied identifier. Messages with the
         * supplied identifier with severity lower than this level will not be logged regardless of
         * the default log level. Pass null to clear any level cutoff for `ident`.  */
        operator fun set(ident: String, level: Level?): Levels {
            if (level == null) _levels.remove(ident) else _levels.put(ident, level)
            return this
        }

        /** Configures the log level for messages from the supplied logger. Messages from the
         * supplied logger with severity lower than this level will not be logged regardless of
         * the default log level. Pass null to clear any level cutoff for `logger`.  */
        operator fun set(logger: Logger, level: Level): Levels {
            _levels.put(logger._ident, level)
            return this
        }

        /** Returns the current default log level.  */
        fun defaultLevel(): Level {
            return _defaultLevel
        }

        /** Returns the current log level for the specified identifier, or null if no level is
         * configured for that identifier.  */
        fun level(ident: String): Level? {
            return _levels[ident]
        }

        /** Returns true if a message with the specified level and ident should be logged.  */
        fun shouldLog(level: Level, ident: String): Boolean {
            val ilevel = _levels[ident]
            if (ilevel != null) return level.ordinal >= ilevel.ordinal
            return level.ordinal >= _defaultLevel.ordinal
        }

        protected var _defaultLevel = Level.DEBUG
        protected var _levels: MutableMap<String, Level> = HashMap()
    }

    /**
     * A logging back-end that writes to PlayN.
     */
    class PlayNImpl(private val plat: Platform) : Impl {
        override fun log(level: Level, ident: String, message: String, t: Throwable?) {
            val msg = ident + ": " + message
            when (level) {
                Logger.Level.DEBUG -> if (t != null)
                    plat.log.debug(msg, t)
                else
                    plat.log.debug(msg)
                Logger.Level.INFO -> if (t != null)
                    plat.log.info(msg, t)
                else
                    plat.log.info(msg)
                Logger.Level.WARNING -> if (t != null)
                    plat.log.warn(msg, t)
                else
                    plat.log.warn(msg)
                Logger.Level.ERROR -> if (t != null)
                    plat.log.error(msg, t)
                else
                    plat.log.error(msg)
                Logger.Level.OFF -> {
                }
            }
        }
    }

    /**
     * Tests if this logger will output messages of the given level.
     */
    fun shouldLog(level: Level): Boolean {
        return levels.shouldLog(level, _ident)
    }

    /**
     * Logs a debug message.

     * @param message the text of the message.
     * *
     * @param args a series of zero or more key/value pairs followed by an optional [ ] cause.
     */
    fun debug(message: String, vararg args: Any?) {
        if (levels.shouldLog(Level.DEBUG, _ident)) {
            log(Level.DEBUG, _ident, message, *args)
        }
    }

    /**
     * Logs an info message.

     * @param message the text of the message.
     * *
     * @param args a series of zero or more key/value pairs followed by an optional [ ] cause.
     */
    fun info(message: String, vararg args: Any?) {
        if (levels.shouldLog(Level.INFO, _ident)) {
            log(Level.INFO, _ident, message, *args)
        }
    }

    /**
     * Logs a warning message.

     * @param message the text of the message.
     * *
     * @param args a series of zero or more key/value pairs followed by an optional [ ] cause.
     */
    fun warning(message: String, vararg args: Any?) {
        if (levels.shouldLog(Level.WARNING, _ident)) {
            log(Level.WARNING, _ident, message, *args)
        }
    }

    /**
     * Logs an error message.

     * @param message the text of the message.
     * *
     * @param args a series of zero or more key/value pairs followed by an optional [ ] cause.
     */
    fun error(message: String, vararg args: Any?) {
        if (levels.shouldLog(Level.ERROR, _ident)) {
            log(Level.ERROR, _ident, message, *args)
        }
    }

    protected fun log(level: Level, ident: String, message: String, vararg args: Any?) {
        val sb = StringBuilder().append(message)
        if (args.size > 1) {
            sb.append(" [")
            format(sb, *args)
            sb.append("]")
        }
        val error = if (args.size % 2 == 1) args[args.size - 1] else null
        _impl.log(level, ident, sb.toString(), error as Throwable?)
    }

    companion object {

        /** Log levels can be configured via this instance.  */
        var levels = Levels()

        /**
         * Configures the logging back-end. This should be called before any code that makes use of the
         * logging services. The default back-end logs to `stderr`, which is useful when running
         * in unit tests. `null` may be supplied to restore the default (stderr) back-end.
         */
        fun setImpl(impl: Impl?) {
            _impl = impl ?: DEFAULT
        }

        /**
         * Formats and returns the supplied message and key/value arguments as
         * `message [key=value, key=value, ...]`.
         */
        fun format(message: Any, vararg args: Any?): String {
            return format(StringBuilder().append(message).append(" ["), *args).append("]").toString()
        }

        /**
         * Formats the supplied key/value arguments into the supplied string builder as `key=value, key=value, ...`.
         * @return the supplied string builder.
         */
        fun format(into: StringBuilder, vararg args: Any?): StringBuilder {
            var ii = 0
            val ll = args.size / 2
            while (ii < ll) {
                if (ii > 0) {
                    into.append(", ")
                }
                into.append(args[2 * ii]).append("=").append(args[2 * ii + 1])
                ii++
            }
            return into
        }

        protected val DEFAULT: Impl = object : Impl {
            override fun log(level: Level, ident: String, message: String, t: Throwable?) {
                println(ident + ": " + message)
                t?.printStackTrace(System.out)
            }
        }

        protected var _impl = DEFAULT
    }
}
