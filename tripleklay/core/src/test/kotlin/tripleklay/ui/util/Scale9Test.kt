package tripleklay.ui.util

import org.junit.Assert.assertEquals
import org.junit.Test
import tripleklay.ui.util.Scale9.Axis

class Scale9Test {

    @Test fun testAxis() {
        val axis = checkCoords(Axis(1f))
        assertEquals(1f / 3, axis.size(0), DELTA)
        assertEquals(1 - 2f / 3, axis.size(1), DELTA)
        assertEquals(1f / 3, axis.size(2), DELTA)
    }

    @Test fun testAxisResize() {
        val axis1 = checkCoords(Axis(1f).resize(0, .25f).resize(2, .25f))
        val axis2 = checkCoords(Axis(1f).resize(1, .5f))
        for (axis in arrayOf<Axis>(axis1, axis2)) {
            assertEquals(.25f, axis.size(0), DELTA)
            assertEquals(.5f, axis.size(1), DELTA)
            assertEquals(.25f, axis.size(2), DELTA)
        }
    }

    @Test fun testAxisDest() {
        var axis = checkCoords(Axis(1f, Axis(1f)))
        assertEquals(1f / 3, axis.size(0), DELTA)
        assertEquals(1 - 2f / 3, axis.size(1), DELTA)
        assertEquals(1f / 3, axis.size(2), DELTA)

        axis = checkCoords(Axis(2f, Axis(1f)))
        assertEquals(1f / 3, axis.size(0), DELTA)
        assertEquals(2 - 2f / 3, axis.size(1), DELTA)
        assertEquals(1f / 3, axis.size(2), DELTA)

        axis = checkCoords(Axis(.5f, Axis(1f)))
        assertEquals(1f / 3, axis.size(0), DELTA)
        assertEquals(.5f - 2f / 3, axis.size(1), DELTA)
        assertEquals(1f / 3, axis.size(2), DELTA)
    }

    @Test fun testAxisClamp() {
        var axis = checkCoords(Scale9.clamp(Axis(1f), 1f))
        assertEquals(1f / 3, axis.size(0), DELTA)
        assertEquals(1 - 2f / 3, axis.size(1), DELTA)
        assertEquals(1f / 3, axis.size(2), DELTA)

        axis = checkCoords(Scale9.clamp(Axis(2f).resize(1, 1.5f), 1f))
        assertEquals(.25f, axis.size(0), DELTA)
        assertEquals(.5f, axis.size(1), DELTA)
        assertEquals(.25f, axis.size(2), DELTA)

        axis = checkCoords(Scale9.clamp(Axis(1f), .5f))
        assertEquals(.25f, axis.size(0), DELTA)
        assertEquals(0f, axis.size(1), DELTA)
        assertEquals(.25f, axis.size(2), DELTA)
    }

    internal fun checkCoords(axis: Axis): Axis {
        assertEquals(axis.coord(0), 0f, DELTA)
        assertEquals(axis.coord(1), axis.size(0), DELTA)
        assertEquals(axis.coord(2), axis.size(0) + axis.size(1), DELTA)
        return axis
    }

    companion object {
        internal var DELTA = 1e-7.toFloat()
    }
}
