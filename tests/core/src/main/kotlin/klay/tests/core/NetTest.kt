package klay.tests.core

import klay.core.Keyboard
import klay.core.Net
import klay.core.Platform
import klay.core.TextBlock
import klay.core.buffers.ByteBuffer
import klay.scene.ImageLayer
import react.RFuture
import react.Slot

class NetTest(game: TestsGame) : Test(game, "Net", "Tests network support.") {

    private var output: ImageLayer? = null
    private var lastPostURL: String? = null
    private var _websock: Net.WebSocket? = null

    override fun init() {
        output = ImageLayer()
        game.rootLayer.addAt(output!!, 10f, 60f)
        displayText("HTTP response shown here.")

        var x = 10f
        x = addButton("Google", { loadURL("http://www.google.com/") }, x, 10f)

        x = addButton("Enter URL", {
            getText("Enter URL:").onSuccess(object : TextCB() {
                override fun gotText(url: String) {
                    loadURL(url)
                }
            })
        }, x, 10f)

        x = addButton("Post Test", {
            getText("Enter POST body:").onSuccess(object : TextCB() {
                override fun gotText(data: String) {
                    val b = game.net.req("http://www.posttestserver.com/post.php").setPayload(data)
                    // don't add the header on HTML because it causes CORS freakoutery
                    if (game.plat.type() !== Platform.Type.HTML) {
                        b.addHeader("playn-test", "we love to test!")
                    }
                    b.execute().onFailure(displayError).onSuccess { rsp: Net.Response ->
                        val lines = rsp.payloadString().split("[\r\n]+")
                        val urlPre = "View it at "
                        for (line in lines) {
                            System.err.println(line + " " + line.startsWith(urlPre) + " " + urlPre)
                            if (line.startsWith(urlPre)) {
                                lastPostURL = line.substring(urlPre.length)
                                break
                            }
                        }
                        displayResult(rsp)
                    }
                }
            })
        }, x, 10f)

        x = addButton("Fetch Posted Body", {
            if (lastPostURL == null)
                displayText("Click 'Post Test' to post some data first.")
            else
                game.net.req(lastPostURL!!).execute().onFailure(displayError).onSuccess(displayResult)
        }, x, 10f)

        x = addButton("WS Connect", {
            if (_websock != null) displayText("Already connected.")
            _websock = game.net.createWebSocket("ws://echo.websocket.org", object : Net.WebSocket.Listener {
                override fun onOpen() {
                    displayText("WebSocket connected.")
                }

                override fun onTextMessage(msg: String) {
                    displayText("Got WebSocket message: " + msg)
                }

                override fun onDataMessage(msg: ByteBuffer) {
                    displayText("Got WebSocket data message: " + msg.limit())
                }

                override fun onClose() {
                    displayText("WebSocket closed.")
                    _websock = null
                }

                override fun onError(reason: String) {
                    displayText("Got WebSocket error: " + reason)
                    _websock = null
                }
            })
            displayText("WebSocket connection started.")
        }, x, 10f)

        x = addButton("WS Send", {
            if (_websock == null)
                displayText("WebSocket not open.")
            else
                getText("Enter message:").onSuccess(object : TextCB() {
                    override fun gotText(msg: String) {
                        if (_websock == null)
                            displayText("WebSocket disappeared.")
                        else {
                            _websock!!.send(msg)
                            displayText("WebSocket sent: " + msg)
                        }
                    }
                })
        }, x, 10f)

        x = addButton("WS Close", {
            if (_websock == null)
                displayText("WebSocket not open.")
            else
                _websock!!.close()
        }, x, 10f)
    }

    private fun getText(label: String): RFuture<String> {
        return game.input.getText(Keyboard.TextType.DEFAULT, label, "")
    }

    private fun loadURL(url: String) {
        displayText("Loading: " + url)
        try {
            game.net.req(url).execute().onSuccess(displayResult).onFailure(displayError)
        } catch (e: Exception) {
            displayText(e.toString())
        }

    }

    private fun displayText(text: String) {
        output!!.setTile(game.ui.wrapText(text, game.graphics.viewSize.width - 20, TextBlock.Align.LEFT))
    }

    private val displayResult: Slot<Net.Response> = { rsp: Net.Response ->
        val buf = StringBuilder()
        buf.append("Response code: ").append(rsp.responseCode())
        buf.append("\n\nHeaders:\n")

        for (header in rsp.headerNames()) {
            buf.append(header).append(":")
            var vv = 0
            for (value in rsp.headers(header)) {
                if (vv++ > 0) buf.append(",")
                buf.append(" ").append(value)
            }
            buf.append("\n")
        }
        buf.append("\nBody:\n")
        var payload = rsp.payloadString()
        if (payload.length > 1024) payload = payload.substring(0, 1024) + "..."
        buf.append(payload)
        displayText(buf.toString())
    }

    private val displayError: Slot<Throwable> = { error: Throwable -> displayText(error.toString()) }

    private abstract inner class TextCB : Slot<String?> {
        override fun invoke(text: String?) {
            if (text != null && text.isNotEmpty()) gotText(text)
        }

        protected abstract fun gotText(text: String)
    }
}
