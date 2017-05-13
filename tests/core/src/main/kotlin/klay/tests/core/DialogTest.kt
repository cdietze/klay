package klay.tests.core

import klay.core.Keyboard
import klay.scene.ImageLayer

internal class DialogTest(game: TestsGame) : Test(game, "Dialog", "Tests system dialog & text entry support.") {

    override fun init() {
        val left = 50f
        var x = left
        var y = 50f
        val instructions = "Click one of the buttons below to display the text entry UI:"
        val instLayer = ImageLayer(game.ui.formatText(instructions, false))
        game.rootLayer.addAt(instLayer, x, y)
        y += 20f

        var last = game.storage.getItem("last_text")
        if (last == null || last.isEmpty()) last = "..."

        val outputLayer = ImageLayer(game.ui.formatText(last!!, false))
        val onDialogResult = { result: Any? ->
            val text = result?.toString() ?: "canceled"
            if (text.isNotEmpty()) outputLayer.setTile(game.ui.formatText(text, false))
            if (result is String) game.storage.setItem("last_text", result)
        }

        x = left
        for (type in Keyboard.TextType.values()) {
            val button = game.ui.createButton(type.toString(), Runnable { game.input.getText(type, "Enter $type text:", "").onSuccess(onDialogResult) })
            game.rootLayer.addAt(button, x, y)
            x += button.width() + 10
        }
        y += 50f

        game.rootLayer.addAt(outputLayer, left, y)
        y += 40f

        val instr2 = "Click a button below to show a system dialog:"
        game.rootLayer.addAt(ImageLayer(game.ui.formatText(instr2, false)), left, y)
        y += 20f

        x = left
        var button = game.ui.createButton("OK Only", Runnable {
            game.input.sysDialog("OK Only Dialog", "This in an OK only dialog.\n" +
                    "With hard line broken text.\n\n" +
                    "And hopefully a blank line before this one.", "Cool!", null).onSuccess(onDialogResult)
        })
        game.rootLayer.addAt(button, x, y)
        x += button.width() + 10

        button = game.ui.createButton("OK Cancel", Runnable {
            game.input.sysDialog("OK Cancel Dialog", "This is an OK Cancel dialog.\n" +
                    "With hard line breaks.\n\n" +
                    "And hopefully a blank line before this one.", "Cool!", "Yuck!").onSuccess(onDialogResult)
        })
        game.rootLayer.addAt(button, x, y)
        x += button.width() + 10
        y += 50f
    }
}
