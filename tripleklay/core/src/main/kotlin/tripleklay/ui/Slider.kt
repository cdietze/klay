package tripleklay.ui

import klay.scene.Layer
import klay.scene.Pointer
import pythagoras.f.Dimension
import pythagoras.f.IPoint
import pythagoras.f.Point
import react.Signal
import react.SignalView
import react.UnitSlot
import react.Value

/**
 * Displays a bar and a thumb that can be slid along the bar, representing a floating point value
 * between some minimum and maximum.
 */
class Slider @JvmOverloads constructor(value: Float = 0f, min: Float = 0f, max: Float = 0f) : Widget<Slider>() {
    /** Holds the minimum and maximum values for the slider.  */
    class Range(val min: Float, val max: Float) {
        val range: Float

        init {
            if (min > max) throw IllegalArgumentException()
            this.range = max - min
        }
    }

    /** The value of the slider.  */
    val value: Value<Float>

    /** The range of the slider.  */
    val range: Value<Range>

    init {
        this.value = Value.create(value)
        range = Value.create(Range(min, max))
        // update our display if the slider value is changed externally
        val updateThumb: UnitSlot = { updateThumb() }
        this.value.connect(updateThumb)
        range.connect(updateThumb)
    }

    /**
     * Constrains the possible slider values to the given increment. For example, an increment of 1
     * would mean the possible sliders values are [.min], `min() + 1`, etc, up to
     * [.max]. Note this only affects internal updates from pointer or mouse handling. The
     * underlying [.value] may be updated arbitrarily.
     */
    fun setIncrement(increment: Float): Slider {
        _increment = increment
        return this
    }

    /**
     * Getter for the slider increment value. Note that it is a Float object and may be null.
     */
    fun increment(): Float? {
        return _increment
    }

    /** A signal that is emitted when the user has released their finger/pointer after having
     * started adjusting the slider. [.value] will contain the correct current value at the
     * time this signal is emitted.  */
    fun clicked(): SignalView<Slider> {
        return _clicked
    }

    /** Returns our maximum allowed value.  */
    fun max(): Float {
        return range.get().max
    }

    /** Returns our minimum allowed value.  */
    fun min(): Float {
        return range.get().min
    }

    override val styleClass: Class<*>
        get() = Slider::class.java

    override fun wasRemoved() {
        super.wasRemoved()
        if (_barInst != null) {
            _barInst!!.close()
            _barInst = null
        }
        // the thumb is just an image layer and will be destroyed when we are
    }

    override fun createLayoutData(hintX: Float, hintY: Float): LayoutData {
        return SliderLayoutData()
    }

    override fun createBehavior(): Behavior<Slider>? {
        return object : Behavior.Track<Slider>(this@Slider) {
            public override fun onTrack(anchor: Point, drag: Point) {
                setValueFromPointer(drag.x)
            }

            override fun onRelease(iact: Pointer.Interaction): Boolean {
                super.onRelease(iact)
                return true // always emit a click
            }

            override fun onClick(iact: Pointer.Interaction) {
                _clicked.emit(this@Slider)
            }
        }
    }

    protected fun updateThumb() {
        // bail if not laid out yet, we'll get called again layer
        if (_thumb == null) return
        val r = range.get()
        val thumbPct = (value.get() - r.min) / r.range
        _thumb!!.setTranslation(_thumbLeft + _thumbRange * thumbPct, _thumbY)
    }

    protected fun setValueFromPointer(x: Float) {
        var x = x
        val r = range.get()
        val width = _thumbRange
        x = Math.min(width, x - _thumbLeft)
        var pos = Math.max(x, 0f) / width * r.range
        if (_increment != null) {
            val i = _increment!!
            pos = i * Math.round(pos / i)
        }
        value.update(r.min + pos)
    }

    protected inner class SliderLayoutData : LayoutData() {
        val barWidth = resolveStyle(BAR_WIDTH)
        val barHeight = resolveStyle(BAR_HEIGHT)
        val barBG = resolveStyle(BAR_BACKGROUND)
        val thumbImage = resolveStyle(THUMB_IMAGE)
        val thumbOrigin: IPoint? = resolveStyle(THUMB_ORIGIN)

        override fun computeSize(hintX: Float, hintY: Float): Dimension {
            return Dimension(barWidth + thumbImage.width(),
                    Math.max(barHeight, thumbImage.height()))
        }

        override fun layout(left: Float, top: Float, width: Float, height: Float) {
            // note our thumb metrics
            val thumbWidth = thumbImage.width()
            val thumbHeight = thumbImage.height()
            _thumbRange = width - thumbWidth
            _thumbLeft = left + thumbWidth / 2
            _thumbY = top + height / 2

            // configure our thumb layer
            if (_thumb != null) _thumb!!.close()
            _thumb = thumbImage.render().setDepth(1f)
            layer.add(_thumb!!)
            if (thumbOrigin == null) {
                _thumb!!.setOrigin(thumbWidth / 2, thumbHeight / 2)
            } else {
                _thumb!!.setOrigin(thumbOrigin.x, thumbOrigin.y)
            }

            // configure our bar background instance
            if (_barInst != null) _barInst!!.close()
            if (width > 0 && height > 0) {
                _barInst = barBG.instantiate(Dimension(width - thumbWidth, barHeight))
                _barInst!!.addTo(layer, _thumbLeft, top + (height - barHeight) / 2, 1f)
            }

            // finally update the thumb position
            updateThumb()
        }
    }

    protected val _clicked = Signal<Slider>()

    protected var _thumb: Layer? = null
    protected var _barInst: Background.Instance? = null
    protected var _thumbLeft: Float = 0.toFloat()
    protected var _thumbRange: Float = 0.toFloat()
    protected var _thumbY: Float = 0.toFloat()
    protected var _increment: Float? = null

    companion object {

        /** The width of the bar of an unstretched slider. The slider's preferred width will be this
         * width plus the width of the thumb image (which can extend past the left edge of the bar by
         * half its width and the right edge of the bar by half its width). Inherited.  */
        var BAR_WIDTH = Style.newStyle(true, 100f)

        /** The height of the bar. The slider's preferred height will be the larger of this height and
         * the height of the thumb image. Inherited.  */
        var BAR_HEIGHT = Style.newStyle(true, 5f)

        /** The background that renders the bar (defaults to a black rectangle). Inherited.  */
        var BAR_BACKGROUND = Style.newStyle(true, Background.solid(0xFF000000.toInt()))

        /** The image to use for the slider thumb. Inherited.  */
        var THUMB_IMAGE = Style.newStyle(true, createDefaultThumbImage())

        /** The origin of the thumb image (used to center the thumb image over the tray). If left as
         * the default (null), the center of the thumb image will be used as its origin. Inherited.  */
        var THUMB_ORIGIN = Style.newStyle<IPoint?>(false, null)

        protected fun createDefaultThumbImage(): Icon {
            return Icons.solid(0xFF000000.toInt(), 24f)
        }
    }
}
/** Constructs a new slider with empty range and zero value.  */