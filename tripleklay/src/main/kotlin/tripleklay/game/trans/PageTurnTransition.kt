package tripleklay.game.trans

import klay.core.Platform
import klay.core.Surface
import klay.scene.Layer
import euklid.f.MathUtil
import tripleklay.game.ScreenStack.Screen
import tripleklay.shaders.RotateYBatch
import tripleklay.util.Interpolator

/**
 * Opens the current screen like the page of a book, revealing the new screen beneath.
 */
class PageTurnTransition : InterpedTransition<PageTurnTransition>() {

    private var _alpha: Float = 0.toFloat()
    private var _close: Boolean = false

    private lateinit var _toflip: Screen
    private lateinit var _shadow: Layer
    private lateinit var _batch: RotateYBatch

    /**
     * Reverses this transition, making it a page close instead of open. Note that this changes the
     * interpolator, so if you want a custom interpolator, configure it *after* calling this
     * method.
     */
    fun close(): PageTurnTransition {
        _close = true
        _interp = Interpolator.EASE_INOUT
        return this
    }

    override fun init(plat: Platform, oscreen: Screen, nscreen: Screen) {
        super.init(plat, oscreen, nscreen)
        nscreen.layer.setDepth((if (_close) 1 else -1).toFloat())
        _toflip = if (_close) nscreen else oscreen
        _batch = RotateYBatch(plat.graphics.gl, 0f, 0.5f, 1.5f)
        _toflip.layer.setBatch(_batch)
        val fwidth = _toflip.size().width
        val fheight = _toflip.size().height
        _shadow = object : Layer() {
            override fun paintImpl(surf: Surface) {
                surf.setAlpha(_alpha).setFillColor(0xFF000000.toInt()).fillRect(0f, 0f, fwidth / 4, fheight)
            }
        }
        _toflip.layer.addAt(_shadow, fwidth, 0f)
        updateAngle(0f) // start things out appropriately
    }

    override fun update(oscreen: Screen, nscreen: Screen, elapsed: Float): Boolean {
        updateAngle(elapsed)
        return elapsed >= _duration
    }

    override fun complete(oscreen: Screen, nscreen: Screen) {
        super.complete(oscreen, nscreen)
        _shadow.close()
        nscreen.layer.setDepth(0f)
        _toflip.layer.setBatch(null)
    }

    override fun defaultDuration(): Float {
        return 1500f
    }

    override fun defaultInterpolator(): Interpolator {
        return Interpolator.EASE_IN
    }

    private fun updateAngle(elapsed: Float) {
        var pct = _interp.applyClamp(0f, 0.5f, elapsed, _duration)
        if (_close) pct = 0.5f - pct
        _alpha = pct
        _batch.angle = MathUtil.PI * pct
    }
}
