package klay.core

import klay.core.json.JsonParserException
import klay.core.json.JsonSink
import java.util.*

/**
 * Klay JSON parsing and serialization interface.
 */
interface Json {
    /**
     * A [JsonSink] that writes JSON to a string.
     */
    interface Writer : JsonSink<Writer> {

        /**
         * Completes this JSON writing session and returns the internal representation as a
         * [String].
         */
        fun write(): String

        /**
         * Tells the writer whether to use a verbose, more human-readable [String]
         * representation.
         */
        fun useVerboseFormat(verbose: Boolean): Writer
    }

    /**
     * A JSON array that assumes all values are of a uniform JSON type.
     */
    interface TypedArray<T> : Iterable<T> {
        /**
         * Returns the number of values in this array.
         */
        fun length(): Int

        /**
         * Returns the value at the given index, or `null` if there's a
         * value of a different type at the index.

         * @throws ArrayIndexOutOfBoundsException if `index < 0` or `index >= length`
         */
        operator fun get(index: Int): T?

        /**
         * Returns an iterator over the values of the assumed type in this array. If a value at a given
         * index isn't of the assumed type, the default value for the assumed type will be returned by
         * `next`.
         * TODO(cdi) check whether the default values are really used, or if we should rather throw on type check errors
         */
        override fun iterator(): Iterator<T>

        /**
         * Contains utility methods for creating typed arrays to supply as the default when fetching
         * optional typed arrays from your JSON model. For example:
         * <pre>`Json.TypedArray<Integer> sizes = json.getArray(
         * "sizes", Integer.class, Json.TypedArray.Util.create(3, 5, 9));
        `</pre> *
         */
        object Util {

            /** Creates a typed array using `data` as its backing data.  */
            fun create(vararg data: Boolean): TypedArray<Boolean> {
                return Util.toArray<Boolean>(data.toTypedArray())
            }

            /** Creates a typed array using `data` as its backing data.  */
            fun create(vararg data: Int): TypedArray<Int> {
                return Util.toArray<Int>(data.toTypedArray())
            }

            /** Creates a typed array using `data` as its backing data.  */
            fun create(vararg data: Float): TypedArray<Float> {
                return Util.toArray<Float>(data.toTypedArray())
            }

            /** Creates a typed array using `data` as its backing data.  */
            fun create(vararg data: Double): TypedArray<Double> {
                return Util.toArray<Double>(data.toTypedArray())
            }

            /** Creates a typed array using `data` as its backing data.  */
            fun create(vararg data: String): TypedArray<String> {
                return Util.toArray<String>(data)
            }

            /** Creates a typed array using `data` as its backing data.  */
            fun create(vararg data: Json.Object): TypedArray<Json.Object> {
                return Util.toArray<Json.Object>(data)
            }

            /** Creates a typed array using `data` as its backing data.  */
            fun create(vararg data: Json.Array): TypedArray<Json.Array> {
                return Util.toArray<Json.Array>(data)
            }

            private fun <T> toArray(data: kotlin.Array<out Any>): TypedArray<T> {
                return object : TypedArray<T> {
                    override fun length(): Int {
                        return data.size
                    }

                    override fun get(index: Int): T {
                        val value = data[index] as T
                        return value
                    }

                    override fun iterator(): Iterator<T> {
                        val list = Arrays.asList<Any>(*data) as List<T>
                        return list.iterator()
                    }
                }
            }
        }
    }

    /**
     * Represents a parsed JSON array as a simple `int->value` map.
     */
    interface Array {

        /**
         * Gets the length of this array.
         */
        fun length(): Int

        /**
         * Gets the boolean value at the given index, or `null` if there is no value at this
         * index.
         */
        fun getBoolean(index: Int): Boolean?

        /**
         * Gets the float value at the given index, or `null` if there is no value at this
         * index.
         */
        fun getNumber(index: Int): Float?

        /**
         * Gets the double value at the given index, or `null` if there is no value at this
         * index.
         */
        fun getDouble(index: Int): Double?

        /**
         * Gets the integer value at the given index, or `null` if there is no value at this
         * index.
         */
        fun getInt(index: Int): Int?

        /**
         * Gets the long value at the given index, or `null` if there is no value at this
         * index. *NOTE:* this is not accurate on the HTML backend as all numbers are
         * represented as doubles, which cannot represent all possible long values. This is included
         * for projects that use only the other backends and need long values.
         */
        fun getLong(index: Int): Long?

        /**
         * Gets the string value at the given index, or `null` if there is no value at this
         * index.
         */
        fun getString(index: Int): String?

        /**
         * Gets the object value at the given index, or `null` if there is no value at this
         * index.
         */
        fun getObject(index: Int): Object?

        /**
         * Gets the array value at the given index, or `null` if there is no value at this
         * index.
         */
        fun getArray(index: Int): Array?

        /**
         * Gets an array at the given index that assumes its values are of the given json type, or
         * `null` if there is no value at this index.

         * @param jsonType one of Json.Object, Json.Array, Boolean, Integer, Float, Double, or String
         * *
         * *
         * @throws IllegalArgumentException if jsonType is of an invalid type.
         */
        fun <T> getArray(index: Int, jsonType: Class<T>): TypedArray<T>?

        /**
         * Returns `true` if the value at the given index is an array.
         */
        fun isArray(index: Int): Boolean

        /**
         * Returns `true` if the value at the given index is a boolean.
         */
        fun isBoolean(index: Int): Boolean

        /**
         * Returns `true` if the value at the given index is null, or does not exist.
         */
        fun isNull(index: Int): Boolean

        /**
         * Returns `true` if the value at the given index is a number.
         */
        fun isNumber(index: Int): Boolean

        /**
         * Returns `true` if the value at the given index is a string.
         */
        fun isString(index: Int): Boolean

        /**
         * Returns `true` if the value at the given index is an object.
         */
        fun isObject(index: Int): Boolean

        /**
         * Appends a JSON boolean, null, number, object, or array value.

         * @return  a reference to this, allowing the calls to be chained together.
         */
        fun add(value: Any?): Array

        /**
         * Inserts a JSON boolean, null, number, object, or array value at the given index. If the index
         * is past the end of the array, the array is null-padded to the given index.

         * @return  a reference to this, allowing the calls to be chained together.
         */
        fun add(index: Int, value: Any?): Array

        /**
         * Removes a JSON value from the given index. If the index is out of bounds, this is a no-op.

         * @return  a reference to this, allowing the calls to be chained together.
         */
        fun remove(index: Int): Array

        /**
         * Sets a JSON boolean, null, number, object, or array value at the given index. If the index
         * is past the end of the array, the array is null-padded to the given index.

         * @return  a reference to this, allowing the calls to be chained together.
         */
        operator fun set(index: Int, value: Any?): Array

        /**
         * Writes this object to a [JsonSink].
         */
        fun <T : JsonSink<T>> write(sink: JsonSink<T>): JsonSink<T>
    }

    /**
     * Represents a parsed JSON object as a simple `string->value` map.
     */
    interface Object {
        /**
         * Gets the boolean value at the given key, or `null` if there is no value at this
         * key.
         */
        fun getBoolean(key: String): Boolean?

        /**
         * Gets the float value at the given key, or `null` if there is no value at this key.
         */
        fun getNumber(key: String): Float?

        /**
         * Gets the double value at the given key, or `null` if there is no value at this key.
         */
        fun getDouble(key: String): Double?

        /**
         * Gets the integer value at the given key, or `null` if there is no value at this key.
         */
        fun getInt(key: String): Int?

        /**
         * Gets the long value at the given key, or `null` if there is no value at this key.
         * *NOTE:* this is not accurate on the HTML backend as all numbers are represented as
         * doubles, which cannot represent all possible long values. This is included for projects that
         * use only the other backends and need long values.
         */
        fun getLong(key: String): Long?

        /**
         * Gets the string value at the given key, or `null` if there is no value at this
         * key.
         */
        fun getString(key: String): String?

        /**
         * Gets the object value at the given key, or `null` if there is no value at this
         * key.
         */
        fun getObject(key: String): Object?

        /**
         * Gets the array value at the given key, or `null` if there is no value at this key.
         */
        fun getArray(key: String): Array?

        /**
         * Gets an array at the given key that assumes its values are of the given json type, or
         * `null` if there is no value at this key.

         * @param jsonType one of Json.Object, Json.Array, Boolean, Integer, Float, Double, or String
         * *
         * *
         * @throws IllegalArgumentException if jsonType is of an invalid type.
         */
        fun <T> getArray(key: String, jsonType: Class<T>): TypedArray<T>?

        /**
         * Returns true if this object contains a value at the specified key (even if that element is null),
         * false if not.
         */
        fun containsKey(key: String): Boolean

        /**
         * Gets a snapshot of the current set of keys for this JSON object. Modifications to the object
         * will not be reflected in this set of keys.
         */
        fun keys(): TypedArray<String>

        /**
         * Returns `true` if the value at the given key is an array.
         */
        fun isArray(key: String): Boolean

        /**
         * Returns `true` if the value at the given key is a boolean.
         */
        fun isBoolean(key: String): Boolean

        /**
         * Returns `true` if the value at the given key is null or does not exist.
         */
        fun isNull(key: String): Boolean

        /**
         * Returns `true` if the value at the given key is a number.
         */
        fun isNumber(key: String): Boolean

        /**
         * Returns `true` if the value at the given key is a string.
         */
        fun isString(key: String): Boolean

        /**
         * Returns `true` if the value at the given key is an object.
         */
        fun isObject(key: String): Boolean

        /**
         * Inserts a JSON null, object, array or string value at the given key.

         * @return  a reference to this, allowing the calls to be chained together.
         */
        fun put(key: String, value: Any?): Object

        /**
         * Removes a JSON value at the given key.

         * @return  a reference to this, allowing the calls to be chained together.
         */
        fun remove(key: String): Object

        /**
         * Writes this object to a [JsonSink].
         */
        fun <T : JsonSink<T>> write(sink: JsonSink<T>): JsonSink<T>
    }

    /**
     * Creates an new, empty [Array].
     */
    fun createArray(): Array

    /**
     * Creates an new, empty [Object].
     */
    fun createObject(): Object

    /**
     * Determines if the given object is a JSON [Array].
     */
    fun isArray(o: Any): Boolean

    /**
     * Determines if the given object is a JSON [Object].
     */
    fun isObject(o: Any): Boolean

    /**
     * Creates a new [Writer], which can be used to serialize data into the JSON format.

     * <pre>`// An example of using the JSON writer interface.
     * String jsonString = json.newWriter()
     * .object()
     * .value("x", 10)
     * .value("y", 10)
     * .object("nestedObject")
     * .value("id", "xyz123")
     * .end()
     * .array("nestedArray")
     * .value(1)
     * .value(2)
     * .value(3)
     * .value(4)
     * .value(5)
     * .end()
     * .end()
     * .done();

     * // Produces:
     * {
     * 'x': 10,
     * 'y': 10,
     * 'nestedObject': {
     * 'id': 'xyz123'
     * },
     * 'nestedArray': [
     * 1, 2, 3, 4, 5
     * ]
     * }
    `</pre> *
     */
    fun newWriter(): Writer

    /**
     * Parses the given JSON string into an [Object] that can be dynamically introspected.
     */
    @Throws(JsonParserException::class)
    fun parse(json: String): Object

    /**
     * Parses the given JSON string into an [Array] that can be dynamically introspected.
     */
    @Throws(JsonParserException::class)
    fun parseArray(json: String): Array
}
