package tripleklay.demo.jvm.anim

import klay.core.Font
import klay.scene.ImageLayer
import klay.scene.Pointer
import pythagoras.f.MathUtil
import tripleklay.anim.AnimGroup
import tripleklay.anim.Animation
import tripleklay.anim.Animator
import tripleklay.demo.jvm.DemoScreen
import tripleklay.ui.Group
import tripleklay.ui.Root
import tripleklay.util.StyledText
import tripleklay.util.TextStyle

class AnimDemo : DemoScreen() {
    override fun name(): String {
        return "Anims"
    }

    override fun title(): String {
        return "Various Animations"
    }

    override fun createIface(root: Root): Group? {
        // demo a repeating animation
        val canvas = graphics().createCanvas(100f, 100f)
        canvas.setFillColor(0xFFFFCC99.toInt()).fillCircle(50f, 50f, 50f)
        val circle = ImageLayer(canvas.toTexture())

        val width = size().width
        iface.anim.addAt(layer, circle, 50f, 100f).then().repeat(circle).tweenX(circle).to(width - 150).`in`(1000f).easeInOut().then().tweenX(circle).to(50f).`in`(1000f).easeInOut()

        // demo the shake animation
        val click = StyledText.span(graphics(), "Click to Shake", STYLE).toLayer()
        click.events().connect(object : Pointer.Listener {
            override fun onStart(iact: Pointer.Interaction) {
                if (_shaker != null)
                    _shaker!!.complete()
                else
                    _shaker = iface.anim.shake(click).bounds(-3f, 3f, -3f, 0f).cycleTime(25f, 25f).`in`(1000f).then().action(_clear).handle()
            }

            protected val _clear: Runnable = Runnable { _shaker = null }
            protected var _shaker: Animation.Handle? = null
        })
        layer.addAt(click, (width - click.width()) / 2f, 275f)

        // demo animation groups
        val ball = graphics().createCanvas(40f, 40f)
        ball.setFillColor(0xFF99CCFF.toInt()).fillCircle(20f, 20f, 20f)
        val balltex = ball.toTexture()
        val balls = arrayOfNulls<ImageLayer>(6)
        for (ii in balls.indices) {
            balls[ii] = ImageLayer(balltex)
            layer.addAt(balls[ii]!!, 170f + ii * 50f, 350f)
        }
        iface.anim.repeat(layer).add(dropBalls(balls as Array<ImageLayer>, 0, 1)).then().add(dropBalls(balls, 1, 2)).then().add(dropBalls(balls, 3, 3))

        // test barrier delay
        val sqimg = graphics().createCanvas(50f, 50f)
        sqimg.setFillColor(0xFF99CCFF.toInt()).fillRect(0f, 0f, 50f, 50f)
        val square = ImageLayer(sqimg.toTexture())
        square.setOrigin(25f, 25f)
        layer.addAt(square, 50f, 300f)
        square.events().connect(object : Pointer.Listener {
            override fun onStart(iact: Pointer.Interaction) {
                square.setInteractive(false)
                _banim.tweenXY(square).to(50f, 350f)
                _banim.delay(250f).then().tweenRotation(square).to(MathUtil.PI).`in`(500f)
                _banim.addBarrier(1000f)
                _banim.tweenXY(square).to(50f, 300f)
                _banim.delay(250f).then().tweenRotation(square).to(0f).`in`(500f)
                _banim.addBarrier()
                _banim.action(Runnable { square.setInteractive(true) })
            }
        })

        return null
    }

    protected fun dropBalls(balls: Array<ImageLayer>, offset: Int, count: Int): Animation {
        val startY = 350f
        val group = AnimGroup()
        for (ii in 0..count - 1) {
            val ball = balls[ii + offset]
            group.tweenY(ball).to(startY + 100).`in`(1000f).easeIn().then().tweenY(ball).to(startY).`in`(1000f).easeOut()
        }
        return group.toAnim()
    }

    // a separate animator used for testing barriers
    protected var _banim = Animator(paint)

    companion object {

        protected val STYLE = TextStyle.DEFAULT.withFont(Font("Helvetica", 48f))
    }
}
