package tripleklay.game.trans

import klay.core.Platform
import tripleklay.game.ScreenStack.Screen

/**
 * Fades the new screen in front of the old one.
 */
class FadeTransition : InterpedTransition<SlideTransition>() {
    override fun init(plat: Platform, oscreen: Screen, nscreen: Screen) {
        super.init(plat, oscreen, nscreen)
        nscreen.layer.setAlpha(0f)
    }

    override fun update(oscreen: Screen, nscreen: Screen, elapsed: Float): Boolean {
        val nalpha = _interp.applyClamp(0f, 1f, elapsed, _duration)
        nscreen.layer.setAlpha(nalpha)
        return elapsed >= _duration
    }

    override fun complete(oscreen: Screen, nscreen: Screen) {
        super.complete(oscreen, nscreen)
        nscreen.layer.setAlpha(1f)
    }
}
