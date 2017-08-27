package tripleklay.entity

import react.Closeable
import tripleklay.util.BitVec

/**
 * Tracks the state of a single entity. This includes its enabled state, as well as the components
 * which are attached to this entity.
 */
class Entity
/** Creates an entity in the specified world, initializes it with the supplied set of
 * components and queues it to be added to the world on the next update.
 */
(
        /** The world to which this entity belongs.  */
        val world: World,
        /** This entity's unique id. This id will be valid for as long as the entity remains alive.
         * Once [.close] is called, this id may be reused by a new entity.  */
        val id: Int) : Closeable {

    /** Returns whether this entity has been disposed.  */
    val isDisposed: Boolean
        get() = _flags and DISPOSED != 0

    /** Returns whether this entity is currently enabled.  */
    /** Enables or disables this entity. When an entity is disabled, it is removed from all systems
     * in which it is currently an active participant (prior to the next update). When it is
     * re-enabled, it is added back to all systems that are interested in it (prior to the next
     * update).
     */
    var isEnabled: Boolean
        get() = _flags and (ENABLED or DISPOSED) == ENABLED
        set(enabled) {
            checkDisposed("Cannot modify disposed entity.")
            val isEnabled = _flags and ENABLED != 0
            if (isEnabled && !enabled) {
                _flags = _flags and ENABLED.inv()
                world.toRemove.add(this)
            } else if (!isEnabled && enabled) {
                _flags = _flags or ENABLED
                world.toAdd.add(this)
            }
        }

    /** Returns true if this entity has the component `comp`, false otherwise.  */
    fun has(comp: Component): Boolean {
        return comps.isSet(comp.id)
    }

    /** Adds the specified component to this entity. This will queue the component up to be added
     * or removed to appropriate systems on the next update.

     * @return this entity for call chaining.
     */
    fun add(comp: Component): Entity {
        checkDisposed("Cannot add components to disposed entity.")
        comp.add(this)
        queueChange()
        return this
    }

    /** Adds the specified components to this entity. This will queue the component up to be added
     * or removed to appropriate systems on the next update. *Note:* this method uses varags
     * and thus creates an array every time it is called. If you are striving to eliminate all
     * unnecessary garbage generation, use repeated calls to [.add], or [ ][.add] with a pre-allocated array.

     * @return this entity for call chaining.
     */
    fun add(c1: Component, c2: Component, vararg rest: Component): Entity {
        checkDisposed("Cannot add components to disposed entity.")
        c1.add(this)
        c2.add(this)
        for (cn in rest) cn.add(this)
        queueChange()
        return this
    }

    /** Adds the supplied components to this entity. This will queue the component up to be added
     * or removed to appropriate systems on the next update. This avoids the garbage generation of
     * the varags `add` method, and is slightly more efficient than a sequence of calls to
     * [.add]. The expectation is that you would keep an array around with the
     * components for a particular kind of entity, like so:

     * <pre>`Entity createFoo (...) {
     * Entity foo = create(true).add(FOO_COMPS);
     * // init foo...
     * return foo;
     * }
     * private static final Component[] FOO_COMPS = { pos, vel, etc. };
    `</pre> *

     * @return this entity for call chaining.
     */
    fun add(comps: Array<Component>): Entity {
        checkDisposed("Cannot add components to disposed entity.")
        for (comp in comps) comp.add(this)
        queueChange()
        return this
    }

    /** Removes the specified component from this entity. This will queue the component up to be
     * added or removed to appropriate systems on the next update.
     */
    fun remove(comp: Component): Entity {
        checkDisposed("Cannot remove components from disposed entity.")
        comp.remove(this)
        queueChange()
        return this
    }

    /** Disposes this entity, causing it to be removed from the world on the next update.  */
    fun dispose() {
        if (!isDisposed) {
            _flags = _flags or DISPOSED
            world.toRemove.add(this)
        }
    }

    /** An alias for [.dispose]. Needed to implement [Closeable].  */
    override fun close() {
        dispose()
    }

    /** Indicates that this entity has changed, and causes it to be reconsidered for inclusion or
     * exclusion from systems on the next update. This need not be called when adding or removing
     * components, and should only be called if some other external circumstance changes that
     * requires recalculation of which systems are interested in this entity.
     */
    fun didChange() {
        checkDisposed("Cannot didChange disposed entity.")
        queueChange()
    }

    override fun toString(): String {
        return "[id=" + id + ", sys=" + systems + ", comps=" + comps +
                ", flags=" + Integer.toBinaryString(_flags) + "]"
    }

    protected fun queueChange() {
        // if we're not yet added, we can stop now because we'll be processed when added; if we're
        // not currently enabled, we can stop now and we'll be processed when re-enabled
        if (_flags and (ADDED or ENABLED) == 0) return
        // if we're already queued up as changing, we've got nothing to do either
        if (_flags and CHANGING != 0) return
        // otherwise, mark ourselves as changing and queue on up
        _flags = _flags or CHANGING
        world.toChange.add(this)
    }

    protected fun checkDisposed(error: String) {
        if (_flags and DISPOSED != 0) throw IllegalStateException(error)
    }

    internal val isAdded: Boolean
        get() = _flags and ADDED != 0

    internal fun noteAdded() {
        _flags = _flags or ADDED
    }

    internal fun clearChanging() {
        _flags = _flags and CHANGING.inv()
    }

    internal fun reset() {
        _flags = 0
    }

    /** A bit mask indicating which systems are interested in this entity.  */
    internal val systems = BitVec(2)

    /** A bit mask indicating which components are possessed by this entity.  */
    internal val comps = BitVec(2)

    /** Flags pertaining to this entity's state.  */
    protected var _flags: Int = 0

    companion object {

        protected val ENABLED = 1 shl 0
        protected val DISPOSED = 1 shl 1
        protected val ADDED = 1 shl 2
        protected val CHANGING = 1 shl 3
    }
}
