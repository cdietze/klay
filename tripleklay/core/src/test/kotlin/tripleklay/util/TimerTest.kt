package tripleklay.util

import klay.core.Platform
import klay.core.StubPlatform
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TimerTest {

    @Test fun testOneShot() {
        val timer = Timer(0)
        val r1 = Counter()
        timer.after(10, r1)
        // make sure the timer hasn't run yet
        timer.update(plat, 3)
        assertEquals(0, r1.ranCount.toLong())
        // elapse time past our expiry and make sure it has run
        timer.update(plat, 15)
        assertEquals(1, r1.ranCount.toLong())
    }

    @Test fun testCancelBefore() {
        val timer = Timer(0)
        val r1 = Counter()
        val h1 = timer.after(10, r1)
        // make sure the timer hasn't run yet
        timer.update(plat, 3)
        assertEquals(0, r1.ranCount.toLong())
        h1.cancel()
        // elapse time past our expiry and make sure our canceled timer did not run
        timer.update(plat, 15)
        assertEquals(0, r1.ranCount.toLong())
    }

    @Test fun testRepeat() {
        var time: Long = 25
        val timer = Timer(time)
        val r1 = Counter()
        val h1 = timer.every(10, r1)
        // make sure the timer hasn't run yet
        time += 3
        timer.update(plat, time)
        assertEquals(0, r1.ranCount.toLong())
        // elapse time past our expiry and make sure our action ran
        time += 10
        timer.update(plat, time)
        assertEquals(1, r1.ranCount.toLong())
        // elapse time past our expiry again and make sure our action ran again
        time += 10
        timer.update(plat, time)
        assertEquals(2, r1.ranCount.toLong())
        // cancel our timer and make sure it ceases to run
        h1.cancel()
        time += 10
        timer.update(plat, time)
        assertEquals(2, r1.ranCount.toLong())
    }

    @Test fun testMultiple() {
        var time: Long = 0
        val timer = Timer(time)
        val r1 = Counter()
        val r2 = Counter()
        val r3 = Counter()
        // set up timers to expire after 10, 10, and 25, the latter two repeating every 10 and 15
        timer.after(10, r1)
        timer.every(10, r2)
        timer.atThenEvery(25, 15, r3)

        // T=T+3: no timers have run
        time += 3
        timer.update(plat, time)
        assertEquals(0, r1.ranCount.toLong())
        assertEquals(0, r2.ranCount.toLong())
        assertEquals(0, r3.ranCount.toLong())

        // T=T+13; h1 and h2 expire, h3 doesn't
        time += 10
        timer.update(plat, time)
        assertEquals(1, r1.ranCount.toLong())
        assertEquals(1, r2.ranCount.toLong())
        assertEquals(0, r3.ranCount.toLong())

        // T=T+23; h1 is gone, h2 expires again, h3 not yet
        time += 10
        timer.update(plat, time)
        assertEquals(1, r1.ranCount.toLong())
        assertEquals(2, r2.ranCount.toLong())
        assertEquals(0, r3.ranCount.toLong())

        // T=T+33; h1 is gone, h2 expires again, h3 expires once
        time += 10
        timer.update(plat, time)
        assertEquals(1, r1.ranCount.toLong())
        assertEquals(3, r2.ranCount.toLong())
        assertEquals(1, r3.ranCount.toLong())

        // T=T+43; h2 expires again, h3 expires again
        time += 10
        timer.update(plat, time)
        assertEquals(1, r1.ranCount.toLong())
        assertEquals(4, r2.ranCount.toLong())
        assertEquals(2, r3.ranCount.toLong())
    }

    @Test fun testOrder() {
        var time: Long = 0
        val timer = Timer(time)
        val r1 = IntArray(1)
        val r2 = IntArray(1)
        val r3 = IntArray(1)

        // make sure that three timers set to expire at the same time go off in the order they were
        // registered
        timer.every(10, Runnable {
            r1[0] += 1
            assertTrue(r1[0] == r2[0] + 1)
            assertTrue(r1[0] == r3[0] + 1)
        })
        timer.every(10, Runnable {
            assertTrue(r1[0] == r2[0] + 1)
            r2[0] += 1
            assertTrue(r2[0] == r3[0] + 1)
        })
        timer.every(10, Runnable {
            assertTrue(r1[0] == r3[0] + 1)
            assertTrue(r2[0] == r3[0] + 1)
            r3[0] += 1
        })

        // T=T+3: no timers have run
        time += 3
        timer.update(plat, time)
        assertEquals(0, r1[0].toLong())
        assertEquals(0, r2[0].toLong())
        assertEquals(0, r3[0].toLong())

        // T=T+13: all timers have run once
        time += 10
        timer.update(plat, time)
        assertEquals(1, r1[0].toLong())
        assertEquals(1, r2[0].toLong())
        assertEquals(1, r3[0].toLong())

        // T=T+23: all timers have run twice
        time += 10
        timer.update(plat, time)
        assertEquals(2, r1[0].toLong())
        assertEquals(2, r2[0].toLong())
        assertEquals(2, r3[0].toLong())
    }

    @Test fun testConcurrentReschedule() {
        // check to make sure we can reschedule concurrently
        Rescheduler(true).test()
        Rescheduler(false).test()
    }

    @Test fun testRescheduler() {
        val timer = Timer(0)

        // a sub task that may be rescheduled
        val sub = Counter()

        // a timer task that schedules the sub
        val main = object : Counter() {
            override fun run() {
                super.run()
                sub.cancel()
                sub.handle = timer.after(2, sub)
            }
        }

        // queue up the subtask for tick 2
        timer.after(0, main)
        timer.update(plat, 0)
        assertEquals(1, main.ranCount.toLong())
        assertEquals(0, sub.ranCount.toLong())

        // dequeue and queue the subtask for tick 3
        timer.after(0, main)
        timer.update(plat, 1)
        assertEquals(2, main.ranCount.toLong())
        assertEquals(0, sub.ranCount.toLong())

        // process to tick 3, it should run the subtask once
        timer.update(plat, 2)
        timer.update(plat, 3)
        assertEquals(2, main.ranCount.toLong())
        assertEquals(1, sub.ranCount.toLong())
    }

    @Test fun testDoubleCancel() {
        val ran1 = Counter()
        val ran2 = Counter()
        val t = Timer(0)
        val h = t.after(1, ran1)
        t.after(2, ran2)
        t.update(plat, 1)
        h.cancel()
        h.cancel()
        t.update(plat, 2)
        assertEquals(1, ran1.ranCount.toLong())
        assertEquals(1, ran2.ranCount.toLong())
    }

    @Test fun testInternalCancel() {
        val ran1 = InternalCanceler()
        val t = Timer(0)
        ran1.handle = t.every(2, ran1)
        for (ii in 0..6) t.update(plat, (ii + 1).toLong())
        assertEquals(1, ran1.ranCount.toLong())
    }

    protected open class Counter : Runnable {
        var ranCount: Int = 0
        var handle: Timer.Handle? = null

        override fun run() {
            ++ranCount
        }

        fun cancel() {
            if (handle != null) {
                handle!!.cancel()
                handle = null
            }
        }
    }

    protected class Rescheduler(var cancelBefore: Boolean) : Runnable {
        var timer = Timer(0)
        var handle: Timer.Handle
        var ran: Int = 0

        init {
            handle = timer.after(1, this)
        }

        override fun run() {
            ran++
            val h = handle
            if (cancelBefore) {
                h.cancel()
            }
            handle = timer.after(1, this)
            if (!cancelBefore) {
                h.cancel()
            }
        }

        fun test() {
            timer.update(plat, 1)
            timer.update(plat, 2)
            assertEquals(2, ran.toLong())
        }
    }

    protected class InternalCanceler : Counter() {
        override fun run() {
            super.run()
            cancel()
        }
    }

    companion object {

        var plat: Platform = StubPlatform()
    }
}
