package klay.jvm

import klay.core.*
import pythagoras.f.MathUtil
import java.awt.Color
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.*
import java.awt.image.BufferedImage
import java.util.*

internal class JavaCanvas(gfx: Graphics, image: JavaImage) : Canvas(gfx, image) {

    val g2d: Graphics2D
    private val stateStack = LinkedList<JavaCanvasState>()

    private val ellipse = Ellipse2D.Float()
    private val line = Line2D.Float()
    private val rect = Rectangle2D.Float()
    private val roundRect = RoundRectangle2D.Float()

    init {

        g2d = image.bufferedImage()!!.createGraphics()
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        val scale = image.scale.factor
        g2d.scale(scale.toDouble(), scale.toDouble())

        // push default state
        stateStack.push(JavaCanvasState())

        // All clears go to rgba(0,0,0,0).
        g2d.background = Color(0, true)
    }

    fun alpha(): Float {
        return currentState().alpha
    }

    override fun snapshot(): Image {
        val bmp = (image as JavaImage).bufferedImage()!!
        val cm = bmp.colorModel
        val isAlphaPremultiplied = bmp.isAlphaPremultiplied
        val raster = bmp.copyData(null)
        val snap = BufferedImage(cm, raster, isAlphaPremultiplied, null)
        return JavaImage(gfx, image.scale, snap, "<canvas>")
    }

    override fun clear(): Canvas {
        currentState().prepareClear(g2d)
        g2d.clearRect(0, 0, MathUtil.iceil(width), MathUtil.iceil(height))
        isDirty = true
        return this
    }

    override fun clearRect(x: Float, y: Float, width: Float, height: Float): Canvas {
        currentState().prepareClear(g2d)
        g2d.clearRect(MathUtil.ifloor(x), MathUtil.ifloor(y),
                MathUtil.iceil(width), MathUtil.iceil(height))
        isDirty = true
        return this
    }

    override fun clip(path: Path): Canvas {
        currentState().clipper = path as JavaPath
        return this
    }

    override fun clipRect(x: Float, y: Float, width: Float, height: Float): Canvas {
        val cx = MathUtil.ifloor(x)
        val cy = MathUtil.ifloor(y)
        val cwidth = MathUtil.iceil(width)
        val cheight = MathUtil.iceil(height)
        currentState().clipper = object:JavaCanvasState.Clipper {
            override fun setClip(gfx: Graphics2D) {
                gfx.setClip(cx, cy, cwidth, cheight)
            }
        }
        return this
    }

    override fun createPath(): Path {
        return JavaPath()
    }

    override fun createGradient(cfg: Gradient.Config): Gradient {
        if (cfg is Gradient.Linear)
            return JavaGradient.create(cfg as Gradient.Linear)
        else if (cfg is Gradient.Radial)
            return JavaGradient.create(cfg as Gradient.Radial)
        else
            throw IllegalArgumentException("Unknown config: " + cfg)
    }

    override fun drawLine(x0: Float, y0: Float, x1: Float, y1: Float): Canvas {
        currentState().prepareStroke(g2d)
        line.setLine(x0, y0, x1, y1)
        g2d.draw(line)
        isDirty = true
        return this
    }

    override fun drawPoint(x: Float, y: Float): Canvas {
        currentState().prepareStroke(g2d)
        g2d.drawLine(x.toInt(), y.toInt(), x.toInt(), y.toInt())
        isDirty = true
        return this
    }

    override fun drawText(text: String, x: Float, y: Float): Canvas {
        currentState().prepareFill(g2d)
        g2d.drawString(text, x, y)
        isDirty = true
        return this
    }

    override fun fillCircle(x: Float, y: Float, radius: Float): Canvas {
        currentState().prepareFill(g2d)
        ellipse.setFrame(x - radius, y - radius, 2 * radius, 2 * radius)
        g2d.fill(ellipse)
        isDirty = true
        return this
    }

    override fun fillPath(path: Path): Canvas {
        currentState().prepareFill(g2d)
        g2d.fill((path as JavaPath).path)
        isDirty = true
        return this
    }

    override fun fillRect(x: Float, y: Float, width: Float, height: Float): Canvas {
        currentState().prepareFill(g2d)
        rect.setRect(x, y, width, height)
        g2d.fill(rect)
        isDirty = true
        return this
    }

    override fun fillRoundRect(x: Float, y: Float, width: Float, height: Float, radius: Float): Canvas {
        currentState().prepareFill(g2d)
        roundRect.setRoundRect(x, y, width, height, radius * 2, radius * 2)
        g2d.fill(roundRect)
        isDirty = true
        return this
    }

    override fun fillText(layout: TextLayout, x: Float, y: Float): Canvas {
        currentState().prepareFill(g2d)
        (layout as JavaTextLayout).fill(g2d, x, y)
        isDirty = true
        return this
    }

    override fun restore(): Canvas {
        stateStack.pop()
        g2d.transform = currentState().transform
        return this
    }

    override fun rotate(angle: Float): Canvas {
        g2d.rotate(angle.toDouble())
        return this
    }

    override fun save(): Canvas {
        // update saved transform
        currentState().transform = g2d.transform

        // clone to maintain current state
        stateStack.push(JavaCanvasState(currentState()))
        return this
    }

    override fun scale(x: Float, y: Float): Canvas {
        g2d.scale(x.toDouble(), y.toDouble())
        return this
    }

    override fun setAlpha(alpha: Float): Canvas {
        currentState().alpha = alpha
        return this
    }

    override fun setCompositeOperation(composite: Composite): Canvas {
        currentState().composite = composite
        return this
    }

    override fun setFillColor(color: Int): Canvas {
        currentState().fillColor = color
        currentState().fillGradient = null
        currentState().fillPattern = null
        return this
    }

    override fun setFillGradient(gradient: Gradient): Canvas {
        currentState().fillGradient = gradient as JavaGradient
        currentState().fillPattern = null
        currentState().fillColor = 0
        return this
    }

    override fun setFillPattern(pattern: Pattern): Canvas {
        currentState().fillPattern = pattern as JavaPattern
        currentState().fillGradient = null
        currentState().fillColor = 0
        return this
    }

    override fun setLineCap(cap: LineCap): Canvas {
        currentState().lineCap = cap
        return this
    }

    override fun setLineJoin(join: LineJoin): Canvas {
        currentState().lineJoin = join
        return this
    }

    override fun setMiterLimit(miter: Float): Canvas {
        currentState().miterLimit = miter
        return this
    }

    override fun setStrokeColor(color: Int): Canvas {
        currentState().strokeColor = color
        return this
    }

    override fun setStrokeWidth(w: Float): Canvas {
        currentState().strokeWidth = w
        return this
    }

    override fun strokeCircle(x: Float, y: Float, radius: Float): Canvas {
        currentState().prepareStroke(g2d)
        ellipse.setFrame(x - radius, y - radius, 2 * radius, 2 * radius)
        g2d.draw(ellipse)
        isDirty = true
        return this
    }

    override fun strokePath(path: Path): Canvas {
        currentState().prepareStroke(g2d)
        g2d.color = Color(currentState().strokeColor, false)
        g2d.draw((path as JavaPath).path)
        isDirty = true
        return this
    }

    override fun strokeRect(x: Float, y: Float, width: Float, height: Float): Canvas {
        currentState().prepareStroke(g2d)
        rect.setRect(x, y, width, height)
        g2d.draw(rect)
        isDirty = true
        return this
    }

    override fun strokeRoundRect(x: Float, y: Float, width: Float, height: Float, radius: Float): Canvas {
        currentState().prepareStroke(g2d)
        roundRect.setRoundRect(x, y, width, height, radius * 2, radius * 2)
        g2d.draw(roundRect)
        isDirty = true
        return this
    }

    override fun strokeText(layout: TextLayout, x: Float, y: Float): Canvas {
        currentState().prepareStroke(g2d)
        (layout as JavaTextLayout).stroke(g2d, x, y)
        isDirty = true
        return this
    }

    override fun transform(m11: Float, m12: Float, m21: Float, m22: Float, dx: Float, dy: Float): Canvas {
        g2d.transform(AffineTransform(m11, m12, m21, m22, dx, dy))
        return this
    }

    override fun translate(x: Float, y: Float): Canvas {
        g2d.translate(x.toDouble(), y.toDouble())
        return this
    }

    override fun gc(): Graphics2D {
        currentState().prepareFill(g2d)
        return g2d
    }

    private fun currentState(): JavaCanvasState {
        return stateStack.first
    }
}
