package klay.core

actual fun StubPlatform.currentTimeMillis(): Long = System.currentTimeMillis()
