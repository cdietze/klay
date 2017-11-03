package tripleklay.util

import klay.core.printStackTrace
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Tests aspects of the [Logger] class.
 */
class LoggerTest {
    @Test
    fun testOutput() {
        val buf = configWriterImpl()
        val log = Logger("test")
        log.info("This is a test.")
        assertEquals("This is a test." + NEWLINE, buf.toString())
        Logger.setImpl(null)
    }

    @Test
    fun testParams() {
        val buf = configWriterImpl()
        val log = Logger("test")
        log.info("Foo.", "bar", "baz", "one", 1, "null", null)
        assertEquals("Foo. [bar=baz, one=1, null=null]" + NEWLINE, buf.toString())
        Logger.setImpl(null)
    }

    @Test
    fun testDefaultLevel() {
        val buf = configWriterImpl()
        Logger.levels.setDefault(Logger.Level.WARNING)
        val log = Logger("test")
        log.debug("Debug")
        log.info("Info")
        log.warning("Warning")
        assertEquals("Warning" + NEWLINE, buf.toString())
        Logger.levels.setDefault(Logger.Level.DEBUG)
        Logger.setImpl(null)
    }

    @Test
    fun testLevels() {
        val buf = configWriterImpl()
        Logger.levels["test"] = Logger.Level.WARNING
        val log = Logger("test")
        log.debug("Debug")
        log.info("Info")
        log.warning("Warning")
        assertEquals("Warning" + NEWLINE, buf.toString())
        Logger.levels["test"] = null
        Logger.setImpl(null)
    }

    @Test
    fun testException() {
        val buf = configWriterImpl()
        val log = Logger("test")
        log.info("Foo.", "bar", "baz", Exception())
        val lines = buf.toString().split(NEWLINE.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        assertEquals("Foo. [bar=baz]", lines[0])
        assertEquals("java.lang.Exception", lines[1])
        Logger.setImpl(null)
    }

    companion object {

        protected fun configWriterImpl(): StringBuilder {
            val buf = StringBuilder()
            Logger.setImpl(object : Logger.Impl {
                override fun log(level: Logger.Level, ident: String, message: String, t: Throwable?) {
                    buf.append(message)
                    t?.printStackTrace(buf)
                }
            })
            return buf
        }

        protected val NEWLINE = "\n"
    }
}
