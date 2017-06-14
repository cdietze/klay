package tripleklay.util

/**
 * Facilities for parsing JSON data
 * TODO(cdi) re-add when klay.core.Json is implemented
 */
//object JsonUtil {
//    /**
//     * @return the Enum whose name corresponds to string for the given key, or `defaultVal`
//     * * if the key doesn't exist.
//     */
//    fun <T : Enum<T>> getEnum(json: Json.Object, key: String, enumType: Class<T>,
//                              defaultVal: T): T {
//        return Enum.valueOf<T>(enumType, getString(json, key, defaultVal.toString()))
//    }
//
//    /**
//     * @return the Enum whose name corresponds to string for the given key.
//     * * Throws a RuntimeException if the key doesn't exist.
//     */
//    fun <T : Enum<T>> requireEnum(json: Json.Object, key: String,
//                                  enumType: Class<T>): T {
//        return Enum.valueOf<T>(enumType, requireString(json, key))
//    }
//
//    /**
//     * @return the boolean value at the given key, or `defaultVal` if the key
//     * * doesn't exist.
//     */
//    fun getBoolean(json: Json.Object, key: String, defaultVal: Boolean): Boolean {
//        return if (json.containsKey(key)) json.getBoolean(key) else defaultVal
//    }
//
//    /**
//     * @return the boolean value at the given key.
//     * *
//     * @throws RuntimeException if the key doesn't exist.
//     */
//    fun requireBoolean(json: Json.Object, key: String): Boolean {
//        requireKey(json, key)
//        return json.getBoolean(key)
//    }
//
//    /**
//     * @return the double value at the given key, or `defaultVal` if the key
//     * * doesn't exist.
//     */
//    fun getNumber(json: Json.Object, key: String, defaultVal: Double): Double {
//        return if (json.containsKey(key)) json.getNumber(key) else defaultVal
//    }
//
//    /**
//     * @return the double value at the given key.
//     * *
//     * @throws RuntimeException if the key doesn't exist.
//     */
//    fun requireNumber(json: Json.Object, key: String): Double {
//        requireKey(json, key)
//        return json.getNumber(key)
//    }
//
//    /**
//     * @return the float value at the given key, or `defaultVal` if the key
//     * * doesn't exist.
//     */
//    fun getFloat(json: Json.Object, key: String, defaultVal: Float): Float {
//        return getNumber(json, key, defaultVal.toDouble()).toFloat()
//    }
//
//    /**
//     * @return the float value at the given key.
//     * *
//     * @throws RuntimeException if the key doesn't exist.
//     */
//    fun requireFloat(json: Json.Object, key: String): Float {
//        return requireNumber(json, key).toFloat()
//    }
//
//    /**
//     * @return the int value at the given key, or `defaultVal` if the key
//     * * doesn't exist.
//     */
//    fun getInt(json: Json.Object, key: String, defaultVal: Int): Int {
//        return if (json.containsKey(key)) json.getInt(key) else defaultVal
//    }
//
//    /**
//     * @return the int value at the given key.
//     * *
//     * @throws RuntimeException if the key doesn't exist.
//     */
//    fun requireInt(json: Json.Object, key: String): Int {
//        requireKey(json, key)
//        return json.getInt(key)
//    }
//
//    /**
//     * @return the String value at the given key, or `defaultVal` if the key
//     * * doesn't exist.
//     */
//    fun getString(json: Json.Object, key: String, defaultVal: String): String {
//        return if (json.containsKey(key)) json.getString(key) else defaultVal
//    }
//
//    /**
//     * @return the String value at the given key.
//     * *
//     * @throws RuntimeException if the key doesn't exist.
//     */
//    fun requireString(json: Json.Object, key: String): String {
//        requireKey(json, key)
//        return json.getString(key)
//    }
//
//    /**
//     * @return the Json.Object value at the given key, or `defaultVal` if the key
//     * * doesn't exist.
//     */
//    fun getObject(json: Json.Object, key: String, defaultVal: Json.Object): Json.Object {
//        return if (json.containsKey(key)) json.getObject(key) else defaultVal
//    }
//
//    /**
//     * @return the Json.Object at the given key.
//     * *
//     * @throws RuntimeException if the key doesn't exist.
//     */
//    fun requireObject(json: Json.Object, key: String): Json.Object {
//        requireKey(json, key)
//        return json.getObject(key)
//    }
//
//    /**
//     * @return the Json.Object value at the given key, or `defaultVal` if the key
//     * * doesn't exist.
//     */
//    fun getArray(json: Json.Object, key: String, defaultVal: Json.Array): Json.Array {
//        return if (json.containsKey(key)) json.getArray(key) else defaultVal
//    }
//
//    /**
//     * @return the Json.Array at the given key.
//     * *
//     * @throws RuntimeException if the key doesn't exist.
//     */
//    fun requireArray(json: Json.Object, key: String): Json.Array {
//        requireKey(json, key)
//        return json.getArray(key)
//    }
//
//    /**
//     * @return a String representation of the given Json
//     */
//    fun toString(plat: Platform, json: Json.Object, verbose: Boolean): String {
//        val writer = plat.json().newWriter().useVerboseFormat(verbose)
//        writer.`object`()
//        json.write(writer)
//        writer.end()
//
//        return writer.write()
//    }
//
//    protected fun requireKey(json: Json.Object, key: String) {
//        if (!json.containsKey(key)) {
//            throw RuntimeException("Missing required key [name=$key]")
//        }
//    }
//}
