package klay.core.json

import klay.core.Json
import kotlin.reflect.KClass

/**
 * Base [klay.core.Json.TypedArray] implementation shared among platforms.

 * This class is public so that backends can re-use it, but it is not part of the public API.
 */
class JsonTypedArray<T : Any>(private val array: Json.Array, type: KClass<T>) : Json.TypedArray<T> {
    private val getter: Getter<T>

    private interface Getter<T> {
        fun get(array: Json.Array, index: Int): T?
    }

    init {
        val getter = getters[type] as Getter<T>? ?: throw IllegalArgumentException("Only json types may be used for TypedArray, not '"
                + type.simpleName + "'")
        this.getter = getter
    }

    override fun length(): Int = array.length()

    override fun get(index: Int): T? = getter.get(array, index)

    override fun iterator(): Iterator<T> {
        return object : Iterator<T> {
            override fun hasNext(): Boolean {
                return index < length()
            }

            override fun next(): T {
                if (index >= length()) {
                    throw NoSuchElementException()
                }
                return get(index++)!!
            }

            private var index: Int = 0
        }
    }

    companion object {

        private val getters = HashMap<KClass<*>, Getter<*>>()

        init {
            getters.put(Boolean::class, object : Getter<Boolean> {
                override fun get(array: Json.Array, index: Int): Boolean? = array.getBoolean(index)
            })
            getters.put(Int::class, object : Getter<Int> {
                override fun get(array: Json.Array, index: Int): Int? = array.getInt(index)
            })
            getters.put(Double::class, object : Getter<Double> {
                override fun get(array: Json.Array, index: Int): Double? = array.getDouble(index)
            })
            getters.put(Float::class, object : Getter<Float> {
                override fun get(array: Json.Array, index: Int): Float? = array.getFloat(index)
            })
            getters.put(String::class, object : Getter<String> {
                override fun get(array: Json.Array, index: Int): String? = array.getString(index)
            })
            getters.put(Json.Array::class, object : Getter<Json.Array> {
                override fun get(array: Json.Array, index: Int): Json.Array? = array.getArray(index)
            })
            getters.put(Json.Object::class, object : Getter<Json.Object> {
                override fun get(array: Json.Array, index: Int): Json.Object? = array.getObject(index)
            })
        }
    }
}
