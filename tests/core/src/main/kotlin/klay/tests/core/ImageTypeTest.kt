package klay.tests.core

import klay.core.Color
import klay.core.Image
import klay.scene.ImageLayer

class ImageTypeTest(game: TestsGame) : Test(game, "ImageType", "Test that image types display the same. Left-to-right: ImageLayer, SurfaceImage, " + "CanvasImage, ground truth (expected).") {

    override fun init() {
        // add a half white, half blue background
        val bwidth = 4 * width
        val bheight = 4 * height
        val bg = game.createSurface(bwidth, bheight)
        bg.begin().setFillColor(Color.rgb(255, 255, 255)).fillRect(0f, 0f, bwidth, bheight).setFillColor(Color.rgb(0, 0, 255)).fillRect(0f, bwidth / 2, bwidth, bheight / 2).end().close()
        game.rootLayer.add(ImageLayer(bg.texture))

        // once the image loads, create our layers
        game.assets.getImage(imageSrc).state.onSuccess { image: Image ->
            val imtex = image.texture()
            game.rootLayer.addAt(ImageLayer(imtex), offset.toFloat(), offset.toFloat())
            game.rootLayer.addAt(ImageLayer(imtex), offset.toFloat(), offset + 2 * height)

            val surf = game.createSurface(image.width, image.height)
            surf.begin().clear().draw(imtex, 0f, 0f).end().close()
            game.rootLayer.addAt(ImageLayer(surf.texture), offset + width, offset.toFloat())
            game.rootLayer.addAt(ImageLayer(surf.texture), offset + width, offset + 2 * height)

            val canvas = game.graphics.createCanvas(image.width, image.height)
            canvas.draw(image, 0f, 0f)
            val cantex = canvas.toTexture()
            game.rootLayer.addAt(ImageLayer(cantex), offset + 2 * width, offset.toFloat())
            game.rootLayer.addAt(ImageLayer(cantex), offset + 2 * width, offset + 2 * height)
        }

        // add ground truth image
        val truth = game.assets.getImage(imageGroundTruthSrc)
        game.rootLayer.addAt(ImageLayer(truth), 3 * width, 0f)
    }

    companion object {

        internal var width = 100f
        internal var height = 100f
        internal var offset = 5
        internal var imageSrc = "images/imagetypetest.png"
        internal var imageGroundTruthSrc = "images/imagetypetest_expected.png"
    }
}
