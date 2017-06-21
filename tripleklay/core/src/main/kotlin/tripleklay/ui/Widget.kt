package tripleklay.ui

/**
 * The base class for all user interface widgets. Provides helper methods for managing a canvas
 * into which a widget is rendered when its state changes.
 */
abstract class Widget<T : Widget<T>> protected constructor() : Element<T>() {
    protected val _behave: Behavior<T>?

    init {
        _behave = createBehavior()
        if (_behave != null) {
            // absorbs clicks and do not descend (propagate clicks to sublayers)
            set(Element.Flag.HIT_DESCEND, false)
            set(Element.Flag.HIT_ABSORB, true)
            // wire up our behavior as a layer listener
            layer.events().connect(_behave)
        }
    }

    override fun layout() {
        super.layout()
        _behave?.layout()
    }

    /**
     * Creates the behavior for this widget, if any. Defaults to returning null, which means no
     * behavior. This is called once, in the widget's constructor.
     */
    protected open fun createBehavior(): Behavior<T>? {
        return null
    }
}
