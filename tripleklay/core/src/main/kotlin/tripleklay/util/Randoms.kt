package tripleklay.util

import tripleklay.entity.IntBag
import java.util.*

/**
 * Provides utility routines to simplify obtaining randomized values.
 */

/**
 * Returns a pseudorandom, uniformly distributed `int` value between `0`
 * (inclusive) and `high` (exclusive).
 *
 * @param high the high value limiting the random number sought.
 * @throws IllegalArgumentException if `high` is not positive.
 */
fun Random.getInt(high: Int): Int {
    return this.nextInt(high)
}

/**
 * Returns a pseudorandom, uniformly distributed `int` value between `low`
 * (inclusive) and `high` (exclusive).
 *
 * @throws IllegalArgumentException if `high - low` is not positive.
 */
fun Random.getInRange(low: Int, high: Int): Int {
    return low + this.nextInt(high - low)
}

/**
 * Returns a pseudorandom, uniformly distributed `float` value between `0.0`
 * (inclusive) and the `high` (exclusive).
 *
 * @param high the high value limiting the random number sought.
 */
fun Random.getFloat(high: Float): Float {
    return this.nextFloat() * high
}

/**
 * Returns a pseudorandom, uniformly distributed `float` value between `low`
 * (inclusive) and `high` (exclusive).
 */
fun Random.getInRange(low: Float, high: Float): Float {
    return low + this.nextFloat() * (high - low)
}

/**
 * Returns true approximately one in `n` times.
 *
 * @throws IllegalArgumentException if `n` is not positive.
 */
fun Random.getChance(n: Int): Boolean {
    return 0 == this.nextInt(n)
}

/**
 * Has a probability `p` of returning true.
 */
fun Random.getProbability(p: Float): Boolean {
    return this.nextFloat() < p
}

/**
 * Returns `true` or `false` with approximately even probability.
 */
fun Random.getBoolean(): Boolean = this.nextBoolean()

/**
 * Returns a pseudorandom, normally distributed `float` value around the `mean`
 * with the standard deviation `dev`.
 */
fun Random.getNormal(mean: Float, dev: Float): Float {
    return this.nextGaussian().toFloat() * dev + mean
}

/**
 * Shuffle the specified list using our Random.
 */
inline fun <reified T> Random.shuffle(list: MutableList<T>) {
    // we can't use Collections.shuffle here because GWT doesn't implement it
    val size = list.size
    if (list is RandomAccess) {
        for (ii in size downTo 2) {
            _swap(list, ii - 1, this.nextInt(ii))
        }
    } else {
        val array = list.toTypedArray()
        for (ii in size downTo 2) {
            _swap(array, ii - 1, this.nextInt(ii))
        }
        val it = list.listIterator()
        for (ii in 0 until size) {
            it.next()
            it.set(array[ii])
        }
    }
}

/**
 * Pick a random element from the specified IntBag, or return `null` if it is empty.
 */
fun Random.pick(intBag: IntBag): Int? {
    if (intBag.size() == 0) return null
    return intBag[this.getInt(intBag.size())]
}

/**
 * Pick a random element from the specified Iterator, or return `null` if it is empty.
 *
 * **Implementation note:** because the total size of the Iterator is not known,
 * the random number generator is queried after the second element and every element
 * thereafter.
 *
 * @throws NullPointerException if the iterator is null.
 */
fun <T> Random.pick(iterator: kotlin.collections.Iterator<T>): T? {
    if (!iterator.hasNext()) {
        return null
    }
    var pick: T = iterator.next()
    var count = 2
    while (iterator.hasNext()) {
        val next = iterator.next()
        if (0 == this.nextInt(count)) {
            pick = next
        }
        count++
    }
    return pick
}

/**
 * Pick a random element from the specified Iterable, or return `null` if it is empty.
 *
 * **Implementation note:** optimized implementations are used if the Iterable
 * is a List or Collection. Otherwise, it behaves as if calling [pick]
 * with the Iterable's Iterator.
 *
 * @throws NullPointerException if the iterable is null.
 */
fun <T> Random.pick(iterable: Iterable<T>): T? {
    return this.pickPluck(iterable, false)
}


/**
 * Pick a random *key* from the specified mapping of weight values, or return `null` if no mapping has a weight greater than `0`. Each weight value is evaluated
 * as a double.
 *
 * **Implementation note:** a random number is generated for every entry with a
 * non-zero weight after the first such entry.
 *
 * @throws NullPointerException if the map is null.
 * @throws IllegalArgumentException if any weight is less than 0.
 */
fun <T> Random.pick(weightMap: Map<out T, Number>): T? {
    var pick: T? = null
    var total = 0.0
    for (entry in weightMap.entries) {
        val weight = entry.value.toDouble()
        if (weight > 0.0) {
            total += weight
            if (total == weight || this.nextDouble() * total < weight) {
                pick = entry.key
            }
        } else if (weight < 0.0) {
            throw IllegalArgumentException("Weight less than 0: " + entry)
        } // else: weight == 0.0 is OK
    }
    return pick
}

/**
 * Pluck (remove) a random element from the specified Iterable, or return `null` if it
 * is empty.
 *
 * **Implementation note:** optimized implementations are used if the Iterable
 * is a List or Collection. Otherwise, two Iterators are created from the Iterable
 * and a random number is generated after the second element and all beyond.
 *
 * @throws NullPointerException if the iterable is null.
 */
fun <T> Random.pluck(iterable: MutableIterable<T>): T? {
    return pickPluck(iterable, true)
}

/**
 * Shared code for pick and pluck.
 */
private fun <T> Random.pickPluck(iterable: Iterable<T>, remove: Boolean): T? {
    if (iterable is Collection<*>) {
        // optimized path for Collection
        val coll = iterable
        val size = coll.size
        if (coll is MutableList<*>) {
            // extra-special optimized path for Lists
            val list = coll as MutableList<T>
            val idx = this.nextInt(size)
            return if (remove) list.removeAt(idx) else list[idx]
        }
        // for other Collections, we must iterate
        val it = coll.iterator()
        for (idx in this.nextInt(size) downTo 1) {
            it.next()
        }
        try {
            return it.next()
        } finally {
            if (remove) {
                (it as MutableIterator<T>).remove()
            }
        }
    }

    if (!remove) {
        return pick(iterable.iterator())
    }

    // from here on out, we're doing a pluck with a complicated two-iterator solution
    val it = iterable.iterator()
    if (!it.hasNext()) {
        return null
    }
    val lagIt = iterable.iterator()
    var pick: T = it.next()
    lagIt.next()
    var count = 2
    var lag = 1
    while (it.hasNext()) {
        val next = it.next()
        if (0 == this.nextInt(count)) {
            pick = next
            // catch up lagIt so that it has just returned 'pick' as well
            while (lag > 0) {
                lagIt.next()
                lag--
            }
        }
        count++
        lag++
    }
    (lagIt as MutableIterator<T>).remove() // remove 'pick' from the lagging iterator
    return pick
}

/** Helper for [shuffle].  */
inline fun <reified T> _swap(list: MutableList<T>, ii: Int, jj: Int) {
    list[ii] = list.set(jj, list[ii])
}

/** Helper for [shuffle].  */
inline fun <reified T> _swap(array: Array<T>, ii: Int, jj: Int) {
    val tmp = array[ii]
    array[ii] = array[jj]
    array[jj] = tmp
}


