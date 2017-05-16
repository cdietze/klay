package klay.jvm

import klay.core.BatchImpl
import klay.core.Log
import klay.core.Storage
import java.util.*
import java.util.prefs.AbstractPreferences
import java.util.prefs.BackingStoreException
import java.util.prefs.Preferences

/**
 * JavaStorage is backed by the Java Preferences system.
 */
class JavaStorage(private val log: Log, storageFileName: String) : Storage {
    private val preferences: Preferences
    override var isPersisted: Boolean = false
        private set

    init {
        var prefs: Preferences?
        try {
            isPersisted = Preferences.userRoot().nodeExists(storageFileName)
            prefs = Preferences.userRoot().node(storageFileName)
        } catch (e: Exception) {
            log.warn("Couldn't open Preferences: " + e.message)
            isPersisted = false
            prefs = MemoryPreferences()
        }

        preferences = prefs!!
    }

    override fun setItem(key: String, value: String) {
        preferences.put(key, value)
        maybePersistPreferences()
    }

    override fun removeItem(key: String) {
        preferences.remove(key)
        maybePersistPreferences()
    }

    override fun getItem(key: String): String? {
        return preferences.get(key, null)
    }

    override fun startBatch(): Storage.Batch {
        return object : BatchImpl(this) {
            override fun setImpl(key: String, data: String) {
                preferences.put(key, data)
            }

            override fun removeImpl(key: String) {
                preferences.remove(key)
            }

            override fun onAfterCommit() {
                maybePersistPreferences()
            }
        }
    }

    override fun keys(): Iterable<String> {
        try {
            return Arrays.asList(*preferences.keys())
        } catch (e: Exception) {
            log.warn("Error reading preferences: " + e.message)
            return emptyList()
        }

    }

    private fun maybePersistPreferences() {
        if (preferences is MemoryPreferences) return
        try {
            preferences.flush()
            isPersisted = true
        } catch (e: Exception) {
            log.info("Error persisting properties: " + e.message)
            isPersisted = false
        }

    }

    /**
     * Wraps a HashMap up as Preferences for in-memory use.
     */
    private inner class MemoryPreferences internal constructor() : AbstractPreferences(null, "") {
        override fun putSpi(key: String, value: String) {
            _values.put(key, value)
        }

        override fun getSpi(key: String): String? {
            return _values[key]
        }

        override fun removeSpi(key: String) {
            _values.remove(key)
        }

        override fun removeNodeSpi() {
            throw BackingStoreException("Not implemented")
        }

        override fun keysSpi(): Array<String> {
            return _values.keys.toTypedArray()
        }

        override fun childrenNamesSpi(): Array<String> {
            throw BackingStoreException("Not implemented")
        }

        override fun childSpi(name: String): AbstractPreferences {
            throw RuntimeException("Not implemented")
        }

        override fun syncSpi() {
            throw BackingStoreException("Not implemented")
        }

        override fun flushSpi() {
            throw BackingStoreException("Not implemented")
        }

        protected var _values: MutableMap<String, String> = HashMap()
    }
}
