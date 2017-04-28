//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.f

/**
 * Implements some code shared by the various [Transform] implementations.
 */
abstract class AbstractTransform : Transform {
    override // from Transform
    fun scale(): Vector {
        return Vector(scaleX(), scaleY())
    }

    override // from Transform
    fun translation(): Vector {
        return Vector(tx(), ty())
    }

    override // from Transform
    fun setUniformScale(scale: Float): Transform {
        throw UnsupportedOperationException()
    }

    override // from Transform
    fun setScale(scaleX: Float, scaleY: Float): Transform {
        setScaleX(scaleX)
        setScaleY(scaleY)
        return this
    }

    override // from Transform
    fun setScaleX(scaleX: Float): Transform {
        throw UnsupportedOperationException()
    }

    override // from Transform
    fun setScaleY(scaleY: Float): Transform {
        throw UnsupportedOperationException()
    }

    override // from Transform
    fun setRotation(angle: Float): Transform {
        throw UnsupportedOperationException()
    }

    override // from Transform
    fun setTranslation(tx: Float, ty: Float): Transform {
        setTx(tx)
        setTy(ty)
        return this
    }

    override // from Transform
    fun uniformScale(scale: Float): Transform {
        throw UnsupportedOperationException()
    }

    override // from Transform
    fun scale(scaleX: Float, scaleY: Float): Transform {
        scaleX(scaleX)
        scaleY(scaleY)
        return this
    }

    override // from Transform
    fun scaleX(scaleX: Float): Transform {
        throw UnsupportedOperationException()
    }

    override // from Transform
    fun scaleY(scaleY: Float): Transform {
        throw UnsupportedOperationException()
    }

    override // from Transform
    fun rotate(angle: Float): Transform {
        throw UnsupportedOperationException()
    }

    override // from Transform
    fun translate(tx: Float, ty: Float): Transform {
        translateX(tx)
        translateY(ty)
        return this
    }

    override // from Transform
    fun translateX(tx: Float): Transform {
        throw UnsupportedOperationException()
    }

    override // from Transform
    fun translateY(ty: Float): Transform {
        throw UnsupportedOperationException()
    }

    override // from Transform
    fun shear(sx: Float, sy: Float): Transform {
        shearX(sx)
        shearY(sy)
        return this
    }

    override // from Transform
    fun shearX(sx: Float): Transform {
        throw UnsupportedOperationException()
    }

    override // from Transform
    fun shearY(sy: Float): Transform {
        throw UnsupportedOperationException()
    }

    override // from Transform
    fun setTx(tx: Float): Transform {
        throw UnsupportedOperationException()
    }

    override // from Transform
    fun setTy(ty: Float): Transform {
        throw UnsupportedOperationException()
    }

    override // from Transform
    fun setTransform(m00: Float, m01: Float, m10: Float, m11: Float, tx: Float, ty: Float): Transform {
        throw UnsupportedOperationException()
    }

    @Deprecated("")
    override // from Transform
    fun clone(): Transform {
        return copy()
    }

    abstract override // from Transform
    fun copy(): Transform
}
