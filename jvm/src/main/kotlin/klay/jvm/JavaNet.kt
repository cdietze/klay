package klay.jvm

import klay.core.Exec
import klay.core.Net
import react.RFuture
import react.RPromise
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

open class JavaNet(private val exec: Exec) : Net() {

    override fun createWebSocket(url: String, listener: WebSocket.Listener): WebSocket {
        return JavaWebSocket(exec, url, listener)
    }

    override fun execute(req: Builder): RFuture<Response> {
        val result: RPromise<Response> = exec.deferredPromise()
        exec.invokeAsync(object : Runnable {
            override fun run() {
                try {
                    // configure the request
                    val url = URL(canonicalizeUrl(req.url))
                    val conn = url.openConnection() as HttpURLConnection
                    for (header in req.headers) {
                        conn.setRequestProperty(header.name, header.value)
                    }
                    conn.requestMethod = req.method()
                    if (req.isPost) {
                        conn.doOutput = true
                        conn.doInput = true
                        conn.allowUserInteraction = false
                        conn.setRequestProperty("Content-type", req.contentType())
                        conn.connect()
                        conn.outputStream.write(
                                if (req.payloadString == null) req.payloadBytes else req.payloadString!!.toByteArray())
                        conn.outputStream.close()
                    }

                    // issue the request and process the response
                    try {
                        val code = conn.responseCode

                        val stream = if (code >= 400) conn.errorStream else conn.inputStream
                        val payload = if (stream == null) ByteArray(0) else JavaAssets.toByteArray(stream)

                        var encoding: String? = conn.contentEncoding
                        if (encoding == null) encoding = UTF8

                        result.succeed(object : Response.Binary(code, payload, encoding!!) {
                            override fun extractHeaders(): Map<String, List<String>> {
                                // conn.headerFields actually contains one entry with a null key
                                // and the repsonse status as value, so filter that one out
                                return conn.headerFields.filterKeys { it != null }
                            }
                        })
                    } finally {
                        conn.disconnect()
                    }

                } catch (e: MalformedURLException) {
                    result.fail(e)
                } catch (e: IOException) {
                    result.fail(e)
                }

            }

            override fun toString(): String {
                return "JavaNet." + req.method().toLowerCase() + "(" + req.url + ")"
            }
        })
        return result
    }

    // Super-simple url-cleanup: assumes it either starts with "http", or that
    // it's an absolute path on the current server.
    private fun canonicalizeUrl(url: String): String {
        if (!url.startsWith("http")) {
            return "http://" + server() + url
        }
        return url
    }

    // TODO: Make this specifiable somewhere.
    private fun server(): String {
        return "127.0.0.1:8080"
    }
}
