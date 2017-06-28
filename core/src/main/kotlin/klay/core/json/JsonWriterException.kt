package klay.core.json

/**
 * Thrown when [klay.core.Json.Writer] is used to write invalid JSON.
 */
class JsonWriterException : RuntimeException {

    internal constructor(message: String) : super(message) {}

    internal constructor(t: Throwable) : super(t) {}

    companion object {
        private val serialVersionUID = 1L
    }
}
