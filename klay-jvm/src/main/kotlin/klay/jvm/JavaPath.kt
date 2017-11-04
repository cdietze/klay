package klay.jvm

import klay.core.Path
import java.awt.Graphics2D
import java.awt.geom.GeneralPath
import java.awt.geom.Path2D

internal class JavaPath : Path, JavaCanvasState.Clipper {

    var path: Path2D = GeneralPath()

    override fun reset(): Path {
        path.reset()
        return this
    }

    override fun close(): Path {
        path.closePath()
        return this
    }

    override fun moveTo(x: Float, y: Float): Path {
        path.moveTo(x.toDouble(), y.toDouble())
        return this
    }

    override fun lineTo(x: Float, y: Float): Path {
        path.lineTo(x.toDouble(), y.toDouble())
        return this
    }

    override fun quadraticCurveTo(cpx: Float, cpy: Float, x: Float, y: Float): Path {
        path.quadTo(cpx.toDouble(), cpy.toDouble(), x.toDouble(), y.toDouble())
        return this
    }

    override fun bezierTo(c1x: Float, c1y: Float, c2x: Float, c2y: Float, x: Float, y: Float): Path {
        path.curveTo(c1x.toDouble(), c1y.toDouble(), c2x.toDouble(), c2y.toDouble(), x.toDouble(), y.toDouble())
        return this
    }

    override fun setClip(gfx: Graphics2D) {
        gfx.clip = path
    }
}
