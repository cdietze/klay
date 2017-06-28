package klay.core.json

import java.io.Flushable
import java.io.IOException

internal class JsonAppendableWriter(appendable: Appendable) : JsonWriterBase<JsonAppendableWriter>(appendable), JsonSink<JsonAppendableWriter> {

    /**
     * Closes this JSON writer and flushes the underlying [Appendable] if it is also [Flushable].

     * @throws JsonWriterException
     * *             if the underlying [Flushable] [Appendable] failed to flush.
     */
    @Throws(JsonWriterException::class)
    fun done() {
        super.doneInternal()
        if (appendable is Flushable) {
            try {
                (appendable as Flushable).flush()
            } catch (e: IOException) {
                throw JsonWriterException(e)
            }

        }
    }
}
