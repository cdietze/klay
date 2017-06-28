package klay.core.json

import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.math.BigInteger
import java.nio.charset.Charset
import java.util.zip.GZIPInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class InternalJsonParserTest {
    @Test
    @Throws(JsonParserException::class)
    fun testWhitespace() {
        assertEquals(JsonObject::class,
                JsonParser.`object`().from(
                        " \t\r\n  { \t\r\n \"abc\"   \t\r\n : \t\r\n  1 \t\r\n  }  \t\r\n   ")::class)
        assertEquals("{}", JsonParser.`object`().from("{}").toString())
    }

    @Test
    @Throws(JsonParserException::class)
    fun testEmptyObject() {
        assertEquals(JsonObject::class, JsonParser.`object`().from("{}")::class)
        assertEquals("{}", JsonParser.`object`().from("{}").toString())
    }

    @Test
    @Throws(JsonParserException::class)
    fun testObjectOneElement() {
        assertEquals(JsonObject::class, JsonParser.`object`().from("{\"a\":1}")::class)
        assertEquals("{a=1}", JsonParser.`object`().from("{\"a\":1}").toString())
    }

    @Test
    @Throws(JsonParserException::class)
    fun testObjectTwoElements() {
        assertEquals(JsonObject::class, JsonParser.`object`().from("{\"a\":1,\"B\":1}")::class)
        assertEquals("{B=1, a=1}", JsonParser.`object`().from("{\"a\":1,\"B\":1}").toString())
    }

    @Test
    @Throws(JsonParserException::class)
    fun testEmptyArray() {
        assertEquals(JsonArray::class, JsonParser.array().from("[]")::class)
        assertEquals("[]", JsonParser.array().from("[]").toString())
    }

    @Test
    @Throws(JsonParserException::class)
    fun testArrayOneElement() {
        assertEquals(JsonArray::class, JsonParser.array().from("[1]")::class)
        assertEquals("[1]", JsonParser.array().from("[1]").toString())
    }

    @Test
    @Throws(JsonParserException::class)
    fun testArrayTwoElements() {
        assertEquals(JsonArray::class, JsonParser.array().from("[1,1]")::class)
        assertEquals("[1, 1]", JsonParser.array().from("[1,1]").toString())
    }

    @Test
    @Throws(JsonParserException::class)
    fun testBasicTypes() {
        assertEquals("true", JsonParser.any().from("true").toString())
        assertEquals("false", JsonParser.any().from("false").toString())
        assertNull(JsonParser.any().from("null"))
        assertEquals("1", JsonParser.any().from("1").toString())
        assertEquals("1.0", JsonParser.any().from("1.0").toString())
        assertEquals("", JsonParser.any().from("\"\"").toString())
        assertEquals("a", JsonParser.any().from("\"a\"").toString())
    }

    @Test
    @Throws(JsonParserException::class)
    fun testArrayWithEverything() {
        val a = JsonParser.array().from(
                "[1, -1.0e6, \"abc\", [1,2,3], {\"abc\":123}, true, false]")
        assertEquals("[1, -1000000.0, abc, [1, 2, 3], {abc=123}, true, false]", a.toString())
        assertEquals(1.0, a.getDouble(0)!!, 0.001)
        assertEquals(1, a.getInt(0))
        assertEquals(-1000000, a.getInt(1))
        assertEquals(-1000000.0, a.getDouble(1)!!, 0.001)
        assertEquals("abc", a.getString(2))
        assertEquals(1, a.getArray(3)!!.getInt(0))
        assertEquals(123, a.getObject(4)!!.getInt("abc"))
        assertTrue(a.getBoolean(5)!!)
        assertFalse(a.getBoolean(6)!!)
    }

    @Test
    @Throws(JsonParserException::class)
    fun testObjectWithEverything() {
        // TODO: Is this deterministic if we use string keys?
        val o = JsonParser.`object`().from(
                "{\"abc\":123, \"def\":456.0, \"ghi\":[true, false], \"jkl\":null, \"mno\":true}")
        assertEquals("{abc=123, def=456.0, ghi=[true, false], jkl=null, mno=true}", o.toString())
        assertEquals(123, o.getInt("abc"))
        assertEquals(456, o.getInt("def"))
        assertEquals(true, o.getArray("ghi")!!.getBoolean(0))
        assertEquals(null, o["jkl"])
        assertTrue(o.isNull("jkl"))
        assertTrue(o.getBoolean("mno")!!)
    }

    @Test
    @Throws(JsonParserException::class)
    fun testStringEscapes() {
        assertEquals("\n", JsonParser.any().from("\"\\n\""))
        assertEquals("\r", JsonParser.any().from("\"\\r\""))
        assertEquals("\t", JsonParser.any().from("\"\\t\""))
        assertEquals("\b", JsonParser.any().from("\"\\b\""))
        assertEquals("\u000C", JsonParser.any().from("\"\\u000C\"")) // \u000C == \f == form feed character
        assertEquals("/", JsonParser.any().from("\"/\""))
        assertEquals("\\", JsonParser.any().from("\"\\\\\""))
        assertEquals("\"", JsonParser.any().from("\"\\\"\""))
        assertEquals("\u0000", JsonParser.any().from("\"\\u0000\""))
        assertEquals("\u8000", JsonParser.any().from("\"\\u8000\""))
        assertEquals("\uffff", JsonParser.any().from("\"\\uffff\""))

        assertEquals("all together: \\/\n\r\t\b\u000C (fin)",
                JsonParser.any().from("\"all together: \\\\\\/\\n\\r\\t\\b\\u000C (fin)\""))
    }

    @Test
    @Throws(JsonParserException::class)
    fun testNumbers() {
        val testCases = arrayOf("0", "1", "-0", "-1", "0.1", "1.1", "-0.1", "0.10", "-0.10", "0e1", "0e0", "-0e-1", "0.0e0", "-0.0e0")
        for (testCase in testCases) {
            val n = JsonParser.any().from(testCase) as Number
            assertEquals(java.lang.Double.parseDouble(testCase), n.toDouble(), java.lang.Double.MIN_NORMAL)
            val n2 = JsonParser.any().from(testCase.toUpperCase()) as Number
            assertEquals(java.lang.Double.parseDouble(testCase.toUpperCase()), n2.toDouble(), java.lang.Double.MIN_NORMAL)
        }
    }

    /**
     * Test that negative zero ends up as negative zero in both the parser and the writer.
     */
    @Test
    @Throws(JsonParserException::class)
    fun testNegativeZero() {
        assertEquals("-0.0", java.lang.Double.toString((JsonParser.any().from("-0") as Number).toDouble()))
        assertEquals("-0.0", java.lang.Double.toString((JsonParser.any().from("-0.0") as Number).toDouble()))
        assertEquals("-0.0", java.lang.Double.toString((JsonParser.any().from("-0.0e0") as Number).toDouble()))
        assertEquals("-0.0", java.lang.Double.toString((JsonParser.any().from("-0e0") as Number).toDouble()))
        assertEquals("-0.0", java.lang.Double.toString((JsonParser.any().from("-0e1") as Number).toDouble()))
        assertEquals("-0.0", java.lang.Double.toString((JsonParser.any().from("-0e-1") as Number).toDouble()))
        assertEquals("-0.0", java.lang.Double.toString((JsonParser.any().from("-0e-0") as Number).toDouble()))
        assertEquals("-0.0", java.lang.Double.toString((JsonParser.any().from("-0e-01") as Number).toDouble()))
        assertEquals("-0.0", java.lang.Double.toString((JsonParser.any().from("-0e-000000000001") as Number).toDouble()))

        assertEquals("-0.0", JsonStringWriter.toString(-0.0))
        assertEquals("-0.0", JsonStringWriter.toString(-0.0f))
    }

    /**
     * Test the basic numbers from -100 to 100 as a sanity check.
     */
    @Test
    @Throws(JsonParserException::class)
    fun testBasicNumbers() {
        for (i in -100..+100) {
            assertEquals(i.toLong(), (JsonParser.any().from("" + i) as Int).toLong())
        }
    }

    @Test
    @Throws(JsonParserException::class)
    fun testBigint() {
        val o = JsonParser.`object`().from("{\"v\":123456789123456789123456789}")
        val bigint = o["v"] as BigInteger?
        assertEquals("123456789123456789123456789", bigint!!.toString())
    }

    @Test
    fun testFailWrongType() {
        try {
            JsonParser.`object`().from("1")
            fail("Should have failed to parse")
        } catch (e: JsonParserException) {
            testException(e, 1, 1, "did not contain the correct type")
        }

    }

    @Test
    fun testFailNull() {
        try {
            JsonParser.`object`().from("null")
            fail("Should have failed to parse")
        } catch (e: JsonParserException) {
            testException(e, 1, 4, "did not contain the correct type")
        }

    }

    @Test
    fun testFailNoJson1() {
        try {
            JsonParser.`object`().from("")
            fail("Should have failed to parse")
        } catch (e: JsonParserException) {
            testException(e, 1, 0)
        }

    }

    @Test
    fun testFailNoJson2() {
        try {
            JsonParser.`object`().from(" ")
            fail("Should have failed to parse")
        } catch (e: JsonParserException) {
            testException(e, 1, 1)
        }

    }

    @Test
    fun testFailNoJson3() {
        try {
            JsonParser.`object`().from("  ")
            fail("Should have failed to parse")
        } catch (e: JsonParserException) {
            testException(e, 1, 2)
        }

    }

    @Test
    fun testFailNumberEdgeCases() {
        val edgeCases = arrayOf("-", ".", "e", "01", "-01", "+01", "01.1", "-01.1", "+01.1", ".1", "-.1", "+.1", "+1", "0.", "-0.", "+0.", "0.e", "-0.e", "+0.e", "0e", "-0e", "+0e", "0e-", "-0e-", "+0e-", "0e+", "-0e+", "+0e+")
        for (edgeCase in edgeCases) {
            try {
                JsonParser.`object`().from(edgeCase)
                fail("Should have failed to parse: " + edgeCase)
            } catch (e: JsonParserException) {
                testException(e, 1, 1)
            }

            try {
                JsonParser.`object`().from(edgeCase.toUpperCase())
                fail("Should have failed to parse: " + edgeCase.toUpperCase())
            } catch (e: JsonParserException) {
                testException(e, 1, 1)
            }

        }
    }

    @Test
    fun testFailBustedNumber1() {
        try {
            // There's no 'f' in double, but it treats it as a new token
            JsonParser.`object`().from("123f")
            fail()
        } catch (e: JsonParserException) {
            testException(e, 1, 4)
        }

    }

    @Test
    fun testFailBustedNumber2() {
        try {
            // Badly formed number
            JsonParser.`object`().from("-1-1")
            fail()
        } catch (e: JsonParserException) {
            testException(e, 1, 1)
        }

    }

    @Test
    fun testFailBustedString1() {
        try {
            // Missing " at end
            JsonParser.`object`().from("\"abc")
            fail()
        } catch (e: JsonParserException) {
            testException(e, 1, 1)
        }

    }

    @Test
    fun testFailBustedString2() {
        try {
            // \n in middle of string
            JsonParser.`object`().from("\"abc\n\"")
            fail()
        } catch (e: JsonParserException) {
            testException(e, 2, 1)
        }

    }

    @Test
    fun testFailBustedString3() {
        try {
            // Bad escape "\x" in middle of string
            JsonParser.`object`().from("\"abc\\x\"")
            fail()
        } catch (e: JsonParserException) {
            testException(e, 1, 6)
        }

    }

    @Test
    fun testFailBustedString4() {
        try {
            // Bad escape "\\u123x" in middle of string
            JsonParser.`object`().from("\"\\u123x\"")
            fail()
        } catch (e: JsonParserException) {
            testException(e, 1, 7)
        }

    }

    @Test
    fun testFailBustedString5() {
        try {
            // Incomplete unicode escape
            JsonParser.`object`().from("\"\\u222\"")
            fail()
        } catch (e: JsonParserException) {
            testException(e, 1, 7)
        }

    }

    @Test
    fun testFailBustedString6() {
        try {
            // String that terminates halfway through a unicode escape
            JsonParser.`object`().from("\"\\u222")
            fail()
        } catch (e: JsonParserException) {
            testException(e, 1, 6)
        }

    }

    @Test
    fun testFailBustedString7() {
        try {
            // String that terminates halfway through a regular escape
            JsonParser.`object`().from("\"\\")
            fail()
        } catch (e: JsonParserException) {
            testException(e, 1, 2)
        }

    }

    @Test
    fun testFailArrayTrailingComma1() {
        try {
            JsonParser.`object`().from("[,]")
            fail()
        } catch (e: JsonParserException) {
            testException(e, 1, 2)
        }

    }

    @Test
    fun testFailArrayTrailingComma2() {
        try {
            JsonParser.`object`().from("[1,]")
            fail()
        } catch (e: JsonParserException) {
            testException(e, 1, 4)
        }

    }

    @Test
    fun testFailObjectTrailingComma1() {
        try {
            JsonParser.`object`().from("{,}")
            fail()
        } catch (e: JsonParserException) {
            testException(e, 1, 2)
        }

    }

    @Test
    fun testFailObjectTrailingComma2() {
        try {
            JsonParser.`object`().from("{\"abc\":123,}")
            fail()
        } catch (e: JsonParserException) {
            testException(e, 1, 12)
        }

    }

    @Test
    fun testFailObjectBadKey1() {
        try {
            JsonParser.`object`().from("{true:1}")
            fail()
        } catch (e: JsonParserException) {
            testException(e, 1, 2)
        }

    }

    @Test
    fun testFailObjectBadKey2() {
        try {
            JsonParser.`object`().from("{2:1}")
            fail()
        } catch (e: JsonParserException) {
            testException(e, 1, 2)
        }

    }

    @Test
    fun testFailObjectBadColon1() {
        try {
            JsonParser.`object`().from("{\"abc\":}")
            fail()
        } catch (e: JsonParserException) {
            testException(e, 1, 8)
        }

    }

    @Test
    fun testFailObjectBadColon2() {
        try {
            JsonParser.`object`().from("{\"abc\":1:}")
            fail()
        } catch (e: JsonParserException) {
            testException(e, 1, 9)
        }

    }

    @Test
    fun testFailObjectBadColon3() {
        try {
            JsonParser.`object`().from("{:\"abc\":1}")
            fail()
        } catch (e: JsonParserException) {
            testException(e, 1, 2)
        }

    }

    @Test
    fun testFailBadKeywords1() {
        try {
            JsonParser.`object`().from("truef")
            fail()
        } catch (e: JsonParserException) {
            testException(e, 1, 1, "'truef'")
        }

    }

    @Test
    fun testFailBadKeywords2() {
        try {
            JsonParser.`object`().from("true1")
            fail()
        } catch (e: JsonParserException) {
            testException(e, 1, 5)
        }

    }

    @Test
    fun testFailBadKeywords3() {
        try {
            JsonParser.`object`().from("tru")
            fail()
        } catch (e: JsonParserException) {
            testException(e, 1, 1, "'tru'")
        }

    }

    @Test
    fun testFailBadKeywords4() {
        try {
            JsonParser.`object`().from("[truef,true]")
            fail()
        } catch (e: JsonParserException) {
            testException(e, 1, 2, "'truef'")
        }

    }

    @Test
    fun testFailBadKeywords5() {
        try {
            JsonParser.`object`().from("grue")
            fail()
        } catch (e: JsonParserException) {
            testException(e, 1, 1, "'grue'")
        }

    }

    @Test
    fun testFailBadKeywords6() {
        try {
            JsonParser.`object`().from("trueeeeeeeeeeeeeeeeeeee")
            fail()
        } catch (e: JsonParserException) {
            testException(e, 1, 1, "'trueeeeeeeeeeee'")
        }

    }

    @Test
    fun testFailBadKeywords7() {
        try {
            JsonParser.`object`().from("g")
            fail()
        } catch (e: JsonParserException) {
            testException(e, 1, 1, "'g'")
        }

    }

    @Test
    fun testFailTrailingCommaMultiline() {
        val testString = "{\n\"abc\":123,\n\"def\":456,\n}"
        try {
            JsonParser.`object`().from(testString)
            fail()
        } catch (e: JsonParserException) {
            testException(e, 4, 1)
        }

    }

    @Test
    @Throws(IOException::class)
    fun failureTestsFromYui() {
        val input = javaClass.getResourceAsStream("yui_fail_cases.txt")

        val failCases = readAsUtf8(input).split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (failCase in failCases) {
            try {
                JsonParser.`object`().from(failCase)
                fail("Should have failed, but didn't: " + failCase)
            } catch (e: JsonParserException) {
                // expected
            }

        }
    }

    @Test
    @Throws(JsonParserException::class, IOException::class)
    fun tortureTest() {
        val input = javaClass.getResourceAsStream("torturetest.json.gz")
        val o = JsonParser.`object`().from(readAsUtf8(GZIPInputStream(input)))
        assertNotNull(o["a"])
        assertNotNull(o.getObject("a")!!.getArray("b\uecee\u8324\u007a\\\ue768.N"))
        val json = JsonStringWriter().`object`(o).write()
        val o2 = JsonParser.`object`().from(json)
        /* String json2 = */JsonStringWriter().`object`(o2).write()

        // This doesn't work - keys don't sort properly
        // assertEquals(json, json2);
    }

    //  @Test
    //  public void tortureTestUrl() throws JsonParserException {
    //    JsonObject o = JsonParser.object().from(getClass().getClassLoader().getResource("sample.json"));
    //    assertNotNull(o.getObject("a").getArray("b\uecee\u8324\u007a\\\ue768.N"));
    //  }
    //
    //  @Test
    //  public void tortureTestStream() throws JsonParserException {
    //    JsonObject o = JsonParser.object().from(getClass().getClassLoader().getResourceAsStream("sample.json"));
    //    assertNotNull(o.getObject("a").getArray("b\uecee\u8324\u007a\\\ue768.N"));
    //  }

    /**
     * Tests from json.org: http://www.json.org/JSON_checker/

     * Skips two tests that don't match reality (ie: Chrome).
     */
    @Test
    @Throws(IOException::class)
    fun jsonOrgTest() {
        val input = javaClass.getResourceAsStream("json_org_test.zip")
        val zip = ZipInputStream(input)

        while (true) {
            val ze: ZipEntry = zip.nextEntry ?: break
            if (ze.isDirectory)
                continue

            // skip "A JSON payload should be an object or array, not a string."
            if (ze.name.contains("fail1.json"))
                continue

            // skip "Too deep"
            if (ze.name.contains("fail18.json"))
                continue

            val positive = ze.name.startsWith("test/pass")
            var offset = 0
            var size = ze.size.toInt()
            val buffer = ByteArray(size)
            while (size > 0) {
                val r = zip.read(buffer, offset, buffer.size - offset)
                if (r <= 0)
                    break
                size -= r
                offset += r
            }

            val testCase = String(buffer, Charsets.US_ASCII)
            if (positive) {
                try {
                    val out = JsonParser.any().from(testCase)
                    JsonStringWriter.toString(out)
                } catch (e: JsonParserException) {
                    e.printStackTrace()
                    fail("Should not have failed " + ze.name + ": " + testCase)
                }

            } else {
                try {
                    JsonParser.`object`().from(testCase)
                    fail("Should have failed " + ze.name + ": " + testCase)
                } catch (e: JsonParserException) {
                    // expected
                }

            }

        }
    }

    @Throws(IOException::class)
    private fun readAsUtf8(input: InputStream): String {
        val out = ByteArrayOutputStream()
        val b = ByteArray(1024 * 1024)
        while (true) {
            val r = input.read(b)
            if (r <= 0)
                break
            out.write(b, 0, r)
        }
        val utf8 = Charset.forName("UTF8")
        val s = String(out.toByteArray(), utf8)
        return s
    }

    private fun testException(e: JsonParserException, linePos: Int, charPos: Int) {
        assertEquals("line $linePos char $charPos",
                "line " + e.linePosition + " char " + e.charPosition)
    }

    private fun testException(e: JsonParserException, linePos: Int, charPos: Int, inError: String) {
        assertEquals("line $linePos char $charPos",
                "line " + e.linePosition + " char " + e.charPosition)
        assertTrue("Error did not contain '" + inError + "': " + e.message,
                e.message!!.contains(inError))
    }
}
