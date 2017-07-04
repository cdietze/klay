package klay.core

import klay.core.json.JsonImpl
import java.util.*

/**
 * A stub implementation of [Platform] that provides implementations of those services that
 * can be usefully implemented for unit tests, and throws [UnsupportedOperationException] for
 * the rest. This can usefully be extended in tests to provide test implementations for just the
 * aspects of the platform that are needed to support the code under test.

 *
 *  The services that are implemented are:
 *  *  [.type] - reports [Platform.Type.STUB]
 *  *  [.time] - returns current time
 *  *  [.invokeLater] - invokes the supplied action immediately on the calling thread
 *  *  [.input] - allows listener registration, never generates events
 *  *  [.log] - writes logs to `stderr`
 *  *  [.json] - provides full JSON parsing and formatting
 *  *  [.storage] - maintains an in-memory storage map
 *
 */
class StubPlatform : Platform() {

    override val storage = object : Storage {
        private val _data = HashMap<String, String>()

        @Throws(RuntimeException::class)
        override fun setItem(key: String, data: String) {
            _data.put(key, data)
        }

        override fun removeItem(key: String) {
            _data.remove(key)
        }

        override fun getItem(key: String): String? {
            return _data[key]
        }

        override fun startBatch(): Storage.Batch {
            return BatchImpl(this)
        }

        override fun keys(): Iterable<String> {
            return _data.keys
        }

        override val isPersisted: Boolean
            get() = true
    }

    override val input = Input(this)
    override val log = object : Log() {
        override fun logImpl(level: Log.Level, msg: String, e: Throwable?) {
            val prefix: String
            when (level) {
                Log.Level.DEBUG -> prefix = "D: "
                Log.Level.INFO -> prefix = ""
                Log.Level.WARN -> prefix = "W: "
                Log.Level.ERROR -> prefix = "E: "
            }
            System.err.println(prefix + msg)
            e?.printStackTrace(System.err)
        }
    }
    override val exec = object : Exec.Default(this) {
        override fun invokeLater(action: () -> Unit) {
            action()
        } // now is later!
    }

    override val json: Json = JsonImpl()

    private val start = System.currentTimeMillis()

    override fun type(): Platform.Type {
        return Platform.Type.STUB
    }

    override fun time(): Double {
        return System.currentTimeMillis().toDouble()
    }

    override fun tick(): Int {
        return (System.currentTimeMillis() - start).toInt()
    }

    override fun openURL(url: String) {
        throw UnsupportedOperationException()
    }

    override val assets: Assets
        get() = throw UnsupportedOperationException()
    override val audio: Audio
        get() = throw UnsupportedOperationException()
    override val graphics: Graphics
        get() = throw UnsupportedOperationException()
    override val net: Net
        get() = throw UnsupportedOperationException()
}
