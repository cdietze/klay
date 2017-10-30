package klay.core.json

// TODO(cdi): Remove this class: with [Flushable] not being in kotlin common, this class doesnt make much sense
internal class JsonAppendableWriter(appendable: Appendable) : JsonWriterBase<JsonAppendableWriter>(appendable) {

    /**
     * Closes this JSON writer and flushes the underlying [Appendable] if it is also [Flushable].

     * @throws JsonWriterException
     * *             if the underlying [Flushable] [Appendable] failed to flush.
     */
    fun done() {
        super.doneInternal()
    }
}
