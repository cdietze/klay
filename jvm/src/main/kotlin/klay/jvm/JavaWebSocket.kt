package klay.jvm

import klay.core.Exec
import klay.core.Net
import org.java_websocket.client.WebSocketClient
import org.java_websocket.drafts.Draft_17
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.net.URISyntaxException
import java.nio.ByteBuffer

class JavaWebSocket(exec: Exec, uri: String, listener: Net.WebSocket.Listener) : Net.WebSocket {

    private val socket: WebSocketClient

    init {
        val juri: URI?
        try {
            juri = URI(uri)
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }

        socket = object : WebSocketClient(juri, Draft_17()) {
            override fun onMessage(buffer: ByteBuffer) {
                exec.invokeLater(Runnable { listener.onDataMessage(buffer) })
            }

            override fun onMessage(msg: String) {
                exec.invokeLater(Runnable { listener.onTextMessage(msg) })
            }

            override fun onError(e: Exception) {
                exec.invokeLater(Runnable { listener.onError(e.message!!) })
            }

            override fun onClose(code: Int, reason: String, remote: Boolean) {
                exec.invokeLater(Runnable { listener.onClose() })
            }

            override fun onOpen(handshake: ServerHandshake) {
                exec.invokeLater(Runnable { listener.onOpen() })
            }
        }
        socket.connect()
    }

    override fun close() {
        socket.close()
    }

    override fun send(data: String) {
        try {
            socket.getConnection().send(data)
        } catch (e: Throwable) {
            throw RuntimeException(e)
        }

    }

    override fun send(data: ByteBuffer) {
        try {
            socket.getConnection().send(data)
        } catch (e: Throwable) {
            throw RuntimeException(e)
        }

    }
}
