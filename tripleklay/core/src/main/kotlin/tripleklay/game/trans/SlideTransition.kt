package tripleklay.game.trans

import klay.core.Platform
import tripleklay.game.ScreenStack
import tripleklay.game.ScreenStack.Screen

/**
 * Slides the old screen off, and the new screen on right behind.
 */
class SlideTransition(stack: ScreenStack) : InterpedTransition<SlideTransition>() {

    protected val _originX: Float = stack.originX
    protected val _originY: Float = stack.originY
    protected var _dir: ScreenStack.Transition.Dir = ScreenStack.Transition.Dir.LEFT
    protected var _osx: Float = 0.toFloat()
    protected var _osy: Float = 0.toFloat()
    protected var _odx: Float = 0.toFloat()
    protected var _ody: Float = 0.toFloat()
    protected var _nsx: Float = 0.toFloat()
    protected var _nsy: Float = 0.toFloat()

    fun up(): SlideTransition {
        return dir(ScreenStack.Transition.Dir.UP)
    }

    fun down(): SlideTransition {
        return dir(ScreenStack.Transition.Dir.DOWN)
    }

    fun left(): SlideTransition {
        return dir(ScreenStack.Transition.Dir.LEFT)
    }

    fun right(): SlideTransition {
        return dir(ScreenStack.Transition.Dir.RIGHT)
    }

    fun dir(dir: ScreenStack.Transition.Dir): SlideTransition {
        _dir = dir
        return this
    }

    override fun init(plat: Platform, oscreen: Screen, nscreen: Screen) {
        super.init(plat, oscreen, nscreen)
        when (_dir) {
            ScreenStack.Transition.Dir.UP -> {
                _odx = _originX
                _ody = _originY - oscreen.size().height
                _nsx = _originX
                _nsy = _originY + nscreen.size().height
            }
            ScreenStack.Transition.Dir.DOWN -> {
                _odx = _originX
                _ody = _originY + oscreen.size().height
                _nsx = _originX
                _nsy = _originY - nscreen.size().height
            }
            ScreenStack.Transition.Dir.LEFT -> {
                _odx = _originX - oscreen.size().width
                _ody = _originY
                _nsx = _originX + nscreen.size().width
                _nsy = _originY
            }
            ScreenStack.Transition.Dir.RIGHT -> {
                _odx = _originX + oscreen.size().width
                _ody = _originY
                _nsx = _originX - nscreen.size().width
                _nsy = _originY
            }
        }
        _osx = oscreen.layer.tx()
        _osy = oscreen.layer.ty()
        nscreen.layer.setTranslation(_nsx, _nsy)
    }

    override fun update(oscreen: Screen, nscreen: Screen, elapsed: Float): Boolean {
        val ox = _interp.applyClamp(_originX, _odx - _originX, elapsed, _duration)
        val oy = _interp.applyClamp(_originY, _ody - _originY, elapsed, _duration)
        oscreen.layer.setTranslation(ox, oy)
        val nx = _interp.applyClamp(_nsx, _originX - _nsx, elapsed, _duration)
        val ny = _interp.applyClamp(_nsy, _originY - _nsy, elapsed, _duration)
        nscreen.layer.setTranslation(nx, ny)
        return elapsed >= _duration
    }

    override fun complete(oscreen: Screen, nscreen: Screen) {
        super.complete(oscreen, nscreen)
        oscreen.layer.setTranslation(_osx, _osy)
    }

    override fun defaultDuration(): Float {
        return 500f
    }
}
