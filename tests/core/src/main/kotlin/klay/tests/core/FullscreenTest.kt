package klay.tests.core

class FullscreenTest(game: TestsGame) : Test(game, "Full Screen", "Tests support for full screen modes") {

    class Mode {
        var width: Int = 0
        var height: Int = 0
        var depth: Int = 0
        override fun toString(): String {
            return "" + width + "x" + height + "x" + depth
        }
    }

    interface Host {
        fun enumerateModes(): Array<Mode>
        fun setMode(mode: Mode)
    }

    override fun available(): Boolean {
        return host != null
    }

    override fun init() {
        val spacing = 5f
        var y = spacing
        var x = spacing
        var nextX = spacing
        for (mode in host!!.enumerateModes()) {
            val button = game.ui.createButton(mode.toString(), { host!!.setMode(mode) })
            game.rootLayer.add(button)
            if (y + button.height() + spacing >= game.graphics.viewSize.height) {
                x = nextX + spacing
                y = spacing
            }
            button.setTranslation(x, y)
            y += button.height() + spacing
            nextX = Math.max(nextX, x + button.width())
        }
    }

    companion object {
        var host: Host? = null
    }
}
