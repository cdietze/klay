package klay.core

import klay.core.buffers.ByteBuffer
import react.RFuture

/**
 * Klay network interface.
 */
abstract class Net {

    /** Encapsulates a web socket.  */
    interface WebSocket {
        /** Notifies game of web socket events.  */
        interface Listener {
            /** Reports that a requested web socket is now open and ready for use.  */
            fun onOpen()

            /** Reports that a text message has arrived on a web socket.  */
            fun onTextMessage(msg: String)

            /** Reports that a binary message has arrived on a web socket.  */
            fun onDataMessage(msg: ByteBuffer)

            /** Reports that a web socket has been closed.  */
            fun onClose()

            /** Reports that a web socket has encountered an error.
             * TODO: is it closed as a result of this?  */
            fun onError(reason: String)
        }

        /** Requests that this web socket be closed. This will result in a call to [ ][Listener.onClose] when the socket closure is completed.  */
        fun close()

        /** Queues the supplied text message to be sent over the socket.  */
        fun send(data: String)

        /** Queues the supplied binary message to be sent over the socket.  */
        fun send(data: ByteBuffer)
    }

    /** Used to report HTTP error responses by [.get] and [.post].  */
    class HttpException(
            /** The HTTP error code reported by the server.  */
            val errorCode: Int, message: String) : Exception(message) {

        override fun toString(): String {
            val msg = message
            return "HTTP " + errorCode + if (msg == null) "" else ": " + msg
        }
    }

    /** Contains data for an HTTP header. Used by [Builder].  */
    class Header(val name: String, val value: String)

    /** Builds a request and allows it to be configured and executed.  */
    inner class Builder(val url: String) {
        val headers: MutableList<Header> = ArrayList()
        var _contentType = "text/plain"
        var payloadString: String? = null
        var payloadBytes: ByteArray? = null

        /** Configures the payload of this request as a UTF-8 string with content type configured as
         * "`_contentType`; charset=UTF-8". The supplied content type should probably be something
         * like `text/plain` or `text/xml` or `application/json`. This converts the
         * request to a POST.  */
        fun setPayload(payload: String, contentType: String = "text/plain"): Builder {
            this.payloadString = payload
            this._contentType = contentType
            return this
        }

        /** Configures the payload of this request as raw bytes with the specified content type. This
         * converts the request to a POST.  */
        fun setPayload(payload: ByteArray, contentType: String = "application/octet-stream"): Builder {
            this.payloadBytes = payload
            this._contentType = contentType
            return this
        }

        /** Adds the supplied request header.
         * @param name the name of the header (e.g. `Authorization`).
         * *
         * @param value the value of the header.
         */
        fun addHeader(name: String, value: String): Builder {
            headers.add(Header(name, value))
            return this
        }

        /** Executes this request, delivering the response via `callback`.  */
        fun execute(): RFuture<Response> {
            return this@Net.execute(this)
        }

        val isPost: Boolean
            get() = payloadString != null || payloadBytes != null

        fun method(): String {
            return if (isPost) "POST" else "GET"
        }

        fun contentType(): String {
            return _contentType + if (payloadString != null) "; charset=" + UTF8 else ""
        }

        init {
            assert(url.startsWith("http:") || url.startsWith("https:")) { "Only http and https URLs are supported" }
        }
    }
    /** Configures the payload of this request as a UTF-8 string with content type "text/plain".
     * This converts the request to a POST.  */
    /** Configures the payload of this request as raw bytes with content type
     * "application/octet-stream". This converts the request to a POST.  */

    /** Communicates an HTTP response to the caller.  */
    abstract class Response protected constructor(private val responseCode: Int) {
        private var headersMap: Map<String, List<String>>? = null

        /** Returns the HTTP response code provided by the server.  */
        fun responseCode(): Int {
            return this.responseCode
        }

        /** Returns the names of all headers returned by the server.  */
        fun headerNames(): Iterable<String> {
            return headers().keys
        }

        /** Returns the value of the header with the specified name, or null. If there are multiple
         * response headers with this name, one will be chosen using an undefined algorithm.  */
        fun header(name: String): String? {
            val values = headers()[name]
            return if (values == null) null else values[0]
        }

        /** Returns the value of all headers with the specified name, or the empty list.
         *
         * *NOTE:* on the iOS backend, repeated headers will be coalesced into a single
         * header separated by commas. This sucks but we can't "undo" the coalescing without breaking
         * otherwise normal headers that happent to contain commas. Complain to Apple.  */
        fun headers(name: String): List<String> {
            val values = headers()[name]
            return values ?: emptyList<String>()
        }

        /** Returns the response payload as a string, decoded using the character set specified in the
         * response's content type.  */
        abstract fun payloadString(): String

        /** Returns the response payload as raw bytes. Note: this is not available on the HTML
         * backend.  */
        open fun payload(): ByteArray {
            throw UnsupportedOperationException()
        }

        protected abstract fun extractHeaders(): Map<String, List<String>>

        private fun headers(): Map<String, List<String>> {
            if (headersMap == null) {
                headersMap = extractHeaders()
            }
            return headersMap!!
        }
    }

    /** Used to deliver binary response data.  */
    abstract inner class BinaryResponse(responseCode: Int, private val payload: ByteArray, private val encoding: String) : Response(responseCode) {

        override fun payloadString(): String {
            return encodeString(payload, encoding)
        }

        override fun payload(): ByteArray {
            return payload
        }
    }

    /**
     * Converts the given [bytes] into a [String] using the specified [encoding].
     */
    protected abstract fun encodeString(bytes: ByteArray, encoding: String): String

    /**
     * Create a websocket with given URL and listener.
     */
    open fun createWebSocket(url: String, listener: WebSocket.Listener): WebSocket {
        throw UnsupportedOperationException()
    }

    /**
     * Performs an HTTP GET request to the specified URL.
     */
    operator fun get(url: String): RFuture<String> {
        return req(url).execute().map(GET_PAYLOAD)
    }

    /**
     * Performs an HTTP POST request to the specified URL.
     */
    fun post(url: String, data: String): RFuture<String> {
        return req(url).setPayload(data).execute().map(GET_PAYLOAD)
    }

    /**
     * Creates a builder for a request with the specified URL.
     */
    fun req(url: String): Builder {
        return Builder(url)
    }

    protected open fun execute(req: Builder): RFuture<Response> {
        return RFuture.failure(UnsupportedOperationException())
    }

    companion object {

        val UTF8 = "UTF-8"

        private val GET_PAYLOAD: (Response) -> String = { rsp -> rsp.payloadString() }
    }
}
