package klay.core.json

import klay.core.assertEquals
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InternalJsonTypesTest {
    @Test
    fun testObjectInt() {
        val o = JsonObject()
        o.put("key", 1)
        assertEquals(1, o.getInt("key"))
        assertEquals(1.0, o.getDouble("key")!!, 0.0001)
        assertEquals(1.0f, o.getFloat("key")!!, 0.0001f)
        assertEquals(1f, o.getFloat("key")!!, 0.0001f)
        assertEquals(1, o["key"])

        assertEquals(null, o.getString("key"))
        assertEquals("foo", o.getString("key") ?: "foo")
        assertFalse(o.isNull("key"))
    }

    @Test
    fun testObjectString() {
        val o = JsonObject()
        o.put("key", "1")
        assertEquals(null, o.getInt("key"))
        assertEquals(null, o.getDouble("key"))
        assertEquals(null, o.getFloat("key"))
        assertEquals("1", o["key"])
        assertFalse(o.isNull("key"))
    }

    @Test
    fun testObjectNull() {
        val o = JsonObject()
        o.put("key", null)
        assertEquals(null, o.getInt("key"))
        assertEquals(null, o.getDouble("key"))
        assertEquals(null, o.getFloat("key"))
        assertEquals(null, o["key"])
        assertTrue(o.isNull("key"))
    }

    @Test
    fun testArrayInt() {
        val o = JsonArray(listOf(null as String?, null, null, null))
        o[3] = 1
        assertEquals(1, o.getInt(3))
        assertEquals(1.0, o.getDouble(3)!!, 0.0001)
        assertEquals(1.0f, o.getFloat(3)!!, 0.0001f)
        assertEquals(1f, o.getFloat(3)!!, 0.0001f)
        assertEquals(1, o[3])

        assertEquals(null, o.getString(3))
        assertEquals("foo", o.getString(3) ?: "foo")
        assertFalse(o.isNull(3))
    }

    @Test
    fun testArrayString() {
        val o = JsonArray(listOf(null as String?, null, null, null))
        o[3] = "1"
        assertEquals(null, o.getInt(3))
        assertEquals(null, o.getDouble(3))
        assertEquals(null, o.getFloat(3))
        assertEquals("1", o[3])
        assertFalse(o.isNull(3))
    }

    @Test
    fun testArrayNull() {
        val a = JsonArray(listOf(null as String?, null, null, null))
        a[3] = null
        assertEquals(null, a.getInt(3))
        assertEquals(null, a.getDouble(3))
        assertEquals(null, a.getFloat(3))
        assertEquals(null, a[3])
        assertTrue(a.isNull(3))
    }

    @Test
    fun testArrayBounds() {
        val a = JsonArray(listOf(null as String?, null, null, null))
        assertEquals(null, a.getInt(4))
        assertEquals(null, a.getDouble(4))
        assertEquals(null, a.getFloat(4))
        assertEquals(null, a[4])
        assertTrue(a.isNull(4))
    }

    @Test
    fun testJsonArrayBuilder() {
        //@formatter:off
        val a = JsonArray.builder()
                .value(true)
                .value(1.0)
                .value(1.0f)
                .value(1)
                .value("hi")
                .`object`()
                .value("abc", 123)
                .end()
                .array()
                .value(1)
                .nul()
                .end()
                .array(JsonArray.from(1, 2, 3))
                .`object`(JsonObject.builder().nul("a").nul("b").nul("c").done())
                .done()
        //@formatter:on

        assertEquals(
                "[true,1.0,1.0,1,\"hi\",{\"abc\":123},[1,null],[1,2,3],{\"a\":null,\"b\":null,\"c\":null}]",
                JsonStringWriter.toString(a))
    }

    @Test
    fun testJsonObjectBuilder() {
        //@formatter:off
        val a = JsonObject.builder()
                .value("bool", true)
                .value("double", 1.0)
                .value("float", 1.0f)
                .value("int", 1)
                .value("string", "hi")
                .nul("null")
                .`object`("object")
                .value("abc", 123)
                .end()
                .array("array")
                .value(1)
                .nul()
                .end()
                .array("existingArray", JsonArray.from(1, 2, 3))
                .`object`("existingObject", JsonObject.builder().nul("a").nul("b").nul("c").done())
                .done()
        //@formatter:on

        assertEquals(
                "{\"array\":[1,null],\"bool\":true,\"double\":1.0," +
                        "\"existingArray\":[1,2,3],\"existingObject\":{\"a\":null,\"b\":null,\"c\":null}," +
                        "\"float\":1.0,\"int\":1,\"null\":null,\"object\":{\"abc\":123},\"string\":\"hi\"}",
                JsonStringWriter.toString(a))
    }

    @Test
    fun testJsonArrayBuilderFailCantCloseRoot() {
        assertFailsWith(JsonWriterException::class, {
            JsonArray.builder().end()
        })
    }

    @Test
    fun testJsonArrayBuilderFailCantAddKeyToArray() {
        assertFailsWith(JsonWriterException::class, {
            JsonArray.builder().value("abc", 1)
        })
    }

    @Test
    fun testJsonArrayBuilderFailCantAddNonKeyToObject() {
        assertFailsWith(JsonWriterException::class, {
            JsonObject.builder().value(1)
        })
    }
}
