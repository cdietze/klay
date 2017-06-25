package tripleklay.util

import klay.core.Platform
import react.Slot

/**
 * Handles execution of actions after a specified delay. Create a `Timer` and connect it to
 * the [Platform.frame] signal:

 * <pre>`public class MyGame extends SceneGame {
 * public final Timer timer = new Timer();
 * public MyGame (Platform plat) {
 * plat.frame.connect(timer);
 * }
 * }
`</pre> *

 * Then you can register actions to be performed at times in the future like so:

 * <pre>`// wherever
 * game.timer.after(500, () -> {
 * // this is run after 500ms
 * });
`</pre> *
 */
class Timer
// This is internal so we can unit test this class
internal constructor(
        var _currentTime: Long) : Slot<Platform> {
    /** A handle on registered actions that can be used to cancel them.  */
    interface Handle {
        /** Cancels the action in question.  */
        fun cancel()
    }

    /** Creates a timer instance that can be used to schedule actions. Connect this timer to the
     * frame signal to make it operable. */
    constructor() : this(System.currentTimeMillis())

    /** Executes the supplied action after the specified number of milliseconds have elapsed.
     * @return a handle that can be used to cancel the execution of the action.
     */
    fun after(millis: Int, action: Runnable): Handle {
        return add(millis, 0, action)
    }

    /** Executes the supplied action starting `millis` from now and every `millis`
     * thereafter.
     * @return a handle that can be used to cancel the execution of the action.
     */
    fun every(millis: Int, action: Runnable): Handle {
        return atThenEvery(millis, millis, action)
    }

    /** Executes the supplied action starting `initialMillis` from now and every `repeatMillis` there after.
     * @return a handle that can be used to cancel the execution of the action.
     */
    fun atThenEvery(initialMillis: Int, repeatMillis: Int, action: Runnable): Handle {
        return add(initialMillis, repeatMillis, action)
    }

    override fun invoke(plat: Platform) {
        update(plat, System.currentTimeMillis())
    }

    // This is internal so we can unit test this class
    internal fun update(plat: Platform, now: Long) {
        _currentTime = now

        val root = _root
        while (root.next != null && root.next!!.nextExpire <= now) {
            val act = root.next
            if (!act!!.cancelled()) {
                execute(plat, act.action!!)
                if (act.repeatMillis == 0) {
                    act.cancel()
                } else if (!act.cancelled()) {
                    act.nextExpire += act.repeatMillis.toLong()
                    root.next = insert(act, act.next)
                }
            }
        }
    }

    private fun execute(plat: Platform, runnable: Runnable) {
        try {
            runnable.run()
        } catch (e: Exception) {
            plat.log.warn("Action failed", e)
        }

    }

    private fun add(initialMillis: Int, repeatMillis: Int, action: Runnable): Handle {
        val act = Action(initialMillis, repeatMillis, action)
        _root.next = insert(act, _root.next)
        return act
    }

    private fun insert(target: Action, tail: Action?): Action {
        if (tail == null || tail.nextExpire > target.nextExpire) {
            target.next = tail
            return target
        } else {
            tail.next = insert(target, tail.next)
            return tail
        }
    }

    private fun remove(target: Action, tail: Action?): Action? {
        if (target === tail)
            return tail.next
        else if (tail == null)
            return null
        else {
            tail.next = remove(target, tail.next)
            return tail
        }
    }

    private inner class Action(initialMillis: Int, val repeatMillis: Int, val action: Runnable?) : Handle {

        var nextExpire: Long = 0
        var next: Action? = null

        init {
            this.nextExpire = _currentTime + initialMillis
        }

        fun cancelled(): Boolean {
            return nextExpire == -1L
        }

        override fun cancel() {
            if (!cancelled()) {
                _root.next = remove(this, _root.next)
                nextExpire = -1
                next = null
            }
        }

        override fun toString(): String {
            return nextExpire.toString() + "/" + repeatMillis + "/" + action + " -> " + next
        }
    }

    private val _root = Action(0, 0, null)
}
