package klay.tests.core

import euklid.f.MathUtil
import klay.core.Clock
import klay.core.Gradient
import klay.core.Surface
import klay.scene.ClippedLayer
import klay.scene.GroupLayer
import klay.scene.ImageLayer
import klay.scene.Layer
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.round
import kotlin.math.sin

class ClippedLayerTest(game: TestsGame) : Test(game, "ClippedLayer", "Tests rendering of layers with and without clipping. Clipped layers " + "should not overdraw one pixel black lines that circumscribes them.") {

    private var elapsed: Float = 0.toFloat()
    private var rotation: Float = 0.toFloat()

    override fun init() {
        addClippedLayers()
        addClippedGroupLayers()
    }

    private fun addClippedLayers() {
        val circle = game.graphics.createCanvas(100f, 100f)
        circle.setFillColor(0xFFCC99FF.toInt()).fillCircle(50f, 50f, 50f)
        val cirtex = circle.toTexture()

        val sausage = game.graphics.createCanvas(100f, 50f)
        val linear = sausage.createGradient(Gradient.Linear(
                0f, 0f, 100f, 100f, intArrayOf(0xFF0000FF.toInt(), 0xFF00FF00.toInt()), floatArrayOf(0f, 1f)))
        sausage.setFillGradient(linear).fillRoundRect(0f, 0f, 100f, 50f, 10f)
        val saustex = sausage.toTexture()

        // add an unclipped layer which will draw our background and outlines
        game.rootLayer.add(object : Layer() {
            override fun paintImpl(surf: Surface) {
                surf.setFillColor(0xFFFFCC99.toInt()).fillRect(
                        0f, 0f, game.graphics.viewSize.width, game.graphics.viewSize.height)

                // fill a rect that will be covered except for one pixel by the clipped immediate layers
                surf.setFillColor(0xFF000000.toInt())
                surf.fillRect(29f, 29f, 152f, 152f)
                surf.fillRect(259f, 29f, 102f, 102f)
                surf.fillRect(389f, 29f, 102f, 102f)
            }
        })

        // add a clipped layer that will clip a fill and image draw
        val clayer = object : ClippedLayer(150f, 150f) {
            override fun paintClipped(surf: Surface) {
                // this fill should be clipped to our bounds
                surf.setFillColor(0xFF99CCFF.toInt())
                surf.fillRect(-50f, -50f, 200f, 200f)
                // and this image should be clipped to our bounds
                surf.draw(cirtex, 80f, -25f)
            }
        }
        // adjust the origin to ensure that is accounted for in the clipping
        game.rootLayer.addAt(clayer.setOrigin(100f, 100f), 130f, 130f)

        // add a clipped layer that draws an image through a rotation transform
        game.rootLayer.addAt(object : ClippedLayer(100f, 100f) {
            override fun paintClipped(surf: Surface) {
                surf.setFillColor(0xFF99CCFF.toInt()).fillRect(0f, 0f, 100f, 100f)
                surf.translate(50f, 50f).rotate(rotation).translate(-50f, -50f)
                surf.draw(saustex, 0f, 25f)
            }
        }, 260f, 30f)

        // add a clipped layer that draws an image through a translation transform
        game.rootLayer.addAt(object : ClippedLayer(100f, 100f) {
            override fun paintClipped(surf: Surface) {
                surf.setFillColor(0xFF99CCFF.toInt()).fillRect(0f, 0f, 100f, 100f)
                surf.translate(sin(elapsed) * 50, cos(elapsed) * 50 + 25)
                surf.draw(saustex, 0f, 0f)
            }
        }, 390f, 30f)

        conns.add(game.paint.connect { clock: Clock ->
            elapsed = clock.tick / 1000f
            rotation = elapsed * MathUtil.PI / 2
        })
    }

    private fun addClippedGroupLayers() {
        val iwidth = 100f
        val iheight = 50f
        val img = game.graphics.createCanvas(iwidth, iheight)
        val linear = img.createGradient(Gradient.Linear(
                0f, 0f, 100f, 100f, intArrayOf(0xFF0000FF.toInt(), 0xFF00FF00.toInt()), floatArrayOf(0f, 1f)))
        img.setFillGradient(linear).fillRoundRect(0f, 0f, 100f, 50f, 10f)
        val tex = img.toTexture()

        // create a group layer with a static clip, and a rotating image inside
        val g1 = GroupLayer(100f, 100f)
        // test the origin not being at zero/zero
        g1.setOrigin(50f, 0f)
        val i1 = ImageLayer(tex)
        i1.setOrigin(i1.width() / 2, i1.height() / 2)
        g1.addAt(i1, 50f, 50f)

        // static image inside and animated clipped width
        val g2 = GroupLayer(100f, 100f)
        g2.setOrigin(50f, 50f)
        g2.addAt(ImageLayer(tex), (100 - iwidth) / 2, (100 - iheight) / 2)

        // nest a group layer inside with an animated origin
        val inner = GroupLayer()
        inner.addAt(ImageLayer(tex), (100 - iwidth) / 2, (100 - iheight) / 2)
        val g3 = GroupLayer(100f, 100f)
        g3.add(inner)

        // create a group layer with a static clip, and a rotating surface image inside
        val g4 = GroupLayer(100f, 100f)
        val si = game.createSurface(100f, 50f)
        si.begin().setFillColor(0xFF99CCFF.toInt()).fillRect(0f, 0f, 100f, 50f).end().close()
        val s1 = ImageLayer(si.texture)
        s1.setOrigin(s1.width() / 2, s1.height() / 2)
        g4.addAt(s1, 50f, 50f)

        // put a large clipped group inside a small one
        val g5Inner = GroupLayer(150f, 150f)
        g5Inner.addAt(ImageLayer(tex).setScale(2f), -iwidth, -iheight)
        g5Inner.addAt(ImageLayer(tex).setScale(2f), -iwidth, iheight)
        g5Inner.addAt(ImageLayer(tex).setScale(2f), iwidth, -iheight)
        g5Inner.addAt(ImageLayer(tex).setScale(2f), iwidth, iheight)
        val g5 = GroupLayer(100f, 100f)
        g5.addAt(g5Inner, -25f, -25f)

        // create a layer that draws the boundaries of our clipped group layers
        game.rootLayer.add(object : Layer() {
            override fun paintImpl(surf: Surface) {
                // draw the border of our various clipped groups
                surf.setFillColor(0xFF000000.toInt())
                outline(surf, g1)
                outline(surf, g2)
                outline(surf, g3)
                outline(surf, g4)
                outline(surf, g5)
            }

            private fun outline(surf: Surface, gl: GroupLayer) {
                drawRect(surf, gl.tx() - gl.originX(), gl.ty() - gl.originY(), gl.width(), gl.height())
            }

            private fun drawRect(surf: Surface, x: Float, y: Float, w: Float, h: Float) {
                val left = x - 1
                val top = y - 1
                val right = x + w + 2f
                val bot = y + h + 2f
                surf.drawLine(left, top, right, top, 1f)
                surf.drawLine(right, top, right, bot, 1f)
                surf.drawLine(left, top, left, bot, 1f)
                surf.drawLine(left, bot, right, bot, 1f)
            }
        })
        game.rootLayer.addAt(g1, 75f, 225f)
        game.rootLayer.addAt(g2, 200f, 275f)
        game.rootLayer.addAt(g3, 275f, 225f)
        game.rootLayer.addAt(g4, 400f, 225f)
        game.rootLayer.addAt(g5, 525f, 225f)

        conns.add(game.paint.connect { clock: Clock ->
            val elapsed = clock.tick / 1000f
            i1.setRotation(elapsed * MathUtil.PI / 2)
            s1.setRotation(elapsed * MathUtil.PI / 2)
            g2.setWidth(round(abs(100f * sin(elapsed))))
            inner.setOrigin(sin(elapsed * 2f) * 50, cos(elapsed * 2f) * 50)
            val cycle = elapsed / (MathUtil.PI * 2)
            if (MathUtil.ifloor(cycle) % 2 == 0) {
                // go in a circle without going out of bounds
                g5Inner.setTranslation(-25 + 50 * cos(elapsed),
                        -25 + 50 * sin(elapsed))
            } else {
                // go out of bounds on right and left
                g5Inner.setTranslation(25 + 250 * cos(elapsed + MathUtil.PI / 2), -25f)
            }
        })
    }
}
