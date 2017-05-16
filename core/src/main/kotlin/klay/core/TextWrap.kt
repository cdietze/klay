package klay.core

/**
 * Contains the configuration needed when wrapping text.
 */
class TextWrap
/** Creates a text wrap config with the specified width and indent.  */
constructor(
        /** The width at which the text is wrapped.  */
        val width: Float,
        /** An indent applied to the first line of text.  */
        val indent: Float = 0f) {

    override fun hashCode(): Int {
        return width.toInt() xor indent.toInt()
    }

    override fun equals(other: Any?): Boolean {
        if (other is TextWrap) {
            val ow = other
            return width == ow.width && indent == ow.indent
        } else {
            return false
        }
    }

    companion object {

        /** An instance that indicates that we should only wrap on manual line breaks and not at any
         * specific width.  */
        val MANUAL = TextWrap(Float.MAX_VALUE)
    }
}
/** Creates a text wrap config with the specified width and no indent.  */
