package tripleklay.ui

import klay.scene.Layer
import react.RFuture

/**
 * Used to apply effects to an Icon.
 */
abstract class IconEffect {

    /** Does the needful.  */
    abstract fun apply(icon: Icon): Icon

    /** Wrap an Icon for fiddling.  */
    protected open class Proxy protected constructor(protected val _icon: Icon) : Icon {
        override fun width(): Float {
            return _icon.width()
        }

        override fun height(): Float {
            return _icon.height()
        }

        override fun render(): Layer {
            return _icon.render()
        }

        override fun state(): RFuture<Icon> {
            return _icon.state()
        }
    }

    companion object {
        /** Leaves well enough alone.  */
        val NONE: IconEffect = object : IconEffect() {
            override fun apply(icon: Icon): Icon {
                return icon
            }
        }

        /**
         * Creates an IconEffect that sets the alpha on the Icon's created layer.
         */
        fun alpha(alpha: Float): IconEffect {
            return object : IconEffect() {
                override fun apply(icon: Icon): Icon {
                    return object : Proxy(icon) {
                        override fun render(): Layer {
                            return super.render().setAlpha(alpha)
                        }
                    }
                }
            }
        }
    }
}
