package tripleklay.demo.core.ui

import tripleklay.demo.core.DemoScreen
import tripleklay.ui.*
import tripleklay.ui.layout.AbsoluteLayout
import tripleklay.ui.layout.AxisLayout
import tripleklay.ui.layout.TableLayout
import tripleklay.ui.layout.TableLayout.Companion.COL

/**
 * Displays some layouts and their configuration options.
 */
class LayoutDemo : DemoScreen() {
    override fun name(): String {
        return "Layouts"
    }

    override fun title(): String {
        return "UI: Various Layouts"
    }

    override fun createIface(root: Root): Group {
        val main = TableLayout(COL.stretch(), COL).gaps(15, 15)
        val alignDemo = TableLayout(
                COL.alignLeft(), COL.alignRight(), COL.stretch()).gaps(5, 5)
        val fixedDemo = TableLayout(COL.fixed(), COL, COL.stretch()).gaps(5, 5)
        val minWidthDemo = TableLayout(
                COL.minWidth(100f), COL.minWidth(100f).stretch(), COL).gaps(5, 5)

        val greyBg = Styles.make(Style.BACKGROUND.`is`(Background.solid(0xFFCCCCCC.toInt()).inset(5f)))
        val greenBg = Styles.make(Style.BACKGROUND.`is`(Background.solid(0xFFCCFF99.toInt()).inset(5f)))

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
                        AbsoluteLayout.at(Label("+50+20"), 50f, 20f),
                        AbsoluteLayout.at(Button("150x35+150+50"), 150f, 50f, 150f, 35f)))

        return iface
    }
}
