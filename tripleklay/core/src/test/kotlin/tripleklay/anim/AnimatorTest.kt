package tripleklay.anim

import org.junit.Assert.fail
import org.junit.Test

class AnimatorTest {
    @Test fun testAnimDoubleRegisterFreakout() {
        val anim = Animator()
        val NOOP = { }
        val chain = anim.action(NOOP).then()
        // it's OK to keep chaining animations
        chain.action(NOOP).then().action(NOOP)
        // it's not OK to try to chain an animation off the then() builder to which we kept a
        // reference and off of which we have already chained an animation
        try {
            chain.action(NOOP)
            fail("Double register failed to freakout")
        } catch (ise: IllegalStateException) {
        }
        // success
    }
}
