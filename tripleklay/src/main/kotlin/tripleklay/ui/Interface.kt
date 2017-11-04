package tripleklay.ui

import klay.core.Clock
import klay.core.PaintClock
import klay.core.Platform
import klay.scene.GroupLayer
import react.Closeable
import react.Signal
import tripleklay.anim.Animator

/**
 * The main class that integrates the TripleKlay UI with a PlayN game. This class is mainly
 * necessary to automatically validate hierarchies of `Element`s during each paint.
 * Create an interface instance, create [Root] groups via the interface and add the
 * [Root.layer]s into your scene graph wherever you desire.
 */
class Interface
/** Creates an interface for `plat`. The interface will be connected to `frame` to
 * drive any per-frame animations and activity. Either provide a frame signal whose lifetime
 * is the same as the interface (for example `Screen.paint`), or call [.close]
 * when this interface should be disconnected from the frame signal.  */
(
        /** The platform in which this interface is operating.  */
        val plat: Platform,
        /** A signal emitted just before we render a frame.  */
        val frame: Signal<PaintClock>) : Closeable {

    /** An animator that can be used to animate things in this interface.  */
    val anim = Animator()

    private val _onFrame: Closeable
    private val _roots: MutableList<Root> = ArrayList()

    init {
        _onFrame = Closeable.Util.join(
                frame.connect({ paint(it) }),
                frame.connect(anim.onPaint))
    }

    override fun close() {
        _onFrame.close()
    }

    /** Returns an iterable over the current roots. Don't delete from this iterable!  */
    fun roots(): Iterable<Root> {
        return _roots
    }

    /**
     * Creates a root element with the specified layout and stylesheet.
     */
    fun createRoot(layout: Layout, sheet: Stylesheet): Root {
        return addRoot(Root(this, layout, sheet))
    }

    /**
     * Creates a root element with the specified layout and stylesheet and adds its layer to the
     * specified parent.
     */
    fun createRoot(layout: Layout, sheet: Stylesheet, parent: GroupLayer): Root {
        val root = createRoot(layout, sheet)
        parent.add(root.layer)
        return root
    }

    /**
     * Adds a root to this interface. The root must have been created with this interface and not
     * be added to any other interfaces. Generally you should use [.createRoot], but this
     * method is exposed for callers with special needs.
     */
    fun <R : Root> addRoot(root: R): R {
        _roots.add(root)
        return root
    }

    /**
     * Removes the supplied root element from this interface, iff it's currently added. If the
     * root's layer has a parent, the layer will be removed from the parent as well. This leaves
     * the Root's layer in existence, so it may be used again. If you're done with the Root and all
     * of the elements inside of it, call [.disposeRoot] to free its resources.

     * @return true if the root was removed, false if it was not currently added.
     */
    fun removeRoot(root: Root): Boolean {
        if (!_roots.remove(root)) return false
        root.wasRemoved()
        if (root.layer.parent() != null) root.layer.parent()!!.remove(root.layer)
        return true
    }

    /**
     * Removes the supplied root element from this interface and disposes its layer, iff it's
     * currently added. Disposing the layer disposes the layers of all elements contained in the
     * root as well. Use this method if you're done with the Root. If you'd like to reuse it, call
     * [.removeRoot] instead.

     * @return true if the root was removed and disposed, false if it was not currently added.
     */
    fun disposeRoot(root: Root): Boolean {
        if (!_roots.remove(root)) return false
        root.set(Element.Flag.WILL_DISPOSE, true)
        root.wasRemoved()
        root.layer.close()
        return true
    }

    /**
     * Removes and disposes all roots in this interface.
     */
    fun disposeRoots() {
        while (!_roots.isEmpty()) disposeRoot(_roots[0])
    }

    fun paint(clock: Clock) {
        // ensure that our roots are validated
        var ii = 0
        val ll = _roots.size
        while (ii < ll) {
            _roots[ii].validate()
            ii++
        }
    }
}
