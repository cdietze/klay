package klay.core

open class BatchImpl(protected val storage: Storage) : Storage.Batch {
    private var updates: MutableMap<String, String?>? = HashMap()

    override fun setItem(key: String, data: String) {
        updates!!.put(key, data)
    }

    override fun removeItem(key: String) {
        updates!!.put(key, null)
    }

    override fun commit() {
        try {
            onBeforeCommit()
            for ((key, data) in updates!!) {
                if (data == null)
                    removeImpl(key)
                else
                    setImpl(key, data)
            }
            onAfterCommit()

        } finally {
            updates = null // prevent further use
        }
    }

    protected fun onBeforeCommit() {}

    protected open fun setImpl(key: String, data: String) {
        storage.setItem(key, data)
    }

    protected open fun removeImpl(key: String) {
        storage.removeItem(key)
    }

    protected open fun onAfterCommit() {}
}
