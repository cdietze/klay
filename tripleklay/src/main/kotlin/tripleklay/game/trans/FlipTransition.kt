package tripleklay.game.trans

import klay.core.Platform
import pythagoras.f.MathUtil
import tripleklay.game.ScreenStack.Screen
import tripleklay.shaders.RotateYBatch
import tripleklay.util.Interpolator

/**
 * Flips the current screen over, revealing the new screen as if it were on the reverse side of the
 * current screen.
 */
class FlipTransition : InterpedTransition<FlipTransition>() {

    private var _flipped: Boolean = false
    private var _unflip: Boolean = false
    private lateinit var _obatch: RotateYBatch
    private lateinit var _nbatch: RotateYBatch

    /** Reverses this transition, making it flip the other direction.  */
    fun unflip(): FlipTransition {
        _unflip = true
        return this
    }

    override fun init(plat: Platform, oscreen: Screen, nscreen: Screen) {
        super.init(plat, oscreen, nscreen)
        nscreen.layer.setDepth(-1f)
        _obatch = RotateYBatch(plat.graphics.gl, 0.5f, 0.5f, 1f)
        oscreen.layer.setBatch(_obatch)
        _nbatch = RotateYBatch(plat.graphics.gl, 0.5f, 0.5f, 1f)
        nscreen.layer.setBatch(_nbatch)
    }

    override fun update(oscreen: Screen, nscreen: Screen, elapsed: Float): Boolean {
        var pct = _interp.applyClamp(0f, 1f, elapsed, _duration)
        if (pct >= 0.5f && !_flipped) {
            nscreen.layer.setDepth(0f)
            oscreen.layer.setDepth(-1f)
            _flipped = true
        }
        if (_unflip) pct = -pct
        _obatch.angle = MathUtil.PI * pct
        _nbatch.angle = MathUtil.PI * (pct - 1)
        return elapsed >= _duration
    }

    override fun complete(oscreen: Screen, nscreen: Screen) {
        super.complete(oscreen, nscreen)
        oscreen.layer.setDepth(0f)
        oscreen.layer.setBatch(null)
        nscreen.layer.setDepth(0f)
        nscreen.layer.setBatch(null)
    }

    override fun defaultInterpolator(): Interpolator {
        return Interpolator.LINEAR
    }
}
