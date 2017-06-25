package klay.tests.core

import klay.core.*
import klay.scene.ClippedLayer
import klay.scene.GroupLayer
import klay.scene.ImageLayer
import klay.scene.Layer
import pythagoras.f.AffineTransform
import pythagoras.f.MathUtil
import pythagoras.f.Rectangle
import react.RFuture
import react.Slot
import java.util.*

class SurfaceTest(game: TestsGame) : Test(game, "Surface", "Tests various Surface rendering features.") {

    private var paintUpped: TextureSurface? = null

    override fun init() {
        val tile = game.assets.getImage("images/tile.png")
        val orange = game.assets.getImage("images/orange.png")
        var errY = 0f
        val onError: Slot<Throwable> = { err: Throwable ->
            addDescrip("Error: " + err.message, 10f, errY, game.graphics.viewSize.width - 20)
            errY += 30f
        }
        tile.state.onFailure(onError)
        orange.state.onFailure(onError)
        RFuture.collect(Arrays.asList(tile.state, orange.state)).onSuccess { _ -> addTests(orange, tile) }
    }

    override fun dispose() {
        super.dispose()
        if (paintUpped != null) {
            paintUpped!!.close()
            paintUpped = null
        }
    }

    private fun addTests(orange: Image, tile: Image) {
        val otex = orange.texture()
        val ttex = tile.createTexture(Texture.Config.DEFAULT.repeat(true, true))

        // make samples big enough to force a buffer size increase
        val samples = 128
        val hsamples = samples / 2
        val verts = FloatArray((samples + 1) * 4)
        val indices = IntArray(samples * 6)
        tessellateCurve(0f, 40 * Math.PI.toFloat(), verts, indices, object : F {
            override fun apply(x: Float): Float {
                return Math.sin((x / 20).toDouble()).toFloat() * 50
            }
        })

        val ygap = 20f
        var ypos = 10f

        // draw some wide lines
        ypos = ygap + addTest(10f, ypos, object : Layer() {
            override fun paintImpl(surf: Surface) {
                drawLine(surf, 0f, 0f, 50f, 50f, 15f)
                drawLine(surf, 70f, 50f, 120f, 0f, 10f)
                drawLine(surf, 0f, 70f, 120f, 120f, 10f)
            }
        }, 120f, 120f, "drawLine with width")

        ypos = ygap + addTest(20f, ypos, object : Layer() {
            override fun paintImpl(surf: Surface) {
                surf.setFillColor(0xFF0000FF.toInt()).fillRect(0f, 0f, 100f, 25f)
                // these two alpha fills should look the same
                surf.setFillColor(0x80FF0000.toInt()).fillRect(0f, 0f, 50f, 25f)
                surf.setAlpha(0.5f).setFillColor(0xFFFF0000.toInt()).fillRect(50f, 0f, 50f, 25f).setAlpha(1f)
            }
        }, 100f, 25f, "left and right half both same color")

        ypos = ygap + addTest(20f, ypos, object : Layer() {
            override fun paintImpl(surf: Surface) {
                surf.setFillColor(0xFF0000FF.toInt()).fillRect(0f, 0f, 100f, 50f)
                surf.setAlpha(0.5f)
                surf.fillRect(0f, 50f, 50f, 50f)
                surf.draw(otex, 55f, 5f)
                surf.draw(otex, 55f, 55f)
                surf.setAlpha(1f)
            }
        }, 100f, 100f, "fillRect and drawImage at 50% alpha")

        ypos = 10f

        val triangleBatch = TriangleBatch(game.graphics.gl)
        val af = AffineTransform().scale(game.graphics.scale().factor, game.graphics.scale().factor).translate(160f, ygap + 150)

        ypos = ygap + addTest(160f, ypos, object : Layer() {
            override fun paintImpl(surf: Surface) {
                // fill some shapes with patterns
                surf.setFillPattern(ttex).fillRect(10f, 0f, 100f, 100f)
                // render a sliding window of half of our triangles to test the slice rendering
                triangleBatch.addTris(ttex, Tint.NOOP_TINT, af,
                        verts, offset * 4, (hsamples + 1) * 4, ttex.width, ttex.height,
                        indices, offset * 6, hsamples * 6, offset * 2)
                offset += doff
                if (offset == 0)
                    doff = 1
                else if (offset == hsamples) doff = -1
            }

            private var offset = 0
            private var doff = 1
        }.setBatch(triangleBatch), 120f, 210f, "ImmediateLayer patterned fillRect, fillTriangles")

        val patted = game.createSurface(100f, 100f)
        patted.begin().clear().setFillPattern(ttex).fillRect(0f, 0f, 100f, 100f).end().close()
        ypos = ygap + addTest(170f, ypos, ImageLayer(patted.texture),
                "SurfaceImage patterned fillRect")

        ypos = 10f

        // fill a patterned quad in a clipped group layer
        val twidth = 150f
        val theight = 75f
        val group = GroupLayer()
        ypos = ygap + addTest(315f, 10f, group, twidth, theight,
                "Clipped pattern should not exceed grey rectangle")
        group.add(object : Layer() {
            override fun paintImpl(surf: Surface) {
                surf.setFillColor(0xFFCCCCCC.toInt()).fillRect(0f, 0f, twidth, theight)
            }
        })
        group.add(object : ClippedLayer(twidth, theight) {
            override fun paintClipped(surf: Surface) {
                surf.setFillPattern(ttex).fillRect(-10f, -10f, twidth + 20, theight + 20)
            }
        })

        // add a surface layer that is updated on every call to paint
        // (a bad practice, but one that should actually work)
        paintUpped = game.createSurface(100f, 100f)
        ypos = ygap + addTest(315f, ypos, ImageLayer(paintUpped!!.texture),
                "SurfaceImage updated in paint()")

        // draw some randomly jiggling dots inside a bounded region
        val dots = ArrayList<ImageLayer>()
        val dotBox = Rectangle(315f, ypos, 200f, 100f)
        ypos = ygap + addTest(dotBox.x, dotBox.y, object : Layer() {
            override fun paintImpl(surf: Surface) {
                surf.setFillColor(0xFFCCCCCC.toInt()).fillRect(0f, 0f, dotBox.width, dotBox.height)
            }
        }, dotBox.width, dotBox.height, "Randomly positioned SurfaceImages")
        for (ii in 0..9) {
            val dot = game.createSurface(10f, 10f)
            dot.begin().setFillColor(0xFFFF0000.toInt()).fillRect(0f, 0f, 5f, 5f).fillRect(5f, 5f, 5f, 5f).setFillColor(0xFF0000FF.toInt()).fillRect(5f, 0f, 5f, 5f).fillRect(0f, 5f, 5f, 5f).end().close()
            val dotl = ImageLayer(dot.texture)
            dotl.setTranslation(dotBox.x + Math.random().toFloat() * (dotBox.width - 10),
                    dotBox.y + Math.random().toFloat() * (dotBox.height - 10))
            dots.add(dotl)

            game.rootLayer.add(dotl)
        }

        conns.add(game.paint.connect { clock: Clock ->
            for (dot in dots) {
                if (Math.random() > 0.95) {
                    dot.setTranslation(dotBox.x + Math.random().toFloat() * (dotBox.width - 10),
                            dotBox.y + Math.random().toFloat() * (dotBox.height - 10))
                }
            }

            val now = clock.tick / 1000f
            val sin = Math.abs(MathUtil.sin(now))
            val cos = Math.abs(MathUtil.cos(now))
            val sinColor = (sin * 255).toInt()
            val cosColor = (cos * 255).toInt()
            val c1 = 0xFF shl 24 or (sinColor shl 16) or (cosColor shl 8)
            val c2 = 0xFF shl 24 or (cosColor shl 16) or (sinColor shl 8)
            paintUpped!!.begin().clear().setFillColor(c1).fillRect(0f, 0f, 50f, 50f).setFillColor(c2).fillRect(50f, 50f, 50f, 50f).end()
        })
    }

    internal fun drawLine(surf: Surface, x1: Float, y1: Float, x2: Float, y2: Float, width: Float) {
        val xmin = Math.min(x1, x2)
        val xmax = Math.max(x1, x2)
        val ymin = Math.min(y1, y2)
        val ymax = Math.max(y1, y2)
        surf.setFillColor(0xFF0000AA.toInt()).fillRect(xmin, ymin, xmax - xmin, ymax - ymin)
        surf.setFillColor(0xFF99FFCC.toInt()).drawLine(x1, y1, x2, y2, width)
        surf.setFillColor(0xFFFF0000.toInt()).fillRect(x1, y1, 1f, 1f).fillRect(x2, y2, 1f, 1f)
    }

    interface F {
        fun apply(x: Float): Float
    }

    internal fun tessellateCurve(minx: Float, maxx: Float, verts: FloatArray, indices: IntArray, f: F) {
        val slices = (verts.size - 1) / 4
        var vv = 0
        val dx = (maxx - minx) / slices
        var x = minx
        while (vv < verts.size) {
            verts[vv++] = x
            verts[vv++] = 0f
            verts[vv++] = x
            verts[vv++] = f.apply(x)
            x += dx
        }
        var ss = 0
        var ii = 0
        while (ss < slices) {
            val base = ss * 2
            indices[ii++] = base
            indices[ii++] = base + 1
            indices[ii++] = base + 3
            indices[ii++] = base
            indices[ii++] = base + 3
            indices[ii++] = base + 2
            ss++
        }
    }
}
