package tripleklay.demo.core.util

import klay.core.Clock
import klay.scene.ImageLayer
import klay.scene.Layer
import tripleklay.demo.core.DemoScreen
import tripleklay.ui.*
import tripleklay.ui.layout.TableLayout
import tripleklay.util.Interpolator

class InterpDemo : DemoScreen() {
    init {
        paint.connect({ clock: Clock ->
            for (driver in _drivers) if (driver!!.elapsed >= 0) driver.paint(clock)
        })
    }

    override fun name(): String {
        return "Interps"
    }

    override fun title(): String {
        return "Util: Interpolators"
    }

    override fun createIface(root: Root): Group {
        val grid = Group(TableLayout(TableLayout.COL.stretch().fixed(),
                TableLayout.COL).gaps(10, 10))
        val square = graphics().createCanvas(20f, 20f)
        square.setFillColor(0xFFFF0000.toInt()).fillRect(0f, 0f, 20f, 20f)
        val sqtex = square.toTexture()

        for (ii in INTERPS.indices) {
            val knob = ImageLayer(sqtex)
            val tray = Shim(300f, 20f)
            tray.addStyles(Style.BACKGROUND.`is`(Background.solid(0xFF666666.toInt())))
            tray.layer.add(knob)
            val driver = Driver(INTERPS[ii], knob)
            _drivers[ii] = driver
            grid.add(Button(INTERPS[ii].toString()).onClick({
                driver.elapsed = 0f
            }))
            grid.add(tray)
        }
        grid.add(Button("ALL").onClick({
            for (driver in _drivers) driver!!.elapsed = 0f
        }))
        return grid.addStyles(Style.BACKGROUND.`is`(Background.blank().inset(15f)))
    }

    protected fun demoInterp(interp: Interpolator, knob: Layer) {
        // TODO
    }

    protected inner class Driver(val interp: Interpolator, val knob: Layer) {
        var elapsed = -1f

        fun paint(clock: Clock) {
            if (elapsed > 2500) { // spend 500ms at max value
                knob.setTx(0f)
                elapsed = -1f
            } else {
                elapsed += clock.dt.toFloat()
                knob.setTx(interp.applyClamp(0f, 300f, elapsed, 2000f))
            }
        }
    }

    protected val INTERPS = arrayOf(Interpolator.LINEAR, Interpolator.EASE_IN, Interpolator.EASE_OUT, Interpolator.EASE_INOUT, Interpolator.EASE_IN_BACK, Interpolator.EASE_OUT_BACK, Interpolator.BOUNCE_OUT, Interpolator.EASE_OUT_ELASTIC)
    protected val _drivers = arrayOfNulls<Driver>(INTERPS.size)
}
