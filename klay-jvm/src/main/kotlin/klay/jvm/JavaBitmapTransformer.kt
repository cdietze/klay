package klay.jvm

import klay.core.Image
import java.awt.image.BufferedImage

/**
 * Enables the transformation of Java image bitmaps.
 */
interface JavaBitmapTransformer : Image.BitmapTransformer {
    /**
     * Transforms the supplied buffered image into a new buffered image which will be used as the
     * source data for a new Klay image. *Do not* modify the buffered image passed into this
     * method or you will break things.
     */
    fun transform(image: BufferedImage): BufferedImage
}
