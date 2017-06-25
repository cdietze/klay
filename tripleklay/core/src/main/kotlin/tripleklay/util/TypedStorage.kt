package tripleklay.util

import klay.core.Log
import klay.core.Storage
import react.IntValue
import react.Slot
import react.Value

/**
 * Makes using PlayN [Storage] more civilized. Provides getting and setting of typed values
 * (ints, booleans, etc.). Provides support for default values. Provides [Value] interface to
 * storage items.
 */
class TypedStorage(protected val _log: Log, protected val _storage: Storage) {

    /**
     * Returns whether the specified key is mapped to some value.
     */
    operator fun contains(key: String): Boolean {
        return _storage.getItem(key) != null
    }

    /**
     * Returns the specified property as a string, returning null if the property does not exist.
     */
    operator fun get(key: String): String? {
        return _storage.getItem(key)
    }

    /**
     * Returns the specified property as a string, returning the supplied defautl value if the
     * property does not exist.
     */
    fun get(key: String, defval: String): String {
        val value = _storage.getItem(key)
        return value ?: defval
    }

    /**
     * Sets the specified property to the supplied string value.
     */
    operator fun set(key: String, value: String) {
        _storage.setItem(key, value)
    }

    /**
     * Returns the specified property as an int. If the property does not exist, the default value
     * will be returned. If the property cannot be parsed as an int, an error will be logged and
     * the default value will be returned.
     */
    fun get(key: String, defval: Int): Int {
        var value: String? = null
        try {
            value = _storage.getItem(key)
            return if (value == null) defval else Integer.parseInt(value!!)
        } catch (e: Exception) {
            _log.warn("Failed to parse int prop [key=$key, value:$value]", e)
            return defval
        }

    }

    /**
     * Sets the specified property to the supplied int value.
     */
    operator fun set(key: String, value: Int) {
        _storage.setItem(key, value.toString())
    }

    /**
     * Returns the specified property as a long. If the property does not exist, the default value
     * will be returned. If the property cannot be parsed as a long, an error will be logged and
     * the default value will be returned.
     */
    fun get(key: String, defval: Long): Long {
        var value: String? = null
        try {
            value = _storage.getItem(key)
            return if (value == null) defval else java.lang.Long.parseLong(value!!)
        } catch (e: Exception) {
            _log.warn("Failed to parse long prop [key=$key, value:$value]", e)
            return defval
        }

    }

    /**
     * Sets the specified property to the supplied long value.
     */
    operator fun set(key: String, value: Long) {
        _storage.setItem(key, value.toString())
    }

    /**
     * Returns the specified property as a double. If the property does not exist, the default
     * value will be returned. If the property cannot be parsed as a double, an error will be
     * logged and the default value will be returned.
     */
    fun get(key: String, defval: Double): Double {
        var value: String? = null
        try {
            value = _storage.getItem(key)
            return if (value == null) defval else java.lang.Double.parseDouble(value!!)
        } catch (e: Exception) {
            _log.warn("Failed to parse double prop [key=$key, value:$value]", e)
            return defval
        }

    }

    /**
     * Sets the specified property to the supplied double value.
     */
    operator fun set(key: String, value: Double) {
        _storage.setItem(key, value.toString())
    }

    /**
     * Returns the specified property as a boolean. If the property does not exist, the default
     * value will be returned. Any existing value equal to `t` (ignoring case) will be
     * considered true; all others, false.
     */
    fun get(key: String, defval: Boolean): Boolean {
        val value = _storage.getItem(key)
        return if (value == null) defval else value!!.equals("t", ignoreCase = true)
    }

    /**
     * Sets the specified property to the supplied boolean value.
     */
    operator fun set(key: String, value: Boolean) {
        _storage.setItem(key, if (value) "t" else "f")
    }

    /**
     * Returns the specified property as an enum. If the property does not exist, the default value
     * will be returned.
     * @throws NullPointerException if `defval` is null.
     */
    inline fun <reified E : Enum<E>> get(key: String, defval: E): E {
        var value: String? = null
        try {
            value = _storage.getItem(key)
            return if (value == null) defval else enumValueOf<E>(value)
        } catch (e: Exception) {
            _log.warn("Failed to parse enum prop [key=$key, value:$value]", e)
            return defval
        }

    }

    /**
     * Sets the specified property to the supplied enum value.
     */
    operator fun set(key: String, value: Enum<*>) {
        _storage.setItem(key, value.name)
    }

    /**
     * Removes the specified key (and its value) from storage.
     */
    fun remove(key: String) {
        _storage.removeItem(key)
    }

    /**
     * Exposes the specified property as a [Value]. The supplied default value will be used
     * if the property has no current value. Updates to the value will be written back to the
     * storage system. Note that each call to this method yields a new [Value] and those
     * values will not coordinate with one another, so the caller must be sure to only call this
     * method once for a given property and share that value properly.
     */
    fun valueFor(key: String, defval: String): Value<String> {
        val value = Value.create(get(key, defval))
        value.connect(object : Slot<String> {
            override fun invoke(value: String) {
                set(key, value)
            }
        })
        return value
    }

    /**
     * Exposes the specified property as an [IntValue]. The supplied default value will be
     * used if the property has no current value. Updates to the value will be written back to the
     * storage system. Note that each call to this method yields a new [IntValue] and those
     * values will not coordinate with one another, so the caller must be sure to only call this
     * method once for a given property and share that value properly.
     */
    fun valueFor(key: String, defval: Int): IntValue {
        val value = IntValue(get(key, defval))
        value.connect(object : Slot<Int> {
            override fun invoke(value: Int) {
                set(key, value)
            }
        })
        return value
    }

    /**
     * Exposes the specified property as a [Value]. The supplied default value will be used
     * if the property has no current value. Updates to the value will be written back to the
     * storage system. Note that each call to this method yields a new [Value] and those
     * values will not coordinate with one another, so the caller must be sure to only call this
     * method once for a given property and share that value properly.
     */
    fun valueFor(key: String, defval: Long): Value<Long> {
        val value = Value.create(get(key, defval))
        value.connect(object : Slot<Long> {
            override fun invoke(value: Long) {
                set(key, value)
            }
        })
        return value
    }

    /**
     * Exposes the specified property as a [Value]. The supplied default value will be used
     * if the property has no current value. Updates to the value will be written back to the
     * storage system. Note that each call to this method yields a new [Value] and those
     * values will not coordinate with one another, so the caller must be sure to only call this
     * method once for a given property and share that value properly.
     */
    fun valueFor(key: String, defval: Double): Value<Double> {
        val value = Value.create(get(key, defval))
        value.connect(object : Slot<Double> {
            override fun invoke(value: Double) {
                set(key, value)
            }
        })
        return value
    }

    /**
     * Exposes the specified property as a [Value]. The supplied default value will be used
     * if the property has no current value. Updates to the value will be written back to the
     * storage system. Note that each call to this method yields a new [Value] and those
     * values will not coordinate with one another, so the caller must be sure to only call this
     * method once for a given property and share that value properly.
     */
    fun valueFor(key: String, defval: Boolean): Value<Boolean> {
        val value = Value.create(get(key, defval))
        value.connect(object : Slot<Boolean> {
            override fun invoke(value: Boolean) {
                set(key, value)
            }
        })
        return value
    }

    /**
     * Exposes the specified property as a [Value]. The supplied default value will be used
     * if the property has no current value. Updates to the value will be written back to the
     * storage system. Note that each call to this method yields a new [Value] and those
     * values will not coordinate with one another, so the caller must be sure to only call this
     * method once for a given property and share that value properly.
     */
    inline fun <reified E : Enum<E>> valueFor(key: String, defval: E): Value<E> {
        val value = Value.create(get(key, defval))
        value.connect(object : Slot<E> {
            override fun invoke(value: E) {
                set(key, value)
            }
        })
        return value
    }

    /**
     * Exposes the specified property as an [RSet] using `impl` as the concrete set
     * implementation. See [.setFor] for more details.
     */
    // TODO(cdi) re-add when react.RSet is implemented
//    @JvmOverloads fun <E> setFor(key: String, toFunc: Function<String, E>,
//                                 fromFunc: Function<E, String>, impl: Set<E> = HashSet<E>()): RSet<E> {
//        val rset = RSet.create(impl)
//        val data = get(key, null as String?)
//        if (data != null) {
//            for (value in data!!.split(",".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()) {
//                try {
//                    rset.add(toFunc.apply(value))
//                } catch (e: Exception) {
//                    _log.warn("Invalid value", "key", key, "value", value, e)
//                }
//
//            }
//        }
//        rset.connect(object : RSet.Listener<E>() {
//            fun onAdd(unused: E) {
//                save()
//            }
//
//            fun onRemove(unused: E) {
//                save()
//            }
//
//            protected fun save() {
//                if (rset.isEmpty())
//                    remove(key)
//                else {
//                    val buf = StringBuilder()
//                    var ii = 0
//                    for (value in rset) {
//                        if (ii++ > 0) buf.append(",")
//                        buf.append(fromFunc.apply(value))
//                    }
//                    set(key, buf.toString())
//                }
//            }
//        })
//        return rset
//    }
}
/**
 * Exposes the specified property as an [RSet]. The contents of the set will be encoded
 * as a comma separated string and the supplied `toFunc` and `fromFunc` will be
 * used to convert an individual set item to and from a string. The to and from functions
 * should perform escaping and unescaping of commas if the encoded representation of the items
 * might naturally contain commas.

 *
 * Any modifications to the set will be immediately persisted back to storage. Note that
 * each call to this method yields a new [RSet] and those sets will not coordinate with
 * one another, so the caller must be sure to only call this method once for a given property
 * and share that set properly. Changes to the underlying persistent value that do not take
 * place through the returned set will *not* be reflected in the set and will be
 * overwritten if the set changes.
 */
