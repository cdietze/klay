package tripleklay.ui.bgs

import klay.scene.GroupLayer
import euklid.f.IDimension
import tripleklay.ui.Background

/**
 * A background that displays nothing. This is the default for groups.
 */
class BlankBackground : Background() {
    override fun instantiate(size: IDimension): Background.Instance {
        return object : Background.Instance(size) {
            override fun addTo(parent: GroupLayer, x: Float, y: Float, depthAdjust: Float) {}
            override fun close() {}
        }
    }
}
