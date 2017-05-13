package klay.core

/**
 * Stores settings in a key/value map. This will attempt to store persistently, but will fall back
 * to an in-memory map. Use [.isPersisted] to check if the data are being persisted.
 */
interface Storage {

    /**
     * Represents a batch of edits to be applied to storage in one transaction. Individual edits are
     * expensive on some platforms, and this batch interface allows multiple edits to be applied
     * substantially more efficiently (more than an order of magnitude) on those platforms. If you're
     * going to make hundreds or thousands of changes at once, use this mechanism.
     */
    interface Batch {
        /** Adds an update to the batch.  */
        fun setItem(key: String, data: String)

        /** Adds an deletion to the batch.  */
        fun removeItem(key: String)

        /** Commits the batch, applying all queued changes. Attempts to call [.setItem] or
         * [.removeItem] after a call to this method will fail.  */
        fun commit()
    }

    /**
     * Sets the value associated with the specified key to `data`.

     * @param key identifies the value.
     * *
     * @param data the value associated with the key, which must not be null.
     */
    fun setItem(key: String, data: String)

    /**
     * Removes the item in the Storage associated with the specified key.

     * @param key identifies the value.
     */
    fun removeItem(key: String)

    /**
     * Returns the item associated with `key`, or null if no item is associated with it.

     * @param key identifies the value.
     * *
     * @return the value associated with the given key, or null.
     */
    fun getItem(key: String): String?

    /**
     * Creates a [Batch] that can be used to effect multiple changes to storage in a single,
     * more efficient, operation.
     */
    fun startBatch(): Batch

    /**
     * Returns an object that can be used to iterate over all storage keys. *Note:* changes
     * made to storage while iterating over the keys will not be reflected in the iteration, nor will
     * they conflict with it.
     */
    fun keys(): Iterable<String>

    /**
     * Returns true if storage data will be persistent across restarts.
     */
    val isPersisted: Boolean
}
