package tripleklay.demo.core

import klay.core.Game
import tripleklay.demo.core.anim.AnimDemo
import tripleklay.demo.core.anim.FlickerDemo
import tripleklay.demo.core.ui.*
import tripleklay.game.ScreenStack
import tripleklay.ui.*
import tripleklay.ui.layout.AxisLayout
import tripleklay.ui.layout.TableLayout

/**
 * Displays a top-level menu of our various demo screens.
 */
class DemoMenuScreen(protected val _stack: ScreenStack) : ScreenStack.UIScreen(TripleDemo.game!!.plat) {
    protected val _rlabels: Array<String>
    protected val _screens: Array<DemoScreen?>

    init {
        _rlabels = arrayOf("tripleplay.ui", "", "", "", "", "tripleplay.anim", "tripleplay.game", "tripleplay.entity", "tripleplay.particle", "tripleplay.flump", "tripleplay.util")
        _screens = arrayOf<DemoScreen?>(
                // tripleplay.ui
                MiscDemo(), LabelDemo(), /* TODO(cdi) MenuDemo(), */ SliderDemo(), SelectorDemo(), BackgroundDemo(), ScrollerDemo(), TabsDemo(), HistoryGroupDemo(), LayoutDemo(), FlowLayoutDemo(), BorderLayoutDemo(), TableLayoutDemo(), AbsoluteLayoutDemo(), null,
                // tripleplay.anim
                null, // TODO(cdi) readd when ported: FramesDemo(),
                AnimDemo(), FlickerDemo()
                // tripleplay.game
                // TODO(cdi) ScreensDemo(_stack), ScreenSpaceDemo(), null,
                // tripleplay.entity
                // TODO(cdi) reactivate after port of tripleplay.entity
                // new AsteroidsDemo(), null, null,
                // tripleplay.particle
                // TODO(cdi) reactivate after port of tripleplay.particle
                // new FountainDemo(), new FireworksDemo(), null,
                // tripleplay.flump
                // TODO(cdi) reactivate after port of tripleplay.flump
                // new FlumpDemo(), null, null,
                // tripleplay.util
                // TODO(cdi) ColorsDemo(), InterpDemo(), null
        )
    }

    override fun game(): Game {
        return TripleDemo.game!!
    }

    override fun wasAdded() {
        super.wasAdded()
        val root = iface.createRoot(AxisLayout.vertical().gap(15),
                SimpleStyles.newSheet(game().plat.graphics), layer)
        root.addStyles(Style.BACKGROUND.`is`(
                Background.bordered(0xFFCCCCCC.toInt(), 0xFF99CCFF.toInt(), 5f).inset(5f, 10f)))
        root.setSize(size())
        root.add(Label("Tripleklay Demos").addStyles(Style.FONT.`is`(DemoScreen.TITLE_FONT)))

        val grid = Group(TableLayout(
                TableLayout.COL.alignRight(),
                TableLayout.COL.stretch(),
                TableLayout.COL.stretch(),
                TableLayout.COL.stretch()).gaps(10, 10))
        root.add(grid)

        var shown = 0
        val toShow = if (TripleDemo.mainArgs.size == 0)
            -1
        else
            Integer.parseInt(TripleDemo.mainArgs[0])

        for (ii in _screens.indices) {
            if (ii % 3 == 0) grid.add(Label(_rlabels[ii / 3]))
            val screen = _screens[ii]
            if (screen == null) {
                grid.add(Shim(1f, 1f))
            } else {
                grid.add(Button(screen.name()).onClick({
                    _stack.push(screen)
                    screen.back.clicked().connect({
                        _stack.remove(screen)
                    })
                }))
                // push this screen immediately if it was specified on the command line
                if (shown++ == toShow) _stack.push(screen, ScreenStack.NOOP)
            }
        }
    }

    override fun wasRemoved() {
        super.wasRemoved()
        iface.disposeRoots()
    }

    protected fun screen(title: String, factory: ScreenFactory): Button {
        return Button(title).onClick({
            val screen = factory.apply()
            _stack.push(screen)
            screen.back.clicked().connect({
                _stack.remove(screen)
            })
        })
    }

    protected interface ScreenFactory {
        fun apply(): DemoScreen
    }
}
