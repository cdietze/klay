package klay.core.json

import klay.core.Json

/**
 * Common interface for things that accept JSON objects. Normally not referenced by users.

 * @param <SELF> A subclass of [JsonSink].
</SELF> */
interface JsonSink<SELF : JsonSink<SELF>> {
    /**
     * Emits the start of an array.
     */
    fun array(c: Collection<*>): SELF

    /**
     * Emits the start of an array.
     */
    fun array(c: Json.Array): SELF

    /**
     * Emits the start of an array with a key.
     */
    fun array(key: String, c: Collection<*>): SELF

    /**
     * Emits the start of an array with a key.
     */
    fun array(key: String, c: Json.Array): SELF

    /**
     * Emits the start of an object.
     */
    fun `object`(obj: Map<*, *>): SELF

    /**
     * Emits the start of an object.
     */
    fun `object`(obj: Json.Object): SELF

    /**
     * Emits the start of an object with a key.
     */
    fun `object`(key: String, obj: Map<*, *>): SELF

    /**
     * Emits the start of an object with a key.
     */
    fun `object`(key: String, obj: Json.Object): SELF

    /**
     * Emits a 'null' token.
     */
    fun nul(): SELF

    /**
     * Emits a 'null' token with a key.
     */
    fun nul(key: String): SELF

    /**
     * Emits an object if it is a JSON-compatible type, otherwise throws an exception.
     */
    fun value(o: Any?): SELF

    /**
     * Emits an object with a key if it is a JSON-compatible type, otherwise throws an exception.
     */
    fun value(key: String, o: Any?): SELF

    /**
     * Emits a string value (or null).
     */
    fun value(s: String?): SELF

    /**
     * Emits a boolean value.
     */
    fun value(b: Boolean): SELF

    /**
     * Emits a [Number] value.
     */
    fun value(n: Number?): SELF

    /**
     * Emits a string value (or null) with a key.
     */
    fun value(key: String, s: String?): SELF

    /**
     * Emits a boolean value with a key.
     */
    fun value(key: String, b: Boolean): SELF

    /**
     * Emits a [Number] value with a key.
     */
    fun value(key: String, n: Number?): SELF

    /**
     * Starts an array.
     */
    fun array(): SELF

    /**
     * Starts an object.
     */
    fun `object`(): SELF

    /**
     * Starts an array within an object, prefixed with a key.
     */
    fun array(key: String): SELF

    /**
     * Starts an object within an object, prefixed with a key.
     */
    fun `object`(key: String): SELF

    /**
     * Ends the current array or object.
     */
    fun end(): SELF
}
