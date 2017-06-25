package klay.tests.core

import klay.core.*
import klay.scene.GroupLayer
import klay.scene.ImageLayer
import klay.scene.Layer
import pythagoras.f.MathUtil
import pythagoras.f.Rectangle

class SubImageTest(game: TestsGame) : Test(game, "SubImage", "Tests sub-image rendering in various circumstances.") {

    override fun init() {
        // create a canvas image and draw subimages of that
        val r = 30f
        val canvas = game.graphics.createCanvas((2 * r), (2 * r))
        canvas.setFillColor(0xFF99CCFF.toInt()).fillCircle(r, r, r)
        fragment("CanvasImage", canvas.toTexture(), 250f, 160f)

        // draw subimages of a simple static image
        game.assets.getImage("images/orange.png").state.onSuccess { orange: Image ->
            val otex = orange.texture()
            fragment("Image", otex, 250f, 10f)

            val pw = orange.width
            val ph = orange.height
            val phw = pw / 2
            val phh = ph / 2
            val otile = otex.tile(0f, phh / 2, pw, phh)

            // create tileable sub-texture
            val subtex = game.graphics.createTexture(
                    otile.width, otile.height, Texture.Config.DEFAULT.repeat(true, true))
            TextureSurface(game.graphics, game.defaultBatch, subtex).begin().clear().draw(otile, 0f, 0f).end().close()

            // tile a sub-image, oh my!
            val tiled = ImageLayer(subtex)
            tiled.setSize(100f, 100f)
            addTest(10f, 10f, tiled, "Tile to reptex to ImageLayer")

            // draw a subimage to a canvas
            val split = game.graphics.createCanvas(orange.width, orange.height)
            split.draw(orange.region(0f, 0f, phw, phh), phw, phh)
            split.draw(orange.region(phw, 0f, phw, phh), 0f, phh)
            split.draw(orange.region(0f, phh, phw, phh), phw, 0f)
            split.draw(orange.region(phw, phh, phw, phh), 0f, 0f)
            addTest(140f, 10f, ImageLayer(split.toTexture()), "Canvas draw Image.Region", 80f)

            // draw a subimage in an immediate layer
            addTest(130f, 100f, object : Layer() {
                override fun paintImpl(surf: Surface) {
                    surf.draw(otile, 0f, 0f)
                    surf.draw(otile, pw, 0f)
                    surf.draw(otile, 0f, phh)
                    surf.draw(otile, pw, phh)
                }
            }, 2 * pw, 2 * phh, "Surface draw Tile", 100f)

            // draw an image layer whose image region oscillates
            val osci = ImageLayer(otex)
            osci.region = Rectangle(0f, 0f, orange.width, orange.height)
            addTest(10f, 150f, osci, "ImageLayer with changing width", 100f)

            conns.add(game.paint.connect { clock: Clock ->
                val t = clock.tick / 1000f
                // round the width so that it sometimes goes to zero; just to be sure zero doesn't choke
                osci.region!!.width = Math.round(Math.abs(MathUtil.sin(t)) * osci.tile()!!.width).toFloat()
            })
        }.onFailure(logFailure("Failed to load orange image"))
    }

    private fun fragment(source: String, tex: Texture, ox: Float, oy: Float) {
        val hw = tex.displayWidth / 2f
        val hh = tex.displayHeight / 2f
        val ul = tex.tile(0f, 0f, hw, hh)
        val ur = tex.tile(hw, 0f, hw, hh)
        val ll = tex.tile(0f, hh, hw, hh)
        val lr = tex.tile(hw, hh, hw, hh)
        val ctr = tex.tile(hw / 2, hh / 2, hw, hh)

        val dx = hw + 10
        val dy = hh + 10
        val group = GroupLayer()
        group.addAt(ImageLayer(ul), 0f, 0f)
        group.addAt(ImageLayer(ur), dx, 0f)
        group.addAt(ImageLayer(ll), 0f, dy)
        group.addAt(ImageLayer(lr), dx, dy)
        group.addAt(ImageLayer(ctr), dx / 2, 2 * dy)

        val xoff = tex.displayWidth + 20
        group.addAt(ImageLayer(ul).setScale(2f), xoff, 0f)
        group.addAt(ImageLayer(ur).setScale(2f), xoff + 2 * dx, 0f)
        group.addAt(ImageLayer(ll).setScale(2f), xoff, 2 * dy)
        group.addAt(ImageLayer(lr).setScale(2f), xoff + 2 * dx, 2 * dy)

        game.rootLayer.addAt(group, ox, oy)
        addDescrip(source + " to Texture to Tiles, and scaled", ox, oy + tex.displayHeight * 2 + 25,
                3 * tex.displayWidth + 40)
    }
}
