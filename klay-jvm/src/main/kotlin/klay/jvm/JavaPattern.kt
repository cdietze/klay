package klay.jvm

import klay.core.Pattern
import java.awt.TexturePaint

internal class JavaPattern(repeatX: Boolean, repeatY: Boolean, val paint: TexturePaint?) : Pattern(repeatX, repeatY) {

    init {
        assert(paint != null)
    }
}
