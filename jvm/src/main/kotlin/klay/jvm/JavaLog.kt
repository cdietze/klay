package klay.jvm

import klay.core.Log

class JavaLog : Log() {

    override fun logImpl(level: Log.Level, msg: String, e: Throwable?) {
        when (level) {
            Log.Level.WARN, Log.Level.ERROR -> {
                System.err.println(msg)
                e?.printStackTrace(System.err)
            }
            else -> {
                println(msg)
                e?.printStackTrace(System.out)
            }
        }
    }
}
