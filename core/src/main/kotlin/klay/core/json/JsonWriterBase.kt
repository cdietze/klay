package klay.core.json

import klay.core.Json
import java.io.IOException
import java.util.*

/**
 * Internal class that handles emitting to an [Appendable]. Users only see the public
 * subclasses, [JsonStringWriter] and [JsonAppendableWriter].

 * @param <SELF> A subclass of [JsonSink].
</SELF> */
internal open class JsonWriterBase<SELF : JsonSink<SELF>>(protected val appendable: Appendable) : JsonSink<SELF> {
    private val states = Stack<Boolean>()
    private var first = true
    private var inObject: Boolean = false
    private var verboseFormat: Boolean = false

    /**
     * This is guaranteed to be safe as the type of "this" will always be the type of "SELF".
     */
    private fun castThis(): SELF {
        return this as SELF
    }

    /**
     * Tells the writer whether to use a verbose, more human-readable [String]
     * representation.
     */
    fun useVerboseFormat(verboseFormat: Boolean): SELF {
        this.verboseFormat = verboseFormat
        return castThis()
    }

    override fun array(c: Collection<*>): SELF {
        array()
        for (o in c) {
            value(o)
        }
        return end()
    }

    override fun array(c: Json.Array): SELF {
        array()
        c.write(this)
        return end()
    }

    override fun array(key: String, c: Collection<*>): SELF {
        array(key)
        for (o in c) {
            value(o)
        }
        return end()
    }

    override fun array(key: String, c: Json.Array): SELF {
        array(key)
        c.write(this)
        return end()
    }

    override fun `object`(obj: Map<*, *>): SELF {
        `object`()
        for ((key1, o) in obj) {
            if (key1 !is String)
                throw JsonWriterException("Invalid key type for obj: " + (key1?.javaClass ?: "null"))
            value(key1, o)
        }
        return end()
    }

    override fun `object`(obj: Json.Object): SELF {
        `object`()
        obj.write(this)
        return end()
    }

    override fun `object`(key: String, obj: Map<*, *>): SELF {
        `object`(key)
        for ((key1, o) in obj) {
            if (key1 !is String)
                throw JsonWriterException("Invalid key type for obj: " + (key1?.javaClass ?: "null"))
            value(key1, o)
        }
        return end()
    }

    override fun `object`(key: String, obj: Json.Object): SELF {
        `object`(key)
        obj.write(this)
        return end()
    }

    override fun nul(): SELF {
        preValue()
        raw("null")
        return castThis()
    }

    override fun nul(key: String): SELF {
        preValue(key)
        raw("null")
        return castThis()
    }

    override fun value(o: Any?): SELF {
        if (o == null)
            return nul()
        else if (o is String)
            return value(o as String?)
        else if (o is Number)
            return value(o as Number?)
        else if (o is Boolean)
            return value(o)
        else if (o is Collection<*>)
            return array(o)
        else if (o is Map<*, *>)
            return `object`(o)
        else if (JsonTypes.isArray(o))
            return array(o as Json.Array)
        else if (JsonTypes.isObject(o))
            return `object`(o as Json.Object)
        else
            throw JsonWriterException("Unable to handle type: " + o.javaClass)// TODO(mmastrac): Implement in future playn-server
        //    else if (o.getClass().isArray()) {
        //      int length = Array.getLength(o);
        //      array();
        //      for (int i = 0; i < length; i++)
        //        value(Array.get(o, i));
        //      return end();
    }

    override fun value(key: String, o: Any?): SELF {
        if (o == null)
            return nul(key)
        else if (o is String)
            return value(key, o as String?)
        else if (o is Number)
            return value(key, o as Number?)
        else if (o is Boolean)
            return value(key, o)
        else if (o is Collection<*>)
            return array(key, o)
        else if (o is Map<*, *>)
            return `object`(key, o)
        else if (JsonTypes.isArray(o))
            return array(key, o as Json.Array)
        else if (JsonTypes.isObject(o))
            return `object`(key, o as Json.Object)
        else
            throw JsonWriterException("Unable to handle type: " + o.javaClass)// TODO(mmastrac): Implement in future playn-server
        //    else if (o.getClass().isArray()) {
        //      int length = Array.getLength(o);
        //      array(key);
        //      for (int i = 0; i < length; i++)
        //        value(Array.get(o, i));
        //      return end();
    }

    override fun value(s: String?): SELF {
        if (s == null)
            return nul()
        preValue()
        emitStringValue(s)
        return castThis()
    }

    override fun value(b: Boolean): SELF {
        preValue()
        raw(java.lang.Boolean.toString(b))
        return castThis()
    }

    override fun value(n: Number?): SELF {
        preValue()
        if (n == null)
            raw("null")
        else
            raw(n.toString())
        return castThis()
    }

    override fun value(key: String, s: String?): SELF {
        if (s == null)
            return nul(key)
        preValue(key)
        emitStringValue(s)
        return castThis()
    }

    override fun value(key: String, b: Boolean): SELF {
        preValue(key)
        raw(java.lang.Boolean.toString(b))
        return castThis()
    }

    override fun value(key: String, n: Number?): SELF {
        if (n == null)
            return nul(key)
        preValue(key)
        raw(n.toString())
        return castThis()
    }

    override fun array(): SELF {
        preValue()
        states.push(inObject)
        inObject = false
        first = true
        raw('[')
        if (verboseFormat)
            raw('\n')
        return castThis()
    }

    override fun `object`(): SELF {
        preValue()
        states.push(inObject)
        inObject = true
        first = true
        raw('{')
        if (verboseFormat)
            raw('\n')
        return castThis()
    }

    override fun array(key: String): SELF {
        preValue(key)
        states.push(inObject)
        inObject = false
        first = true
        raw('[')
        if (verboseFormat)
            raw('\n')
        return castThis()
    }

    override fun `object`(key: String): SELF {
        preValue(key)
        states.push(inObject)
        inObject = true
        first = true
        raw('{')
        if (verboseFormat)
            raw('\n')
        return castThis()
    }

    override fun end(): SELF {
        if (states.size == 0)
            throw JsonWriterException("Invalid call to end()")

        val wasInObject = inObject

        first = false
        inObject = states.pop()

        if (verboseFormat) {
            raw('\n')
            indent()
        }

        if (wasInObject) {
            raw('}')
        } else {
            raw(']')
        }

        return castThis()
    }

    /**
     * Ensures that the object is in the finished state.

     * @throws JsonWriterException if the written JSON is not properly balanced, ie: all arrays and
     * *           objects that were started have been properly ended.
     */
    protected fun doneInternal() {
        if (states.size > 0)
            throw JsonWriterException("Unclosed JSON objects and/or arrays when closing writer")
        if (first)
            throw JsonWriterException("Nothing was written to the JSON writer")
    }

    private fun indent() {
        // indent 2 spaces per level we've descended
        for (level in states.indices) {
            raw("  ")
        }
    }

    private fun raw(s: String) {
        try {
            appendable.append(s)
        } catch (e: IOException) {
            throw JsonWriterException(e)
        }

    }

    private fun raw(c: Char) {
        try {
            appendable.append(c)
        } catch (e: IOException) {
            throw JsonWriterException(e)
        }

    }

    private fun pre() {
        if (first) {
            first = false
        } else {
            if (states.size == 0)
                throw JsonWriterException("Invalid call to emit a value in a finished JSON writer")
            raw(',')
            if (verboseFormat)
                raw('\n')
        }
        if (verboseFormat)
            indent()
    }

    private fun preValue() {
        if (inObject)
            throw JsonWriterException("Invalid call to emit a keyless value while writing an object")

        pre()
    }

    private fun preValue(key: String) {
        if (!inObject)
            throw JsonWriterException("Invalid call to emit a key value while not writing an object")

        pre()

        emitStringValue(key)
        raw(':')
    }

    /**
     * Emits a quoted string value, escaping characters that are required to be escaped.
     */
    private fun emitStringValue(s: String) {
        raw('"')
        var b: Char = 0.toChar()
        var c: Char = 0.toChar()
        for (i in 0..s.length - 1) {
            b = c
            c = s[i]

            when (c) {
                '\\', '"' -> {
                    raw('\\')
                    raw(c)
                }
                '/' -> {
                    // Special case to ensure that </script> doesn't appear in JSON
                    // output
                    if (b == '<')
                        raw('\\')
                    raw(c)
                }
                '\b' -> raw("\\b")
                '\t' -> raw("\\t")
                '\n' -> raw("\\n")
                '\u000C' -> raw("\\f") // \u000C == \f == form feed character
                '\r' -> raw("\\r")
                else -> if (shouldBeEscaped(c)) {
                    val t = "000" + Integer.toHexString(c.toInt())
                    raw("\\u" + t.substring(t.length - "0000".length))
                } else {
                    raw(c)
                }
            }
        }

        raw('"')
    }

    /**
     * json.org spec says that all control characters must be escaped.
     */
    private fun shouldBeEscaped(c: Char): Boolean {
        return c < ' ' || c >= '\u0080' && c < '\u00a0' || c >= '\u2000' && c < '\u2100'
    }

    companion object {

        /**
         * Used for testing.
         */
        fun escape(s: String): String {
            val json = JsonStringWriter().value(s).write()
            return json.substring(1, json.length - 1)
        }
    }
}
