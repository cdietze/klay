package tripleklay.demo.core

import klay.core.*

import tripleklay.game.ScreenStack
import tripleklay.ui.*
import tripleklay.ui.layout.AxisLayout

/**
 * The base class for all demo screens.
 */
abstract class DemoScreen : ScreenStack.UIScreen(TripleDemo.game!!.plat) {

    lateinit var back: Button

    override fun game(): Game {
        return TripleDemo.game!!
    }

    override fun wasAdded() {
        super.wasAdded()
        val root = iface.createRoot(
                AxisLayout.vertical().gap(0).offStretch(), stylesheet(), layer)
        root.addStyles(Style.BACKGROUND.`is`(background()), Style.VALIGN.top)
        root.setSize(size())
        val bg = Background.solid(0xFFCC99FF.toInt()).inset(0f, 0f, 5f, 0f)
        this.back = Button("Back")
        root.add(Group(AxisLayout.horizontal(), Style.HALIGN.left, Style.BACKGROUND.`is`(bg)).add(
                this.back,
                Label(title()).addStyles(Style.FONT.`is`(TITLE_FONT), Style.HALIGN.center).setConstraint(AxisLayout.stretched())))
        if (subtitle() != null) root.add(Label(subtitle()))
        val iface = createIface(root)
        if (iface != null) root.add(iface.setConstraint(AxisLayout.stretched()))
    }

    override fun wasRemoved() {
        super.wasRemoved()
        iface.disposeRoots()
        layer.disposeAll()
    }

    /** The label to use on the button that displays this demo.  */
    abstract fun name(): String

    /** Returns the title of this demo.  */
    protected abstract fun title(): String

    /** Returns an explanatory subtitle for this demo, or null.  */
    protected fun subtitle(): String? {
        return null
    }

    /** Override this method and return a group that contains your main UI, or null. Note: `root` is provided for reference, the group returned by this call will automatically be
     * added to the root group.  */
    protected abstract fun createIface(root: Root): Group?

    /** Returns the stylesheet to use for this screen.  */
    protected fun stylesheet(): Stylesheet {
        return SimpleStyles.newSheet(game().plat.graphics)
    }

    /** Returns the background to use for this screen.  */
    protected fun background(): Background {
        return Background.bordered(0xFFCCCCCC.toInt(), 0xFFCC99FF.toInt(), 5f).inset(5f)
    }

    protected fun assets(): Assets {
        return game().plat.assets
    }

    protected fun graphics(): Graphics {
        return game().plat.graphics
    }

    protected fun input(): Input {
        return game().plat.input
    }

    protected fun json(): Nothing {
        return game().plat.json
    }

    protected fun log(): klay.core.Log {
        return game().plat.log
    }

    companion object {
        val TITLE_FONT = Font("Helvetica", 24f)
    }
}
