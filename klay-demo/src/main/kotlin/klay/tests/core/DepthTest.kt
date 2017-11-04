package klay.tests.core

import klay.scene.ImageLayer

class DepthTest(game: TestsGame) : Test(game, "Depth", "Tests that layers added with non-zero depth are inserted/rendered in proper order.") {

    override fun init() {
        val depths = intArrayOf(0, -1, 1, 3, 2, -4, -3, 4, -2)
        val fills = intArrayOf(0xFF99CCFF.toInt(), 0xFFFFFF33.toInt(), 0xFF9933FF.toInt(), 0xFF999999.toInt(), 0xFFFF0033.toInt(), 0xFF00CC00.toInt(), 0xFFFF9900.toInt(), 0xFF0066FF.toInt(), 0x0FFCC6666.toInt())
        val width = 200f
        val height = 200f
        for (ii in depths.indices) {
            val depth = depths[ii]
            val canvas = game.graphics.createCanvas(width, height)
            canvas.setFillColor(fills[ii]).fillRect(0f, 0f, width, height)
            canvas.setFillColor(0xFF000000.toInt()).drawText(depth.toString() + "/" + ii, 5f, 15f)
            val layer = ImageLayer(canvas.toTexture())
            layer.setDepth(depth.toFloat()).setTranslation(225f - 50f * depth, 125f + 25f * depth)
            game.rootLayer.add(layer)
        }
    }
}
