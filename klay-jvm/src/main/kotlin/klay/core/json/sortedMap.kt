package klay.core.json

import java.util.*

actual fun <K, V> sortedMutableMap(): MutableMap<K, V> = TreeMap()
