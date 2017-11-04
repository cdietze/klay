package tripleklay.demo.core.game

import tripleklay.demo.core.TripleDemo
import tripleklay.game.ScreenStack
import tripleklay.ui.*
import tripleklay.ui.layout.AxisLayout

/**
 * A screen that contains UI elements.
 */
abstract class TestScreen(protected val _depth: Int) : ScreenStack.UIScreen(TripleDemo.game!!.plat) {

    override fun wasAdded() {
        super.wasAdded()
        val root = iface.createRoot(createLayout(), stylesheet(), layer)
        root.addStyles(Style.BACKGROUND.`is`(background()))
        root.setSize(size())
        createIface(root)
    }

    override fun wasShown() {
        super.wasShown()
        game().plat.log.info(this.toString() + ".wasShown()")
    }

    override fun wasHidden() {
        super.wasHidden()
        game().plat.log.info(this.toString() + ".wasHidden()")
    }

    override fun wasRemoved() {
        super.wasRemoved()
        game().plat.log.info(this.toString() + ".wasRemoved()")
        iface.disposeRoots()
        layer.close()
    }

    override fun showTransitionCompleted() {
        super.showTransitionCompleted()
        game().plat.log.info(this.toString() + ".showTransitionCompleted()")
    }

    override fun hideTransitionStarted() {
        super.hideTransitionStarted()
        game().plat.log.info(this.toString() + ".hideTransitionStarted()")
    }

    /** Returns the stylesheet to use for this screen.  */
    protected fun stylesheet(): Stylesheet {
        return SimpleStyles.newSheet(game().plat.graphics)
    }

    /** Creates the layout for the interface root. The default is a vertical axis layout.  */
    protected fun createLayout(): Layout {
        return AxisLayout.vertical()
    }

    /** Returns the background to use for this screen.  */
    protected fun background(): Background {
        val borderColor = if (_depth % 2 == 0) 0xFF99CCFF.toInt() else 0xFFCC99FF.toInt()
        return Background.bordered(0xFFCCCCCC.toInt(), borderColor, 15f).inset(15f, 10f)
    }

    /** Override this method and create your UI in it. Add elements to `root`.  */
    protected abstract fun createIface(root: Root)
}
