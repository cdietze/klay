package klay.core.json

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InternalJsonWriterTest {
    /**
     * Test emitting simple values.
     */
    @Test
    fun testSimpleValues() {
        assertEquals("true", JsonStringWriter().value(true).write())
        assertEquals("null", JsonStringWriter().nul().write())
        assertEquals("1.0", JsonStringWriter().value(1.0).write())
        assertEquals("1.0", JsonStringWriter().value(1.0f).write())
        assertEquals("1", JsonStringWriter().value(1).write())
        assertEquals("\"abc\"", JsonStringWriter().value("abc").write())
    }

    /**
     * Test various ways of writing null, as well as various situations.
     */
    @Test
    fun testNull() {
        assertEquals("null", JsonStringWriter().value(null as String?).write())
        assertEquals("null", JsonStringWriter().value(null as Number?).write())
        assertEquals("null", JsonStringWriter().nul().write())
        assertEquals("[null]", JsonStringWriter().array().value(null as String?).end().write())
        assertEquals("[null]", JsonStringWriter().array().value(null as Number?).end().write())
        assertEquals("[null]", JsonStringWriter().array().nul().end().write())
        assertEquals("{\"a\":null}", JsonStringWriter().`object`().value("a", null as String?).end().write())
        assertEquals("{\"a\":null}", JsonStringWriter().`object`().value("a", null as Number?).end().write())
        assertEquals("{\"a\":null}", JsonStringWriter().`object`().nul("a").end().write())
    }

    /**
     * Test escaping of chars < 256.
     */
    @Test
    fun testStringControlCharacters() {
        val chars = StringBuilder()
        for (i in 0..159)
            chars.append(i.toChar())

        assertEquals(
                "\"\\u0000\\u0001\\u0002\\u0003\\u0004\\u0005\\u0006\\u0007\\b\\t\\n\\u000b\\f\\r\\u000e\\u000f"
                        + "\\u0010\\u0011\\u0012\\u0013\\u0014\\u0015\\u0016\\u0017\\u0018\\u0019\\u001a\\u001b\\u001c"
                        + "\\u001d\\u001e\\u001f !\\\"#$%&'()*+,-./0123456789:;<=>?@"
                        + "ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\\u0080\\u0081\\u0082"
                        + "\\u0083\\u0084\\u0085\\u0086\\u0087\\u0088\\u0089\\u008a\\u008b\\u008c\\u008d\\u008e\\u008f"
                        + "\\u0090\\u0091\\u0092\\u0093\\u0094\\u0095\\u0096\\u0097\\u0098\\u0099\\u009a\\u009b\\u009c"
                        + "\\u009d\\u009e\\u009f\"", JsonStringWriter.toString(chars.toString()))
    }

    /**
     * Test escaping of chars < 256.
     */
    @Test
    fun testEscape() {
        val chars = StringBuilder()
        for (i in 0..159)
            chars.append(i.toChar())

        assertEquals(
                "\\u0000\\u0001\\u0002\\u0003\\u0004\\u0005\\u0006\\u0007\\b\\t\\n\\u000b\\f\\r\\u000e\\u000f"
                        + "\\u0010\\u0011\\u0012\\u0013\\u0014\\u0015\\u0016\\u0017\\u0018\\u0019\\u001a\\u001b\\u001c"
                        + "\\u001d\\u001e\\u001f !\\\"#$%&'()*+,-./0123456789:;<=>?@"
                        + "ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\\u0080\\u0081\\u0082"
                        + "\\u0083\\u0084\\u0085\\u0086\\u0087\\u0088\\u0089\\u008a\\u008b\\u008c\\u008d\\u008e\\u008f"
                        + "\\u0090\\u0091\\u0092\\u0093\\u0094\\u0095\\u0096\\u0097\\u0098\\u0099\\u009a\\u009b\\u009c"
                        + "\\u009d\\u009e\\u009f", JsonWriterBase.escape(chars.toString()))
    }

    // TODO(cdi) try to revive
//    @Test
//    fun testWriteToSystemOutLikeStream() {
//        val bytes = ByteArrayOutputStream()
//        JsonAppendableWriter(
//                PrintStream(bytes)).`object`().value("a", 1).value("b", 2).end().done()
//        assertEquals("{\"a\":1,\"b\":2}", String(bytes.toByteArray(), Charset.forName("UTF-8")))
//    }

    /**
     * Test escaping of / when following < to handle &lt;/script&gt;.
     */
    @Test
    fun testScriptEndEscaping() {
        assertEquals("\"<\\/script>\"", JsonStringWriter.toString("</script>"))
        assertEquals("\"/script\"", JsonStringWriter.toString("/script"))
    }

    /**
     * Test a simple array.
     */
    @Test
    fun testArray() {
        val json = JsonStringWriter().array().value(true).value(false).value(true).end().write()
        assertEquals("[true,false,true]", json)
    }

    /**
     * Test an empty array.
     */
    @Test
    fun testArrayEmpty() {
        val json = JsonStringWriter().array().end().write()
        assertEquals("[]", json)
    }

    /**
     * Test an array of empty arrays.
     */
    @Test
    fun testArrayOfEmpty() {
        val json = JsonStringWriter().array().array().end().array().end().end().write()
        assertEquals("[[],[]]", json)
    }

    /**
     * Test a nested array.
     */
    @Test
    fun testNestedArray() {
        val json = JsonStringWriter().array().array().array().value(true).value(false).value(true).end().end().end().write()
        assertEquals("[[[true,false,true]]]", json)
    }

    /**
     * Test a nested array.
     */
    @Test
    fun testNestedArray2() {
        val json = JsonStringWriter().array().value(true).array().array().value(false).end().end().value(true).end().write()
        assertEquals("[true,[[false]],true]", json)
    }

    /**
     * Test a simple object.
     */
    @Test
    fun testObject() {
        val json = JsonStringWriter().`object`().value("a", true).value("b", false).value("c", true).end().write()
        assertEquals("{\"a\":true,\"b\":false,\"c\":true}", json)
    }

    /**
     * Test a nested object.
     */
    @Test
    fun testNestedObject() {
        val json = JsonStringWriter().`object`().`object`("a").value("b", false).value("c", true).end().end().write()
        assertEquals("{\"a\":{\"b\":false,\"c\":true}}", json)
    }

    /**
     * Test a nested object and array.
     */
    @Test
    fun testNestedObjectArray() {
        //@formatter:off
        val json = JsonStringWriter()
                .`object`()
                .`object`("a")
                .array("b")
                .`object`()
                .value("a", 1)
                .value("b", 2)
                .end()
                .`object`()
                .value("c", 1.0)
                .value("d", 2.0)
                .end()
                .end()
                .value("c", JsonArray.from("a", "b", "c"))
                .end()
                .end()
                .write()
        //@formatter:on
        assertEquals("{\"a\":{\"b\":[{\"a\":1,\"b\":2},{\"c\":1.0,\"d\":2.0}]," + "\"c\":[\"a\",\"b\",\"c\"]}}", json)
    }

    /**
     * Tests the [Appendable] code.
     */
    @Test
    fun testAppendable() {
        val writer = StringBuilder()
        JsonAppendableWriter(writer).`object`().value("abc", "def").end().done()
        assertEquals("{\"abc\":\"def\"}", writer.toString())
    }

    @Test
    fun testQuickJson() {
        assertEquals("true", JsonStringWriter.toString(true))
    }

    @Test
    fun testQuickJsonArray() {
        assertEquals("[1,2,3]", JsonStringWriter.toString(JsonArray.from(1, 2, 3)))
    }

    @Test
    fun testQuickArray() {
        assertEquals("[1,2,3]", JsonStringWriter.toString(listOf(1, 2, 3)))
    }

    @Test
    fun testQuickArrayEmpty() {
        assertEquals("[]", JsonStringWriter.toString(emptyList<Any>()))
    }

    //    @Ignore("unsupported right now")
//    @Test
    fun testQuickObjectArray() {
        assertEquals("[1,2,3]", JsonStringWriter.toString(arrayOf<Any>(1, 2, 3)))
    }

    //    @Ignore("unsupported right now")
//    @Test
    fun testQuickObjectArrayNested() {
        assertEquals("[[1,2],[[3]]]", JsonStringWriter.toString(
                arrayOf<Any>(arrayOf<Any>(1, 2), arrayOf<Any>(arrayOf<Any>(3)))))
    }

    //    @Ignore("unsupported right now")
//    @Test
    fun testQuickObjectArrayEmpty() {
        assertEquals("[]", JsonStringWriter.toString(arrayOfNulls<Any>(0)))
    }

    //    @Ignore("unsupported right now")
//    @Test
    fun testObjectArrayInMap() {
        val o = JsonObject()
        o.put("array of string", arrayOf("a", "b", "c"))
        o.put("array of Boolean", arrayOf(true, false))
        o.put("array of int", intArrayOf(1, 2, 3))
        o.put("array of JsonObject", arrayOf<JsonObject?>(JsonObject(), null))
        assertEquals("{\"array of JsonObject\":[{},null],\"array of Boolean\":[true,false]," + "\"array of string\":[\"a\",\"b\",\"c\"],\"array of int\":[1,2,3]}",
                JsonStringWriter.toString(o))
    }

    @Test
    fun testFailureNoKeyInObject() {
        try {
            JsonStringWriter().`object`().value(true).end().write()
            fail()
        } catch (e: JsonWriterException) {
            // OK
        }

    }

    @Test
    fun testFailureNoKeyInObject2() {
        try {
            JsonStringWriter().`object`().value("a", 1).value(true).end().write()
            fail()
        } catch (e: JsonWriterException) {
            // OK
        }

    }

    @Test
    fun testFailureKeyInArray() {
        try {
            JsonStringWriter().array().value("x", true).end().write()
            fail()
        } catch (e: JsonWriterException) {
            // OK
        }

    }

    @Test
    fun testFailureKeyInArray2() {
        try {
            JsonStringWriter().array().value(1).value("x", true).end().write()
            fail()
        } catch (e: JsonWriterException) {
            // OK
        }

    }

    @Test
    fun testFailureNotFullyClosed() {
        try {
            JsonStringWriter().array().value(1).write()
            fail()
        } catch (e: JsonWriterException) {
            // OK
        }

    }

    @Test
    fun testFailureNotFullyClosed2() {
        try {
            JsonStringWriter().array().write()
            fail()
        } catch (e: JsonWriterException) {
            // OK
        }

    }

    @Test
    fun testFailureEmpty() {
        try {
            JsonStringWriter().write()
            fail()
        } catch (e: JsonWriterException) {
            // OK
        }

    }

    @Test
    fun testFailureEmpty2() {
        try {
            JsonStringWriter().end()
            fail()
        } catch (e: JsonWriterException) {
            // OK
        }

    }

    @Test
    fun testFailureMoreThanOneRoot() {
        try {
            JsonStringWriter().value(1).value(1).write()
            fail()
        } catch (e: JsonWriterException) {
            // OK
        }

    }

    @Test
    fun testFailureMoreThanOneRoot2() {
        try {
            JsonStringWriter().array().value(1).end().value(1).write()
            fail()
        } catch (e: JsonWriterException) {
            // OK
        }

    }

    @Test
    fun testFailureMoreThanOneRoot3() {
        try {
            JsonStringWriter().array().value(1).end().array().value(1).end().write()
            fail()
        } catch (e: JsonWriterException) {
            // OK
        }

    }
}
