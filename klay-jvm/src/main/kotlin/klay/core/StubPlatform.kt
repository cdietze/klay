package klay.core

actual fun StubPlatform.currentTimeMillis(): Long = System.currentTimeMillis()

actual fun StubPlatform.printStacktrace(t: Throwable) {
    t.printStackTrace()
}
