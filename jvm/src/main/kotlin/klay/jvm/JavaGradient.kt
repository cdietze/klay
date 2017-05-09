package klay.jvm

import klay.core.Gradient
import java.awt.Color
import java.awt.LinearGradientPaint
import java.awt.Paint
import java.awt.RadialGradientPaint
import java.awt.geom.Point2D

internal class JavaGradient private constructor(var paint: Paint) : Gradient() {
    companion object {

        fun create(cfg: Linear): JavaGradient {
            val start = Point2D.Float(cfg.x0, cfg.y0)
            val end = Point2D.Float(cfg.x1, cfg.y1)
            val javaColors = convertColors(cfg.colors)
            return JavaGradient(LinearGradientPaint(start, end, cfg.positions, javaColors))
        }

        fun create(cfg: Radial): JavaGradient {
            val center = Point2D.Float(cfg.x, cfg.y)
            val javaColors = convertColors(cfg.colors)
            return JavaGradient(RadialGradientPaint(center, cfg.r, cfg.positions, javaColors))
        }

        private fun convertColors(colors: IntArray): Array<Color> {
            return Array(colors.size, { Color(colors[it], true) })
        }
    }
}
