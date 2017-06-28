package klay.core.json

import klay.core.Json

/**
 * [Json] implementation for Java-like platforms.

 * This class is public to allow the other engines to reference it, but it is not part of the public API.
 */
class JsonImpl : Json {

    override fun newWriter(): Json.Writer {
        return JsonStringWriter()
    }

    override fun createArray(): Json.Array {
        return JsonArray()
    }

    override fun createObject(): Json.Object {
        return JsonObject()
    }

    override fun isArray(o: Any): Boolean {
        return JsonTypes.isArray(o)
    }

    override fun isObject(o: Any): Boolean {
        return JsonTypes.isObject(o)
    }

    @Throws(JsonParserException::class)
    override fun parse(json: String): Json.Object {
        return JsonParser.`object`().from(json)
    }

    @Throws(JsonParserException::class)
    override fun parseArray(json: String): Json.Array {
        return JsonParser.array().from(json)
    }

    companion object {

        internal fun checkJsonType(value: Any?) {
            if (value == null || value is String || value is Json.Object || value is Json.Array || value is Boolean || value is Number)
                return

            throw IllegalArgumentException("Invalid JSON type [value=" + value + ", class=" + value.javaClass + "]")
        }
    }
}
