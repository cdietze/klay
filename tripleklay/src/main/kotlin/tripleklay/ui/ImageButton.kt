package tripleklay.ui

import euklid.f.Dimension
import klay.core.TileSource
import klay.scene.ImageLayer
import react.SignalView
import react.SignalViewListener
import kotlin.reflect.KClass

/**
 * A button that uses images for its different states.
 */
class ImageButton
/** Creates a button with the supplied up and down images.  */
constructor(up: TileSource, down: TileSource = up) : Widget<ImageButton>(), Clickable<ImageButton> {

    private val _ilayer = ImageLayer()
    private var _up: TileSource
    private var _down: TileSource

    init {
        layer.add(_ilayer)
        _up = up
        _up.tileAsync().onSuccess({ invalidate() })
        _down = down
        _down.tileAsync().onSuccess({ invalidate() })
    }

    /** Configures the image used in our up state.  */
    fun setUp(up: TileSource): ImageButton {
        _up = up
        _up.tileAsync().onSuccess({ invalidate() })
        return this
    }

    /** Configures the image used in our down state.  */
    fun setDown(down: TileSource): ImageButton {
        _down = down
        _down.tileAsync().onSuccess({ invalidate() })
        return this
    }

    /** Programmatically triggers a click of this button. This triggers the action sound, but does
     * not cause any change in the button's visualization. *Note:* this does not check the
     * button's enabled state, so the caller must handle that if appropriate.  */
    override fun click() {
        (_behave as Behavior.Click<ImageButton>).click()
    }

    /** A convenience method for registering a click handler. Assumes you don't need the result of
     * [SignalView.connect], because it throws it away.  */
    fun onClick(onClick: SignalViewListener<ImageButton>): ImageButton {
        clicked().connect(onClick)
        return this
    }

    override fun clicked(): SignalView<ImageButton> {
        return (_behave as Behavior.Click<ImageButton>).clicked
    }

    override fun toString(): String {
        return "ImageButton(" + size() + ")"
    }

    override val styleClass: KClass<*>
        get() = ImageButton::class

    override fun createBehavior(): Behavior<ImageButton>? {
        return Behavior.Click(this)
    }

    override fun computeSize(ldata: LayoutData, hintX: Float, hintY: Float): Dimension {
        return if (_up.isLoaded)
            Dimension(_up.tile().width, _up.tile().height)
        else
            Dimension()
    }

    override fun layout(ldata: LayoutData, left: Float, top: Float,
                        width: Float, height: Float) {
        _ilayer.setTile(if (isSelected) _down.tile() else _up.tile())
        _ilayer.setTranslation(left, top)
    }
}
/** Creates a button with the supplied image for use in up and down states.  */
