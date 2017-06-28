package klay.core.json

import klay.core.Json

internal class JsonStringWriter : JsonWriterBase<Json.Writer>(StringBuilder()), Json.Writer {

    /**
     * Completes this JSON writing session and returns the internal representation as a [String].
     */
    override fun write(): String {
        super.doneInternal()
        return appendable.toString()
    }

    companion object {

        /**
         * Used for testing.
         */
        // TODO(mmastrac): Expose this on the Json interface
        fun toString(value: Any): String {
            return JsonStringWriter().value(value).write()
        }
    }

}
