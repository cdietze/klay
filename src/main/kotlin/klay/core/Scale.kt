package klay.core

import pythagoras.f.MathUtil

/**
 * Encapsulates a scale factor, provides useful utility methods.
 */
class Scale(
        /** The scale factor for HiDPI mode, or 1 if HDPI mode is not enabled.  */
        val factor: Float) {

    /** Used by [.getScaledResources].  */
    class ScaledResource(
            /** The scale factor for this resource.  */
            val scale: Scale,
            /**
             * The path to the resource, including any scale factor annotation. If the scale is one, the
             * image path is unadjusted. If the scale is greater than one, the scale is tacked onto the
             * image path (before the extension). The scale factor will be converted to an integer per the
             * following examples:
             *
             *  *  Scale factor 2: `foo.png` becomes `foo@2x.png`
             *  *  Scale factor 4: `foo.png` becomes `foo@4x.png`
             *  *  Scale factor 1.5: `foo.png` becomes `foo@15x.png`
             *  *  Scale factor 1.25: `foo.png` becomes `foo@13x.png`
             *
             */
            val path: String) {

        override fun toString(): String {
            return scale.toString() + ": " + path
        }
    }

    init {
        assert(factor > 0) { "Scale factor must be > 0." }
    }

    /** Returns the supplied length scaled by our scale factor.  */
    fun scaled(length: Float): Float {
        return factor * length
    }

    /** Returns the supplied length scaled by our scale factor and rounded up.  */
    fun scaledCeil(length: Float): Int {
        return MathUtil.iceil(scaled(length))
    }

    /** Returns the supplied length scaled by our scale factor and rounded down.  */
    fun scaledFloor(length: Float): Int {
        return MathUtil.ifloor(scaled(length))
    }

    /** Returns the supplied length inverse scaled by our scale factor.  */
    fun invScaled(length: Float): Float {
        return length / factor
    }

    /** Returns the supplied length inverse scaled by our scale factor and rounded down.  */
    fun invScaledFloor(length: Float): Int {
        return MathUtil.ifloor(invScaled(length))
    }

    /** Returns the supplied length inverse scaled by our scale factor and rounded up.  */
    fun invScaledCeil(length: Float): Int {
        return MathUtil.iceil(invScaled(length))
    }

    /**
     * Returns an ordered series of scaled resources to try when loading an asset. The highest
     * (presumably native) resolution will be tried first, then that will be stepped down to all
     * whole integers less than the native resolution. Often this is simply `2, 1`, but on a
     * Retina iPad, it could be `4, 3, 2, 1`, and on Android devices it may often be something
     * like `3, 2, 1` or `2.5, 2, 1`.
     */
    fun getScaledResources(path: String): List<ScaledResource> {
        val rsrcs = ArrayList<ScaledResource>()
        rsrcs.add(ScaledResource(this, computePath(path, factor)))
        var rscale = MathUtil.ifloor(factor)
        while (rscale > 1) {
            if (rscale.toFloat() != factor)
                rsrcs.add(
                        ScaledResource(Scale(rscale.toFloat()), computePath(path, rscale.toFloat())))
            rscale -= 1
        }
        rsrcs.add(ScaledResource(ONE, path))
        return rsrcs
    }

    override fun toString(): String {
        return "x" + factor
    }

    private fun computePath(path: String, scale: Float): String {
        if (scale <= 1) return path
        var scaleFactor = (scale * 10).toInt()
        if (scaleFactor % 10 == 0)
            scaleFactor /= 10
        val didx = path.lastIndexOf(".")
        if (didx == -1) {
            return path // no extension!?
        } else {
            return path.substring(0, didx) + "@" + scaleFactor + "x" + path.substring(didx)
        }
    }

    companion object {

        /** An unscaled scale factor singleton.  */
        val ONE = Scale(1f)
    }
}
