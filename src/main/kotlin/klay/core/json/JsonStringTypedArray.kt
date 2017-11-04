package klay.core.json

import klay.core.Json

internal class JsonStringTypedArray(contents: Collection<String>) : ArrayList<String>(contents), Json.TypedArray<String> {

    override fun length(): Int {
        return size
    }

    companion object {
        private val serialVersionUID = 1L
    }
}
