//
// Pythagoras - a collection of geometry classes
// http://github.com/samskivert/pythagoras

package pythagoras.d

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
    fun setUniformScale(scale: Double): Transform {
        throw UnsupportedOperationException()
    }

    override // from Transform
    fun setScale(scaleX: Double, scaleY: Double): Transform {
        setScaleX(scaleX)
        setScaleY(scaleY)
        return this
    }

    override // from Transform
    fun setScaleX(scaleX: Double): Transform {
        throw UnsupportedOperationException()
    }

    override // from Transform
    fun setScaleY(scaleY: Double): Transform {
        throw UnsupportedOperationException()
    }

    override // from Transform
    fun setRotation(angle: Double): Transform {
        throw UnsupportedOperationException()
    }

    override // from Transform
    fun setTranslation(tx: Double, ty: Double): Transform {
        setTx(tx)
        setTy(ty)
        return this
    }

    override // from Transform
    fun uniformScale(scale: Double): Transform {
        throw UnsupportedOperationException()
    }

    override // from Transform
    fun scale(scaleX: Double, scaleY: Double): Transform {
        scaleX(scaleX)
        scaleY(scaleY)
        return this
    }

    override // from Transform
    fun scaleX(scaleX: Double): Transform {
        throw UnsupportedOperationException()
    }

    override // from Transform
    fun scaleY(scaleY: Double): Transform {
        throw UnsupportedOperationException()
    }

    override // from Transform
    fun rotate(angle: Double): Transform {
        throw UnsupportedOperationException()
    }

    override // from Transform
    fun translate(tx: Double, ty: Double): Transform {
        translateX(tx)
        translateY(ty)
        return this
    }

    override // from Transform
    fun translateX(tx: Double): Transform {
        throw UnsupportedOperationException()
    }

    override // from Transform
    fun translateY(ty: Double): Transform {
        throw UnsupportedOperationException()
    }

    override // from Transform
    fun shear(sx: Double, sy: Double): Transform {
        shearX(sx)
        shearY(sy)
        return this
    }

    override // from Transform
    fun shearX(sx: Double): Transform {
        throw UnsupportedOperationException()
    }

    override // from Transform
    fun shearY(sy: Double): Transform {
        throw UnsupportedOperationException()
    }

    override // from Transform
    fun setTx(tx: Double): Transform {
        throw UnsupportedOperationException()
    }

    override // from Transform
    fun setTy(ty: Double): Transform {
        throw UnsupportedOperationException()
    }

    override // from Transform
    fun setTransform(m00: Double, m01: Double, m10: Double, m11: Double, tx: Double, ty: Double): Transform {
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
