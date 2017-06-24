package tripleklay.demo.util

import klay.core.Canvas

import tripleklay.demo.DemoScreen
import tripleklay.ui.Group
import tripleklay.ui.Icon
import tripleklay.ui.Icons
import tripleklay.ui.Label
import tripleklay.ui.Root
import tripleklay.ui.Style
import tripleklay.ui.layout.AxisLayout
import tripleklay.ui.layout.TableLayout
import tripleklay.util.Colors

class ColorsDemo : DemoScreen() {
    protected fun name(): String {
        return "Colors"
    }

    protected fun title(): String {
        return "Util: Colors"
    }

    protected fun createIface(root: Root): Group {
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
        val size = 16
        val canvas = graphics().createCanvas(size * 17, size)
        var lighter = baseColor
        for (ii in 0..8) {
            canvas.setFillColor(lighter)
            canvas.fillRect((size * (ii + 8)).toFloat(), 0f, size.toFloat(), size.toFloat())
            lighter = Colors.brighter(lighter)
        }
        var darker = baseColor
        for (ii in 0..7) {
            canvas.setFillColor(darker)
            canvas.fillRect((size * (7 - ii)).toFloat(), 0f, size.toFloat(), size.toFloat())
            darker = Colors.darker(darker)
        }

        canvas.setStrokeColor(Colors.BLACK)
        canvas.strokeRect((size * 8).toFloat(), 0f, (size - 1).toFloat(), (size - 1).toFloat())
        return Icons.image(canvas.toTexture())
    }
}
