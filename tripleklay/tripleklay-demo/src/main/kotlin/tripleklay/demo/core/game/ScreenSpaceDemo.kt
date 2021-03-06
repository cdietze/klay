package tripleklay.demo.core.game

import klay.core.Clock
import react.UnitSlot
import tripleklay.demo.core.DemoScreen
import tripleklay.demo.core.TripleDemo
import tripleklay.game.ScreenSpace
import tripleklay.ui.*
import tripleklay.ui.layout.AxisLayout

class ScreenSpaceDemo : DemoScreen() {

    override fun name(): String {
        return "ScreenSpace"
    }

    override fun title(): String {
        return "ScreenSpace and Transitions"
    }

    override fun createIface(root: Root): Group {
        // things are a bit hacky here because we're bridging between the ScreenStack world (which
        // is used for the demo) and the ScreenSpace world; normally a game would use *only*
        // ScreenStack or ScreenSpace, but for this demo we want to show both; so we put no UI in
        // our ScreenStack screen, and let the root ScreenSpace screen do the driving
        _space = ScreenSpace(TripleDemo.game!!, TripleDemo.game!!.rootLayer)
        closeOnHide(paint.connect({ clock: Clock ->
            // bind the pos of our stack screen to the position of our top space screen
            if (_top != null) {
                layer.setTx(_top!!.layer.tx())
                layer.setTy(_top!!.layer.ty())
            }
        }))
        return Group(AxisLayout.vertical())
    }

    override fun showTransitionCompleted() {
        _top = createScreen(0)
        _space!!.add(_top!!, ScreenSpace.FLIP)
    }

    override fun hideTransitionStarted() {
        _space!!.pop(_top!!)
        _top = null
    }

    override fun wasRemoved() {
        super.wasRemoved()
        _space = null
    }

    protected fun addScreen(dir: ScreenSpace.Dir): UnitSlot {
        return {
            val screen = createScreen(_space!!.screenCount())
            _space!!.add(screen, dir)
        }
    }

    protected fun createScreen(id: Int): ScreenSpace.Screen {
        return object : ScreenSpace.UIScreen(TripleDemo.game!!) {
            override fun toString(): String {
                return "Screen-" + id
            }

            protected override fun createUI() {
                val root = iface.createRoot(AxisLayout.vertical(),
                        SimpleStyles.newSheet(graphics()), layer)
                val blue = id * 0x16
                root.addStyles(Style.BACKGROUND.`is`(Background.solid(0xFF333300.toInt() + blue)))
                root.add(Label(toString()))
                root.add(Button("Up").onClick(addScreen(ScreenSpace.UP)))
                root.add(Button("Down").onClick(addScreen(ScreenSpace.DOWN)))
                root.add(Button("Left").onClick(addScreen(ScreenSpace.LEFT)))
                root.add(Button("Right").onClick(addScreen(ScreenSpace.RIGHT)))
                root.add(Button("In").onClick(addScreen(ScreenSpace.IN)))
                root.add(Button("Out").onClick(addScreen(ScreenSpace.OUT)))
                root.add(Button("Flip").onClick(addScreen(ScreenSpace.FLIP)))
                val self = this
                root.add(Shim(30f, 30f), Button("Pop").onClick({
                    if (_top === self) {
                        this@ScreenSpaceDemo.back.click()
                    } else {
                        _space!!.pop(self)
                    }
                }))
                root.setSize(size())
            }
        }
    }

    protected var _space: ScreenSpace? = null
    protected var _top: ScreenSpace.Screen? = null
}
