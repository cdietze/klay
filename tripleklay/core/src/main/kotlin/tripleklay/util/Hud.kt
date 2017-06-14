package tripleklay.util

import klay.core.*
import klay.scene.CanvasLayer
import klay.scene.LayerUtil
import klay.scene.SceneGame
import pythagoras.f.Dimension
import pythagoras.f.IDimension
import react.Slot
import react.Value

import java.util.ArrayList

/**
 * Maintains a (usually debugging) HUD with textual information displayed in one or two columns.
 * The text is all rendered to a single [Canvas] (and updated only when values change) to put
 * as little strain on the renderer as possible. Example usage:
 * <pre>`class MyGame extends SceneGame {
 * private Hud.Stock hud = new Hud.Stock(this);
 * public void init () {
 * hud.layer.setDepth(Short.MAX_VALUE);
 * rootLayer.add(hud.layer);
 * }
 * }
`</pre> *
 */
open class Hud(protected val _game: SceneGame) {
    /** A stock HUD that provides a bunch of standard PlayN performance info and handles
     * once-per-second updating.  */
    class Stock(game: SceneGame) : Hud(game) {
        private val _frames = Value.create(0)
        private val _shaderCreates = Value.create(0)
        private val _fbCreates = Value.create(0)
        private val _texCreates = Value.create(0)
        private val _shaderBinds = Value.create(0)
        private val _fbBinds = Value.create(0)
        private val _texBinds = Value.create(0)
        private val _rQuads = Value.create(0)
        private val _rTris = Value.create(0)
        private val _shaderFlushes = Value.create(0)

        private val _quadShader = Value.create("")
        private val _trisShader = Value.create("")

        init {
            add("Shader info:", true)
            add(_quadShader)
            add(_trisShader)

            add("Per second:", true)
            add("Frames:", _frames)
            add("Shader creates:", _shaderCreates)
            add("FB creates:", _fbCreates)
            add("Tex creates:", _texCreates)

            add("Per frame:", true)
            add("Shader binds:", _shaderBinds)
            add("FB binds:", _fbBinds)
            add("Tex binds:", _texBinds)
            add("Quads drawn:", _rQuads)
            add("Tris drawn:", _rTris)
            add("Shader flushes:", _shaderFlushes)

            // receive paint updates while our layer is connected
            LayerUtil.bind(layer, game.paint, object : Slot<Clock> {
                override fun invoke(clock: Clock) {
                    val now = clock.tick
                    if (now > _nextUpdate) {
                        willUpdate()
                        update()
                        _nextUpdate = now + 1000
                    }
                }

                protected var _nextUpdate: Int = 0
            })
        }

        /** Called when the HUD is about to update its display. Values added to the HUD should be
         * updated by this call if they've not been already.
         * Must call `super.willUpdate()`.  */
        protected fun willUpdate() {
            // GLContext.Stats stats = graphics().ctx().stats();
            // int frames = Math.max(stats.frames, 1);
            // _frames.update(frames);
            // _shaderCreates.update(stats.shaderCreates);
            // _fbCreates.update(stats.frameBufferCreates);
            // _texCreates.update(stats.texCreates);
            // _shaderBinds.update(stats.shaderBinds/frames);
            // _fbBinds.update(stats.frameBufferBinds/frames);
            // _texBinds.update(stats.texBinds/frames);
            // _rQuads.update(stats.quadsRendered/frames);
            // _rTris.update(stats.trisRendered/frames);
            // _shaderFlushes.update(stats.shaderFlushes/frames);
            // stats.reset();
            // _quadShader.update("Quad: " + graphics().ctx().quadShaderInfo());
            // _trisShader.update("Tris: " + graphics().ctx().trisShaderInfo());
        }
    }

    /** The layer that contains this HUD. Add to the scene graph where desired.  */
    val layer: CanvasLayer

    init {
        layer = CanvasLayer(_game.plat.graphics, 1f, 1f)
    }

    /** Configures the font used to display the HUD. Must be called before adding rows.  */
    fun setFont(font: Font): Hud {
        if (!_rows.isEmpty()) throw IllegalStateException("Set font before adding rows.")
        _fmt = _fmt.withFont(font)
        return this
    }

    /** Configures the foreground and background colors. Must be called before adding rows.  */
    fun setColors(textColor: Int, bgColor: Int): Hud {
        if (!_rows.isEmpty()) throw IllegalStateException("Set colors before adding rows.")
        _textColor = textColor
        _bgColor = bgColor
        return this
    }

    /** Adds a static label that spans the width of the HUD.  */
    fun add(label: String, header: Boolean) {
        val layout = _game.plat.graphics.layoutText(label, _fmt)
        _rows.add(object : Row {
            override fun update() {} // noop
            override fun labelWidth(): Float {
                return 0f
            }

            override fun size(): IDimension {
                return layout.size
            }

            override fun render(canvas: Canvas, x: Float, y: Float, valueX: Float) {
                if (header) canvas.drawLine(0f, y - 1, canvas.width, y - 1)
                canvas.fillText(layout, x, y)
                val by = y + layout.size.height
                if (header) canvas.drawLine(0f, by, canvas.width, by)
            }
        })
    }

    /** Adds a changing label that spans the width of the HUD.  */
    fun add(label: Value<*>) {
        _rows.add(object : Row {
            private var _layout: TextLayout? = null

            override fun update() {
                _layout = _game.plat.graphics.layoutText(label.get().toString(), _fmt)
            }

            override fun labelWidth(): Float {
                return 0f
            }

            override fun size(): IDimension {
                return _layout!!.size
            }

            override fun render(canvas: Canvas, x: Float, y: Float, valueX: Float) {
                canvas.fillText(_layout!!, x, y)
            }
        })
    }

    /** Adds a static label and changing value, which will be rendered in two columns.  */
    fun add(label: String, value: Value<*>) {
        val llayout = _game.plat.graphics.layoutText(label, _fmt)
        _rows.add(object : Row {

            private var _vlayout: TextLayout? = null
            private var _size = Dimension()

            override fun update() {
                _vlayout = _game.plat.graphics.layoutText(value.get().toString(), _fmt)
                _size.setSize(llayout.size.width + GAP + _vlayout!!.size.width,
                        Math.max(llayout.size.height, _vlayout!!.size.height))
            }

            override fun labelWidth(): Float {
                return llayout.size.width
            }

            override fun size(): IDimension {
                return _size
            }

            override fun render(canvas: Canvas, x: Float, y: Float, valueX: Float) {
                canvas.fillText(llayout, x, y)
                canvas.fillText(_vlayout!!, valueX, y)
            }
        })
    }

    /** Updates the HUDs rendered image. Call this after all of its values have been updated
     * (usually once per second).  */
    fun update() {
        // update all of our rows and compute layout metrics
        var width = 0f
        var height = 0f
        var labelWidth = 0f
        for (row in _rows) {
            row.update()
            width = Math.max(row.size().width, width)
            labelWidth = Math.max(row.labelWidth(), labelWidth)
            height += row.size().height
        }
        // add in borders
        width += 5 * GAP
        height += GAP * _rows.size + GAP
        // create a new image if necessary
        if (layer.width() < width || layer.height() < height) layer.resize(width, height)
        // clear our image and render our rows
        val canvas = layer.begin()
        canvas.clear()
        canvas.setFillColor(_bgColor).fillRect(0f, 0f, width, height)
        canvas.setStrokeColor(_textColor).setFillColor(_textColor)
        val x = GAP
        var y = GAP
        val valueX = labelWidth + 2 * GAP
        for (row in _rows) {
            row.render(canvas, x, y, valueX)
            y += row.size().height + GAP
        }
        layer.end()
    }

    protected interface Row {
        fun update()
        fun labelWidth(): Float
        fun size(): IDimension
        fun render(canvas: Canvas, x: Float, y: Float, valueX: Float)
    }

    protected val _rows: MutableList<Row> = ArrayList()

    protected var _fmt = TextFormat(Font("Helvetica", 12f))
    protected var _textColor = 0xFF000000.toInt()
    protected var _bgColor = 0xFFFFFFFF.toInt()

    companion object {

        protected val GAP = 5f
    }
}
