package tripleklay.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Tests aspects of the [Styles] class.
 */
class StylesTest {
    // static {
    //     JavaPlatform.Config config = new JavaPlatform.Config();
    //     config.headless = true;
    //     JavaPlatform.register(config);
    // }

    @Test fun testEmpty() {
        val s = Styles.none()
        checkIsNull(s, Style.COLOR)
    }

    @Test fun testNonReceiverMod() {
        val s = Styles.none()
        checkIsNull(s, Style.COLOR)
        val s1 = s.add(Style.COLOR.`is`(0xFFAABBCC.toInt()))
        checkIsNull(s, Style.COLOR)
        checkEquals(0xFFAABBCC.toInt(), s1, Style.COLOR)
    }

    @Test fun testAddsGets() {
        val s = Styles.make(Style.COLOR.`is`(0xFFAABBCC.toInt()),
                Style.SHADOW.`is`(0xFF333333.toInt()),
                Style.HIGHLIGHT.`is`(0xFFAAAAAA.toInt()))
        checkEquals(0xFFAABBCC.toInt(), s, Style.COLOR)
        checkEquals(0xFF333333.toInt(), s, Style.SHADOW)
        checkEquals(0xFFAAAAAA.toInt(), s, Style.HIGHLIGHT)
    }

    @Test fun testOverwrite() {
        val s = Styles.make(Style.COLOR.`is`(0xFFAABBCC.toInt()),
                Style.SHADOW.`is`(0xFF333333.toInt()))
        checkEquals(0xFFAABBCC.toInt(), s, Style.COLOR)
        checkEquals(0xFF333333.toInt(), s, Style.SHADOW)

        var ns = s.add(Style.COLOR.`is`(0xFFBBAACC.toInt()))
        checkEquals(0xFFBBAACC.toInt(), ns, Style.COLOR)

        ns = s.add(Style.COLOR.`is`(0xFFBBAACC.toInt()), Style.HIGHLIGHT.`is`(0xFFAAAAAA.toInt()))
        checkEquals(0xFFBBAACC.toInt(), ns, Style.COLOR)
        checkEquals(0xFFAAAAAA.toInt(), ns, Style.HIGHLIGHT)

        ns = s.add(Style.HIGHLIGHT.`is`(0xFFAAAAAA.toInt()), Style.COLOR.`is`(0xFFBBAACC.toInt()))
        checkEquals(0xFFBBAACC.toInt(), ns, Style.COLOR)
        checkEquals(0xFFAAAAAA.toInt(), ns, Style.HIGHLIGHT)
    }

    @Test fun testClear() {
        var s = Styles.make(Style.COLOR.`is`(0xFFAABBCC.toInt()),
                Style.SHADOW.`is`(0xFF333333.toInt()))
        checkEquals(0xFFAABBCC.toInt(), s, Style.COLOR)
        checkEquals(0xFF333333.toInt(), s, Style.SHADOW)

        s = s.clear(Style.Mode.DEFAULT, Style.COLOR)
        checkEquals(null, s, Style.COLOR)
    }

    companion object {

        protected fun <V> checkIsNull(s: Styles, style: Style<V>) {
            assertNull(s[style, Label()])
        }

        protected fun <V> checkEquals(value: V?, s: Styles, style: Style<V>) {
            assertEquals(value, s[style, Label()])
        }
    }
}
