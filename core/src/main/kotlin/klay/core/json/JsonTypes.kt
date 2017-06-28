package klay.core.json

import klay.core.Json

/**
 * Type-checking routines. These live in their own file so that we can replace them wholesale in GWT
 * mode.
 */
internal object JsonTypes {
    fun isArray(o: Any): Boolean {
        return o is Json.Array
    }

    fun isObject(o: Any): Boolean {
        return o is Json.Object
    }

}
