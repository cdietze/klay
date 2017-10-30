package klay.core.json

import klay.core.Json
import klay.core.Json.TypedArray
import kotlin.reflect.KClass

/**
 * Extends a [Map] with helper methods to determine the underlying JSON type of the map
 * element.
 */
internal class JsonObject : Json.Object {
    private val map: MutableMap<String, Any?>

    /**
     * Creates an empty [JsonObject] with the default capacity.
     */
    init {
        // we use a tree map here to ensure predictable iteration order; for better or worse we have a
        // bunch of tests that rely on a specific iteration order and those broke when we moved from
        // JDK7 to 8; now we use an API that guarantees iteration order
        map = LinkedHashMap<String, Any?>()
    }

    override fun getArray(key: String): Json.Array? {
        return get(key) as? Json.Array
    }

    override fun getBoolean(key: String): Boolean? {
        return get(key) as? Boolean
    }

    override fun getDouble(key: String): Double? {
        return (get(key) as? Number)?.toDouble()
    }

    override fun getFloat(key: String): Float? {
        return (get(key) as? Number)?.toFloat()
    }

    override fun getInt(key: String): Int? {
        return (get(key) as? Number)?.toInt()
    }

    override fun getLong(key: String): Long? {
        return (get(key) as? Number)?.toLong()
    }

    override fun getObject(key: String): Json.Object? {
        return get(key) as? JsonObject
    }

    override fun getString(key: String): String? {
        return get(key) as? String
    }

    override fun containsKey(key: String): Boolean {
        return map.containsKey(key)
    }

    override fun isArray(key: String): Boolean {
        return get(key) is Json.Array
    }

    override fun isBoolean(key: String): Boolean {
        return get(key) is Boolean
    }

    override fun isNull(key: String): Boolean {
        return get(key) == null
    }

    override fun isNumber(key: String): Boolean {
        return get(key) is Number
    }

    override fun isString(key: String): Boolean {
        return get(key) is String
    }

    override fun isObject(key: String): Boolean {
        return get(key) is Json.Object
    }

    override fun <T : Any> getArray(key: String, valueType: KClass<T>): TypedArray<T>? {
        val array = getArray(key)
        return if (array == null) null else JsonTypedArray(array, valueType)
    }

    override fun keys(): TypedArray<String> {
        return JsonStringTypedArray(map.keys)
    }

    override fun put(key: String, value: Any?): JsonObject {
        JsonImpl.checkJsonType(value)
        map.put(key, value)
        return this
    }

    override fun remove(key: String): JsonObject {
        map.remove(key)
        return this
    }

    override fun toString(): String {
        return map.toString()
    }

    override fun <T : JsonSink<T>> write(sink: JsonSink<T>): JsonSink<T> {
        for ((key, value) in map)
            sink.value(key, value)
        return sink
    }

    /**
     * Gets the JSON value at the given key.
     */
    operator fun get(key: String): Any? {
        return map[key]
    }

    companion object {

        /**
         * Creates a [JsonBuilder] for a [JsonObject].
         */
        fun builder(): JsonBuilder<JsonObject> {
            return JsonBuilder(JsonObject())
        }
    }
}
