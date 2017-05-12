package klay.tests.core

import klay.core.Color
import klay.core.Image
import klay.scene.GroupLayer
import klay.scene.ImageLayer

class AlphaLayerTest(game: TestsGame) : Test(game, "AlphaLayer", "Test that alpha works the same on all layer types and that alpha is 'additive'.") {

    override fun init() {
        val rootLayer = game.rootLayer
        val fullWidth = 6 * width
        val fullHeight = 3 * height

        // add a half white, half blue background
        val bg = game.createSurface(fullWidth, fullHeight)
        bg.begin().setFillColor(Color.rgb(255, 255, 255)).fillRect(0f, 0f, fullWidth, fullHeight).setFillColor(Color.rgb(0, 0, 255)).fillRect(0f, 2 * height, fullWidth, height).end().close()
        rootLayer.add(ImageLayer(bg.texture))

        addDescrip("all layers contained in group layer with a=0.5\n" + "thus, fully composited a=0.25", offset.toFloat(), fullHeight + 5, fullWidth)

        // add a 50% transparent group layer
        val groupLayer = GroupLayer()
        groupLayer.setAlpha(0.5f)
        rootLayer.add(groupLayer)

        game.assets.getImage("images/alphalayertest.png").state.onSuccess { image: Image ->
            val imtex = image.texture()
            var x = offset.toFloat()
            val y0 = offset.toFloat()
            val y1 = offset + height
            val y2 = offset + 2 * height

            // add the layers over a white background, then again over blue
            groupLayer.addAt(ImageLayer(imtex).setAlpha(0.5f), x, y0)
            addDescrip("image\nimg layer a=0.5", x, y1, width)
            groupLayer.addAt(ImageLayer(imtex).setAlpha(0.5f), x, y2)
            x += width

            val surf1 = game.createSurface(image.width, image.height)
            surf1.begin().clear().setAlpha(0.5f).draw(imtex, 0f, 0f).end().close()
            groupLayer.addAt(ImageLayer(surf1.texture), x, y0)
            addDescrip("surface a=0.5\nimg layer a=1", x, y1, width)
            groupLayer.addAt(ImageLayer(surf1.texture), x, y2)
            x += width

            val surf2 = game.createSurface(image.width, image.height)
            surf2.begin().clear().draw(imtex, 0f, 0f).end().close()
            groupLayer.addAt(ImageLayer(surf2.texture).setAlpha(0.5f), x, y0)
            addDescrip("surface a=1\nimg layer a=0.5", x, y1, width)
            groupLayer.addAt(ImageLayer(surf2.texture).setAlpha(0.5f), x, y2)
            x += width

            val canvas1 = game.graphics.createCanvas(image.width, image.height)
            canvas1.draw(image, 0f, 0f)
            val cantex1 = canvas1.toTexture()
            groupLayer.addAt(ImageLayer(cantex1).setAlpha(0.5f), x, y0)
            addDescrip("canvas a=1\nimg layer a=0.5", x, y1, width)
            groupLayer.addAt(ImageLayer(cantex1).setAlpha(0.5f), x, y2)
            x += width

            val canvas2 = game.graphics.createCanvas(image.width, image.height)
            canvas2.setAlpha(0.5f).draw(image, 0f, 0f)
            val cantex2 = canvas2.toTexture()
            groupLayer.addAt(ImageLayer(cantex2), x, y0)
            addDescrip("canvas a=0.5\nimg layer a=1", x, y1, width)
            groupLayer.addAt(ImageLayer(cantex2), x, y2)
            x += width

            // add some copies of the image at 1, 0.5, 0.25 and 0.125 alpha
            x = offset + width
            for (alpha in floatArrayOf(1f, 1 / 2f, 1 / 4f, 1 / 8f)) {
                val y = fullHeight + 50
                rootLayer.addAt(ImageLayer(imtex).setAlpha(alpha), x, y)
                addDescrip("image a=" + alpha, x, y + height / 2, width / 2)
                x += width
            }
        }

        // add ground truth of 25% opaque image
        val truth = game.assets.getImage("images/alphalayertest_expected.png")
        rootLayer.addAt(ImageLayer(truth), 5 * width, 0f)
        addDescrip("ground truth", 5 * width, offset + height, width)
    }

    companion object {

        internal var width = 100f
        internal var height = 100f
        internal var offset = 5
    }
}
