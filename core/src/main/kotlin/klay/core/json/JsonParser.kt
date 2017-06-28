package klay.core.json

import java.math.BigInteger

/**
 * Simple JSON parser.

 * <pre>
 * Object json = [JsonParser].any().from("{\"a\":[true,false], \"b\":1}");
 * Number json = ([Number])[JsonParser].any().from("123.456e7");
 * JsonObject json = [JsonParser].object().from("{\"a\":[true,false], \"b\":1}");
 * JsonArray json = [JsonParser].array().from("[1, {\"a\":[true,false], \"b\":1}]");
</pre> *
 */
internal class JsonParser @Throws(JsonParserException::class)
constructor(private val string: String) {
    private var linePos = 1
    private var rowPos: Int = 0
    private val charOffset: Int = 0
    private var utf8adjust: Int = 0
    private var tokenLinePos: Int = 0
    private var tokenCharPos: Int = 0
    private var tokenCharOffset: Int = 0
    private var value: Any? = null
    private var token: Token? = null
    private val reusableBuffer = StringBuilder()

    private var eof: Boolean = false
    private var index: Int = 0
    private val bufferLength: Int

    /**
     * The tokens available in JSON.
     */
    private enum class Token(var isValue: Boolean) {
        EOF(false), NULL(true), TRUE(true), FALSE(true), STRING(true), NUMBER(true), COMMA(false), COLON(false), //
        OBJECT_START(true), OBJECT_END(false), ARRAY_START(true), ARRAY_END(false)
    }

    /**
     * Returns a type-safe parser context for a [JsonObject], [JsonArray] or "any" type from which you can
     * parse a [String].
     */
    class JsonParserContext<T> internal constructor(private val clazz: Class<T>) {

        /**
         * Parses the current JSON type from a [String].
         */
        @Throws(JsonParserException::class)
        fun from(s: String): T {
            return JsonParser(s).parse(clazz)
        }
    }

    init {
        this.bufferLength = string.length
        eof = string.isEmpty()
    }

    /**
     * Parse a single JSON value from the string, expecting an EOF at the end.
     */
    @Throws(JsonParserException::class)
    fun <T> parse(clazz: Class<T>): T {
        advanceToken()
        val parsed = currentValue()
        if (advanceToken() != Token.EOF)
            throw createParseException(null, "Expected end of input, got " + token!!, true)
        if (clazz != Any::class.java && (parsed == null || clazz != parsed.javaClass))
            throw createParseException(null, "JSON did not contain the correct type, expected " + clazz.name
                    + ".", true)
        return parsed as T
    }

    /**
     * Starts parsing a JSON value at the current token position.
     */
    @Throws(JsonParserException::class)
    private fun currentValue(): Any? {
        // Only a value start token should appear when we're in the context of parsing a JSON value
        if (token!!.isValue)
            return value
        throw createParseException(null, "Expected JSON value, got " + token!!, true)
    }

    /**
     * Consumes a token, first eating up any whitespace ahead of it. Note that number tokens are not necessarily valid
     * numbers.
     */
    @Throws(JsonParserException::class)
    private fun advanceToken(): Token {
        var c = advanceChar()
        while (isWhitespace(c))
            c = advanceChar()

        tokenLinePos = linePos
        tokenCharPos = index - rowPos - utf8adjust
        tokenCharOffset = charOffset + index

        when (c) {
            -1 -> return assignToken(Token.EOF)
            '['.toInt() // Inlined function to avoid additional stack
            -> {
                val list = JsonArray()
                if (advanceToken() != Token.ARRAY_END)
                    while (true) {
                        list.add(currentValue())
                        if (advanceToken() == Token.ARRAY_END)
                            break
                        if (token != Token.COMMA)
                            throw createParseException(null, "Expected a comma or end of the array instead of " + token!!,
                                    true)
                        if (advanceToken() == Token.ARRAY_END)
                            throw createParseException(null, "Trailing comma found in array", true)
                    }
                value = list
                return assignToken(Token.ARRAY_START)
            }
            ']'.toInt() -> return assignToken(Token.ARRAY_END)
            ','.toInt() -> return assignToken(Token.COMMA)
            ':'.toInt() -> return assignToken(Token.COLON)
            '{'.toInt() // Inlined function to avoid additional stack
            -> {
                val map = JsonObject()
                if (advanceToken() != Token.OBJECT_END)
                    while (true) {
                        if (token != Token.STRING)
                            throw createParseException(null, "Expected STRING, got " + token!!, true)
                        val key = value as String
                        if (advanceToken() != Token.COLON)
                            throw createParseException(null, "Expected COLON, got " + token!!, true)
                        advanceToken()
                        map.put(key, currentValue())
                        if (advanceToken() == Token.OBJECT_END)
                            break
                        if (token != Token.COMMA)
                            throw createParseException(null, "Expected a comma or end of the object instead of " + token!!,
                                    true)
                        if (advanceToken() == Token.OBJECT_END)
                            throw createParseException(null, "Trailing object found in array", true)
                    }
                value = map
                return assignToken(Token.OBJECT_START)
            }
            '}'.toInt() -> return assignToken(Token.OBJECT_END)
            't'.toInt() -> {
                consumeKeyword(c.toChar(), TRUE)
                value = java.lang.Boolean.TRUE
                return assignToken(Token.TRUE)
            }
            'f'.toInt() -> {
                consumeKeyword(c.toChar(), FALSE)
                value = java.lang.Boolean.FALSE
                return assignToken(Token.FALSE)
            }
            'n'.toInt() -> {
                consumeKeyword(c.toChar(), NULL)
                value = null
                return assignToken(Token.NULL)
            }
            '\"'.toInt() -> {
                value = consumeTokenString()
                return assignToken(Token.STRING)
            }
            '-'.toInt(), in '0'.toInt()..'9'.toInt() -> {
                value = consumeTokenNumber(c.toChar())
                return assignToken(Token.NUMBER)
            }
            '+'.toInt(), '.'.toInt() -> throw createParseException(null, "Numbers may not start with '" + c.toChar() + "'", true)
        }

        if (isAsciiLetter(c))
            throw createHelpfulException(c.toChar(), null, 0)

        throw createParseException(null, "Unexpected character: " + c.toChar(), true)
    }

    /**
     * Expects a given string at the current position.
     */
    @Throws(JsonParserException::class)
    private fun consumeKeyword(first: Char, expected: CharArray) {
        for (i in expected.indices)
            if (advanceChar() != expected[i].toInt())
                throw createHelpfulException(first, expected, i)

        // The token should end with something other than an ASCII letter
        if (isAsciiLetter(peekChar()))
            throw createHelpfulException(first, expected, expected.size)
    }

    /**
     * Steps through to the end of the current number token (a non-digit token).
     */
    @Throws(JsonParserException::class)
    private fun consumeTokenNumber(c: Char): Number {
        val start = index - 1
        var end = index

        var isDouble = false
        while (isDigitCharacter(peekChar())) {
            val next = advanceChar().toChar()
            isDouble = next == '.' || next == 'e' || next == 'E' || isDouble
            end++
        }

        val number = string.substring(start, end)

        try {
            if (isDouble) {
                // Special zero handling to match JSON spec. Leading zero is only allowed if next character is . or e
                if (number[0] == '0') {
                    if (number[1] == '.') {
                        if (number.length == 2)
                            throw createParseException(null, "Malformed number: " + number, true)
                    } else if (number[1] != 'e' && number[1] != 'E')
                        throw createParseException(null, "Malformed number: " + number, true)
                }
                if (number[0] == '-') {
                    if (number[1] == '0') {
                        if (number[2] == '.') {
                            if (number.length == 3)
                                throw createParseException(null, "Malformed number: " + number, true)
                        } else if (number[2] != 'e' && number[2] != 'E')
                            throw createParseException(null, "Malformed number: " + number, true)
                    } else if (number[1] == '.') {
                        throw createParseException(null, "Malformed number: " + number, true)
                    }
                }

                return java.lang.Double.parseDouble(number)
            }

            // Special zero handling to match JSON spec. No leading zeros allowed for integers.
            if (number[0] == '0') {
                if (number.length == 1)
                    return 0
                throw createParseException(null, "Malformed number: " + number, true)
            }
            if (number.length > 1 && number[0] == '-' && number[1] == '0') {
                if (number.length == 2)
                    return -0.0
                throw createParseException(null, "Malformed number: " + number, true)
            }

            // HACK: Attempt to parse using the approximate best type for this
            val length = if (number[0] == '-') number.length - 1 else number.length
            if (length < 10)
            // 2 147 483 647
                return Integer.parseInt(number)
            if (length < 19)
            // 9 223 372 036 854 775 807
                return java.lang.Long.parseLong(number)
            return BigInteger(number)
        } catch (e: NumberFormatException) {
            throw createParseException(e, "Malformed number: " + number, true)
        }

    }

    /**
     * Steps through to the end of the current string token (the unescaped double quote).
     */
    @Throws(JsonParserException::class)
    private fun consumeTokenString(): String {
        reusableBuffer.setLength(0)
        while (true) {
            val c = stringChar()

            when (c) {
                '\"' -> return reusableBuffer.toString()
                '\\' -> {
                    val escape = advanceChar()
                    when (escape) {
                        -1 -> throw createParseException(null, "EOF encountered in the middle of a string escape", false)
                        'b'.toInt() -> reusableBuffer.append('\b')
                        'f'.toInt() -> reusableBuffer.append('\u000C') // \u000C == \f == form feed
                        'n'.toInt() -> reusableBuffer.append('\n')
                        'r'.toInt() -> reusableBuffer.append('\r')
                        't'.toInt() -> reusableBuffer.append('\t')
                        '"'.toInt(), '/'.toInt(), '\\'.toInt() -> reusableBuffer.append(escape.toChar())
                        'u'.toInt() -> reusableBuffer.append((stringHexChar() shl 12 or (stringHexChar() shl 8 //
                                ) or (stringHexChar() shl 4) or stringHexChar()).toChar())
                        else -> throw createParseException(null, "Invalid escape: \\" + escape.toChar(), false)
                    }
                }
                else -> reusableBuffer.append(c)
            }
        }
    }

    /**
     * Advances a character, throwing if it is illegal in the context of a JSON string.
     */
    @Throws(JsonParserException::class)
    private fun stringChar(): Char {
        val c = advanceChar()
        if (c == -1)
            throw createParseException(null, "String was not terminated before end of input", true)
        if (c < 32)
            throw createParseException(null,
                    "Strings may not contain control characters: 0x" + Integer.toString(c, 16), false)
        return c.toChar()
    }

    /**
     * Advances a character, throwing if it is illegal in the context of a JSON string hex unicode escape.
     */
    @Throws(JsonParserException::class)
    private fun stringHexChar(): Int {
        // GWT-compatible Character.digit(char, int)
        val c = "0123456789abcdef0123456789ABCDEF".indexOf(advanceChar().toChar()) % 16
        if (c == -1)
            throw createParseException(null, "Expected unicode hex escape character", false)
        return c
    }

    /**
     * Quick test for digit characters.
     */
    private fun isDigitCharacter(c: Int): Boolean {
        return c in '0'.toInt()..'9'.toInt() || c == 'e'.toInt() || c == 'E'.toInt() || c == '.'.toInt() || c == '+'.toInt() || c == '-'.toInt()
    }

    /**
     * Quick test for whitespace characters.
     */
    private fun isWhitespace(c: Int): Boolean {
        return c == ' '.toInt() || c == '\n'.toInt() || c == '\r'.toInt() || c == '\t'.toInt()
    }

    /**
     * Quick test for ASCII letter characters.
     */
    private fun isAsciiLetter(c: Int): Boolean {
        return c in 'A'.toInt()..'Z'.toInt() || c in 'a'.toInt()..'z'.toInt()
    }

    /**
     * Peek one char ahead, don't advance, returns [Token.EOF] on end of input.
     */
    private fun peekChar(): Int {
        return if (eof) -1 else string[index].toInt()
    }

    private fun assignToken(newToken: Token): Token {
        token = newToken
        return newToken
    }

    /**
     * Advance one character ahead, or return [Token.EOF] on end of input.
     */
    @Throws(JsonParserException::class)
    private fun advanceChar(): Int {
        if (eof)
            return -1
        val c = string[index].toInt()
        if (c == '\n'.toInt()) {
            linePos++
            rowPos = index + 1
            utf8adjust = 0
        }

        index++
        if (index >= bufferLength)
            eof = true

        return c
    }

    /**
     * Throws a helpful exception based on the current alphanumeric token.
     */
    @Throws(JsonParserException::class)
    private fun createHelpfulException(first: Char, expected: CharArray?, failurePosition: Int): JsonParserException {
        // Build the first part of the token
        val errorToken = StringBuilder(first + if (expected == null) "" else String(expected, 0, failurePosition))

        // Consume the whole pseudo-token to make a better error message
        while (isAsciiLetter(peekChar()) && errorToken.length < 15)
            errorToken.append(advanceChar().toChar())

        return createParseException(null, "Unexpected token '" + errorToken + "'"
                + if (expected == null) "" else ". Did you mean '" + first + String(expected) + "'?", true)
    }

    /**
     * Creates a [JsonParserException] and fills it from the current line and char position.
     */
    private fun createParseException(e: Exception?, message: String, tokenPos: Boolean): JsonParserException {
        if (tokenPos)
            return JsonParserException(e, "$message on line $tokenLinePos, char $tokenCharPos",
                    tokenLinePos, tokenCharPos, tokenCharOffset)
        else {
            val charPos = Math.max(1, index - rowPos - utf8adjust)
            return JsonParserException(e, "$message on line $linePos, char $charPos", linePos, charPos,
                    index + charOffset)
        }
    }

    companion object {

        private val TRUE = charArrayOf('r', 'u', 'e')
        private val FALSE = charArrayOf('a', 'l', 's', 'e')
        private val NULL = charArrayOf('u', 'l', 'l')

        /**
         * Parses a [JsonObject] from a source.

         * <pre>
         * JsonObject json = [JsonParser].object().from("{\"a\":[true,false], \"b\":1}");
        </pre> *
         */
        fun `object`(): JsonParserContext<JsonObject> {
            return JsonParserContext(JsonObject::class.java)
        }

        /**
         * Parses a [JsonArray] from a source.

         * <pre>
         * JsonArray json = [JsonParser].array().from("[1, {\"a\":[true,false], \"b\":1}]");
        </pre> *
         */
        fun array(): JsonParserContext<JsonArray> {
            return JsonParserContext(JsonArray::class.java)
        }

        /**
         * Parses any object from a source. For any valid JSON, returns either a null (for the JSON string 'null'), a
         * [String], a [Number], a [Boolean], a [JsonObject] or a [JsonArray].

         * <pre>
         * Object json = [JsonParser].any().from("{\"a\":[true,false], \"b\":1}");
         * Number json = ([Number])[JsonParser].any().from("123.456e7");
        </pre> *
         */
        fun any(): JsonParserContext<Any> {
            return JsonParserContext(Any::class.java)
        }
    }
}
