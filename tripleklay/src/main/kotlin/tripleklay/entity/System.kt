package tripleklay.entity

import klay.core.Clock
import klay.core.PaintClock

/**
 * Handles a single concern in an entity-based game. That might be processing collisions, or
 * updating entity's logical positions, or regenerating health, etc. A system operates on all
 * entities which meet its criteria on a given tick. See [isInterested] for an explanation
 * of how to choose which entities on which to operate.
 */
abstract class System
/** Creates a new system.
 * It shall be registered with a world using [World.register].
 * @param priorty this system's priority with respect to other systems. Systems with higher
 * * priority will be notified of entity addition/removal and will be processed before systems
 * * with lower priority. Systems with the same priority will be processed in unspecified order.
 * * When order matters, use priority, don't rely on the order you register systems with the
 * * world.
 */
protected constructor(
        /** This system's priority with respect to other systems. See [System].  */
        internal val priority: Int = 0) {
    /** Provides a way to iterate over this system's active entities.  */
    interface Entities {
        /** Returns the size of the active entities set.  */
        fun size(): Int

        /** Returns the entity at `index`. Entities are arbitrarily ordered.  */
        operator fun get(index: Int): Int
    }

    /** Our active entities.  */
    protected val _active = IntBag()

    /** Whether or not this system is enabled.  */
    private var _enabled = true

    /** Enables or disables this system. When a system is disabled, it will not be processed every
     * frame. However, it will still be checked for entity interest and be notified when entities
     * are added to and removed from its active set.
     */
    fun setEnabled(enabled: Boolean) {
        _enabled = enabled
    }

    /** Returns the number of active entities in this system.  */
    fun entityCount(): Int {
        return _active.size()
    }

    /** Returns the id of the `ii`th active entity in this system.  */
    fun entityId(ii: Int): Int {
        return _active.get(ii)
    }

    /** Called when an entity is added to our world (or an already added entity is changed) which
     * matches this system's criteria. This entity will subsequently be processed by this system
     * until it is removed from the world or no longer matches our criteria.
     */
    protected open fun wasAdded(entity: Entity) {}

    /**
     * Called when an entity that was previously in our active set is removed from the world or is
     * changed such that it no longer matches our criteria.

     * @param index the index in the [_active] bag from which the entity was removed. This
     * * makes it more efficient for a derived class to keep a parallel bag containing the entities
     * * themselves, if needed.
     */
    protected open fun wasRemoved(entity: Entity, index: Int) {}

    /**
     * Processes this system's active entities. This is where each entity's simulation state would
     * be updated. This is not called if the system is disabled.
     */
    protected open fun update(clock: Clock, entities: Entities) {}

    /**
     * Paints this system's active entities. This should only be used to perform interpolation on
     * values computed during [update]. Entities *must not* be added,
     * changed or removed during this call. This is not called if the system is disabled.
     */
    protected open fun paint(clock: PaintClock, entities: Entities) {}

    /**
     * Indicates whether this system is "interested" in this entity. A system will process all
     * entries in which it is interested, every tick. As entities are added to the world or changed
     * while added to the world, all systems will be checked for interest in the entity. Generally
     * a system will be interested in entities that have a particular combination of components,
     * but any criteria are allowed as long as any change to the criteria (on active entities) are
     * accompanied by a call to [Entity.didChange] so that all systems may be rechecked for
     * interest in the entity. Note that `didChange` is called automatically when components
     * are added to or removed from an entity.
     */
    protected abstract fun isInterested(entity: Entity): Boolean

    internal fun entityAdded(systemId: Int, entity: Entity) {
        if (isInterested(entity)) addEntity(systemId, entity)
    }

    internal fun entityChanged(systemId: Int, entity: Entity) {
        val wasAdded = entity.systems.isSet(systemId)
        val haveInterest = isInterested(entity)
        if (haveInterest && !wasAdded) addEntity(systemId, entity)
        else if (!haveInterest && wasAdded) removeEntity(systemId, entity)
    }

    internal fun entityRemoved(systemId: Int, entity: Entity) {
        if (entity.systems.isSet(systemId)) removeEntity(systemId, entity)
    }

    internal fun update(clock: Clock) {
        if (!_enabled) return
        update(clock, _active)
    }

    internal fun paint(clock: PaintClock) {
        if (!_enabled) return
        paint(clock, _active)
    }

    private fun addEntity(systemId: Int, entity: Entity) {
        _active.add(entity.id)
        entity.systems.set(systemId)
        wasAdded(entity)
    }

    private fun removeEntity(systemId: Int, entity: Entity) {
        // TODO: this is O(N), would be nice if it was O(log N) or O(1)
        val idx = _active.remove(entity.id)
        entity.systems.clear(systemId)
        wasRemoved(entity, idx)
    }
}
