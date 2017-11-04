package tripleklay.demo.core.anim

import klay.core.Font
import klay.scene.GroupLayer
import klay.scene.ImageLayer
import tripleklay.anim.Flicker
import tripleklay.demo.core.DemoScreen
import tripleklay.ui.Group
import tripleklay.ui.Root
import tripleklay.util.StyledText
import tripleklay.util.TextStyle

/**
 * Demonstrates the flicker.
 */
class FlickerDemo : DemoScreen() {
    override fun name(): String {
        return "Flicker"
    }

    override fun title(): String {
        return "Flicker Demo"
    }

    override fun createIface(root: Root): Group? {
        val width = 410f
        val height = 400f
        val clip = GroupLayer(410f, 400f)
        layer.addAt(clip, (size().width - width) / 2f, (size().height - height) / 2f)

        val scroll = GroupLayer()
        clip.add(scroll)
        // add a bunch of image layers to our root layer
        var y = 0f
        for (ii in 0..IMG_COUNT - 1) {
            val image = graphics().createCanvas(width, IMG_HEIGHT)
            val text = StringBuilder()
            if (ii == 0)
                text.append("Tap & fling")
            else if (ii == IMG_COUNT - 1)
                text.append("Good job!")
            else
                for (tt in 0..24) text.append(ii)
            StyledText.span(graphics(), text.toString(), TEXT).render(image, 0f, 0f)
            val layer = ImageLayer(image.toTexture())
            scroll.addAt(layer, 0f, y)
            y += layer.scaledHeight()
        }

        val flicker = object : Flicker(0f, height - IMG_HEIGHT * IMG_COUNT, 0f) {
            override fun friction(): Float {
                return 0.001f
            }
        }
        clip.events().connect(flicker)
        flicker.changed.connect({ flicker: Flicker ->
            scroll.setTy(flicker.position)
        })
        closeOnHide(paint.connect(flicker.onPaint))

        return null
    }

    companion object {
        protected val IMG_HEIGHT = 100f
        protected val IMG_COUNT = 20
        protected val TEXT = TextStyle.DEFAULT.withFont(Font("Helvetiva", 72f))
    }
}
