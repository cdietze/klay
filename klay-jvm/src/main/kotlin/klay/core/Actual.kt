package klay.core

import java.io.PrintWriter
import java.io.StringWriter

actual fun Throwable.printStackTrace() {
    this.printStackTrace()
}

actual fun Throwable.printStackTrace(appendable: Appendable) {
    val buf = StringWriter()
    this.printStackTrace(PrintWriter(buf))
    appendable.append(buf.toString())
}

actual fun Int.toHexString(): String = Integer.toHexString(this)

actual fun Int.toBinaryString(): String = Integer.toBinaryString(this)
