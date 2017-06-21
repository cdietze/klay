package tripleklay.ui.bgs

import klay.scene.GroupLayer
import klay.scene.Layer
import pythagoras.f.Dimension
import pythagoras.f.IDimension
import tripleklay.ui.Background
import tripleklay.ui.util.Insets

/**
 * A background consisting of multiple other backgrounds. Note: callers should not inset this
 * background since it derives all of its insets from the contained backgrounds and relies on the
 * values during instantiation.
 */
class CompositeBackground
/**
 * Creates a new composite background with the given constituents. The first background
 * is the outermost, the 2nd one is inside that and so on. The insets of this background
 * are set to the sum of the insets of the constituents.
 */
(vararg constituents: Background) : Background() {

    protected val _constituents: Array<out Background>
    protected var _reverseDepth: Boolean = false

    init {
        _constituents = constituents
        for (bg in constituents) {
            insets = insets.mutable().add(bg.insets)
        }
    }

    /**
     * Reverses the usual depth of the constituent backgrounds' layers. Normally the outermost
     * background's layer is lowest (rendered first). Use this method to render the innermost
     * background's layer first instead.
     */
    fun reverseDepth(): CompositeBackground {
        _reverseDepth = true
        return this
    }

    override fun instantiate(size: IDimension): Background.Instance {
        // we use one layer, and add the constituents to that
        val layer = GroupLayer()
        val instances = arrayOfNulls<Background.Instance>(_constituents.size)

        var current = Insets.ZERO
        run {
            var ii = 0
            val ll = _constituents.size
            while (ii < ll) {
                val bg = _constituents[ii]

                // create and save off the instance so we can dispose it later
                instances[ii] = Background.instantiate(bg, current.subtractFrom(Dimension(size)))

                // add to our composite layer and translate the layers added
                instances[ii]!!.addTo(layer, current.left(), current.top(), 0f)

                // adjust the bounds
                current = current.mutable().add(bg.insets)
                ii++
            }
        }

        if (_reverseDepth) {
            // simple reversal, if optimization is needed it would be better to simply
            // instantiate the backgrounds in reverse order above
            val temp = arrayOfNulls<Layer>(layer.children())
            var ii = 0
            val nn = temp.size
            while (ii < nn) {
                temp[ii] = layer.childAt(ii)
                ii++
            }
            var depth = 0f
            for (l in temp) {
                l!!.setDepth(depth)
                depth -= 1f
            }
        }

        return object : Background.LayerInstance(size, layer) {
            override fun close() {
                for (i in instances) i!!.close()
                super.close()
            }
        }
    }
}
