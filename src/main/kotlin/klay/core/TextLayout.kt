package klay.core

import pythagoras.f.Dimension
import pythagoras.f.IDimension
import pythagoras.f.IRectangle

/**
 * Contains metrics and metadata for a laid out body of text. The text may subsequently be rendered
 * to a canvas.
 */
abstract class TextLayout protected constructor(
        /** The text that was laid out.  */
        val text: String,
        /** The [TextFormat] used to lay out this text.  */
        val format: TextFormat,
        /** Returns the precise bounds of the text rendered by this layout. The x and y position may be
         * non-zero if the text is rendered somewhat offset from the "natural" origin of the text line.
         * Unfortunately x may even be negative in some cases which makes rendering the text into a
         * bespoke image troublesome. [TextBlock] provides methods to help with this.  */
        val bounds: IRectangle, height: Float) {

    /** The size of the bounding box that contains all of the rendered text. Note: the height is the
     * height of a line of text. It is not a tight bounding box, but rather the ascent plus the
     * descent and is thus consistent regardless of what text is rendered. Use [.bounds] if
     * you want the precise height of just the rendered text.  */
    val size: IDimension

    /** The number of pixels from the top of a line of text to the baseline.  */
    abstract fun ascent(): Float

    /** The number of pixels from the baseline to the bottom of a line of text.  */
    abstract fun descent(): Float

    /** The number of pixels between the bottom of one line of text and the top of the next.  */
    abstract fun leading(): Float

    init {
        // if the x position is positive, we need to include extra space in our full-width for it
        this.size = Dimension(maxOf(bounds.x, 0f) + bounds.width, height)
    }

    companion object {

        /** A helper function for normalizing EOL prior to processing.  */
        fun normalizeEOL(text: String): String {
            // normalize newlines in the text (Windows: CRLF -> LF, Mac OS pre-X: CR -> LF)
            return text.replace("\r\n", "\n").replace('\r', '\n')
        }
    }
}
