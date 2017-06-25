package tripleklay.demo.core.util

import tripleklay.demo.core.DemoScreen
import tripleklay.ui.*
import tripleklay.ui.layout.AxisLayout
import tripleklay.ui.layout.TableLayout
import tripleklay.util.Colors

class ColorsDemo : DemoScreen() {
    override fun name(): String {
        return "Colors"
    }

    override fun title(): String {
        return "Util: Colors"
    }

    override fun createIface(root: Root): Group {
        return Group(AxisLayout.vertical(), Style.HALIGN.center).add(
                Group(TableLayout(TableLayout.COL.fixed().alignRight(),
                        TableLayout.COL.fixed().alignLeft()).gaps(1, 5)).add(
                        Label("White"), createLabel(Colors.WHITE),
                        Label("Light Gray"), createLabel(Colors.LIGHT_GRAY),
                        Label("Gray"), createLabel(Colors.GRAY),
                        Label("Dark Gray"), createLabel(Colors.DARK_GRAY),
                        Label("Black"), createLabel(Colors.BLACK),
                        Label("Red"), createLabel(Colors.RED),
                        Label("Pink"), createLabel(Colors.PINK),
                        Label("Orange"), createLabel(Colors.ORANGE),
                        Label("Yellow"), createLabel(Colors.YELLOW),
                        Label("Green"), createLabel(Colors.GREEN),
                        Label("Magenta"), createLabel(Colors.MAGENTA),
                        Label("Cyan"), createLabel(Colors.CYAN),
                        Label("Blue"), createLabel(Colors.BLUE)))
    }

    protected fun createLabel(baseColor: Int): Label {
        return Label(createSampler(baseColor))
    }

    protected fun createSampler(baseColor: Int): Icon {
        val size = 16f
        val canvas = graphics().createCanvas(size * 17f, size)
        var lighter = baseColor
        for (ii in 0..8) {
            canvas.setFillColor(lighter)
            canvas.fillRect((size * (ii + 8)), 0f, size, size)
            lighter = Colors.brighter(lighter)
        }
        var darker = baseColor
        for (ii in 0..7) {
            canvas.setFillColor(darker)
            canvas.fillRect((size * (7 - ii)), 0f, size, size)
            darker = Colors.darker(darker)
        }

        canvas.setStrokeColor(Colors.BLACK)
        canvas.strokeRect((size * 8), 0f, (size - 1), (size - 1))
        return Icons.image(canvas.toTexture())
    }
}
