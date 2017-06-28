package klay.core.json

import klay.core.Json
import klay.core.Json.TypedArray
import java.util.*

/**
 * Extends an [ArrayList] with helper methods to determine the underlying JSON type of the list element.
 */
internal class JsonArray : Json.Array {
    private val list: ArrayList<Any?>

    /**
     * Creates an empty [JsonArray] with the default capacity.
     */
    constructor() {
        list = ArrayList<Any?>()
    }

    /**
     * Creates an empty [JsonArray] from the given collection of objects.
     */
    constructor(collection: Collection<Any>) {
        list = ArrayList(collection)
    }

    override fun add(value: Any?): JsonArray {
        JsonImpl.checkJsonType(value)
        list.add(value)
        return this
    }

    override fun add(index: Int, value: Any?): JsonArray {
        JsonImpl.checkJsonType(value)
        // TODO(mmastrac): Use an array rather than ArrayList to make this more efficient
        while (list.size < index)
            list.add(null)
        list.add(index, value)
        return this
    }

    override fun getArray(index: Int): Json.Array? {
        return get(index) as? Json.Array?
    }

    override fun <T> getArray(index: Int, jsonType: Class<T>): TypedArray<T>? {
        val array = getArray(index)
        return if (array == null) null else JsonTypedArray(array, jsonType)
    }

    override fun getBoolean(index: Int): Boolean? {
        return get(index) as? Boolean?
    }

    override fun getDouble(index: Int): Double? {
        return (get(index) as? Number?)?.toDouble()
    }

    override fun getNumber(index: Int): Float? {
        return (get(index) as? Number?)?.toFloat()
    }

    override fun getInt(index: Int): Int? {
        return (get(index) as? Number?)?.toInt()
    }

    override fun getLong(index: Int): Long? {
        return (get(index) as? Number?)?.toLong()
    }

    override fun getObject(index: Int): Json.Object? {
        return get(index) as? Json.Object?
    }

    override fun getString(index: Int): String? {
        return get(index) as? String?
    }

    override fun isArray(index: Int): Boolean {
        return get(index) is Json.Array
    }

    override fun isBoolean(index: Int): Boolean {
        return get(index) is Boolean
    }

    override fun isNull(index: Int): Boolean {
        return get(index) == null
    }

    override fun isNumber(index: Int): Boolean {
        return get(index) is Number
    }

    override fun isString(index: Int): Boolean {
        return get(index) is String
    }

    override fun isObject(index: Int): Boolean {
        return get(index) is Json.Object
    }

    override fun length(): Int {
        return list.size
    }

    override fun remove(index: Int): JsonArray {
        if (index < 0 || index >= list.size)
            return this
        list.removeAt(index)
        return this
    }

    override fun set(index: Int, value: Any?): JsonArray {
        JsonImpl.checkJsonType(value)
        // TODO(mmastrac): Use an array rather than ArrayList to make this more efficient
        while (list.size <= index)
            list.add(null)
        list[index] = value
        return this
    }

    override fun toString(): String {
        return list.toString()
    }

    override fun <T : JsonSink<T>> write(sink: JsonSink<T>): JsonSink<T> {
        for (i in list.indices)
            sink.value(list[i])
        return sink
    }

    /**
     * Returns the underlying object at the given index, or null if it does not exist or is out of
     * bounds (to match the HTML implementation).
     */
    operator fun get(key: Int): Any? {
        return if (key >= 0 && key < list.size) list[key] else null
    }

    companion object {

        /**
         * Creates a [JsonArray] from an array of contents.
         */
        fun from(vararg contents: Any): JsonArray {
            return JsonArray(Arrays.asList(*contents))
        }

        /**
         * Creates a [JsonBuilder] for a [JsonArray].
         */
        fun builder(): JsonBuilder<JsonArray> {
            return JsonBuilder(JsonArray())
        }
    }

}
