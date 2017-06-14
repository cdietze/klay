package klay.tests.core

import klay.core.*
import klay.scene.*
import klay.scene.Mouse
import klay.scene.Pointer
import klay.scene.Touch
import pythagoras.f.Point
import pythagoras.f.Vector
import react.Connection
import java.util.*

internal class PointerMouseTouchTest
// private TestsGame.NToggle<String> propagate;

(game: TestsGame) : Test(game, "PointerMouseTouch", "Tests the Pointer, Mouse, and Touch interfaces.") {

    private val baseFormat = TextFormat(Font("Times New Roman", 20f))
    private val logFormat = TextFormat(Font("Times New Roman", 12f))

    private var logger: TextLogger? = null
    private var motionLabel: TextMapper? = null

    private var preventDefault: TestsGame.Toggle? = null
    private var capture: TestsGame.Toggle? = null

    override fun init() {
        var y = 20f
        var x = 20f

        preventDefault = TestsGame.Toggle("Prevent Default")
        game.rootLayer.addAt(preventDefault!!.layer, x, y)
        x += preventDefault!!.layer.width() + 5

        capture = TestsGame.Toggle("Capture")
        game.rootLayer.addAt(capture!!.layer, x, y)
        x += capture!!.layer.width() + 5

        // propagate = new TestsGame.NToggle<String>("Propagation", "Off", "On", "On (stop)") {
        //   @Override public void set(int value) {
        //     super.set(value);
        //     platform().setPropagateEvents(value != 0);
        //   }
        // };
        // graphics().rootLayer().addAt(propagate.layer, x, y);

        y += preventDefault!!.layer.height() + 5
        x = 20f

        val boxWidth = 300f
        val boxHeight = 110f
        val mouse = Box("Mouse", 0xffff8080.toInt(), boxWidth, boxHeight)
        game.rootLayer.addAt(mouse.layer, x, y)
        y += mouse.layer.height() + 5

        val pointer = Box("Pointer", 0xff80ff80.toInt(), boxWidth, boxHeight)
        game.rootLayer.addAt(pointer.layer, x, y)
        y += pointer.layer.height() + 5

        val touch = Box("Touch", 0xff8080ff.toInt(), boxWidth, boxHeight)
        game.rootLayer.addAt(touch.layer, x, y)

        y = mouse.layer.ty()
        x += touch.layer.width() + 5

        // setup the logger and its layer
        y += createLabel("Event Log", 0, x, y).height()
        logger = TextLogger(375f, 15, logFormat)
        logger!!.layer.setTranslation(x, y)
        game.rootLayer.add(logger!!.layer)
        y += logger!!.layer.height() + 5

        // setup the motion logger and its layer
        y += createLabel("Motion Log", 0, x, y).height()
        motionLabel = TextMapper(375f, 6, logFormat)
        motionLabel!!.layer.setTranslation(x, y)
        game.rootLayer.add(motionLabel!!.layer)

        // add mouse layer listener
        mouse.label.events().connect(object : Mouse.Listener {
            internal var label = mouse.label

            override fun onButton(event: klay.core.Mouse.ButtonEvent, iact: Mouse.Interaction) {
                if (event.down) {
                    _lstart = label.transform().translation
                    _pstart = Vector(event.x, event.y)
                    label.setAlpha(0.5f)
                    modify(event)
                    logger!!.log(describe(event, "mouse down"))
                } else {
                    label.setAlpha(1.0f)
                    modify(event)
                    logger!!.log(describe(event, "mouse up"))
                }
            }

            override fun onDrag(event: klay.core.Mouse.MotionEvent, iact: Mouse.Interaction) {
                val delta = Vector(event.x, event.y).subtractLocal(_pstart!!)
                label.setTranslation(_lstart!!.x + delta.x, _lstart!!.y + delta.y)
                modify(event)
                motionLabel!!["mouse drag"] = describe(event, "")
            }

            override fun onMotion(event: klay.core.Mouse.MotionEvent, iact: Mouse.Interaction) {
                modify(event)
                motionLabel!!["mouse move"] = describe(event, "")
            }

            override fun onHover(event: Mouse.HoverEvent, iact: Mouse.Interaction) {
                modify(event)
                logger!!.log(describe(event, if (event.inside) "mouse over" else "mouse out"))
            }

            override fun onWheel(event: klay.core.Mouse.WheelEvent, iact: Mouse.Interaction) {
                modify(event)
                logger!!.log(describe(event, "mouse wheel"))
            }

            protected var _lstart: Vector? = null
            protected var _pstart: Vector? = null
        })

        // add mouse layer listener to parent
        mouse.layer.events().connect(object : Mouse.Listener {
            internal var start: Double = 0.toDouble()
            override fun onButton(event: klay.core.Mouse.ButtonEvent, iact: Mouse.Interaction) {
                if (event.down) {
                    start = event.time
                    logger!!.log(describe(event, "parent mouse down " + capture!!.value()))
                } else
                    logger!!.log(describe(event, "parent mouse up"))
            }

            override fun onDrag(event: klay.core.Mouse.MotionEvent, iact: Mouse.Interaction) {
                motionLabel!!["parent mouse drag"] = describe(event, "")
                if (capture!!.value() && event.time - start > 1000 && !iact.captured()) iact.capture()
            }

            override fun onMotion(event: klay.core.Mouse.MotionEvent, iact: Mouse.Interaction) {
                motionLabel!!["parent mouse move"] = describe(event, "")
            }

            override fun onHover(event: Mouse.HoverEvent, iact: Mouse.Interaction) {
                logger!!.log(describe(event, "parent mouse " + if (event.inside) "over" else "out"))
            }

            override fun onWheel(event: klay.core.Mouse.WheelEvent, iact: Mouse.Interaction) {
                logger!!.log(describe(event, "parent mouse wheel"))
            }
        })

        // add pointer layer listener
        pointer.label.events().connect(object : Pointer.Listener {
            internal var label = pointer.label
            override fun onStart(iact: Pointer.Interaction) {
                val event = iact.event!!
                _lstart = label.transform().translation
                _pstart = Vector(event.x, event.y)
                label.setAlpha(0.5f)
                modify(event)
                logger!!.log(describe(event, "pointer start"))
            }

            override fun onDrag(iact: Pointer.Interaction) {
                val event = iact.event!!
                val delta = Vector(event.x, event.y).subtractLocal(_pstart!!)
                label.setTranslation(_lstart!!.x + delta.x, _lstart!!.y + delta.y)
                modify(event)
                motionLabel!!["pointer drag"] = describe(event, "")
            }

            override fun onEnd(iact: Pointer.Interaction) {
                val event = iact.event!!
                label.setAlpha(1.0f)
                modify(event)
                logger!!.log(describe(event, "pointer end"))
            }

            override fun onCancel(iact: Pointer.Interaction) {
                val event = iact.event!!
                label.setAlpha(1.0f)
                modify(event)
                logger!!.log(describe(event, "pointer cancel"))
            }

            protected var _lstart: Vector? = null
            protected var _pstart: Vector? = null
        })

        // add pointer listener for parent layer
        pointer.layer.events().connect(object : Pointer.Listener {
            internal var start: Double = 0.toDouble()
            override fun onStart(iact: Pointer.Interaction) {
                val event = iact.event!!
                logger!!.log(describe(event, "parent pointer start"))
                start = event.time
            }

            override fun onDrag(iact: Pointer.Interaction) {
                val event = iact.event!!
                motionLabel!!["parent pointer drag"] = describe(event, "")
                if (capture!!.value() && event.time - start > 1000 && !iact.captured()) iact.capture()
            }

            override fun onEnd(iact: Pointer.Interaction) {
                val event = iact.event!!
                logger!!.log(describe(event, "parent pointer end"))
            }

            override fun onCancel(iact: Pointer.Interaction) {
                val event = iact.event!!
                logger!!.log(describe(event, "parent pointer cancel"))
            }
        })

        // add touch layer listener
        touch.label.events().connect(object : Touch.Listener {
            internal var label = touch.label
            override fun onStart(iact: Touch.Interaction) {
                val event = iact.event!!
                _lstart = label.transform().translation
                _pstart = Vector(event.x, event.y)
                label.setAlpha(0.5f)
                modify(event)
                logger!!.log(describe(event, "touch start"))
            }

            override fun onMove(iact: Touch.Interaction) {
                val event = iact.event!!
                val delta = Vector(event.x, event.y).subtractLocal(_pstart!!)
                label.setTranslation(_lstart!!.x + delta.x, _lstart!!.y + delta.y)
                modify(event)
                motionLabel!!["touch move"] = describe(event, "")
            }

            override fun onEnd(iact: Touch.Interaction) {
                val event = iact.event!!
                label.setAlpha(1.0f)
                modify(event)
                logger!!.log(describe(event, "touch end"))
            }

            override fun onCancel(iact: Touch.Interaction) {
                val event = iact.event!!
                label.setAlpha(1.0f)
                modify(event)
                logger!!.log(describe(event, "touch cancel"))
            }

            protected var _lstart: Vector? = null
            protected var _pstart: Vector? = null
        })

        // add touch parent layer listener
        touch.layer.events().connect(object : Touch.Listener {
            override fun onStart(iact: Touch.Interaction) {
                val event = iact.event!!
                logger!!.log(describe(event, "parent touch start"))
            }

            override fun onMove(iact: Touch.Interaction) {
                val event = iact.event!!
                motionLabel!!["parent touch move"] = describe(event, "")
            }

            override fun onEnd(iact: Touch.Interaction) {
                val event = iact.event!!
                logger!!.log(describe(event, "parent touch end"))
            }

            override fun onCancel(iact: Touch.Interaction) {
                val event = iact.event!!
                logger!!.log(describe(event, "parent touch cancel"))
            }
        })

        conns.add<Connection>(game.plat.frame.connect { _ ->
            logger!!.paint()
            motionLabel!!.paint()
        })
    }

    override fun usesPositionalInputs(): Boolean {
        return true
    }

    protected fun createLabel(text: String, bg: Int, x: Float, y: Float): ImageLayer {
        return createLabel(text, game.rootLayer, 0xFF202020.toInt(), bg, x, y, 0f)
    }

    protected fun createLabel(text: String, parent: GroupLayer,
                              fg: Int, bg: Int, x: Float, y: Float, padding: Float): ImageLayer {
        val layout = game.graphics.layoutText(text, baseFormat)
        val twidth = layout.size.width + padding * 2
        val theight = layout.size.height + padding * 2
        val canvas = game.graphics.createCanvas(twidth, theight)
        if (bg != 0) canvas.setFillColor(bg).fillRect(0f, 0f, twidth, theight)
        canvas.setFillColor(fg).fillText(layout, padding, padding)
        val imageLayer = ImageLayer(canvas.toTexture())
        parent.addAt(imageLayer, x, y)
        return imageLayer
    }

    protected fun modify(event: Event.XY) {
        event.updateFlag(Event.F_PREVENT_DEFAULT, preventDefault!!.value())
        // TODO
        // event.flags().setPropagationStopped(propagate.valueIdx() == 2);
    }

    protected fun describe(event: Event.XY, handler: String): String {
        val sb = StringBuilder()
        sb.append("@").append(event.time.toLong() % 10000).append(" ")
        sb.append(if (event.isSet(Event.F_PREVENT_DEFAULT)) "pd " else "")
        sb.append(handler).append(" (").append(event.x).append(",").append(event.y).append(")")
        sb.append(" m[")
        if (event.isAltDown) sb.append("A")
        if (event.isCtrlDown) sb.append("C")
        if (event.isMetaDown) sb.append("M")
        if (event.isShiftDown) sb.append("S")
        sb.append("]")
        if (event is klay.core.Pointer.Event) {
            sb.append(" isTouch(").append(event.isTouch).append(")")
        }
        if (event is klay.core.Mouse.ButtonEvent) {
            sb.append(" button(").append(event.button).append(")")
        }
        if (event is klay.core.Mouse.MotionEvent) {
            sb.append(" d(").append(event.dx).append(",").append(event.dy).append(")")
        }

        return sb.toString()
    }

    protected open inner class Label(wid: Float, hei: Float, private val format: TextFormat) {
        val layer: CanvasLayer
        private var layout: Array<out TextLayout>? = null
        private var text: String? = null
        private var dirty: Boolean = false

        init {
            layer = CanvasLayer(game.graphics, wid, hei)
        }

        fun set(text: String) {
            this.text = text
            dirty = true
        }

        fun paint() {
            if (!dirty) {
                return
            }

            val canvas = layer.begin()
            canvas.clear()
            canvas.setFillColor(0xFF202020.toInt())
            layout = game.graphics.layoutText(text!!, format, TextWrap.MANUAL)
            var yy = 0f
            for (line in layout!!.indices) {
                canvas.fillText(layout!![line], 0f, yy)
                yy += layout!![line].size.height
            }
            // if (yy > layer.height()) {
            //   game.log.error("Clipped");
            // }
            layer.end()
            dirty = false
        }
    }

    protected inner class TextMapper(wid: Float, lines: Int, format: TextFormat) : Label(wid, game.graphics.layoutText(".", format).size.height * lines, format) {
        var values: MutableMap<String, String> = TreeMap()

        operator fun set(name: String, value: String) {
            values.put(name, value)
            update()
        }

        fun update() {
            val sb = StringBuilder()
            val iter = values.entries.iterator()
            if (iter.hasNext()) append(sb, iter.next())
            while (iter.hasNext()) append(sb.append('\n'), iter.next())
            set(sb.toString())
        }

        internal fun append(sb: StringBuilder, entry: MutableMap.MutableEntry<String, String>) {
            sb.append(entry.key).append(": ").append(entry.value)
        }
    }

    protected inner class TextLogger(wid: Float, private val lineCount: Int, format: TextFormat) : Label(wid, game.graphics.layoutText(".", format).size.height * lineCount, format) {
        private val entries = ArrayList<String>()

        fun log(text: String) {
            entries.add(text)
            if (entries.size > lineCount) {
                entries.removeAt(0)
            }
            val sb = StringBuilder()
            for (i in entries.indices.reversed()) {
                sb.append(entries[i])
                sb.append('\n')
            }

            set(sb.toString())
        }
    }

    protected inner class Box internal constructor(text: String, color: Int, wid: Float, hei: Float) : Layer.HitTester {
        internal val layer: GroupLayer
        internal val label: ImageLayer

        init {
            layer = GroupLayer(wid, hei)
            layer.add(object : Layer() {
                override fun paintImpl(surface: Surface) {
                    surface.setFillColor(0xff000000.toInt())
                    val t = 0.5f
                    val l = 0.5f
                    val b = layer.height() - 0.5f
                    val r = layer.width() - 0.5f
                    surface.drawLine(l, t, l, b, 1f)
                    surface.drawLine(r, t, r, b, 1f)
                    surface.drawLine(l, b, r, b, 1f)
                    surface.drawLine(l, t, r, t, 1f)
                }
            })
            label = createLabel(text, layer, 0xff000000.toInt(), color, 0f, 0f, 40f)
            layer.addAt(label, (wid - label.width()) / 2, (hei - label.height()) / 2)
            layer.setHitTester(this)
        }

        override fun hitTest(layer: Layer, p: Point): Layer? {
            if (p.x >= 0 && p.y >= 0 && p.x < this.layer.width() && p.y < this.layer.height()) {
                return layer.hitTestDefault(p)
            }
            return null
        }
    }
}
