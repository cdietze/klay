package klay.core.json

internal class JsonAppendableWriter(appendable: Appendable) : JsonWriterBase<JsonAppendableWriter>(appendable) {

    /**
     * Closes this JSON writing session.
     */
    fun done() {
        super.doneInternal()
    }
}
