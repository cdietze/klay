package tripleklay.shaders

/**
 * Shader related utility methods.
 */
object ShaderUtil {
    /**
     * Formats a floating point value for inclusion in a shader program. Ensures that the value
     * always contains a '.' and a trailing '0' if needed.
     */
    fun format(value: Float): String {
        val fmt = value.toString()
        return if (fmt.indexOf('.') == -1) fmt + ".0" else fmt
    }
}
