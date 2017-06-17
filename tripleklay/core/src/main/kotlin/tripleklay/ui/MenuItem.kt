package tripleklay.ui

import klay.scene.Pointer
import pythagoras.f.Dimension
import react.Closeable
import react.SignalView
import react.Value

/**
 * An item in a menu. This overrides clicking with a two phase click behavior: clicking an
 * unselected menu item selects it; clicking a selected menu item triggers it.
 */
class MenuItem
/**
 * Creates a new menu item with the given label and icon.
 */
@JvmOverloads constructor(label: String, icon: Icon = null) : TextWidget<MenuItem>(), Togglable<MenuItem> {
    /** Modes of text display.  */
    enum class ShowText {
        ALWAYS, NEVER, WHEN_ACTIVE
    }

    /** The text shown.  */
    val text = Value.create(null)

    /** The icon shown.  */
    val icon = Value.create(null)

    init {
        this.text.update(label)
        this.text.connect(textDidChange())
        // update after connect so we trigger iconDidChange, in case our icon is a not-ready-image
        this.icon.connect(iconDidChange())
        this.icon.update(icon)
    }

    /**
     * Sets the text display mode for this menu item.
     */
    fun showText(value: ShowText): MenuItem {
        _showText = value
        invalidate()
        return this
    }

    /**
     * Sets the menu item to show its text when the item is selected
     */
    fun hideTextWhenInactive(): MenuItem {
        return showText(ShowText.WHEN_ACTIVE)
    }

    /**
     * Sets the menu item to only use an icon and no tex. This is useful for layouts that show the
     * text of the selected item in a central location.
     */
    fun hideText(): MenuItem {
        return showText(ShowText.NEVER)
    }

    /**
     * Sets the preferred size of the menu item.
     */
    fun setPreferredSize(wid: Float, hei: Float): MenuItem {
        _preferredSize.setSize(wid, hei)
        invalidate()
        return this
    }

    /**
     * Gets the signal that dispatches when a menu item is triggered. Most callers will just
     * connect to [Menu.itemTriggered].
     */
    fun triggered(): SignalView<MenuItem> {
        return toToggle().clicked
    }

    // from Togglable and Clickable
    override fun selected(): Value<Boolean> {
        return toToggle().selected
    }

    override fun clicked(): SignalView<MenuItem> {
        return toToggle().clicked
    }

    override fun click() {
        toToggle().click()
    }

    protected fun trigger() {
        toToggle().click()
    }

    protected fun setRelay(relay: Closeable) {
        _relay.close()
        _relay = relay
    }

    protected fun toToggle(): Behavior.Toggle<MenuItem> {
        return _behave as Behavior.Toggle<MenuItem>
    }

    protected override val styleClass: Class<*>
        get() = MenuItem::class.java

    override fun icon(): Icon? {
        return icon.get()
    }

    override fun createBehavior(): Behavior<MenuItem>? {
        return object : Behavior.Toggle<MenuItem>(this) {
            override fun onStart(iact: Pointer.Interaction) {}
            override fun onDrag(iact: Pointer.Interaction) {}
            override fun onEnd(iact: Pointer.Interaction) {}
            override fun onClick(iact: Pointer.Interaction) {
                click()
            }
        }
    }

    override fun text(): String? {
        when (_showText) {
            MenuItem.ShowText.NEVER -> return ""
            MenuItem.ShowText.WHEN_ACTIVE -> return if (isSelected) text.get() else ""
            MenuItem.ShowText.ALWAYS,
            else -> return text.get()
        }
    }

    override fun createLayoutData(hintX: Float, hintY: Float): Element.LayoutData {
        return Element.SizableLayoutData(super.createLayoutData(hintX, hintY), _preferredSize)
    }

    protected var _relay = Closeable.Util.NOOP

    /** Size override.  */
    protected val _preferredSize = Dimension(0f, 0f)
    /** Text display mode.  */
    protected var _showText = ShowText.ALWAYS
}
/**
 * Creates a new menu item with the given label.
 */
