package tripleklay.ui

import klay.core.Clock
import klay.core.Platform
import klay.core.StubPlatform
import org.junit.Test
import react.Signal
import tripleklay.ui.layout.AxisLayout
import kotlin.test.assertTrue

// import klay.java.JavaPlatform;

class ElementTest {
    val frame = Signal<Clock>()

    internal open class TestGroup : Group(AxisLayout.vertical()) {
        var added: Int = 0
        var removed: Int = 0

        fun assertAdded(count: Int) {
            assertTrue(added == count && removed == count - 1)
        }

        fun assertRemoved(count: Int) {
            assertTrue(removed == count && added == count)
        }

        override fun wasAdded() {
            super.wasAdded()
            added++
        }

        override fun wasRemoved() {
            super.wasRemoved()
            removed++
        }
    }

    internal var iface = Interface(stub, frame)

    internal fun newRoot(): Root {
        return iface.createRoot(AxisLayout.vertical(), Stylesheet.builder().create())
    }

    /** Tests the basic functionality of adding and removing elements and that the wasAdded
     * and wasRemoved members are called as expected.  */
    @Test fun testAddRemove() {
        val root = newRoot()

        val g1 = TestGroup()
        val g2 = TestGroup()
        g1.assertRemoved(0)

        root.add(g1)
        g1.assertAdded(1)

        g1.add(g2)
        g1.assertAdded(1)
        g2.assertAdded(1)

        g1.remove(g2)
        g1.assertAdded(1)
        g2.assertRemoved(1)

        root.remove(g1)
        g1.assertRemoved(1)
        g2.assertRemoved(1)

        g1.add(g2)
        g1.assertRemoved(1)
        g2.assertRemoved(1)

        root.add(g1)
        g1.assertAdded(2)
        g2.assertAdded(2)
    }

    /** Tests that a group may add a child into another group whilst being removed and that the
     * child receives the appropriate calls to wasAdded and wasRemoved. Similarly tests for
     * adding the child during its own add.  */
    @Test fun testChildTransfer() {

        class Pa : TestGroup() {
            var child1 = TestGroup()
            var child2 = TestGroup()
            var brother = TestGroup()

            override fun wasRemoved() {
                // hand off the children to brother
                brother.add(child1)
                super.wasRemoved()
                brother.add(child2)
            }

            override fun wasAdded() {
                // steal the children back from brother
                add(child1)
                super.wasAdded()
                add(child2)
            }
        }

        val root = newRoot()
        val pa = Pa()
        pa.assertRemoved(0)

        root.add(pa)
        pa.assertAdded(1)
        pa.child1.assertAdded(1)
        pa.child2.assertAdded(1)

        root.remove(pa)
        pa.assertRemoved(1)
        pa.child1.assertRemoved(1)
        pa.child2.assertRemoved(1)

        root.add(pa.brother)
        pa.child1.assertAdded(2)
        pa.child2.assertAdded(2)

        root.add(pa)
        pa.assertAdded(2)
        pa.child1.assertAdded(3)
        pa.child2.assertAdded(3)

        root.remove(pa)
        pa.assertRemoved(2)
        pa.child1.assertAdded(4)
        pa.child2.assertAdded(4)
    }

    /** Tests that a group may add a grandchild into another group whilst being removed and that
     * the grandchild receives the appropriate calls to wasAdded and wasRemoved. Similarly tests
     * for adding the grandchild during its own add.  */
    @Test fun testGrandchildTransfer() {

        class GrandPa : TestGroup() {
            var child = TestGroup()
            var grandchild1 = TestGroup()
            var grandchild2 = TestGroup()
            var brother = TestGroup()

            init {
                add(child)
            }

            override fun wasRemoved() {
                brother.add(grandchild1)
                super.wasRemoved()
                brother.add(grandchild2)
            }

            override fun wasAdded() {
                child.add(grandchild1)
                super.wasAdded()
                child.add(grandchild2)
            }
        }

        val root = newRoot()
        val pa = GrandPa()
        pa.assertRemoved(0)

        root.add(pa)
        pa.assertAdded(1)
        pa.grandchild1.assertAdded(1)
        pa.grandchild2.assertAdded(1)

        root.remove(pa)
        pa.assertRemoved(1)
        pa.grandchild1.assertRemoved(1)
        pa.grandchild2.assertRemoved(1)

        root.add(pa.brother)
        pa.grandchild1.assertAdded(2)
        pa.grandchild2.assertAdded(2)

        root.add(pa)
        pa.assertAdded(2)
        pa.grandchild1.assertAdded(3)
        pa.grandchild2.assertAdded(3)

        root.remove(pa)
        pa.assertRemoved(2)
        pa.grandchild1.assertAdded(4)
        pa.grandchild2.assertAdded(4)
    }

    companion object {
        // static {
        //     JavaPlatform.Config config = new JavaPlatform.Config();
        //     config.headless = true;
        //     JavaPlatform.register(config);
        // }

        var stub: Platform = StubPlatform()
    }
}
