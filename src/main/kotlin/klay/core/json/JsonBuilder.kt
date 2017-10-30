package klay.core.json

import klay.core.Json

/**
 * Builds a [JsonObject] or [JsonArray].

 * @param <T>
 * *            The type of JSON object to build.
</T> */
internal class JsonBuilder<T : Any>(private val root: T) : JsonSink<JsonBuilder<T>> {
    private val json = mutableListOf<Any>()

    init {
        json.add(root)
    }

    /**
     * Completes this builder, closing any unclosed objects and returns the built object.
     */
    fun done(): T {
        return root
    }

    override fun array(c: Collection<*>): JsonBuilder<T> {
        return value(c)
    }

    override fun array(c: Json.Array): JsonBuilder<T> {
        return value(c)
    }

    override fun array(key: String, c: Collection<*>): JsonBuilder<T> {
        return value(key, c)
    }

    override fun array(key: String, c: Json.Array): JsonBuilder<T> {
        return value(key, c)
    }

    override fun `object`(obj: Map<*, *>): JsonBuilder<T> {
        return value(obj)
    }

    override fun `object`(`object`: Json.Object): JsonBuilder<T> {
        return value(`object`)
    }

    override fun `object`(key: String, obj: Map<*, *>): JsonBuilder<T> {
        return value(key, obj)
    }

    override fun `object`(key: String, `object`: Json.Object): JsonBuilder<T> {
        return value(key, `object`)
    }

    override fun nul(): JsonBuilder<T> {
        return value(null as Any?)
    }

    override fun nul(key: String): JsonBuilder<T> {
        return value(key, null as Any?)
    }

    override fun value(o: Any?): JsonBuilder<T> {
        arr().add(o)
        return this
    }

    override fun value(key: String, o: Any?): JsonBuilder<T> {
        obj().put(key, o)
        return this
    }

    override fun value(s: String?): JsonBuilder<T> {
        return value(s as Any)
    }

    override fun value(b: Boolean): JsonBuilder<T> {
        return value(b as Any)
    }

    override fun value(n: Number?): JsonBuilder<T> {
        return value(n as Any)
    }

    override fun value(key: String, s: String?): JsonBuilder<T> {
        return value(key, s as Any)
    }

    override fun value(key: String, b: Boolean): JsonBuilder<T> {
        return value(key, b as Any)
    }

    override fun value(key: String, n: Number?): JsonBuilder<T> {
        return value(key, n as Any)
    }

    override fun array(): JsonBuilder<T> {
        val a = JsonArray()
        value(a)
        json.add(a)
        return this
    }

    override fun `object`(): JsonBuilder<T> {
        val o = JsonObject()
        value(o)
        json.add(o)
        return this
    }

    override fun array(key: String): JsonBuilder<T> {
        val a = JsonArray()
        value(key, a)
        json.add(a)
        return this
    }

    override fun `object`(key: String): JsonBuilder<T> {
        val o = JsonObject()
        value(key, o)
        json.add(o)
        return this
    }

    override fun end(): JsonBuilder<T> {
        if (json.size == 1)
            throw JsonWriterException("Cannot end the root object or array")
        json.removeAt(json.size - 1)
        return this
    }

    private fun obj(): JsonObject {
        return json[json.size - 1].let {
            if (it !is JsonObject) throw JsonWriterException("Attempted to write a keyed value to a JsonArray")
            it
        }
    }

    private fun arr(): JsonArray {
        return json[json.size - 1].let {
            if (it !is JsonArray) throw JsonWriterException("Attempted to write a non-keyed value to a JsonObject")
            it
        }
    }
}
