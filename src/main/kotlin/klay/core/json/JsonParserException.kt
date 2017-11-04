package klay.core.json

/**
 * Thrown when the parser encounters malformed JSON.
 */
class JsonParserException(message: String,
                          /**
                           * Gets the 1-based line position of the error.
                           */
                          val linePosition: Int,
                          /**
                           * Gets the 1-based character position of the error.
                           */
                          val charPosition: Int,
                          /**
                           * Gets the 0-based character offset of the error from the beginning of the string.
                           */
                          val charOffset: Int) : RuntimeException(message) {
    companion object {
        private val serialVersionUID = 1L
    }
}
