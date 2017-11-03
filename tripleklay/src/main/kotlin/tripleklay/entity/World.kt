package tripleklay.entity

import klay.core.Clock
import klay.core.assert
import react.Closeable
import react.Signal
import tripleklay.util.Bag
import tripleklay.util.BitVec

/**
 * A collection of entities and systems. A world is completely self-contained, so it would be
 * possible to have multiple separate worlds running simultaneously, though this would be uncommon.
 */
open class World : Iterable<Entity> {
    /** A signal emitted when an entity is added to this world.  */
    val entityAdded = Signal<Entity>()

    /** A signal emitted when an entity in this world has changed (usually this means components
     * have been added to or removed from the entity).  */
    val entityChanged = Signal<Entity>()

    /** A signal emitted when an entity in this world which was disabled becomes enabled.  */
    val entityEnabled = Signal<Entity>()

    /** A signal emitted when an entity in this world which was enabled becomes disabled.  */
    val entityDisabled = Signal<Entity>()

    /** A signal emitted when an entity is removed from the world. This happens when an entity is
     * disabled, as well as when it is destroyed.  */
    val entityRemoved = Signal<Entity>()

    /** Connects this world to the supplied `update` and `paint` signals.
     * @return an object that can be used to disconnect both connections.
     */
    fun connect(update: Signal<Clock>, paint: Signal<Clock>): Closeable {
        return Closeable.Util.join(
                update.connect({ clk -> update(clk) }),
                paint.connect({ clk -> paint(clk) }))
    }

    /** Creates and returns an entity. The entity may actually be obtained from a pool of free
     * entities to avoid unnecessary garbage generation.
     * @param enabled whether the entity should be enabled by default. If it is enabled, it will
     * * automatically be queued for addition to the world. If it is not enabled, it will remain
     * * dormant until [Entity.setEnabled] is used to enable it.
     */
    fun create(enabled: Boolean): Entity {
        val e: Entity
        if (_ids.isEmpty()) {
            e = create(genEntityId())
        } else {
            e = _entities[_ids.removeLast()]!!
            e.reset()
        }
        if (enabled) e.isEnabled = true
        return e
    }

    /** Creates an entity with the specified id and component bitvec. This is only used when
     * restoring entities from persistent storage. Use [.create] to create new entities.
     */
    fun restore(id: Int, components: BitVec): Entity {
        val ent = create(id)
        // init the components directly to avoid extra checking done by Entity.add(Component)
        ent.comps.set(components)
        var ii = 0
        val ll = _comps.size
        while (ii < ll) {
            if (components.isSet(ii)) _comps[ii].add(ent)
            ii++
        }
        return ent
    }

    /** Returns the entity with the specified id. Note: this method is optimized for speed, which
     * means that passing an invalid/unused entity id to this method may return a destroyed entity
     * or it may throw an exception.
     */
    fun entity(id: Int): Entity {
        return _entities[id]!!
    }

    /** Returns an iterator over all entities in the world. [Iterator.remove] is not
     * implemented for this iterator.
     */
    fun entities(): Iterator<Entity> {
        return object : Iterator<Entity> {
            override fun hasNext(): Boolean {
                return _nextIdx >= 0
            }

            override fun next(): Entity {
                val next = _entities[_nextIdx]!!
                _nextIdx = findNext(_nextIdx + 1)
                return next
            }

            protected fun findNext(idx: Int): Int {
                var idx = idx
                while (idx < _entities.size) {
                    val e = _entities[idx]
                    if (e != null && !e.isDisposed) return idx
                    idx++
                }
                return -1
            }

            protected var _nextIdx = findNext(0)
        }
    }

    /** Updates all of the [System]s in this world. The systems will likely in turn update
     * the components of registered [Entity]s.  */
    open fun update(clock: Clock) {
        // init any to-be-initted systems (before we add to-be-added entities)
        for (ii in _toInit.size() - 1 downTo 0) {
            val sys = _toInit.removeLast()
            for (ent in _entities) {
                // skip non-existent or disabled entities
                if (ent == null || !ent.isEnabled) continue
                // if the entity is already added tell the new system about it, otherwise it is in
                // the toAdd list and we'll tell all systems about it in the next block
                if (ent.isAdded) sys.entityAdded(ent)
            }
        }

        // process any pending entity additions
        for (ii in toAdd.size() - 1 downTo 0) {
            val entity = toAdd.removeLast()
            // we note that the entity is added before passing it through the systems, both so that
            // they see an added entity and so that if any of those systems then change the
            // entity's components, that triggers the entity to be queued up as changed again;
            // otherwise a system half-way through the list could change things and the systems in
            // the first half of the list would no longer be aware of the entity's real state
            entity.noteAdded()
            var ss = 0
            val ll = _systems.size
            while (ss < ll) {
                _systems[ss].entityAdded(entity)
                ss++
            }
            entityAdded.emit(entity)
        }

        // process any pending entity changes
        for (ii in toChange.size() - 1 downTo 0) {
            val entity = toChange.removeLast()
            // we clear the entity's changing flag before passing it through the systems, so that
            // if any of those systems then change the entity's components yet further, that
            // triggers the entity to be queued up as changed again; otherwise a system half-way
            // through the list could change things and the systems in the first half of the list
            // would no longer be aware of the entity's real state
            entity.clearChanging()
            var ss = 0
            val ll = _systems.size
            while (ss < ll) {
                _systems[ss].entityChanged(entity)
                ss++
            }
            entityChanged.emit(entity)
        }

        // process any pending entity removals
        for (ii in toRemove.size() - 1 downTo 0) {
            val entity = toRemove.removeLast()
            run {
                var ss = 0
                val ll = _systems.size
                while (ss < ll) {
                    _systems[ss].entityRemoved(entity)
                    ss++
                }
            }
            entityRemoved.emit(entity)
            // if the entity is destroyed, remove its components and return it to the pool
            if (entity.isDisposed) {
                var cc = 0
                val ll = _comps.size
                while (cc < ll) {
                    if (entity.comps.isSet(cc)) _comps[cc].remove(entity)
                    cc++
                }
                _ids.add(entity.id)
            }
        }

        // and finally update all of our systems
        var ii = 0
        val ll = _systems.size
        while (ii < ll) {
            _systems[ii].update(clock)
            ii++
        }
    }

    /** Paints all of the [System]s in this world.  */
    open fun paint(clock: Clock) {
        var ii = 0
        val ll = _systems.size
        while (ii < ll) {
            _systems[ii].paint(clock)
            ii++
        }
    }

    // from interface Iterable<Entity>
    override fun iterator(): Iterator<Entity> {
        return entities()
    }

    /** Registers `system` with this world.
     * @return a unique index assigned to the system for use in bitmasks.
     */
    internal fun register(system: System): Int {
        var idx = 0 // insert the system based on its priority
        for (ii in _systems.indices.reversed()) {
            if (_systems[ii].priority >= system.priority) {
                idx = ii + 1
                break
            }
        }
        _systems.add(idx, system)
        _toInit.add(system) // tell it about existing entities on the next update
        return _systems.size - 1
    }

    /** Registers `component` with this world.
     * @return a unique index assigned to the component for use in bitmasks.
     */
    internal fun register(component: Component): Int {
        _comps.add(component)
        return _comps.size - 1
    }

    // Entity will add itself to the appropriate set as needed
    internal val toAdd = Bag<Entity>()
    internal val toChange = Bag<Entity>()
    internal val toRemove = Bag<Entity>()

    protected fun genEntityId(): Int {
        return _nextEntityId++
    }

    protected fun create(id: Int): Entity {
        if (_entities.size <= id) {
            _entities = _entities.copyOf(_entities.size * 2)
        }
        assert(_entities[id] == null) { "Entity already exists with id " + id }
        _entities[id] = Entity(this, id)
        return _entities[id]!!
    }

    protected fun components(ent: Entity): BitVec {
        return ent.comps
    }

    // Systems that need to be initted on the next update
    protected val _toInit = Bag<System>()

    protected val _systems = ArrayList<System>()
    protected val _comps = ArrayList<Component>()

    /** Ids of allocated entities that have been removed and may be reused. */
    protected val _ids = IntBag()
    protected var _entities = arrayOfNulls<Entity>(64)
    protected var _nextEntityId = 1
}
