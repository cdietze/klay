package tripleklay.util

import klay.core.Json
import klay.core.Platform

/**
 * Facilities for parsing JSON data
 */
object JsonUtil {
    /**
     * @return the Enum whose name corresponds to string for the given key, or `null` if the key doesn't exist.
     */
    inline fun <reified T : Enum<T>> getEnum(json: Json.Object, key: String): T? {
        return getString(json, key)?.let { enumValueOf<T>(it) }
    }

    /**
     * @return the Enum whose name corresponds to string for the given key.
     * @throws RuntimeException if the key doesn't exist.
     */
    inline fun <reified T : Enum<T>> requireEnum(json: Json.Object, key: String): T {
        return enumValueOf(requireString(json, key))
    }

    /**
     * @return the boolean value at the given key, or `defaultVal` if the key doesn't exist.
     */
    fun getBoolean(json: Json.Object, key: String): Boolean? = json.getBoolean(key)

    /**
     * @return the boolean value at the given key.
     * @throws RuntimeException if the key doesn't exist.
     */
    fun requireBoolean(json: Json.Object, key: String): Boolean {
        requireKey(json, key)
        return json.getBoolean(key)!!
    }

    /**
     * @return the double value at the given key, or `null` if the key doesn't exist.
     */
    fun getDouble(json: Json.Object, key: String): Double? = json.getDouble(key)

    /**
     * @return the double value at the given key.
     * @throws RuntimeException if the key doesn't exist.
     */
    fun requireDouble(json: Json.Object, key: String): Double {
        requireKey(json, key)
        return json.getDouble(key)!!
    }

    /**
     * @return the float value at the given key, or `null` if the key doesn't exist.
     */
    fun getFloat(json: Json.Object, key: String): Float? = json.getFloat(key)

    /**
     * @return the float value at the given key.
     * @throws RuntimeException if the key doesn't exist.
     */
    fun requireFloat(json: Json.Object, key: String): Float {
        requireKey(json, key)
        return json.getFloat(key)!!
    }

    /**
     * @return the int value at the given key, or `null` if the key doesn't exist.
     */
    fun getInt(json: Json.Object, key: String): Int? = json.getInt(key)

    /**
     * @return the int value at the given key.
     * @throws RuntimeException if the key doesn't exist.
     */
    fun requireInt(json: Json.Object, key: String): Int {
        requireKey(json, key)
        return json.getInt(key)!!
    }

    /**
     * @return the String value at the given key, or `null` if the key doesn't exist.
     */
    fun getString(json: Json.Object, key: String): String? {
        return json.getString(key)
    }

    /**
     * @return the String value at the given key.
     * @throws RuntimeException if the key doesn't exist.
     */
    fun requireString(json: Json.Object, key: String): String {
        requireKey(json, key)
        return json.getString(key)!!
    }

    /**
     * @return the Json.Object value at the given key, or `null` if the key doesn't exist.
     */
    fun getObject(json: Json.Object, key: String): Json.Object? = json.getObject(key)

    /**
     * @return the Json.Object at the given key.
     * @throws RuntimeException if the key doesn't exist.
     */
    fun requireObject(json: Json.Object, key: String): Json.Object {
        requireKey(json, key)
        return json.getObject(key)!!
    }

    /**
     * @return the Json.Object value at the given key, or `null` if the key doesn't exist.
     */
    fun getArray(json: Json.Object, key: String): Json.Array? = json.getArray(key)

    /**
     * @return the Json.Array at the given key.
     * @throws RuntimeException if the key doesn't exist.
     */
    fun requireArray(json: Json.Object, key: String): Json.Array {
        requireKey(json, key)
        return json.getArray(key)!!
    }

    /**
     * @return a String representation of the given Json
     */
    fun toString(plat: Platform, json: Json.Object, verbose: Boolean): String {
        val writer = plat.json.newWriter().useVerboseFormat(verbose)
        writer.`object`()
        json.write(writer)
        writer.end()

        return writer.write()
    }

    private fun requireKey(json: Json.Object, key: String) {
        if (!json.containsKey(key)) {
            throw RuntimeException("Missing required key [name=$key]")
        }
    }
}
