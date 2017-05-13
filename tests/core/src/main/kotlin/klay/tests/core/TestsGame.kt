package klay.tests.core

import klay.core.*
import klay.scene.ImageLayer
import klay.scene.Layer
import klay.scene.Pointer
import klay.scene.SceneGame
import java.util.*

class TestsGame(plat: Platform, args: Array<String>) : SceneGame(plat, Test.UPDATE_RATE) {

    /** Helpful class for allowing selection of an one of a set of values for a test.  */
    open class NToggle<T> @SafeVarargs
    constructor(name: String, vararg values: T) {
        val layer = ImageLayer()
        val prefix: String
        val values: MutableList<T> = ArrayList()
        private var valueIdx: Int = 0

        init {
            for (value in values) {
                this.values.add(value)
            }
            this.prefix = name + ": "
            layer.events().connect(object : Pointer.Listener() {
                override fun onStart(iact: Pointer.Interaction) {
                    set((valueIdx + 1) % this@NToggle.values.size)
                }
            })

            set(0)
        }

        fun toString(value: T): String {
            return value.toString()
        }

        open fun set(idx: Int) {
            this.valueIdx = idx
            layer.setTile(game!!.ui.formatButton(prefix + toString(values[idx])))
        }

        fun value(): T {
            return values[valueIdx]
        }

        fun valueIdx(): Int {
            return valueIdx
        }
    }

    class Toggle(name: String) : NToggle<Boolean>(name, java.lang.Boolean.FALSE, java.lang.Boolean.TRUE)

    private val tests: Array<Test>
    private var currentTest: Test? = null

    val log = plat.log
    val input = plat.input
    val graphics = plat.graphics
    val assets = plat.assets
    val storage = plat.storage
    val pointer: Pointer
    val ui: UI

    init {
        game = this
        ui = UI(this)

        pointer = Pointer(plat, rootLayer, true)
        input.touchEvents.connect(klay.scene.Touch.Dispatcher(rootLayer, true))
        input.mouseEvents.connect(klay.scene.Mouse.Dispatcher(rootLayer, true))

        tests = arrayOf<Test>(
                CanvasTest(this),
                SurfaceTest(this),
                SubImageTest(this),
                ClippedLayerTest(this),
                CanvasStressTest(this),
                ImageTypeTest(this),
                AlphaLayerTest(this),
                ImageScalingTest(this),
                DepthTest(this),
                ClearBackgroundTest(this),
                PauseResumeTest(this),
                TextTest(this),
                ScaledTextTest(this),
                DialogTest(this),
                LayerClickTest(this),
                PointerMouseTouchTest(this),
                MouseWheelTest(this),
                ShaderTest(this)
//                SoundTest(this),
//                NetTest(this),
//                FullscreenTest(this)
        )
        // display basic instructions
        log.info("Right click, touch with two fingers, or type ESC to return to test menu.")

        // add global listeners which navigate back to the menu

        input.mouseEvents.connect(Mouse.buttonSlot { event: Mouse.ButtonEvent ->
            if (currentTest != null && currentTest!!.usesPositionalInputs()) return@buttonSlot
            if (event.button === Mouse.ButtonEvent.Id.RIGHT) displayMenuLater()
        })
        var _active: MutableSet<Int> = HashSet()
        input.touchEvents.connect({ events: Array<Touch.Event> ->
            if (currentTest != null && currentTest!!.usesPositionalInputs()) return@connect
            when (events[0].kind) {
                Touch.Event.Kind.START -> {
                    // Android and iOS handle touch events rather differently, so we need to do this
                    // finagling to determine whether there is an active two or three finger touch
                    for (event in events) _active.add(event.id)
                    if (_active.size > 1) displayMenuLater()
                }
                Touch.Event.Kind.END,
                Touch.Event.Kind.CANCEL -> for (event in events) _active.remove(event.id)
            }
        })
        input.keyboardEvents.connect(Keyboard.keySlot { event: Keyboard.KeyEvent ->
            if (event.down && event.key === Key.ESCAPE) displayMenu()
        })

        displayMenu()

        for (arg in args) {
            if (arg.startsWith("test")) {
                startTest(tests[Integer.parseInt(arg.substring(4))])
                break
            }
        }
    }

    fun createSurface(width: Float, height: Float): TextureSurface {
        return TextureSurface(graphics, defaultBatch, width, height)
    }

    fun onHardwardBack(): Boolean {
        if (currentTest == null) return false
        displayMenuLater() // we're not currently on the GL thread
        return true
    }

    // defers display of menu by one frame to avoid the right click or touch being processed by the
    // menu when it is displayed
    internal fun displayMenuLater() {
        plat.exec.invokeLater(Runnable { displayMenu() })
    }

    internal fun displayMenu() {
        clearTest()
        rootLayer.disposeAll()
        rootLayer.add(createWhiteBackground())

        val gap = 20f
        var x = gap
        var y = gap
        var maxHeight = 0f

        val info = "Renderer: gl (batch=" + defaultBatch + ")" +
                " / Screen: " + graphics.screenSize() +
                " / Window: " + graphics.viewSize
        val infoTex = ui.formatText(info, false)
        rootLayer.addAt(ImageLayer(infoTex), x, y)
        y += infoTex.displayHeight + gap

        for (test in tests) {
            if (!test.available()) continue
            val button = ui.createButton(test.name, Runnable { startTest(test) })
            if (x + button.width() > graphics.viewSize.width - gap) {
                x = gap
                y += maxHeight + gap
                maxHeight = 0f
            }
            maxHeight = Math.max(maxHeight, button.height())
            rootLayer.addAt(button, x, y)
            x += button.width() + gap
        }
    }

    internal fun clearTest() {
        if (currentTest != null) {
            currentTest!!.dispose()
            currentTest = null
        }
    }

    internal fun startTest(test: Test) {
        clearTest()
        currentTest = test

        // setup root layer for next test
        rootLayer.disposeAll()
        rootLayer.add(createWhiteBackground())

        log.info("Starting " + currentTest!!.name)
        log.info(" Description: " + currentTest!!.descrip)
        currentTest!!.init()

        if (currentTest!!.usesPositionalInputs()) {
            // slap on a Back button if the test is testing the usual means of backing out
            val back = ui.createButton("Back", Runnable { displayMenuLater() })
            rootLayer.addAt(back, graphics.viewSize.width - back.width() - 10f, 10f)
        }
    }

    protected fun createWhiteBackground(): Layer {
        val bg = object : Layer() {
            override fun paintImpl(surf: Surface) {
                surf.setFillColor(0xFFFFFFFF.toInt()).fillRect(
                        0f, 0f, graphics.viewSize.width, graphics.viewSize.height)
            }
        }
        bg.setDepth(java.lang.Float.NEGATIVE_INFINITY) // render behind everything
        return bg
    }

    companion object {
        var game: TestsGame? = null
    }
}
