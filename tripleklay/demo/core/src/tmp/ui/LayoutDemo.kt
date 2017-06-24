package tripleklay.demo.ui

import tripleklay.demo.DemoScreen
import tripleklay.ui.Background
import tripleklay.ui.Button
import tripleklay.ui.Group
import tripleklay.ui.Label
import tripleklay.ui.Root
import tripleklay.ui.Shim
import tripleklay.ui.Style
import tripleklay.ui.Styles
import tripleklay.ui.layout.AbsoluteLayout
import tripleklay.ui.layout.AxisLayout
import tripleklay.ui.layout.TableLayout

import tripleplay.ui.layout.TableLayout.COL

/**
 * Displays some layouts and their configuration options.
 */
class LayoutDemo : DemoScreen() {
    protected fun name(): String {
        return "Layouts"
    }

    protected fun title(): String {
        return "UI: Various Layouts"
    }

    protected fun createIface(root: Root): Group {
        val main = TableLayout(COL.stretch(), COL).gaps(15, 15)
        val alignDemo = TableLayout(
                COL.alignLeft(), COL.alignRight(), COL.stretch()).gaps(5, 5)
        val fixedDemo = TableLayout(COL.fixed(), COL, COL.stretch()).gaps(5, 5)
        val minWidthDemo = TableLayout(
                COL.minWidth(100), COL.minWidth(100).stretch(), COL).gaps(5, 5)

        val greyBg = Styles.make(Style.BACKGROUND.`is`(Background.solid(0xFFCCCCCC.toInt()).inset(5)))
        val greenBg = Styles.make(Style.BACKGROUND.`is`(Background.solid(0xFFCCFF99.toInt()).inset(5)))

        val iface = Group(AxisLayout.vertical().offStretch()).add(
                Shim(15f, 15f),
                Label("Table Layout"),
                Group(main, greyBg).add(
                        Label("This column is stretched"),
                        Label("This column is not"),

                        Group(TableLayout(COL, COL).gaps(5, 5), greenBg).add(
                                Label("Upper left"), Label("Upper right"),
                                Label("Lower left"), Label("Lower right")),

                        Group(alignDemo, greenBg).add(
                                Button("Foo"),
                                Button("Bar"),
                                Button("Baz"),
                                Button("Foozle"),
                                Button("Barzle"),
                                Button("Bazzle")),

                        Group(fixedDemo, greenBg).add(
                                Button("Fixed"),
                                Button("Free"),
                                Button("Stretch+free"),
                                Button("Fixed"),
                                Button("Free"),
                                Button("Stretch+free")),

                        Group(minWidthDemo, greenBg).add(
                                Button("Min"),
                                Button("M+stretch"),
                                Button("Free"),
                                Button("Min"),
                                Button("M+stretch"),
                                Button("Free"))),

                Shim(15f, 15f),
                Label("Absolute Layout"),
                Group(AbsoluteLayout(), greyBg).add(
                        AbsoluteLayout.at(Label("+50+20"), 50, 20),
                        AbsoluteLayout.at(Button("150x35+150+50"), 150, 50, 150, 35)))

        return iface
    }
}
