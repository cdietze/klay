package klay.tests.core

import klay.core.*
import klay.scene.CanvasLayer
import klay.scene.ImageLayer
import klay.scene.Layer
import pythagoras.f.FloatMath

class CanvasTest(game: TestsGame) : Test(game, "Canvas", "Tests various Canvas rendering features.") {
    private var nextX: Float = 0.toFloat()
    private var nextY: Float = 0.toFloat()
    private var maxY: Float = 0.toFloat()

    private var time: CanvasLayer? = null
    private var lastSecs: Int = 0

    override fun init() {
        nextY = GAP
        nextX = nextY
        lastSecs = -1

        addTestCanvas("radial fill gradient", 100, 100, object : Drawer {
            override fun draw(canvas: Canvas) {
                val cfg = Gradient.Radial(
                        0f, 0f, 50f, intArrayOf(0xFFFF0000.toInt(), 0xFF00FF00.toInt()), floatArrayOf(0f, 1f))
                canvas.setFillGradient(canvas.createGradient(cfg))
                canvas.fillRect(0f, 0f, 100f, 100f)
            }
        })

        addTestCanvas("linear fill gradient", 100, 100, object : Drawer {
            override fun draw(canvas: Canvas) {
                val cfg = Gradient.Linear(
                        0f, 0f, 100f, 100f, intArrayOf(0xFF0000FF.toInt(), 0xFF00FF00.toInt()), floatArrayOf(0f, 1f))
                canvas.setFillGradient(canvas.createGradient(cfg))
                canvas.fillRect(0f, 0f, 100f, 100f)
            }
        })

        //TODO(cdi) re-add once Assets work
//        addTestCanvas("image fill pattern", 100, 100, "images/tile.png", object : ImageDrawer {
//            override fun draw(canvas: Canvas, tile: Image) {
//                canvas.setFillPattern(tile.createPattern(true, true))
//                canvas.fillRect(0f, 0f, 100f, 100f)
//            }
//        })

        addTestCanvas("lines and circles", 100, 100, object : Drawer {
            override fun draw(canvas: Canvas) {
                canvas.setFillColor(0xFF99CCFF.toInt())
                canvas.fillRect(0f, 0f, 100f, 100f)
                // draw a point and some lines
                canvas.setStrokeColor(0xFFFF0000.toInt())
                canvas.drawPoint(50f, 50f)
                canvas.drawLine(0f, 25f, 100f, 25f)
                canvas.drawLine(0f, 75f, 100f, 75f)
                canvas.drawLine(25f, 0f, 25f, 100f)
                canvas.drawLine(75f, 0f, 75f, 100f)
                // stroke and fill a circle
                canvas.strokeCircle(25f, 75f, 10f)
                canvas.setFillColor(0xFF0000FF.toInt())
                canvas.fillCircle(75f, 75f, 10f)
            }
        })

        //TODO(cdi) re-add once Assets work
//        addTestCanvas("image, subimage", 100, 100, "images/orange.png", object : ImageDrawer {
//            override fun draw(canvas: Canvas, orange: Image) {
//                canvas.setFillColor(0xFF99CCFF.toInt())
//                canvas.fillRect(0f, 0f, 100f, 100f)
//
//                // draw an image normally, scaled, cropped, cropped and scaled, etc.
//                val half = 37 / 2f
//                canvas.draw(orange, 10f, 10f)
//                canvas.draw(orange, 55f, 10f, 37f, 37f, half, half, half, half)
//                canvas.draw(orange, 10f, 55f, 37f, 37f, half, 0f, half, half)
//                canvas.draw(orange, 55f, 55f, 37f, 37f, half, half / 2, half, half)
//            }
//        })

        val repcan = createCanvas(30, 30, object : Drawer {
            override fun draw(canvas: Canvas) {
                canvas.setFillColor(0xFF99CCFF.toInt()).fillCircle(15f, 15f, 15f)
                canvas.setStrokeColor(0xFF000000.toInt()).strokeRect(0f, 0f, 30f, 30f)
            }
        })
        val repeat = Texture.Config.DEFAULT.repeat(true, true)
        val reptex = repcan.toTexture(repeat)
        addTestLayer("ImageLayer repeat x/y", 100, 100, ImageLayer(reptex).setSize(100f, 100f))

        time = CanvasLayer(game.graphics, 100f, 100f)
        addTestLayer("updated canvas", 100, 100, time!!)

        val linear = repcan.createGradient(Gradient.Linear(
                0f, 0f, 100f, 100f, intArrayOf(0xFF0000FF.toInt(), 0xFF00FF00.toInt()), floatArrayOf(0f, 1f)))
        val dotRadius = 40f
        val radial = repcan.createGradient(Gradient.Radial(
                100 / 3f, 100 / 2.5f, dotRadius, intArrayOf(0xFFFFFFFF.toInt(), 0xFFCC66FF.toInt()), floatArrayOf(0f, 1f)))

        addTestCanvas("filled bezier path", 100, 100, object : Drawer {
            override fun draw(canvas: Canvas) {
                // draw a rounded rect with bezier curves
                val path = canvas.createPath()
                path.moveTo(10f, 0f)
                path.lineTo(90f, 0f)
                path.bezierTo(95f, 0f, 100f, 5f, 100f, 10f)
                path.lineTo(100f, 90f)
                path.bezierTo(100f, 95f, 95f, 100f, 90f, 100f)
                path.lineTo(10f, 100f)
                path.bezierTo(5f, 100f, 0f, 95f, 0f, 90f)
                path.lineTo(0f, 10f)
                path.bezierTo(0f, 5f, 5f, 0f, 10f, 0f)
                path.close()
                canvas.setFillGradient(linear).fillPath(path)
            }
        })

        addTestCanvas("gradient round rect", 100, 100, object : Drawer {
            override fun draw(canvas: Canvas) {
                // draw a rounded rect directly
                canvas.setFillGradient(linear).fillRoundRect(0f, 0f, 100f, 100f, 10f)
            }
        })

        addTestCanvas("gradient filled text", 100, 100, object : Drawer {
            override fun draw(canvas: Canvas) {
                // draw a rounded rect directly
                canvas.setFillGradient(linear)
                val capF = game.graphics.layoutText("F", TextFormat(F_FONT.derive(96f)))
                canvas.fillText(capF, 15f, 5f)
            }
        })

        addTestCanvas("nested round rect", 100, 100, object : Drawer {
            override fun draw(canvas: Canvas) {
                // demonstrates a bug (now worked around) in Android round-rect drawing
                canvas.setFillColor(0xFFFFCC99.toInt()).fillRoundRect(0f, 0f, 98.32f, 29.5f, 12f)
                canvas.setFillColor(0xFF99CCFF.toInt()).fillRoundRect(3f, 3f, 92.32f, 23.5f, 9.5f)
            }
        })

        addTestCanvas("android fill/stroke bug", 100, 100, object : Drawer {
            override fun draw(canvas: Canvas) {
                canvas.save()
                canvas.setFillGradient(radial).fillCircle(50f, 50f, dotRadius)
                canvas.restore()
                canvas.setStrokeColor(0xFF000000.toInt()).setStrokeWidth(1.5f).strokeCircle(50f, 50f, dotRadius)
            }
        })

        addTestCanvas("transform test", 100, 100, object : Drawer {
            override fun draw(canvas: Canvas) {
                canvas.setFillColor(0xFFCCCCCC.toInt()).fillRect(0f, 0f, 50f, 50f)
                canvas.setFillColor(0xFFCCCCCC.toInt()).fillRect(50f, 50f, 50f, 50f)
                val capF = game.graphics.layoutText("F", TextFormat(F_FONT))
                val theta = -FloatMath.PI / 4
                val tsin = FloatMath.sin(theta)
                val tcos = FloatMath.cos(theta)
                canvas.setFillColor(0xFF000000.toInt()).fillText(capF, 0f, 0f)
                canvas.transform(tcos, -tsin, tsin, tcos, 50f, 50f)
                canvas.setFillColor(0xFF000000.toInt()).fillText(capF, 0f, 0f)
            }
        })

        addTestCanvas("round rect precision", 100, 100, object : Drawer {
            internal var bwid = 4f
            internal fun outer(canvas: Canvas, y: Float) {
                canvas.setFillColor(0xFF000000.toInt())
                canvas.fillRect(2f, y, 94f, 30f)
            }

            internal fun inner(canvas: Canvas, y: Float) {
                canvas.setFillColor(0xFF555555.toInt())
                canvas.fillRect(2 + bwid, y + bwid, 94 - bwid * 2, 30 - bwid * 2)
            }

            internal fun stroke(canvas: Canvas, y: Float) {
                canvas.setStrokeColor(0xFF99CCFF.toInt())
                canvas.setStrokeWidth(bwid)
                canvas.strokeRoundRect(2 + bwid / 2, y + bwid / 2, 94 - bwid, 30 - bwid, 10f)
            }

            override fun draw(canvas: Canvas) {
                var y = 1f
                outer(canvas, y)
                inner(canvas, y)
                stroke(canvas, y)

                y += 34f
                outer(canvas, y)
                stroke(canvas, y)
                inner(canvas, y)

                y += 34f
                stroke(canvas, y)
                outer(canvas, y)
                inner(canvas, y)
            }
        })

        //TODO(cdi) re-add once Assets work
//        val tileLayer = ImageLayer(
//                game.assets.getImage("images/tile.png").setConfig(repeat))
//        addTestLayer("img layer anim setWidth", 100, 100, tileLayer.setSize(0f, 100f))
//        conns.add<Connection>(game.paint.connect({ clock: Clock ->
//            val curSecs = clock.tick / 1000
//            if (curSecs != lastSecs) {
//                val tcanvas = time!!.begin()
//                tcanvas.clear()
//                tcanvas.setStrokeColor(0xFF000000.toInt()).strokeRect(0f, 0f, 99f, 99f)
//                tcanvas.drawText("" + curSecs, 40f, 55f)
//                lastSecs = curSecs
//                time!!.end()
//            }
//
//            // round the width so that it goes to zero sometimes (which should be fine)
//            if (tileLayer != null)
//                tileLayer.forceWidth = Math.round(
//                        Math.abs(FloatMath.sin(clock.tick / 2000f)) * 100f).toFloat()
//        }))

        val cancan = createCanvas(50, 50, object : Drawer {
            override fun draw(canvas: Canvas) {
                canvas.setFillGradient(radial).fillRect(0f, 0f, canvas.width, canvas.height)
            }
        })
        addTestCanvas("canvas drawn on canvas", 100, 100, object : Drawer {
            override fun draw(canvas: Canvas) {
                canvas.translate(50f, 25f)
                canvas.rotate(FloatMath.PI / 4)
                canvas.draw(cancan.image, 0f, 0f)
            }
        })
    }

    private interface Drawer {
        fun draw(canvas: Canvas)
    }

    private fun addTestCanvas(descrip: String, width: Int, height: Int, drawer: Drawer) {
        val canvas = createCanvas(width, height, drawer)
        addTestLayer(descrip, width, height, ImageLayer(canvas.toTexture()))
    }

    private fun createCanvas(width: Int, height: Int, drawer: Drawer): Canvas {
        val canvas = game.graphics.createCanvas(width.toFloat(), height.toFloat())
        drawer.draw(canvas)
        return canvas
    }

    private fun addTestLayer(descrip: String, width: Int, height: Int, layer: Layer) {
        // if this layer won't fit in this row, wrap down to the next
        if (nextX + width > game.graphics.viewSize.width) {
            nextY += maxY + GAP
            nextX = GAP
            maxY = 0f
        }

        // add the layer and its description below
        game.rootLayer.addAt(layer, nextX, nextY)
        val dlayer = createDescripLayer(descrip, width.toFloat())
        game.rootLayer.addAt(dlayer, nextX + Math.round((width - dlayer.width()) / 2),
                nextY + height.toFloat() + 2f)

        // update our positioning info
        nextX += width + GAP
        maxY = Math.max(maxY, height + dlayer.height() + 2)
    }

    private interface ImageDrawer {
        fun draw(canvas: Canvas, image: Image)
    }

    //TODO(cdi) re-add once Assets work
//    private fun addTestCanvas(descrip: String, width: Int, height: Int, imagePath: String,
//                              drawer: ImageDrawer) {
//        val target = game.graphics.createCanvas(width.toFloat(), height.toFloat())
//        val layer = ImageLayer().setSize(width.toFloat(), height.toFloat())
//        game.assets.getImage(imagePath).state.onSuccess({ image: Image ->
//            drawer.draw(target, image)
//            layer.setTile(target.toTexture())
//        })
//        addTestLayer(descrip, width, height, layer)
//    }

    private val F_FONT = Font("Helvetica", Font.Style.BOLD, 48f)

    companion object {

        private val GAP = 10f
    }
}
