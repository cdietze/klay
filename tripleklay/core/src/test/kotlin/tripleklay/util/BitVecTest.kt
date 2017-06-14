package tripleklay.util

import org.junit.Test

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

class BitVecTest {
    @Test fun test() {
        val vec = BitVec(4)
        for (ii in 0..9999) assertFalse(vec.isSet(ii))
        for (ii in 0..9999) vec.set(ii)
        for (ii in 0..9999) assertTrue(vec.isSet(ii))
        for (ii in 0..9999) vec.clear(ii)
        for (ii in 0..9999) assertFalse(vec.isSet(ii))
    }
}
