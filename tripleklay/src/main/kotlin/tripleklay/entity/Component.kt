package tripleklay.entity

import pythagoras.f.Dimension
import pythagoras.f.Point
import pythagoras.f.Vector
import pythagoras.system.arrayCopy

/**
 * A component contains the data for a single aspect of an entity. This might be its position in a
 * 2D space, or its animation state, or any other piece of data that evolves as the entity exists
 * in the world.
 *
 * A `Component` instance contains the data for *all* entities that possess the
 * component in question (in a sparse array). This enables a data-driven approach to entity
 * processing where a system can process one or more components for its active entities with a
 * cache-friendly memory access pattern.
 */
abstract class Component protected constructor(
        /** The world in which this component exists.  */
        val world: World) {

    /** A component implementation for arbitrary objects.  */
    class Generic<T>(world: World) : Component(world) {

        /** Returns the value of this component for `entityId`.  */
        operator fun get(entityId: Int): T {
            val block = _blocks[entityId / BLOCK]!!
            val value = block[entityId % BLOCK] as T
            return value
        }

        /** Updates the value of this component for `entityId`.  */
        operator fun set(entityId: Int, value: T) {
            _blocks[entityId / BLOCK]!![entityId % BLOCK] = value
        }

        override fun init(entityId: Int) {
            val blockIdx = entityId / BLOCK
            if (blockIdx >= _blocks.size) {
                _blocks = _blocks.copyOf(_blocks.size * 2)
            }
            if (_blocks[blockIdx] == null) _blocks[blockIdx] = arrayOfNulls<Any>(BLOCK)
        }

        override fun clear(entityId: Int) {
            val block = _blocks[entityId / BLOCK]!!
            val idx = entityId % BLOCK
            val prev = block[idx] as T
            block[idx] = null
            release(prev)
        }

        /** Releases an instance of this component for an entity that is no longer using it. The
         * default does nothing (assuming the object will be garbage collected), but if the
         * component was set to an object fetched from a pool, this is a convenient place to return
         * it to the pool.  */
        protected fun release(value: T) {}

        protected var _blocks = arrayOfNulls<Array<Any?>>(INDEX_BLOCKS)
    }

    /** A component implementation for a single scalar `int`.  */
    class IScalar(world: World) : Component(world) {

        /** Returns the value of this component for `entityId`.  */
        operator fun get(entityId: Int): Int {
            return _blocks[entityId / BLOCK]!![entityId % BLOCK]
        }

        /** Updates the value of this component for `entityId`.  */
        operator fun set(entityId: Int, value: Int) {
            _blocks[entityId / BLOCK]!![entityId % BLOCK] = value
        }

        /** Adds `dv` to the value of this component for `entityId`.  */
        fun add(entityId: Int, dv: Int) {
            _blocks[entityId / BLOCK]!![entityId % BLOCK] += dv
        }

        override fun init(entityId: Int) {
            val blockIdx = entityId / BLOCK
            if (blockIdx >= _blocks.size) {
                val blocks = arrayOfNulls<IntArray>(_blocks.size * 2)
                arrayCopy(_blocks, 0, blocks, 0, _blocks.size)
                _blocks = blocks
            }
            if (_blocks[blockIdx] == null) _blocks[blockIdx] = IntArray(BLOCK)
        }

        private var _blocks = arrayOfNulls<IntArray>(INDEX_BLOCKS)
    }

    /** A component implementation for a single scalar `float`.  */
    class FScalar(world: World) : Component(world) {

        /** Returns the value of this component for `entityId`.  */
        operator fun get(entityId: Int): Float {
            return _blocks[entityId / BLOCK]!![entityId % BLOCK]
        }

        /** Updates the value of this component for `entityId`.  */
        operator fun set(entityId: Int, value: Float) {
            _blocks[entityId / BLOCK]!![entityId % BLOCK] = value
        }

        /** Adds `dv` to the value of this component for `entityId`.  */
        fun add(entityId: Int, dv: Float) {
            _blocks[entityId / BLOCK]!![entityId % BLOCK] += dv
        }

        override fun init(entityId: Int) {
            val blockIdx = entityId / BLOCK
            if (blockIdx >= _blocks.size) {
                val blocks = arrayOfNulls<FloatArray>(_blocks.size * 2)
                arrayCopy(_blocks, 0, blocks, 0, _blocks.size)
                _blocks = blocks
            }
            if (_blocks[blockIdx] == null) _blocks[blockIdx] = FloatArray(BLOCK)
        }

        private var _blocks = arrayOfNulls<FloatArray>(INDEX_BLOCKS)
    }

    /** A component implementation for a pair of `int`s.  */
    class IXY(world: World) : Component(world) {

        /** Returns the x component of the point for `entityId`.  */
        fun getX(entityId: Int): Int {
            return _blocks[entityId / BLOCK]!![2 * (entityId % BLOCK)]
        }

        /** Returns the y component of the point for `entityId`.  */
        fun getY(entityId: Int): Int {
            return _blocks[entityId / BLOCK]!![2 * (entityId % BLOCK) + 1]
        }

        /** Writes the x/y components of the point for `entityId` into `into`.
         * @return into for easy method chaining.
         */
        fun get(entityId: Int, into: pythagoras.i.Point): pythagoras.i.Point {
            val block = _blocks[entityId / BLOCK]!!
            val idx = 2 * (entityId % BLOCK)
            into.x = block[idx]
            into.y = block[idx + 1]
            return into
        }

        /** Writes the x/y components of the point for `entityId` into `into`.
         * @return into for easy method chaining.
         */
        fun get(entityId: Int, into: pythagoras.i.Dimension): pythagoras.i.Dimension {
            val block = _blocks[entityId / BLOCK]!!
            val idx = 2 * (entityId % BLOCK)
            into.width = block[idx]
            into.height = block[idx + 1]
            return into
        }

        /** Updates the x component of the point for `entityId`.  */
        fun setX(entityId: Int, x: Int) {
            _blocks[entityId / BLOCK]!![2 * (entityId % BLOCK)] = x
        }

        /** Updates the y component of the point for `entityId`.  */
        fun setY(entityId: Int, y: Int) {
            _blocks[entityId / BLOCK]!![2 * (entityId % BLOCK) + 1] = y
        }

        /** Updates the x/y components of the point for `entityId`.  */
        operator fun set(entityId: Int, value: pythagoras.i.Point) {
            set(entityId, value.x, value.y)
        }

        /** Updates the x/y components of the point for `entityId`.  */
        fun set(entityId: Int, x: Int, y: Int) {
            val block = _blocks[entityId / BLOCK]!!
            val idx = 2 * (entityId % BLOCK)
            block[idx] = x
            block[idx + 1] = y
        }

        /** Copies the value of `other` for `entityId` to this component.  */
        operator fun set(entityId: Int, other: IXY) {
            val blockIdx = entityId / BLOCK
            val idx = 2 * (entityId % BLOCK)
            val oblock = other._blocks[blockIdx]!!
            val block = _blocks[blockIdx]!!
            block[idx] = oblock[idx]
            block[idx + 1] = oblock[idx + 1]
        }

        /** Adds `dx` and `dy` to the x and y components for `entityId`.  */
        fun add(entityId: Int, dx: Int, dy: Int) {
            val block = _blocks[entityId / BLOCK]!!
            val idx = 2 * (entityId % BLOCK)
            block[idx] += dx
            block[idx + 1] += dy
        }

        override fun init(entityId: Int) {
            val blockIdx = entityId / BLOCK
            if (blockIdx >= _blocks.size) {
                val blocks = arrayOfNulls<IntArray>(_blocks.size * 2)
                arrayCopy(_blocks, 0, blocks, 0, _blocks.size)
                _blocks = blocks
            }
            if (_blocks[blockIdx] == null) _blocks[blockIdx] = IntArray(2 * BLOCK)
        }

        private var _blocks: Array<IntArray?> = arrayOfNulls(INDEX_BLOCKS)
    }

    /** A component implementation for a pair of `float`s.  */
    class FXY(world: World) : Component(world) {

        /** Returns the x component of the point for `entityId`.  */
        fun getX(entityId: Int): Float {
            return _blocks[entityId / BLOCK]!![2 * (entityId % BLOCK)]
        }

        /** Returns the y component of the point for `entityId`.  */
        fun getY(entityId: Int): Float {
            return _blocks[entityId / BLOCK]!![2 * (entityId % BLOCK) + 1]
        }

        /** Writes the x/y components of the point for `entityId` into `into`.
         * @return into for easy method chaining.
         */
        fun get(entityId: Int, into: Point): Point {
            val block = _blocks[entityId / BLOCK]!!
            val idx = 2 * (entityId % BLOCK)
            into.x = block[idx]
            into.y = block[idx + 1]
            return into
        }

        /** Writes the x/y components of the point for `entityId` into `into`.
         * @return into for easy method chaining.
         */
        fun get(entityId: Int, into: Vector): Vector {
            val block = _blocks[entityId / BLOCK]!!
            val idx = 2 * (entityId % BLOCK)
            into.x = block[idx]
            into.y = block[idx + 1]
            return into
        }

        /** Writes the x/y components of the point for `entityId` into `into`.
         * @return into for easy method chaining.
         */
        fun get(entityId: Int, into: Dimension): Dimension {
            val block = _blocks[entityId / BLOCK]!!
            val idx = 2 * (entityId % BLOCK)
            into.width = block[idx]
            into.height = block[idx + 1]
            return into
        }

        /** Updates the x component of the point for `entityId`.  */
        fun setX(entityId: Int, x: Float) {
            _blocks[entityId / BLOCK]!![2 * (entityId % BLOCK)] = x
        }

        /** Updates the y component of the point for `entityId`.  */
        fun setY(entityId: Int, y: Float) {
            _blocks[entityId / BLOCK]!![2 * (entityId % BLOCK) + 1] = y
        }

        /** Updates the x/y components of the point for `entityId`.  */
        operator fun set(entityId: Int, value: pythagoras.f.XY) {
            set(entityId, value.x, value.y)
        }

        /** Updates the x/y components of the point for `entityId`.  */
        fun set(entityId: Int, x: Float, y: Float) {
            val block = _blocks[entityId / BLOCK]!!
            val idx = 2 * (entityId % BLOCK)
            block[idx] = x
            block[idx + 1] = y
        }

        /** Copies the value of `other` for `entityId` to this component.  */
        operator fun set(entityId: Int, other: Component.FXY) {
            val blockIdx = entityId / BLOCK
            val idx = 2 * (entityId % BLOCK)
            val oblock = other._blocks[blockIdx]!!
            val block = _blocks[blockIdx]!!
            block[idx] = oblock[idx]
            block[idx + 1] = oblock[idx + 1]
        }

        /** Adds `dx` and `dy` to the x and y components for `entityId`.  */
        fun add(entityId: Int, dx: Float, dy: Float) {
            val block = _blocks[entityId / BLOCK]!!
            val idx = 2 * (entityId % BLOCK)
            block[idx] += dx
            block[idx + 1] += dy
        }

        override fun init(entityId: Int) {
            val blockIdx = entityId / BLOCK
            if (blockIdx >= _blocks.size) {
                val blocks = arrayOfNulls<FloatArray>(_blocks.size * 2)
                arrayCopy(_blocks, 0, blocks, 0, _blocks.size)
                _blocks = blocks
            }
            if (_blocks[blockIdx] == null) _blocks[blockIdx] = FloatArray(2 * BLOCK)
        }

        private var _blocks = arrayOfNulls<FloatArray>(INDEX_BLOCKS)
    }

    /** A component implementation for a single `int` bit mask.  */
    class IMask(world: World) : Component(world) {

        /** Returns the value of this component for `entityId`.  */
        operator fun get(entityId: Int): Int {
            return _blocks[entityId / BLOCK]!![entityId % BLOCK]
        }

        /** Updates the entire mask for `entityId`.  */
        operator fun set(entityId: Int, value: Int) {
            _blocks[entityId / BLOCK]!![entityId % BLOCK] = value
        }

        /** Sets the mask for `entityId` to `current & mask`.  */
        fun setAnd(entityId: Int, mask: Int) {
            _blocks[entityId / BLOCK]!![entityId % BLOCK] = _blocks[entityId / BLOCK]!![entityId % BLOCK] and mask
        }

        /** Sets the mask for `entityId` to `current | mask`.  */
        fun setOr(entityId: Int, mask: Int) {
            _blocks[entityId / BLOCK]!![entityId % BLOCK] = _blocks[entityId / BLOCK]!![entityId % BLOCK] or mask
        }

        /** Returns whether `flag` is set in this mask.
         * @param flag an integer with the appropriate flag bit set.
         */
        fun isSet(entityId: Int, flag: Int): Boolean {
            return get(entityId) and flag != 0
        }

        /** Sets `flag` in the mask for `entityId`.
         * @param flag an integer with the appropriate flag bit set.
         */
        fun setFlag(entityId: Int, flag: Int) {
            setOr(entityId, flag)
        }

        /** Clears `flag` from the mask for `entityId`.
         * @param flag an integer with the appropriate flag bit set.
         */
        fun clearFlag(entityId: Int, flag: Int) {
            setAnd(entityId, flag.inv())
        }

        override fun init(entityId: Int) {
            val blockIdx = entityId / BLOCK
            if (blockIdx >= _blocks.size) {
                val blocks = arrayOfNulls<IntArray>(_blocks.size * 2)
                arrayCopy(_blocks, 0, blocks, 0, _blocks.size)
                _blocks = blocks
            }
            if (_blocks[blockIdx] == null) _blocks[blockIdx] = IntArray(BLOCK)
        }

        private var _blocks = arrayOfNulls<IntArray>(INDEX_BLOCKS)
    }

    /** This component's unique id (used in bit masks).  */
    internal val id: Int = world.register(this)

    override fun toString(): String {
        return this::class.simpleName + "#" + id
    }

    /** Ensures that space is allocated for the component at `index`.  */
    protected abstract fun init(index: Int)

    /** Clears the value of the component at `index`.  */
    protected open fun clear(index: Int) {} // noop by default

    internal fun add(entity: Entity) {
        entity.comps.set(id)
        init(entity.id)
    }

    internal fun remove(entity: Entity) {
        entity.comps.clear(id)
        clear(entity.id)
    }

    companion object {

        /** The number of components in a single block.  */
        protected val BLOCK = 256

        /** The number of index blocks to allocate by default.  */
        protected val INDEX_BLOCKS = 32
    }
}
